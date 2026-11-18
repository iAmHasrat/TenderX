package com.tenderx.evaluation;

import java.util.ArrayList;
import java.util.List;

public class EvaluationResult {
    private String bidId;
    private String bidderName;
    private double totalQuote;
    private int proposedDays;
    private int rank;
    private double totalScore;
    private boolean eligible;
    private String statusNote;
    private List<CriterionScore> criterionScores;

    public EvaluationResult(String bidId, String bidderName, double totalQuote, int proposedDays) {
        this.bidId = bidId;
        this.bidderName = bidderName;
        this.totalQuote = totalQuote;
        this.proposedDays = proposedDays;
        this.eligible = true;
        this.criterionScores = new ArrayList<>();
    }

    public String getBidId() { return bidId; }
    public String getBidderName() { return bidderName; }
    public double getTotalQuote() { return totalQuote; }
    public int getProposedDays() { return proposedDays; }

    public int getRank() { return rank; }
    public void setRank(int rank) { this.rank = rank; }

    public double getTotalScore() { return totalScore; }
    public void setTotalScore(double totalScore) { this.totalScore = totalScore; }

    public boolean isEligible() { return eligible; }
    public void setEligible(boolean eligible) { this.eligible = eligible; }

    public String getStatusNote() { return statusNote; }
    public void setStatusNote(String statusNote) { this.statusNote = statusNote; }

    public List<CriterionScore> getCriterionScores() { return criterionScores; }
    public void addCriterionScore(CriterionScore cs) { this.criterionScores.add(cs); }
}

