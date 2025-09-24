# chess_analyser.py
import chess
import chess.engine
import pandas as pd
import xgboost as xgb
from ml_training.feature_extraction import compute_features

# --- Paths ---
MODEL_PATH = r"C:\Users\alexa\OneDrive\Desktop\Projects\ChessAnalyser\ml_training\human_playability_model.json"
STOCKFISH_PATH = r"C:\Users\alexa\Downloads\stockfish\stockfish-windows-x86-64-avx2.exe"

# --- Load XGBoost model ---
model = xgb.Booster()
model.load_model(MODEL_PATH)

# --- Ask for a FEN ---
fen = input("Enter a FEN: ")
board = chess.Board(fen)

# --- Start Stockfish engine ---
with chess.engine.SimpleEngine.popen_uci(STOCKFISH_PATH) as engine:
    features = compute_features(board, engine)

# --- Build DataFrame with correct feature names ---
feature_cols = [
    "volatility", "move_ease", "trap_susceptibility", "king_exposure", "castling_status",
    "defending_pieces", "pawn_structure", "doubled_pawns", "backward_pawns", "pawn_majority",
    "mobility", "piece_coordination", "rooks_connected", "bishop_pair", "overworked_defenders",
    "checks", "captures", "pins", "tactical_motifs", "material_imbalance", "phase",
    "space_control", "passed_pawns", "center_control"
]

X = pd.DataFrame([{k: features[k] for k in feature_cols}])

# --- Convert to DMatrix ---
dX = xgb.DMatrix(X)

# --- Predict ---
probability = model.predict(dX)[0]
prediction = int(probability > 0.5)

print("\n--- Human Playability ---")
print(f"Model prediction: {'EASY' if prediction == 1 else 'HARD'}")
print(f"Confidence (probability): {probability:.2f}")

print("\n--- Raw Metrics ---")
for k, v in features.items():
    if k not in ["top_moves", "evals_dict"]:
        print(f"{k}: {v:.2f}")
