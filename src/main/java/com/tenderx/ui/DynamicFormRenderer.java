package com.tenderx.ui;

import com.tenderx.model.EvaluationCriterion;
import com.tenderx.model.FormFieldDefinition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DynamicFormRenderer {

    private final Map<String, Control> fieldInputControls = new HashMap<>();
    private final Map<String, FormFieldDefinition> fieldDefinitions = new HashMap<>();

    public VBox renderCriteriaForms(List<EvaluationCriterion> criteria) {
        VBox root = new VBox(20);
        root.setPadding(new Insets(10));
        fieldInputControls.clear();
        fieldDefinitions.clear();

        for (int i = 0; i < criteria.size(); i++) {
            EvaluationCriterion criterion = criteria.get(i);
            VBox criterionCard = new VBox(12);
            criterionCard.getStyleClass().add("card-criterion");

            // Header of the criterion section
            HBox header = new HBox(12);
            header.setAlignment(Pos.CENTER_LEFT);

            Label badge = new Label(String.format("Criterion %d (Weight: %.0f%%)", i + 1, criterion.getWeight()));
            badge.getStyleClass().add("badge-primary");

            Label title = new Label(criterion.getName());
            title.getStyleClass().add("criterion-header-title");

            header.getChildren().addAll(badge, title);

            if (criterion.getRationale() != null && !criterion.getRationale().isEmpty()) {
                Label rationale = new Label("AI Priority Rationale: " + criterion.getRationale());
                rationale.getStyleClass().add("criterion-rationale-note");
                rationale.setWrapText(true);
                criterionCard.getChildren().addAll(header, rationale);
            } else {
                criterionCard.getChildren().add(header);
            }

            // Grid or VBox of fields
            VBox fieldsBox = new VBox(14);
            fieldsBox.setPadding(new Insets(8, 0, 4, 0));

            for (FormFieldDefinition field : criterion.getFields()) {
                fieldDefinitions.put(field.getFieldKey(), field);
                Node fieldNode = createFieldControl(field);
                fieldsBox.getChildren().add(fieldNode);
            }

            criterionCard.getChildren().add(fieldsBox);
            root.getChildren().add(criterionCard);
        }

        return root;
    }

    private Node createFieldControl(FormFieldDefinition field) {
        VBox container = new VBox(6);

        HBox labelBox = new HBox(6);
        labelBox.setAlignment(Pos.CENTER_LEFT);

        Label label = new Label(field.getLabel());
        label.getStyleClass().add("field-label");

        labelBox.getChildren().add(label);
        if (field.isRequired()) {
            Label req = new Label("*");
            req.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
            labelBox.getChildren().add(req);
        }

        Control inputControl;
        if ("ENUM".equalsIgnoreCase(field.getType()) && field.getOptions() != null && !field.getOptions().isEmpty()) {
            ComboBox<String> combo = new ComboBox<>();
            combo.getItems().addAll(field.getOptions());
            combo.getSelectionModel().selectFirst();
            combo.setMaxWidth(Double.MAX_VALUE);
            combo.getStyleClass().add("modern-combo");
            inputControl = combo;
        } else if ("NUMBER".equalsIgnoreCase(field.getType())) {
            TextField tf = new TextField();
            tf.setPromptText("Enter number" + (field.getUnit().isEmpty() ? "" : " (" + field.getUnit() + ")"));
            tf.getStyleClass().add("modern-input");
            inputControl = tf;
        } else if ("BOOLEAN".equalsIgnoreCase(field.getType())) {
            CheckBox cb = new CheckBox("Yes, confirmed and compliant");
            cb.getStyleClass().add("modern-checkbox");
            inputControl = cb;
        } else {
            // TEXT or long text
            if (field.getLabel().toLowerCase().contains("plan") || field.getLabel().toLowerCase().contains("method") || field.getLabel().toLowerCase().contains("spec")) {
                TextArea ta = new TextArea();
                ta.setPromptText("Describe the detailed approach or technical parameters...");
                ta.setPrefRowCount(3);
                ta.setWrapText(true);
                ta.getStyleClass().add("modern-textarea");
                inputControl = ta;
            } else {
                TextField tf = new TextField();
                tf.setPromptText("Enter details...");
                tf.getStyleClass().add("modern-input");
                inputControl = tf;
            }
        }

        fieldInputControls.put(field.getFieldKey(), inputControl);
        container.getChildren().addAll(labelBox, inputControl);
        return container;
    }

    public Map<String, String> getFormValues() {
        Map<String, String> values = new HashMap<>();
        for (Map.Entry<String, Control> entry : fieldInputControls.entrySet()) {
            String key = entry.getKey();
            Control ctrl = entry.getValue();
            String val = "";

            if (ctrl instanceof ComboBox) {
                Object selected = ((ComboBox<?>) ctrl).getSelectionModel().getSelectedItem();
                val = selected != null ? selected.toString() : "";
            } else if (ctrl instanceof TextField) {
                val = ((TextField) ctrl).getText().trim();
            } else if (ctrl instanceof TextArea) {
                val = ((TextArea) ctrl).getText().trim();
            } else if (ctrl instanceof CheckBox) {
                val = ((CheckBox) ctrl).isSelected() ? "Yes" : "No";
            }
            values.put(key, val);
        }
        return values;
    }

    public Map<String, FormFieldDefinition> getFieldDefinitions() {
        return fieldDefinitions;
    }

    public void autoFillRealisticData() {
        for (Map.Entry<String, Control> entry : fieldInputControls.entrySet()) {
            String key = entry.getKey().toLowerCase();
            Control ctrl = entry.getValue();

            if (ctrl instanceof ComboBox) {
                ComboBox<?> cb = (ComboBox<?>) ctrl;
                if (!cb.getItems().isEmpty()) {
                    cb.getSelectionModel().select(0);
                }
            } else if (ctrl instanceof TextField) {
                TextField tf = (TextField) ctrl;
                if (key.contains("day") || key.contains("time")) {
                    tf.setText("45");
                } else if (key.contains("exp") || key.contains("project")) {
                    tf.setText("7");
                } else if (key.contains("cost") || key.contains("amount") || key.contains("quote") || key.contains("price")) {
                    tf.setText("2280000");
                } else {
                    tf.setText("Verified specification complying with department code");
                }
            } else if (ctrl instanceof TextArea) {
                TextArea ta = (TextArea) ctrl;
                if (key.contains("drain")) {
                    ta.setText("High-discharge pre-cast concrete U-drains with non-clogging slotted catch pits and stabilized roadside runoff channels.");
                } else if (key.contains("compaction") || key.contains("subgrade")) {
                    ta.setText("Subgrade stabilization with 4% cementitious additive and 98% heavy modified compaction under calibrated roller passes.");
                } else if (key.contains("safety") || key.contains("traffic")) {
                    ta.setText("Dedicated two-lane daytime diversion with computerized flashing beacons and certified roadside safety marshals.");
                } else {
                    ta.setText("Comprehensive quality assurance protocol with weekly core extraction and NABL testing.");
                }
            } else if (ctrl instanceof CheckBox) {
                ((CheckBox) ctrl).setSelected(true);
            }
        }
    }
}

