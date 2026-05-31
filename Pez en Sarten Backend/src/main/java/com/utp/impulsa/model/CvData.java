package com.utp.impulsa.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.utp.impulsa.config.JsonNodeConverter;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "cv_data")
public class CvData {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Convert(converter = JsonNodeConverter.class)
    @Column(name = "personal_info", columnDefinition = "jsonb")
    private JsonNode personalInfo;

    @Convert(converter = JsonNodeConverter.class)
    @Column(name = "education", columnDefinition = "jsonb")
    private JsonNode education;

    @Convert(converter = JsonNodeConverter.class)
    @Column(name = "experience", columnDefinition = "jsonb")
    private JsonNode experience;

    @Convert(converter = JsonNodeConverter.class)
    @Column(name = "projects", columnDefinition = "jsonb")
    private JsonNode projects;

    public CvData() {
    }

    public CvData(UUID userId) {
        this.userId = userId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public JsonNode getPersonalInfo() {
        return personalInfo;
    }

    public void setPersonalInfo(JsonNode personalInfo) {
        this.personalInfo = personalInfo;
    }

    public JsonNode getEducation() {
        return education;
    }

    public void setEducation(JsonNode education) {
        this.education = education;
    }

    public JsonNode getExperience() {
        return experience;
    }

    public void setExperience(JsonNode experience) {
        this.experience = experience;
    }

    public JsonNode getProjects() {
        return projects;
    }

    public void setProjects(JsonNode projects) {
        this.projects = projects;
    }
}
