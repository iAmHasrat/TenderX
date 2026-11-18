package com.tenderx.ai;

import com.tenderx.model.EvaluationCriterion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TenderSchemaValidatorTest {

    @Test
    @DisplayName("Verify that valid 5-criteria schema totaling 100% passes validation")
    void testValidSchema() {
        TenderDesignService service = new TenderDesignService();
        List<EvaluationCriterion> criteria = service.generateEvaluationSchema(
                "Repair road in rainfall area",
                "Civil & Road Construction",
                1000000,
                60
        );

        TenderSchemaValidator.ValidationResult result = TenderSchemaValidator.validate(criteria);
        assertTrue(result.isValid());
        assertTrue(result.getErrors().isEmpty());
    }

    @Test
    @DisplayName("Verify that schema with weights not summing to 100% is rejected")
    void testInvalidWeightsSum() {
        TenderDesignService service = new TenderDesignService();
        List<EvaluationCriterion> criteria = service.generateEvaluationSchema(
                "Repair road in rainfall area",
                "Civil & Road Construction",
                1000000,
                60
        );

        // Intentionally tamper with weight
        criteria.get(0).setWeight(50.0); // Now sums to 115%

        TenderSchemaValidator.ValidationResult result = TenderSchemaValidator.validate(criteria);
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("must total exactly 100%")));
    }
}

