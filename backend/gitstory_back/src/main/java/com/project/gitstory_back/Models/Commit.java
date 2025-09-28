package com.project.gitstory_back.Models;


import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "commits")
public class Commit {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "commit_hash", length = 100)
    private String commitHash;

    @Column(columnDefinition = "text")
    private String message;

    private String author;

    private String commitDate;


    @Column(columnDefinition = "text")
    private String embedding;

    @Column(name = "cluster_id")
    private Integer clusterId;

    // Constructors
    public Commit() {}

    // Getters & setters
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

    public String  getEmbedding() { return embedding; }
    public void setEmbedding(String  embedding) { this.embedding = embedding; }

    public Integer getClusterId() { return clusterId; }
    public void setClusterId(Integer clusterId) { this.clusterId = clusterId; }
}
