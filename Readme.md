# Royal Board

A simple two-player chess application built with **Java 21, Spring Boot, React.js, and Vite**. The backend handles chess rules and move validation, while the frontend displays an interactive 8×8 chessboard.

The project is designed to demonstrate **object-oriented programming (OOP)** concepts and how a React frontend communicates with a Spring Boot REST API.

## Tech Stack

| Backend | Frontend |
| --- | --- |
| Java 21 | React.js (JavaScript / JSX) |
| Spring Boot | Vite |
| REST APIs | CSS Grid |
| Lombok | Fetch API |
| Maven | Unicode chess symbols |

**Database:** Not required. The current game is stored in memory.

## Features

- Standard 8×8 chessboard with white and black pieces
- Start or reset a game
- Select a piece and choose a destination square
- Validate moves on the backend
- Alternate turns between White and Black
- Display the current turn and game status
- Handle normal moves and captures
- Implement castling, en passant, and automatic queen promotion through backend rules
- Evaluate check, checkmate, and stalemate using the existing evaluator
- Display chess pieces using Unicode symbols instead of image files
- Protect every chess API endpoint with stateless Spring Security authentication
- Responsive sign-in and game workspace with turn state and move history

## Project Structure

The backend and frontend can be kept in separate folders:

```text
Chess-Board-Application/
├── backend/
│   └── src/main/java/com/chess/chessboard/
│       ├── Controller/
│       │   └── ChessGameController.java
│       ├── service/
│       │   └── ChessGameService.java
│       ├── model/
│       │   ├── Board.java
│       │   ├── ChessGame.java
│       │   ├── Move.java
│       │   ├── MoveHistory.java
│       │   ├── Player.java
│       │   ├── Position.java
│       │   └── Square.java
│       ├── pieces/
│       ├── rules/
│       ├── validator/
│       ├── evaluator/
│       ├── factory/
│       ├── enums/
│       └── ChessBoardApplication.java
└── chess-frontend/
    ├── src/
    │   ├── components/
    │   │   └── ChessBoard.jsx
    │   ├── App.jsx
    │   ├── App.css
    │   ├── index.css
    │   └── main.jsx
    ├── vite.config.js
    └── package.json
```

## How It Works

1. React opens `/ws/chess` and authenticates through the WebSocket connection.
2. Spring sends the authoritative board, turn, and game status through a `STATE` message.
3. The user clicks a chess piece and then a destination square.
4. React sends a `MOVE` message containing the source, destination, and move type.
5. `ChessGame` uses the validator, special-move rules, and game-state evaluator to decide whether the move is allowed.
6. If accepted, the backend updates the board and changes the current turn.
7. Spring broadcasts the updated board and status to every authenticated socket.

## WebSocket Protocol

The frontend uses `ws://localhost:8080/ws/chess` in development. Authentication
is the first socket message, so credentials are not placed in a URL:

```json
{"type":"AUTHENTICATE","username":"player","password":"chess123"}
```

Game commands use `START_GAME`, `MOVE`, and `UNDO`. The backend responds with
`AUTHENTICATED`, `STATE`, or `ERROR` messages. `STATE` messages include an event
name (`SYNC`, `MOVE`, `MOVE_UNDONE`, or `GAME_STARTED`) and the authoritative board, status,
and current turn. Successful state changes are broadcast to all authenticated
connections.

### Production WebSocket configuration

Vite's `server.proxy` only runs during `npm run dev`; it is not part of the
production build. If the frontend and backend are deployed on different hosts,
set this variable in the **frontend** hosting service before building:

```text
VITE_WS_URL=https://your-backend.onrender.com
```

The frontend converts `https` to `wss` and appends `/ws/chess`. A complete URL
such as `wss://your-backend.onrender.com/ws/chess` is also accepted. In the
backend hosting service, allow the exact deployed frontend origin (with no path
or trailing slash):

```text
CHESS_ALLOWED_ORIGIN=https://your-frontend.example.com
```

After changing `VITE_WS_URL`, rebuild/redeploy the frontend because Vite embeds
`VITE_*` values at build time. The backend must also support WebSocket upgrade
requests on `/ws/chess`.

## REST API Endpoints

Base URL: `http://localhost:8080/api/chess`

