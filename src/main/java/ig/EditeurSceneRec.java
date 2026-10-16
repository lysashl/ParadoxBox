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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * editeur visuel pour les niveaux recursifs Parabox
 * panneau droit avec les outils necessaires
 * selection du monde 
 * legende 
 */
public class EditeurSceneRec {

    private static final int CELL_SIZE = 52;

    private static final Map<Character, String> COULEURS = new HashMap<>();
    static {
        COULEURS.put('A', "#E74C3C");
        COULEURS.put('B', "#3498DB");
        COULEURS.put('C', "#2ECC71");
        COULEURS.put('D', "#F39C12");
        COULEURS.put('E', "#9B59B6");
        COULEURS.put('F', "#1ABC9C");
    }

    private final Stage      stage;
    private final PlateauRec plateauRec;
    private EditeurPlateauRec editeur;

    private Pane       canvasPane;
    private Label      statusLabel;
    private Label      mondeCourantLabel;
    private ToggleGroup toolGroup;
    private char       mondeCaisseChoisie = ' ';

    public EditeurSceneRec(Stage stage, PlateauRec plateauRec) {
        this.stage      = stage;
        this.plateauRec = plateauRec;
        this.editeur    = new EditeurPlateauRec(plateauRec);
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

        Label titre = new Label("EDITEUR PARABOX");
        titre.setFont(Font.font("Arial Black", 18));
        titre.setTextFill(Color.GOLD);

        mondeCourantLabel = new Label("Monde : Principal");
        mondeCourantLabel.setFont(Font.font("Arial", 14));
        mondeCourantLabel.setTextFill(Color.LIGHTGRAY);

        Label hint = new Label("Cliquer ou glisser pour dessiner");
        hint.setFont(Font.font("Arial", 13));
        hint.setTextFill(Color.web("#8899aa"));

        bar.getChildren().addAll(titre, mondeCourantLabel, hint);
        return bar;
    }

