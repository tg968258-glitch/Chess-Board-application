package com.chess.chessboard.validator;

import com.chess.chessboard.model.Board;
import com.chess.chessboard.model.Move;
import com.chess.chessboard.model.Position;
import com.chess.chessboard.pieces.Piece;

public interface MoveValidator {
        boolean validate(Board board, Move move);
    }
