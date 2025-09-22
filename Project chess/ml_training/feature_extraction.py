def compute_features(board, engine, top_n=5, time_per_move=0.1):
    import chess
    import statistics

    top_moves_info = engine.analyse(board, chess.engine.Limit(time=time_per_move), multipv=top_n)

    # --- Volatility ---
    evals = [info["score"].pov(board.turn).score(mate_score=100000) or 0 for info in top_moves_info]
    volatility = statistics.variance(evals) if len(evals) > 1 else 0
    volatility_score = 1 / (1 + volatility / 100)

    # --- Move ease ---
    top_moves = [info["pv"][0] for info in top_moves_info]
    forcing_score = 0
    for move in top_moves:
        board.push(move)
        if board.is_check():
            forcing_score += 3
        elif board.is_capture(move):
            forcing_score += 2
        else:
            forcing_score += 1
        board.pop()
    move_ease = forcing_score / (3 * top_n)

    # --- Trap susceptibility ---
    trap_susceptibility = 0
    for move in top_moves:
        board.push(move)
        response = engine.analyse(board, chess.engine.Limit(time=time_per_move))
        eval_after = response["score"].pov(board.turn).score(mate_score=100000) or 0
        board.pop()
        # If eval collapses after seemingly normal move
        if eval_after < -150:
            trap_susceptibility += 1
    trap_susceptibility /= top_n

    # --- King safety ---
    king_square = board.king(board.turn)
    king_zone = [sq for sq in chess.SquareSet(chess.BB_KING_ATTACKS[king_square])]
    enemy_control = sum(1 for sq in king_zone if board.is_attacked_by(not board.turn, sq))

    # Castling status
    has_kingside_rights = board.has_kingside_castling_rights(board.turn)
    has_queenside_rights = board.has_queenside_castling_rights(board.turn)
    if has_kingside_rights or has_queenside_rights:
        castling_status = 0 # Can still castle
    else:
        # Check if king is on original square (e1/e8)
        starting_rank = 0 if board.turn == chess.WHITE else 7
        castling_status = -1 if chess.square_rank(king_square) == starting_rank and chess.square_file(king_square) == 4 else 1

    defending_pieces = sum(
        1 for piece_type in [chess.BISHOP, chess.KNIGHT, chess.ROOK]
        for sq in board.pieces(piece_type, board.turn)
        if chess.square_distance(sq, king_square) <= 2
    )

    # --- Pawn structure ---
    pawns = list(board.pieces(chess.PAWN, board.turn))
    files = [chess.square_file(p) for p in pawns]
    doubled_pawns = sum(files.count(f) - 1 for f in set(files))

    # Pawn majorities
    queenside = sum(1 for p in pawns if chess.square_file(p) < 4)
    kingside = len(pawns) - queenside
    pawn_majority = queenside - kingside

    backward_pawns = 0
    for p in pawns:
        f = chess.square_file(p)
        r = chess.square_rank(p)
        if all(0 <= f + dx < 8 and board.piece_at(chess.square(f + dx, r)) not in board.piece_map().values()
               for dx in [-1, 1]):
            backward_pawns += 1
    pawn_structure_score = 1 / (1 + doubled_pawns + backward_pawns)

    # --- Mobility ---
    mobility = min(top_n / 40, 1.0)

    # --- Piece coordination ---
    pieces = board.piece_map()
    connectedness = sum(1 for sq, piece in pieces.items()
                        if board.is_attacked_by(piece.color, sq) > 1)

    # Rooks connected
    rooks = list(board.pieces(chess.ROOK, board.turn))
    rooks_connected = int(len(rooks) == 2 and board.is_attacked_by(board.turn, rooks[0]) and board.is_attacked_by(board.turn, rooks[1]))
    # Bishop pair
    bishop_pair = int(len(board.pieces(chess.BISHOP, board.turn)) == 2)
    # Overworked defenders
    overworked = sum(1 for sq, piece in pieces.items() if piece.color == board.turn and len(list(board.attackers(piece.color, sq))) > 1)

    # --- Tactical complexity ---
    checks = sum(1 for move in board.legal_moves if board.gives_check(move))
    captures = sum(1 for move in board.legal_moves if board.is_capture(move))
    pins = sum(1 for sq, piece in board.piece_map().items() if piece.color == board.turn and board.is_pinned(board.turn, sq))
    
    # For simplicity, approximate forks/skewers/discovered attacks
    tactical_motifs = pins  # placeholder for forks/skewers/discovered

    # --- Material imbalance (using standard piece values) ---
    values = {chess.PAWN: 1, chess.KNIGHT: 3, chess.BISHOP: 3, chess.ROOK: 5, chess.QUEEN: 9}
    material_white = sum(values.get(p.piece_type, 0) for sq, p in board.piece_map().items() if p.color == chess.WHITE)
    material_black = sum(values.get(p.piece_type, 0) for sq, p in board.piece_map().items() if p.color == chess.BLACK)
    material_imbalance = abs(material_white - material_black)

    # --- Game phase ---
    total_material = material_white + material_black
    phase = 0 if total_material > 40 else 1 if total_material > 20 else 2

    # --- Space control ---
    space_control = sum(1 for sq in board.pieces(chess.PAWN, board.turn) if (4 <= chess.square_rank(sq) <= 5))

    # --- Passed pawns ---
    passed_pawns = 0
    for p in pawns:
        f = chess.square_file(p)
        r = chess.square_rank(p)
        blocked = False
        if board.turn == chess.WHITE:
            for rr in range(r+1, 8):
                for dx in [-1, 0, 1]:
                    if 0 <= f + dx < 8 and board.piece_at(chess.square(f+dx, rr)):
                        blocked = True
                        break
                if blocked: break
        else:  # BLACK
            for rr in range(r-1, -1, -1):
                for dx in [-1, 0, 1]:
                    if 0 <= f + dx < 8 and board.piece_at(chess.square(f+dx, rr)):
                        blocked = True
                        break
                if blocked: break
        if not blocked:
            passed_pawns += 1

    # --- Center control ---
    center_squares = [chess.D4, chess.E4, chess.D5, chess.E5]
    center_control = sum(1 for sq in center_squares if board.is_attacked_by(board.turn, sq))

    # --- Return features ---
    return {
        "volatility": volatility_score,
        "move_ease": move_ease,
        "trap_susceptibility": trap_susceptibility,
        "king_exposure": enemy_control,
        "castling_status": castling_status,
        "defending_pieces": defending_pieces,
        "pawn_structure": pawn_structure_score,
        "doubled_pawns": doubled_pawns,
        "backward_pawns": backward_pawns,
        "pawn_majority": pawn_majority,
        "mobility": mobility,
        "piece_coordination": connectedness / (len(pieces) + 1e-6),
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
        "top_moves": [m.uci() for m in top_moves]
    }
