package com.tenderx.ui;

import com.tenderx.ai.TenderDesignService;
import com.tenderx.ai.TenderSchemaValidator;
import com.tenderx.db.DatabaseManager;
import com.tenderx.model.EvaluationCriterion;
import com.tenderx.model.FormFieldDefinition;
import com.tenderx.model.Tender;
import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class AITenderDesignerView {

    private final MainView mainView;
    private final TenderDesignService aiService = new TenderDesignService();
    private final DatabaseManager db = DatabaseManager.getInstance();

    private final VBox root = new VBox(20);
    private TextField txtTitle;
    private TextArea txtDescription;
    private ComboBox<String> comboCategory;
    private TextField txtBudget;
    private TextField txtDays;

    private VBox generatedSchemaBox;
    private Label lblTotalWeight;
    private Label lblWeightStatus;
    private Button btnPublish;

    private List<EvaluationCriterion> currentCriteria = new ArrayList<>();
    private final Map<String, Slider> weightSliders = new HashMap<>();
    private final Map<String, Label> weightLabels = new HashMap<>();

    public AITenderDesignerView(MainView mainView) {
        this.mainView = mainView;
        buildUI();
    }

    public VBox getView() {
        return root;
    }

    private void buildUI() {
        root.setPadding(new Insets(24));

        // 1. Header
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        VBox titleBox = new VBox(4);
        Label title = new Label("AI Tender Specification & Criteria Designer");
        title.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 20px; -fx-font-weight: 800;");
        Label subtitle = new Label("Step 1 to 4: Describe project in natural language -> AI extracts criteria, weights & dynamic form -> Issuer reviews & approves");
        subtitle.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 13px;");
        titleBox.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnBack = new Button("<- Back to Dashboard");
        btnBack.getStyleClass().add("btn-secondary");
        btnBack.setOnAction(e -> mainView.navigateTo("DASHBOARD"));

        header.getChildren().addAll(titleBox, spacer, btnBack);
        root.getChildren().add(header);

        // 2. Input Card (Project Description & Parameters)
        VBox inputCard = new VBox(14);
        inputCard.getStyleClass().add("card-highlight");

        HBox quickScenarios = new HBox(10);
        quickScenarios.setAlignment(Pos.CENTER_LEFT);
        Label scenarioLbl = new Label("Quick Test Scenarios:");
        scenarioLbl.setStyle("-fx-text-fill: #38bdf8; -fx-font-weight: bold; -fx-font-size: 12px;");

        Button chipMonsoon = new Button("🌧️ Scenario A: Road in High-Rainfall Area");
        chipMonsoon.getStyleClass().add("chip-btn");
        chipMonsoon.setOnAction(e -> loadMonsoonScenario());

        Button chipSolar = new Button("☀️ Scenario B: Hospital Solar Rooftop");
        chipSolar.getStyleClass().add("chip-btn");
        chipSolar.setOnAction(e -> loadSolarScenario());

        Button chipPainting = new Button("🎨 Scenario C: Routine Building Repainting");
        chipPainting.getStyleClass().add("chip-btn");
        chipPainting.setOnAction(e -> loadPaintingScenario());

        quickScenarios.getChildren().addAll(scenarioLbl, chipMonsoon, chipSolar, chipPainting);
        inputCard.getChildren().add(quickScenarios);

        // Grid for fields
        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(12);

        // Project Title
        Label lblT = new Label("Tender Project Title *");
        lblT.getStyleClass().add("field-label");
        txtTitle = new TextField();
        txtTitle.setPromptText("e.g. State Highway 12 Monsoon Pavement Stabilization");
        txtTitle.getStyleClass().add("modern-input");
        grid.add(lblT, 0, 0);
        grid.add(txtTitle, 0, 1, 2, 1);

        // Category
        Label lblC = new Label("Procurement Category *");
        lblC.getStyleClass().add("field-label");
        comboCategory = new ComboBox<>();
        comboCategory.getItems().addAll(
                "Civil & Road Construction",
                "Solar & Renewable Energy",
                "Building Maintenance & Painting",
                "IT Infrastructure & Networking"
        );
        comboCategory.getSelectionModel().selectFirst();
        comboCategory.getStyleClass().add("modern-combo");
        comboCategory.setMaxWidth(Double.MAX_VALUE);
        grid.add(lblC, 2, 0);
        grid.add(comboCategory, 2, 1);

        // Natural Language Description
        Label lblD = new Label("Natural Language Project Description & Constraints * (Describe road, weather, soil, or goals)");
        lblD.getStyleClass().add("field-label");
        txtDescription = new TextArea();
        txtDescription.setPromptText("Enter full natural language project scope. e.g.: Repair 2 km of road near a waterlogged area with heavy rainfall. The road carries heavy vehicles. Work must finish within 60 days.");
        txtDescription.setPrefRowCount(3);
        txtDescription.setWrapText(true);
        txtDescription.getStyleClass().add("modern-textarea");
        grid.add(lblD, 0, 2, 3, 1);
        grid.add(txtDescription, 0, 3, 3, 1);

        // Budget & Timeline
        Label lblB = new Label("Estimated Budget Cap (₹) *");
        lblB.getStyleClass().add("field-label");
        txtBudget = new TextField("1000000");
        txtBudget.getStyleClass().add("modern-input");
        grid.add(lblB, 0, 4);
        grid.add(txtBudget, 0, 5);

        Label lblTime = new Label("Mandatory Completion Deadline (Days) *");
        lblTime.getStyleClass().add("field-label");
        txtDays = new TextField("60");
        txtDays.getStyleClass().add("modern-input");
        grid.add(lblTime, 1, 4);
        grid.add(txtDays, 1, 5);

        inputCard.getChildren().add(grid);

        // Generate Button
        HBox btnBox = new HBox(12);
        btnBox.setAlignment(Pos.CENTER_LEFT);

        Button btnGenerate = new Button("✨ Analyze Description & Generate Criteria + Dynamic Form with AI");
        btnGenerate.getStyleClass().add("btn-ai");
        btnGenerate.setOnAction(e -> handleGenerateAI(btnGenerate));

        Label aiNote = new Label("⚡ LLM extracts environmental parameters and proposes calibrated weights & response fields");
        aiNote.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px; -fx-font-style: italic;");

        btnBox.getChildren().addAll(btnGenerate, aiNote);
        inputCard.getChildren().add(btnBox);

        root.getChildren().add(inputCard);

        // 3. Generated Schema & Review Section
        generatedSchemaBox = new VBox(16);
        generatedSchemaBox.setVisible(false);
        generatedSchemaBox.setManaged(false);

        // Weight verification bar
        HBox weightBar = new HBox(16);
        weightBar.setAlignment(Pos.CENTER_LEFT);
        weightBar.setStyle("-fx-background-color: #1e293b; -fx-padding: 14 20; -fx-background-radius: 8px; -fx-border-color: #334155; -fx-border-radius: 8px;");

        Label weightBarTitle = new Label("Review & Calibrate Weights (Deterministic Rule: Must Total Exactly 100%):");
        weightBarTitle.setStyle("-fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-font-size: 13px;");

        lblTotalWeight = new Label("Total: 100%");
        lblTotalWeight.setStyle("-fx-font-size: 15px; -fx-font-weight: 900; -fx-text-fill: #10b981;");

        lblWeightStatus = new Label("✔ VALID SCHEMA APPROVED");
        lblWeightStatus.getStyleClass().add("badge-success");

        Region weightSpacer = new Region();
        HBox.setHgrow(weightSpacer, Priority.ALWAYS);

        btnPublish = new Button("✅ Approve Criteria & Publish Tender");
        btnPublish.getStyleClass().add("btn-success");
        btnPublish.setOnAction(e -> handlePublishTender());

        weightBar.getChildren().addAll(weightBarTitle, lblTotalWeight, lblWeightStatus, weightSpacer, btnPublish);
        generatedSchemaBox.getChildren().add(weightBar);

        root.getChildren().add(generatedSchemaBox);

        // Pre-load default scenario for immediate view
        loadMonsoonScenario();
    }

    private void loadMonsoonScenario() {
        txtTitle.setText("NH-48 Monsoon Hill Corridor Repair & Anti-Waterlogging Drainage");
        comboCategory.getSelectionModel().select("Civil & Road Construction");
        txtDescription.setText("Repair 2.5 km of state highway sector in a high-rainfall area with chronic monsoon waterlogging. The road carries intense heavy vehicle traffic. Work must finish within 60 days before peak monsoon. Pavement and slope drainage are critical.");
        txtBudget.setText("1850000");
        txtDays.setText("60");
    }

    private void loadSolarScenario() {
        txtTitle.setText("District Memorial Hospital 200kW Rooftop Solar Captive System");
        comboCategory.getSelectionModel().select("Solar & Renewable Energy");
        txtDescription.setText("Turnkey installation of a 200 kWp rooftop solar photovoltaic system for the hospital campus. Must integrate Tier-1 high efficiency solar PV modules, string inverters with net-metering grid synchronization, and electrical safety certifications. Complete within 90 days.");
        txtBudget.setText("9500000");
        txtDays.setText("90");
    }

    private void loadPaintingScenario() {
        txtTitle.setText("Civil Secretariat Administrative Complex External Repainting");
        comboCategory.getSelectionModel().select("Building Maintenance & Painting");
        txtDescription.setText("Routine exterior repainting and crack sealing for 4 institutional multi-storey blocks. Work involves scaffolding at heights, weather-proof elastomeric coating, and high cost economy. Must finish in 45 days.");
        txtBudget.setText("650000");
        txtDays.setText("45");
    }

    private void handleGenerateAI(Button btnGenerate) {
        String originalText = btnGenerate.getText();
        btnGenerate.setText("⏳ AI Agent Analyzing Prompt & Generating Schema...");
        btnGenerate.setDisable(true);

        // Small realistic visual pause to simulate LLM thinking
        PauseTransition pause = new PauseTransition(Duration.millis(600));
        pause.setOnFinished(ev -> {
            try {
                double budget = Double.parseDouble(txtBudget.getText().replaceAll("[^0-9.]", ""));
                int days = Integer.parseInt(txtDays.getText().replaceAll("[^0-9]", ""));

                currentCriteria = aiService.generateEvaluationSchema(
                        txtDescription.getText(),
                        comboCategory.getValue(),
                        budget,
                        days
                );

                renderGeneratedCriteria();
                generatedSchemaBox.setVisible(true);
                generatedSchemaBox.setManaged(true);
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Input Error", "Please verify budget and timeline numbers: " + ex.getMessage());
            } finally {
                btnGenerate.setText(originalText);
                btnGenerate.setDisable(false);
            }
        });
        pause.play();
    }

    private void renderGeneratedCriteria() {
        // Remove previous criterion cards (keep index 0 which is weightBar)
        while (generatedSchemaBox.getChildren().size() > 1) {
            generatedSchemaBox.getChildren().remove(1);
        }

        weightSliders.clear();
        weightLabels.clear();

        for (int i = 0; i < currentCriteria.size(); i++) {
            EvaluationCriterion c = currentCriteria.get(i);
            VBox card = new VBox(12);
            card.getStyleClass().add("card-criterion");

            // Header Row: Criterion Name, AI Rationale, Weight Slider
            HBox topRow = new HBox(12);
            topRow.setAlignment(Pos.CENTER_LEFT);

            Label badge = new Label(String.format("Criterion %d", i + 1));
            badge.getStyleClass().add("badge-primary");

            Label name = new Label(c.getName());
            name.getStyleClass().add("criterion-header-title");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Label lblWeight = new Label(String.format("%.0f%%", c.getWeight()));
            lblWeight.setStyle("-fx-text-fill: #38bdf8; -fx-font-weight: 800; -fx-font-size: 14px;");
            weightLabels.put(c.getCriterionKey(), lblWeight);

            Slider slider = new Slider(5, 60, c.getWeight());
            slider.setPrefWidth(140);
            slider.setBlockIncrement(5);
            slider.valueProperty().addListener((obs, oldVal, newVal) -> {
                double rounded = Math.round(newVal.doubleValue() / 5.0) * 5.0;
                c.setWeight(rounded);
                lblWeight.setText(String.format("%.0f%%", rounded));
                updateTotalWeightStatus();
            });
            weightSliders.put(c.getCriterionKey(), slider);

            topRow.getChildren().addAll(badge, name, spacer, new Label("Adjust Weight:"), slider, lblWeight);
            card.getChildren().add(topRow);

            // AI Rationale box
            if (c.getRationale() != null) {
                HBox ratBox = new HBox(8);
                ratBox.setAlignment(Pos.CENTER_LEFT);
                ratBox.setStyle("-fx-background-color: rgba(6, 182, 212, 0.1); -fx-padding: 8 12; -fx-background-radius: 6px;");

                Label aiBadge = new Label("🤖 AI RATIONALE");
                aiBadge.getStyleClass().add("badge-ai");

                Label ratText = new Label(c.getRationale());
                ratText.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 12px;");
                ratText.setWrapText(true);

                ratBox.getChildren().addAll(aiBadge, ratText);
                card.getChildren().add(ratBox);
            }

            // Dynamic Fields Generated
            VBox fieldsBox = new VBox(8);
            Label fieldsHeader = new Label("DYNAMIC BIDDER FIELDS GENERATED FOR THIS CRITERION:");
            fieldsHeader.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px; -fx-font-weight: bold;");
            fieldsBox.getChildren().add(fieldsHeader);

            if (c.getFields() != null) {
                for (FormFieldDefinition f : c.getFields()) {
                    HBox fRow = new HBox(10);
                    fRow.setAlignment(Pos.CENTER_LEFT);
                    fRow.setStyle("-fx-background-color: #0f172a; -fx-padding: 6 10; -fx-background-radius: 6px;");

                    Label typeBadge = new Label(f.getType());
                    typeBadge.setStyle("-fx-background-color: #334155; -fx-text-fill: #38bdf8; -fx-font-size: 10px; -fx-padding: 2 6; -fx-background-radius: 4px;");

                    Label fName = new Label(f.getLabel());
                    fName.setStyle("-fx-text-fill: #f1f5f9; -fx-font-size: 12px;");

                    fRow.getChildren().addAll(typeBadge, fName);

                    if (f.getOptions() != null && !f.getOptions().isEmpty()) {
                        Label opt = new Label("Options: [" + String.join(", ", f.getOptions()) + "]");
                        opt.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11px;");
                        fRow.getChildren().add(opt);
                    }

                    fieldsBox.getChildren().add(fRow);
                }
            }

            card.getChildren().add(fieldsBox);
            generatedSchemaBox.getChildren().add(card);
        }

        updateTotalWeightStatus();
    }

    private void updateTotalWeightStatus() {
        double total = 0.0;
        for (EvaluationCriterion c : currentCriteria) {
            total += c.getWeight();
        }

        lblTotalWeight.setText(String.format("Total: %.0f%%", total));
        if (Math.abs(total - 100.0) < 0.01) {
            lblTotalWeight.setStyle("-fx-font-size: 15px; -fx-font-weight: 900; -fx-text-fill: #10b981;");
            lblWeightStatus.setText("✔ VALID SCHEMA APPROVED");
            lblWeightStatus.getStyleClass().clear();
            lblWeightStatus.getStyleClass().add("badge-success");
            btnPublish.setDisable(false);
        } else {
            lblTotalWeight.setStyle("-fx-font-size: 15px; -fx-font-weight: 900; -fx-text-fill: #ef4444;");
            lblWeightStatus.setText("⚠️ INVALID: Weights must total 100%");
            lblWeightStatus.getStyleClass().clear();
            lblWeightStatus.getStyleClass().add("badge-warning");
            btnPublish.setDisable(true);
        }
    }

    private void handlePublishTender() {
        TenderSchemaValidator.ValidationResult val = TenderSchemaValidator.validate(currentCriteria);
        if (!val.isValid()) {
            showAlert(Alert.AlertType.ERROR, "Validation Failed", String.join("\n", val.getErrors()));
            return;
        }

        String id = "TND-" + (1000 + new Random().nextInt(9000));
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        Tender tender = new Tender(
                id,
                txtTitle.getText().trim(),
                txtDescription.getText().trim(),
                comboCategory.getValue(),
                Double.parseDouble(txtBudget.getText().replaceAll("[^0-9.]", "")),
                Integer.parseInt(txtDays.getText().replaceAll("[^0-9]", "")),
                "PUBLISHED",
                "Department Tender Committee",
                LocalDateTime.now().format(dtf)
        );

        for (EvaluationCriterion c : currentCriteria) {
            c.setTenderId(tender.getId());
        }
        tender.setCriteria(currentCriteria);

        db.saveTender(tender);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Tender Published Successfully");
        alert.setHeaderText("Tender ID: " + tender.getId() + " is now LIVE");
        alert.setContentText("The 5 criteria, dynamic response form, and weights have been officially approved and locked into the deterministic evaluation database.\n\nContractors can now submit bids against this dynamic schema.");
        alert.showAndWait();

        mainView.navigateTo("DASHBOARD");
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

