package com.tenderx.db;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tenderx.ai.TenderDesignService;
import com.tenderx.model.*;

import java.lang.reflect.Type;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:tenderx.db";
    private static final Gson gson = new Gson();
    private static DatabaseManager instance;

    private DatabaseManager() {
        initDatabase();
        seedInitialDemoData();
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    private void initDatabase() {
        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            // Tenders table
            stmt.execute("CREATE TABLE IF NOT EXISTS tenders (" +
                    "id TEXT PRIMARY KEY, " +
                    "title TEXT NOT NULL, " +
                    "description TEXT NOT NULL, " +
                    "category TEXT, " +
                    "estimated_budget REAL, " +
                    "max_timeline_days INTEGER, " +
                    "status TEXT DEFAULT 'DRAFT', " +
                    "awarded_bid_id TEXT, " +
                    "awarded_bidder_name TEXT, " +
                    "created_by TEXT, " +
                    "created_at TEXT)");

            // Tender criteria
            stmt.execute("CREATE TABLE IF NOT EXISTS tender_criteria (" +
                    "id TEXT PRIMARY KEY, " +
                    "tender_id TEXT NOT NULL, " +
                    "criterion_key TEXT NOT NULL, " +
                    "name TEXT NOT NULL, " +
                    "weight REAL NOT NULL, " +
                    "rationale TEXT, " +
                    "FOREIGN KEY (tender_id) REFERENCES tenders(id) ON DELETE CASCADE)");

            // Dynamic Form Fields under criteria
            stmt.execute("CREATE TABLE IF NOT EXISTS criterion_fields (" +
                    "id TEXT PRIMARY KEY, " +
                    "criterion_id TEXT NOT NULL, " +
                    "field_key TEXT NOT NULL, " +
                    "label TEXT NOT NULL, " +
                    "field_type TEXT NOT NULL, " +
                    "options_json TEXT, " +
                    "is_required INTEGER DEFAULT 1, " +
                    "unit TEXT, " +
                    "FOREIGN KEY (criterion_id) REFERENCES tender_criteria(id) ON DELETE CASCADE)");

            // Bids table
            stmt.execute("CREATE TABLE IF NOT EXISTS bids (" +
                    "id TEXT PRIMARY KEY, " +
                    "tender_id TEXT NOT NULL, " +
                    "bidder_id TEXT NOT NULL, " +
                    "bidder_name TEXT NOT NULL, " +
                    "total_quote REAL NOT NULL, " +
                    "proposed_timeline_days INTEGER, " +
                    "submitted_at TEXT, " +
                    "status TEXT DEFAULT 'SUBMITTED', " +
                    "total_score REAL DEFAULT 0, " +
                    "evaluation_summary TEXT, " +
                    "FOREIGN KEY (tender_id) REFERENCES tenders(id) ON DELETE CASCADE)");

            // Dynamic Bid Responses per field
            stmt.execute("CREATE TABLE IF NOT EXISTS bid_responses (" +
                    "id TEXT PRIMARY KEY, " +
                    "bid_id TEXT NOT NULL, " +
                    "field_key TEXT NOT NULL, " +
                    "field_label TEXT, " +
                    "response_value TEXT, " +
                    "FOREIGN KEY (bid_id) REFERENCES bids(id) ON DELETE CASCADE)");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveTender(Tender tender) {
        String sqlTender = "INSERT OR REPLACE INTO tenders (id, title, description, category, estimated_budget, max_timeline_days, status, awarded_bid_id, awarded_bidder_name, created_by, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = connect()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sqlTender)) {
                ps.setString(1, tender.getId());
                ps.setString(2, tender.getTitle());
                ps.setString(3, tender.getDescription());
                ps.setString(4, tender.getCategory());
                ps.setDouble(5, tender.getEstimatedBudget());
                ps.setInt(6, tender.getMaxTimelineDays());
                ps.setString(7, tender.getStatus());
                ps.setString(8, tender.getAwardedBidId());
                ps.setString(9, tender.getAwardedBidderName());
                ps.setString(10, tender.getCreatedBy());
                ps.setString(11, tender.getCreatedAt());
                ps.executeUpdate();
            }

            // Delete old criteria & fields for replacement
            try (PreparedStatement psDel = conn.prepareStatement("DELETE FROM tender_criteria WHERE tender_id = ?")) {
                psDel.setString(1, tender.getId());
                psDel.executeUpdate();
            }

            // Insert Criteria & Fields
            String sqlCrit = "INSERT INTO tender_criteria (id, tender_id, criterion_key, name, weight, rationale) VALUES (?, ?, ?, ?, ?, ?)";
            String sqlField = "INSERT INTO criterion_fields (id, criterion_id, field_key, label, field_type, options_json, is_required, unit) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

            for (EvaluationCriterion c : tender.getCriteria()) {
                try (PreparedStatement psCrit = conn.prepareStatement(sqlCrit)) {
                    psCrit.setString(1, c.getId());
                    psCrit.setString(2, tender.getId());
                    psCrit.setString(3, c.getCriterionKey());
                    psCrit.setString(4, c.getName());
                    psCrit.setDouble(5, c.getWeight());
                    psCrit.setString(6, c.getRationale());
                    psCrit.executeUpdate();
                }

                if (c.getFields() != null) {
                    for (FormFieldDefinition f : c.getFields()) {
                        try (PreparedStatement psField = conn.prepareStatement(sqlField)) {
                            psField.setString(1, f.getId() != null ? f.getId() : UUID.randomUUID().toString());
                            psField.setString(2, c.getId());
                            psField.setString(3, f.getFieldKey());
                            psField.setString(4, f.getLabel());
                            psField.setString(5, f.getType());
                            psField.setString(6, gson.toJson(f.getOptions()));
                            psField.setInt(7, f.isRequired() ? 1 : 0);
                            psField.setString(8, f.getUnit());
                            psField.executeUpdate();
                        }
                    }
                }
            }

            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Tender> getAllTenders() {
        List<Tender> list = new ArrayList<>();
        String sql = "SELECT * FROM tenders ORDER BY created_at DESC";

        try (Connection conn = connect(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Tender t = new Tender(
                        rs.getString("id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getString("category"),
                        rs.getDouble("estimated_budget"),
                        rs.getInt("max_timeline_days"),
                        rs.getString("status"),
                        rs.getString("created_by"),
                        rs.getString("created_at")
                );
                t.setAwardedBidId(rs.getString("awarded_bid_id"));
                t.setAwardedBidderName(rs.getString("awarded_bidder_name"));
                loadCriteriaForTender(conn, t);
                list.add(t);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Tender getTender(String id) {
        String sql = "SELECT * FROM tenders WHERE id = ?";
        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Tender t = new Tender(
                            rs.getString("id"),
                            rs.getString("title"),
                            rs.getString("description"),
                            rs.getString("category"),
                            rs.getDouble("estimated_budget"),
                            rs.getInt("max_timeline_days"),
                            rs.getString("status"),
                            rs.getString("created_by"),
                            rs.getString("created_at")
                    );
                    t.setAwardedBidId(rs.getString("awarded_bid_id"));
                    t.setAwardedBidderName(rs.getString("awarded_bidder_name"));
                    loadCriteriaForTender(conn, t);
                    return t;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void loadCriteriaForTender(Connection conn, Tender tender) throws SQLException {
        String sqlCrit = "SELECT * FROM tender_criteria WHERE tender_id = ?";
        String sqlField = "SELECT * FROM criterion_fields WHERE criterion_id = ?";
        Type listType = new TypeToken<List<String>>(){}.getType();

        try (PreparedStatement psCrit = conn.prepareStatement(sqlCrit)) {
            psCrit.setString(1, tender.getId());
            try (ResultSet rsCrit = psCrit.executeQuery()) {
                while (rsCrit.next()) {
                    EvaluationCriterion c = new EvaluationCriterion(
                            rsCrit.getString("id"),
                            rsCrit.getString("tender_id"),
                            rsCrit.getString("criterion_key"),
                            rsCrit.getString("name"),
                            rsCrit.getDouble("weight"),
                            rsCrit.getString("rationale")
                    );

                    try (PreparedStatement psField = conn.prepareStatement(sqlField)) {
                        psField.setString(1, c.getId());
                        try (ResultSet rsField = psField.executeQuery()) {
                            while (rsField.next()) {
                                List<String> opts = gson.fromJson(rsField.getString("options_json"), listType);
                                FormFieldDefinition f = new FormFieldDefinition(
                                        rsField.getString("id"),
                                        rsField.getString("criterion_id"),
                                        rsField.getString("field_key"),
                                        rsField.getString("label"),
                                        rsField.getString("field_type"),
                                        opts,
                                        rsField.getInt("is_required") == 1,
                                        rsField.getString("unit")
                                );
                                c.addField(f);
                            }
                        }
                    }
                    tender.getCriteria().add(c);
                }
            }
        }
    }

    public void saveBid(Bid bid) {
        String sqlBid = "INSERT OR REPLACE INTO bids (id, tender_id, bidder_id, bidder_name, total_quote, proposed_timeline_days, submitted_at, status, total_score, evaluation_summary) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String sqlResp = "INSERT INTO bid_responses (id, bid_id, field_key, field_label, response_value) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = connect()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sqlBid)) {
                ps.setString(1, bid.getId());
                ps.setString(2, bid.getTenderId());
                ps.setString(3, bid.getBidderId());
                ps.setString(4, bid.getBidderName());
                ps.setDouble(5, bid.getTotalQuote());
                ps.setInt(6, bid.getProposedTimelineDays());
                ps.setString(7, bid.getSubmittedAt());
                ps.setString(8, bid.getStatus());
                ps.setDouble(9, bid.getTotalScore());
                ps.setString(10, bid.getEvaluationSummary());
                ps.executeUpdate();
            }

            try (PreparedStatement psDel = conn.prepareStatement("DELETE FROM bid_responses WHERE bid_id = ?")) {
                psDel.setString(1, bid.getId());
                psDel.executeUpdate();
            }

            for (BidResponse r : bid.getResponses()) {
                try (PreparedStatement psResp = conn.prepareStatement(sqlResp)) {
                    psResp.setString(1, r.getId() != null ? r.getId() : UUID.randomUUID().toString());
                    psResp.setString(2, bid.getId());
                    psResp.setString(3, r.getFieldKey());
                    psResp.setString(4, r.getFieldLabel());
                    psResp.setString(5, r.getResponseValue());
                    psResp.executeUpdate();
                }
            }

            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Bid> getBidsForTender(String tenderId) {
        List<Bid> bids = new ArrayList<>();
        String sql = "SELECT * FROM bids WHERE tender_id = ? ORDER BY total_score DESC";
        String sqlResp = "SELECT * FROM bid_responses WHERE bid_id = ?";

        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tenderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Bid b = new Bid(
                            rs.getString("id"),
                            rs.getString("tender_id"),
                            rs.getString("bidder_id"),
                            rs.getString("bidder_name"),
                            rs.getDouble("total_quote"),
                            rs.getInt("proposed_timeline_days"),
                            rs.getString("submitted_at")
                    );
                    b.setStatus(rs.getString("status"));
                    b.setTotalScore(rs.getDouble("total_score"));
                    b.setEvaluationSummary(rs.getString("evaluation_summary"));

                    try (PreparedStatement psResp = conn.prepareStatement(sqlResp)) {
                        psResp.setString(1, b.getId());
                        try (ResultSet rsResp = psResp.executeQuery()) {
                            while (rsResp.next()) {
                                b.addResponse(new BidResponse(
                                        rsResp.getString("id"),
                                        rsResp.getString("bid_id"),
                                        rsResp.getString("field_key"),
                                        rsResp.getString("field_label"),
                                        rsResp.getString("response_value")
                                ));
                            }
                        }
                    }
                    bids.add(b);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bids;
    }

    public void saveBidScore(String bidId, double totalScore, String summary) {
        String sql = "UPDATE bids SET total_score = ?, evaluation_summary = ?, status = 'EVALUATED' WHERE id = ?";
        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, totalScore);
            ps.setString(2, summary);
            ps.setString(3, bidId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateTenderAward(String tenderId, String awardedBidId, String awardedBidderName) {
        String sql = "UPDATE tenders SET status = 'AWARDED', awarded_bid_id = ?, awarded_bidder_name = ? WHERE id = ?";
        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, awardedBidId);
            ps.setString(2, awardedBidderName);
            ps.setString(3, tenderId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateTenderStatus(String tenderId, String status) {
        String sql = "UPDATE tenders SET status = ? WHERE id = ?";
        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, tenderId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void seedInitialDemoData() {
        if (!getAllTenders().isEmpty()) return; // Already seeded

        TenderDesignService ai = new TenderDesignService();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        // --- Demo Tender 1: NH-48 Heavy Rainfall Road Repair ---
        String t1Id = "TND-2026-089";
        String t1Title = "NH-48 Monsoon Hill Corridor Pavement & Drainage Restoration";
        String t1Desc = "Repair a 3.5 km sector of National Highway 48 passing through a heavy-rainfall Ghat area prone to severe waterlogging. The road carries over 14,000 commercial heavy axle vehicles daily. Work must finish within 60 days before the peak monsoon. Estimated budget ₹25,00,000.";
        Tender t1 = new Tender(t1Id, t1Title, t1Desc, "Civil & Road Construction", 2500000.0, 60, "PUBLISHED", "Ministry of Road Transport & Highways", LocalDateTime.now().minusDays(5).format(dtf));
        t1.setCriteria(ai.generateEvaluationSchema(t1Desc, t1.getCategory(), t1.getEstimatedBudget(), t1.getMaxTimelineDays()));
        saveTender(t1);

        // Seed 3 realistic competing bids for Tender 1
        // Bid A: Apex Infra Ltd (Concrete M-40, high durability, slightly higher price)
        Bid b1 = new Bid("BID-101", t1Id, "BDR-01", "Apex Infrastructure & Geotech Ltd", 2420000.0, 52, LocalDateTime.now().minusDays(2).format(dtf));
        b1.addResponse(new BidResponse(UUID.randomUUID().toString(), b1.getId(), "surface_material", "Proposed Road Surface & Grade", "Cement Concrete M-40 High-Strength"));
        b1.addResponse(new BidResponse(UUID.randomUUID().toString(), b1.getId(), "drainage_solution", "Storm Drainage & Waterlogging Mitigation Plan", "Prefabricated RCC U-drains with graded slope filter beds and non-clogging culverts"));
        b1.addResponse(new BidResponse(UUID.randomUUID().toString(), b1.getId(), "subgrade_compaction", "Subgrade Stabilization & Compaction Spec", "Lime-stabilized subgrade compacted to 98% modified Proctor density"));
        b1.addResponse(new BidResponse(UUID.randomUUID().toString(), b1.getId(), "monsoon_road_experience", "Number of Heavy Highway / Monsoon Projects Completed", "9"));
        b1.addResponse(new BidResponse(UUID.randomUUID().toString(), b1.getId(), "equipment_owned", "Key Machinery Owned", "2x Vogele Sensor Pavers, 3x Hamm Tandem Rollers, Mobile Batching Plant"));
        b1.addResponse(new BidResponse(UUID.randomUUID().toString(), b1.getId(), "completion_days", "Proposed Completion Days", "52"));
        b1.addResponse(new BidResponse(UUID.randomUUID().toString(), b1.getId(), "quality_lab_plan", "On-site Material Testing & Core Extraction Protocol", "NABL Accredited Field Lab On-site"));
        b1.addResponse(new BidResponse(UUID.randomUUID().toString(), b1.getId(), "traffic_safety_plan", "Heavy Vehicle Traffic Diversion & Worker Safety Plan", "Dual-lane bypass diversion with retroreflective signage and OSHA-certified marshals"));
        b1.addResponse(new BidResponse(UUID.randomUUID().toString(), b1.getId(), "total_bid_amount", "Total All-Inclusive Bid Amount", "2420000"));
        saveBid(b1);

        // Bid B: BuildWell Civil (Dense Bituminous, cheapest price, lower spec)
        Bid b2 = new Bid("BID-102", t1Id, "BDR-02", "BuildWell Roadways & Civil Corp", 2150000.0, 58, LocalDateTime.now().minusDays(1).format(dtf));
        b2.addResponse(new BidResponse(UUID.randomUUID().toString(), b2.getId(), "surface_material", "Proposed Road Surface & Grade", "Standard Bituminous"));
        b2.addResponse(new BidResponse(UUID.randomUUID().toString(), b2.getId(), "drainage_solution", "Storm Drainage & Waterlogging Mitigation Plan", "Surface earthen side drains along unpaved shoulder"));
        b2.addResponse(new BidResponse(UUID.randomUUID().toString(), b2.getId(), "subgrade_compaction", "Subgrade Stabilization & Compaction Spec", "Standard roller pass compaction"));
        b2.addResponse(new BidResponse(UUID.randomUUID().toString(), b2.getId(), "monsoon_road_experience", "Number of Heavy Highway / Monsoon Projects Completed", "3"));
        b2.addResponse(new BidResponse(UUID.randomUUID().toString(), b2.getId(), "equipment_owned", "Key Machinery Owned", "Hired mechanical pavers and basic rollers"));
        b2.addResponse(new BidResponse(UUID.randomUUID().toString(), b2.getId(), "completion_days", "Proposed Completion Days", "58"));
        b2.addResponse(new BidResponse(UUID.randomUUID().toString(), b2.getId(), "quality_lab_plan", "On-site Material Testing & Core Extraction Protocol", "Internal QC Process"));
        b2.addResponse(new BidResponse(UUID.randomUUID().toString(), b2.getId(), "traffic_safety_plan", "Heavy Vehicle Traffic Diversion & Worker Safety Plan", "Standard traffic barricades during daylight hours"));
        b2.addResponse(new BidResponse(UUID.randomUUID().toString(), b2.getId(), "total_bid_amount", "Total All-Inclusive Bid Amount", "2150000"));
        saveBid(b2);

        // Bid C: Sterling Infra (Polymer Modified Bitumen, balanced)
        Bid b3 = new Bid("BID-103", t1Id, "BDR-03", "Sterling Heavy Engineering Pvt Ltd", 2380000.0, 48, LocalDateTime.now().minusHours(18).format(dtf));
        b3.addResponse(new BidResponse(UUID.randomUUID().toString(), b3.getId(), "surface_material", "Proposed Road Surface & Grade", "Polymer Modified Bitumen (PMB-40)"));
        b3.addResponse(new BidResponse(UUID.randomUUID().toString(), b3.getId(), "drainage_solution", "Storm Drainage & Waterlogging Mitigation Plan", "Reinforced box drains with storm chambers"));
        b3.addResponse(new BidResponse(UUID.randomUUID().toString(), b3.getId(), "subgrade_compaction", "Subgrade Stabilization & Compaction Spec", "Geo-grid reinforcement with 97% compaction"));
        b3.addResponse(new BidResponse(UUID.randomUUID().toString(), b3.getId(), "monsoon_road_experience", "Number of Heavy Highway / Monsoon Projects Completed", "6"));
        b3.addResponse(new BidResponse(UUID.randomUUID().toString(), b3.getId(), "equipment_owned", "Key Machinery Owned", "1x Sensor Paver, 2x Pneumatic Tyre Rollers"));
        b3.addResponse(new BidResponse(UUID.randomUUID().toString(), b3.getId(), "completion_days", "Proposed Completion Days", "48"));
        b3.addResponse(new BidResponse(UUID.randomUUID().toString(), b3.getId(), "quality_lab_plan", "On-site Material Testing & Core Extraction Protocol", "Third-Party Certified Lab"));
        b3.addResponse(new BidResponse(UUID.randomUUID().toString(), b3.getId(), "traffic_safety_plan", "Heavy Vehicle Traffic Diversion & Worker Safety Plan", "Traffic management plan with automated variable message signs"));
        b3.addResponse(new BidResponse(UUID.randomUUID().toString(), b3.getId(), "total_bid_amount", "Total All-Inclusive Bid Amount", "2380000"));
        saveBid(b3);

        // --- Demo Tender 2: District Hospital 200kW Solar Rooftop ---
        String t2Id = "TND-2026-042";
        String t2Title = "District Memorial Hospital 200kW Solar Rooftop Captive Plant";
        String t2Desc = "Turnkey engineering, procurement, and commissioning of a 200 kWp grid-tied solar photovoltaic system on the flat concrete roofs of the hospital blocks. Project includes net-metering grid synchronization and 5 years warranty maintenance. Budget ₹95,00,000, deadline 90 days.";
        Tender t2 = new Tender(t2Id, t2Title, t2Desc, "Solar & Renewable Energy", 9500000.0, 90, "PUBLISHED", "State Directorate of Health Services", LocalDateTime.now().minusDays(3).format(dtf));
        t2.setCriteria(ai.generateEvaluationSchema(t2Desc, t2.getCategory(), t2.getEstimatedBudget(), t2.getMaxTimelineDays()));
        saveTender(t2);
    }
}
