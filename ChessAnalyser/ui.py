import customtkinter as ctk
import tkinter as tk
from PIL import Image, ImageTk
import os
import chess
import chess.engine
import pandas as pd
import joblib
import json
import threading
from ml_training.feature_extraction import compute_features

# --- Paths ---
SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
MODEL_DIR = os.path.join(SCRIPT_DIR, "ml_training", "elo_models")
METRICS_FILE = os.path.join(MODEL_DIR, "model_metrics.json")
FEATURE_SETS_FILE = os.path.join(SCRIPT_DIR, "ml_training", "feature_sets.json")
STOCKFISH_PATH = r"C:\Users\alexa\Downloads\stockfish\stockfish-windows-x86-64-avx2.exe"
PIECE_PATH = os.path.join(SCRIPT_DIR, "pieces")  # PNG images: wP.png, bK.png, etc.

# Load metrics and feature sets
with open(METRICS_FILE, "r") as f:
    model_metrics = json.load(f)
with open(FEATURE_SETS_FILE, "r") as f:
    FEATURE_SETS = json.load(f)

def categorize_elo(avg_elo):
    if avg_elo < 800: return "800-"
    elif avg_elo <= 1100: return "800-1100"
    elif avg_elo <= 1400: return "1100-1400"
    elif avg_elo <= 1600: return "1400-1600"
    elif avg_elo <= 1800: return "1600-1800"
    elif avg_elo <= 2000: return "1800-2000"
    elif avg_elo <= 2200: return "2000-2200"
    else: return "2200+"

ctk.set_appearance_mode("dark")
ctk.set_default_color_theme("blue")

