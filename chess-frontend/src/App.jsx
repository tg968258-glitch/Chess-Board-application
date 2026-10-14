import { useEffect, useMemo, useRef, useState } from "react";
import ChessBoard from "./components/ChessBoard";
import "./App.css";

const FILES = ["a", "b", "c", "d", "e", "f", "g", "h"];
const FINISHED_STATES = new Set(["CHECKMATE", "STALEMATE", "DRAW"]);
const INITIAL_TIME_SECONDS = 10 * 60;
const PROMOTION_OPTIONS = [
  { type: "QUEEN", label: "Queen", symbols: { WHITE: "♕", BLACK: "♛" } },
  { type: "ROOK", label: "Rook", symbols: { WHITE: "♖", BLACK: "♜" } },
  { type: "BISHOP", label: "Bishop", symbols: { WHITE: "♗", BLACK: "♝" } },
  { type: "KNIGHT", label: "Knight", symbols: { WHITE: "♘", BLACK: "♞" } },
];

function webSocketUrl() {
  const configuredUrl = import.meta.env.VITE_WS_URL?.trim();
  if (!configuredUrl) {
    const protocol = window.location.protocol === "https:" ? "wss:" : "ws:";
    return `${protocol}//${window.location.host}/ws/chess`;
  }

  const url = new URL(configuredUrl, window.location.href);
  if (url.protocol === "https:") url.protocol = "wss:";
  if (url.protocol === "http:") url.protocol = "ws:";
  if (url.pathname === "/") url.pathname = "/ws/chess";
  return url.toString();
}

function squareName(row, column) { return `${FILES[column]}${row + 1}`; }
function formatTime(totalSeconds) {
  const minutes = Math.floor(totalSeconds / 60);
  const seconds = totalSeconds % 60;
  return `${String(minutes).padStart(2, "0")}:${String(seconds).padStart(2, "0")}`;
}
function Icon({ name }) {
  const paths = { rotate: "M20 11a8 8 0 1 0-2.3 5.7M20 4v7h-7", undo: "M9 8l-4 4 4 4m-4-4h8a6 6 0 0 1 6 6", logout: "M10 17l5-5-5-5m5 5H3m10-9h6a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-6", shield: "M12 3l8 3v5c0 5-3.4 8.7-8 10-4.6-1.3-8-5-8-10V6l8-3Z" };
  return <svg aria-hidden="true" viewBox="0 0 24 24"><path d={paths[name]} /></svg>;
}

