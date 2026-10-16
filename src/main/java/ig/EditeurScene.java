package ig;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import modele.*;
import plateau.*;

/**
 * editeur visuel de plateau classique
 * panneau droit avec outils, taille, actions et legende
 */
public class EditeurScene {

    private static final int CELL_SIZE = 52;

    private final Stage   stage;
    private final Plateau plateauInit;

    private Plateau        plateau;
    private EditeurPlateau editeur;
    private Pane           canvasPane;
    private Label          statusLabel;
    private Spinner<Integer> colsSpinner;
    private Spinner<Integer> rowsSpinner;
    private ToggleGroup    toolGroup;

    public EditeurScene(Stage stage, Plateau plateauInit) {
        this.stage       = stage;
        this.plateauInit = plateauInit;
        if (plateauInit != null) {
            this.plateau = plateauInit;
        } else {
            this.plateau = creerPlateauVide(10, 8);
        }
        this.editeur = new EditeurPlateau(this.plateau);
    }

    public Scene build() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #1a1a2e;");

        root.setTop(buildTopBar());
        root.setRight(buildSidePanel());
        root.setBottom(buildStatusBar());

        canvasPane = new Pane();
        canvasPane.setStyle("-fx-background-color: #16213e;");
        canvasPane.setOnMouseClicked(e -> appliquerOutil(e.getX(), e.getY()));
        canvasPane.setOnMouseDragged(e -> appliquerOutil(e.getX(), e.getY()));

        ScrollPane scroll = new ScrollPane(canvasPane);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: #16213e;");
        root.setCenter(scroll);

