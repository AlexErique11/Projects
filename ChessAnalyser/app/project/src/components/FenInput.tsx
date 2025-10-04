import { useState } from 'react';
import { Check, X } from 'lucide-react';

interface FenInputProps {
  onFenSubmit: (fen: string) => void;
  currentFen: string;
}

export default function FenInput({ onFenSubmit, currentFen }: FenInputProps) {
  const [fenInput, setFenInput] = useState('');
  const [error, setError] = useState('');

  const handleSubmit = () => {
    try {
      if (!fenInput.trim()) {
        setError('FEN string cannot be empty');
        return;
      }

      onFenSubmit(fenInput);
      setError('');
      setFenInput('');
    } catch (err) {
      setError('Invalid FEN format');
    }
  };

  return (
    <div className="bg-white p-6 rounded-lg shadow-lg space-y-4">
      <h3 className="text-lg font-semibold text-slate-800">FEN Position</h3>

      <div className="space-y-2">
        <label className="block text-sm font-medium text-slate-600">
          Enter FEN String
        </label>
        <textarea
          value={fenInput}
          onChange={(e) => setFenInput(e.target.value)}
          placeholder="rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
          className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-cyan-500 focus:border-transparent resize-none"
          rows={3}
        />
        {error && (
          <p className="text-sm text-red-600 flex items-center gap-1">
            <X size={14} />
            {error}
          </p>
        )}
      </div>

      <button
        onClick={handleSubmit}
        className="w-full py-2 px-4 bg-gradient-to-r from-cyan-600 to-teal-600 hover:from-cyan-700 hover:to-teal-700 text-white rounded-lg font-medium transition-colors flex items-center justify-center gap-2 shadow-lg"
      >
        <Check size={18} />
        Load Position
      </button>

      <div className="pt-4 border-t border-slate-200">
        <label className="block text-sm font-medium text-slate-600 mb-2">
          Current FEN
        </label>
        <div className="p-3 bg-slate-50 rounded-lg">
          <code className="text-xs text-slate-700 break-all">{currentFen}</code>
        </div>
      </div>
    </div>
  );
}
