import chess
import chess.engine
import pandas as pd
import xgboost as xgb
import joblib
import json
import os
from ml_training.feature_extraction import compute_features

# --- Paths ---
SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
MODEL_DIR = os.path.join(SCRIPT_DIR, "ml_training", "elo_models")
METRICS_FILE = os.path.join(MODEL_DIR, "model_metrics.json")
STOCKFISH_PATH = r"C:\Users\alexa\Downloads\stockfish\stockfish-windows-x86-64-avx2.exe"

# --- Load metrics ---
with open(METRICS_FILE, "r") as f:
    model_metrics = json.load(f)

# --- Elo categorization function ---
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

# --- Ask for FEN and Elo ---
fen = input("Enter a FEN: ")
avg_elo = int(input("Enter average Elo: "))
elo_range = categorize_elo(avg_elo)

# --- Load correct model ---
model_path = os.path.join(MODEL_DIR, f"model_{elo_range}.pkl")
if not os.path.exists(model_path):
    raise ValueError(f"No trained model found for Elo range {elo_range}")
model = joblib.load(model_path)

# --- Start Stockfish ---
board = chess.Board(fen)
with chess.engine.SimpleEngine.popen_uci(STOCKFISH_PATH) as engine:
    features = compute_features(board, engine)

# --- Feature columns ---
feature_cols = [
    "volatility", "move_ease", "trap_susceptibility", "king_exposure",
    "defending_pieces", "doubled_pawns", "backward_pawns", "pawn_majority",
    "mobility", "piece_coordination", "rooks_connected", "bishop_pair", "overworked_defenders",
    "pins", "tactical_motifs", "material_imbalance", "phase",
    "space_control", "passed_pawns", "center_control"
]

X = pd.DataFrame([{k: features[k] for k in feature_cols}])

# --- Predict ---
predicted_score = model.predict(X)[0]  # regression output

# --- Fetch metrics ---
metrics = model_metrics.get(elo_range, {})
n_games = metrics.get("n_games", "unknown")
n_positions = metrics.get("n_positions", "unknown")


# --- Certainty based on RMSE ---
rmse = metrics.get("rmse", None)
certainty = None
if rmse is not None:
    # Convert RMSE into a certainty (lower RMSE = higher certainty)
    certainty = max(0.0, 1.0 - rmse)  # scale: RMSE close to 0 → certainty near 1

# --- Print results ---
print("\n--- Human Playability ---")
print(f"Predicted ease score: {predicted_score:.4f}")
if certainty is not None:
    print(f"Model certainty: {certainty*100:.1f}% (based on validation RMSE)")
print(f"Trained on: {n_games} games ({n_positions} positions) for Elo {elo_range}")


print("\n--- Raw Metrics ---")
for k, v in features.items():
    if k not in ["top_moves", "evals_dict"]:
        print(f"{k}: {v:.2f}")
