package com.tenderx.ui;

import com.tenderx.db.DatabaseManager;
import com.tenderx.evaluation.CriterionScore;
import com.tenderx.evaluation.EvaluationResult;
import com.tenderx.evaluation.ScoringEngine;
import com.tenderx.model.Bid;
import com.tenderx.model.Tender;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;

public class EvaluationRoomView {

    private final MainView mainView;
    private final DatabaseManager db = DatabaseManager.getInstance();
    private final ScoringEngine scoringEngine = new ScoringEngine();

    private final VBox root = new VBox(20);
    private ComboBox<TenderItem> comboTenders;
    private VBox evaluationResultsBox;
    private VBox winnerCardContainer;

    private static class TenderItem {
        final Tender tender;
        TenderItem(Tender tender) { this.tender = tender; }
        @Override
        public String toString() {
            return String.format("[%s] %s (%s)", tender.getId(), tender.getTitle(), tender.getStatus());
        }
    }

    public EvaluationRoomView(MainView mainView) {
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
        Label title = new Label("Deterministic Java Evaluation & Awarding Room");
        title.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 20px; -fx-font-weight: 800;");
        Label subtitle = new Label("Step 6: Rule-based, 100% reproducible scoring engine applying approved criteria weights");
        subtitle.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 13px;");
        titleBox.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnBack = new Button("<- Back to Dashboard");
        btnBack.getStyleClass().add("btn-secondary");
        btnBack.setOnAction(e -> mainView.navigateTo("DASHBOARD"));

        header.getChildren().addAll(titleBox, spacer, btnBack);
        root.getChildren().add(header);

        // Control Card: Select Tender & Run Evaluation
        VBox ctrlCard = new VBox(12);
        ctrlCard.getStyleClass().add("card-highlight");

        Label selectLbl = new Label("Select Tender to Evaluate *");
        selectLbl.getStyleClass().add("field-label");

        HBox selectRow = new HBox(12);
        selectRow.setAlignment(Pos.CENTER_LEFT);

        comboTenders = new ComboBox<>();
        comboTenders.setMaxWidth(Double.MAX_VALUE);
        comboTenders.getStyleClass().add("modern-combo");
        HBox.setHgrow(comboTenders, Priority.ALWAYS);
        comboTenders.setOnAction(e -> handleTenderSelected());

        Button btnRun = new Button("⚙️ Run Deterministic Java Evaluation");
        btnRun.getStyleClass().add("btn-ai");
        btnRun.setOnAction(e -> runEvaluation());

        selectRow.getChildren().addAll(comboTenders, btnRun);
        ctrlCard.getChildren().addAll(selectLbl, selectRow);
        root.getChildren().add(ctrlCard);

        // Winner Card Container
        winnerCardContainer = new VBox();
        root.getChildren().add(winnerCardContainer);

        // Results Container
        evaluationResultsBox = new VBox(16);
        root.getChildren().add(evaluationResultsBox);
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
        handleTenderSelected();
    }

    private void handleTenderSelected() {
        winnerCardContainer.getChildren().clear();
        evaluationResultsBox.getChildren().clear();
        TenderItem item = comboTenders.getSelectionModel().getSelectedItem();
        if (item == null) return;

        List<Bid> bids = db.getBidsForTender(item.tender.getId());
        if (bids.isEmpty()) {
            Label noBids = new Label("No bids have been submitted yet for this tender. Switch to Bidder Submission Portal to submit proposals.");
            noBids.setStyle("-fx-text-fill: #94a3b8; -fx-padding: 20px;");
            evaluationResultsBox.getChildren().add(noBids);
        } else {
            // Auto run evaluation on selection for seamless demo
            runEvaluation();
        }
    }