    // panneau droit
    private ScrollPane buildSidePanel() {
        VBox panel = new VBox(8);
        panel.setPadding(new Insets(14, 14, 14, 14));
        panel.setStyle("-fx-background-color: #16213e;");
        panel.setAlignment(Pos.TOP_CENTER);
        panel.setPrefWidth(220);

        // choix du monde
        Label titreMonde = sectionLabel("CHOISIR UN MONDE");

        // monde principal
        Button btnPrincipal = boutonMonde("Principal", "#555");
        btnPrincipal.setOnAction(e -> changerMonde(' '));

        VBox mondesBox = new VBox(6, btnPrincipal);

        for (char c : new char[]{'A','B','C','D','E','F'}) {
            if (plateauRec.getMonde(c) == null) continue;
            String couleur = COULEURS.getOrDefault(c, "#555");
            HBox row = new HBox(8);
            row.setAlignment(Pos.CENTER_LEFT);

            // carre de couleur
            Rectangle carre = new Rectangle(12, 12, Color.web(couleur));
            carre.setArcWidth(3); carre.setArcHeight(3);

            Button btn = boutonMonde("Monde " + c, couleur);
            HBox.setHgrow(btn, Priority.ALWAYS);
            final char monde = c;
            btn.setOnAction(e -> changerMonde(monde));

            row.getChildren().addAll(carre, btn);
            mondesBox.getChildren().add(row);
        }

        // outils
        Label titreOutils = sectionLabel("OUTILS DE DESSIN");
        toolGroup = new ToggleGroup();

        ToggleButton murBtn    = outil(" Mur",     "#5D6D7E", toolGroup);
        ToggleButton videBtn   = outil("  Vide",    "#2C3E50", toolGroup);
        ToggleButton cibleBtn  = outil("  Cible",   "#E67E22", toolGroup);
        ToggleButton joueurBtn = outil("  Joueur",   "#2980B9", toolGroup);
        ToggleButton caisseBtn = outil("  Caisse",  "#8B4513", toolGroup);
        ToggleButton effBtn    = outil("  Effacer", "#922B21", toolGroup);
        murBtn.setSelected(true);

        // description outil
        Label descOutil = new Label("Poser un mur solide");
        descOutil.setFont(Font.font("Arial", 11));
        descOutil.setTextFill(Color.web("#8899aa"));
        descOutil.setWrapText(true);

        toolGroup.selectedToggleProperty().addListener((obs, o, n) -> {
            if (n == null) return;
            String t = ((ToggleButton) n).getText();
            if (t.contains("Mur"))      descOutil.setText("Poser un mur solide");
            else if (t.contains("Vide"))    descOutil.setText("Effacer et remettre case vide");
            else if (t.contains("Cible"))   descOutil.setText("Poser une cible (point d arrivee)");
            else if (t.contains("Joueur"))  descOutil.setText("Placer le joueur initial du monde");
            else if (t.contains("Caisse"))  descOutil.setText("Placer une caisse (choisir type ci-dessous)");
            else if (t.contains("Effacer")) descOutil.setText("Effacer cible et caisse a cette case");
        });

        // type de caisse
        Label titreCaisse = sectionLabel("TYPE DE CAISSE");
        Label hintCaisse  = new Label("Actif quand outil = Caisse");
        hintCaisse.setFont(Font.font("Arial", 10));
        hintCaisse.setTextFill(Color.web("#556677"));

        ToggleGroup mondeGroup = new ToggleGroup();
        VBox caisseBox = new VBox(5);

        ToggleButton btnSimple = toggleCaisse("Caisse simple", "#8B4513", mondeGroup);
        btnSimple.setSelected(true);
        btnSimple.setOnAction(e -> mondeCaisseChoisie = ' ');
        caisseBox.getChildren().add(btnSimple);

        for (char c : new char[]{'A','B','C','D','E','F'}) {
            if (plateauRec.getMonde(c) == null) continue;
            String couleur = COULEURS.getOrDefault(c, "#555");
            ToggleButton btn = toggleCaisse("Monde " + c, couleur, mondeGroup);
            final char monde = c;
            btn.setOnAction(e -> mondeCaisseChoisie = monde);
            caisseBox.getChildren().add(btn);
        }

        // actions
        Label titreActions = sectionLabel("ACTIONS");

        Button sauvegarderBtn = bouton("  Sauvegarder", "#27ae60");
        Button validerBtn     = bouton(" Valider monde", "#2980b9");
        Button testerBtn      = bouton(" Tester",        "#8e44ad");
        Button menuBtn        = bouton(" Menu",            "#555");

        sauvegarderBtn.setOnAction(e -> sauvegarder());
        validerBtn.setOnAction(e     -> valider());
        testerBtn.setOnAction(e      -> tester());
        menuBtn.setOnAction(e        -> Window.showMenu());

        // legende
        Label titreLegende = sectionLabel("LEGENDE");
        VBox legende = new VBox(5,
            legendeItem("#2C3E50", "  #  ", "Mur"),
            legendeItem("#ECEFF4", "     ", "Case vide"),
            legendeItem("#E67E22", "  .  ", "Cible"),
            legendeItem("#2980B9", "  @  ", "Joueur initial"),
            legendeItem("#8B4513", "  $  ", "Caisse simple"),
            legendeItem("#E74C3C", "  A  ", "Caisse-monde A"),
            legendeItem("#3498DB", "  B  ", "Caisse-monde B"),
            legendeItem("#2ECC71", "  C  ", "Caisse-monde C")
        );

        panel.getChildren().addAll(
            titreMonde, sep(), mondesBox,
            sep(), titreOutils, sep(),
            murBtn, videBtn, cibleBtn, joueurBtn, caisseBtn, effBtn,
            descOutil,
            sep(), titreCaisse, hintCaisse, sep(),
            caisseBox,
            sep(), titreActions, sep(),
            sauvegarderBtn, validerBtn, testerBtn,
            sep(), menuBtn,
            sep(), titreLegende, sep(),
            legende
        );

        // scroll si panneau trop long
        ScrollPane panelScroll = new ScrollPane(panel);
        panelScroll.setFitToWidth(true);
        panelScroll.setStyle("-fx-background: transparent; -fx-background-color: #16213e;");
        panelScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        panelScroll.setPrefWidth(240);

        VBox wrapper = new VBox(panelScroll);
        wrapper.setStyle("-fx-background-color: #16213e;");
        wrapper.setPrefWidth(240);
        return panelScroll;
    }

