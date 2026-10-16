package ig;

import chemins.CheminRecursif;
import resolutionautomatique.SolverRec;
import resolutionautomatique.IndiceSolverRec;
import persistance.SauvegardePartieRec;
import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;
import modele.*;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * scene de jeu pour le mode recursif parabox
 * deux piles paralleles configs et caisses visitees
 * timer score et coups affiches comme la version classique
 * sauvegarde et chargement via SauvegardePartieRec
 */
public class GameSceneRec {

    private static final int WIDTH  = 1000;
    private static final int HEIGHT = 650;

    private final Stage      stage;
    private final PlateauRec plateauRec;
    private final String     cheminNiveau;

    private final Deque<ConfigurationRec> pile        = new ArrayDeque<>();
    private final ArrayList<CaisseMonde>  pileCaisses = new ArrayList<>();

    private ConfigurationRec configPrincipaleRef;

    private BoardViewRec boardView;
    private BorderPane   rootPane;
    private Label        breadcrumbLabel;
    private Label        statusLabel;
    private Label        coupsLabel;
    private Label        scoreLabel;
    private Label        timerLabel;
    private GameTimer    timer;
    private Button       loadBtn;

    private boolean busy    = false;
    private boolean enPause = false;
    private javafx.scene.control.Label indiceLabel = null;

    public GameSceneRec(Stage stage, PlateauRec plateauRec, String cheminNiveau) {
        this.stage        = stage;
        this.plateauRec   = plateauRec;
        this.cheminNiveau = cheminNiveau;
        initialiserPile();
    }

    private void initialiserPile() {
        pile.clear();
        pileCaisses.clear();
        char nom    = plateauRec.getMondePrincipal();
        Monde monde = plateauRec.getMonde(nom);
        configPrincipaleRef = new ConfigurationRec(monde, plateauRec);
        pile.push(configPrincipaleRef);
        pileCaisses.add(null);
    }

    private String cheminSauvegarde() {
        // sauvegardes dans sauvegardes/rec/ avec le nom du fichier niveau
        String nom = new java.io.File(cheminNiveau).getName().replace(".txt", "");
        return "sauvegardes/rec/" + nom + ".sav";
    }

    public Scene build() {
        coupsLabel = new Label("Coups : 0");
        scoreLabel = new Label("Score : 0");
        timerLabel = new Label("00:00");

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #1a1a2e;");
        root.setFocusTraversable(true);
        rootPane = root;

        root.setTop(buildTopBar());
        root.setRight(buildSidePanel());
        root.setBottom(buildStatusBar());

        boardView = new BoardViewRec(configActuelle(), caisseActuelle(), configParente());
        boardView.setStyle("-fx-background-color: #16213e;");
        boardView.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        BorderPane.setAlignment(boardView, Pos.CENTER);
        root.setCenter(boardView);

        Scene scene = new Scene(root, WIDTH, HEIGHT);

        scene.setOnKeyPressed(e -> {
            if (busy) return;
            e.consume();
            switch (e.getCode()) {
                case Z, UP              -> deplacer(Direction.HAUT);
                case S, DOWN            -> deplacer(Direction.BAS);
                case Q, LEFT            -> deplacer(Direction.GAUCHE);
                case D, RIGHT           -> deplacer(Direction.DROITE);
                case ENTER              -> entrerDansMonde();
                case ESCAPE, BACK_SPACE -> sortirDuMonde();
                case R                  -> reset();
                case I                  -> afficherIndice();
                default -> {}
            }
        });

        boardView.setOnMouseClicked(e -> {
            if (busy) return;

            double w = boardView.getWidth();
            double h = boardView.getHeight();
            if (w <= 0 || h <= 0) return;

            double cs;
            double startX;
            double startY;
            double MARGE_EXT = 0.25;

            if (caisseActuelle() != null && configParente() != null) {
                cs = Math.floor(Math.min(
                        w / (configActuelle().getX() + 2 * MARGE_EXT),
                        h / (configActuelle().getY() + 2 * MARGE_EXT)));
                double margePixels = cs * MARGE_EXT;
                double totalW = configActuelle().getX() * cs + 2 * margePixels;
                double totalH = configActuelle().getY() * cs + 2 * margePixels;
                startX = Math.floor((w - totalW) / 2) + margePixels;
                startY = Math.floor((h - totalH) / 2) + margePixels;
            } else {
                cs = Math.floor(Math.min(
                        w / configActuelle().getX(),
                        h / configActuelle().getY()));
                double totalW = configActuelle().getX() * cs;
                double totalH = configActuelle().getY() * cs;
                startX = Math.floor((w - totalW) / 2);
                startY = Math.floor((h - totalH) / 2);
            }

            if (cs <= 0) return;

            int cx = (int)((e.getX() - startX) / cs);
            int cy = (int)((e.getY() - startY) / cs);

            if (cx < 0 || cx >= configActuelle().getX()
             || cy < 0 || cy >= configActuelle().getY()) return;

            Position cible  = new Position(cx, cy);
            Position joueur = configActuelle().getJoueur().getPosition();
            if (cible.equals(joueur)) return;
            if (!configActuelle().estVide(cible)) return;

            CheminRecursif bfs = new CheminRecursif(configActuelle());
            List<Position> chemin = bfs.trouverChemin(joueur, cible);
            if (chemin.isEmpty()) return;

            animerCheminRec(chemin, 1);
        });

        root.setOnMouseClicked(e -> root.requestFocus());
        root.requestFocus();

        timer = new GameTimer(timerLabel);
        timer.start();

        return scene;
    }