| Method | Endpoint | Description |
| --- | --- | --- |
| `POST` | `/start` | Start or reset the game |
| `GET` | `/board` | Get the current chessboard |
| `POST` | `/move` | Attempt a chess move |
| `GET` | `/status` | Get game status and current turn |
| `GET` | `/me` | Get the authenticated player's username |

### Example: Make a Move

**Request:** `POST http://localhost:8080/api/chess/move`

```json
{
  "fromRow": 1,
  "fromColumn": 4,
  "toRow": 3,
  "toColumn": 4,
  "moveType": "NORMAL"
}
```

This example moves the White pawn from **e2 to e4**.

A successful request returns:

```text
Move successful
```

### Example: Game Status

**Request:** `GET http://localhost:8080/api/chess/status`

```json
{
  "status": "IN_PROGRESS",
  "currentTurn": "BLACK"
}
```

The status example assumes White has just completed a valid move.

## Getting Started

### Prerequisites

- Java 21
- Maven (or the Maven wrapper included in your backend)
- Node.js and npm
- An IDE such as IntelliJ IDEA or VS Code

### 1. Run the Spring Boot Backend

Open the backend project in IntelliJ IDEA and run `ChessBoardApplication.java`.

Alternatively, from the backend folder:

```bash
mvn spring-boot:run
```

The backend should run at `http://localhost:8080`.

The API uses HTTP Basic authentication. For local development, sign in with
`player` / `chess123`. Override both values outside local development:

```powershell
$env:CHESS_USERNAME="your-player-name"
$env:CHESS_PASSWORD="use-a-strong-password"
$env:CHESS_ALLOWED_ORIGIN="http://localhost:5173"
.\mvnw.cmd spring-boot:run
```

The backend is stateless, passwords are encoded through Spring Security's
delegating password encoder, and the frontend keeps the Basic credential only
in the browser session (it is cleared when the tab is closed).

**Database configuration:** The current implementation does not use a database. If Spring Boot tries to configure a DataSource because JPA is installed, remove the unused JPA dependency or configure the appropriate auto-configuration exclusion for your Spring Boot version.

### 2. Run the React Frontend

Open a second terminal in the frontend folder:

```bash
npm install
npm run dev
```

Open the address shown by Vite, usually `http://localhost:5173`.

### 3. Configure the Vite Development Proxy

Make sure `vite.config.js` forwards API calls to Spring Boot:

```javascript
import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      "/api": {
        target: "http://localhost:8080",
        changeOrigin: true
      },
      "/ws": {
        target: "ws://localhost:8080",
        ws: true,
        changeOrigin: true
      }
    }
  }
});
```

React should use relative API URLs such as `fetch('/api/chess/board')`.

## Chess Piece Display

The frontend uses Unicode chess characters rather than image files:

| Piece | White | Black |
| --- | --- | --- |
| King | ♔ | ♚ |
| Queen | ♕ | ♛ |
| Rook | ♖ | ♜ |
| Bishop | ♗ | ♝ |
| Knight | ♘ | ♞ |
| Pawn | ♙ | ♟ |

The backend provides each piece's `color` and `type`, and React maps those values to the correct symbol.

## Main Backend Components

- **`ChessGame`** — Manages turns, the board, move history, and overall game status.
- **`Board`** — Stores the 8×8 squares and applies piece movements.
- **`Piece` and subclasses** — Define how each type of chess piece moves.
- **`BasicMoveValidator`** — Checks move validity and delegates special moves to rules.
- **`CastlingRule`, `EnPassantRule`, `PromotionRule`** — Validate special chess moves.
- **`StandardGameStateEvaluator`** — Evaluates check, checkmate, and stalemate.
- **`ChessGameService`** — Manages the in-memory game and delegates operations to `ChessGame`.
- **`ChessGameController`** — Exposes the REST endpoints consumed by React.

## Future Improvements

- Highlight all legal destination squares when a piece is selected
- Improve special-move edge-case handling
- Complete draw detection
- Add move history to the frontend
- Add undo functionality with correct board restoration
- Support multiple independent games
- Add optional online multiplayer support

## Learning Objectives

This project demonstrates Java abstraction, inheritance, polymorphism, interfaces, encapsulation, and separation of responsibilities, alongside React components, props, state management, event handling, and REST API integration.