function BrandLogo() {
  return <svg className="brand-logo" aria-hidden="true" viewBox="0 0 64 64">
    <path className="logo-crown" d="M16 22l9 7 7-15 7 15 9-7-4 25H20l-4-25Z" />
    <path className="logo-base" d="M20 47h24M23 52h18" />
    <circle cx="16" cy="20" r="3" /><circle cx="32" cy="12" r="3" /><circle cx="48" cy="20" r="3" />
  </svg>;
}
function Login({ onLogin, error, loading }) {
  const [mode, setMode] = useState("register");
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const registering = mode === "register";
  return <main className="login-shell">
    <section className="login-brand" aria-label="Chess application introduction">
      <div className="hero-brand"><div className="brand-mark large"><BrandLogo /></div><div><strong>Royal Board</strong><span>Classic chess, simply played</span></div></div>
      <div className="hero-copy"><p className="eyebrow">A board for two minds</p><h1>Every move<br /><em>tells a story.</em></h1><p>Settle in, take your time, and let the position speak.</p></div>
      <div className="position-card" aria-hidden="true"><div><span>Current line</span><strong>e2 — e4</strong></div><div className="position-sequence"><span>♙</span><i></i><span>♟</span></div><small>WHITE TO MOVE</small></div>
    </section>
    <section className="login-panel"><form className="login-card" onSubmit={(event) => { event.preventDefault(); onLogin(username.trim(), password, registering); }}>
      <div className="auth-tabs" aria-label="Account action"><button type="button" className={registering ? "active" : ""} onClick={() => setMode("register")}>New player</button><button type="button" className={!registering ? "active" : ""} onClick={() => setMode("signin")}>Sign in</button></div>
      <div className="account-heading"><div className="account-icon" aria-hidden="true">♙</div><div><p className="eyebrow">{registering ? "Your board awaits" : "Continue your game"}</p><h2>{registering ? "Choose a name. Make your move." : "Good to see you again."}</h2></div></div>
      <p className="account-copy">{registering ? "Create a private player profile—no ranking or personal details needed." : "Enter your player name and password to return to Royal Board."}</p>
      <div className="account-fields"><label><span>Player name</span><input autoComplete="username" value={username} onChange={(e) => setUsername(e.target.value)} placeholder="e.g. NightRook" minLength="3" maxLength="20" pattern="[A-Za-z0-9]+" required /></label>
      <label><span>Password</span><input autoComplete={registering ? "new-password" : "current-password"} type="password" value={password} onChange={(e) => setPassword(e.target.value)} placeholder={registering ? "Create 8+ characters" : "Your password"} minLength="8" maxLength="72" required /></label></div>
      {error && <p className="form-error" role="alert">{error}</p>}
      <button className="primary-button account-submit" disabled={loading || !username || !password}><span>{loading ? "Opening the board…" : registering ? "Join Royal Board" : "Enter the board"}</span><span aria-hidden="true">→</span></button>
      <p className="account-note"><Icon name="shield" /> Your player profile stays private.</p>
    </form></section>
  </main>;
}
function App() {
  const [credentials, setCredentials] = useState(() => sessionStorage.getItem("chessAuth"));
  const [authenticated, setAuthenticated] = useState(false);
  const [username, setUsername] = useState(() => sessionStorage.getItem("chessUser") || "Player");
  const [squares, setSquares] = useState([]);
  const [selectedSquare, setSelectedSquare] = useState(null);
  const [currentTurn, setCurrentTurn] = useState("WHITE");
  const [gameStatus, setGameStatus] = useState("IN_PROGRESS");
  const [message, setMessage] = useState("");
  const [loading, setLoading] = useState(false);
  const [loginError, setLoginError] = useState("");
  const [moves, setMoves] = useState([]);
  const [clocks, setClocks] = useState({ WHITE: INITIAL_TIME_SECONDS, BLACK: INITIAL_TIME_SECONDS });
  const [clockExpired, setClockExpired] = useState(null);
  const [pendingPromotion, setPendingPromotion] = useState(null);
  const [authCommand, setAuthCommand] = useState("AUTHENTICATE");
  const socketRef = useRef(null);

  useEffect(() => {
    if (!credentials) return;
    const [user, password] = atob(credentials).split(/:(.*)/s);
    const socket = new WebSocket(webSocketUrl());
    socketRef.current = socket;

    socket.onopen = () => socket.send(JSON.stringify({ type: authCommand, username: user, password }));
    socket.onmessage = (event) => {
      const payload = JSON.parse(event.data);
      if (payload.type === "AUTHENTICATED") {
        sessionStorage.setItem("chessAuth", credentials);
        sessionStorage.setItem("chessUser", payload.username);
        setUsername(payload.username); setAuthenticated(true); setLoading(false); setLoginError("");
        return;
      }
      if (payload.type === "STATE") {
        setSquares(payload.board.squares); setCurrentTurn(payload.currentTurn); setGameStatus(payload.status); setSelectedSquare(null); setPendingPromotion(null); setLoading(false);
        if (payload.event === "GAME_STARTED") {
          setMoves([]); setClocks({ WHITE: INITIAL_TIME_SECONDS, BLACK: INITIAL_TIME_SECONDS }); setClockExpired(null); setMessage("A fresh board is ready.");
        } else if (payload.event === "MOVE_UNDONE") {
          setMoves((history) => history.slice(0, -1)); setClockExpired(null); setMessage("Last move undone.");
        } else if (payload.event === "MOVE" && payload.move) {
          const move = payload.move;
          setMoves((history) => [...history, { piece: move.pieceType, from: squareName(move.fromRow, move.fromColumn), to: squareName(move.toRow, move.toColumn), type: move.moveType, promotion: move.promotionPieceType }]);
          setMessage("");
        }
        return;
      }
      if (payload.type === "ERROR") {
        setLoading(false);
        if (payload.code === "AUTHENTICATION_FAILED" || payload.code === "UNAUTHORIZED" || payload.code === "REGISTRATION_FAILED") {
          sessionStorage.removeItem("chessAuth"); sessionStorage.removeItem("chessUser"); setAuthenticated(false); setCredentials(null); setLoginError(payload.message);
        } else {
          setMessage(payload.code === "INVALID_MOVE" ? "That move is not legal. Try another square." : payload.message);
        }
      }
    };
    socket.onerror = () => { setLoading(false); setLoginError("The live game server is unavailable."); };
    socket.onclose = () => { if (socketRef.current === socket) socketRef.current = null; };
    return () => { socketRef.current = null; socket.close(); };
  }, [authCommand, credentials]);

  useEffect(() => {
    if (!credentials || loading || pendingPromotion || clockExpired || FINISHED_STATES.has(gameStatus)) return;
    const timer = window.setInterval(() => {
      setClocks((previous) => {
        const nextValue = Math.max(0, previous[currentTurn] - 1);
        return { ...previous, [currentTurn]: nextValue };
      });
    }, 1000);
    return () => window.clearInterval(timer);
  }, [clockExpired, credentials, currentTurn, gameStatus, loading, pendingPromotion]);

  useEffect(() => {
    if (clocks[currentTurn] !== 0 || clockExpired) return;
    // A zero clock is an external timer event that ends local interaction.
    // eslint-disable-next-line react-hooks/set-state-in-effect
    setClockExpired(currentTurn);
    setMessage(`${currentTurn === "WHITE" ? "White" : "Black"} ran out of time.`);
  }, [clockExpired, clocks, currentTurn]);

  function login(user, password, registering) {
    setLoading(true); setLoginError(""); setAuthCommand(registering ? "REGISTER" : "AUTHENTICATE");
    setCredentials(btoa(`${user}:${password}`));
  }
  function logout() {
    socketRef.current?.close(); sessionStorage.removeItem("chessAuth"); sessionStorage.removeItem("chessUser"); setAuthenticated(false); setCredentials(null); setSquares([]); setMoves([]); setPendingPromotion(null);
  }
  function startGame() {
    if (socketRef.current?.readyState !== WebSocket.OPEN) { setMessage("The live connection is not ready."); return; }
    setLoading(true);
    socketRef.current.send(JSON.stringify({ type: "START_GAME" }));
  }
  function undoMove() {
    if (socketRef.current?.readyState !== WebSocket.OPEN) { setMessage("The live connection is not ready."); return; }
    setLoading(true);
    socketRef.current.send(JSON.stringify({ type: "UNDO" }));
  }
  function sendMove(move, promotionPieceType = null) {
    if (socketRef.current?.readyState !== WebSocket.OPEN) { setMessage("The live connection is not ready."); return; }
    setLoading(true);
    socketRef.current.send(JSON.stringify({ type: "MOVE", move: { ...move, promotionPieceType } }));
  }
  function choosePromotion(promotionPieceType) {
    if (!pendingPromotion) return;
    sendMove(pendingPromotion, promotionPieceType);
  }
  function handleSquareClick(row, column) {
    if (loading || pendingPromotion || clockExpired || FINISHED_STATES.has(gameStatus)) return;
    const square = squares[row]?.[column]; if (!square) return;
    if (!selectedSquare) { if (square.piece?.color === currentTurn) { setSelectedSquare({ row, column }); setMessage(""); } return; }
    if (selectedSquare.row === row && selectedSquare.column === column) { setSelectedSquare(null); return; }
    if (square.piece?.color === currentTurn) { setSelectedSquare({ row, column }); return; }
    const from = selectedSquare; const piece = squares[from.row][from.column].piece;
    let moveType = square.piece ? "CAPTURE" : "NORMAL";
    if (piece.type === "PAWN" && (row === 0 || row === 7)) moveType = "PROMOTION";
    if (piece.type === "KING" && Math.abs(column - from.column) === 2) moveType = "CASTLING";
    if (piece.type === "PAWN" && column !== from.column && !square.piece) moveType = "EN_PASSANT";
    const move = { fromRow: from.row, fromColumn: from.column, toRow: row, toColumn: column, moveType, pieceType: piece.type };
    if (moveType === "PROMOTION") {
      setPendingPromotion(move);
      setMessage("Choose the piece for pawn promotion.");
      return;
    }
    sendMove(move);
  }
  const statusLabel = useMemo(() => gameStatus.replaceAll("_", " ").toLowerCase(), [gameStatus]);
  if (!authenticated) return <Login onLogin={login} error={loginError} loading={loading} />;
  return <div className="app-shell">
    <header className="topbar"><a className="brand" href="#game" aria-label="Royal Board home"><span className="brand-mark"><BrandLogo /></span><span>Royal Board</span></a><div className="header-actions"><span className="user-chip"><span>{username.charAt(0).toUpperCase()}</span>{username}</span><button className="icon-button" onClick={logout} title="Sign out"><Icon name="logout" /></button></div></header>
    <main className="game-layout" id="game"><section className="board-area">
      <div className="game-heading"><div><p className="eyebrow">Local match</p><h1>Classic chess</h1></div><div className={`status-badge ${gameStatus !== "IN_PROGRESS" ? "finished" : ""}`}><span></span>{statusLabel}</div></div>
      <div className="player-row opponent"><div className="avatar dark">B</div><div><strong>Black</strong><span>Opponent</span></div><div className={`player-clock ${currentTurn === "BLACK" && !clockExpired ? "active" : ""} ${clockExpired === "BLACK" ? "expired" : ""}`} aria-label={`Black clock ${formatTime(clocks.BLACK)}`}><span className="clock-dot"></span>{formatTime(clocks.BLACK)}</div></div>
      <div className="board-frame">{squares.length === 8 ? <ChessBoard squares={squares} selectedSquare={selectedSquare} onSquareClick={handleSquareClick} disabled={loading || Boolean(pendingPromotion)} /> : <div className="board-loading"><span>♞</span>Preparing the board…</div>}</div>
      <div className="player-row"><div className="avatar light-avatar">W</div><div><strong>White</strong><span>You are playing locally</span></div><div className={`player-clock ${currentTurn === "WHITE" && !clockExpired ? "active" : ""} ${clockExpired === "WHITE" ? "expired" : ""}`} aria-label={`White clock ${formatTime(clocks.WHITE)}`}><span className="clock-dot"></span>{formatTime(clocks.WHITE)}</div></div>{message && <div className="notice" role="status">{message}</div>}
    </section><aside className="game-panel">
      <div className="panel-section turn-card"><p className="eyebrow">On the clock</p><div className="turn-display"><div className={`turn-piece ${currentTurn.toLowerCase()}`}>{currentTurn === "WHITE" ? "♔" : "♚"}</div><div><span>{clockExpired ? "Time expired" : "Current turn"}</span><strong>{clockExpired ? `${clockExpired === "WHITE" ? "Black" : "White"} wins` : currentTurn.charAt(0) + currentTurn.slice(1).toLowerCase()}</strong></div></div><div className="clock-summary"><div><span>White</span><strong>{formatTime(clocks.WHITE)}</strong></div><div><span>Black</span><strong>{formatTime(clocks.BLACK)}</strong></div></div></div>
      <div className="panel-section history-section"><div className="section-title"><div><p className="eyebrow">Game record</p><h2>Move history</h2></div><span>{moves.length}</span></div><div className="move-list">{moves.length === 0 ? <div className="empty-history"><span>♙</span><p>Your moves will appear here.</p><small>Select a piece, then choose its destination.</small></div> : moves.map((move, index) => <div className="move-row" key={`${move.from}-${move.to}-${index}`}><span>{index + 1}</span><strong>{move.piece.toLowerCase()}</strong><code>{move.from} → {move.to}</code>{move.type !== "NORMAL" && <em>{move.type.toLowerCase()}{move.promotion ? ` → ${move.promotion.toLowerCase()}` : ""}</em>}</div>)}</div></div>
      <div className="panel-actions"><div className="action-buttons"><button className="undo-button" onClick={undoMove} disabled={loading || moves.length === 0}><Icon name="undo" />Undo</button><button className="secondary-button" onClick={startGame} disabled={loading}><Icon name="rotate" />New game</button></div><p><Icon name="shield" />Moves are validated securely by the server.</p></div>
    </aside></main>
    {pendingPromotion && <div className="promotion-backdrop" role="presentation">
      <section className="promotion-dialog" role="dialog" aria-modal="true" aria-labelledby="promotion-title">
        <p className="eyebrow">Pawn promotion</p><h2 id="promotion-title">Choose your new piece</h2>
        <p>Your pawn reached the final rank. Select the piece it should become.</p>
        <div className="promotion-options">{PROMOTION_OPTIONS.map((option) => <button key={option.type} onClick={() => choosePromotion(option.type)} disabled={loading} aria-label={`Promote pawn to ${option.label}`}><span>{option.symbols[currentTurn]}</span><strong>{option.label}</strong></button>)}</div>
        <button className="promotion-cancel" onClick={() => { setPendingPromotion(null); setMessage(""); }} disabled={loading}>Cancel move</button>
      </section>
    </div>}
  </div>;
}
export default App;
