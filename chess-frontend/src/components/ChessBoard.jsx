const pieces = { WHITE: { KING: "♔", QUEEN: "♕", ROOK: "♖", BISHOP: "♗", KNIGHT: "♘", PAWN: "♙" }, BLACK: { KING: "♚", QUEEN: "♛", ROOK: "♜", BISHOP: "♝", KNIGHT: "♞", PAWN: "♟" } };
const files = ["a", "b", "c", "d", "e", "f", "g", "h"];
function ChessBoard({ squares, selectedSquare, onSquareClick, disabled }) {
  return <div className={`chess-board ${disabled ? "disabled" : ""}`} role="grid" aria-label="Chess board">
    {squares.slice().reverse().map((row, rowIndex) => row.map((square, colIndex) => {
      const actualRow = 7 - rowIndex; const piece = square.piece; const isSelected = selectedSquare?.row === actualRow && selectedSquare?.column === colIndex; const isLight = (actualRow + colIndex) % 2 !== 0;
      const label = `${files[colIndex]}${actualRow + 1}${piece ? `, ${piece.color.toLowerCase()} ${piece.type.toLowerCase()}` : ""}`;
      return <button key={`${actualRow}-${colIndex}`} className={`square ${isLight ? "light" : "dark"} ${isSelected ? "selected" : ""}`} onClick={() => onSquareClick(actualRow, colIndex)} role="gridcell" aria-label={label} aria-pressed={isSelected}>
        {colIndex === 0 && <span className="rank-label">{actualRow + 1}</span>}{actualRow === 0 && <span className="file-label">{files[colIndex]}</span>}{piece && <span className={`chess-piece ${piece.color.toLowerCase()}`}>{pieces[piece.color]?.[piece.type]}</span>}
      </button>;
    }))}
  </div>;
}
export default ChessBoard;
