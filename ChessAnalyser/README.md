# Chess Analyser

A clean and simple chess position analysis interface with evaluation bars.

## Features

- Interactive chess board with piece movement
- Position quality and move ease evaluation bars
- FEN input/output with turn detection
- Copy current position to clipboard
- Reset to starting position
- Clean, responsive web interface

## Architecture

- **Frontend**: React with TypeScript and Tailwind CSS
- **Analysis**: Client-side with consistent mock evaluations

## Setup Instructions

1. Navigate to the frontend directory:
```bash
cd app/project
```

2. Install Node.js dependencies:
```bash
npm install
```

3. Start the development server:
```bash
npm run dev
```

The app will run on http://localhost:5173

## Usage

1. Start the React frontend
2. Use the interactive chess board to make moves or input FEN positions
3. View consistent evaluation bars:
   - **Left bar**: Position Quality (how good the position is)
   - **Right bar**: Move Ease (how easy it is to find good moves)
4. Copy the current FEN or reset to the starting position
5. Hover over evaluation bars to see position features


## Files Structure

```
ChessAnalyser/
├── chess_analyser.py       # Original command-line analyzer
├── app/project/           # React frontend
│   ├── src/
│   │   ├── components/    # React components (ChessBoard, EvalBar, FenInput)
│   │   ├── pages/         # Page components
│   │   ├── utils/         # Utility functions (FEN parsing, chess logic, analysis)
│   │   └── types/         # TypeScript types
│   └── package.json       # Node.js dependencies
└── ml_training/           # ML models and training data
```

## Changes Made

1. **Replaced FEN display box** with a copy button
2. **Added turn detection** from FEN input
3. **Added reset button** to return to starting position
4. **Added evaluation bars** on left (Position Quality) and right (Move Ease) sides of the board
5. **Simplified analysis** with consistent client-side evaluations
6. **Clean UI** with focused functionality

## How It Works

The evaluation bars use a simple hash function to generate consistent values based on the FEN position. This means:

- ✅ **Same position = same evaluation** every time
- ✅ **Different positions = different evaluations** 
- ✅ **No external dependencies** required
- ✅ **Instant analysis** with no loading time

The values are generated client-side and are consistent but not based on actual chess analysis - they're for demonstration purposes.
