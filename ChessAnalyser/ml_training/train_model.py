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
from sklearn.model_selection import train_test_split, RandomizedSearchCV
import matplotlib.pyplot as plt
import numpy as np
import warnings

warnings.filterwarnings("ignore", category=UserWarning)

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
MAX_GAMES = 200
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
            eval_list = []

            for move in game.mainline_moves():
                info = engine.analyse(board, chess.engine.Limit(depth=6))
                eval_score = info["score"].pov(board.turn).score(mate_score=10000)
                eval_list.append(eval_score)

                features = compute_features(board, engine)
                features["human_move"] = move.uci()
                features["game_number"] = current_game_index
                features["eval_score"] = eval_score

                game_positions.append(features)
                board.push(move)

            for i, pos_features in enumerate(game_positions):
                pos_features["eval_list"] = json.dumps(eval_list)

            positions.extend(game_positions)
            game_count += 1
            pbar.update(1)
            game = chess.pgn.read_game(text_stream)

pbar.close()

# --- Append new features ---
if positions:
    df_new = pd.DataFrame(positions)
    if not df_features.empty:
        df_features = pd.concat([df_features, df_new], ignore_index=True)
    else:
        df_features = df_new

    df_features.to_csv(FEATURES_CSV, index=False)
    print(f"Features saved to {FEATURES_CSV}, total games now: {df_features['game_number'].max()}")

# --- Label function: change in eval after 10 moves ---
def eval_change_score(eval_list_json, move_index, lookahead=10):
    eval_list = json.loads(eval_list_json)
    current_eval = eval_list[move_index]
    future_index = move_index + lookahead

    if future_index >= len(eval_list):
        future_eval = eval_list[-1]
    else:
        future_eval = eval_list[future_index]

    eval_diff = future_eval - current_eval
    score = 1 / (1 + abs(eval_diff) / 100)
    return float(score)

df_features["move_index"] = df_features.groupby("game_number").cumcount()
df_features["label_move_ease"] = [
    eval_change_score(eval_list, idx, lookahead=10)
    for eval_list, idx in zip(df_features["eval_list"], df_features["move_index"])
]

# --- Features and target ---
feature_cols = [
    "volatility", "move_ease", "trap_susceptibility", "king_exposure", "castling_status",
    "defending_pieces", "doubled_pawns", "backward_pawns", "pawn_majority",
    "mobility", "piece_coordination", "rooks_connected", "bishop_pair", "overworked_defenders",
    "pins", "tactical_motifs", "material_imbalance", "phase",
    "space_control", "passed_pawns", "center_control"
]

X = df_features[feature_cols]
y = df_features["label_move_ease"]

X_train, X_val, y_train, y_val = train_test_split(X, y, test_size=0.2, random_state=42)

from sklearn.model_selection import GridSearchCV
from xgboost import XGBRegressor

print("Running hyperparameter tuning...")

param_grid = {
    "subsample": [0.7, 0.8, 0.9],
    "n_estimators": [200, 300, 400],
    "min_child_weight": [1, 3],
    "max_depth": [6, 8],
    "learning_rate": [0.05, 0.1],
    "gamma": [0, 1],
    "colsample_bytree": [0.8, 1.0]
}

xgb_model = XGBRegressor(
    objective="reg:squarederror",
    tree_method="hist",
    seed=42
)
from sklearn.model_selection import RandomizedSearchCV

grid_search = RandomizedSearchCV(
    estimator=xgb_model,
    param_distributions=param_grid,
    n_iter=100,   # number of random parameter combinations to try
    scoring="neg_mean_squared_error",
    cv=3,
    verbose=1,
    n_jobs=-1,
    random_state=42
)


grid_search.fit(X_train, y_train)
best_params = grid_search.best_params_
print(f"Best hyperparameters found: {best_params}")

# ---- Final model with early stopping ----
final_model = XGBRegressor(
    **best_params,
    objective="reg:squarederror",
    eval_metric="rmse",
    tree_method="hist",
    seed=42
)

final_model.fit(
    X_train, y_train,
    eval_set=[(X_val, y_val)],
    verbose=False
)


print("Final model trained successfully.")

# --- Evaluate model ---
y_pred = final_model.predict(X_val)

rmse = mean_squared_error(y_val, y_pred) ** 0.5
r2 = r2_score(y_val, y_pred)
corr = np.corrcoef(y_val, y_pred)[0, 1]

print("\n--- Model Evaluation ---")
print(f"Validation RMSE: {rmse:.4f}")
print(f"Validation R²:   {r2:.4f}")
print(f"Correlation:     {corr:.4f}")

# --- Feature importance ---
importances = final_model.feature_importances_
importance_df = pd.DataFrame({
    "feature": feature_cols,
    "importance": importances
}).sort_values(by="importance", ascending=False)

print("\n--- Top 10 Features ---")
print(importance_df.head(10))

# --- Plot feature importance ---
plt.figure(figsize=(10, 6))
plt.barh(importance_df["feature"], importance_df["importance"], color="skyblue")
plt.gca().invert_yaxis()
plt.title("Feature Importance (XGBoost Gain)")
plt.xlabel("Importance")
plt.tight_layout()
plt.show()

# --- Plot learning curve (if eval history is available) ---
if final_model.evals_result():
    evals_result = final_model.evals_result()
    epochs = len(evals_result['validation_0']['rmse'])
    x_axis = range(0, epochs)

    plt.figure(figsize=(8, 5))
    plt.plot(x_axis, evals_result['validation_0']['rmse'], label='Validation')
    plt.title('XGBoost RMSE over Iterations')
    plt.xlabel('Iteration')
    plt.ylabel('RMSE')
    plt.legend()
    plt.tight_layout()
    plt.show()