    private HBox buildStatusBar() {
        statusLabel = new Label("Selectionnez un outil puis cliquez sur le plateau");
        statusLabel.setTextFill(Color.LIGHTGRAY);
        statusLabel.setFont(Font.font("Arial", 13));
        HBox bar = new HBox(statusLabel);
        bar.setPadding(new Insets(8, 20, 8, 20));
        bar.setStyle("-fx-background-color: #0f3460;");
        bar.setAlignment(Pos.CENTER_LEFT);
        return bar;
    }

    private void changerMonde(char nom) {
        try {
            editeur = (nom == ' ')
                    ? new EditeurPlateauRec(plateauRec)
                    : new EditeurPlateauRec(plateauRec, nom);
            String nomStr = (nom == ' ') ? "Principal" : "Monde " + nom;
            mondeCourantLabel.setText(" Monde : " + nomStr);
            redessiner();
            setStatus("Vous editez maintenant le " + nomStr);
        } catch (Exception ex) {
            setStatus("Erreur : " + ex.getMessage());
        }
    }

    private void appliquerOutil(double px, double py) {
        Monde monde = editeur.getMondeCourant();
        int x = (int)(px / CELL_SIZE);
        int y = (int)(py / CELL_SIZE);
        if (x < 0 || x >= monde.getX() || y < 0 || y >= monde.getY()) return;
        Position pos = new Position(x, y);
        OutilEdition outil = getOutilActif();
        if (outil == null) return;
        try {
            if (outil == OutilEdition.CAISSE)
                editeur.appliquer(pos, outil, mondeCaisseChoisie);
            else
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

    private void valider() {
        try {
            ValidationNiveauRec.verifierMonde(editeur.getMondeCourant(), plateauRec);
            setStatus(" Monde valide !");
        } catch (Exception ex) {
            setStatus( ex.getMessage());
        }
    }

    private void sauvegarder() {
        try {
            ValidationNiveauRec.verifierPlateau(plateauRec);
            String chemin = "plateau/niveauRec_edite.txt";
            ecrirePlateauRec(chemin);
            setStatus("Niveau sauvegarde : " + chemin);
        } catch (IllegalStateException ex) {
            setStatus( ex.getMessage());
        } catch (Exception ex) {
            setStatus("Erreur sauvegarde : " + ex.getMessage());
        }
    }

    private void tester() {
        try {
            ValidationNiveauRec.verifierPlateau(plateauRec);
            stage.setScene(new GameSceneRec(stage, plateauRec, "plateau/niveauRec_edite.txt").build());
        } catch (Exception ex) {
            setStatus(" Impossible de tester : " + ex.getMessage());
        }
    }

    private void ecrirePlateauRec(String chemin) throws Exception {
        File f = new File(chemin);
        if (f.getParentFile() != null) f.getParentFile().mkdirs();
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(f))) {
            ecrireMonde(bw, plateauRec.getMonde(plateauRec.getMondePrincipal()));
            for (char c : new char[]{'A','B','C','D','E','F'}) {
                Monde m = plateauRec.getMonde(c);
                if (m == null) continue;
                bw.newLine();
                bw.write("[" + c + "]");
                bw.newLine();
                ecrireMonde(bw, m);
            }
        }
    }

