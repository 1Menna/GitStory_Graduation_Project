package com.project.gitstory_back.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.gitstory_back.Dto.CommitDTO;
import com.project.gitstory_back.Models.Commit;
import com.project.gitstory_back.Repository.CommitRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CommitService {

    private final CommitRepository commitRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate;

    @Value("${github.token:}")
    private String githubToken;

    @Value("${huggingface.api.key:}")
    private String hfApiKey;

    public CommitService(CommitRepository commitRepository, RestTemplateBuilder builder) {
        this.commitRepository = commitRepository;
        // ✅ RestTemplate مع إعدادات connect/read timeout
        this.restTemplate = builder
                .requestFactory(() -> {
                    var factory = new org.springframework.http.client.SimpleClientHttpRequestFactory();
                    factory.setConnectTimeout(10000); // 10 ثواني
                    factory.setReadTimeout(25000); // 25 ثانية
                    return factory;
                })
                .build();
    }

    // --------------------------------------------
    // 1️⃣ Import commits from GitHub
    // --------------------------------------------
    public void importCommitsFromRepoUrl(String repoUrl, String startDate, String endDate) {
        try {
            commitRepository.deleteAll();

            String repoPath = repoUrl.replace("https://github.com/", "").replace(".git", "");

            int page = 1;
            boolean hasMore = true;

            while (hasMore) {
                StringBuilder apiUrl = new StringBuilder("https://api.github.com/repos/" + repoPath + "/commits");
                apiUrl.append("?per_page=100&page=").append(page);

                if (startDate != null && !startDate.isEmpty()) apiUrl.append("&since=").append(startDate);
                if (endDate != null && !endDate.isEmpty()) apiUrl.append("&until=").append(endDate);

                HttpHeaders headers = new HttpHeaders();
                if (githubToken != null && !githubToken.isBlank()) {
                    headers.set("Authorization", "token " + githubToken);
                }
                HttpEntity<String> entity = new HttpEntity<>(headers);

                ResponseEntity<String> response = restTemplate.exchange(apiUrl.toString(), HttpMethod.GET, entity, String.class);
                JsonNode root = objectMapper.readTree(response.getBody());

                if (root == null || root.isEmpty()) {
                    hasMore = false;
                } else {
                    for (JsonNode node : root) {
                        Commit commit = new Commit();
                        commit.setCommitHash(node.path("sha").asText());
                        commit.setMessage(node.path("commit").path("message").asText());
                        commit.setAuthor(node.path("commit").path("author").path("name").asText());
                        commit.setCommitDate(node.path("commit").path("author").path("date").asText());
                        commit.setCommitType("unclassified");
                        commit.setStoryTitle(null);
                        commitRepository.save(commit);
                    }
                    page++;
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to import commits from repo: " + repoUrl, e);
        }
    }

    // --------------------------------------------
    // 2️⃣ Convert commits to DTOs
    // --------------------------------------------
    public List<CommitDTO> getAllCommitDTOs() {
        return commitRepository.findAll().stream()
                .sorted(Comparator.comparing(Commit::getCommitDate))
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private CommitDTO toDTO(Commit c) {
        CommitDTO dto = new CommitDTO();
        dto.setId(c.getId());
        dto.setCommitHash(c.getCommitHash());
        dto.setMessage(c.getMessage());
        dto.setAuthor(c.getAuthor());
        dto.setCommitDate(c.getCommitDate());
        dto.setCommitType(c.getCommitType());
        dto.setStoryTitle(c.getStoryTitle());
        return dto;
    }

    // --------------------------------------------
    // 3️⃣ Classify commits
    // --------------------------------------------
    public void classifyAllCommits() {
        List<Commit> commits = commitRepository.findAll();
        if (commits.isEmpty()) return;

        for (Commit commit : commits) {
            try {
                String message = commit.getMessage();
                if (message == null || message.isBlank()) continue;

                String type = null;
                if (hfApiKey != null && !hfApiKey.isBlank()) {
                    type = callHFClassifier(message);
                }

                if (type == null || type.equalsIgnoreCase("unknown") || type.isBlank()) {
                    type = detectTypeByKeyword(message);
                }

                commit.setCommitType(type);
                commit.setStoryTitle(detectStory(message));
                commitRepository.save(commit);

                Thread.sleep(200); // لتجنب rate limit

            } catch (Exception e) {
                commit.setCommitType("chore");
                commit.setStoryTitle("General");
                commitRepository.save(commit);
            }
        }
    }

    // --------------------------------------------
    // 4️⃣ Generate grouped commit story schema
    // --------------------------------------------
    public Map<String, Map<String, List<String>>> generateCommitStorySchema() {
        List<Commit> commits = commitRepository.findAll();

        Map<String, List<Commit>> storyGroups = new LinkedHashMap<>();
        for (Commit c : commits) {
            String story = detectStory(c.getMessage());
            storyGroups.computeIfAbsent(story, k -> new ArrayList<>()).add(c);
        }

        mergeGenericStories(storyGroups);

        Map<String, Map<String, List<String>>> schema = new LinkedHashMap<>();
        for (var entry : storyGroups.entrySet()) {
            String storyName = entry.getKey();
            Map<String, List<String>> typeMap = new LinkedHashMap<>();

            for (Commit commit : entry.getValue()) {
                String type = commit.getCommitType();
                if (type == null || type.equalsIgnoreCase("unclassified") || type.equalsIgnoreCase("unknown"))
                    type = detectTypeByKeyword(commit.getMessage());

                typeMap.computeIfAbsent(type, k -> new ArrayList<>())
                        .add(cleanMessage(commit.getMessage()));
            }
            schema.put("Story: " + storyName, typeMap);
        }

        return schema;
    }

    // --------------------------------------------
    // Story detection
    // --------------------------------------------
    private String detectStory(String message) {
        if (message == null) return "General";
        String m = message.toLowerCase();

        if (m.matches(".*\\b(auth|login|jwt|password|token|signup)\\b.*")) return "Authentication System";
        if (m.matches(".*\\b(payment|invoice|checkout|order|billing)\\b.*")) return "Payment & Billing";
        if (m.matches(".*\\b(user|profile|account|admin|role|permission)\\b.*")) return "User Management";
        if (m.matches(".*\\b(ui|frontend|navbar|layout|css|component|react|design|style)\\b.*")) return "User Interface";
        if (m.matches(".*\\b(api|endpoint|controller|route|request|response|fetch)\\b.*")) return "Backend API";
        if (m.matches(".*\\b(db|database|schema|query|sql|migration|entity)\\b.*")) return "Database Layer";
        if (m.matches(".*\\b(test|unit|integration|pipeline)\\b.*")) return "Testing & CI";
        if (m.matches(".*\\b(doc|readme|markdown|documentation)\\b.*")) return "Documentation";
        if (m.matches(".*\\b(ai|ml|model|data|training|neural)\\b.*")) return "Machine Learning";
        if (m.matches(".*\\b(deploy|docker|aws|gcp|infra|server)\\b.*")) return "Deployment & Infrastructure";

        return "General";
    }

    private void mergeGenericStories(Map<String, List<Commit>> stories) {
        if (!stories.containsKey("General")) return;
        List<Commit> generals = stories.remove("General");
        if (generals == null || generals.isEmpty()) return;

        for (Commit c : generals) {
            String msg = c.getMessage().toLowerCase();
            String targetStory = stories.keySet().stream()
                    .filter(k -> msg.contains(k.split(" ")[0].toLowerCase()))
                    .findFirst()
                    .orElse("Miscellaneous Improvements");
            stories.computeIfAbsent(targetStory, k -> new ArrayList<>()).add(c);
        }
    }

    // --------------------------------------------
    // Keyword classification
    // --------------------------------------------
    private String detectTypeByKeyword(String message) {
        if (message == null) return "chore";
        String m = message.toLowerCase();
        if (m.matches(".*\\b(fix|bug|error|issue|patch)\\b.*")) return "bugfix";
        if (m.matches(".*\\b(add|implement|create|feature|support|build|develop|design)\\b.*")) return "feature";
        if (m.matches(".*\\b(refactor|cleanup|restructure|optimize|simplify|enhance)\\b.*")) return "refactor";
        if (m.matches(".*\\b(doc|readme|test|config|merge|setup|update|misc)\\b.*")) return "chore";
        return "chore";
    }

    // --------------------------------------------
    // Hugging Face classifier (lightweight & stable)
    // --------------------------------------------
    private String callHFClassifier(String message) {
        int retries = 3;
        while (retries-- > 0) {
            try {
                String HF_CLASSIFY_URL = "https://router.huggingface.co/hf-inference/models/valhalla/distilbart-mnli-12-3";

                Map<String, Object> payload = Map.of(
                        "inputs", message,
                        "parameters", Map.of(
                                "candidate_labels", List.of("feature", "bugfix", "refactor", "chore")
                        )
                );

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setAccept(List.of(MediaType.APPLICATION_JSON));
                headers.setBearerAuth(hfApiKey);

                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

                ResponseEntity<String> response = restTemplate.exchange(
                        HF_CLASSIFY_URL,
                        HttpMethod.POST,
                        entity,
                        String.class
                );

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    JsonNode root = objectMapper.readTree(response.getBody());
                    if (root.has("labels") && root.get("labels").isArray()) {
                        return root.get("labels").get(0).asText();
                    }
                }

            } catch (Exception e) {
                System.err.println("⚠️ HF Classifier error (retry " + (3 - retries) + "): " + e.getMessage());
                try { Thread.sleep(3000); } catch (InterruptedException ignored) {}
            }
        }
        return "unknown";
    }

    // --------------------------------------------
    // Cleaning commit message
    // --------------------------------------------
    private String cleanMessage(String msg) {
        if (msg == null) return "";
        return msg.replaceAll("(?i)^commit message[:]?\\s*", "")
                .replaceAll("\\s{2,}", " ")
                .trim();
    }
}