    /**
     * construit la scene depuis une configuration restauree
     * utilise apres chargement d une sauvegarde
     */
    public Scene buildFromSave(ConfigurationRec restored) {
        // remplacer configPrincipaleRef par la config restauree
        pile.clear();
        pileCaisses.clear();
        configPrincipaleRef = restored;
        pile.push(configPrincipaleRef);
        pileCaisses.add(null);
        return build();
    }

    private void animerCheminRec(List<Position> chemin, int index) {
        if (index >= chemin.size()) {
            busy = false;
            return;
        }
        busy = true;

        Position actuelle = chemin.get(index - 1);
        Position suivante = chemin.get(index);
        int dx = suivante.getX() - actuelle.getX();
        int dy = suivante.getY() - actuelle.getY();
        Direction dir =
                dx ==  1 ? Direction.DROITE :
                dx == -1 ? Direction.GAUCHE :
                dy ==  1 ? Direction.BAS    : Direction.HAUT;

        deplacer(dir);

        PauseTransition pause = new PauseTransition(Duration.millis(100));
        pause.setOnFinished(ev -> animerCheminRec(chemin, index + 1));
        pause.play();
    }
//barre du haut
    private HBox buildTopBar() {
        HBox bar = new HBox(20);
        bar.setStyle("-fx-background-color: #0f3460;");
        bar.setPadding(new Insets(10, 20, 10, 20));
        bar.setAlignment(Pos.CENTER_LEFT);

        Label titre = new Label("PARABOX");
        titre.setFont(Font.font("Arial Black", 20));
        titre.setTextFill(Color.GOLD);

        breadcrumbLabel = new Label("Monde principal");
        breadcrumbLabel.setFont(Font.font("Arial", 14));
        breadcrumbLabel.setTextFill(Color.LIGHTGRAY);

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

        bar.getChildren().addAll(titre, breadcrumbLabel, spacer, stats);
        return bar;
    }
//panneau droit
    private ScrollPane buildSidePanel() {
        VBox panel = new VBox(6);
        panel.setPadding(new Insets(10, 10, 10, 10));
        panel.setStyle("-fx-background-color: #16213e;");
        panel.setAlignment(Pos.TOP_CENTER);
        panel.setPrefWidth(190);
        panel.setFocusTraversable(false);

        timerLabel.setFont(Font.font("Arial Black", 22));
        timerLabel.setTextFill(Color.GOLD);
        coupsLabel.setFont(Font.font("Arial Black", 15));
        coupsLabel.setTextFill(Color.WHITE);
        scoreLabel.setFont(Font.font("Arial", 13));
        scoreLabel.setTextFill(Color.LIGHTGRAY);

        Label titreSec = new Label("CONTROLES");
        titreSec.setFont(Font.font("Arial Black", 14));
        titreSec.setTextFill(Color.LIGHTGRAY);

        Button resetBtn = bouton("Reset",        "#c0392b");
        Button pauseBtn = bouton("⏸  Pause",    "#e67e22");
        Button saveBtn  = bouton("Sauvegarder",  "#27ae60");
        loadBtn         = bouton("Charger",      "#8e44ad");
        Button menuBtn  = bouton("Menu",         "#555");

        loadBtn.setDisable(!new java.io.File(cheminSauvegarde()).exists());

        for (Button b : new Button[]{resetBtn, pauseBtn, saveBtn, loadBtn, menuBtn})
            b.setFocusTraversable(false);

        resetBtn.setOnAction(e -> reset());
        menuBtn.setOnAction(e  -> { timer.stop(); Window.showMenu(); });

        pauseBtn.setOnAction(e -> {
            enPause = !enPause;
            if (enPause) {
                timer.stop();
                busy = true;
                pauseBtn.setText("▶  Reprendre");
                pauseBtn.setStyle(
                    "-fx-background-color:#27ae60;-fx-text-fill:white;-fx-background-radius:10;");
                rootPane.setCenter(buildPauseOverlay());
            } else {
                timer.start();
                busy = false;
                pauseBtn.setText("⏸  Pause");
                pauseBtn.setStyle(
                    "-fx-background-color:#e67e22;-fx-text-fill:white;-fx-background-radius:10;");
                rootPane.setCenter(boardView);
                rootPane.requestFocus();
            }
        });

        saveBtn.setOnAction(e -> {
            try {
                SauvegardePartieRec.sauvegarder(
                        configPrincipaleRef, plateauRec,
                        cheminNiveau, cheminSauvegarde());
                setStatus("Partie sauvegardee !");
                statusLabel.setTextFill(Color.LIGHTGREEN);
                loadBtn.setDisable(false);
            } catch (Exception ex) {
                setStatus("Erreur sauvegarde : " + ex.getMessage());
                statusLabel.setTextFill(Color.SALMON);
            }
            boardView.requestFocus();
        });

        loadBtn.setOnAction(e -> {
            try {
                ConfigurationRec restored = SauvegardePartieRec.charger(cheminSauvegarde());
                if (restored != null) {
                    timer.stop();
                    Window.getStage().setScene(buildFromSave(restored));
                } else {
                    setStatus("Aucune sauvegarde trouvee");
                    statusLabel.setTextFill(Color.SALMON);
                }
            } catch (Exception ex) {
                setStatus("Erreur chargement : " + ex.getMessage());
                statusLabel.setTextFill(Color.SALMON);
            }
            boardView.requestFocus();
        });

        Label lblLegende = new Label("MONDES");
        lblLegende.setFont(Font.font("Arial Black", 12));
        lblLegende.setTextFill(Color.LIGHTGRAY);

        VBox legende = new VBox(6);
        for (char c : new char[]{'A','B','C','D','E','F','G','H','I','J'}) {
            if (plateauRec.getMonde(c) == null) continue;
            HBox row = new HBox(8);
            row.setAlignment(Pos.CENTER_LEFT);
            javafx.scene.shape.Rectangle carre =
                    new javafx.scene.shape.Rectangle(16, 16,
                            BoardViewRec.getCouleurMonde(c));
            carre.setArcWidth(4); carre.setArcHeight(4);
            Label lbl = new Label("Monde " + c);
            lbl.setTextFill(Color.LIGHTGRAY);
            lbl.setFont(Font.font(12));
            row.getChildren().addAll(carre, lbl);
            legende.getChildren().add(row);
        }

        panel.getChildren().addAll(
                timerLabel, coupsLabel, scoreLabel,
                sep(),
                titreSec, sep(),
                info("Z/Q/S/D", "Deplacer"),
                info("ENTREE",  "Entrer monde"),
                info("ECHAP",   "Sortir monde"),
                info("R",       "Reset"),
                info("I",       "Indice"),
                info("Clic",    "Se deplacer"),
                sep(),
                resetBtn, pauseBtn,
                sep(),
                saveBtn, loadBtn,
                sep(),
                menuBtn,
                sep(),
                lblLegende, legende);

        // scroll si panel trop long
        ScrollPane scroll = new ScrollPane(panel);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: #16213e;");
        scroll.setPrefWidth(190);
        return scroll;
    }
//barre du bas
    private Label buildStatusBar() {
        statusLabel = new Label(
                "Z/Q/S/D : bouger   ENTREE : entrer   ECHAP : sortir   R : reset   Clic : se deplacer");
        statusLabel.setTextFill(Color.LIGHTGRAY);
        statusLabel.setFont(Font.font("Arial", 13));
        statusLabel.setPadding(new Insets(8, 20, 8, 20));
        statusLabel.setMaxWidth(Double.MAX_VALUE);
        statusLabel.setStyle("-fx-background-color: #0f3460;");
        return statusLabel;
    }
//logique jeu
    private void deplacer(Direction dir) {
        try { configActuelle().deplacerJoueur(dir); }
        catch (Exception ignored) {}
        updateLabels();
        mettreAJourVue();
        if (configPrincipaleRef.victoire()) {
            busy = false;
            timer.stop();
            Window.showVictory(-1,
                    configPrincipaleRef.getScore(),
                    configPrincipaleRef.getCoups());
            return;
        }
        if (configActuelle().estPerdu()) {
            busy = false;
            timer.stop();
            Window.showDefaite(0);
        }
    }

