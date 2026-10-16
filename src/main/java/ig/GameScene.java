package ig;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import modele.Configuration;

/**
 * scene principale du jeu classique
 * barre haut sombre, panneau droit, barre bas
 * bouton pause
 */
public class GameScene {

    private static final int WIDTH  = 1000;
    private static final int HEIGHT = 700;

    private final Stage         stage;
    private final Configuration config;
    private final int           niveau;

    // reference au boardview pour le remettre apres pause
    private BoardView boardView;

    public GameScene(Stage stage, Configuration config, int niveau) {
        this.stage  = stage;
        this.config = config;
        this.niveau = niveau;
    }

    public Scene build() {
        Label coupsLabel  = new Label("Coups : 0");
        Label scoreLabel  = new Label("Score : 0");
        Label timerLabel  = new Label("00:00");
        Label statusLabel   = new Label(
                "Z/Q/S/D pour bouger  |  Ctrl+Z annuler  |  I indice  |  Clic pour se deplacer");
        Label progressLabel = new Label("Caisses : 0/" + config.getCaisses().size());

        // barre de progression
        javafx.scene.layout.StackPane progressBar = new javafx.scene.layout.StackPane();
        progressBar.setPrefSize(166, 18);
        progressBar.setStyle("-fx-background-color:#0f3460;-fx-background-radius:9;");
        javafx.scene.shape.Rectangle barreRemplie = new javafx.scene.shape.Rectangle(0, 18);
        barreRemplie.setFill(javafx.scene.paint.Color.web("#2ecc71"));
        barreRemplie.setArcWidth(9); barreRemplie.setArcHeight(9);
        barreRemplie.setId("barreRemplie");
        javafx.scene.control.Label barreLabel = new javafx.scene.control.Label("0/" + config.getCaisses().size());
        barreLabel.setFont(javafx.scene.text.Font.font("Arial Black", 10));
        barreLabel.setTextFill(javafx.scene.paint.Color.WHITE);
        javafx.scene.layout.StackPane.setAlignment(barreRemplie, javafx.geometry.Pos.CENTER_LEFT);
        progressBar.getChildren().addAll(barreRemplie, barreLabel);

        Button resetBtn = bouton("Reset",       "#c0392b");
        Button undoBtn  = bouton("Undo",        "#2980b9");
        Button pauseBtn = bouton("Pause",   "#e67e22");
        Button saveBtn  = bouton("Sauvegarder", "#27ae60");
        Button loadBtn  = bouton("Charger",     "#8e44ad");
        Button menuBtn  = bouton("Menu",        "#555");
        Button quitBtn  = bouton("Quitter",     "#333");

        loadBtn.setDisable(!SauvegardeManager.sauvegardeExiste(niveau));

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #1a1a2e;");

        root.setTop(buildTopBar(coupsLabel, scoreLabel, timerLabel));
        root.setRight(buildSidePanel(resetBtn, undoBtn, pauseBtn, saveBtn, loadBtn,
                menuBtn, quitBtn, timerLabel, coupsLabel, scoreLabel, progressLabel, progressBar, barreRemplie, barreLabel, config.getCaisses().size()));
        root.setBottom(buildStatusBar(statusLabel));

        boardView = new BoardView(config);
        boardView.setStyle("-fx-background-color: #16213e;");
        boardView.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        BorderPane.setAlignment(boardView, Pos.CENTER);
        root.setCenter(boardView);

        GameTimer timer = new GameTimer(timerLabel);

        final boolean[] enPause = {false};

        InputHandler handler = new InputHandler(
                config, niveau, boardView,
                resetBtn, undoBtn, quitBtn,
                coupsLabel, scoreLabel, progressLabel,
                barreRemplie, barreLabel, config.getCaisses().size(), root
        );

        handler.setOnVictory(() -> {
            timer.stop();
            Window.showVictory(niveau, config.getScore(), config.getCoups());
        });

        handler.setOnDefaite(() -> {
            timer.stop();
            Window.showDefaite(niveau);
        });

        //bouton pause 
        pauseBtn.setOnAction(e -> {
            enPause[0] = !enPause[0];
            if (enPause[0]) {
                // mettre en pause
                timer.stop();
                handler.setPause(true);
                pauseBtn.setText("  Reprendre");
                pauseBtn.setStyle(
                    "-fx-background-color:#27ae60;-fx-text-fill:white;-fx-background-radius:10;");
                // afficher overlay
                StackPane overlay = buildPauseOverlay();
                overlay.setId("pauseOverlay");
                root.setCenter(overlay);
            } else {
                // reprendre
                timer.start();
                handler.setPause(false);
                pauseBtn.setText(" Pause");
                pauseBtn.setStyle(
                    "-fx-background-color:#e67e22;-fx-text-fill:white;-fx-background-radius:10;");
                // remettre le plateau
                root.setCenter(boardView);
                boardView.requestFocus();
            }
        });

        //sauvegarde 
        saveBtn.setOnAction(e -> {
            boolean ok = SauvegardeManager.sauvegarder(config, niveau);
            statusLabel.setText(ok ? "Partie sauvegardee !" : "Erreur sauvegarde");
            statusLabel.setTextFill(ok ? Color.LIGHTGREEN : Color.SALMON);
            loadBtn.setDisable(false);
            boardView.requestFocus();
        });

        loadBtn.setOnAction(e -> {
            Configuration restored = SauvegardeManager.charger(niveau);
            if (restored != null) {
                timer.stop();
                stage.setScene(new GameScene(stage, restored, niveau).build());
            } else {
                statusLabel.setText("Aucune sauvegarde trouvee");
                statusLabel.setTextFill(Color.SALMON);
                boardView.requestFocus();
            }
        });

        menuBtn.setOnAction(e -> {
            timer.stop();
            Window.showMenu();
        });

        timer.start();
        return new Scene(root, WIDTH, HEIGHT);
    }

