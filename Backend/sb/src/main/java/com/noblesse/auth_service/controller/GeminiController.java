package com.noblesse.auth_service.controller;

import com.noblesse.auth_service.service.GeminiService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ai-chat")
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GeminiController {

    GeminiService geminiService;

    @GetMapping("/ask")
    public ResponseEntity<String> generateContentGet(@RequestBody String prompt) {
        log.info("Received GET request to generate content with prompt: {}", prompt);
        String response = geminiService.callGemini(prompt);
        return ResponseEntity.ok(response);
    }

}