    private void entrerDansMonde() {
        ConfigurationRec conf = configActuelle();
        Position pj = conf.getJoueur().getPosition();
        for (Direction d : Direction.getDirections()) {
            Position v = pj.add(d);
            if (conf.estCaisse(v)) {
                CaisseMonde cm = conf.getCaisse(v);
                if (!cm.estCaisseSimple() && cm.getConfInterne() != null) {
                    pile.push(cm.getConfInterne());
                    pileCaisses.add(cm);
                    mettreAJourVue();
                    updateBreadcrumb();
                    setStatus("Monde " + cm.getMondeNom());
                    return;
                }
            }
        }
        setStatus("Aucune caisse monde adjacente !");
    }

    private void sortirDuMonde() {
        if (pile.size() <= 1) { setStatus("Deja dans le monde principal !"); return; }
        pile.pop();
        pileCaisses.remove(pileCaisses.size() - 1);
        mettreAJourVue();
        updateBreadcrumb();
        setStatus("Retour au monde precedent");
    }

    private void reset() {
        busy = false;
        timer.stop();
        initialiserPile();
        mettreAJourVue();
        updateBreadcrumb();
        updateLabels();
        timer.start();
        setStatus("Recommence !");
    }

    private void updateLabels() {
        coupsLabel.setText("Coups : " + configActuelle().getCoups());
        scoreLabel.setText("Score : " + configPrincipaleRef.getScore());
    }

