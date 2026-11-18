export const DEMO_USERS = {
  publisher: {
    id: "usr_publisher_01",
    email: "issuer@nhai.gov.in",
    password: "password123",
    name: "Eng. Rajesh Verma",
    role: "publisher",
    roleTitle: "Tender Authority / Issuer",
    organization: "National Highways Authority of India (NHAI)",
    badgeColor: "indigo"
  },
  bidder1: {
    id: "usr_bidder_01",
    email: "bids@apexinfra.com",
    password: "password123",
    name: "Vikramaditya Rao",
    role: "bidder",
    roleTitle: "Grade-A Highway Contractor",
    organization: "Apex Infrastructure & Geotech Ltd",
    badgeColor: "emerald"
  },
  bidder2: {
    id: "usr_bidder_02",
    email: "bids@buildwell.com",
    password: "password123",
    name: "Sunita Deshmukh",
    role: "bidder",
    roleTitle: "Civil Works Contractor",
    organization: "BuildWell Roadways & Civil Corp",
    badgeColor: "teal"
  },
  bidder3: {
    id: "usr_bidder_03",
    email: "bids@sterling.com",
    password: "password123",
    name: "Arvind Kumar",
    role: "bidder",
    roleTitle: "Heavy Engineering Contractor",
    organization: "Sterling Heavy Engineering Pvt Ltd",
    badgeColor: "cyan"
  },
  admin: {
    id: "usr_admin_01",
    email: "admin@tenderx.gov.in",
    password: "password123",
    name: "Dr. Shalini Mehta",
    role: "admin",
    roleTitle: "Chief Procurement Auditor",
    organization: "Independent Public Vigilance Commission",
    badgeColor: "amber"
  }
};

