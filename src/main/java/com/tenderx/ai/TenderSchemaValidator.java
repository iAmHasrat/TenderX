package com.tenderx.ai;

import com.tenderx.model.EvaluationCriterion;
import com.tenderx.model.FormFieldDefinition;

import java.util.*;

public class TenderSchemaValidator {

    public static class ValidationResult {
        private final boolean valid;
        private final List<String> errors;

        public ValidationResult(boolean valid, List<String> errors) {
            this.valid = valid;
            this.errors = errors;
        }

        public boolean isValid() { return valid; }
        public List<String> getErrors() { return errors; }
    }

    public static ValidationResult validate(List<EvaluationCriterion> criteria) {
        List<String> errors = new ArrayList<>();

        if (criteria == null || criteria.size() != 5) {
            errors.add("Schema must contain exactly 5 mandatory evaluation criteria.");
            return new ValidationResult(false, errors);
        }

        Set<String> requiredKeys = new HashSet<>(Arrays.asList(
                "TECHNICAL", "EXPERIENCE", "TIMELINE", "QUALITY_SAFETY", "COST"
        ));

        double totalWeight = 0.0;
        Set<String> seenKeys = new HashSet<>();

        for (EvaluationCriterion c : criteria) {
            if (c.getCriterionKey() == null || !requiredKeys.contains(c.getCriterionKey())) {
                errors.add("Invalid or unrecognized criterion key: " + c.getCriterionKey());
            }
            if (seenKeys.contains(c.getCriterionKey())) {
                errors.add("Duplicate criterion key detected: " + c.getCriterionKey());
            }
            seenKeys.add(c.getCriterionKey());

            if (c.getWeight() <= 0 || c.getWeight() > 100) {
                errors.add("Criterion weight must be between 1% and 100%: " + c.getName());
            }
            totalWeight += c.getWeight();

            // Validate fields under criterion
            if (c.getFields() != null) {
                for (FormFieldDefinition f : c.getFields()) {
                    if (f.getFieldKey() == null || f.getFieldKey().trim().isEmpty()) {
                        errors.add("Field under " + c.getName() + " missing field key.");
                    }
                    if (f.getLabel() == null || f.getLabel().trim().isEmpty()) {
                        errors.add("Field under " + c.getName() + " missing display label.");
                    }
                    if (f.getType() == null) {
                        errors.add("Field " + f.getFieldKey() + " missing data type.");
                    }
                }
            }
        }

        if (Math.abs(totalWeight - 100.0) > 0.01) {
            errors.add(String.format("Criterion weights must total exactly 100%%. Current total: %.1f%%", totalWeight));
        }

        return new ValidationResult(errors.isEmpty(), errors);
    }
}

