package ig;

import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import modele.Configuration;

/**
 * ecran de selection des niveaux
 */
public class SelectionNiveau {

    private static final int TOTAL_LEVELS = 5;
    private static final int COLS         = 3;

    private static final String[] COULEURS_NIVEAU = {
        "#3498db",
        "#9b59b6",
        "#e67e22",
        "#e74c3c",
        "#1abc9c"
    };

    private SelectionNiveau() {}

    public static Scene createScene(Stage stage, int totalNiveaux) {
        StackPane root = new StackPane();

        // image de fond
        try {
            ImageView bg = new ImageView(
                     new Image(SelectionNiveau.class.getResource("/ressources/background.png").toExternalForm()));
            bg.setFitWidth(900);
            bg.setFitHeight(720);
            bg.setPreserveRatio(false);
            root.getChildren().add(bg);
        } catch (Exception ignored) {
            root.setStyle("-fx-background-color: #0d1117;");
        }

        // overlay sombre
        Rectangle overlay = new Rectangle(900, 720, Color.color(0, 0, 0, 0.78));
        root.getChildren().add(overlay);

        // grille de points decoratifs
        Pane dotsPane = new Pane();
        dotsPane.setMouseTransparent(true);
        for (int i = 0; i < 15; i++) {
            for (int j = 0; j < 12; j++) {
                Rectangle dot = new Rectangle(2, 2,
                        Color.web("#ffffff", 0.03 + Math.random() * 0.04));
                dot.setLayoutX(30 + i * 60);
                dot.setLayoutY(30 + j * 60);
                dot.setArcWidth(2); dot.setArcHeight(2);
                dotsPane.getChildren().add(dot);
            }
        }
        root.getChildren().add(dotsPane);

        // contenu principal
        VBox main = new VBox(0);
        main.setAlignment(Pos.TOP_CENTER);

        // barre du haut
        HBox topBar = new HBox();
        topBar.setPadding(new Insets(18, 30, 18, 30));
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setSpacing(16);
        topBar.setStyle("-fx-background-color: rgba(10,20,40,0.92);");

        Rectangle accentLeft = new Rectangle(4, 32, Color.web("#f39c12"));
        accentLeft.setArcWidth(4); accentLeft.setArcHeight(4);

        VBox titreBox = new VBox(2);
        Label titreTop = new Label("SOKOBAN");
        titreTop.setFont(Font.font("Arial Black", 22));
        titreTop.setTextFill(Color.GOLD);
        Label sousTitreTop = new Label("Selection du niveau");
        sousTitreTop.setFont(Font.font("Arial", 12));
        sousTitreTop.setTextFill(Color.web("#8899aa"));
        titreBox.getChildren().addAll(titreTop, sousTitreTop);

        Region spacerTop = new Region();
        HBox.setHgrow(spacerTop, Priority.ALWAYS);

        HBox legendeBox = new HBox(16);
        legendeBox.setAlignment(Pos.CENTER);
        legendeBox.getChildren().addAll(
                legendeItem("#3498db", "Nouveau"),
                legendeItem("#27ae60", "Sauvegarde"));

        Button retourBtn = boutonMenu("← Menu");
        retourBtn.setOnAction(e -> Window.showMenu());

        topBar.getChildren().addAll(accentLeft, titreBox, spacerTop, legendeBox, retourBtn);

        // section classique
        VBox sectionClassique = new VBox(24);
        sectionClassique.setAlignment(Pos.CENTER);
        sectionClassique.setPadding(new Insets(36, 60, 28, 60));

        Label labelClassique = new Label("NIVEAUX CLASSIQUES");
        labelClassique.setFont(Font.font("Arial Black", 12));
        labelClassique.setTextFill(Color.web("#8899aa"));

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(20);
        grid.setAlignment(Pos.CENTER);

        for (int i = 0; i < totalNiveaux; i++) {
            final int niveau = i + 1;
            boolean hasSave = SauvegardeManager.sauvegardeExiste(niveau);
            String couleur = COULEURS_NIVEAU[i % COULEURS_NIVEAU.length];
            VBox card = createLevelCard(niveau, hasSave, couleur);
            card.setOnMouseClicked(e -> {
                if (hasSave) showChoixPopup(stage, niveau);
                else Window.loadLevel(niveau);
            });
            grid.add(card, i % COLS, i / COLS);
        }

        sectionClassique.getChildren().addAll(labelClassique, grid);

        // separateur
        HBox sepBox = new HBox();
        sepBox.setAlignment(Pos.CENTER);
        sepBox.setPadding(new Insets(8, 60, 8, 60));
        Rectangle sep = new Rectangle(760, 1, Color.web("#ffffff", 0.08));
        sepBox.getChildren().add(sep);

        // section parabox
        VBox sectionPara = new VBox(18);
        sectionPara.setAlignment(Pos.CENTER);
        sectionPara.setPadding(new Insets(16, 60, 40, 60));

        Label labelPara = new Label("MODE RECURSIF — PARABOX");
        labelPara.setFont(Font.font("Arial Black", 12));
        labelPara.setTextFill(Color.web("#8899aa"));

        GridPane gridRec = new GridPane();
        gridRec.setHgap(20);
        gridRec.setVgap(20);
        gridRec.setAlignment(Pos.CENTER);

        int MAX_NIVEAUX_REC = 20;
        for (int i = 1; i <= MAX_NIVEAUX_REC; i++) {
            final String chemin = "plateau/niveauRec" + i + ".txt";
            if (SelectionNiveau.class.getResource("/" + chemin) == null) break;

            final String num      = String.valueOf(i);
            final String cheminSav = "sauvegardes/rec/niveauRec" + i + ".sav";
            final boolean hasSaveRec = new java.io.File(cheminSav).exists();

            VBox card = createParaCard(num, hasSaveRec);
            card.setOnMouseClicked(e -> {
                if (hasSaveRec) showChoixPopupRec(stage, chemin, cheminSav);
                else Window.loadLevelRec(chemin);
            });
            gridRec.add(card, (i - 1) % 4, (i - 1) / 4);
        }

        sectionPara.getChildren().addAll(labelPara, gridRec);

        main.getChildren().addAll(topBar, sectionClassique, sepBox, sectionPara);

        javafx.scene.control.ScrollPane scroll =
                new javafx.scene.control.ScrollPane(main);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scroll.setPannable(true);
        scroll.setHbarPolicy(
                javafx.scene.control.ScrollPane.ScrollBarPolicy.NEVER);

        root.getChildren().add(scroll);

        return new Scene(root, 900, 720);
    }

