package com.tenderx.evaluation;

public class CriterionScore {
    private String criterionKey;
    private String criterionName;
    private double weight;
    private double rawScoreOutOf100;
    private double weightedScore;
    private String explanation;

    public CriterionScore(String criterionKey, String criterionName, double weight, double rawScoreOutOf100, double weightedScore, String explanation) {
        this.criterionKey = criterionKey;
        this.criterionName = criterionName;
        this.weight = weight;
        this.rawScoreOutOf100 = rawScoreOutOf100;
        this.weightedScore = weightedScore;
        this.explanation = explanation;
    }

    public String getCriterionKey() { return criterionKey; }
    public String getCriterionName() { return criterionName; }
    public double getWeight() { return weight; }
    public double getRawScoreOutOf100() { return rawScoreOutOf100; }
    public double getWeightedScore() { return weightedScore; }
    public String getExplanation() { return explanation; }
}

