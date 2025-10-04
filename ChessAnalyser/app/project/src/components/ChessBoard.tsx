import { useState } from 'react';
import { Board, PieceType } from '../types/chess';
import { isValidMove, wouldBeInCheck } from '../utils/chessLogic';

interface ChessBoardProps {
  board: Board;
  onMove: (fromRow: number, fromCol: number, toRow: number, toCol: number) => boolean;
  currentTurn: 'white' | 'black';
  isInCheck: boolean;
}

const pieceSymbols: Record<PieceType, string> = {
  'k': '♚', 'q': '♛', 'r': '♜', 'b': '♝', 'n': '♞', 'p': '♟',
  'K': '♔', 'Q': '♕', 'R': '♖', 'B': '♗', 'N': '♘', 'P': '♙',
};

export default function ChessBoard({ board, onMove, currentTurn, isInCheck }: ChessBoardProps) {
  const [selectedSquare, setSelectedSquare] = useState<[number, number] | null>(null);
  const [validMoves, setValidMoves] = useState<Set<string>>(new Set());

  const handleSquareClick = (row: number, col: number) => {
    if (selectedSquare) {
      const [fromRow, fromCol] = selectedSquare;

      if (fromRow === row && fromCol === col) {
        setSelectedSquare(null);
        setValidMoves(new Set());
        return;
      }

      const moveSuccessful = onMove(fromRow, fromCol, row, col);
      setSelectedSquare(null);
      setValidMoves(new Set());
    } else {
      const piece = board[row][col];
      if (piece) {
        const pieceIsWhite = piece === piece.toUpperCase();
        const canSelect = (currentTurn === 'white' && pieceIsWhite) || (currentTurn === 'black' && !pieceIsWhite);

        if (canSelect) {
          setSelectedSquare([row, col]);
          calculateValidMoves(row, col, pieceIsWhite);
        }
      }
    }
  };

  const calculateValidMoves = (fromRow: number, fromCol: number, isWhite: boolean) => {
    const moves = new Set<string>();
    for (let row = 0; row < 8; row++) {
      for (let col = 0; col < 8; col++) {
        if (isValidMove(board, fromRow, fromCol, row, col)) {
          if (!wouldBeInCheck(board, fromRow, fromCol, row, col, isWhite)) {
            moves.add(`${row}-${col}`);
          }
        }
      }
    }
    setValidMoves(moves);
  };

  return (
    <div className="inline-block shadow-2xl rounded-lg overflow-hidden border-4 border-cyan-500">
      <div className="grid grid-cols-8 gap-0">
        {board.map((row, rowIndex) =>
          row.map((piece, colIndex) => {
            const isLight = (rowIndex + colIndex) % 2 === 0;
            const isSelected = selectedSquare && selectedSquare[0] === rowIndex && selectedSquare[1] === colIndex;
            const isValidMoveSquare = validMoves.has(`${rowIndex}-${colIndex}`);

            return (
              <div
                key={`${rowIndex}-${colIndex}`}
                onClick={() => handleSquareClick(rowIndex, colIndex)}
                className={`w-16 h-16 flex items-center justify-center text-5xl transition-all cursor-pointer relative ${
                  isSelected
                    ? 'bg-cyan-400 ring-4 ring-cyan-500'
                    : isValidMoveSquare && selectedSquare
                    ? isLight ? 'bg-green-200' : 'bg-green-600'
                    : isLight ? 'bg-stone-200' : 'bg-stone-700'
                } hover:brightness-110`}
              >
                {piece && (
                  <span
                    className={`select-none drop-shadow-lg ${
                      piece === piece.toUpperCase()
                        ? 'text-white'
                        : 'text-gray-900'
                    }`}
                    style={{
                      filter: piece === piece.toUpperCase()
                        ? 'drop-shadow(0 2px 4px rgba(0,0,0,0.8))'
                        : 'drop-shadow(0 2px 4px rgba(255,255,255,0.3))'
                    }}
                  >
                    {pieceSymbols[piece]}
                  </span>
                )}
                {isValidMoveSquare && selectedSquare && (
                  <div className="absolute w-3 h-3 bg-green-500 rounded-full opacity-70"></div>
                )}
              </div>
            );
          })
        )}
      </div>
    </div>
  );
}
