package com.noblesse.auth_service.controller;

import com.noblesse.auth_service.service.GeminiService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ai-chat")
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GeminiController {

    GeminiService geminiService;

    @PostMapping("/ask")
    public ResponseEntity<String> generateContent(
            @RequestBody String prompt,
            @RequestHeader("Session-Id") String sessionId,
            @RequestHeader("User-Id") String userId) {

        log.info("Received request from user: {}, session: {}", userId, sessionId);

        try {
            // Nếu không có sessionId, sử dụng phương thức không lưu lịch sử
            if (sessionId == null || sessionId.isEmpty()) {
                String response = geminiService.callGemini(prompt);
                return ResponseEntity.ok(response);
            }

            // Nếu có sessionId, sử dụng phương thức có lưu lịch sử
            String response = geminiService.callGeminiWithHistory(userId, sessionId, prompt);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Validation error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            log.error("Internal server error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while processing your request");
        }
    }

    @GetMapping("/history/{sessionId}")
    public ResponseEntity<?> getConversationHistory(
            @PathVariable String sessionId,
            @RequestHeader("User-Id") String userId) {

        log.info("Getting history for user: {}, session: {}", userId, sessionId);

        try {
            List<Map<String, String>> history = geminiService.getConversationHistory(userId, sessionId);
            return ResponseEntity.ok(history);

        } catch (IllegalArgumentException e) {
            log.error("Validation error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            log.error("Internal server error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to retrieve chat history");
        }
    }

    @DeleteMapping("/history/{sessionId}")
    public ResponseEntity<?> clearConversationHistory(
            @PathVariable String sessionId,
            @RequestHeader("User-Id") String userId) {

        log.info("Clearing history for user: {}, session: {}", userId, sessionId);

        try {
            geminiService.clearConversationHistory(userId, sessionId);
            return ResponseEntity.ok().build();

        } catch (IllegalArgumentException e) {
            log.error("Validation error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            log.error("Internal server error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to clear chat history");
        }
    }
}