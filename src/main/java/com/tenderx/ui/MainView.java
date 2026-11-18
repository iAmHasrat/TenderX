package com.tenderx.ui;

import com.tenderx.model.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class MainView {

    private final BorderPane root = new BorderPane();
    private final StackPane contentArea = new StackPane();

    private final DashboardView dashboardView;
    private final AITenderDesignerView designerView;
    private final BidSubmissionView bidSubmissionView;
    private final EvaluationRoomView evaluationRoomView;

    private Button btnNavDashboard;
    private Button btnNavDesigner;
    private Button btnNavBids;
    private Button btnNavEval;

    private Label lblCurrentUserName;
    private Label lblCurrentUserRole;
    private ComboBox<User> comboUserRole;

    private User currentUser;

    public MainView() {
        // Default mocked user: Government Tender Issuer
        this.currentUser = new User("USR-01", "Eng. Rajesh Verma", "Tender Authority / Issuer", "National Highways Authority of India (NHAI)");

        this.dashboardView = new DashboardView(this);
        this.designerView = new AITenderDesignerView(this);
        this.bidSubmissionView = new BidSubmissionView(this);
        this.evaluationRoomView = new EvaluationRoomView(this);

        buildLayout();
        navigateTo("DASHBOARD");
    }

    public BorderPane getRoot() {
        return root;
    }

    private void buildLayout() {
        // 1. Sidebar (Left)
        VBox sidebar = new VBox(18);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(260);

        // Brand
        VBox brandBox = new VBox(2);
        Label brand = new Label("⚡ TenderX");
        brand.getStyleClass().add("brand-title");

        Label brandSub = new Label("LLM-ASSISTED PROCUREMENT");
        brandSub.getStyleClass().add("brand-subtitle");
        brandBox.getChildren().addAll(brand, brandSub);
        sidebar.getChildren().add(brandBox);

        // Nav Section
        VBox navBox = new VBox(8);
        navBox.setPadding(new Insets(10, 0, 10, 0));

        btnNavDashboard = createNavButton("📊 Dashboard Overview", "DASHBOARD");
        btnNavDesigner = createNavButton("✨ AI Tender Designer", "DESIGNER");
        btnNavBids = createNavButton("📝 Bidder Submission Portal", "BIDS");
        btnNavEval = createNavButton("⚖️ Deterministic Evaluation", "EVAL");

        navBox.getChildren().addAll(btnNavDashboard, btnNavDesigner, btnNavBids, btnNavEval);
        sidebar.getChildren().add(navBox);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        sidebar.getChildren().add(spacer);

        // Mocked Authentication / Persona Switcher Card
        VBox authCard = new VBox(8);
        authCard.setStyle("-fx-background-color: #0f172a; -fx-padding: 12; -fx-background-radius: 8px; -fx-border-color: #334155; -fx-border-radius: 8px;");

        Label authTitle = new Label("SIMULATED ROLE AUTH");
        authTitle.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 10px; -fx-font-weight: bold; -fx-letter-spacing: 0.5px;");

        comboUserRole = new ComboBox<>();
        comboUserRole.getItems().addAll(
                new User("USR-01", "Eng. Rajesh Verma", "Tender Authority / Issuer", "NHAI (Ministry of Road Transport)"),
                new User("USR-02", "Vikramaditya Rao", "General Contractor / Bidder", "Apex Infrastructure & Geotech Ltd"),
                new User("USR-03", "Dr. Shalini Mehta", "Chief Auditor / Vigilance", "Independent Procurement Oversight Board")
        );
        comboUserRole.getSelectionModel().selectFirst();
        comboUserRole.setMaxWidth(Double.MAX_VALUE);
        comboUserRole.getStyleClass().add("modern-combo");
        comboUserRole.setOnAction(e -> {
            User selected = comboUserRole.getSelectionModel().getSelectedItem();
            if (selected != null) {
                this.currentUser = selected;
                lblCurrentUserName.setText(selected.getName());
                lblCurrentUserRole.setText(selected.getRole() + " · " + selected.getOrganization());
            }
        });

        authCard.getChildren().addAll(authTitle, comboUserRole);
        sidebar.getChildren().add(authCard);

        root.setLeft(sidebar);

        // 2. Top Header (Center Top)
        HBox topHeader = new HBox(16);
        topHeader.setAlignment(Pos.CENTER_RIGHT);
        topHeader.getStyleClass().add("top-header");

        Label platformBadge = new Label("SYSTEM: ONLINE (SQLITE EMBEDDED + AI ENGINE ACTIVE)");
        platformBadge.setStyle("-fx-text-fill: #34d399; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 4 10; -fx-background-color: rgba(16, 185, 129, 0.15); -fx-background-radius: 12px;");

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);

        VBox userProfile = new VBox(2);
        userProfile.setAlignment(Pos.CENTER_RIGHT);
        lblCurrentUserName = new Label(currentUser.getName());
        lblCurrentUserName.setStyle("-fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-font-size: 13px;");

        lblCurrentUserRole = new Label(currentUser.getRole() + " · " + currentUser.getOrganization());
        lblCurrentUserRole.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px;");
        userProfile.getChildren().addAll(lblCurrentUserName, lblCurrentUserRole);

        topHeader.getChildren().addAll(platformBadge, headerSpacer, userProfile);
        root.setTop(topHeader);

        // 3. Center Content Area inside ScrollPane
        ScrollPane scrollPane = new ScrollPane(contentArea);
        scrollPane.setFitToWidth(true);
        root.setCenter(scrollPane);
    }

    private Button createNavButton(String text, String targetView) {
        Button btn = new Button(text);
        btn.getStyleClass().add("nav-button");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setOnAction(e -> navigateTo(targetView));
        return btn;
    }

    public void navigateTo(String viewName) {
        contentArea.getChildren().clear();

        // Reset nav button states
        btnNavDashboard.getStyleClass().setAll("nav-button");
        btnNavDesigner.getStyleClass().setAll("nav-button");
        btnNavBids.getStyleClass().setAll("nav-button");
        btnNavEval.getStyleClass().setAll("nav-button");

        if ("DASHBOARD".equalsIgnoreCase(viewName)) {
            btnNavDashboard.getStyleClass().setAll("nav-button-active");
            contentArea.getChildren().add(dashboardView.getView());
        } else if ("DESIGNER".equalsIgnoreCase(viewName)) {
            btnNavDesigner.getStyleClass().setAll("nav-button-active");
            contentArea.getChildren().add(designerView.getView());
        } else if ("BIDS".equalsIgnoreCase(viewName)) {
            btnNavBids.getStyleClass().setAll("nav-button-active");
            contentArea.getChildren().add(bidSubmissionView.getView());
        } else if ("EVAL".equalsIgnoreCase(viewName)) {
            btnNavEval.getStyleClass().setAll("nav-button-active");
            contentArea.getChildren().add(evaluationRoomView.getView());
        }
    }

    public void openBidSubmission(String tenderId) {
        navigateTo("BIDS");
        bidSubmissionView.selectTender(tenderId);
    }

    public void openEvaluationRoom(String tenderId) {
        navigateTo("EVAL");
        evaluationRoomView.selectTender(tenderId);
    }
}