    private void mettreAJourVue() {
        boardView.setConfig(configActuelle(), caisseActuelle(), configParente());
    }

    private void updateBreadcrumb() {
        if (pile.size() == 1) { breadcrumbLabel.setText("Monde principal"); return; }
        StringBuilder sb = new StringBuilder("Monde principal");
        ConfigurationRec[] arr = pile.toArray(new ConfigurationRec[0]);
        for (int i = arr.length - 2; i >= 0; i--)
            sb.append(" > ").append(arr[i].getMonde().getNom());
        breadcrumbLabel.setText(sb.toString());
    }

    private void setStatus(String msg) {
        if (statusLabel != null) {
            statusLabel.setText(msg);
            statusLabel.setTextFill(Color.LIGHTGRAY);
        }
    }

    private ConfigurationRec configActuelle() { return pile.peek(); }

    private CaisseMonde caisseActuelle() {
        if (pileCaisses.isEmpty()) return null;
        return pileCaisses.get(pileCaisses.size() - 1);
    }

    private ConfigurationRec configParente() {
        if (pile.size() <= 1) return null;
        ConfigurationRec[] arr = pile.toArray(new ConfigurationRec[0]);
        return arr[1];
    }

    private void afficherIndice() {
        new Thread(() -> {
            try {
                SolverRec solver = new SolverRec(configPrincipaleRef);
                IndiceSolverRec indiceSolver = new IndiceSolverRec(solver);
                Character indice = indiceSolver.getIndice();

                String msg;
                if (indice == null) {
                    msg = "Aucune solution trouvee !";
                } else {
                    msg = switch (indice) {
                        case 'Z' -> "Indice : allez vers le HAUT (Z)";
                        case 'S' -> "Indice : allez vers le BAS (S)";
                        case 'Q' -> "Indice : allez vers la GAUCHE (Q)";
                        case 'D' -> "Indice : allez vers la DROITE (D)";
                        case 'E' -> "Indice : entrez dans un monde (ENTREE)";
                        case 'X' -> "Indice : sortez du monde (ECHAP)";
                        default  -> "Indice inconnu";
                    };
                }
                final String finalMsg = msg;
                javafx.application.Platform.runLater(() -> afficherMessage(finalMsg));
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> afficherMessage("Calcul impossible"));
            }
        }).start();
    }

    private void afficherMessage(String msg) {
        if (indiceLabel != null) {
            boardView.getChildren().remove(indiceLabel);
            indiceLabel = null;
        }
        indiceLabel = new javafx.scene.control.Label(msg);
        indiceLabel.setStyle(
            "-fx-background-color: rgba(0,0,0,0.75);" +
            "-fx-text-fill: gold;" +
            "-fx-font-size: 15px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 10 20 10 20;" +
            "-fx-background-radius: 12;"
        );
        indiceLabel.setLayoutX(20);
        indiceLabel.setLayoutY(20);
        boardView.getChildren().add(indiceLabel);

        PauseTransition pause = new PauseTransition(Duration.seconds(3));
        pause.setOnFinished(e -> {
            boardView.getChildren().remove(indiceLabel);
            indiceLabel = null;
        });
        pause.play();
    }

    private StackPane buildPauseOverlay() {
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.75);");
        javafx.scene.text.Text pauseText = new javafx.scene.text.Text("PAUSE");
        pauseText.setFont(Font.font("Arial Black", 72));
        pauseText.setFill(Color.WHITE);
        javafx.scene.text.Text hint = new javafx.scene.text.Text(
                "Cliquez sur Reprendre pour continuer");
        hint.setFont(Font.font("Arial", 18));
        hint.setFill(Color.LIGHTGRAY);
        VBox box = new VBox(16, pauseText, hint);
        box.setAlignment(Pos.CENTER);
        overlay.getChildren().add(box);
        return overlay;
    }

    private Label info(String touche, String action) {
        Label l = new Label(touche + " \u2192 " + action);
        l.setTextFill(Color.LIGHTGRAY);
        l.setFont(Font.font("Arial", 12));
        l.setWrapText(true);
        return l;
    }

    private Button bouton(String text, String couleur) {
        Button btn = new Button(text);
        btn.setPrefWidth(166); btn.setPrefHeight(32);
        btn.setFont(Font.font("Arial Black", 12));
        String n = "-fx-background-color:" + couleur + ";-fx-text-fill:white;-fx-background-radius:10;";
        String h = "-fx-background-color:derive(" + couleur + ",20%);-fx-text-fill:white;-fx-background-radius:10;";
        btn.setStyle(n);
        btn.setOnMouseEntered(e -> btn.setStyle(h));
        btn.setOnMouseExited(e  -> btn.setStyle(n));
        return btn;
    }

    private Region sep() {
        Region r = new Region();
        r.setPrefHeight(1); r.setMaxWidth(Double.MAX_VALUE);
        r.setStyle("-fx-background-color:#0f3460;");
        VBox.setMargin(r, new Insets(4, 0, 4, 0));
        return r;
    }
}
