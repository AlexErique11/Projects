# train_model.py
import os
import chess
import chess.pgn
import pandas as pd
import zstandard as zstd
import io
import xgboost as xgb
import json
import joblib
from tqdm import tqdm
from feature_extraction import compute_features
from sklearn.metrics import mean_squared_error, r2_score
from sklearn.model_selection import train_test_split, RandomizedSearchCV
import matplotlib.pyplot as plt
import numpy as np
import warnings
import os
import json

# Load feature sets from feature_sets.json
SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
FEATURE_SETS_PATH = os.path.join(SCRIPT_DIR, "feature_sets.json")

with open(FEATURE_SETS_PATH, "r") as f:
    FEATURE_SETS = json.load(f)

warnings.filterwarnings("ignore", category=UserWarning)

# --- Paths ---
STOCKFISH_PATH = r"C:\Users\alexa\Downloads\stockfish\stockfish-windows-x86-64-avx2.exe"
SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
DATA_PATH = os.path.join(SCRIPT_DIR, "data", "lichess_data.zst")
FEATURES_CSV = os.path.join(SCRIPT_DIR, "features.csv")
MODEL_DIR = os.path.join(SCRIPT_DIR, "elo_models")

os.makedirs(MODEL_DIR, exist_ok=True)

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


# --- Determine time control category ---
def categorize_time_control(game_headers):
    if "TimeControl" not in game_headers:
        return "unknown"
    tc = game_headers["TimeControl"]
    try:
        tc = int(tc.split("+")[0])  # base time in seconds
    except ValueError:
        return "unknown"

    if tc < 180:  # <3 min → Bullet
        return "bullet"
    elif tc < 600:  # 3-10 min → Blitz
        return "blitz"
    else:  # 10+ min → Rapid/Classical
        return "rapid_classical"

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

            # --- Extract average Elo ---
            avg_elo = None
            if "WhiteElo" in game.headers and "BlackElo" in game.headers:
                try:
                    white_elo = int(game.headers["WhiteElo"])
                    black_elo = int(game.headers["BlackElo"])
                    avg_elo = (white_elo + black_elo) / 2
                except ValueError:
                    avg_elo = None

            # if avg_elo is None or avg_elo < 800 or avg_elo >= 1100:
            #     game = chess.pgn.read_game(text_stream)
            #     continue
            # here

            # --- Determine time control ---
            time_control = categorize_time_control(game.headers)
            if time_control == "bullet":  # skip bullet games
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
                human_move = move.uci()
                features["human_move"] = human_move
                features["game_number"] = current_game_index
                features["eval_score"] = eval_score
                features["avg_elo"] = avg_elo
                features["time_control"] = time_control

                # --- Label 1: Ease score (after 10 moves) handled later ---

                # --- Label 2: Move quality (distance from best move) ---
                best_move = info["pv"][0] if "pv" in info else None
                if best_move is not None:
                    # eval after best move
                    board.push(best_move)
                    best_info = engine.analyse(board, chess.engine.Limit(depth=6))
                    board.pop()
                    best_eval_score = best_info["score"].pov(board.turn).score(mate_score=10000)

                    diff = abs(best_eval_score - eval_score)
                    move_ease = 1 / (1 + diff / 100)  # scaled to (0,1]
                else:
                    move_ease = 0.5

                features["label_move_ease"] = move_ease

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
def eval_change_score(eval_list_json, move_index, lookahead=20):
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
df_features["label_position_quality"] = [
    eval_change_score(eval_list, idx, lookahead=10)
    for eval_list, idx in zip(df_features["eval_list"], df_features["move_index"])
]

# --- Elo range categorization ---
def categorize_elo(avg_elo):
    if avg_elo is None:
        return "unknown"
    if avg_elo < 800:
        return "800-"
    elif avg_elo <= 1100:
        return "800-1100"
    elif avg_elo <= 1400:
        return "1100-1400"
    elif avg_elo <= 1600:
        return "1400-1600"
    elif avg_elo <= 1800:
        return "1600-1800"
    elif avg_elo <= 2000:
        return "1800-2000"
    elif avg_elo <= 2200:
        return "2000-2200"
    else:
        return "2200+"

