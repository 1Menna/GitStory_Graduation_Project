package com.project.gitstory_back.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

@Service
public class AIService {

    @Value("${huggingface.api.key:}")
    private String hfApiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String classifyCommit(String message) {
        try {
            String apiUrl = "https://router.huggingface.co/hf-inference/models/facebook/bart-large-mnli";
            Map<String, Object> payload = Map.of(
                    "inputs", message,
                    "parameters", Map.of("candidate_labels", List.of("feature", "bugfix", "refactor", "chore"))
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
            headers.setBearerAuth(hfApiKey);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
            ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.POST, request, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());
            if (root.has("labels")) {
                String label = root.get("labels").get(0).asText();
                return label.toLowerCase();
            }
        } catch (Exception e) {
            System.err.println("AIService.classifyCommit error: " + e.getMessage());
        }
        return "unknown";
    }

    public String summarizeCommit(String message) {
        try {
            String apiUrl = "https://router.huggingface.co/hf-inference/models/facebook/bart-large-cnn";
            Map<String, Object> payload = Map.of("inputs", "Summarize this commit into a short title: " + message);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
            headers.setBearerAuth(hfApiKey);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
            ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.POST, request, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());
            if (root.isArray() && root.get(0).has("summary_text")) {
                return root.get(0).get("summary_text").asText().trim();
            }
        } catch (Exception e) {
            System.err.println("AIService.summarizeCommit error: " + e.getMessage());
        }
        return null;
    }
}
