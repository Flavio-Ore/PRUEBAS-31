package com.utp.impulsa.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "match_results")
public class MatchResult {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "task_id", nullable = false)
    private UUID taskId;

    @Column(name = "score_affinity", nullable = false)
    private int scoreAffinity;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String analysis;

    @Column(name = "suggested_skills", columnDefinition = "text[]")
    private List<String> suggestedSkills = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public MatchResult() {
    }

    public MatchResult(UUID id, UUID userId, UUID taskId, int scoreAffinity, String analysis, List<String> suggestedSkills) {
        this.id = id;
        this.userId = userId;
        this.taskId = taskId;
        this.scoreAffinity = scoreAffinity;
        this.analysis = analysis;
        this.suggestedSkills = suggestedSkills;
        this.createdAt = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UUID getTaskId() {
        return taskId;
    }

    public void setTaskId(UUID taskId) {
        this.taskId = taskId;
    }

    public int getScoreAffinity() {
        return scoreAffinity;
    }

    public void setScoreAffinity(int scoreAffinity) {
        this.scoreAffinity = scoreAffinity;
    }

    public String getAnalysis() {
        return analysis;
    }

    public void setAnalysis(String analysis) {
        this.analysis = analysis;
    }

    public List<String> getSuggestedSkills() {
        return suggestedSkills;
    }

    public void setSuggestedSkills(List<String> suggestedSkills) {
        this.suggestedSkills = suggestedSkills;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