export const INITIAL_TENDERS = [
  {
    id: "TND-2026-089",
    title: "NH-48 Monsoon Hill Corridor Pavement & Drainage Restoration",
    category: "Civil & Road Construction",
    description: "Repair a 3.5 km sector of National Highway 48 passing through a heavy-rainfall Ghat area prone to severe waterlogging. The road carries over 14,000 commercial heavy axle vehicles daily. Work must finish within 60 days before the peak monsoon. Pavement durability and storm drainage are critical.",
    estimatedBudget: 2500000,
    maxTimelineDays: 60,
    status: "PUBLISHED",
    createdBy: "Ministry of Road Transport & Highways",
    createdAt: "2026-11-12 10:30",
    awardedBidId: null,
    awardedBidderName: null,
    criteria: [
      {
        id: "crit_tech_01",
        key: "TECHNICAL",
        name: "Technical Solution & Material Suitability",
        weight: 35,
        rationale: "The prompt emphasizes heavy rainfall and waterlogging; technical suitability elevated to 35% to prioritize durable pavements and storm drainage.",
        fields: [
          {
            id: "fld_1",
            key: "surface_material",
            label: "Proposed Road Surface & Pavement Grade",
            type: "ENUM",
            options: ["Cement Concrete M-40 High-Strength", "Polymer Modified Bitumen (PMB-40)", "Dense Bituminous Macadam (VG-30)", "Standard Bituminous"],
            required: true
          },
          {
            id: "fld_2",
            key: "drainage_solution",
            label: "Storm Drainage & Waterlogging Mitigation Plan",
            type: "TEXT",
            placeholder: "Describe roadside drains, culverts and slope protection...",
            required: true
          },
          {
            id: "fld_3",
            key: "subgrade_compaction",
            label: "Subgrade Stabilization & Compaction Spec",
            type: "TEXT",
            placeholder: "Compaction density % and stabilization chemicals used...",
            required: true
          }
        ]
      },
      {
        id: "crit_exp_01",
        key: "EXPERIENCE",
        name: "Relevant Experience & Track Record",
        weight: 20,
        rationale: "Ensures contractor has demonstrated competency in high-traffic or monsoon terrain.",
        fields: [
          {
            id: "fld_4",
            key: "monsoon_road_experience",
            label: "Number of Heavy Highway / Monsoon Projects Completed",
            type: "NUMBER",
            unit: "projects",
            required: true
          },
          {
            id: "fld_5",
            key: "equipment_owned",
            label: "Key Machinery Owned (Sensor Pavers, Tandem Rollers)",
            type: "TEXT",
            required: true
          }
        ]
      },
      {
        id: "crit_time_01",
        key: "TIMELINE",
        name: "Execution Plan & Timeline",
        weight: 10,
        rationale: "Compressed timeline weight to prevent rushing pavement curing in wet conditions.",
        fields: [
          {
            id: "fld_6",
            key: "completion_days",
            label: "Proposed Completion Days",
            type: "NUMBER",
            unit: "days",
            required: true
          }
        ]
      },
      {
        id: "crit_qs_01",
        key: "QUALITY_SAFETY",
        name: "Quality Control & Traffic Safety",
        weight: 20,
        rationale: "High vehicle density requires strict safety barricading and on-site testing.",
        fields: [
          {
            id: "fld_7",
            key: "quality_lab_plan",
            label: "On-site Material Testing & Core Extraction Protocol",
            type: "ENUM",
            options: ["NABL Accredited Field Lab On-site", "Third-Party Certified Lab", "Internal QC Process"],
            required: true
          },
          {
            id: "fld_8",
            key: "traffic_safety_plan",
            label: "Heavy Vehicle Traffic Diversion & Worker Safety Plan",
            type: "TEXT",
            required: true
          }
        ]
      },
      {
        id: "crit_cost_01",
        key: "COST",
        name: "Cost & Lifecycle Value",
        weight: 15,
        rationale: "Cost weight tempered to 15% to deter substandard low-ball bidding on critical pavement layers.",
        fields: [
          {
            id: "fld_9",
            key: "total_bid_amount",
            label: "Total All-Inclusive Bid Amount",
            type: "NUMBER",
            unit: "₹",
            required: true
          }
        ]
      }
    ]
  },
  {
    id: "TND-2026-042",
    title: "District Memorial Hospital 200kW Rooftop Solar Captive System",
    category: "Solar & Renewable Energy",
    description: "Turnkey engineering, procurement, and commissioning of a 200 kWp grid-tied solar photovoltaic system on the flat concrete roofs of the hospital blocks. Project includes net-metering grid synchronization and 5 years warranty maintenance. Budget ₹95,00,000, deadline 90 days.",
    estimatedBudget: 9500000,
    maxTimelineDays: 90,
    status: "PUBLISHED",
    createdBy: "State Directorate of Health Services",
    createdAt: "2026-11-14 14:15",
    awardedBidId: null,
    awardedBidderName: null,
    criteria: [
      {
        id: "sol_c1",
        key: "TECHNICAL",
        name: "Technical Solution & Solar Efficiency",
        weight: 30,
        rationale: "Ensures Tier-1 PV efficiency and grid-tie harmonics compliance.",
        fields: [
          {
            id: "sf_1",
            key: "solar_cell_tech",
            label: "Solar PV Cell Technology",
            type: "ENUM",
            options: ["Mono-PERC Tier-1 (550W+)", "Bifacial High Efficiency", "Polycrystalline Standard"],
            required: true
          },
          {
            id: "sf_2",
            key: "inverter_topology",
            label: "Inverter Topology & Efficiency Rating",
            type: "ENUM",
            options: ["String Inverter (>98.6% Eff)", "Central Inverter with MPPT", "Microinverter System"],
            required: true
          }
        ]
      },
      {
        id: "sol_c2",
        key: "EXPERIENCE",
        name: "Relevant Experience & Solar Track Record",
        weight: 20,
        rationale: "Proven track record in hospital or institutional rooftop solar.",
        fields: [
          {
            id: "sf_3",
            key: "solar_completed_projects",
            label: "Number of Grid-Tied Solar Projects (>=100kW)",
            type: "NUMBER",
            unit: "projects",
            required: true
          }
        ]
      },
      {
        id: "sol_c3",
        key: "TIMELINE",
        name: "Execution Plan & Commissioning Schedule",
        weight: 15,
        rationale: "Fast turnaround without disrupting hospital operations.",
        fields: [
          {
            id: "sf_4",
            key: "commissioning_days",
            label: "Proposed Commissioning Timeline",
            type: "NUMBER",
            unit: "days",
            required: true
          }
        ]
      },
      {
        id: "sol_c4",
        key: "QUALITY_SAFETY",
        name: "Quality Control, MNRE & Safety Standards",
        weight: 15,
        rationale: "Strict electrical safety and statutory solar compliance.",
        fields: [
          {
            id: "sf_5",
            key: "solar_certifications",
            label: "Statutory Certifications Held",
            type: "ENUM",
            options: ["IEC 61215 & IEC 61730 Certified", "BIS Approved Tier-1", "Standard ISO 9001"],
            required: true
          }
        ]
      },
      {
        id: "sol_c5",
        key: "COST",
        name: "Cost & 25-Year Lifecycle Value",
        weight: 20,
        rationale: "Evaluates capex and 25-year levelized cost of solar generation.",
        fields: [
          {
            id: "sf_6",
            key: "total_bid_quote",
            label: "Total Turnkey Bid Amount",
            type: "NUMBER",
            unit: "₹",
            required: true
          }
        ]
      }
    ]
  }
];

