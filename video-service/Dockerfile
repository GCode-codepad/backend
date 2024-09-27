# Stage 1: Build the application (if applicable)
FROM node:18-alpine AS build

# Set the working directory
WORKDIR /app

# Copy package.json and package-lock.json first for better caching
COPY package*.json ./

# Install dependencies
RUN npm install --production

# Copy the rest of the application code
COPY . .

# If you have a build step (e.g., TypeScript compilation), add it here
# RUN npm run build

# Stage 2: Run the application
FROM node:18-alpine

# Set the working directory
WORKDIR /app

# Copy only the necessary files from the build stage
COPY --from=build /app ./

# Add metadata labels
LABEL authors="yuqi.guo17@gmail.com"

# Set environment variables (optional)
ENV NODE_ENV=production

# Expose the specified port
EXPOSE 8082

# Install only production dependencies (redundant if already done in build stage)
# RUN npm prune --production

# Define the command to run the application
CMD ["node", "index.js"]