    private void runEvaluation() {
        TenderItem item = comboTenders.getSelectionModel().getSelectedItem();
        if (item == null) return;

        Tender tender = db.getTender(item.tender.getId());
        List<Bid> bids = db.getBidsForTender(tender.getId());

        if (bids.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("No Bids Found");
            alert.setHeaderText(null);
            alert.setContentText("No bids submitted yet for this tender. Please submit at least one bid.");
            alert.showAndWait();
            return;
        }

        List<EvaluationResult> results = scoringEngine.evaluateTenderBids(tender, bids);

        // Update database with calculated scores
        for (EvaluationResult res : results) {
            db.saveBidScore(res.getBidId(), res.getTotalScore(), res.getStatusNote());
        }

        renderEvaluationUI(tender, results);
    }

    private void renderEvaluationUI(Tender tender, List<EvaluationResult> results) {
        winnerCardContainer.getChildren().clear();
        evaluationResultsBox.getChildren().clear();

        if (results.isEmpty()) return;

        EvaluationResult winner = results.get(0);

        // 1. Highlighted Winner Card
        VBox winnerCard = new VBox(12);
        winnerCard.getStyleClass().add("card-winner");

        HBox top = new HBox(12);
        top.setAlignment(Pos.CENTER_LEFT);

        Label trophy = new Label("🥇 RANK 1 RECOMMENDED CONTRACTOR");
        trophy.getStyleClass().add("badge-success");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label scoreBadge = new Label(String.format("FINAL SCORE: %.2f / 100", winner.getTotalScore()));
        scoreBadge.setStyle("-fx-background-color: #10b981; -fx-text-fill: #ffffff; -fx-font-weight: 900; -fx-padding: 4 12; -fx-background-radius: 6px;");

        top.getChildren().addAll(trophy, spacer, scoreBadge);

        Label winnerName = new Label(winner.getBidderName());
        winnerName.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 18px; -fx-font-weight: 800;");

        Label stats = new Label(String.format("Quoted Bid: ₹%,.0f   |   Proposed Timeline: %d Days   |   Status: %s",
                winner.getTotalQuote(), winner.getProposedDays(), winner.getStatusNote()));
        stats.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 13px;");

        HBox actionRow = new HBox(14);
        actionRow.setAlignment(Pos.CENTER_LEFT);

        Button btnAward = new Button("🏛️ Issuer Approval: Award Official Contract");
        btnAward.getStyleClass().add("btn-success");
        if ("AWARDED".equalsIgnoreCase(tender.getStatus())) {
            btnAward.setText("✔ Contract Officially Awarded & Sealed");
            btnAward.setDisable(true);
        } else {
            btnAward.setOnAction(e -> handleAwardContract(tender, winner));
        }

        Label auditNote = new Label("Auditable decision: Scores calculated via published weights & inverse cost ratio.");
        auditNote.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px; -fx-font-style: italic;");

        actionRow.getChildren().addAll(btnAward, auditNote);

        winnerCard.getChildren().addAll(top, winnerName, stats, actionRow);
        winnerCardContainer.getChildren().add(winnerCard);

        // 2. Comprehensive Comparative Leaderboard
        VBox tableCard = new VBox(12);
        tableCard.getStyleClass().add("card");

        Label tableTitle = new Label("Full Procurement Leaderboard & Criterion Points Breakdown");
        tableTitle.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 16px; -fx-font-weight: bold;");
        tableCard.getChildren().add(tableTitle);

        VBox rowsBox = new VBox(8);
        for (EvaluationResult res : results) {
            rowsBox.getChildren().add(createResultRow(res));
        }
        tableCard.getChildren().add(rowsBox);
        evaluationResultsBox.getChildren().add(tableCard);

        // 3. Explainability & Audit Trail Box
        VBox explainCard = new VBox(12);
        explainCard.getStyleClass().add("card");

        Label explainTitle = new Label("🔍 Deterministic Scoring Audit Trace & Why This Bidder Won");
        explainTitle.setStyle("-fx-text-fill: #38bdf8; -fx-font-size: 15px; -fx-font-weight: bold;");

        VBox traces = new VBox(8);
        for (CriterionScore cs : winner.getCriterionScores()) {
            HBox traceRow = new HBox(8);
            traceRow.setAlignment(Pos.CENTER_LEFT);
            traceRow.setStyle("-fx-background-color: #0f172a; -fx-padding: 8 12; -fx-background-radius: 6px;");

            Label critLbl = new Label(String.format("[%s - Weight %.0f%%]:", cs.getCriterionKey(), cs.getWeight()));
            critLbl.setStyle("-fx-text-fill: #818cf8; -fx-font-weight: bold; -fx-font-size: 12px;");

            Label ptsLbl = new Label(String.format("%.2f pts", cs.getWeightedScore()));
            ptsLbl.setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");

            Label expLbl = new Label(cs.getExplanation());
            expLbl.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 12px;");
            expLbl.setWrapText(true);

            traceRow.getChildren().addAll(critLbl, ptsLbl, expLbl);
            traces.getChildren().add(traceRow);
        }

        explainCard.getChildren().addAll(explainTitle, traces);
        evaluationResultsBox.getChildren().add(explainCard);
    }

