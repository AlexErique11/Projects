import chess
import chess.engine
import statistics

# ===== CONSTANTS =====
STOCKFISH_PATH = r"C:\Users\alexa\Downloads\stockfish\stockfish-windows-x86-64-avx2.exe"
DEPTH = 6
MATE_SCORE = 100000

def evaluate_all_moves(board, engine, depth=DEPTH):
    legal_moves = list(board.legal_moves)
    n = len(legal_moves)
    if n == 0:
        return 0, {}

    infos = engine.analyse(board, chess.engine.Limit(depth=depth), multipv=n)

    results = {}
    for info in infos:
        move = info["pv"][0] if "pv" in info else None
        score_obj = info["score"].pov(board.turn)
        if score_obj.mate() is not None:
            score = MATE_SCORE if score_obj.mate() > 0 else -MATE_SCORE
        else:
            score = score_obj.score(mate_score=MATE_SCORE) or 0

        results[move] = score

    # Fill missing moves with score 0
    for m in legal_moves:
        results.setdefault(m, 0)

    best_eval = max(results.values()) if results else 0
    return best_eval, results



def compute_features(board, engine, depth=DEPTH):
    """
    Compute human-playability metrics for a given board state.
    Optimized to use a single engine call for all move evaluations.
    """
    legal_moves = list(board.legal_moves)
    n_moves = len(legal_moves)

    # Cheap features (no engine required)
    move_ease_score, checks, captures = 0, 0, 0
    for move in legal_moves:
        if board.gives_check(move):
            move_ease_score += 3
            checks += 1
        elif board.is_capture(move):
            move_ease_score += 2
            captures += 1
        else:
            move_ease_score += 1
    move_ease = move_ease_score / (3 * n_moves) if n_moves else 0

    pins = sum(
        1 for sq, piece in board.piece_map().items()
        if board.is_pinned(piece.color, sq)
    )
    tactical_motifs = pins

    # King safety
    king_square = board.king(board.turn)
    king_zone = list(chess.SquareSet(chess.BB_KING_ATTACKS[king_square]))
    king_exposure = sum(1 for sq in king_zone if board.is_attacked_by(not board.turn, sq))

    has_kingside = board.has_kingside_castling_rights(board.turn)
    has_queenside = board.has_queenside_castling_rights(board.turn)
    castling_status = 0 if has_kingside or has_queenside else \
        (-1 if chess.square_rank(king_square) in [0, 7] and chess.square_file(king_square) == 4 else 1)

    defending_pieces = sum(
        1 for piece_type in [chess.BISHOP, chess.KNIGHT, chess.ROOK]
        for sq in board.pieces(piece_type, board.turn)
        if chess.square_distance(sq, king_square) <= 2
    )

    # Pawn structure
    pawns = list(board.pieces(chess.PAWN, board.turn))
    files = [chess.square_file(p) for p in pawns]
    doubled_pawns = sum(files.count(f) - 1 for f in set(files))

    # Correct backward pawn detection
    backward_pawns = 0
    for p in pawns:
        f, r = chess.square_file(p), chess.square_rank(p)
        if all(
            all(
                board.piece_at(chess.square(f + dx, r_target)) is None or
                board.piece_at(chess.square(f + dx, r_target)).piece_type != chess.PAWN
                for r_target in (range(r + 1, 8) if board.turn == chess.WHITE else range(0, r))
            )
            for dx in [-1, 1] if 0 <= f + dx < 8
        ):
            backward_pawns += 1

    pawn_structure_score = 1 / (1 + doubled_pawns + backward_pawns)
    queenside = sum(1 for p in pawns if chess.square_file(p) < 4)
    kingside = len(pawns) - queenside
    pawn_majority = queenside - kingside

    mobility = min(n_moves / 40, 1.0)

    # Piece coordination
    pieces = board.piece_map()
    connectedness = sum(
        1 for sq, piece in pieces.items()
        if len(board.attackers(piece.color, sq)) > 1
    )
    piece_coordination = connectedness / max(1, len(pieces))
    rooks = list(board.pieces(chess.ROOK, board.turn))
    rooks_connected = int(len(rooks) == 2 and board.is_attacked_by(board.turn, rooks[0]) and board.is_attacked_by(board.turn, rooks[1]))
    bishop_pair = int(len(board.pieces(chess.BISHOP, board.turn)) == 2)

    overworked = sum(
        1 for sq, piece in pieces.items()
        if piece.color == board.turn and len(list(board.attackers(piece.color, sq))) > 1
    )

    # Engine-heavy features
    best_eval, evals_dict = evaluate_all_moves(board, engine, depth=depth)
    evals = list(evals_dict.values())
    volatility = statistics.variance(evals) if len(evals) > 1 else 0
    volatility_score = 1 / (1 + volatility / 100)
    trap_susceptibility = sum(1 for ev in evals if ev < best_eval - 150) / max(1, len(evals))

    # Material imbalance
    material_white = sum(p.piece_type for p in board.piece_map().values() if p.color == chess.WHITE)
    material_black = sum(p.piece_type for p in board.piece_map().values() if p.color == chess.BLACK)
    material_imbalance = abs(material_white - material_black)
    total_material = material_white + material_black
    phase = 0 if total_material > 40 else 1 if total_material > 20 else 2

    # Space and passed pawns
    space_control = sum(1 for sq in board.pieces(chess.PAWN, board.turn) if chess.square_rank(sq) in (4, 5))
    passed_pawns = 0
    for p in pawns:
        f, r = chess.square_file(p), chess.square_rank(p)
        ranks_ahead = range(r + 1, 8) if board.turn == chess.WHITE else range(0, r)
        blocked = any(
            board.piece_at(chess.square(f + dx, r_target)) and
            board.piece_at(chess.square(f + dx, r_target)).piece_type == chess.PAWN
            for dx in [-1, 0, 1] if 0 <= f + dx < 8
            for r_target in ranks_ahead if 0 <= r_target < 8
        )
        if not blocked:
            passed_pawns += 1

    center_squares = [chess.D4, chess.E4, chess.D5, chess.E5]
    center_control = sum(1 for sq in center_squares if board.is_attacked_by(board.turn, sq))

    return {
        "volatility": volatility,
        "volatility_score": volatility_score,
        "move_ease": move_ease,
        "trap_susceptibility": trap_susceptibility,
        "king_exposure": king_exposure,
        "castling_status": castling_status,
        "defending_pieces": defending_pieces,
        "pawn_structure": pawn_structure_score,
        "doubled_pawns": doubled_pawns,
        "backward_pawns": backward_pawns,
        "pawn_majority": pawn_majority,
        "mobility": mobility,
        "piece_coordination": piece_coordination,
        "rooks_connected": rooks_connected,
        "bishop_pair": bishop_pair,
        "overworked_defenders": overworked,
        "checks": checks,
        "captures": captures,
        "pins": pins,
        "tactical_motifs": tactical_motifs,
        "material_imbalance": material_imbalance,
        "phase": phase,
        "space_control": space_control,
        "passed_pawns": passed_pawns,
        "center_control": center_control,
        "top_moves": [m.uci() for m in legal_moves],
        "evals_dict": {m.uci(): v for m, v in evals_dict.items()}
    }
