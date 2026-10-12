package com.chess.chessboard.rules;

import com.chess.chessboard.model.Board;
import com.chess.chessboard.model.Move;

public interface GameRule {
    boolean isApplicable(Board board, Move move);

    boolean validate(Board board, Move move);
}
