import { useState, useEffect } from 'react';
import { AlertCircle } from 'lucide-react';
import ChessBoard from '../components/ChessBoard';
import MoveControls from '../components/MoveControls';
import FenInput from '../components/FenInput';
import { parseFEN, boardToFEN, STARTING_FEN } from '../utils/fenParser';
import { isValidMove, makeMove, isInCheck, wouldBeInCheck } from '../utils/chessLogic';
import { Board } from '../types/chess';

export default function HomePage() {
  const [board, setBoard] = useState<Board>(parseFEN(STARTING_FEN));
  const [currentMove, setCurrentMove] = useState(0);
  const [positions, setPositions] = useState<string[]>([STARTING_FEN]);
  const [currentFen, setCurrentFen] = useState(STARTING_FEN);
  const [currentTurn, setCurrentTurn] = useState<'white' | 'black'>('white');
  const [whiteInCheck, setWhiteInCheck] = useState(false);
  const [blackInCheck, setBlackInCheck] = useState(false);

  useEffect(() => {
    const fen = positions[currentMove];
    const newBoard = parseFEN(fen);
    setBoard(newBoard);
    setCurrentFen(fen);

    setWhiteInCheck(isInCheck(newBoard, true));
    setBlackInCheck(isInCheck(newBoard, false));
  }, [currentMove, positions]);

  const handleMove = (fromRow: number, fromCol: number, toRow: number, toCol: number): boolean => {
    const piece = board[fromRow][fromCol];
    if (!piece) return false;

    const pieceIsWhite = piece === piece.toUpperCase();
    const isCorrectTurn = (currentTurn === 'white' && pieceIsWhite) || (currentTurn === 'black' && !pieceIsWhite);

    if (!isCorrectTurn) return false;

    if (!isValidMove(board, fromRow, fromCol, toRow, toCol)) {
      return false;
    }

    if (wouldBeInCheck(board, fromRow, fromCol, toRow, toCol, pieceIsWhite)) {
      return false;
    }

    const newBoard = makeMove(board, fromRow, fromCol, toRow, toCol);
    const newFen = boardToFEN(newBoard);

    setBoard(newBoard);
    setPositions([...positions.slice(0, currentMove + 1), newFen]);
    setCurrentMove(currentMove + 1);
    setCurrentFen(newFen);
    setCurrentTurn(currentTurn === 'white' ? 'black' : 'white');

    setWhiteInCheck(isInCheck(newBoard, true));
    setBlackInCheck(isInCheck(newBoard, false));

    return true;
  };

  const handleFenSubmit = (fen: string) => {
    try {
      const newBoard = parseFEN(fen);
      setBoard(newBoard);
      setPositions([...positions.slice(0, currentMove + 1), fen]);
      setCurrentMove(currentMove + 1);
      setCurrentFen(fen);
    } catch (error) {
      console.error('Invalid FEN:', error);
    }
  };

  const currentPlayerInCheck = currentTurn === 'white' ? whiteInCheck : blackInCheck;

  return (
    <div className="flex-1 bg-gradient-to-br from-blue-50 via-cyan-50 to-teal-50 p-8">
      <div className="max-w-7xl mx-auto">
        <div className="flex items-center justify-between mb-8">
          <h2 className="text-3xl font-bold bg-gradient-to-r from-cyan-600 to-teal-600 bg-clip-text text-transparent">
            Interactive Chess Board
          </h2>
          <div className="flex items-center gap-4">
            <div className={`px-4 py-2 rounded-lg font-semibold ${
              currentTurn === 'white' ? 'bg-white text-slate-800 shadow-lg' : 'bg-slate-800 text-white shadow-lg'
            }`}>
              {currentTurn === 'white' ? 'White' : 'Black'} to move
            </div>
            {currentPlayerInCheck && (
              <div className="px-4 py-2 bg-red-500 text-white rounded-lg font-semibold flex items-center gap-2 shadow-lg animate-pulse">
                <AlertCircle size={20} />
                Check!
              </div>
            )}
          </div>
        </div>

        <div className="flex gap-8">
          <div className="flex flex-col items-center gap-6">
            <ChessBoard
              board={board}
              onMove={handleMove}
              currentTurn={currentTurn}
              isInCheck={currentPlayerInCheck}
            />
            <MoveControls
              currentMove={currentMove}
              totalMoves={positions.length - 1}
              onMoveChange={setCurrentMove}
            />
          </div>

          <div className="flex-1 max-w-md">
            <FenInput onFenSubmit={handleFenSubmit} currentFen={currentFen} />
          </div>
        </div>
      </div>
    </div>
  );
}