    private void ecrireMonde(BufferedWriter bw, Monde monde) throws Exception {
        Position posJoueur = monde.getPositionJoueurInitiale();
        for (int y = 0; y < monde.getY(); y++) {
            StringBuilder sb = new StringBuilder();
            for (int x = 0; x < monde.getX(); x++) {
                Position pos = new Position(x, y);
                boolean estMur    = monde.get(pos) != null && monde.get(pos).getType() == Type.MUR;
                boolean estJoueur = posJoueur != null && posJoueur.equals(pos);
                boolean estCible  = monde.estCible(pos);
                boolean estCaisse = false;
                char nomCaisse = ' ';
                if (monde.getPositionsCaisses() != null) {
                    for (var pc : monde.getPositionsCaisses()) {
                        if (pc.getPos().equals(pos)) {
                            estCaisse = true;
                            nomCaisse = pc.getMondeNom();
                            break;
                        }
                    }
                }
                if (estMur)         sb.append('#');
                else if (estJoueur) sb.append(estCible ? '+' : '@');
                else if (estCaisse) sb.append(nomCaisse == ' '
                        ? (estCible ? '*' : '$')
                        : nomCaisse);
                else if (estCible)  sb.append('.');
                else                sb.append(' ');
            }
            bw.write(sb.toString());
            bw.newLine();
        }
    }

    // rendu plateau
    
    private void redessiner() {
        canvasPane.getChildren().clear();
        Monde monde = editeur.getMondeCourant();
        if (monde == null) return;

        canvasPane.setPrefSize(monde.getX() * CELL_SIZE, monde.getY() * CELL_SIZE);

        for (int y = 0; y < monde.getY(); y++) {
            for (int x = 0; x < monde.getX(); x++) {
                Position pos = new Position(x, y);
                double px = x * CELL_SIZE;
                double py = y * CELL_SIZE;
                Element elem = monde.get(pos);
                if (elem == null) continue;

                if (elem.getType() == Type.MUR) {
                    Rectangle r = new Rectangle(px, py, CELL_SIZE, CELL_SIZE);
                    r.setFill(Color.web("#2C3E50"));
                    r.setStroke(Color.web("#4C566A", 0.5));
                    canvasPane.getChildren().add(r);
                } else {
                    Rectangle cell = new Rectangle(px, py, CELL_SIZE, CELL_SIZE);
                    cell.setFill(Color.web("#ECEFF4"));
                    cell.setStroke(Color.web("#B0BEC5", 0.5));
                    cell.setArcWidth(6); cell.setArcHeight(6);
                    canvasPane.getChildren().add(cell);

                    if (monde.estCible(pos)) {
                        Circle c = new Circle(px + CELL_SIZE/2.0, py + CELL_SIZE/2.0,
                                CELL_SIZE * 0.2, Color.web("#E67E22", 0.85));
                        canvasPane.getChildren().add(c);
                    }

                    if (monde.getPositionsCaisses() != null) {
                        for (var pc : monde.getPositionsCaisses()) {
                            if (pc.getPos().equals(pos)) {
                                char nom = pc.getMondeNom();
                                String couleur = (nom == ' ')
                                        ? "#8B4513"
                                        : COULEURS.getOrDefault(nom, "#95A5A6");
                                double b = 5;
                                Rectangle rc = new Rectangle(px+b, py+b,
                                        CELL_SIZE-b*2, CELL_SIZE-b*2);
                                rc.setFill(Color.web(couleur));
                                rc.setArcWidth(8); rc.setArcHeight(8);
                                canvasPane.getChildren().add(rc);
                                if (nom != ' ') {
                                    Label lbl = new Label(String.valueOf(nom));
                                    lbl.setFont(Font.font("Arial Black", 18));
                                    lbl.setTextFill(Color.WHITE);
                                    lbl.setLayoutX(px + CELL_SIZE/2.0 - 7);
                                    lbl.setLayoutY(py + CELL_SIZE/2.0 - 12);
                                    canvasPane.getChildren().add(lbl);
                                }
                                break;
                            }
                        }
                    }

                    if (pos.equals(monde.getPositionJoueurInitiale())) {
                        Circle c = new Circle(px + CELL_SIZE/2.0, py + CELL_SIZE/2.0,
                                CELL_SIZE * 0.34, Color.web("#2980B9"));
                        canvasPane.getChildren().add(c);
                        Label lbl = new Label("@");
                        lbl.setFont(Font.font("Arial Black", 16));
                        lbl.setTextFill(Color.WHITE);
                        lbl.setLayoutX(px + CELL_SIZE/2.0 - 7);
                        lbl.setLayoutY(py + CELL_SIZE/2.0 - 11);
                        canvasPane.getChildren().add(lbl);
                    }
                }

                Rectangle grille = new Rectangle(px, py, CELL_SIZE, CELL_SIZE);
                grille.setFill(Color.TRANSPARENT);
                grille.setStroke(Color.web("#4C566A", 0.2));
                canvasPane.getChildren().add(grille);
            }
        }
    }

