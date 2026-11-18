package com.tenderx.evaluation;

import com.tenderx.ai.TenderDesignService;
import com.tenderx.model.Bid;
import com.tenderx.model.BidResponse;
import com.tenderx.model.Tender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ScoringEngineTest {

    private ScoringEngine engine;
    private TenderDesignService designService;
    private Tender sampleTender;

    @BeforeEach
    void setUp() {
        engine = new ScoringEngine();
        designService = new TenderDesignService();

        sampleTender = new Tender(
                "TND-TEST",
                "Highway Rain Drainage Repair",
                "Repair road in high rainfall area. High heavy vehicle traffic.",
                "Civil & Road Construction",
                2000000.0,
                60,
                "PUBLISHED",
                "Govt Test Dept",
                "2026-11-16 12:00"
        );
        sampleTender.setCriteria(designService.generateEvaluationSchema(
                sampleTender.getDescription(),
                sampleTender.getCategory(),
                sampleTender.getEstimatedBudget(),
                sampleTender.getMaxTimelineDays()
        ));
    }

    @Test
    @DisplayName("Verify that bids are scored deterministically and ranked by total score")
    void testDeterministicScoringAndRanking() {
        // Bid A: Premium Spec (Concrete M-40, good drainage, 50 days, 1.9M)
        Bid bidA = new Bid("BID-A", sampleTender.getId(), "B1", "Contractor Alpha", 1900000.0, 50, "2026-11-16");
        bidA.addResponse(new BidResponse("1", "BID-A", "surface_material", "Pavement", "Cement Concrete M-40 High-Strength"));
        bidA.addResponse(new BidResponse("2", "BID-A", "drainage_solution", "Drainage", "Slotted RCC storm water drain"));
        bidA.addResponse(new BidResponse("3", "BID-A", "monsoon_road_experience", "Experience", "8"));
        bidA.addResponse(new BidResponse("4", "BID-A", "quality_lab_plan", "QC", "NABL Accredited Field Lab On-site"));

        // Bid B: Budget Spec (Bituminous, standard, 58 days, 1.6M lowest price)
        Bid bidB = new Bid("BID-B", sampleTender.getId(), "B2", "Contractor Beta", 1600000.0, 58, "2026-11-16");
        bidB.addResponse(new BidResponse("5", "BID-B", "surface_material", "Pavement", "Standard Bituminous"));
        bidB.addResponse(new BidResponse("6", "BID-B", "drainage_solution", "Drainage", "Earthen side ditch"));
        bidB.addResponse(new BidResponse("7", "BID-B", "monsoon_road_experience", "Experience", "3"));
        bidB.addResponse(new BidResponse("8", "BID-B", "quality_lab_plan", "QC", "Internal QC Process"));

        List<EvaluationResult> results = engine.evaluateTenderBids(sampleTender, Arrays.asList(bidA, bidB));

        assertEquals(2, results.size());
        assertEquals(1, results.get(0).getRank());
        assertEquals(2, results.get(1).getRank());

        // In high rainfall conditions (Technical=35%, Safety=20%, Cost=15%),
        // Contractor Alpha should outscore Beta despite higher price due to technical weight!
        assertEquals("BID-A", results.get(0).getBidId(), "Contractor Alpha with superior technical spec should achieve Rank 1");
        assertTrue(results.get(0).getTotalScore() > results.get(1).getTotalScore());
    }

    @Test
    @DisplayName("Verify that bids severely exceeding the budget cap are disqualified")
    void testBudgetDisqualification() {
        // Budget is 2.0M, bid is 2.5M (> 115% threshold)
        Bid excessiveBid = new Bid("BID-X", sampleTender.getId(), "BX", "Overpriced Ltd", 2500000.0, 45, "2026-11-16");
        List<EvaluationResult> results = engine.evaluateTenderBids(sampleTender, List.of(excessiveBid));

        assertFalse(results.get(0).isEligible());
        assertTrue(results.get(0).getStatusNote().contains("DISQUALIFIED"));
    }
}