df_features["elo_range"] = df_features["avg_elo"].apply(categorize_elo)


# --- Features and target ---
feature_cols = [
    "volatility", "move_ease", "trap_susceptibility", "king_exposure",
    "defending_pieces", "doubled_pawns", "backward_pawns", "pawn_majority",
    "mobility", "piece_coordination", "hanging_pieces", "rooks_connected", "bishop_pair", "overworked_defenders",
    "pins", "tactical_motifs", "material_imbalance", "phase",
    "space_control", "passed_pawns", "center_control", "stockfish_eval"
]

targets = ["label_position_quality", "label_move_ease"]
elo_ranges = df_features["elo_range"].unique()
# elo_ranges = ["800-1100"]  # Only train on this Elo range here

models = {}
model_metrics = {}

# --- Train a model for each Elo range ---
time_controls = ["blitz", "rapid_classical"]  # bullet is ignored

for elo_range in elo_ranges:
    for tc in time_controls:
        df_range = df_features[
            (df_features["elo_range"] == elo_range) &
            (df_features["time_control"] == tc)
        ]
        if df_range.shape[0] < 50:
            print(f"Skipping Elo {elo_range}, Time {tc} due to insufficient data")
            continue

        print(f"\nTraining models for Elo {elo_range}, Time {tc} "
              f"(games: {df_range['game_number'].nunique()})")

        for target in targets:
            # Get feature list
            if elo_range in FEATURE_SETS and target in FEATURE_SETS[elo_range]:
                feature_cols = FEATURE_SETS[elo_range][target]
            else:
                feature_cols = FEATURE_SETS["default"][target]

            X = df_range[feature_cols]
            y = df_range[target]

            X_train, X_val, y_train, y_val = train_test_split(
                X, y, test_size=0.2, random_state=42
            )

            # --- Train model (same as current pipeline) ---
            param_grid = {
                "subsample": [0.7, 0.8, 0.9],
                "n_estimators": [200, 300],
                "min_child_weight": [1, 3],
                "max_depth": [6, 8],
                "learning_rate": [0.05, 0.1],
                "gamma": [0, 1],
                "colsample_bytree": [0.8, 1.0]
            }

            xgb_model = xgb.XGBRegressor(
                objective="reg:squarederror",
                tree_method="hist",
                seed=42
            )

            grid_search = RandomizedSearchCV(
                estimator=xgb_model,
                param_distributions=param_grid,
                n_iter=20,
                scoring="neg_mean_squared_error",
                cv=3,
                verbose=1,
                n_jobs=-1,
                random_state=42
            )

            grid_search.fit(X_train, y_train)
            best_params = grid_search.best_params_

            final_model = xgb.XGBRegressor(
                **best_params,
                objective="reg:squarederror",
                eval_metric="rmse",
                tree_method="hist",
                seed=42
            )
            final_model.fit(X_train, y_train, eval_set=[(X_val, y_val)], verbose=False)

            # --- Evaluate & save ---
            y_pred = final_model.predict(X_val)
            rmse = mean_squared_error(y_val, y_pred) ** 0.5
            r2 = r2_score(y_val, y_pred)
            corr = np.corrcoef(y_val, y_pred)[0, 1]

            print(f"[{target}] RMSE: {rmse:.4f} | R²: {r2:.4f} | Corr: {corr:.4f}")

            model_filename = f"model_{elo_range}_{tc}_{target}.pkl"
            joblib.dump(final_model, os.path.join(MODEL_DIR, model_filename))

            if elo_range not in model_metrics:
                model_metrics[elo_range] = {}
            if tc not in model_metrics[elo_range]:
                model_metrics[elo_range][tc] = {}
            model_metrics[elo_range][tc][target] = {
                "rmse": rmse,
                "r2": r2,
                "corr": corr,
                "n_positions": df_range.shape[0],
                "n_games": df_range["game_number"].nunique()
            }


# --- Save metrics for analyser ---

# --- Save metrics for this Elo range after both models are trained ---
with open(os.path.join(MODEL_DIR, "model_metrics.json"), "w") as f:
    json.dump(model_metrics, f, indent=2)

print(f"Metrics for Elo {elo_range} saved.")
print("\n--- Training complete ---")
print("Model metrics:", model_metrics)