    private void setStatus(String msg) {
        if (statusLabel != null) statusLabel.setText(msg);
    }

    private ToggleButton outil(String text, String couleur, ToggleGroup group) {
        ToggleButton btn = new ToggleButton(text);
        btn.setToggleGroup(group);
        btn.setPrefWidth(192); btn.setPrefHeight(34);
        btn.setFont(Font.font("Arial", 13));
        btn.setFocusTraversable(false);
        String n = "-fx-background-color:#0f3460;-fx-text-fill:white;-fx-background-radius:8;" +
                   "-fx-border-color:" + couleur + "77;-fx-border-width:1.5;-fx-border-radius:8;";
        String s = "-fx-background-color:" + couleur + "55;-fx-text-fill:white;-fx-background-radius:8;" +
                   "-fx-border-color:" + couleur + ";-fx-border-width:2;-fx-border-radius:8;";
        btn.setStyle(n);
        btn.selectedProperty().addListener((obs, o, nv) -> btn.setStyle(nv ? s : n));
        return btn;
    }

    private ToggleButton toggleCaisse(String text, String couleur, ToggleGroup group) {
        ToggleButton btn = new ToggleButton(text);
        btn.setToggleGroup(group);
        btn.setPrefWidth(192); btn.setPrefHeight(30);
        btn.setFont(Font.font("Arial", 12));
        btn.setFocusTraversable(false);
        String n = "-fx-background-color:rgba(0,0,0,0.3);-fx-text-fill:white;-fx-background-radius:8;" +
                   "-fx-border-color:" + couleur + "66;-fx-border-width:1;-fx-border-radius:8;";
        String s = "-fx-background-color:" + couleur + "44;-fx-text-fill:white;-fx-background-radius:8;" +
                   "-fx-border-color:" + couleur + ";-fx-border-width:2;-fx-border-radius:8;";
        btn.setStyle(n);
        btn.selectedProperty().addListener((obs, o, nv) -> btn.setStyle(nv ? s : n));
        return btn;
    }

    private Button bouton(String text, String couleur) {
        Button btn = new Button(text);
        btn.setPrefWidth(192); btn.setPrefHeight(36);
        btn.setFont(Font.font("Arial Black", 12));
        btn.setFocusTraversable(false);
        String n = "-fx-background-color:" + couleur + ";-fx-text-fill:white;-fx-background-radius:10;";
        String h = "-fx-background-color:derive(" + couleur + ",20%);-fx-text-fill:white;-fx-background-radius:10;";
        btn.setStyle(n);
        btn.setOnMouseEntered(e -> btn.setStyle(h));
        btn.setOnMouseExited(e  -> btn.setStyle(n));
        return btn;
    }

    private Button boutonMonde(String text, String couleur) {
        Button btn = new Button(text);
        btn.setPrefWidth(192); btn.setPrefHeight(32);
        btn.setFont(Font.font("Arial Black", 12));
        btn.setFocusTraversable(false);
        String n = "-fx-background-color:" + couleur + "55;-fx-text-fill:white;-fx-background-radius:8;" +
                   "-fx-border-color:" + couleur + "88;-fx-border-width:1.5;-fx-border-radius:8;";
        String h = "-fx-background-color:" + couleur + ";-fx-text-fill:white;-fx-background-radius:8;" +
                   "-fx-border-color:" + couleur + ";-fx-border-width:2;-fx-border-radius:8;";
        btn.setStyle(n);
        btn.setOnMouseEntered(e -> btn.setStyle(h));
        btn.setOnMouseExited(e  -> btn.setStyle(n));
        return btn;
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
        l.setFont(Font.font("Arial Black", 11));
        l.setTextFill(Color.web("#8899aa"));
        VBox.setMargin(l, new Insets(6, 0, 0, 0));
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
