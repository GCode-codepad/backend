package org.example.codeservice.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class CodeExecutorService {

    public static String execute(String language, String code) {
        // Input validation
        if (language == null || code == null) {
            return "Error: Language and code must not be null.";
        }

        String fileExtension = getFileExtension(language);
        if (fileExtension == null) {
            return "Error: Unsupported language.";
        }

        // Generate a unique filename to prevent conflicts
        String uniqueId = UUID.randomUUID().toString();
        String fileName = "Main_" + uniqueId + fileExtension;
        Path codeFilePath = Paths.get("/tmp", fileName);

        try {
            // Write the code to a temporary file
            Files.write(codeFilePath, code.getBytes());

            // Build the Docker command
            List<String> command = buildDockerCommand(language, codeFilePath);

            // Execute the command
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true); // Merge stdout and stderr
            Process process = pb.start();

            // Capture the output
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            // Wait for the process to finish with a timeout
            boolean finished = process.waitFor(10, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                return "Error: Execution timed out.";
            }

            return output.toString();

        } catch (IOException | InterruptedException e) {
            // Log the exception (you can replace this with a logging framework)
            System.err.println("Error executing code: " + e.getMessage());
            return "Error executing code: " + e.getMessage();
        } finally {
            // Clean up the temporary file
            try {
                Files.deleteIfExists(codeFilePath);
            } catch (IOException e) {
                System.err.println("Failed to delete temporary file: " + e.getMessage());
            }
        }
    }

    private static String getFileExtension(String language) {
        switch (language.toLowerCase()) {
            case "java":
                return ".java";
            case "python":
                return ".py";
            default:
                return null;
        }
    }

    private static List<String> buildDockerCommand(String language, Path codeFilePath) {
        String dockerImage = getDockerImage(language);
        if (dockerImage == null) {
            throw new IllegalArgumentException("Unsupported language: " + language);
        }

        String containerCodePath = "/code/" + codeFilePath.getFileName().toString();

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
        command.add(codeFilePath.getParent().toString() + ":/code:ro"); // Mount as read-only
        command.add(dockerImage);
        command.addAll(getRunCommand(language, containerCodePath));

        return command;
    }

    private static String getDockerImage(String language) {
        Map<String, String> languageToImageMap = Map.of(
                "java", "openjdk:11",
                "python", "python:3.8"
        );
        return languageToImageMap.get(language.toLowerCase());
    }

    private static List<String> getRunCommand(String language, String codeFilePath) {
        switch (language.toLowerCase()) {
            case "java":
                String className = getClassName(codeFilePath);
                return Arrays.asList("sh", "-c", "javac " + codeFilePath + " && java -cp /code " + className);
            case "python":
                return Arrays.asList("python", codeFilePath);
            default:
                throw new IllegalArgumentException("Unsupported language: " + language);
        }
    }

    private static String getClassName(String codeFilePath) {
        // Extracts the class name from the file name
        String fileName = Paths.get(codeFilePath).getFileName().toString();
        return fileName.substring(0, fileName.lastIndexOf('.'));
    }
}
