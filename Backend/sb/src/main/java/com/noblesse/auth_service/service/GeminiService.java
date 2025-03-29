package com.noblesse.auth_service.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.noblesse.auth_service.entity.AiChat;
import com.noblesse.auth_service.repository.AiChatRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GeminiService {
    static final String API_KEY = "AIzaSyAE3PvQs4TIn4Xi7aW8AKGLRnCwMim5nqg";
    static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + API_KEY;

    final AiChatRepository aiChatRepository;
    final ObjectMapper objectMapper;

    /**
     * Gọi Gemini API không lưu lịch sử
     */
    public String callGemini(String prompt) {
        if (prompt == null || prompt.trim().isEmpty()) {
            return "Prompt is empty!";
        }

        try {
            // Tạo request đơn giản không có lịch sử
            String requestBody = buildSimpleGeminiRequest(prompt);
            log.info("Sending request to Gemini API without history: {}", requestBody);

            // Gửi request và nhận response
            RestTemplate restTemplate = new RestTemplate();
            String responseText = sendRequestToGemini(restTemplate, requestBody);

            return responseText;
        } catch (Exception e) {
            log.error("Error calling Gemini API: {}", e.getMessage(), e);
            return "Error communicating with Gemini API: " + e.getMessage();
        }
    }

    /**
     * Gọi Gemini API có lưu lịch sử
     */
    @Transactional
    public String callGeminiWithHistory(String userId, String sessionId, String prompt) {
        validateUserSession(userId, sessionId);

        if (prompt == null || prompt.trim().isEmpty()) {
            return "Prompt is empty!";
        }

        try {
            // Lấy lịch sử từ database
            List<Map<String, String>> history = getHistoryFromDatabase(userId, sessionId);

            // Tạo request với lịch sử hội thoại
            String requestBody = buildGeminiRequest(history, prompt);
            log.info("Sending request to Gemini API with history: {}", requestBody);

            // Gửi request và nhận response
            RestTemplate restTemplate = new RestTemplate();
            String responseText = sendRequestToGemini(restTemplate, requestBody);

            // Lưu vào database
            saveToDatabase(userId, sessionId, prompt, responseText);

            return responseText;
        } catch (Exception e) {
            log.error("Error calling Gemini API: {}", e.getMessage(), e);
            return "Error communicating with Gemini API: " + e.getMessage();
        }
    }

    private String buildSimpleGeminiRequest(String prompt) throws Exception {
        ObjectNode requestNode = objectMapper.createObjectNode();
        ArrayNode contentsNode = objectMapper.createArrayNode();

        // Chỉ thêm prompt hiện tại
        ObjectNode newContentNode = objectMapper.createObjectNode();
        ArrayNode newPartsNode = objectMapper.createArrayNode();
        ObjectNode newPartNode = objectMapper.createObjectNode();
        newPartNode.put("text", prompt);
        newPartsNode.add(newPartNode);

        newContentNode.put("role", "user");
        newContentNode.set("parts", newPartsNode);
        contentsNode.add(newContentNode);

        requestNode.set("contents", contentsNode);
        return objectMapper.writeValueAsString(requestNode);
    }

    // Các phương thức hỗ trợ khác giữ nguyên như trước
    private List<Map<String, String>> getHistoryFromDatabase(String userId, String sessionId) {
        return aiChatRepository.findByUserIdAndSessionId(userId, sessionId)
                .stream()
                .map(this::convertToHistoryMap)
                .collect(Collectors.toList());
    }

    private Map<String, String> convertToHistoryMap(AiChat chat) {
        Map<String, String> map = new HashMap<>();
        if (chat.getUserMessage() != null) {
            map.put("role", "user");
            map.put("content", chat.getUserMessage());
        } else if (chat.getAiResponse() != null) {
            map.put("role", "model");
            map.put("content", chat.getAiResponse());
        }
        return map;
    }

    @Transactional
    protected void saveToDatabase(String userId, String sessionId, String prompt, String responseText) {
        // Lưu tin nhắn người dùng
        AiChat userMessage = AiChat.builder()
                .userId(userId)
                .sessionId(sessionId)
                .userMessage(prompt)
                .aiResponse(null)
                .createdAt(LocalDateTime.now())
                .build();
        aiChatRepository.save(userMessage);

        // Lưu phản hồi AI
        AiChat aiResponse = AiChat.builder()
                .userId(userId)
                .sessionId(sessionId)
                .userMessage(null)
                .aiResponse(responseText)
                .createdAt(LocalDateTime.now())
                .build();
        aiChatRepository.save(aiResponse);

        // Giới hạn số lượng tin nhắn lưu trữ
        long messageCount = aiChatRepository.countByUserIdAndSessionId(userId, sessionId);
        if (messageCount > 20) {
            Pageable pageable = PageRequest.of(0, (int)(messageCount - 20));
            List<AiChat> oldestMessages = aiChatRepository
                    .findTopNByUserIdAndSessionIdOrderByCreatedAtAsc(userId, sessionId, pageable);
            aiChatRepository.deleteAll(oldestMessages);
        }
    }

    private void validateUserSession(String userId, String sessionId) {
        if (userId == null || userId.isEmpty()) {
            throw new IllegalArgumentException("User ID is required");
        }

        if (!sessionId.startsWith("user_" + userId + "_")) {
            throw new IllegalArgumentException("Session does not belong to this user");
        }
    }

    private String buildGeminiRequest(List<Map<String, String>> history, String prompt) throws Exception {
        ObjectNode requestNode = objectMapper.createObjectNode();
        ArrayNode contentsNode = objectMapper.createArrayNode();

        // Thêm lịch sử vào request
        for (Map<String, String> message : history) {
            ObjectNode contentNode = objectMapper.createObjectNode();
            ArrayNode partsNode = objectMapper.createArrayNode();

            ObjectNode partNode = objectMapper.createObjectNode();
            partNode.put("text", message.get("content"));
            partsNode.add(partNode);

            contentNode.put("role", message.get("role"));
            contentNode.set("parts", partsNode);
            contentsNode.add(contentNode);
        }

        // Thêm prompt mới
        ObjectNode newContentNode = objectMapper.createObjectNode();
        ArrayNode newPartsNode = objectMapper.createArrayNode();
        ObjectNode newPartNode = objectMapper.createObjectNode();
        newPartNode.put("text", prompt);
        newPartsNode.add(newPartNode);
        newContentNode.put("role", "user");
        newContentNode.set("parts", newPartsNode);
        contentsNode.add(newContentNode);

        requestNode.set("contents", contentsNode);
        return objectMapper.writeValueAsString(requestNode);
    }

    private String sendRequestToGemini(RestTemplate restTemplate, String requestBody) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                API_URL,
                HttpMethod.POST,
                entity,
                String.class
        );

        log.info("Response from Gemini API: {}", response.getBody());
        return extractResponseText(response.getBody());
    }

    private String extractResponseText(String responseBody) throws Exception {
        JsonNode jsonNode = objectMapper.readTree(responseBody);

        if (jsonNode.has("candidates") && jsonNode.get("candidates").isArray() &&
                jsonNode.get("candidates").size() > 0) {

            JsonNode candidate = jsonNode.get("candidates").get(0);
            if (candidate.has("content") && candidate.get("content").has("parts") &&
                    candidate.get("content").get("parts").isArray() &&
                    candidate.get("content").get("parts").size() > 0) {

                return candidate.get("content").get("parts").get(0).get("text").asText();
            }
        }
        return "Could not extract response text. Full response: " + responseBody;
    }

    @Transactional(readOnly = true)
    public List<Map<String, String>> getConversationHistory(String userId, String sessionId) {
        validateUserSession(userId, sessionId);

        return aiChatRepository.findByUserIdAndSessionId(userId, sessionId)
                .stream()
                .sorted(Comparator.comparing(AiChat::getCreatedAt))
                .flatMap(chat -> {
                    List<Map<String, String>> messages = new ArrayList<>();
                    if (chat.getUserMessage() != null) {
                        Map<String, String> userMsg = new HashMap<>();
                        userMsg.put("role", "user");
                        userMsg.put("content", chat.getUserMessage());
                        messages.add(userMsg);
                    }
                    if (chat.getAiResponse() != null) {
                        Map<String, String> aiMsg = new HashMap<>();
                        aiMsg.put("role", "model");
                        aiMsg.put("content", chat.getAiResponse());
                        messages.add(aiMsg);
                    }
                    return messages.stream();
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void clearConversationHistory(String userId, String sessionId) {
        validateUserSession(userId, sessionId);
        aiChatRepository.deleteByUserIdAndSessionId(userId, sessionId);
    }
}