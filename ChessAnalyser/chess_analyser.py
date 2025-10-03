# chess_analyser.py
import chess
import chess.engine
import pandas as pd
import joblib
import json
import os
from ml_training.feature_extraction import compute_features

# --- Paths ---
SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
MODEL_DIR = os.path.join(SCRIPT_DIR, "ml_training", "elo_models")
METRICS_FILE = os.path.join(MODEL_DIR, "model_metrics.json")
FEATURE_SETS_FILE = os.path.join(SCRIPT_DIR, "ml_training", "feature_sets.json")
STOCKFISH_PATH = r"C:\Users\alexa\Downloads\stockfish\stockfish-windows-x86-64-avx2.exe"

# --- Load metrics and feature sets ---
with open(METRICS_FILE, "r") as f:
    model_metrics = json.load(f)

with open(FEATURE_SETS_FILE, "r") as f:
    FEATURE_SETS = json.load(f)

# --- Elo categorization ---
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

# --- Ask user for FEN, Elo, and time control ---
fen = input("Enter a FEN: ")
avg_elo = int(input("Enter average Elo: "))
elo_range = categorize_elo(avg_elo)

# Ask for time control
time_control = input("Enter time control ('blitz' or 'rapid_classical'): ").strip().lower()
if time_control not in ["blitz", "rapid_classical"]:
    raise ValueError("Invalid time control. Must be 'blitz' or 'rapid_classical'.")

# --- Start Stockfish & extract features ---
board = chess.Board(fen)
with chess.engine.SimpleEngine.popen_uci(STOCKFISH_PATH) as engine:
    features = compute_features(board, engine)

# --- Targets ---
targets = ["label_position_quality", "label_move_ease"]

print("\n--- Human Playability ---")
for target in targets:
    # Select feature columns (fall back to default if not specified for this elo range)
    if elo_range in FEATURE_SETS and target in FEATURE_SETS[elo_range]:
        feature_cols = FEATURE_SETS[elo_range][target]
    else:
        feature_cols = FEATURE_SETS["default"][target]

    # Prepare input
    X = pd.DataFrame([{k: features[k] for k in feature_cols}])

    # Load model
    model_path = os.path.join(MODEL_DIR, f"model_{elo_range}_{time_control}_{target}.pkl")
    if not os.path.exists(model_path):
        raise ValueError(f"No trained model found for Elo range {elo_range}, time {time_control}, target {target}")
    model = joblib.load(model_path)

    # Predict
    predicted_score = model.predict(X)[0]

    # Fetch metrics
    metrics = model_metrics.get(elo_range, {}).get(time_control, {}).get(target, {})
    n_games = metrics.get("n_games", "unknown")
    n_positions = metrics.get("n_positions", "unknown")
    rmse = metrics.get("rmse", None)
    certainty = max(0.0, 1.0 - rmse) if rmse is not None else None

    # Report
    label_name = "Position quality" if target == "label_position_quality" else "Move ease"
    print(f"\n[{label_name}]")
    print(f"Predicted score: {predicted_score:.4f}")
    if certainty is not None:
        print(f"Model certainty: {certainty*100:.1f}% (based on RMSE)")
    print(f"Trained on: {n_games} games ({n_positions} positions) for Elo {elo_range}, Time {time_control}")

# --- Raw feature values ---
print("\n--- Raw Features ---")
for k, v in features.items():
    if k not in ["top_moves", "evals_dict"]:
        try:
            print(f"{k}: {float(v):.2f}")
        except (ValueError, TypeError):
            print(f"{k}: {v}")
