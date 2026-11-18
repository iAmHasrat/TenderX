package com.tenderx.ai;

import com.tenderx.model.EvaluationCriterion;
import com.tenderx.model.FormFieldDefinition;

import java.util.*;

public class TenderDesignService {

    public List<EvaluationCriterion> generateEvaluationSchema(String projectDescription, String category, double budget, int maxDays) {
        String desc = projectDescription != null ? projectDescription.toLowerCase() : "";
        List<EvaluationCriterion> criteria = new ArrayList<>();

        if (desc.contains("solar") || desc.contains("photovoltaic") || desc.contains("energy") || "Solar & Renewable Energy".equalsIgnoreCase(category)) {
            // Solar & Renewable
            criteria.add(createCriterion(
                    "TECHNICAL", "Technical Solution & Material Suitability", 30.0,
                    "High technical weight assigned to ensure Tier-1 PV module efficiency and grid-tie inverter harmonics compliance.",
                    Arrays.asList(
                            new FormFieldDefinition(UUID.randomUUID().toString(), null, "solar_cell_tech", "Solar PV Cell Technology", "ENUM",
                                    Arrays.asList("Mono-PERC Tier-1 (550W+)", "Bifacial High Efficiency", "Polycrystalline Standard"), true, ""),
                            new FormFieldDefinition(UUID.randomUUID().toString(), null, "inverter_topology", "Inverter Topology & Efficiency", "ENUM",
                                    Arrays.asList("String Inverter (>98.6% Eff)", "Central Inverter with MPPT", "Microinverter System"), true, ""),
                            new FormFieldDefinition(UUID.randomUUID().toString(), null, "net_metering_plan", "Grid Interconnection & Net Metering Plan", "TEXT", null, true, "")
                    )
            ));

            criteria.add(createCriterion(
                    "EXPERIENCE", "Relevant Experience & Track Record", 20.0,
                    "Verifies proven execution in similar MW/kW scale rooftop/ground solar installations.",
                    Arrays.asList(
                            new FormFieldDefinition(UUID.randomUUID().toString(), null, "solar_completed_projects", "Number of Grid-Tied Solar Projects (>=100kW)", "NUMBER", null, true, "projects"),
                            new FormFieldDefinition(UUID.randomUUID().toString(), null, "client_references", "Verifiable Client Reference Letters", "TEXT", null, true, "")
                    )
            ));

            criteria.add(createCriterion(
                    "TIMELINE", "Execution Plan & Commissioning Schedule", 15.0,
                    "Mandates rapid installation without disrupting site operations.",
                    Arrays.asList(
                            new FormFieldDefinition(UUID.randomUUID().toString(), null, "commissioning_days", "Proposed Commissioning Timeline", "NUMBER", null, true, "days")
                    )
            ));

            criteria.add(createCriterion(
                    "QUALITY_SAFETY", "Quality Control, MNRE & Safety Certifications", 15.0,
                    "Strict electrical safety and statutory solar compliance.",
                    Arrays.asList(
                            new FormFieldDefinition(UUID.randomUUID().toString(), null, "solar_certifications", "Statutory Certifications Held", "ENUM",
                                    Arrays.asList("IEC 61215 & IEC 61730 Certified", "BIS Approved Tier-1", "Standard ISO 9001"), true, "")
                    )
            ));

            criteria.add(createCriterion(
                    "COST", "Cost & Lifecycle Value (LCOE)", 20.0,
                    "Evaluates capex and 25-year levelized cost of solar generation.",
                    Arrays.asList(
                            new FormFieldDefinition(UUID.randomUUID().toString(), null, "total_bid_quote", "Total Turnkey Bid Amount", "NUMBER", null, true, "₹")
                    )
            ));

        } else if (desc.contains("rain") || desc.contains("monsoon") || desc.contains("waterlog") || desc.contains("flood") || desc.contains("heavy vehicle") || desc.contains("traffic")) {
            // High Rainfall / Heavy Load Road
            criteria.add(createCriterion(
                    "TECHNICAL", "Technical Solution & Material Suitability", 35.0,
                    "The prompt emphasizes heavy rainfall and waterlogging; technical suitability elevated to 35% to prioritize durable pavements and storm drainage.",
                    Arrays.asList(
                            new FormFieldDefinition(UUID.randomUUID().toString(), null, "surface_material", "Proposed Road Surface & Grade", "ENUM",
                                    Arrays.asList("Cement Concrete M-40 High-Strength", "Polymer Modified Bitumen (PMB-40)", "Dense Bituminous Macadam (VG-30)", "Standard Bituminous"), true, ""),
                            new FormFieldDefinition(UUID.randomUUID().toString(), null, "drainage_solution", "Storm Drainage & Waterlogging Mitigation Plan", "TEXT", null, true, ""),
                            new FormFieldDefinition(UUID.randomUUID().toString(), null, "subgrade_compaction", "Subgrade Stabilization & Compaction Spec", "TEXT", null, true, "")
                    )
            ));

            criteria.add(createCriterion(
                    "EXPERIENCE", "Relevant Experience", 20.0,
                    "Ensures contractor has demonstrated competency in waterlogged or heavy-traffic terrains.",
                    Arrays.asList(
                            new FormFieldDefinition(UUID.randomUUID().toString(), null, "monsoon_road_experience", "Number of Heavy Highway / Monsoon Projects Completed", "NUMBER", null, true, "projects"),
                            new FormFieldDefinition(UUID.randomUUID().toString(), null, "equipment_owned", "Key Machinery Owned (e.g. Sensor Pavers, Tandem Rollers)", "TEXT", null, true, "")
                    )
            ));

            criteria.add(createCriterion(
                    "TIMELINE", "Execution Plan & Timeline", 10.0,
                    "Compressed timeline weight to prevent rushing pavement curing in wet conditions.",
                    Arrays.asList(
                            new FormFieldDefinition(UUID.randomUUID().toString(), null, "completion_days", "Proposed Completion Days", "NUMBER", null, true, "days")
                    )
            ));

            criteria.add(createCriterion(
                    "QUALITY_SAFETY", "Quality Control & Traffic Safety", 20.0,
                    "High traffic requires strict safety barricades and on-site compaction quality tests.",
                    Arrays.asList(
                            new FormFieldDefinition(UUID.randomUUID().toString(), null, "quality_lab_plan", "On-site Material Testing & Core Extraction Protocol", "ENUM",
                                    Arrays.asList("NABL Accredited Field Lab On-site", "Third-Party Certified Lab", "Internal QC Process"), true, ""),
                            new FormFieldDefinition(UUID.randomUUID().toString(), null, "traffic_safety_plan", "Heavy Vehicle Traffic Diversion & Worker Safety Plan", "TEXT", null, true, "")
                    )
            ));

            criteria.add(createCriterion(
                    "COST", "Cost & Lifecycle Value", 15.0,
                    "Cost weight tempered to 15% to deter substandard low-ball bidding on critical pavement layers.",
                    Arrays.asList(
                            new FormFieldDefinition(UUID.randomUUID().toString(), null, "total_bid_amount", "Total All-Inclusive Bid Amount", "NUMBER", null, true, "₹")
                    )
            ));

        } else if (desc.contains("paint") || desc.contains("routine") || desc.contains("maintenance") || desc.contains("renovation")) {
            // Routine Maintenance / Painting
            criteria.add(createCriterion(
                    "TECHNICAL", "Technical Solution & Coating Suitability", 20.0,
                    "Standard surface preparation and coating specs.",
                    Arrays.asList(
                            new FormFieldDefinition(UUID.randomUUID().toString(), null, "coating_type", "Proposed Paint Coating System", "ENUM",
                                    Arrays.asList("Weatherproof Elastomeric Acrylic (8-Yr Warranty)", "Silicone Exterior Emulsion", "Standard Acrylic Emulsion"), true, ""),
                            new FormFieldDefinition(UUID.randomUUID().toString(), null, "surface_prep_method", "Surface Preparation & Crack Sealing Protocol", "TEXT", null, true, "")
                    )
            ));

            criteria.add(createCriterion(
                    "EXPERIENCE", "Relevant Experience", 20.0,
                    "Prior completed commercial/institutional facility painting jobs.",
                    Arrays.asList(
                            new FormFieldDefinition(UUID.randomUUID().toString(), null, "painting_projects", "Number of Similar Multi-Storey Projects Completed", "NUMBER", null, true, "projects")
                    )
            ));

            criteria.add(createCriterion(
                    "TIMELINE", "Execution Plan & Schedule", 15.0,
                    "Floor-by-floor schedule minimizing occupant disturbance.",
                    Arrays.asList(
                            new FormFieldDefinition(UUID.randomUUID().toString(), null, "execution_days", "Proposed Project Duration", "NUMBER", null, true, "days")
                    )
            ));

            criteria.add(createCriterion(
                    "QUALITY_SAFETY", "Workmanship & Heights Safety", 15.0,
                    "Safety harnesses and scaffolding compliance for work at height.",
                    Arrays.asList(
                            new FormFieldDefinition(UUID.randomUUID().toString(), null, "scaffolding_safety", "Scaffolding & Fall-Arrest Compliance Protocol", "TEXT", null, true, "")
                    )
            ));

            criteria.add(createCriterion(
                    "COST", "Cost & Value for Money", 30.0,
                    "For routine painting, economic value and cost efficiency is given high priority.",
                    Arrays.asList(
                            new FormFieldDefinition(UUID.randomUUID().toString(), null, "total_cost", "Total Lump-Sum Cost", "NUMBER", null, true, "₹")
                    )
            ));

        } else {
            // General Infrastructure / Civil Works Default
            criteria.add(createCriterion(
                    "TECHNICAL", "Technical Solution and Material Suitability", 25.0,
                    "Ensures proposed engineering approach satisfies baseline departmental standards.",
                    Arrays.asList(
                            new FormFieldDefinition(UUID.randomUUID().toString(), null, "tech_spec_summary", "Detailed Technical Specification & Materials", "TEXT", null, true, ""),
                            new FormFieldDefinition(UUID.randomUUID().toString(), null, "methodology", "Construction / Execution Methodology", "TEXT", null, true, "")
                    )
            ));

            criteria.add(createCriterion(
                    "EXPERIENCE", "Relevant Experience", 20.0,
                    "Verifies track record on comparable state/central government tenders.",
                    Arrays.asList(
                            new FormFieldDefinition(UUID.randomUUID().toString(), null, "similar_projects_count", "Number of Similar Completed Works", "NUMBER", null, true, "projects")
                    )
            ));

            criteria.add(createCriterion(
                    "TIMELINE", "Execution Plan and Timeline", 15.0,
                    "Evaluates milestone sequencing against the project completion target.",
                    Arrays.asList(
                            new FormFieldDefinition(UUID.randomUUID().toString(), null, "proposed_duration", "Proposed Timeline", "NUMBER", null, true, "days")
                    )
            ));

            criteria.add(createCriterion(
                    "QUALITY_SAFETY", "Quality Control and Safety Standards", 15.0,
                    "Quality management framework, QA/QC documentation, and occupational safety.",
                    Arrays.asList(
                            new FormFieldDefinition(UUID.randomUUID().toString(), null, "qc_certifications", "Quality Management Standard", "ENUM",
                                    Arrays.asList("ISO 9001 & ISO 45001 Certified", "Departmental Approved QA Manual", "Standard Process"), true, "")
                    )
            ));

            criteria.add(createCriterion(
                    "COST", "Cost and Lifecycle Value", 25.0,
                    "Competitive price quote compared against department engineer estimate.",
                    Arrays.asList(
                            new FormFieldDefinition(UUID.randomUUID().toString(), null, "bid_price", "Total Commercial Quote", "NUMBER", null, true, "₹")
                    )
            ));
        }

        return criteria;
    }

    private EvaluationCriterion createCriterion(String key, String name, double weight, String rationale, List<FormFieldDefinition> fields) {
        EvaluationCriterion c = new EvaluationCriterion();
        c.setId(UUID.randomUUID().toString());
        c.setCriterionKey(key);
        c.setName(name);
        c.setWeight(weight);
        c.setRationale(rationale);
        if (fields != null) {
            for (FormFieldDefinition f : fields) {
                f.setCriterionId(c.getId());
                c.addField(f);
            }
        }
        return c;
    }
}

