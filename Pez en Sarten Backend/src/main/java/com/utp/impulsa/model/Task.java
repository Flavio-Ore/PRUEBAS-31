package com.utp.impulsa.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "tasks")
public class Task {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(name = "company_name", nullable = false)
    private String companyName;

    @Column(name = "company_logo")
    private String companyLogo;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(name = "skills_required", columnDefinition = "integer[]")
    private List<Integer> skillsRequired = new ArrayList<>();

    public Task() {
    }

    public Task(UUID id, String title, String companyName, String companyLogo, String description, List<Integer> skillsRequired) {
        this.id = id;
        this.title = title;
        this.companyName = companyName;
        this.companyLogo = companyLogo;
        this.description = description;
        this.skillsRequired = skillsRequired;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getCompanyLogo() {
        return companyLogo;
    }

    public void setCompanyLogo(String companyLogo) {
        this.companyLogo = companyLogo;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Integer> getSkillsRequired() {
        return skillsRequired;
    }

    public void setSkillsRequired(List<Integer> skillsRequired) {
        this.skillsRequired = skillsRequired;
    }
}