    private Node createResultRow(EvaluationResult res) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: #172033; -fx-padding: 10 14; -fx-background-radius: 8px; -fx-border-color: #334155; -fx-border-radius: 8px;");

        Label rankLbl = new Label(res.getRank() == 1 ? "🥇 #1" : (res.getRank() == 2 ? "🥈 #2" : "🥉 #" + res.getRank()));
        rankLbl.setStyle("-fx-font-size: 14px; -fx-font-weight: 900; -fx-text-fill: " + (res.getRank() == 1 ? "#10b981" : "#cbd5e1") + "; -fx-min-width: 50px;");

        VBox nameBox = new VBox(2);
        HBox.setHgrow(nameBox, Priority.ALWAYS);
        Label name = new Label(res.getBidderName());
        name.setStyle("-fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-font-size: 13px;");
        Label sub = new Label(String.format("Quote: ₹%,.0f  |  Days: %d  |  %s", res.getTotalQuote(), res.getProposedDays(), res.getStatusNote()));
        sub.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px;");
        nameBox.getChildren().addAll(name, sub);

        // Individual criterion points
        HBox ptsBox = new HBox(8);
        ptsBox.setAlignment(Pos.CENTER_RIGHT);
        for (CriterionScore cs : res.getCriterionScores()) {
            VBox p = new VBox(2);
            p.setAlignment(Pos.CENTER);
            Label pKey = new Label(cs.getCriterionKey().substring(0, Math.min(4, cs.getCriterionKey().length())));
            pKey.setStyle("-fx-text-fill: #64748b; -fx-font-size: 10px;");
            Label pVal = new Label(String.format("%.1f", cs.getWeightedScore()));
            pVal.setStyle("-fx-text-fill: #38bdf8; -fx-font-size: 12px; -fx-font-weight: bold;");
            p.getChildren().addAll(pKey, pVal);
            ptsBox.getChildren().add(p);
        }

        Label totalBadge = new Label(String.format("%.2f / 100", res.getTotalScore()));
        totalBadge.setStyle("-fx-background-color: " + (res.getRank() == 1 ? "rgba(16, 185, 129, 0.2)" : "rgba(51, 65, 85, 0.5)") +
                "; -fx-text-fill: " + (res.getRank() == 1 ? "#34d399" : "#f8fafc") + "; -fx-font-size: 13px; -fx-font-weight: 800; -fx-padding: 6 12; -fx-background-radius: 6px;");

        row.getChildren().addAll(rankLbl, nameBox, ptsBox, totalBadge);
        return row;
    }

    private void handleAwardContract(Tender tender, EvaluationResult winner) {
        db.updateTenderAward(tender.getId(), winner.getBidId(), winner.getBidderName());

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Contract Officially Awarded");
        alert.setHeaderText("Tender Awarded to " + winner.getBidderName());
        alert.setContentText(String.format("Contract for tender '%s' has been formally awarded.\n\nWinning Quote: ₹%,.0f\nTotal Score: %.2f pts\nAudit Log Reference: EVAL-%s\n\nThe tender is now closed and sealed.",
                tender.getTitle(), winner.getTotalQuote(), winner.getTotalScore(), winner.getBidId()));
        alert.showAndWait();

        refreshTendersList(tender.getId());
    }
}
