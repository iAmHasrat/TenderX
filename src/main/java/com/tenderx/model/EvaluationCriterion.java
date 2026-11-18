package com.tenderx.model;

import java.util.ArrayList;
import java.util.List;

public class EvaluationCriterion {
    private String id;
    private String tenderId;
    private String criterionKey; // TECHNICAL, EXPERIENCE, TIMELINE, QUALITY_SAFETY, COST
    private String name;
    private double weight; // e.g. 25.0 (percentage, total must sum to 100)
    private String rationale;
    private List<FormFieldDefinition> fields;

    public EvaluationCriterion() {
        this.fields = new ArrayList<>();
    }

    public EvaluationCriterion(String id, String tenderId, String criterionKey, String name, double weight, String rationale) {
        this.id = id;
        this.tenderId = tenderId;
        this.criterionKey = criterionKey;
        this.name = name;
        this.weight = weight;
        this.rationale = rationale;
        this.fields = new ArrayList<>();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTenderId() { return tenderId; }
    public void setTenderId(String tenderId) { this.tenderId = tenderId; }

    public String getCriterionKey() { return criterionKey; }
    public void setCriterionKey(String criterionKey) { this.criterionKey = criterionKey; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }

    public String getRationale() { return rationale; }
    public void setRationale(String rationale) { this.rationale = rationale; }

    public List<FormFieldDefinition> getFields() { return fields; }
    public void setFields(List<FormFieldDefinition> fields) { this.fields = fields; }

    public void addField(FormFieldDefinition field) {
        if (this.fields == null) this.fields = new ArrayList<>();
        this.fields.add(field);
    }
}

