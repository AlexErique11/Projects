# train_model.py
import os
import chess
import chess.pgn
import pandas as pd
import zstandard as zstd
import io
import xgboost as xgb
import json
from tqdm import tqdm
from feature_extraction import compute_features
from sklearn.metrics import mean_squared_error, r2_score
from sklearn.model_selection import train_test_split
import matplotlib.pyplot as plt

# --- Paths ---
STOCKFISH_PATH = r"C:\Users\alexa\Downloads\stockfish\stockfish-windows-x86-64-avx2.exe"
SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
DATA_PATH = os.path.join(SCRIPT_DIR, "data", "lichess_data.zst")
MODEL_PATH = os.path.join(SCRIPT_DIR, "human_playability_model.json")
FEATURES_CSV = os.path.join(SCRIPT_DIR, "features.csv")

# --- Load already processed features ---
if os.path.exists(FEATURES_CSV):
    df_features = pd.read_csv(FEATURES_CSV)
    if "game_number" in df_features.columns:
        df_features["game_number"] = pd.to_numeric(df_features["game_number"], errors="coerce")
        processed_games = int(df_features["game_number"].max())
    else:
        processed_games = 0
else:
    df_features = pd.DataFrame()
    processed_games = 0

print(f"Already processed games: {processed_games}")

# --- Process new games ---
MAX_GAMES = 100
game_count = 0
positions = []

with chess.engine.SimpleEngine.popen_uci(STOCKFISH_PATH) as engine:
    with open(DATA_PATH, "rb") as f:
        dctx = zstd.ZstdDecompressor()
        stream = dctx.stream_reader(f)
        text_stream = io.TextIOWrapper(stream, encoding="utf-8")

        current_game_index = 0
        game = chess.pgn.read_game(text_stream)
        pbar = tqdm(total=MAX_GAMES, unit="game", ncols=100, desc="Processing games")

        while game is not None and game_count < MAX_GAMES:
            current_game_index += 1

            if current_game_index <= processed_games:
                game = chess.pgn.read_game(text_stream)
                continue

            board = game.board()
            game_positions = []

            for move in game.mainline_moves():
                features = compute_features(board, engine)
                features["human_move"] = move.uci()
                features["game_number"] = current_game_index
                game_positions.append(features)
                board.push(move)

            positions.extend(game_positions)
            game_count += 1
            pbar.update(1)
            game = chess.pgn.read_game(text_stream)

pbar.close()

# --- Append new features ---
if positions:
    df_new = pd.DataFrame(positions)
    df_new["evals_dict"] = df_new["evals_dict"].apply(json.dumps)

    if not df_features.empty:
        df_features = pd.concat([df_features, df_new], ignore_index=True)
    else:
        df_features = df_new

    df_features.to_csv(FEATURES_CSV, index=False)
    print(f"Features saved to {FEATURES_CSV}, total games now: {df_features['game_number'].max()}")

df_features["evals_dict"] = df_features["evals_dict"].apply(json.loads)

# --- Label function ---
def human_move_score(human_move, evals_dict):
    if not evals_dict:
        return 0.0
    best_eval = max(evals_dict.values())
    human_eval = evals_dict.get(human_move, min(evals_dict.values()))
    eval_range = best_eval - min(evals_dict.values())
    if eval_range == 0:
        return 1.0
    score = (human_eval - min(evals_dict.values())) / eval_range
    return float(score)

df_features["label_move_ease"] = [
    human_move_score(mv, ed) for mv, ed in zip(df_features["human_move"], df_features["evals_dict"])
]

# --- Features and target ---
feature_cols = [
    "volatility", "move_ease", "trap_susceptibility", "king_exposure", "castling_status",
    "defending_pieces", "pawn_structure", "doubled_pawns", "backward_pawns", "pawn_majority",
    "mobility", "piece_coordination", "rooks_connected", "bishop_pair", "overworked_defenders",
    "checks", "captures", "pins", "tactical_motifs", "material_imbalance", "phase",
    "space_control", "passed_pawns", "center_control"
]

X = df_features[feature_cols]
y = df_features["label_move_ease"]

# --- Train/Validation split ---
X_train, X_val, y_train, y_val = train_test_split(X, y, test_size=0.2, random_state=42)

dtrain = xgb.DMatrix(X_train, label=y_train)
dval = xgb.DMatrix(X_val, label=y_val)

params = {"objective": "reg:squarederror", "eval_metric": "rmse"}

num_boost_round = max(200, len(df_features) // 10)

print(f"Training model with {num_boost_round} rounds...")
model = xgb.train(
    params,
    dtrain,
    num_boost_round=num_boost_round,
    evals=[(dtrain, "train"), (dval, "validation")],
    early_stopping_rounds=10
)

model.save_model(MODEL_PATH)
print(f"Model saved to {MODEL_PATH}")

# --- Evaluation ---
y_pred_train = model.predict(dtrain)
y_pred_val = model.predict(dval)

mse_train = mean_squared_error(y_train, y_pred_train)
r2_train = r2_score(y_train, y_pred_train)

mse_val = mean_squared_error(y_val, y_pred_val)
r2_val = r2_score(y_val, y_pred_val)

print(f"Training MSE: {mse_train:.4f} | R²: {r2_train:.4f}")
print(f"Validation MSE: {mse_val:.4f} | R²: {r2_val:.4f}")

# --- Feature importance ---
xgb.plot_importance(model, max_num_features=20)
plt.tight_layout()
plt.show()
