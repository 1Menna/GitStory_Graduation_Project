package com.project.gitstory_back.Controller;


import com.project.gitstory_back.Dto.CommitDTO;
import com.project.gitstory_back.Models.Commit;
import com.project.gitstory_back.Service.CommitService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/commits")
public class CommitController {

    private final CommitService commitService;

    public CommitController(CommitService commitService) {
        this.commitService = commitService;
    }


    @PostMapping("/import")
    public ResponseEntity<String> importRepo(@RequestBody RepoRequest request) {
        commitService.importCommitsFromRepoUrl(request.getRepoUrl());
        return ResponseEntity.ok("Imported commits from: " + request.getRepoUrl());
    }

    @GetMapping("/all")
    public List<CommitDTO> getAllCommits() {
        return commitService.getAllCommitDTOs();
    }


    public static class RepoRequest {
        private String repoUrl;
        public String getRepoUrl() { return repoUrl; }
        public void setRepoUrl(String repoUrl) { this.repoUrl = repoUrl; }
    }
}