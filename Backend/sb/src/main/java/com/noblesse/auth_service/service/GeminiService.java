package com.noblesse.auth_service.service;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GeminiService {
    private static final String API_KEY = "AIzaSyAE3PvQs4TIn4Xi7aW8AKGLRnCwMim5nqg";
    // Cập nhật URL endpoint để sử dụng gemini-2.0-flash thay vì gemini-pro
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + API_KEY;
    public String callGemini(String prompt) {
        RestTemplate restTemplate = new RestTemplate();
        // Kiểm tra prompt có dữ liệu hợp lệ không
        if (prompt == null || prompt.trim().isEmpty()) {
            return "Prompt is empty!";
        }
        try {
            // Tạo JSON request theo đúng format từ curl example
            String requestBody = "{"
                    + "\"contents\": ["
                    + "  {"
                    + "    \"parts\": ["
                    + "      {"
                    + "        \"text\": \"" + prompt.replace("\"", "\\\"") + "\""
                    + "      }"
                    + "    ]"
                    + "  }"
                    + "]"
                    + "}";
            log.info("Sending request to Gemini API: {}", requestBody);
            // Tạo headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
            // Gửi request
            ResponseEntity<String> response = restTemplate.exchange(
                    API_URL,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            log.info("Response from Gemini API: {}", response.getBody());

            // Parse response
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(response.getBody());

            // Trích xuất văn bản từ response theo cấu trúc
            if (jsonNode.has("candidates") && jsonNode.get("candidates").isArray() &&
                    jsonNode.get("candidates").size() > 0) {

                JsonNode candidate = jsonNode.get("candidates").get(0);
                if (candidate.has("content") && candidate.get("content").has("parts") &&
                        candidate.get("content").get("parts").isArray() &&
                        candidate.get("content").get("parts").size() > 0) {

                    return candidate.get("content").get("parts").get(0).get("text").asText();
                }
            }

            return "Could not extract response text. Full response: " + response.getBody();
        } catch (Exception e) {
            log.error("Error calling Gemini API: {}", e.getMessage(), e);
            return "Error communicating with Gemini API: " + e.getMessage();
        }
    }
}