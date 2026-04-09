# Chess Game

A fully-featured chess application built in Java with both console and GUI modes, LAN multiplayer support, and a computer opponent.

## Features

- **JavaFX GUI** with a login system, dashboard, settings, and player profiles
- **Console Mode** for lightweight terminal-based play
- **Player vs Player** (local) on both GUI and console
- **Player vs Computer** with a random-move AI opponent
- **LAN Multiplayer** using socket-based networking (host/client architecture)
- **Game Logic** including legal move validation, check/checkmate detection, castling, en passant, and pawn promotion
- **Player Statistics** with rating tracking, match history, and a leaderboard
- **Sound Effects** for moves and game events
- **Save/Load** game state via snapshot files

## Project Structure

```
src/main/java/
├── Driver/           # Entry points and game mode launchers
│   ├── Launcher.java         # Main entry point (launches GUI)
│   ├── driverclass.java      # Console menu entry point
│   ├── PvsPonConsole.java    # Console PvP game loop
│   └── landriverclass.java   # Console LAN game loop
├── Frontend/         # JavaFX UI components
│   ├── Loginpage.java        # Login/signup screen
│   ├── DashBoardView.java    # Main dashboard
│   ├── chessboard.java       # GUI chessboard (local play)
│   ├── chesslan.java         # GUI chessboard (LAN play)
│   ├── ProfileView.java      # Player profile & stats
│   └── SettingsView.java     # Settings screen
├── gamelogic/        # Core chess engine
│   ├── Board.java            # Board state and move validation
│   ├── piece.java            # Abstract piece class
│   ├── King.java, Queen.java, Rook.java, Bishop.java, Knight.java, Pawn.java
│   ├── Player.java           # Player representation
│   ├── ComputerPlayer.java   # AI opponent
│   ├── MoveManager.java      # Move execution and special moves
│   ├── Snapshot.java         # Save/load game state
│   └── SoundManager.java     # Sound effects
├── Network/          # LAN multiplayer
│   ├── ChessServer.java      # Host (server) side
│   ├── ChessClient.java      # Client side
│   ├── GUIserver.java        # GUI server handler
│   ├── GUIclient.java        # GUI client handler
│   └── Move.java             # Serializable move object
└── makingstats/      # Player statistics
    ├── Players.java          # Player data management
    ├── Rating.java           # ELO rating calculations
    ├── History.java          # Match history
    └── LeaderBoard.java      # Leaderboard rankings
```

## Prerequisites

- **JDK 21** or later
- **JavaFX SDK 21** ([download here](https://openjfx.io/))

## How to Run

### GUI Mode (recommended)

```bash
javac -sourcepath src/main/java --module-path /path/to/javafx-sdk/lib --add-modules javafx.controls,javafx.fxml,javafx.media,javafx.swing -d out src/main/java/Driver/Launcher.java

java --module-path /path/to/javafx-sdk/lib --add-modules javafx.controls,javafx.fxml,javafx.media,javafx.swing -cp out Driver.Launcher
```

### Console Mode

```bash
javac -sourcepath src/main/java -d out src/main/java/Driver/driverclass.java

java -cp out Driver.driverclass
```

### LAN Multiplayer

1. One player starts as **host** (server)
2. The other player connects as **client** using the host's IP address
3. Available in both GUI and console modes

## Built With

- **Java 21**
- **JavaFX 21** for the graphical interface
- **Maven** for dependency management

## Authors

- Eric-ITxx
