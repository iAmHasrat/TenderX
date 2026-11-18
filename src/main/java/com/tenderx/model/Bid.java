package com.tenderx.model;

import java.util.ArrayList;
import java.util.List;

public class Bid {
    private String id;
    private String tenderId;
    private String bidderId;
    private String bidderName;
    private double totalQuote;
    private int proposedTimelineDays;
    private String submittedAt;
    private String status; // SUBMITTED, EVALUATED, ACCEPTED, REJECTED
    private double totalScore;
    private String evaluationSummary;
    private List<BidResponse> responses;

    public Bid() {
        this.responses = new ArrayList<>();
        this.status = "SUBMITTED";
    }

    public Bid(String id, String tenderId, String bidderId, String bidderName, double totalQuote, int proposedTimelineDays, String submittedAt) {
        this.id = id;
        this.tenderId = tenderId;
        this.bidderId = bidderId;
        this.bidderName = bidderName;
        this.totalQuote = totalQuote;
        this.proposedTimelineDays = proposedTimelineDays;
        this.submittedAt = submittedAt;
        this.status = "SUBMITTED";
        this.responses = new ArrayList<>();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTenderId() { return tenderId; }
    public void setTenderId(String tenderId) { this.tenderId = tenderId; }

    public String getBidderId() { return bidderId; }
    public void setBidderId(String bidderId) { this.bidderId = bidderId; }

    public String getBidderName() { return bidderName; }
    public void setBidderName(String bidderName) { this.bidderName = bidderName; }

    public double getTotalQuote() { return totalQuote; }
    public void setTotalQuote(double totalQuote) { this.totalQuote = totalQuote; }

    public int getProposedTimelineDays() { return proposedTimelineDays; }
    public void setProposedTimelineDays(int proposedTimelineDays) { this.proposedTimelineDays = proposedTimelineDays; }

    public String getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(String submittedAt) { this.submittedAt = submittedAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public double getTotalScore() { return totalScore; }
    public void setTotalScore(double totalScore) { this.totalScore = totalScore; }

    public String getEvaluationSummary() { return evaluationSummary; }
    public void setEvaluationSummary(String evaluationSummary) { this.evaluationSummary = evaluationSummary; }

    public List<BidResponse> getResponses() { return responses; }
    public void setResponses(List<BidResponse> responses) { this.responses = responses; }

    public void addResponse(BidResponse response) {
        if (this.responses == null) this.responses = new ArrayList<>();
        this.responses.add(response);
    }
}

