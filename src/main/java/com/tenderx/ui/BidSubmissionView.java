package com.tenderx.ui;

import com.tenderx.db.DatabaseManager;
import com.tenderx.model.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class BidSubmissionView {

    private final MainView mainView;
    private final DatabaseManager db = DatabaseManager.getInstance();
    private final DynamicFormRenderer formRenderer = new DynamicFormRenderer();

    private final VBox root = new VBox(20);
    private ComboBox<TenderItem> comboTenders;
    private TextField txtBidderName;
    private TextField txtTotalQuote;
    private TextField txtProposedDays;
    private VBox dynamicFieldsContainer;

    private static class TenderItem {
        final Tender tender;
        TenderItem(Tender tender) { this.tender = tender; }
        @Override
        public String toString() {
            return String.format("[%s] %s (Cap: ₹%,.0f)", tender.getId(), tender.getTitle(), tender.getEstimatedBudget());
        }
    }

    public BidSubmissionView(MainView mainView) {
        this.mainView = mainView;
        buildUI();
    }

    public VBox getView() {
        refreshTendersList(null);
        return root;
    }

    public void selectTender(String tenderId) {
        refreshTendersList(tenderId);
    }

    private void buildUI() {
        root.setPadding(new Insets(24));

        // Header
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        VBox titleBox = new VBox(4);
        Label title = new Label("Bidder Submission Portal (Dynamic Form Engine)");
        title.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 20px; -fx-font-weight: 800;");
        Label subtitle = new Label("Step 5: Dynamic JavaFX form rendered directly from the AI-generated schema");
        subtitle.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 13px;");
        titleBox.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnBack = new Button("<- Back to Dashboard");
        btnBack.getStyleClass().add("btn-secondary");
        btnBack.setOnAction(e -> mainView.navigateTo("DASHBOARD"));

        header.getChildren().addAll(titleBox, spacer, btnBack);
        root.getChildren().add(header);

        // Tender Selection & Contractor Details Card
        VBox topCard = new VBox(14);
        topCard.getStyleClass().add("card-highlight");

        Label selectLbl = new Label("Select Target Published Tender *");
        selectLbl.getStyleClass().add("field-label");

        comboTenders = new ComboBox<>();
        comboTenders.setMaxWidth(Double.MAX_VALUE);
        comboTenders.getStyleClass().add("modern-combo");
        comboTenders.setOnAction(e -> onTenderSelected());

        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(10);

        Label lblB = new Label("Contractor / Bidding Enterprise Name *");
        lblB.getStyleClass().add("field-label");
        txtBidderName = new TextField("Apex Infrastructure & Geotech Ltd");
        txtBidderName.getStyleClass().add("modern-input");
        grid.add(lblB, 0, 0);
        grid.add(txtBidderName, 0, 1);

        Label lblQ = new Label("Total All-Inclusive Bid Quote (₹) *");
        lblQ.getStyleClass().add("field-label");
        txtTotalQuote = new TextField("2350000");
        txtTotalQuote.getStyleClass().add("modern-input");
        grid.add(lblQ, 1, 0);
        grid.add(txtTotalQuote, 1, 1);

        Label lblD = new Label("Proposed Completion Days *");
        lblD.getStyleClass().add("field-label");
        txtProposedDays = new TextField("50");
        txtProposedDays.getStyleClass().add("modern-input");
        grid.add(lblD, 2, 0);
        grid.add(txtProposedDays, 2, 1);

        topCard.getChildren().addAll(selectLbl, comboTenders, grid);
        root.getChildren().add(topCard);

        // Dynamic Form Fields Section
        VBox dynamicCard = new VBox(14);
        dynamicCard.getStyleClass().add("card");

        HBox formHeader = new HBox(12);
        formHeader.setAlignment(Pos.CENTER_LEFT);

        Label formTitle = new Label("Generated Technical & Compliance Submission Fields");
        formTitle.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 16px; -fx-font-weight: bold;");

        Region formSpacer = new Region();
        HBox.setHgrow(formSpacer, Priority.ALWAYS);

        Button btnAutoFill = new Button("🪄 Auto-Fill Realistic Bid Proposal");
        btnAutoFill.getStyleClass().add("btn-secondary");
        btnAutoFill.setOnAction(e -> formRenderer.autoFillRealisticData());

        formHeader.getChildren().addAll(formTitle, formSpacer, btnAutoFill);
        dynamicCard.getChildren().add(formHeader);

        dynamicFieldsContainer = new VBox(10);
        dynamicCard.getChildren().add(dynamicFieldsContainer);

        // Submit Button Row
        HBox submitBox = new HBox(14);
        submitBox.setAlignment(Pos.CENTER_RIGHT);
        submitBox.setPadding(new Insets(10, 0, 0, 0));

        Button btnSubmit = new Button("🚀 Submit Official Sealed Bid");
        btnSubmit.getStyleClass().add("btn-primary");
        btnSubmit.setOnAction(e -> handleSubmitBid());

        submitBox.getChildren().add(btnSubmit);
        dynamicCard.getChildren().add(submitBox);

        root.getChildren().add(dynamicCard);
    }

    private void refreshTendersList(String selectId) {
        comboTenders.getItems().clear();
        List<Tender> list = db.getAllTenders();
        TenderItem toSelect = null;

        for (Tender t : list) {
            TenderItem item = new TenderItem(t);
            comboTenders.getItems().add(item);
            if (selectId != null && t.getId().equalsIgnoreCase(selectId)) {
                toSelect = item;
            }
        }

        if (toSelect != null) {
            comboTenders.getSelectionModel().select(toSelect);
        } else if (!comboTenders.getItems().isEmpty()) {
            comboTenders.getSelectionModel().selectFirst();
        }
        onTenderSelected();
    }

    private void onTenderSelected() {
        TenderItem item = comboTenders.getSelectionModel().getSelectedItem();
        if (item == null) return;

        Tender t = item.tender;
        // Dynamically render the form controls
        VBox rendered = formRenderer.renderCriteriaForms(t.getCriteria());
        dynamicFieldsContainer.getChildren().clear();
        dynamicFieldsContainer.getChildren().add(rendered);
    }

    private void handleSubmitBid() {
        TenderItem item = comboTenders.getSelectionModel().getSelectedItem();
        if (item == null) return;

        String bidderName = txtBidderName.getText().trim();
        if (bidderName.isEmpty()) {
            showAlert("Validation Error", "Please provide contractor / enterprise name.");
            return;
        }

        double quote;
        int days;
        try {
            quote = Double.parseDouble(txtTotalQuote.getText().replaceAll("[^0-9.]", ""));
            days = Integer.parseInt(txtProposedDays.getText().replaceAll("[^0-9]", ""));
        } catch (Exception ex) {
            showAlert("Input Error", "Please verify bid quote and completion days.");
            return;
        }

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String bidId = "BID-" + (100 + new Random().nextInt(900));

        Bid bid = new Bid(bidId, item.tender.getId(), "BDR-" + (10 + new Random().nextInt(90)), bidderName, quote, days, LocalDateTime.now().format(dtf));

        Map<String, String> responses = formRenderer.getFormValues();
        Map<String, FormFieldDefinition> defs = formRenderer.getFieldDefinitions();

        for (Map.Entry<String, String> entry : responses.entrySet()) {
            FormFieldDefinition def = defs.get(entry.getKey());
            String label = def != null ? def.getLabel() : entry.getKey();
            bid.addResponse(new BidResponse(UUID.randomUUID().toString(), bid.getId(), entry.getKey(), label, entry.getValue()));
        }

        db.saveBid(bid);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Bid Successfully Submitted");
        alert.setHeaderText("Bid Receipt ID: " + bid.getId());
        alert.setContentText(String.format("Bidder: %s\nQuoted: ₹%,.0f\nTimeline: %d days\nTarget Tender: %s\n\nYour response has been sealed and logged into the SQLite database. Open the Evaluation Room to run the deterministic evaluation.",
                bidderName, quote, days, item.tender.getTitle()));
        alert.showAndWait();

        mainView.openEvaluationRoom(item.tender.getId());
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