export const INITIAL_BIDS = [
  {
    id: "BID-101",
    tenderId: "TND-2026-089",
    bidderId: "usr_bidder_01",
    bidderName: "Apex Infrastructure & Geotech Ltd",
    totalQuote: 2420000,
    proposedDays: 52,
    submittedAt: "2026-11-14 11:20",
    status: "EVALUATED",
    totalScore: 89.42,
    responses: {
      surface_material: "Cement Concrete M-40 High-Strength",
      drainage_solution: "Prefabricated RCC U-drains with graded slope filter beds and non-clogging slotted catch pits.",
      subgrade_compaction: "Lime-stabilized subgrade compacted to 98% modified Proctor density under vibrating rollers.",
      monsoon_road_experience: "9",
      equipment_owned: "2x Vogele Sensor Pavers, 3x Hamm Tandem Rollers, Mobile Batching Plant",
      completion_days: "52",
      quality_lab_plan: "NABL Accredited Field Lab On-site",
      traffic_safety_plan: "Dual-lane bypass diversion with retroreflective signage and OSHA-certified marshals.",
      total_bid_amount: "2420000"
    }
  },
  {
    id: "BID-102",
    tenderId: "TND-2026-089",
    bidderId: "usr_bidder_02",
    bidderName: "BuildWell Roadways & Civil Corp",
    totalQuote: 2150000,
    proposedDays: 58,
    submittedAt: "2026-11-14 15:40",
    status: "EVALUATED",
    totalScore: 74.15,
    responses: {
      surface_material: "Standard Bituminous",
      drainage_solution: "Surface earthen side drains along unpaved shoulder.",
      subgrade_compaction: "Standard roller pass compaction.",
      monsoon_road_experience: "3",
      equipment_owned: "Hired mechanical pavers and basic rollers",
      completion_days: "58",
      quality_lab_plan: "Internal QC Process",
      traffic_safety_plan: "Standard traffic barricades during daylight hours.",
      total_bid_amount: "2150000"
    }
  },
  {
    id: "BID-103",
    tenderId: "TND-2026-089",
    bidderId: "usr_bidder_03",
    bidderName: "Sterling Heavy Engineering Pvt Ltd",
    totalQuote: 2380000,
    proposedDays: 48,
    submittedAt: "2026-11-15 09:10",
    status: "EVALUATED",
    totalScore: 84.80,
    responses: {
      surface_material: "Polymer Modified Bitumen (PMB-40)",
      drainage_solution: "Reinforced box drains with storm chambers and silt traps.",
      subgrade_compaction: "Geo-grid reinforcement with 97% compaction.",
      monsoon_road_experience: "6",
      equipment_owned: "1x Sensor Paver, 2x Pneumatic Tyre Rollers",
      completion_days: "48",
      quality_lab_plan: "Third-Party Certified Lab",
      traffic_safety_plan: "Traffic management plan with automated variable message signs.",
      total_bid_amount: "2380000"
    }
  }
];

