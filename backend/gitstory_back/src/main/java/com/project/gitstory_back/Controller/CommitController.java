package com.project.gitstory_back.Controller;

import com.project.gitstory_back.Dto.CommitDTO;
import com.project.gitstory_back.Service.CommitService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;


@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/commits")
public class CommitController {

    private final CommitService commitService;

    public CommitController(CommitService commitService) {
        this.commitService = commitService;
    }

    @PostMapping("/import")
    public ResponseEntity<Map<String, String>> importRepo(@RequestBody RepoRequest request) {
        commitService.importCommitsFromRepoUrl(
                request.getRepoUrl(),
                request.getStartDate(),
                request.getEndDate()
        );

        // Create a JSON object with a "message" field
        Map<String, String> response = new HashMap<>();
        response.put("message", "Imported commits from: " + request.getRepoUrl());

        return ResponseEntity.ok(response); // Return JSON instead of plain text
    }


    @GetMapping("/all")
    public List<CommitDTO> getAllCommits() {
        return commitService.getAllCommitDTOs();
    }

    public static class RepoRequest {
        private String repoUrl;
        private String startDate;
        private String endDate;

        public String getRepoUrl() { return repoUrl; }
        public void setRepoUrl(String repoUrl) { this.repoUrl = repoUrl; }

        public String getStartDate() { return startDate; }
        public void setStartDate(String startDate) { this.startDate = startDate; }

        public String getEndDate() { return endDate; }
        public void setEndDate(String endDate) { this.endDate = endDate; }
    }
}