    private static VBox createLevelCard(int niveau, boolean hasSave, String couleur) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPrefSize(220, 128);
        card.setPadding(new Insets(20));

        String accentColor = hasSave ? "#27ae60" : couleur;

        String bgNormal =
                "-fx-background-color: rgba(0,0,0,0.55);" +
                "-fx-background-radius: 16;" +
                "-fx-border-color: " + accentColor + "66;" +
                "-fx-border-width: 1.5;" +
                "-fx-border-radius: 16;" +
                "-fx-cursor: hand;";
        String bgHover =
                "-fx-background-color: rgba(0,0,0,0.70);" +
                "-fx-background-radius: 16;" +
                "-fx-border-color: " + accentColor + ";" +
                "-fx-border-width: 2;" +
                "-fx-border-radius: 16;" +
                "-fx-cursor: hand;";

        card.setStyle(bgNormal);
        DropShadow shadow = new DropShadow(16, Color.web(accentColor, 0.25));
        card.setEffect(shadow);

        Label numLabel = new Label(String.format("%02d", niveau));
        numLabel.setFont(Font.font("Arial Black", 40));
        numLabel.setTextFill(Color.web(accentColor));

        Rectangle barre = new Rectangle(36, 3, Color.web(accentColor));
        barre.setArcWidth(3); barre.setArcHeight(3);

        Label statusLabel = hasSave
                ? new Label("✓  Sauvegarde")
                : new Label("Nouveau");
        statusLabel.setFont(Font.font("Arial", 12));
        statusLabel.setTextFill(hasSave
                ? Color.web("#27ae60")
                : Color.web("#8899aa"));

        card.getChildren().addAll(numLabel, barre, statusLabel);

        card.setOnMouseEntered(e -> {
            card.setStyle(bgHover);
            shadow.setColor(Color.web(accentColor, 0.55));
            shadow.setRadius(24);
            ScaleTransition st = new ScaleTransition(Duration.millis(140), card);
            st.setToX(1.06); st.setToY(1.06);
            st.play();
        });
        card.setOnMouseExited(e -> {
            card.setStyle(bgNormal);
            shadow.setColor(Color.web(accentColor, 0.25));
            shadow.setRadius(16);
            ScaleTransition st = new ScaleTransition(Duration.millis(140), card);
            st.setToX(1.0); st.setToY(1.0);
            st.play();
        });

