# chess_analyser.py
import chess
import joblib
from ml_training.feature_extraction import compute_features

# Path to trained model
MODEL_PATH = r"C:\Users\alexa\OneDrive\Desktop\Project chess\ml_training\human_playability_model.pkl"
STOCKFISH_PATH = r"C:\Users\alexa\Downloads\stockfish\stockfish-windows-x86-64-avx2.exe"

# Load model
model = joblib.load(MODEL_PATH)

# Ask for a FEN
fen = input("Enter a FEN: ")
board = chess.Board(fen)

# Start Stockfish engine
with chess.engine.SimpleEngine.popen_uci(STOCKFISH_PATH) as engine:
    # Extract features
    features = compute_features(board, engine)

import pandas as pd

# Build DataFrame with correct feature names
X = pd.DataFrame([{
    "volatility": features["volatility"],
    "move_ease": features["move_ease"],
    "trap_susceptibility": features["trap_susceptibility"],
    "king_exposure": features["king_exposure"],
    "castling_status": features["castling_status"],
    "defending_pieces": features["defending_pieces"],
    "pawn_structure": features["pawn_structure"],
    "doubled_pawns": features["doubled_pawns"],
    "backward_pawns": features["backward_pawns"],
    "pawn_majority": features["pawn_majority"],
    "mobility": features["mobility"],
    "piece_coordination": features["piece_coordination"],
    "rooks_connected": features["rooks_connected"],
    "bishop_pair": features["bishop_pair"],
    "overworked_defenders": features["overworked_defenders"],
    "checks": features["checks"],
    "captures": features["captures"],
    "pins": features["pins"],
    "tactical_motifs": features["tactical_motifs"],
    "material_imbalance": features["material_imbalance"],
    "phase": features["phase"],
    "space_control": features["space_control"],
    "passed_pawns": features["passed_pawns"],
    "center_control": features["center_control"]
}])

# Predict
prediction = model.predict(X)[0]
probability = model.predict_proba(X)[0][1]


print("\n--- Human Playability ---")
print(f"Model prediction: {'EASY' if prediction == 1 else 'HARD'}")
print(f"Confidence (probability): {probability:.2f}")

print("\n--- Raw Metrics ---")
for k, v in features.items():
    if k != "top_moves":  # skip move list
        print(f"{k}: {v:.2f}")
