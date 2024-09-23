package org.example.codeservice.service;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class CodeExecutorService {

    public static String execute(String language, String code) {
        String fileName = "Main";
        String fileExtension = "";

        switch (language) {
            case "java":
                fileExtension = ".java";
                break;
            case "python":
                fileExtension = ".py";
                break;
        }

        Path codeFilePath = Paths.get("/tmp", fileName + fileExtension);

        try {
            Files.write(codeFilePath, code.getBytes());

            // Build the Docker command
            List<String> command = new ArrayList<>();
            command.add("docker");
            command.add("run");
            command.add("--rm");
            command.add("--network");
            command.add("none");
            command.add("--memory");
            command.add("128m");
            command.add("--cpus");
            command.add("0.5");
            command.add("-v");
            command.add(codeFilePath.getParent().toString() + ":/code");
            command.add(getDockerImage(language));
            command.addAll(getRunCommand(language, "/code/" + fileName + fileExtension));

            // Execute the command
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            // Read the output
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            // Wait for the process to finish
            boolean finished = process.waitFor(10, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                return "Error: Execution timed out.";
            }

            return output.toString();

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return "Error executing code: " + e.getMessage();
        } finally {
            // Clean up the temporary file
            try {
                Files.deleteIfExists(codeFilePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static String getDockerImage(String language) {
        switch (language) {
            case "java":
                return "openjdk:11";
            case "python":
                return "python:3.8";
            default:
                throw new IllegalArgumentException("Unsupported language: " + language);
        }
    }

    private static List<String> getRunCommand(String language, String codeFilePath) {
        switch (language) {
            case "java":
                return Arrays.asList("sh", "-c", "javac " + codeFilePath + " && java -cp /code Main");
            case "python":
                return Arrays.asList("python", codeFilePath);
            default:
                throw new IllegalArgumentException("Unsupported language: " + language);
        }
    }
}

