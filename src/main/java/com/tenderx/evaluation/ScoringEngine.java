package com.tenderx.evaluation;

import com.tenderx.model.Bid;
import com.tenderx.model.BidResponse;
import com.tenderx.model.EvaluationCriterion;
import com.tenderx.model.Tender;

import java.util.*;

public class ScoringEngine {

    public List<EvaluationResult> evaluateTenderBids(Tender tender, List<Bid> bids) {
        List<EvaluationResult> results = new ArrayList<>();
        if (bids == null || bids.isEmpty()) return results;

        // 1. Identify minimum quote among eligible bids for relative cost formula
        double minQuote = Double.MAX_VALUE;
        for (Bid b : bids) {
            if (b.getTotalQuote() > 0 && b.getTotalQuote() < minQuote) {
                minQuote = b.getTotalQuote();
            }
        }
        if (minQuote == Double.MAX_VALUE) minQuote = tender.getEstimatedBudget();

        // 2. Map criteria weights by key
        Map<String, EvaluationCriterion> criteriaMap = new HashMap<>();
        for (EvaluationCriterion c : tender.getCriteria()) {
            criteriaMap.put(c.getCriterionKey(), c);
        }

        // 3. Score each bid deterministically
        for (Bid bid : bids) {
            EvaluationResult res = new EvaluationResult(
                    bid.getId(),
                    bid.getBidderName(),
                    bid.getTotalQuote(),
                    bid.getProposedTimelineDays()
            );

            // Index bidder responses by fieldKey (case-insensitive)
            Map<String, String> respMap = new HashMap<>();
            for (BidResponse r : bid.getResponses()) {
                if (r.getFieldKey() != null) {
                    respMap.put(r.getFieldKey().toLowerCase(), r.getResponseValue());
                }
            }

            double totalCalculatedScore = 0.0;

            // --- Criterion 1: Technical Solution ---
            EvaluationCriterion techCrit = criteriaMap.get("TECHNICAL");
            double techWeight = techCrit != null ? techCrit.getWeight() : 25.0;
            double techRaw = scoreTechnical(respMap);
            double techWeighted = (techRaw * techWeight) / 100.0;
            totalCalculatedScore += techWeighted;
            res.addCriterionScore(new CriterionScore(
                    "TECHNICAL",
                    techCrit != null ? techCrit.getName() : "Technical Solution & Materials",
                    techWeight,
                    techRaw,
                    Math.round(techWeighted * 100.0) / 100.0,
                    String.format("Technical raw: %.1f/100 -> Weighted: %.2f pts (Evaluated against specified engineering rubric).", techRaw, techWeighted)
            ));

            // --- Criterion 2: Relevant Experience ---
            EvaluationCriterion expCrit = criteriaMap.get("EXPERIENCE");
            double expWeight = expCrit != null ? expCrit.getWeight() : 20.0;
            double expRaw = scoreExperience(respMap);
            double expWeighted = (expRaw * expWeight) / 100.0;
            totalCalculatedScore += expWeighted;
            res.addCriterionScore(new CriterionScore(
                    "EXPERIENCE",
                    expCrit != null ? expCrit.getName() : "Relevant Experience",
                    expWeight,
                    expRaw,
                    Math.round(expWeighted * 100.0) / 100.0,
                    String.format("Experience raw: %.1f/100 -> Weighted: %.2f pts (Track record of comparable projects).", expRaw, expWeighted)
            ));

            // --- Criterion 3: Timeline & Execution ---
            EvaluationCriterion timeCrit = criteriaMap.get("TIMELINE");
            double timeWeight = timeCrit != null ? timeCrit.getWeight() : 15.0;
            double timeRaw = scoreTimeline(bid.getProposedTimelineDays(), tender.getMaxTimelineDays());
            double timeWeighted = (timeRaw * timeWeight) / 100.0;
            totalCalculatedScore += timeWeighted;
            res.addCriterionScore(new CriterionScore(
                    "TIMELINE",
                    timeCrit != null ? timeCrit.getName() : "Execution Plan & Timeline",
                    timeWeight,
                    timeRaw,
                    Math.round(timeWeighted * 100.0) / 100.0,
                    String.format("Proposed %d days vs max allowed %d days (Score: %.1f/100 -> Weighted: %.2f pts).",
                            bid.getProposedTimelineDays(), tender.getMaxTimelineDays(), timeRaw, timeWeighted)
            ));

            // --- Criterion 4: Quality & Safety ---
            EvaluationCriterion qsCrit = criteriaMap.get("QUALITY_SAFETY");
            double qsWeight = qsCrit != null ? qsCrit.getWeight() : 15.0;
            double qsRaw = scoreQualitySafety(respMap);
            double qsWeighted = (qsRaw * qsWeight) / 100.0;
            totalCalculatedScore += qsWeighted;
            res.addCriterionScore(new CriterionScore(
                    "QUALITY_SAFETY",
                    qsCrit != null ? qsCrit.getName() : "Quality & Safety Standards",
                    qsWeight,
                    qsRaw,
                    Math.round(qsWeighted * 100.0) / 100.0,
                    String.format("Quality & Safety raw: %.1f/100 -> Weighted: %.2f pts (Compliance certifications).", qsRaw, qsWeighted)
            ));

            // --- Criterion 5: Cost & Value ---
            EvaluationCriterion costCrit = criteriaMap.get("COST");
            double costWeight = costCrit != null ? costCrit.getWeight() : 25.0;
            double costRaw = 0.0;
            if (bid.getTotalQuote() > 0) {
                // Public procurement standard inverse ratio: (L1 / L_current) * 100
                costRaw = (minQuote / bid.getTotalQuote()) * 100.0;
                if (costRaw > 100.0) costRaw = 100.0;
            }
            double costWeighted = (costRaw * costWeight) / 100.0;
            totalCalculatedScore += costWeighted;
            res.addCriterionScore(new CriterionScore(
                    "COST",
                    costCrit != null ? costCrit.getName() : "Cost & Lifecycle Value",
                    costWeight,
                    Math.round(costRaw * 10.0) / 10.0,
                    Math.round(costWeighted * 100.0) / 100.0,
                    String.format("Quote ₹%,.0f (L1 lowest: ₹%,.0f) -> Formula (L1/Price)*Weight = %.2f pts.",
                            bid.getTotalQuote(), minQuote, costWeighted)
            ));

            // Budget threshold check
            if (bid.getTotalQuote() > tender.getEstimatedBudget() * 1.15) {
                res.setEligible(false);
                res.setStatusNote("DISQUALIFIED: Quote exceeds budget cap by >15%");
            } else {
                res.setStatusNote("Qualified & Evaluated");
            }

            res.setTotalScore(Math.round(totalCalculatedScore * 100.0) / 100.0);
            results.add(res);
        }

        // 4. Rank bids descending by total score (eligible bids first)
        results.sort((a, b) -> {
            if (a.isEligible() != b.isEligible()) {
                return a.isEligible() ? -1 : 1;
            }
            return Double.compare(b.getTotalScore(), a.getTotalScore());
        });

        for (int i = 0; i < results.size(); i++) {
            results.get(i).setRank(i + 1);
        }

        return results;
    }

