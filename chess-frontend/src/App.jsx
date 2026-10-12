
import { useEffect, useState } from "react";
import ChessBoard from "./components/ChessBoard";
import "./App.css";

function App() {
  const [squares, setSquares] = useState([]);
  const [selectedSquare, setSelectedSquare] = useState(null);
  const [currentTurn, setCurrentTurn] = useState("WHITE");
  const [gameStatus, setGameStatus] = useState("IN_PROGRESS");
  const [message, setMessage] = useState("");
  const [loading, setLoading] = useState(false);

  async function loadGame() {
    const [boardResponse, statusResponse] = await Promise.all([
      fetch("/api/chess/board"),
      fetch("/api/chess/status")
    ]);

    if (!boardResponse.ok || !statusResponse.ok) {
      throw new Error("Unable to load game");
    }

    const board = await boardResponse.json();
    const status = await statusResponse.json();

    setSquares(board.squares);
    setCurrentTurn(status.currentTurn);
    setGameStatus(status.status);
  }

  useEffect(() => {
    loadGame().catch(() => setMessage("Cannot connect to backend"));
  }, []);

  async function startGame() {
    setLoading(true);

    try {
      const response = await fetch("/api/chess/start", {
        method: "POST"
      });

      if (!response.ok) {
        throw new Error("Unable to start game");
      }

      setSelectedSquare(null);
      setMessage("");
      await loadGame();
    } catch (error) {
      setMessage(error.message);
    } finally {
      setLoading(false);
    }
  }

  async function handleSquareClick(row, column) {
    if (loading || gameStatus === "CHECKMATE" ||
        gameStatus === "STALEMATE" || gameStatus === "DRAW") {
      return;
    }

    const square = squares[row]?.[column];
    if (!square) return;

    // First click: select your own piece
    if (!selectedSquare) {
      if (square.piece?.color === currentTurn) {
        setSelectedSquare({ row, column });
        setMessage("");
      }
      return;
    }

    // Click selected square again to deselect
    if (selectedSquare.row === row &&
        selectedSquare.column === column) {
      setSelectedSquare(null);
      return;
    }

    // Select another piece of the same color
    if (square.piece?.color === currentTurn) {
      setSelectedSquare({ row, column });
      return;
    }

    const from = selectedSquare;
    const piece = squares[from.row][from.column].piece;

    let moveType = square.piece ? "CAPTURE" : "NORMAL";

    // Pawn promotion
    if (piece.type === "PAWN" &&
        (row === 0 || row === 7)) {
      moveType = "PROMOTION";
    }

    // King castling
    if (piece.type === "KING" &&
        Math.abs(column - from.column) === 2) {
      moveType = "CASTLING";
    }

    // Pawn en passant
    if (piece.type === "PAWN" &&
        column !== from.column &&
        !square.piece) {
      moveType = "EN_PASSANT";
    }

    setLoading(true);

    try {
      const response = await fetch("/api/chess/move", {
        method: "POST",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify({
          fromRow: from.row,
          fromColumn: from.column,
          toRow: row,
          toColumn: column,
          moveType
        })
      });

      if (!response.ok) {
        setMessage("Invalid move");
        return;
      }

      setSelectedSquare(null);
      setMessage("");
      await loadGame();
    } catch (error) {
      setMessage("Move failed. Check backend connection.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="app">
      <h1>Chess Game</h1>

      <div className="game-info">
        <p>
          Current Turn: <strong>{currentTurn}</strong>
        </p>
        <p>
          Game Status: <strong>{gameStatus}</strong>
        </p>
      </div>

      {squares.length === 8 ? (
        <ChessBoard
          squares={squares}
          selectedSquare={selectedSquare}
          onSquareClick={handleSquareClick}
        />
      ) : (
        <p>Loading chessboard...</p>
      )}

      {message && <p className="message">{message}</p>}

      <button
        className="new-game-button"
        onClick={startGame}
        disabled={loading}
      >
        New Game
      </button>
    </div>
  );
}

export default App;