        redessiner();
        return new Scene(root, 1100, 750);
    }

   
    // barre du haut
   
    private HBox buildTopBar() {
        HBox bar = new HBox(16);
        bar.setStyle("-fx-background-color: #0f3460;");
        bar.setPadding(new Insets(12, 24, 12, 24));
        bar.setAlignment(Pos.CENTER_LEFT);

        Label titre = new Label("EDITEUR CLASSIQUE");
        titre.setFont(Font.font("Arial Black", 18));
        titre.setTextFill(Color.GOLD);

        Label hint = new Label("— Cliquer ou glisser pour dessiner");
        hint.setFont(Font.font("Arial", 13));
        hint.setTextFill(Color.web("#8899aa"));

        bar.getChildren().addAll(titre, hint);
        return bar;
    }

    // panneau droit
    
    private VBox buildSidePanel() {
        VBox panel = new VBox(8);
        panel.setPadding(new Insets(16, 14, 16, 14));
        panel.setStyle("-fx-background-color: #16213e;");
        panel.setAlignment(Pos.TOP_CENTER);
        panel.setPrefWidth(210);

        // outils
        Label titreOutils = sectionLabel("OUTILS");
        toolGroup = new ToggleGroup();

        ToggleButton murBtn    = outil(" Mur",    "#5D6D7E", toolGroup);
        ToggleButton videBtn   = outil("  Vide",   "#2C3E50", toolGroup);
        ToggleButton cibleBtn  = outil("  Cible",  "#E67E22", toolGroup);
        ToggleButton joueurBtn = outil("  Joueur", "#2980B9", toolGroup);
        ToggleButton caisseBtn = outil(" Caisse", "#8B4513", toolGroup);
        ToggleButton effBtn    = outil("  Effacer","#922B21", toolGroup);
        murBtn.setSelected(true);

        // description de l outil actif
        Label descOutil = new Label("Poser un mur");
        descOutil.setFont(Font.font("Arial", 11));
        descOutil.setTextFill(Color.web("#8899aa"));
        descOutil.setWrapText(true);

        toolGroup.selectedToggleProperty().addListener((obs, o, n) -> {
            if (n == null) return;
            String t = ((ToggleButton) n).getText();
            if (t.contains("Mur"))     descOutil.setText("Poser un mur solide");
            else if (t.contains("Vide"))   descOutil.setText("Effacer et mettre case vide");
            else if (t.contains("Cible"))  descOutil.setText("Poser une cible (point d arrivee)");
            else if (t.contains("Joueur")) descOutil.setText("Placer le joueur initial");
            else if (t.contains("Caisse")) descOutil.setText("Placer une caisse a pousser");
            else if (t.contains("Effacer"))descOutil.setText("Effacer cible et caisse");
        });

        //taille
        Label titreTaille = sectionLabel("TAILLE DU PLATEAU");

        Label lblCols = miniLabel("Colonnes");
        colsSpinner = new Spinner<>(5, 20, plateau.getX());
        colsSpinner.setPrefWidth(182);
        styleSpinner(colsSpinner);

        Label lblRows = miniLabel("Lignes");
        rowsSpinner = new Spinner<>(5, 20, plateau.getY());
        rowsSpinner.setPrefWidth(182);
        styleSpinner(rowsSpinner);

        Button redimBtn = bouton("Redimensionner", "#8e44ad");
        redimBtn.setOnAction(e -> redimensionner());

        //actions
        Label titreActions = sectionLabel("ACTIONS");

        Button sauvegarderBtn = bouton(" Sauvegarder", "#27ae60");
        Button testerBtn      = bouton("  Tester",       "#2980b9");
        Button viderBtn       = bouton("  Vider grille", "#c0392b");
        Button menuBtn        = bouton(" Menu",           "#555");

        sauvegarderBtn.setOnAction(e -> sauvegarder());
        testerBtn.setOnAction(e      -> tester());
        viderBtn.setOnAction(e       -> vider());
        menuBtn.setOnAction(e        -> Window.showMenu());

        // legende
        Label titreLegende = sectionLabel("LEGENDE");
        VBox legende = new VBox(6,
            legendeItem("#5D6D7E", "█", "Mur"),
            legendeItem("#ECEFF4", "□", "Case vide"),
            legendeItem("#E67E22", "◎", "Cible"),
            legendeItem("#2980B9", "●", "Joueur"),
            legendeItem("#8B4513", "■", "Caisse")
        );

        panel.getChildren().addAll(
            titreOutils, sep(),
            murBtn, videBtn, cibleBtn, joueurBtn, caisseBtn, effBtn,
            descOutil,
            sep(), titreTaille, sep(),
            lblCols, colsSpinner,
            lblRows, rowsSpinner,
            redimBtn,
            sep(), titreActions, sep(),
            sauvegarderBtn, testerBtn, viderBtn,
            sep(), menuBtn,
            sep(), titreLegende, sep(),
            legende
        );

        return panel;
    }

    private HBox buildStatusBar() {
        statusLabel = new Label("Cliquez sur la grille pour dessiner");
        statusLabel.setTextFill(Color.LIGHTGRAY);
        statusLabel.setFont(Font.font("Arial", 13));
        HBox bar = new HBox(statusLabel);
        bar.setPadding(new Insets(8, 20, 8, 20));
        bar.setStyle("-fx-background-color: #0f3460;");
        bar.setAlignment(Pos.CENTER_LEFT);
        return bar;
    }

    private void appliquerOutil(double px, double py) {
        int x = (int)(px / CELL_SIZE);
        int y = (int)(py / CELL_SIZE);
        if (x < 0 || x >= plateau.getX() || y < 0 || y >= plateau.getY()) return;
        Position pos = new Position(x, y);
        OutilEdition outil = getOutilActif();
        if (outil == null) return;
        try {
            editeur.appliquer(pos, outil);
            redessiner();
        } catch (Exception ex) {
            setStatus("Erreur : " + ex.getMessage());
        }
    }

    private OutilEdition getOutilActif() {
        Toggle t = toolGroup.getSelectedToggle();
        if (t == null) return null;
        String text = ((ToggleButton) t).getText();
        if (text.contains("Mur"))     return OutilEdition.MUR;
        if (text.contains("Vide"))    return OutilEdition.VIDE;
        if (text.contains("Cible"))   return OutilEdition.CIBLE_ON;
        if (text.contains("Joueur"))  return OutilEdition.JOUEUR;
        if (text.contains("Caisse"))  return OutilEdition.CAISSE;
        if (text.contains("Effacer")) return OutilEdition.EFFACER;
        return null;
    }

    private void redimensionner() {
        int cols = colsSpinner.getValue();
        int rows = rowsSpinner.getValue();
        Plateau p = creerPlateauVide(cols, rows);
        if (p != null) {
            plateau = p;
            editeur = new EditeurPlateau(plateau);
            redessiner();
            setStatus("Plateau redimensionne : " + cols + "x" + rows);
        }
    }

    private void sauvegarder() {
        try {
            ValidationNiveau.verifierOuException(plateau);
            String chemin = "plateau/niveau_edite.txt";
            persistance.PersistanceNiveau.sauvegarderPlateau(plateau, chemin);
            setStatus("Sauvegarde : " + chemin);
        } catch (IllegalStateException ex) {
            setStatus( ex.getMessage());
        } catch (Exception ex) {
            setStatus("Erreur sauvegarde : " + ex.getMessage());
        }
    }

    private void tester() {
        try {
            ValidationNiveau.verifierOuException(plateau);
            Configuration config = new Configuration(plateau);
            stage.setScene(new GameScene(stage, config, 0).build());
        } catch (Exception ex) {
            setStatus(" Impossible de tester : " + ex.getMessage());
        }
    }

   private void vider() {
    Plateau p = creerPlateauVide(plateau.getX(), plateau.getY());
    if (p != null) {
        plateau = p;
        editeur = new EditeurPlateau(plateau);
        redessiner();
        setStatus("Grille videe");
    }
}
    private void redessiner() {
        canvasPane.getChildren().clear();
        canvasPane.setPrefSize(plateau.getX() * CELL_SIZE, plateau.getY() * CELL_SIZE);

        for (int y = 0; y < plateau.getY(); y++) {
            for (int x = 0; x < plateau.getX(); x++) {
                Position pos = new Position(x, y);
                double px = x * CELL_SIZE;
                double py = y * CELL_SIZE;

                Element elem = plateau.get(pos);
                if (elem == null) continue;

                if (elem.getType() == Type.MUR) {
                    Rectangle r = new Rectangle(px, py, CELL_SIZE, CELL_SIZE);
                    r.setFill(Color.web("#2C3E50"));
                    r.setStroke(Color.web("#4C566A", 0.5));
                    canvasPane.getChildren().add(r);
                } else {
                    Rectangle cell = new Rectangle(px, py, CELL_SIZE, CELL_SIZE);
                    cell.setFill(Color.web("#ECEFF4"));
                    cell.setStroke(Color.web("#B0BEC5", 0.6));
                    cell.setArcWidth(6); cell.setArcHeight(6);
                    canvasPane.getChildren().add(cell);

                    if (plateau.estCible(pos)) {
                        Circle c = new Circle(px + CELL_SIZE/2.0, py + CELL_SIZE/2.0,
                                CELL_SIZE * 0.22, Color.web("#E67E22", 0.8));
                        canvasPane.getChildren().add(c);
                    }

                    if (plateau.getPositionsCaisses() != null &&
                        plateau.getPositionsCaisses().contains(pos)) {
                        double b = 5;
                        Rectangle rc = new Rectangle(px+b, py+b,
                                CELL_SIZE-b*2, CELL_SIZE-b*2);
                        rc.setFill(Color.web("#8B4513"));
                        rc.setArcWidth(6); rc.setArcHeight(6);
                        canvasPane.getChildren().add(rc);
                    }

                    if (pos.equals(plateau.getPositionJoueurInitiale())) {
                        Circle c = new Circle(px + CELL_SIZE/2.0, py + CELL_SIZE/2.0,
                                CELL_SIZE * 0.35, Color.web("#2980B9"));
                        canvasPane.getChildren().add(c);
                        Label lbl = new Label("@");
                        lbl.setFont(Font.font("Arial Black", 14));
                        lbl.setTextFill(Color.WHITE);
                        lbl.setLayoutX(px + CELL_SIZE/2.0 - 6);
                        lbl.setLayoutY(py + CELL_SIZE/2.0 - 10);
                        canvasPane.getChildren().add(lbl);
                    }
                }

                Rectangle grille = new Rectangle(px, py, CELL_SIZE, CELL_SIZE);
                grille.setFill(Color.TRANSPARENT);
                grille.setStroke(Color.web("#4C566A", 0.25));
                canvasPane.getChildren().add(grille);
            }
        }
    }


    /**
     * cree un plateau vide via un fichier temporaire
     * Plateau n accepte que String fichier en constructeur
     */
    private Plateau creerPlateauVide(int cols, int rows) {
        try {
            StringBuilder sb = new StringBuilder();
            for (int y = 0; y < rows; y++) {
                for (int x = 0; x < cols; x++) sb.append(' ');
                sb.append('\n');
            }
            java.io.File tmp = java.io.File.createTempFile("plateau", ".txt");
            java.nio.file.Files.writeString(tmp.toPath(), sb.toString());
            Plateau p = new Plateau(tmp.getAbsolutePath());
            tmp.delete();
            return p;
        } catch (Exception ex) {
            setStatus("Erreur creation plateau : " + ex.getMessage());
            return null;
        }
    }

    private void setStatus(String msg) {
        if (statusLabel != null) statusLabel.setText(msg);
    }

    private ToggleButton outil(String text, String couleur, ToggleGroup group) {
        ToggleButton btn = new ToggleButton(text);
        btn.setToggleGroup(group);
        btn.setPrefWidth(182); btn.setPrefHeight(36);
        btn.setFont(Font.font("Arial", 13));
        btn.setFocusTraversable(false);
        String n = "-fx-background-color:#0f3460;-fx-text-fill:white;-fx-background-radius:8;-fx-border-color:" + couleur + "88;-fx-border-width:1.5;-fx-border-radius:8;";
        String s = "-fx-background-color:" + couleur + "55;-fx-text-fill:white;-fx-background-radius:8;-fx-border-color:" + couleur + ";-fx-border-width:2;-fx-border-radius:8;";
        btn.setStyle(n);
        btn.selectedProperty().addListener((obs, o, nv) -> btn.setStyle(nv ? s : n));
        return btn;
    }

    private Button bouton(String text, String couleur) {
        Button btn = new Button(text);
        btn.setPrefWidth(182); btn.setPrefHeight(36);
        btn.setFont(Font.font("Arial Black", 12));
        btn.setFocusTraversable(false);
        String n = "-fx-background-color:" + couleur + ";-fx-text-fill:white;-fx-background-radius:10;";
        String h = "-fx-background-color:derive(" + couleur + ",20%);-fx-text-fill:white;-fx-background-radius:10;";
        btn.setStyle(n);
        btn.setOnMouseEntered(e -> btn.setStyle(h));
        btn.setOnMouseExited(e  -> btn.setStyle(n));
        return btn;
    }

    private void styleSpinner(Spinner<Integer> sp) {
        sp.setStyle("-fx-background-color:#0f3460;-fx-text-fill:white;");
        sp.setEditable(true);
    }

    private HBox legendeItem(String couleur, String symbole, String texte) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        Rectangle r = new Rectangle(14, 14, Color.web(couleur));
        r.setArcWidth(3); r.setArcHeight(3);
        r.setStroke(Color.web("#4C566A", 0.5));
        Label lbl = new Label(texte);
        lbl.setFont(Font.font("Arial", 12));
        lbl.setTextFill(Color.LIGHTGRAY);
        row.getChildren().addAll(r, lbl);
        return row;
    }

    private Label sectionLabel(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("Arial Black", 12));
        l.setTextFill(Color.web("#8899aa"));
        VBox.setMargin(l, new Insets(6, 0, 0, 0));
        return l;
    }

    private Label miniLabel(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("Arial", 11));
        l.setTextFill(Color.web("#8899aa"));
        return l;
    }

    private Region sep() {
        Region r = new Region();
        r.setPrefHeight(1); r.setMaxWidth(Double.MAX_VALUE);
        r.setStyle("-fx-background-color:#0f3460;");
        VBox.setMargin(r, new Insets(3, 0, 3, 0));
        return r;
    }
}
