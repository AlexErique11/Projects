import os
import joblib
import json
import pandas as pd

# --- Paths ---
SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
FEATURES_CSV = os.path.join(SCRIPT_DIR, "features.csv")
MODEL_DIR = os.path.join(SCRIPT_DIR, "elo_models")
METRICS_FILE = os.path.join(MODEL_DIR, "model_metrics.json")
FEATURE_SETS_FILE = os.path.join(SCRIPT_DIR, "feature_sets.json")

with open(FEATURE_SETS_FILE, "r") as f:
    FEATURE_SETS = json.load(f)

# --- Load features ---
if not os.path.exists(FEATURES_CSV):
    raise FileNotFoundError(f"Features CSV not found at {FEATURES_CSV}")

df_features = pd.read_csv(FEATURES_CSV)


def categorize_elo(avg_elo):
    if pd.isna(avg_elo):
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

# --- Load metrics ---
if not os.path.exists(METRICS_FILE):
    raise FileNotFoundError(f"Metrics file not found at {METRICS_FILE}")

with open(METRICS_FILE, "r") as f:
    model_metrics = json.load(f)

# --- Count games and positions per Elo range ---
elo_range_order = [
    "800-", "800-1100", "1100-1400", "1400-1600",
    "1600-1800", "1800-2000", "2000-2200", "2200+"
]

elo_ranges = [r for r in elo_range_order if r in df_features["elo_range"].unique()]
elo_stats = {}

for elo_range in elo_ranges:
    df_range = df_features[df_features["elo_range"] == elo_range]
    n_games = df_range["game_number"].nunique()
    n_positions = len(df_range)
    metrics = model_metrics.get(elo_range, {}).get("label_move_ease", {})
    elo_stats[elo_range] = {
        "games": n_games,
        "positions": n_positions,
        "rmse": metrics.get("rmse", None),
        "r2": metrics.get("r2", None)
    }

print("\n--- Move Ease Stats (label_move_ease) ---")
for elo_range, stats in elo_stats.items():
    print(f"{elo_range}: {stats['games']} games, {stats['positions']} positions, "
          f"RMSE: {stats['rmse']}, R²: {stats['r2']}")

# --- Ask for single Elo input ---
try:
    elo_input = int(input("\nEnter a single Elo rating (e.g. 1550): ").strip())
except ValueError:
    print("Invalid Elo value.")
    exit()

# --- Find Elo range ---
elo_range = categorize_elo(elo_input)
print(f"Elo {elo_input} falls into range '{elo_range}'")

if elo_range not in elo_stats:
    print(f"No stats found for Elo range '{elo_range}'.")
    exit()

model_file = os.path.join(MODEL_DIR, f"model_{elo_range}_label_move_ease.pkl")
if not os.path.exists(model_file):
    print(f"Model file for Elo range '{elo_range}' not found: {model_file}")
    exit()

# --- Load model ---
model = joblib.load(model_file)

# --- Feature importance ---
try:
    feature_cols = FEATURE_SETS[elo_range]["label_move_ease"]
except KeyError:
    print(f"Feature set for Elo range '{elo_range}' not found. Using default.")
    feature_cols = FEATURE_SETS["default"]["label_move_ease"]

importance_scores = model.feature_importances_

print(f"\n--- Feature Importance for Elo {elo_input} (Range {elo_range}) - label_move_ease ---")
importance_df = pd.DataFrame({
    "Feature": feature_cols,
    "Importance": importance_scores
}).sort_values(by="Importance", ascending=False)

print(importance_df.to_string(index=False))

# --- Show a plot ---
try:
    import matplotlib.pyplot as plt
    plt.figure(figsize=(10, 6))
    plt.barh(importance_df["Feature"], importance_df["Importance"], color="salmon")
    plt.gca().invert_yaxis()
    plt.xlabel("Importance")
    plt.title(f"Feature Importance for Elo {elo_input} (Range {elo_range}) - label_move_ease")
    plt.show()
except ImportError:
    print("Matplotlib not installed — skipping feature importance plot.")
