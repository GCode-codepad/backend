// server.js
const express = require("express");
const http = require("http");
const app = express();
const server = http.createServer(app);
const io = require("socket.io")(server, {
  cors: {
    origin: "*", // Allow all origins for testing
    methods: ["GET", "POST"],
  },
});

const roomCode = {};

io.on("connection", (socket) => {
  console.log('New client connected:', socket.id);
  socket.emit("me", socket.id);

  socket.on("joinRoom", (roomId) => {
    socket.join(roomId);
    console.log(`User ${socket.id} joined room ${roomId}`);

    // Send existing code to the new user
    if (roomCode[roomId]) {
      socket.emit("codeChange", roomCode[roomId]);
    }

    // Notify others in the room
    socket.to(roomId).emit("userJoined", socket.id);
  });

  socket.on("disconnect", () => {
    socket.broadcast.emit("callEnded");
    console.log('Client disconnected:', socket.id);
  });

  socket.on("callUser", (data) => {
    console.log(`callUser from ${data.from} to ${data.userToCall}`);
    io.to(data.userToCall).emit("callUser", {
      signal: data.signalData,
      from: data.from,
      name: data.name,
    });
  });

  socket.on("answerCall", (data) => {
    console.log(`answerCall from ${socket.id} to ${data.to}`);
    io.to(data.to).emit("callAccepted", data.signal);
  });

  // Handle code changes
  socket.on("codeChange", ({ roomId, code }) => {
    console.log(`Code change in room ${roomId} by ${socket.id}`);
    roomCode[roomId] = code;
    socket.to(roomId).emit("codeChange", code);
  });

  socket.on('getUsersInRoom', (roomId) => {
    const clients = io.sockets.adapter.rooms.get(roomId);
    const users = clients ? Array.from(clients) : [];
    socket.emit('usersInRoom', users);
  });
});

server.listen(8002, () => console.log("Server is running on port 8002"));
