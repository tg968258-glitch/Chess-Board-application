package com.chess.chessboard.evaluator;

import com.chess.chessboard.enums.Color;
import com.chess.chessboard.enums.GameStatus;
import com.chess.chessboard.model.Board;

public interface GameStateEvaluator {
    GameStatus evaluate (Board board, Color color);

}