    private double scoreTechnical(Map<String, String> responses) {
        double score = 70.0; // base score
        for (Map.Entry<String, String> entry : responses.entrySet()) {
            String val = entry.getValue().toUpperCase();
            if (val.contains("CONCRETE") || val.contains("TIER-1") || val.contains("ADVANCED") || val.contains("GRADE-A")) {
                score += 25.0;
            } else if (val.contains("ASPHALT") || val.contains("STANDARD") || val.contains("HYBRID")) {
                score += 15.0;
            }
            if (val.contains("DRAINAGE") || val.contains("WATERLOGGING") || val.contains("INVERTER") || val.contains("SAFETY")) {
                score += 5.0;
            }
        }
        return Math.min(100.0, score);
    }

    private double scoreExperience(Map<String, String> responses) {
        // Look for number of projects or experience fields
        for (Map.Entry<String, String> entry : responses.entrySet()) {
            if (entry.getKey().contains("exp") || entry.getKey().contains("project") || entry.getKey().contains("similar")) {
                try {
                    String numStr = entry.getValue().replaceAll("[^0-9]", "");
                    if (!numStr.isEmpty()) {
                        int num = Integer.parseInt(numStr);
                        if (num >= 8) return 100.0;
                        if (num >= 5) return 85.0;
                        if (num >= 3) return 70.0;
                        return 50.0;
                    }
                } catch (Exception ignored) {}
            }
        }
        return 75.0; // default realistic fallback
    }

    private double scoreTimeline(int proposedDays, int maxDays) {
        if (maxDays <= 0) return 80.0;
        if (proposedDays <= 0) proposedDays = maxDays;

        if (proposedDays <= maxDays * 0.70) return 100.0; // Fast turnaround
        if (proposedDays <= maxDays * 0.85) return 92.0;
        if (proposedDays <= maxDays) return 82.0;         // Met deadline exactly
        if (proposedDays <= maxDays * 1.10) return 50.0;  // Slight overrun
        return 20.0;                                     // Severe overrun
    }

    private double scoreQualitySafety(Map<String, String> responses) {
        double score = 75.0;
        for (Map.Entry<String, String> entry : responses.entrySet()) {
            String val = entry.getValue().toUpperCase();
            if (val.contains("ISO") || val.contains("AUDITED") || val.contains("OSHA") || val.contains("CERTIFIED")) {
                score += 20.0;
            }
            if (val.contains("YES") || val.contains("COMPREHENSIVE")) {
                score += 5.0;
            }
        }
        return Math.min(100.0, score);
    }
}

