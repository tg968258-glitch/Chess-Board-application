package com.chess.chessboard.model;

import com.chess.chessboard.enums.MoveType;
import com.chess.chessboard.pieces.Piece;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Move {
    private Position from;
    private Position to;
    private Piece piece;
    private Piece capturedPiece;
private MoveType moveType;
}
