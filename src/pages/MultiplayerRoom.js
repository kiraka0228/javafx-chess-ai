import React, { useState, useEffect } from "react";
import socket from "../socket";
import Chessboard from "chessboardjsx";
import { Chess } from "chess.js";

function MultiplayerRoom() {
  const [roomId, setRoomId] = useState("");
  const [joined, setJoined] = useState(false);
  const [opponentJoined, setOpponentJoined] = useState(false);
  const [game, setGame] = useState(new Chess());
  const [fen, setFen] = useState("start");
  const [isMyTurn, setIsMyTurn] = useState(true);

  useEffect(() => {
    socket.on("opponentJoined", () => {
      setOpponentJoined(true);
    });

    socket.on("opponentMove", ({ from, to, fen }) => {
      game.move({ from, to });
      setFen(fen);
      setIsMyTurn(true);
    });

    socket.on("gameOver", () => {
      alert("Game Over!");
    });

    return () => {
      socket.off("opponentJoined");
      socket.off("opponentMove");
      socket.off("gameOver");
    };
  }, [game]);

  const joinRoom = () => {
    if (roomId) {
      socket.emit("joinRoom", roomId);
      setJoined(true);
    }
  };

  const onDrop = ({ sourceSquare, targetSquare }) => {
    if (!isMyTurn || game.game_over()) return;
    const move = game.move({ from: sourceSquare, to: targetSquare });
    if (move === null) return;

    const newFen = game.fen();
    setFen(newFen);
    setIsMyTurn(false);
    socket.emit("move", { roomId, from: sourceSquare, to: targetSquare, fen: newFen });
  };

  return (
    <div style={{ padding: 20 }}>
      {!joined ? (
        <div>
          <h2>Enter Room ID to Join Game</h2>
          <input
            type="text"
            value={roomId}
            onChange={(e) => setRoomId(e.target.value)}
          />
          <button onClick={joinRoom}>Join Room</button>
        </div>
      ) : (
        <div>
          <h3>Room: {roomId}</h3>
          <p>{opponentJoined ? "Opponent connected." : "Waiting for opponent..."}</p>
          <Chessboard position={fen} onDrop={onDrop} draggable={true} />
        </div>
      )}
    </div>
  );
}

export default MultiplayerRoom;
