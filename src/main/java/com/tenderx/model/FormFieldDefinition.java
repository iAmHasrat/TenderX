package com.tenderx.model;

import java.util.ArrayList;
import java.util.List;

public class FormFieldDefinition {
    private String id;
    private String criterionId;
    private String fieldKey;
    private String label;
    private String type; // TEXT, NUMBER, ENUM, BOOLEAN
    private List<String> options;
    private boolean required;
    private String unit;

    public FormFieldDefinition() {
        this.options = new ArrayList<>();
        this.required = true;
    }

    public FormFieldDefinition(String id, String criterionId, String fieldKey, String label, String type, List<String> options, boolean required, String unit) {
        this.id = id;
        this.criterionId = criterionId;
        this.fieldKey = fieldKey;
        this.label = label;
        this.type = type;
        this.options = options != null ? options : new ArrayList<>();
        this.required = required;
        this.unit = unit != null ? unit : "";
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCriterionId() { return criterionId; }
    public void setCriterionId(String criterionId) { this.criterionId = criterionId; }

    public String getFieldKey() { return fieldKey; }
    public void setFieldKey(String fieldKey) { this.fieldKey = fieldKey; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public List<String> getOptions() { return options; }
    public void setOptions(List<String> options) { this.options = options; }

    public boolean isRequired() { return required; }
    public void setRequired(boolean required) { this.required = required; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
}

