# train_model.py
import os
import chess
import chess.pgn
import pandas as pd
import zstandard as zstd
import io
import xgboost as xgb
from sklearn.metrics import accuracy_score
import chess.engine
from tqdm import tqdm

from feature_extraction import compute_features

# --- Paths ---
STOCKFISH_PATH = r"C:\Users\alexa\Downloads\stockfish\stockfish-windows-x86-64-avx2.exe"
SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
DATA_PATH = os.path.join(SCRIPT_DIR, "data", "lichess_data.zst")
MODEL_PATH = os.path.join(SCRIPT_DIR, "human_playability_model.json")
FEATURES_CSV = os.path.join(SCRIPT_DIR, "features.csv")

# --- Load already processed features ---
if os.path.exists(FEATURES_CSV):
    df_features = pd.read_csv(FEATURES_CSV)
    if 'game_number' in df_features.columns:
        df_features['game_number'] = pd.to_numeric(df_features['game_number'], errors='coerce')
        processed_games = int(df_features['game_number'].max())
    else:
        processed_games = 0
else:
    df_features = pd.DataFrame()
    processed_games = 0

print(f"Already processed games: {processed_games}")

# --- Prepare storage ---
positions = []

# --- Read compressed .zst PGN ---
# --- Read compressed .zst PGN and process games ---
MAX_GAMES = 10  # number of new games to process
game_count = 0  # games processed in this run

with chess.engine.SimpleEngine.popen_uci(STOCKFISH_PATH) as engine:
    with open(DATA_PATH, "rb") as f:
        dctx = zstd.ZstdDecompressor()
        stream = dctx.stream_reader(f)
        text_stream = io.TextIOWrapper(stream, encoding="utf-8")

        current_game_index = 0
        game = chess.pgn.read_game(text_stream)
        pbar = tqdm(total=MAX_GAMES, unit="game", ncols=100, desc="Processing games")
        positions = []

        while game is not None and game_count < MAX_GAMES:
            current_game_index += 1

            # Skip already processed games
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
# --- Save features only once at the end ---
import json

if positions:
    df_new = pd.DataFrame(positions)

    # --- Ensure evals_dict is JSON string before saving ---
    df_new["evals_dict"] = df_new["evals_dict"].apply(json.dumps)

    if not df_features.empty:
        df_features = pd.concat([df_features, df_new], ignore_index=True)
    else:
        df_features = df_new

    df_features.to_csv(FEATURES_CSV, index=False)
    print(f"Features saved to {FEATURES_CSV}, total games now: {df_features['game_number'].max()}")

# --- Reload consistently: JSON → dict ---
df_features["evals_dict"] = df_features["evals_dict"].apply(json.loads)


# --- Label function (expects dict now) ---
def is_human_move_good(human_move, evals_dict, threshold=50):
    if not evals_dict:
        return 0
    best_eval = max(evals_dict.values())
    human_eval = evals_dict.get(human_move, 0)
    return int(human_eval >= best_eval - threshold)

#
# df_features["label_move_ease"] = df_features.apply(
#     lambda row: is_human_move_good(row["human_move"], row["evals_dict"], threshold=50),
#     axis=1
# )
# this is faster than .apply:
labels = [is_human_move_good(mv, ed) for mv, ed in zip(df_features["human_move"], df_features["evals_dict"])]
df_features["label_move_ease"] = labels



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

dtrain = xgb.DMatrix(X, label=y)

# --- Train or continue training model ---
params = {"objective": "binary:logistic", "eval_metric": "logloss"}

if os.path.exists(MODEL_PATH):
    print("Loading existing model and continuing training...")
    model = xgb.Booster()
    model.load_model(MODEL_PATH)
    model = xgb.train(params, dtrain, num_boost_round=50, xgb_model=model)
else:
    print("Training new model from scratch...")
    model = xgb.train(params, dtrain, num_boost_round=200)

model.save_model(MODEL_PATH)
print(f"Model saved to {MODEL_PATH}")

# --- Quick evaluation ---
y_pred = (model.predict(dtrain) > 0.5).astype(int)
acc = accuracy_score(y, y_pred)
print(f"Training accuracy: {acc:.2f}")
