package com.tenderx.ui;

import com.tenderx.db.DatabaseManager;
import com.tenderx.model.Bid;
import com.tenderx.model.Tender;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;

public class DashboardView {

    private final DatabaseManager db = DatabaseManager.getInstance();
    private final MainView mainView;
    private final VBox root = new VBox(24);

    public DashboardView(MainView mainView) {
        this.mainView = mainView;
        buildUI();
    }

    public VBox getView() {
        refreshData();
        return root;
    }

    private void buildUI() {
        root.setPadding(new Insets(24));
        refreshData();
    }

    public void refreshData() {
        root.getChildren().clear();

        List<Tender> tenders = db.getAllTenders();
        int totalBids = 0;
        int activeTenders = 0;
        int awardedTenders = 0;

        for (Tender t : tenders) {
            List<Bid> bids = db.getBidsForTender(t.getId());
            totalBids += bids.size();
            if ("PUBLISHED".equalsIgnoreCase(t.getStatus())) activeTenders++;
            if ("AWARDED".equalsIgnoreCase(t.getStatus())) awardedTenders++;
        }

        // 1. Header Banner
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        VBox titleBox = new VBox(4);
        Label title = new Label("Tender Management & AI Evaluation Dashboard");
        title.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 20px; -fx-font-weight: 800;");
        Label subtitle = new Label("Deterministic Java procurement scoring backed by LLM-assisted criteria generation");
        subtitle.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 13px;");
        titleBox.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnNewTender = new Button("+ Design New Tender with AI");
        btnNewTender.getStyleClass().add("btn-ai");
        btnNewTender.setOnAction(e -> mainView.navigateTo("DESIGNER"));

        header.getChildren().addAll(titleBox, spacer, btnNewTender);
        root.getChildren().add(header);

        // 2. Metrics Cards Row
        HBox statsRow = new HBox(16);
        statsRow.getChildren().addAll(
                createStatCard("ACTIVE TENDERS", String.valueOf(activeTenders), "Open for contractor bidding", "#38bdf8"),
                createStatCard("TOTAL BIDS RECEIVED", String.valueOf(totalBids), "Across all civil & public works", "#818cf8"),
                createStatCard("AWARDED CONTRACTS", String.valueOf(awardedTenders), "Approved & signed tenders", "#34d399"),
                createStatCard("SCORING ENGINE", "Deterministic", "100% Auditable & Explainable", "#f59e0b")
        );
        root.getChildren().add(statsRow);

        // 3. Tenders List Table Card
        VBox tableCard = new VBox(14);
        tableCard.getStyleClass().add("card");

        HBox tableHeader = new HBox(10);
        tableHeader.setAlignment(Pos.CENTER_LEFT);
        Label tableTitle = new Label("Recent Tenders & Bidding Status");
        tableTitle.setStyle("-fx-text-fill: #f8fafc; -fx-font-size: 16px; -fx-font-weight: bold;");
        Region tableSpacer = new Region();
        HBox.setHgrow(tableSpacer, Priority.ALWAYS);

        Button btnRefresh = new Button("Refresh Data");
        btnRefresh.getStyleClass().add("btn-secondary");
        btnRefresh.setOnAction(e -> refreshData());

        tableHeader.getChildren().addAll(tableTitle, tableSpacer, btnRefresh);
        tableCard.getChildren().add(tableHeader);

        VBox tenderListContainer = new VBox(10);
        if (tenders.isEmpty()) {
            Label empty = new Label("No tenders found. Click '+ Design New Tender with AI' to create one.");
            empty.setStyle("-fx-text-fill: #94a3b8; -fx-padding: 20px;");
            tenderListContainer.getChildren().add(empty);
        } else {
            for (Tender t : tenders) {
                tenderListContainer.getChildren().add(createTenderRow(t));
            }
        }

        ScrollPane scroll = new ScrollPane(tenderListContainer);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(420);
        tableCard.getChildren().add(scroll);

        root.getChildren().add(tableCard);
    }

    private Node createStatCard(String label, String value, String sub, String accentColor) {
        VBox card = new VBox(6);
        card.getStyleClass().add("card");
        HBox.setHgrow(card, Priority.ALWAYS);

        Label lbl = new Label(label);
        lbl.getStyleClass().add("stat-label");

        Label val = new Label(value);
        val.getStyleClass().add("stat-number");
        val.setStyle("-fx-text-fill: " + accentColor + ";");

        Label s = new Label(sub);
        s.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11px;");

        card.getChildren().addAll(lbl, val, s);
        return card;
    }

    private Node createTenderRow(Tender t) {
        HBox row = new HBox(16);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: #172033; -fx-background-radius: 8px; -fx-padding: 12 16; -fx-border-color: #334155; -fx-border-radius: 8px;");

        VBox info = new VBox(4);
        HBox.setHgrow(info, Priority.ALWAYS);

        HBox top = new HBox(8);
        top.setAlignment(Pos.CENTER_LEFT);
        Label idLbl = new Label(t.getId());
        idLbl.setStyle("-fx-text-fill: #38bdf8; -fx-font-weight: bold; -fx-font-size: 12px;");

        Label statusBadge = new Label(t.getStatus());
        if ("AWARDED".equalsIgnoreCase(t.getStatus())) {
            statusBadge.getStyleClass().add("badge-success");
        } else if ("PUBLISHED".equalsIgnoreCase(t.getStatus())) {
            statusBadge.getStyleClass().add("badge-primary");
        } else {
            statusBadge.getStyleClass().add("badge-warning");
        }

        Label cat = new Label(t.getCategory());
        cat.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px;");

        top.getChildren().addAll(idLbl, statusBadge, cat);

        Label title = new Label(t.getTitle());
        title.setStyle("-fx-text-fill: #ffffff; -fx-font-weight: 700; -fx-font-size: 14px;");

        Label details = new Label(String.format("Budget: ₹%,.0f  |  Max Duration: %d days  |  Criteria: %d defined",
                t.getEstimatedBudget(), t.getMaxTimelineDays(), t.getCriteria().size()));
        details.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px;");

        info.getChildren().addAll(top, title, details);

        if (t.getAwardedBidderName() != null && !t.getAwardedBidderName().isEmpty()) {
            Label awardLabel = new Label("🏆 Awarded: " + t.getAwardedBidderName());
            awardLabel.setStyle("-fx-text-fill: #34d399; -fx-font-size: 11px; -fx-font-weight: bold;");
            info.getChildren().add(awardLabel);
        }

        // Action Buttons
        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button btnSubmitBid = new Button("Submit Bid");
        btnSubmitBid.getStyleClass().add("btn-secondary");
        btnSubmitBid.setOnAction(e -> mainView.openBidSubmission(t.getId()));

        Button btnEvaluate = new Button("Evaluation Room");
        btnEvaluate.getStyleClass().add("btn-primary");
        btnEvaluate.setOnAction(e -> mainView.openEvaluationRoom(t.getId()));

        actions.getChildren().addAll(btnSubmitBid, btnEvaluate);
        row.getChildren().addAll(info, actions);

        return row;
    }
}