    /**
     * overlay de pause affiche par dessus le plateau
     */
    private StackPane buildPauseOverlay() {
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.75);");

        javafx.scene.text.Text pauseText = new javafx.scene.text.Text("PAUSE");
        pauseText.setFont(Font.font("Arial Black", 72));
        pauseText.setFill(Color.WHITE);
        pauseText.setEffect(new DropShadow(30, Color.BLACK));

        javafx.scene.text.Text hint = new javafx.scene.text.Text(
                "Cliquez sur Reprendre pour continuer");
        hint.setFont(Font.font("Arial", 18));
        hint.setFill(Color.LIGHTGRAY);

        VBox box = new VBox(16, pauseText, hint);
        box.setAlignment(Pos.CENTER);
        overlay.getChildren().add(box);

        return overlay;
    }

    // barre du haut
    private HBox buildTopBar(Label coupsLabel, Label scoreLabel, Label timerLabel) {
        HBox bar = new HBox(20);
        bar.setStyle("-fx-background-color: #0f3460;");
        bar.setPadding(new Insets(10, 20, 10, 20));
        bar.setAlignment(Pos.CENTER_LEFT);

        Label titre = new Label("SOKOBAN");
        titre.setFont(Font.font("Arial Black", 20));
        titre.setTextFill(Color.GOLD);

        Label niveauLabel = new Label("Niveau " + niveau);
        niveauLabel.setFont(Font.font("Arial", 14));
        niveauLabel.setTextFill(Color.LIGHTGRAY);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        coupsLabel.setFont(Font.font("Arial", 14));
        coupsLabel.setTextFill(Color.WHITE);
        scoreLabel.setFont(Font.font("Arial", 14));
        scoreLabel.setTextFill(Color.WHITE);
        timerLabel.setFont(Font.font("Arial Black", 14));
        timerLabel.setTextFill(Color.GOLD);

        HBox stats = new HBox(25, coupsLabel, scoreLabel, timerLabel);
        stats.setAlignment(Pos.CENTER_RIGHT);

        bar.getChildren().addAll(titre, niveauLabel, spacer, stats);
        return bar;
    }

    // panneau droit
    private VBox buildSidePanel(Button resetBtn, Button undoBtn, Button pauseBtn,
                                 Button saveBtn,  Button loadBtn,
                                 Button menuBtn,  Button quitBtn,
                                 Label timerLabel, Label coupsLabel,
                                 Label scoreLabel, Label progressLabel,
                                 javafx.scene.layout.StackPane progressBar,
                                 javafx.scene.shape.Rectangle barreRemplie,
                                 javafx.scene.control.Label barreLabel,
                                 int totalCaisses) {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(20, 12, 20, 12));
        panel.setStyle("-fx-background-color: #16213e;");
        panel.setAlignment(Pos.TOP_CENTER);
        panel.setPrefWidth(190);
        panel.setFocusTraversable(false);

        for (Button b : new Button[]{resetBtn, undoBtn, pauseBtn, saveBtn, loadBtn, menuBtn, quitBtn})
            b.setFocusTraversable(false);

        Label titreSec = new Label("CONTROLES");
        titreSec.setFont(Font.font("Arial Black", 14));
        titreSec.setTextFill(Color.LIGHTGRAY);

        timerLabel.setFont(Font.font("Arial Black", 28));
        timerLabel.setTextFill(Color.GOLD);
        coupsLabel.setFont(Font.font("Arial Black", 15));
        coupsLabel.setTextFill(Color.WHITE);
        scoreLabel.setFont(Font.font("Arial", 13));
        scoreLabel.setTextFill(Color.LIGHTGRAY);
        progressLabel.setFont(Font.font("Arial Black", 13));
        progressLabel.setTextFill(Color.WHITE);

        panel.getChildren().addAll(
                timerLabel, coupsLabel, scoreLabel, progressLabel, progressBar,
                sep(),
                titreSec, sep(),
                info("Z/Q/S/D", "Deplacer"),
                info("Ctrl+Z",  "Annuler"),
                info("I",       "Indice"),
                info("Clic",    "Aller a la case"),
                sep(),
                resetBtn, undoBtn, pauseBtn,
                sep(),
                saveBtn, loadBtn,
                sep(),
                menuBtn, quitBtn
        );
        return panel;
    }
    // barre du bas
    private HBox buildStatusBar(Label statusLabel) {
        statusLabel.setFont(Font.font("Arial", 13));
        statusLabel.setTextFill(Color.LIGHTGRAY);
        statusLabel.setMaxWidth(Double.MAX_VALUE);
        HBox bar = new HBox(statusLabel);
        bar.setPadding(new Insets(8, 20, 8, 20));
        bar.setStyle("-fx-background-color: #0f3460;");
        bar.setAlignment(Pos.CENTER_LEFT);
        return bar;
    }

    private Button bouton(String text, String couleur) {
        Button btn = new Button(text);
        btn.setPrefWidth(166); btn.setPrefHeight(36);
        btn.setFont(Font.font("Arial Black", 12));
        String n = "-fx-background-color:" + couleur + ";-fx-text-fill:white;-fx-background-radius:10;";
        String h = "-fx-background-color:derive(" + couleur + ",25%);-fx-text-fill:white;-fx-background-radius:10;";
        btn.setStyle(n);
        btn.setOnMouseEntered(e -> btn.setStyle(h));
        btn.setOnMouseExited(e  -> btn.setStyle(n));
        return btn;
    }

    private Label info(String touche, String action) {
        Label l = new Label(touche + " \u2192 " + action);
        l.setTextFill(Color.LIGHTGRAY);
        l.setFont(Font.font("Arial", 12));
        l.setWrapText(true);
        return l;
    }

    private Region sep() {
        Region r = new Region();
        r.setPrefHeight(1); r.setMaxWidth(Double.MAX_VALUE);
        r.setStyle("-fx-background-color:#0f3460;");
        VBox.setMargin(r, new Insets(4, 0, 4, 0));
        return r;
    }
}
