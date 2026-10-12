
const pieces = {
  WHITE: {
    KING: "♔",
    QUEEN: "♕",
    ROOK: "♖",
    BISHOP: "♗",
    KNIGHT: "♘",
    PAWN: "♙"
  },
  BLACK: {
    KING: "♚",
    QUEEN: "♛",
    ROOK: "♜",
    BISHOP: "♝",
    KNIGHT: "♞",
    PAWN: "♟"
  }
};

function ChessBoard({ squares, selectedSquare, onSquareClick }) {

  return (
    <div className="chess-board">
      {squares.slice().reverse().map((row, rowIndex) =>
        row.map((square, colIndex) => {

          const actualRow = 7 - rowIndex;
          const piece = square.piece;

          const isSelected =
            selectedSquare?.row === actualRow &&
            selectedSquare?.column === colIndex;

          const isLight = (actualRow + colIndex) % 2 !== 0;

          return (
            <div
              key={`${actualRow}-${colIndex}`}
              className={`square ${isLight ? "light" : "dark"} ${
                isSelected ? "selected" : ""
              }`}
              onClick={() => onSquareClick(actualRow, colIndex)}
            >
              {piece && (
                <span className="chess-piece">
                  {pieces[piece.color]?.[piece.type]}
                </span>
              )}
            </div>
          );
        })
      )}
    </div>
  );
}

export default ChessBoard;
