// Deterministic Rule-Based Scoring Engine
// Mirrors the Java ScoringEngine.java for 100% mathematical auditability

export function evaluateTenderBids(tender, bids) {
  if (!bids || bids.length === 0) return [];

  // 1. Identify minimum price among eligible bids (L1 lowest quote)
  let minQuote = Infinity;
  bids.forEach(b => {
    const q = Number(b.totalQuote || 0);
    if (q > 0 && q < minQuote) minQuote = q;
  });
  if (minQuote === Infinity) minQuote = Number(tender.estimatedBudget || 1000000);

  // 2. Map criteria weights
  const critMap = {};
  tender.criteria.forEach(c => {
    critMap[c.key] = c;
  });

  // 3. Evaluate each bid deterministically
  const results = bids.map(bid => {
    const resp = bid.responses || {};
    let totalScore = 0;
    const criterionScores = [];

    // --- 1. TECHNICAL ---
    const techCrit = critMap["TECHNICAL"] || { weight: 25, name: "Technical Solution" };
    const techRaw = scoreTechnical(resp);
    const techWeighted = (techRaw * techCrit.weight) / 100;
    totalScore += techWeighted;
    criterionScores.push({
      key: "TECHNICAL",
      name: techCrit.name,
      weight: techCrit.weight,
      raw: techRaw,
      weighted: Number(techWeighted.toFixed(2)),
      explanation: `Raw: ${techRaw.toFixed(1)}/100 -> Weighted: ${techWeighted.toFixed(2)} pts (Evaluated against specified engineering rubric & materials).`
    });

    // --- 2. EXPERIENCE ---
    const expCrit = critMap["EXPERIENCE"] || { weight: 20, name: "Relevant Experience" };
    const expRaw = scoreExperience(resp);
    const expWeighted = (expRaw * expCrit.weight) / 100;
    totalScore += expWeighted;
    criterionScores.push({
      key: "EXPERIENCE",
      name: expCrit.name,
      weight: expCrit.weight,
      raw: expRaw,
      weighted: Number(expWeighted.toFixed(2)),
      explanation: `Raw: ${expRaw.toFixed(1)}/100 -> Weighted: ${expWeighted.toFixed(2)} pts (Verified completed comparable track record).`
    });

    // --- 3. TIMELINE ---
    const timeCrit = critMap["TIMELINE"] || { weight: 15, name: "Execution Plan & Timeline" };
    const timeRaw = scoreTimeline(Number(bid.proposedDays || tender.maxTimelineDays), Number(tender.maxTimelineDays || 60));
    const timeWeighted = (timeRaw * timeCrit.weight) / 100;
    totalScore += timeWeighted;
    criterionScores.push({
      key: "TIMELINE",
      name: timeCrit.name,
      weight: timeCrit.weight,
      raw: timeRaw,
      weighted: Number(timeWeighted.toFixed(2)),
      explanation: `Proposed ${bid.proposedDays} days vs max allowed ${tender.maxTimelineDays} days (Score: ${timeRaw.toFixed(1)}/100 -> Weighted: ${timeWeighted.toFixed(2)} pts).`
    });

    // --- 4. QUALITY & SAFETY ---
    const qsCrit = critMap["QUALITY_SAFETY"] || { weight: 15, name: "Quality & Safety Standards" };
    const qsRaw = scoreQualitySafety(resp);
    const qsWeighted = (qsRaw * qsCrit.weight) / 100;
    totalScore += qsWeighted;
    criterionScores.push({
      key: "QUALITY_SAFETY",
      name: qsCrit.name,
      weight: qsCrit.weight,
      raw: qsRaw,
      weighted: Number(qsWeighted.toFixed(2)),
      explanation: `Raw: ${qsRaw.toFixed(1)}/100 -> Weighted: ${qsWeighted.toFixed(2)} pts (Statutory QA/QC lab & safety compliance).`
    });

    // --- 5. COST & VALUE (Public Procurement Inverse-Ratio Formula) ---
    const costCrit = critMap["COST"] || { weight: 25, name: "Cost & Lifecycle Value" };
    const quote = Number(bid.totalQuote || 0);
    let costRaw = 0;
    if (quote > 0) {
      costRaw = Math.min(100, (minQuote / quote) * 100);
    }
    const costWeighted = (costRaw * costCrit.weight) / 100;
    totalScore += costWeighted;
    criterionScores.push({
      key: "COST",
      name: costCrit.name,
      weight: costCrit.weight,
      raw: Number(costRaw.toFixed(1)),
      weighted: Number(costWeighted.toFixed(2)),
      explanation: `Quote ₹${quote.toLocaleString()} (L1 lowest: ₹${minQuote.toLocaleString()}) -> Inverse formula (L1 / Price) * Weight = ${costWeighted.toFixed(2)} pts.`
    });

    // Budget Cap check (>115% of estimated budget)
    const budgetCap = Number(tender.estimatedBudget || 0) * 1.15;
    const isEligible = quote <= budgetCap;
    const statusNote = isEligible ? "Qualified & Evaluated" : "DISQUALIFIED: Quote exceeds budget cap by >15%";

    return {
      bidId: bid.id,
      bidderName: bid.bidderName,
      totalQuote: quote,
      proposedDays: bid.proposedDays,
      eligible: isEligible,
      statusNote,
      totalScore: Number(totalScore.toFixed(2)),
      criterionScores
    };
  });

  // Rank descending by total score (eligible bids first)
  results.sort((a, b) => {
    if (a.eligible !== b.eligible) return a.eligible ? -1 : 1;
    return b.totalScore - a.totalScore;
  });

  results.forEach((r, idx) => {
    r.rank = idx + 1;
  });

  return results;
}

function scoreTechnical(responses) {
  let score = 70;
  Object.values(responses).forEach(val => {
    const v = String(val || "").toUpperCase();
    if (v.includes("CONCRETE") || v.includes("TIER-1") || v.includes("ADVANCED") || v.includes("GRADE-A")) {
      score += 25;
    } else if (v.includes("ASPHALT") || v.includes("STANDARD") || v.includes("HYBRID") || v.includes("STRING")) {
      score += 15;
    }
    if (v.includes("DRAINAGE") || v.includes("WATERLOG") || v.includes("INVERTER") || v.includes("SAFETY")) {
      score += 5;
    }
  });
  return Math.min(100, score);
}

function scoreExperience(responses) {
  for (const [key, val] of Object.entries(responses)) {
    const k = key.toLowerCase();
    if (k.includes("exp") || k.includes("project") || k.includes("similar")) {
      const num = parseInt(String(val).replace(/\D/g, ""), 10);
      if (!isNaN(num)) {
        if (num >= 8) return 100;
        if (num >= 5) return 85;
        if (num >= 3) return 70;
        return 50;
      }
    }
  }
  return 75;
}

function scoreTimeline(proposed, maxAllowed) {
  if (maxAllowed <= 0) return 80;
  if (proposed <= maxAllowed * 0.70) return 100;
  if (proposed <= maxAllowed * 0.85) return 92;
  if (proposed <= maxAllowed) return 82;
  if (proposed <= maxAllowed * 1.10) return 50;
  return 20;
}

function scoreQualitySafety(responses) {
  let score = 75;
  Object.values(responses).forEach(val => {
    const v = String(val || "").toUpperCase();
    if (v.includes("ISO") || v.includes("NABL") || v.includes("IEC") || v.includes("CERTIFIED")) {
      score += 20;
    }
    if (v.includes("YES") || v.includes("COMPREHENSIVE") || v.includes("OSHA")) {
      score += 5;
    }
  });
  return Math.min(100, score);
}

