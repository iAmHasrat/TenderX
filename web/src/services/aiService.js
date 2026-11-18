// Intelligent AI Tender Design & Schema Proposal Engine
// Supports Google Gemini API, xAI Grok API, and zero-latency smart heuristic engine

export async function generateCriteriaWithLLM({ description, category, budget, days, apiKey, provider = 'gemini' }) {
  if (apiKey && apiKey.trim()) {
    try {
      if (provider === 'grok') {
        return await callGrokAPI({ description, category, budget, days, apiKey });
      } else {
        return await callGeminiAPI({ description, category, budget, days, apiKey });
      }
    } catch (err) {
      console.warn("Live LLM API call failed:", err);
      // Re-throw with descriptive message so the UI can inform the user
      throw err;
    }
  }

  // Fallback / Default Smart Heuristic Generator (when no API key is provided)
  return generateCriteriaFromDescription({ description, category, budget, days });
}

// Google Gemini 1.5/2.0 API Handler
async function callGeminiAPI({ description, category, budget, days, apiKey }) {
  const prompt = `You are a certified senior civil & public procurement engineering specialist.
Analyze the following tender project and return a structured JSON evaluation schema.

PROJECT DETAILS:
- Category: ${category}
- Estimated Budget Cap: ₹${budget}
- Mandatory Deadline: ${days} days
- Description & Requirements: ${description}

REQUIREMENTS:
1. You must output EXACTLY 5 criteria with these keys: "TECHNICAL", "EXPERIENCE", "TIMELINE", "QUALITY_SAFETY", "COST".
2. The weights of the 5 criteria MUST sum to exactly 100.
3. For each criterion, provide:
   - "key": one of the 5 keys
   - "name": human-readable name
   - "weight": integer or float (sum = 100)
   - "rationale": one sentence explaining why this weight was assigned based on the project constraints.
   - "fields": an array of 1-3 dynamic response fields bidders must fill out. Each field has:
     - "id": string
     - "key": string (lowercase identifier)
     - "label": string (display name)
     - "type": "TEXT" | "NUMBER" | "ENUM" | "BOOLEAN"
     - "options": array of strings (ONLY if type is ENUM)
     - "unit": string (optional, e.g. "days", "projects", "₹")
     - "required": true

OUTPUT ONLY VALID RAW JSON. No surrounding markdown, no backticks, no explanatory prose.`;

  const url = `https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=${apiKey.trim()}`;
  const response = await fetch(url, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      contents: [{ parts: [{ text: prompt }] }],
      generationConfig: {
        temperature: 0.2,
        responseMimeType: "application/json"
      }
    })
  });

  if (!response.ok) {
    throw new Error(`Gemini API HTTP Error: ${response.status} ${response.statusText}`);
  }

  const data = await response.json();
  const rawText = data.candidates?.[0]?.content?.parts?.[0]?.text || '';
  const cleaned = cleanJsonString(rawText);
  const parsed = JSON.parse(cleaned);

  // If the model returned an object with a "criteria" array or a direct array
  const criteriaList = Array.isArray(parsed) ? parsed : (parsed.criteria || parsed.evaluationCriteria);
  if (!criteriaList || criteriaList.length !== 5) {
    throw new Error("Gemini did not return exactly 5 criteria");
  }

  return normalizeCriteria(criteriaList);
}

// xAI Grok API Handler
async function callGrokAPI({ description, category, budget, days, apiKey }) {
  const prompt = `Analyze this tender and return a JSON object with a "criteria" array of 5 items with keys TECHNICAL, EXPERIENCE, TIMELINE, QUALITY_SAFETY, COST. Total weights must sum to 100.
Project: ${category}, Budget ₹${budget}, Deadline ${days} days.
Description: ${description}.
Each criterion must have: id, key, name, weight, rationale, and fields (array of id, key, label, type, options, required).
Return ONLY JSON.`;

  const response = await fetch("https://api.x.ai/v1/chat/completions", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      "Authorization": `Bearer ${apiKey.trim()}`
    },
    body: JSON.stringify({
      model: "grok-beta",
      messages: [
        { role: "system", content: "You are a public procurement engineering engine that outputs strictly valid JSON." },
        { role: "user", content: prompt }
      ],
      temperature: 0.2
    })
  });

  if (!response.ok) {
    throw new Error(`Grok API HTTP Error: ${response.status}`);
  }

  const data = await response.json();
  const rawText = data.choices?.[0]?.message?.content || '';
  const cleaned = cleanJsonString(rawText);
  const parsed = JSON.parse(cleaned);
  const criteriaList = Array.isArray(parsed) ? parsed : (parsed.criteria || parsed.evaluationCriteria);
  return normalizeCriteria(criteriaList);
}

