package com.tenderx.model;

import java.util.ArrayList;
import java.util.List;

public class Tender {
    private String id;
    private String title;
    private String description;
    private String category;
    private double estimatedBudget;
    private int maxTimelineDays;
    private String status; // DRAFT, PUBLISHED, CLOSED, EVALUATED, AWARDED
    private String awardedBidId;
    private String awardedBidderName;
    private String createdBy;
    private String createdAt;
    private List<EvaluationCriterion> criteria;

    public Tender() {
        this.criteria = new ArrayList<>();
        this.status = "DRAFT";
    }

    public Tender(String id, String title, String description, String category, double estimatedBudget, int maxTimelineDays, String status, String createdBy, String createdAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.estimatedBudget = estimatedBudget;
        this.maxTimelineDays = maxTimelineDays;
        this.status = status;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.criteria = new ArrayList<>();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getEstimatedBudget() { return estimatedBudget; }
    public void setEstimatedBudget(double estimatedBudget) { this.estimatedBudget = estimatedBudget; }

    public int getMaxTimelineDays() { return maxTimelineDays; }
    public void setMaxTimelineDays(int maxTimelineDays) { this.maxTimelineDays = maxTimelineDays; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getAwardedBidId() { return awardedBidId; }
    public void setAwardedBidId(String awardedBidId) { this.awardedBidId = awardedBidId; }

    public String getAwardedBidderName() { return awardedBidderName; }
    public void setAwardedBidderName(String awardedBidderName) { this.awardedBidderName = awardedBidderName; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public List<EvaluationCriterion> getCriteria() { return criteria; }
    public void setCriteria(List<EvaluationCriterion> criteria) { this.criteria = criteria; }
}

