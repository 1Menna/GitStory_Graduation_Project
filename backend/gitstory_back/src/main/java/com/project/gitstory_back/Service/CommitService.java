package com.project.gitstory_back.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.gitstory_back.Dto.CommitDTO;
import com.project.gitstory_back.Models.Commit;
import com.project.gitstory_back.Repository.CommitRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CommitService {

    private final CommitRepository commitRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public CommitService(CommitRepository commitRepository) {
        this.commitRepository = commitRepository;
    }

    public void importCommitsFromRepoUrl(String repoUrl, String startDate, String endDate) {
        try {
            commitRepository.deleteAll();

            String repoPath = repoUrl.replace("https://github.com/", "").replace(".git", "");

            StringBuilder apiUrl = new StringBuilder("https://api.github.com/repos/" + repoPath + "/commits");
            boolean hasQuery = false;

            if (startDate != null && !startDate.isEmpty()) {
                apiUrl.append(hasQuery ? "&" : "?").append("since=").append(startDate);
                hasQuery = true;
            }
            if (endDate != null && !endDate.isEmpty()) {
                apiUrl.append(hasQuery ? "&" : "?").append("until=").append(endDate);
            }

            RestTemplate restTemplate = new RestTemplate();
            String response = restTemplate.getForObject(apiUrl.toString(), String.class);

            JsonNode root = objectMapper.readTree(response);

            for (JsonNode node : root) {
                Commit commit = new Commit();
                commit.setCommitHash(node.get("sha").asText());
                commit.setMessage(node.get("commit").get("message").asText());
                commit.setAuthor(node.get("commit").get("author").get("name").asText());
                commit.setCommitDate(node.get("commit").get("author").get("date").asText());
                commit.setEmbedding(null);
                commit.setClusterId(null);

                commitRepository.save(commit);
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to import commits from repo: " + repoUrl, e);
        }
    }

    public List<CommitDTO> getAllCommitDTOs() {
        return commitRepository.findAll().stream()
                .sorted((c1, c2) -> c1.getCommitDate().compareTo(c2.getCommitDate())) // oldest first
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
        return dto;
    }

    public void updateEmbedding(UUID id, float[] embedding) {
        Commit commit = commitRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Commit not found"));
        commit.setEmbedding(Arrays.toString(embedding));
        commitRepository.save(commit);
    }

    public List<Commit> textSearch(String keyword) {
        return commitRepository.findByMessageContainingIgnoreCase(keyword);
    }

    private String arrayToPgVectorLiteral(float[] arr) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < arr.length; i++) {
            sb.append(arr[i]);
            if (i < arr.length - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }
}
