package com.project.gitstory_back.Dto;

import java.util.UUID;

public class CommitDTO {
    private UUID id;
    private String commitHash;
    private String message;
    private String author;
    private String commitDate;
    private String commitType;
    private String storyTitle;

    public CommitDTO() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getCommitHash() { return commitHash; }
    public void setCommitHash(String commitHash) { this.commitHash = commitHash; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getCommitDate() { return commitDate; }
    public void setCommitDate(String commitDate) { this.commitDate = commitDate; }

    public String getCommitType() { return commitType; }
    public void setCommitType(String commitType) { this.commitType = commitType; }

    public String getStoryTitle() { return storyTitle; }
    public void setStoryTitle(String storyTitle) { this.storyTitle = storyTitle; }
}
