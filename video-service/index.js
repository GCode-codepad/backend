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

const roomCode = {}; // Holds the current code for each room
const roomLanguage = {};

io.on("connection", (socket) => {
  console.log('New client connected:', socket.id);
  socket.emit("me", socket.id);

  socket.on("joinRoom", (roomId) => {
    socket.join(roomId);
    console.log(`User ${socket.id} joined room ${roomId}`);

    // Initialize room code if not present
    if (!roomCode[roomId]) {
      roomCode[roomId] = '// Start coding...';
    }

    // Send existing code to the new user
    socket.emit("codeChange", { code: roomCode[roomId] });
    socket.emit("languageChange", { language: roomLanguage[roomId] });

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

  socket.on('getUsersInRoom', (roomId) => {
    const clients = io.sockets.adapter.rooms.get(roomId);
    const users = clients ? Array.from(clients) : [];
    socket.emit('usersInRoom', users);
  });

  // Handle code changes
  socket.on('codeChange', ({ roomId, code }) => {
    console.log(`Received code change for room ${roomId}`);
    // Update the room's code
    roomCode[roomId] = code;

    // Broadcast the code change to all other clients in the room
    socket.to(roomId).emit('codeChange', { code });
  });

  // Handle language changes
  socket.on('languageChange', ({ roomId, language }) => {
    console.log(`Received language change for room ${roomId}: ${language}`);
    // Update the room's language
    roomLanguage[roomId] = language;

    // Broadcast the language change to all other clients in the room
    socket.to(roomId).emit('languageChange', { language });
  });

  // Handle code output
  socket.on('codeOutput', ({ roomId, output }) => {
    // Broadcast the code output to all other clients in the room
    socket.to(roomId).emit('codeOutput', { output });
  });
});

server.listen(8002, () => console.log("Server is running on port 8002"));
