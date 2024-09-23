package org.example.codeservice.controller;

import org.example.codeservice.model.CodeRequest;
import org.example.codeservice.service.CodeExecutorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/code")
public class CodeExecutionController {

    @PostMapping("/execute")
    public ResponseEntity<Map<String, String>> executeCode(@RequestBody CodeRequest codeRequest) {
        String output = CodeExecutorService.execute(codeRequest.getLanguage(), codeRequest.getCode());
        Map<String, String> response = new HashMap<>();
        response.put("output", output);
        return ResponseEntity.ok(response);
    }
}