function cleanJsonString(str) {
  let s = (str || '').trim();
  const match = s.match(/```(?:json)?\s*([\s\S]*?)\s*```/);
  if (match) {
    return match[1].trim();
  }
  const firstBrace = s.indexOf('{');
  const firstBracket = s.indexOf('[');
  let start = -1;
  if (firstBrace !== -1 && firstBracket !== -1) {
    start = Math.min(firstBrace, firstBracket);
  } else if (firstBrace !== -1) {
    start = firstBrace;
  } else if (firstBracket !== -1) {
    start = firstBracket;
  }
  const lastBrace = s.lastIndexOf('}');
  const lastBracket = s.lastIndexOf(']');
  const end = Math.max(lastBrace, lastBracket);
  if (start !== -1 && end !== -1 && end > start) {
    return s.substring(start, end + 1);
  }
  return s;
}

function normalizeCriteria(list) {
  return list.map((c, idx) => ({
    id: c.id || `crit_${c.key?.toLowerCase() || idx}_${Date.now()}`,
    key: c.key || ["TECHNICAL", "EXPERIENCE", "TIMELINE", "QUALITY_SAFETY", "COST"][idx],
    name: c.name || `Criterion ${idx + 1}`,
    weight: Number(c.weight || 20),
    rationale: c.rationale || "Calibrated by LLM based on specified project constraints.",
    fields: (c.fields || []).map((f, fIdx) => ({
      id: f.id || `fld_${Date.now()}_${fIdx}`,
      key: f.key || `field_${fIdx}`,
      label: f.label || `Requirement ${fIdx + 1}`,
      type: f.type || "TEXT",
      options: f.options || null,
      unit: f.unit || "",
      required: f.required !== false
    }))
  }));
}