        return card;
    }


    private static VBox createParaCard(String num, boolean hasSave) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.CENTER);
        card.setPrefSize(160, 120);
        card.setPadding(new Insets(16));

        String accent = hasSave ? "#27ae60" : "#e74c3c";

        String bgNormal =
                "-fx-background-color: rgba(231,76,60,0.12);" +
                "-fx-background-radius: 16;" +
                "-fx-border-color: " + accent + "80;" +
                "-fx-border-width: 1.5;" +
                "-fx-border-radius: 16;" +
                "-fx-cursor: hand;";
        String bgHover =
                "-fx-background-color: rgba(231,76,60,0.28);" +
                "-fx-background-radius: 16;" +
                "-fx-border-color: " + accent + ";" +
                "-fx-border-width: 2;" +
                "-fx-border-radius: 16;" +
                "-fx-cursor: hand;";

        card.setStyle(bgNormal);
        DropShadow shadow = new DropShadow(14, Color.web(accent, 0.3));
        card.setEffect(shadow);

        Label numLabel = new Label("∞ " + num);
        numLabel.setFont(Font.font("Arial Black", 28));
        numLabel.setTextFill(Color.web(accent));

        Rectangle barre = new Rectangle(30, 3, Color.web(accent));
        barre.setArcWidth(3); barre.setArcHeight(3);

        Label statusLabel = hasSave
                ? new Label("✓  Sauvegarde")
                : new Label("Niveau " + num);
        statusLabel.setFont(Font.font("Arial Black", 10));
        statusLabel.setTextFill(Color.web(accent));
        statusLabel.setPadding(new Insets(2, 8, 2, 8));
        statusLabel.setStyle(
                "-fx-background-color: " + accent + "33;" +
                "-fx-background-radius: 10;");

        card.getChildren().addAll(numLabel, barre, statusLabel);

        card.setOnMouseEntered(e -> {
            card.setStyle(bgHover);
            shadow.setRadius(22);
            ScaleTransition st = new ScaleTransition(Duration.millis(140), card);
            st.setToX(1.06); st.setToY(1.06); st.play();
        });
        card.setOnMouseExited(e -> {
            card.setStyle(bgNormal);
            shadow.setRadius(14);
            ScaleTransition st = new ScaleTransition(Duration.millis(140), card);
            st.setToX(1.0); st.setToY(1.0); st.play();
        });

        return card;
    }

    private static HBox legendeItem(String couleur, String texte) {
        HBox item = new HBox(8);
        item.setAlignment(Pos.CENTER_LEFT);
        Rectangle carre = new Rectangle(10, 10, Color.web(couleur));
        carre.setArcWidth(3); carre.setArcHeight(3);
        Label lbl = new Label(texte);
        lbl.setFont(Font.font("Arial", 12));
        lbl.setTextFill(Color.web("#8899aa"));
        item.getChildren().addAll(carre, lbl);
        return item;
    }

    private static void showChoixPopupRec(Stage parentStage,
                                            String cheminNiveau,
                                            String cheminSav) {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.initOwner(parentStage);
        popup.initStyle(StageStyle.UNDECORATED);

        VBox box = new VBox(14);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(32));
        box.setStyle(
                "-fx-background-color: #0d1117;" +
                "-fx-background-radius: 20;" +
                "-fx-border-color: rgba(255,255,255,0.1);" +
                "-fx-border-width: 1;" +
                "-fx-border-radius: 20;");
        box.setEffect(new DropShadow(40, Color.BLACK));

        Label titrePopup = new Label("PARABOX");
        titrePopup.setFont(Font.font("Arial Black", 11));
        titrePopup.setTextFill(Color.web("#8899aa"));

        Label question = new Label("Reprendre la partie ?");
        question.setFont(Font.font("Arial Black", 20));
        question.setTextFill(Color.WHITE);

        Label sousTitre = new Label("Une sauvegarde est disponible");
        sousTitre.setFont(Font.font("Arial", 13));
        sousTitre.setTextFill(Color.web("#8899aa"));

        Rectangle sepPop = new Rectangle(280, 1, Color.web("#ffffff", 0.08));

        Button continuerBtn = boutonPopup("Continuer", "#27ae60");
        continuerBtn.setOnAction(e -> {
            popup.close();
            try {
                modele.ConfigurationRec restored =
                        persistance.SauvegardePartieRec.charger(cheminSav);
                modele.PlateauRec plateauRec = new modele.PlateauRec(cheminNiveau);
                parentStage.setScene(
                        new GameSceneRec(parentStage, plateauRec, cheminNiveau).buildFromSave(restored));
            } catch (Exception ex) {
                ex.printStackTrace();
                Window.loadLevelRec(cheminNiveau);
            }
        });

        Button recommencerBtn = boutonPopup("Recommencer", "#c0392b");
        recommencerBtn.setOnAction(e -> { popup.close(); Window.loadLevelRec(cheminNiveau); });

        Button annulerBtn = new Button("Annuler");
        annulerBtn.setFont(Font.font("Arial", 12));
        annulerBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #8899aa;");
        annulerBtn.setOnAction(e -> popup.close());

        box.getChildren().addAll(
                titrePopup, question, sousTitre, sepPop,
                continuerBtn, recommencerBtn, annulerBtn);

        popup.setScene(new Scene(box, 340, 300, Color.TRANSPARENT));
        popup.showAndWait();
    }

    private static void showChoixPopup(Stage parentStage, int niveau) {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.initOwner(parentStage);
        popup.initStyle(StageStyle.UNDECORATED);

        VBox box = new VBox(14);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(32));
        box.setStyle(
                "-fx-background-color: #0d1117;" +
                "-fx-background-radius: 20;" +
                "-fx-border-color: rgba(255,255,255,0.1);" +
                "-fx-border-width: 1;" +
                "-fx-border-radius: 20;");
        box.setEffect(new DropShadow(40, Color.BLACK));

        Label niveauLbl = new Label("NIVEAU " + niveau);
        niveauLbl.setFont(Font.font("Arial Black", 11));
        niveauLbl.setTextFill(Color.web("#8899aa"));

        Label question = new Label("Reprendre la partie ?");
        question.setFont(Font.font("Arial Black", 20));
        question.setTextFill(Color.WHITE);

        Label sousTitre = new Label("Une sauvegarde est disponible");
        sousTitre.setFont(Font.font("Arial", 13));
        sousTitre.setTextFill(Color.web("#8899aa"));

        Rectangle sepPop = new Rectangle(280, 1, Color.web("#ffffff", 0.08));

        Button continuerBtn = boutonPopup("Continuer", "#27ae60");
        continuerBtn.setOnAction(e -> {
            popup.close();
            Configuration restored = SauvegardeManager.charger(niveau);
            if (restored != null)
                parentStage.setScene(
                        new GameScene(parentStage, restored, niveau).build());
            else
                Window.loadLevel(niveau);
        });

        Button recommencerBtn = boutonPopup("Recommencer", "#c0392b");
        recommencerBtn.setOnAction(e -> { popup.close(); Window.loadLevel(niveau); });

        Button annulerBtn = new Button("Annuler");
        annulerBtn.setFont(Font.font("Arial", 12));
        annulerBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #8899aa;");
        annulerBtn.setOnAction(e -> popup.close());

        box.getChildren().addAll(
                niveauLbl, question, sousTitre, sepPop,
                continuerBtn, recommencerBtn, annulerBtn);

        popup.setScene(new Scene(box, 340, 300, Color.TRANSPARENT));
        popup.showAndWait();
    }

    private static Button boutonMenu(String text) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Arial Black", 12));
        btn.setPadding(new Insets(8, 18, 8, 18));
        String n = "-fx-background-color:rgba(255,255,255,0.08);-fx-text-fill:white;-fx-background-radius:10;";
        String h = "-fx-background-color:rgba(255,255,255,0.15);-fx-text-fill:white;-fx-background-radius:10;";
        btn.setStyle(n);
        btn.setOnMouseEntered(e -> btn.setStyle(h));
        btn.setOnMouseExited(e  -> btn.setStyle(n));
        return btn;
    }

    private static Button boutonPopup(String text, String couleur) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Arial Black", 13));
        btn.setPrefSize(280, 44);
        String n = "-fx-background-color:" + couleur + ";-fx-text-fill:white;-fx-background-radius:12;";
        String h = "-fx-background-color:derive(" + couleur + ",20%);-fx-text-fill:white;-fx-background-radius:12;";
        btn.setStyle(n);
        btn.setOnMouseEntered(e -> btn.setStyle(h));
        btn.setOnMouseExited(e  -> btn.setStyle(n));
        return btn;
    }
}
