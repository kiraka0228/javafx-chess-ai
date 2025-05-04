const { Server } = require("socket.io");

function setupSocket(server) {
  const io = new Server(server, {
    cors: {
      origin: "*",
      methods: ["GET", "POST"]
    }
  });

  io.on("connection", (socket) => {
    console.log("User connected:", socket.id);

    socket.on("joinRoom", (roomId) => {
      socket.join(roomId);
      console.log(`Socket ${socket.id} joined room ${roomId}`);
      socket.to(roomId).emit("opponentJoined");
    });

    socket.on("move", ({ roomId, from, to, fen }) => {
      socket.to(roomId).emit("opponentMove", { from, to, fen });
    });

    socket.on("gameOver", (roomId) => {
      io.in(roomId).emit("gameOver");
    });

    socket.on("disconnect", () => {
      console.log("User disconnected:", socket.id);
    });
  });
}

module.exports = setupSocket;