// Built-in Smart Heuristic Generator (Offline & Demo Safe)
export function generateCriteriaFromDescription({ description, category, budget, days }) {
  const text = (description || "").toLowerCase();

  let weights = {
    TECHNICAL: 25,
    EXPERIENCE: 20,
    TIMELINE: 15,
    QUALITY_SAFETY: 15,
    COST: 25
  };

  let rationale = {
    TECHNICAL: "Evaluates whether proposed engineering approach satisfies departmental codes.",
    EXPERIENCE: "Verifies contractor track record on comparable public works.",
    TIMELINE: "Checks ability to finish within allotted timeframe.",
    QUALITY_SAFETY: "Standard quality management and occupational safety standards.",
    COST: "Competitive price evaluation against department schedule of rates."
  };

  let dynamicFields = {
    TECHNICAL: [
      { id: "f_tech_1", key: "tech_spec", label: "Proposed Technical Specification & Method", type: "TEXT", required: true },
      { id: "f_tech_2", key: "material_grade", label: "Primary Material Grade / Standard", type: "ENUM", options: ["High Performance Grade A", "Standard Department Grade", "Alternative Approved Spec"], required: true }
    ],
    EXPERIENCE: [
      { id: "f_exp_1", key: "similar_projects", label: "Number of Comparable Projects Completed", type: "NUMBER", unit: "projects", required: true }
    ],
    TIMELINE: [
      { id: "f_time_1", key: "proposed_days", label: "Proposed Total Execution Days", type: "NUMBER", unit: "days", required: true }
    ],
    QUALITY_SAFETY: [
      { id: "f_qs_1", key: "iso_cert", label: "Quality / Safety Certifications Held", type: "ENUM", options: ["ISO 9001 & ISO 45001", "Standard State Certification", "Internal QA Manual"], required: true }
    ],
    COST: [
      { id: "f_cost_1", key: "bid_amount", label: "Total All-Inclusive Commercial Quote", type: "NUMBER", unit: "₹", required: true }
    ]
  };

  // Scenario 1: Rainfall, Monsoon, Waterlogging, Drainage, Heavy Traffic, Highways
  if (text.includes("rain") || text.includes("monsoon") || text.includes("waterlog") || text.includes("drain") || text.includes("flood") || text.includes("heavy vehicle") || text.includes("traffic")) {
    weights = {
      TECHNICAL: 35,
      EXPERIENCE: 20,
      TIMELINE: 10,
      QUALITY_SAFETY: 20,
      COST: 15
    };

    rationale.TECHNICAL = "Description highlights heavy rainfall & intense heavy axle traffic. Technical suitability elevated to 35% to prioritize durable pavements and dedicated storm drainage.";
    rationale.QUALITY_SAFETY = "High traffic necessitates strict safety barricades and on-site compaction quality testing.";
    rationale.TIMELINE = "Compressed to 10% to prevent rushing pavement curing in wet conditions.";
    rationale.COST = "Cost weight tempered to 15% to deter substandard low-ball bidding on critical pavement layers.";

    dynamicFields.TECHNICAL = [
      { id: "f_rd_1", key: "surface_material", label: "Proposed Road Surface & Pavement Grade", type: "ENUM", options: ["Cement Concrete M-40 High-Strength", "Polymer Modified Bitumen (PMB-40)", "Dense Bituminous Macadam (VG-30)", "Standard Bituminous"], required: true },
      { id: "f_rd_2", key: "drainage_solution", label: "Storm Drainage & Waterlogging Mitigation Plan", type: "TEXT", placeholder: "Prefabricated RCC U-drains, culverts, slope stabilization...", required: true },
      { id: "f_rd_3", key: "subgrade_compaction", label: "Subgrade Stabilization & Compaction Spec", type: "TEXT", placeholder: "Compaction density % and stabilization chemicals used...", required: true }
    ];
    dynamicFields.EXPERIENCE = [
      { id: "f_rd_4", key: "monsoon_road_experience", label: "Number of Heavy Highway / Monsoon Projects Completed", type: "NUMBER", unit: "projects", required: true },
      { id: "f_rd_5", key: "equipment_owned", label: "Key Machinery Owned (Sensor Pavers, Tandem Rollers)", type: "TEXT", placeholder: "List pavers, batching plants, and rollers owned...", required: true }
    ];
    dynamicFields.TIMELINE = [
      { id: "f_rd_6", key: "completion_days", label: "Proposed Completion Days", type: "NUMBER", unit: "days", required: true }
    ];
    dynamicFields.QUALITY_SAFETY = [
      { id: "f_rd_7", key: "quality_lab_plan", label: "On-site Material Testing & Core Extraction Protocol", type: "ENUM", options: ["NABL Accredited Field Lab On-site", "Third-Party Certified Lab", "Internal QC Process"], required: true },
      { id: "f_rd_8", key: "traffic_safety_plan", label: "Heavy Vehicle Traffic Diversion & Worker Safety Plan", type: "TEXT", placeholder: "Barricades, marshals, retroreflective signs...", required: true }
    ];
    dynamicFields.COST = [
      { id: "f_rd_9", key: "total_bid_amount", label: "Total All-Inclusive Bid Amount", type: "NUMBER", unit: "₹", required: true }
    ];
  } 
  // Scenario 2: Solar, Renewable Energy, Rooftop, Grid
  else if (text.includes("solar") || text.includes("photovoltaic") || text.includes("energy") || text.includes("inverter") || text.includes("pv") || category === "Solar & Renewable Energy") {
    weights = {
      TECHNICAL: 30,
      EXPERIENCE: 20,
      TIMELINE: 15,
      QUALITY_SAFETY: 15,
      COST: 20
    };

    rationale.TECHNICAL = "High technical priority assigned to ensure Tier-1 PV efficiency and grid-tie harmonics compliance.";
    rationale.COST = "Capex evaluated against 25-year Levelized Cost of Energy (LCOE).";

    dynamicFields.TECHNICAL = [
      { id: "f_sol_1", key: "solar_cell_tech", label: "Solar PV Cell Technology", type: "ENUM", options: ["Mono-PERC Tier-1 (550W+)", "Bifacial High Efficiency", "Polycrystalline Standard"], required: true },
      { id: "f_sol_2", key: "inverter_topology", label: "Inverter Topology & Efficiency Rating", type: "ENUM", options: ["String Inverter (>98.6% Eff)", "Central Inverter with MPPT", "Microinverter System"], required: true },
      { id: "f_sol_3", key: "net_metering", label: "Grid Interconnection & Net Metering Plan", type: "TEXT", placeholder: "Discom sync and protective relay setup...", required: true }
    ];
    dynamicFields.EXPERIENCE = [
      { id: "f_sol_4", key: "solar_completed_projects", label: "Number of Grid-Tied Solar Projects (>=100kW)", type: "NUMBER", unit: "projects", required: true }
    ];
    dynamicFields.TIMELINE = [
      { id: "f_sol_5", key: "commissioning_days", label: "Proposed Commissioning Timeline", type: "NUMBER", unit: "days", required: true }
    ];
    dynamicFields.QUALITY_SAFETY = [
      { id: "f_sol_6", key: "solar_certifications", label: "Statutory Certifications Held", type: "ENUM", options: ["IEC 61215 & IEC 61730 Certified", "BIS Approved Tier-1", "Standard ISO 9001"], required: true }
    ];
    dynamicFields.COST = [
      { id: "f_sol_7", key: "total_bid_quote", label: "Total Turnkey Bid Amount", type: "NUMBER", unit: "₹", required: true }
    ];
  } 
  // Scenario 3: Painting, Routine Renovation, Maintenance
  else if (text.includes("paint") || text.includes("routine") || text.includes("renovat") || text.includes("maintenance")) {
    weights = {
      TECHNICAL: 20,
      EXPERIENCE: 20,
      TIMELINE: 15,
      QUALITY_SAFETY: 15,
      COST: 30
    };

    rationale.COST = "For routine maintenance & painting, cost economy and competitive pricing are prioritized at 30%.";
    rationale.TECHNICAL = "Focuses on surface preparation and weatherproof coating durability.";

    dynamicFields.TECHNICAL = [
      { id: "f_pt_1", key: "coating_type", label: "Proposed Paint Coating System", type: "ENUM", options: ["Weatherproof Elastomeric Acrylic (8-Yr Warranty)", "Silicone Exterior Emulsion", "Standard Acrylic Emulsion"], required: true },
      { id: "f_pt_2", key: "surface_prep", label: "Surface Prep & Crack Sealing Protocol", type: "TEXT", placeholder: "High pressure jet washing and epoxy patching...", required: true }
    ];
    dynamicFields.EXPERIENCE = [
      { id: "f_pt_3", key: "painting_projects", label: "Number of Similar Multi-Storey Facilities Completed", type: "NUMBER", unit: "projects", required: true }
    ];
    dynamicFields.TIMELINE = [
      { id: "f_pt_4", key: "execution_days", label: "Proposed Project Duration", type: "NUMBER", unit: "days", required: true }
    ];
    dynamicFields.QUALITY_SAFETY = [
      { id: "f_pt_5", key: "height_safety", label: "Scaffolding & Work-at-Height Safety Protocol", type: "TEXT", placeholder: "Harnesses, safety netting, certified scaffolding...", required: true }
    ];
    dynamicFields.COST = [
      { id: "f_pt_6", key: "total_cost", label: "Total Lump-Sum Cost", type: "NUMBER", unit: "₹", required: true }
    ];
  }

  return [
    {
      id: "c_tech_" + Date.now(),
      key: "TECHNICAL",
      name: "Technical Solution & Material Suitability",
      weight: weights.TECHNICAL,
      rationale: rationale.TECHNICAL,
      fields: dynamicFields.TECHNICAL
    },
    {
      id: "c_exp_" + Date.now(),
      key: "EXPERIENCE",
      name: "Relevant Experience & Track Record",
      weight: weights.EXPERIENCE,
      rationale: rationale.EXPERIENCE,
      fields: dynamicFields.EXPERIENCE
    },
    {
      id: "c_time_" + Date.now(),
      key: "TIMELINE",
      name: "Execution Plan & Timeline",
      weight: weights.TIMELINE,
      rationale: rationale.TIMELINE,
      fields: dynamicFields.TIMELINE
    },
    {
      id: "c_qs_" + Date.now(),
      key: "QUALITY_SAFETY",
      name: "Quality Control & Safety Standards",
      weight: weights.QUALITY_SAFETY,
      rationale: rationale.QUALITY_SAFETY,
      fields: dynamicFields.QUALITY_SAFETY
    },
    {
      id: "c_cost_" + Date.now(),
      key: "COST",
      name: "Cost & Lifecycle Value",
      weight: weights.COST,
      rationale: rationale.COST,
      fields: dynamicFields.COST
    }
  ];
}

export function validateCriteriaWeights(criteria) {
  if (!criteria || criteria.length !== 5) {
    return { valid: false, message: "Exactly 5 mandatory criteria required." };
  }
  const total = criteria.reduce((sum, c) => sum + Number(c.weight || 0), 0);
  if (Math.abs(total - 100) > 0.01) {
    return { valid: false, total, message: `Weights must sum to exactly 100%. Current total: ${total}%` };
  }
  return { valid: true, total, message: "Weights sum to 100% (Approved)" };
}