class ChessAnalyserUI(ctk.CTk):
    def __init__(self):
        super().__init__()
        self.title("Chess Analyser")
        self.geometry("1200x650")

        self.board = chess.Board()
        self.selected_square = None
        self.last_move = None
        self.piece_images = {}
        self.move_history = []
        self.redo_stack = []

        # --- Main Frame ---
        self.main_frame = ctk.CTkFrame(self, corner_radius=10)
        self.main_frame.pack(fill="both", expand=True, padx=20, pady=20)

        # Left: Board
        self.board_frame = ctk.CTkFrame(self.main_frame, corner_radius=10)
        self.board_frame.pack(side="left", padx=20, pady=20)

        self.board_canvas = tk.Canvas(self.board_frame, width=480, height=480)
        self.board_canvas.pack()
        self.board_canvas.bind("<Button-1>", self.on_square_click)

        # Undo/Redo buttons below board
        self.control_buttons_frame = ctk.CTkFrame(self.board_frame, corner_radius=0)
        self.control_buttons_frame.pack(pady=5)
        self.undo_button = ctk.CTkButton(
            self.control_buttons_frame, text="←", width=40, height=40, command=self.undo_move
        )
        self.undo_button.pack(side="left", padx=5)
        self.redo_button = ctk.CTkButton(
            self.control_buttons_frame, text="→", width=40, height=40, command=self.redo_move
        )
        self.redo_button.pack(side="left", padx=5)

        # Right: Controls & Analysis
        self.control_frame = ctk.CTkFrame(self.main_frame, corner_radius=10)
        self.control_frame.pack(side="right", fill="y", padx=20, pady=20)

        ctk.CTkLabel(self.control_frame, text="FEN:").pack(pady=(10,0))
        self.fen_entry = ctk.CTkEntry(self.control_frame, width=400)
        self.fen_entry.pack(pady=(0,10))
        self.fen_entry.insert(0, chess.STARTING_FEN)

        ctk.CTkLabel(self.control_frame, text="Average Elo:").pack(pady=(10,0))
        self.elo_entry = ctk.CTkEntry(self.control_frame, width=200)
        self.elo_entry.pack(pady=(0,10))
        self.elo_entry.insert(0, "1500")

        ctk.CTkLabel(self.control_frame, text="Time Control:").pack(pady=(10,0))
        self.time_var = tk.StringVar(value="blitz")
        ctk.CTkOptionMenu(self.control_frame, variable=self.time_var, values=["blitz", "rapid_classical"]).pack(pady=(0,20))

        self.analyse_button = ctk.CTkButton(self.control_frame, text="Load FEN", command=self.load_fen)
        self.analyse_button.pack(pady=10)

        self.reset_button = ctk.CTkButton(self.control_frame, text="Reset Board", command=self.reset_board)
        self.reset_button.pack(pady=10)

        ctk.CTkLabel(self.control_frame, text="Analysis Results:").pack(pady=(10,0))
        self.result_text = ctk.CTkTextbox(self.control_frame, width=400, height=250)
        self.result_text.pack(pady=5)

        self.draw_board()

    # --- Board functions ---
    def reset_board(self):
        self.board.reset()
        self.selected_square = None
        self.last_move = None
        self.move_history.clear()
        self.redo_stack.clear()
        self.draw_board()
        self.result_text.delete("0.0", tk.END)

    def load_fen(self):
        fen = self.fen_entry.get()
        try:
            self.board.set_fen(fen)
        except Exception as e:
            self.result_text.delete("0.0", tk.END)
            self.result_text.insert(tk.END, f"Invalid FEN: {e}")
            return
        self.selected_square = None
        self.last_move = None
        self.move_history.clear()
        self.redo_stack.clear()
        self.draw_board()
        threading.Thread(target=self.run_analysis, daemon=True).start()

    # --- Click-to-move ---
    def on_square_click(self, event):
        size = 60
        col = event.x // size
        row = event.y // size
        square = chess.square(col, 7 - row)
        piece = self.board.piece_at(square)

        if self.selected_square is None:
            # Select a piece to move
            if piece and piece.color == self.board.turn:
                self.selected_square = square
                self.draw_board()  # redraw to show highlight
        else:
            # Try moving the selected piece
            move = chess.Move(self.selected_square, square)
            if move in self.board.legal_moves:
                self.board.push(move)
                self.move_history.append(move)
                self.redo_stack.clear()
                self.last_move = move
                # Clear selection only after move
                self.selected_square = None
                self.draw_board()
                threading.Thread(target=self.run_analysis, daemon=True).start()
            else:
                # If invalid move, keep the piece selected (highlight remains)
                self.draw_board()

    # --- Undo / Redo ---
    def undo_move(self):
        if self.move_history:
            move = self.move_history.pop()
            self.board.pop()
            self.redo_stack.append(move)
            self.last_move = self.move_history[-1] if self.move_history else None
            self.draw_board()
            threading.Thread(target=self.run_analysis, daemon=True).start()

    def redo_move(self):
        if self.redo_stack:
            move = self.redo_stack.pop()
            self.board.push(move)
            self.move_history.append(move)
            self.last_move = move
            self.draw_board()
            threading.Thread(target=self.run_analysis, daemon=True).start()

    # --- Draw Board ---
    def draw_board(self):
        self.board_canvas.delete("all")
        size = 60
        for r in range(8):
            for c in range(8):
                square = chess.square(c, 7 - r)
                if (r + c) % 2 == 0:
                    color = "#F0D9B5"
                    selected_color = "#E6CFA1"
                else:
                    color = "#B58863"
                    selected_color = "#A9744B"

                # Last move
                if self.last_move and (square == self.last_move.from_square or square == self.last_move.to_square):
                    color = "#9ACD32"

                # Selected piece
                if self.selected_square == square:
                    color = selected_color

                self.board_canvas.create_rectangle(c*size, r*size, c*size+size, r*size+size, fill=color, outline=color)

        # Draw pieces
        for square, piece in self.board.piece_map().items():
            row = 7 - chess.square_rank(square)
            col = chess.square_file(square)
            filename = f"{piece.color and 'w' or 'b'}{piece.symbol().upper()}.png"
            if filename not in self.piece_images:
                img_path = os.path.join(PIECE_PATH, filename)
                self.piece_images[filename] = ImageTk.PhotoImage(
                    Image.open(img_path).resize((size, size), Image.Resampling.LANCZOS)
                )
            self.board_canvas.create_image(col*size, row*size, anchor="nw", image=self.piece_images[filename])

    # --- Analysis ---
    def run_analysis(self):
        avg_elo = int(self.elo_entry.get())
        time_control = self.time_var.get()
        elo_range = categorize_elo(avg_elo)

        with chess.engine.SimpleEngine.popen_uci(STOCKFISH_PATH) as engine:
            features = compute_features(self.board, engine)

        targets = ["label_position_quality", "label_move_ease"]
        results = []
        for target in targets:
            feature_cols = FEATURE_SETS.get(elo_range, {}).get(target, FEATURE_SETS["default"][target])
            X = pd.DataFrame([{k: features[k] for k in feature_cols}])
            model_path = os.path.join(MODEL_DIR, f"model_{elo_range}_{time_control}_{target}.pkl")
            if not os.path.exists(model_path):
                results.append(f"No trained model for {target}")
                continue
            model = joblib.load(model_path)
            predicted_score = model.predict(X)[0]

            metrics = model_metrics.get(elo_range, {}).get(time_control, {}).get(target, {})
            rmse = metrics.get("rmse", None)
            certainty = max(0.0, 1.0 - rmse) if rmse else None
            label_name = "Position quality" if target == "label_position_quality" else "Move ease"
            results.append(f"{label_name}: {predicted_score:.4f} (Certainty: {certainty*100:.1f}%)")

        self.result_text.delete("0.0", tk.END)
        self.result_text.insert(tk.END, "\n".join(results))


if __name__ == "__main__":
    app = ChessAnalyserUI()
    app.mainloop()
