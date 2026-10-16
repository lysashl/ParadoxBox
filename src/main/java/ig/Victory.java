package ig;

import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Random;

/**
 * ecran de victoire avec etoiles basees sur le score
 * 3 etoiles score > 150
 * 2 etoiles score > 50
 * 1 etoile niveau complete
 */
public class Victory {

    private static final int TOTAL_LEVELS   = 5;
    private static final int CONFETTI_COUNT = 50;

    private final Stage stage;
    private final int   niveau;
    private final int   score;
    private final int   coups;
    private MediaPlayer victoryMusic;

    public Victory(Stage stage, int niveau, int score, int coups) {
        this.stage  = stage;
        this.niveau = niveau;
        this.score  = score;
        this.coups  = coups;
    }

    public Scene createScene() {
        StackPane root = new StackPane();

        addBackground(root);
        addConfetti(root);

        VBox contenu = new VBox(20);
        contenu.setAlignment(Pos.CENTER);
        contenu.setPadding(new Insets(30));

        addTitle(contenu);
        addEtoiles(contenu);
        addScorePanel(contenu);
        addMenu(contenu);

        root.getChildren().add(contenu);
        StackPane.setAlignment(contenu, Pos.CENTER);

        playMusic();
        return new Scene(root, 900, 680);
    }

    private void addBackground(StackPane root) {
        try {
            String uri = resolveUri("/ressources/victory.jpg", "ressources/victory.jpg");
            ImageView bgView = new ImageView(new Image(uri));
            bgView.setFitWidth(900);
            bgView.setFitHeight(680);
            bgView.setPreserveRatio(false);
            root.getChildren().add(bgView);
        } catch (Exception e) {
            try {
                String uri = resolveUri("/ressources/background.png", "ressources/background.png");
                ImageView bgView = new ImageView(new Image(uri));
                bgView.setFitWidth(900);
                bgView.setFitHeight(680);
                root.getChildren().add(bgView);
            } catch (Exception ignored) {
                root.setStyle("-fx-background-color: #1a1a2e;");
            }
        }
        Rectangle overlay = new Rectangle(900, 680, Color.color(0, 0, 0, 0.55));
        root.getChildren().add(overlay);
    }

    private void addConfetti(StackPane root) {
        Pane confettiPane = new Pane();
        Random rng = new Random();
        for (int i = 0; i < CONFETTI_COUNT; i++) {
            javafx.scene.shape.Shape dot;
            if (rng.nextBoolean()) {
                dot = new Circle(3 + rng.nextInt(5),
                        Color.color(rng.nextDouble(), rng.nextDouble(), rng.nextDouble()));
            } else {
                Rectangle r = new Rectangle(6 + rng.nextInt(6), 6 + rng.nextInt(6),
                        Color.color(rng.nextDouble(), rng.nextDouble(), rng.nextDouble()));
                r.setArcWidth(2); r.setArcHeight(2);
                dot = r;
            }
            dot.setOpacity(0.9);
            dot.setLayoutX(rng.nextInt(900));
            dot.setLayoutY(-50);
            TranslateTransition fall = new TranslateTransition(
                    Duration.seconds(2.5 + rng.nextDouble() * 3.5), dot);
            fall.setFromY(-100); fall.setToY(780);
            fall.setCycleCount(Animation.INDEFINITE);
            fall.setDelay(Duration.millis(rng.nextInt(2500)));
            RotateTransition spin = new RotateTransition(
                    Duration.seconds(1 + rng.nextDouble() * 2), dot);
            spin.setByAngle(360);
            spin.setCycleCount(Animation.INDEFINITE);
            new ParallelTransition(dot, fall, spin).play();
            confettiPane.getChildren().add(dot);
        }
        root.getChildren().add(confettiPane);
    }

    private void addTitle(VBox contenu) {
        Text title = new Text("VICTOIRE !");
        title.setFont(Font.font("Arial Black", 75));
        title.setFill(Color.GOLD);
        title.setEffect(new DropShadow(40, Color.web("#0f3460")));
        ScaleTransition pulse = new ScaleTransition(Duration.seconds(0.8), title);
        pulse.setFromX(1.0); pulse.setFromY(1.0);
        pulse.setToX(1.06);  pulse.setToY(1.06);
        pulse.setAutoReverse(true);
        pulse.setCycleCount(Animation.INDEFINITE);
        pulse.play();

        String sousTitreText = (niveau == 0)
                ? "Votre niveau edité est resolu !"
                : niveau == -1 ? "Mode Parabox complete !"
                : "Niveau " + niveau + " complete !";
        Text sousTitre = new Text(sousTitreText);
        sousTitre.setFont(Font.font("Arial", 18));
        sousTitre.setFill(Color.LIGHTGRAY);

        contenu.getChildren().addAll(title, sousTitre);
    }

    /**
     * affiche 1 2 ou 3 etoiles selon le score
     * 3 etoiles score > 150
     * 2 etoiles score > 50
     * 1 etoile  niveau complete
     */
    private void addEtoiles(VBox contenu) {
        int nbEtoiles = score > 150 ? 3 : score > 50 ? 2 : 1;

        HBox etoilesBox = new HBox(10);
        etoilesBox.setAlignment(Pos.CENTER);

        for (int i = 0; i < 3; i++) {
            Text etoile = new Text("★");
            boolean active = i < nbEtoiles;
            etoile.setFont(Font.font("Arial Black", 55));
            etoile.setFill(active ? Color.GOLD : Color.web("#555555"));
            if (active) {
                etoile.setEffect(new Glow(0.8));
                // animation decalee pour chaque etoile active
                final int delay = i * 200;
                PauseTransition wait = new PauseTransition(Duration.millis(delay));
                wait.setOnFinished(e -> {
                    ScaleTransition pop = new ScaleTransition(Duration.millis(400), etoile);
                    pop.setFromX(0); pop.setFromY(0);
                    pop.setToX(1);   pop.setToY(1);
                    pop.setInterpolator(javafx.animation.Interpolator.EASE_OUT);
                    pop.play();
                });
                wait.play();
                etoile.setScaleX(0);
                etoile.setScaleY(0);
            }
            etoilesBox.getChildren().add(etoile);
        }

        // message selon nombre d etoiles
        String msg = nbEtoiles == 3 ? "Parfait"                  : nbEtoiles == 2 ? "Excellent travail !"
                   : "Niveau complete !";
        Text msgText = new Text(msg);
        msgText.setFont(Font.font("Arial Black", 16));
        Color msgColor = nbEtoiles == 3 ? Color.web("#2ecc71")
                       : nbEtoiles == 2 ? Color.web("#f39c12")
                       : Color.LIGHTGRAY;
        msgText.setFill(msgColor);

        VBox etoilesVBox = new VBox(6, etoilesBox, msgText);
        etoilesVBox.setAlignment(Pos.CENTER);
        contenu.getChildren().add(etoilesVBox);
    }

    private void addScorePanel(VBox contenu) {
        VBox panel = new VBox(10);
        panel.setAlignment(Pos.CENTER);
        panel.setPadding(new Insets(16, 40, 16, 40));
        panel.setStyle(
            "-fx-background-color: rgba(15,52,96,0.85);" +
            "-fx-background-radius: 20;" +
            "-fx-border-color: gold;" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 20;"
        );
        panel.setMaxWidth(380);

        Text scoreTitre = new Text("RESULTATS");
        scoreTitre.setFont(Font.font("Arial Black", 14));
        scoreTitre.setFill(Color.GOLD);

        Rectangle sep = new Rectangle(300, 1, Color.web("#0f3460"));

        HBox ligneScore = ligneStat("Score final", String.valueOf(score), Color.GOLD);
        HBox ligneCoups = ligneStat("Coups joues", String.valueOf(coups), Color.WHITE);

        panel.getChildren().addAll(scoreTitre, sep, ligneScore, ligneCoups);
        contenu.getChildren().add(panel);
    }

    private HBox ligneStat(String label, String valeur, Color couleurValeur) {
        HBox ligne = new HBox();
        ligne.setAlignment(Pos.CENTER);
        ligne.setSpacing(10);
        ligne.setPrefWidth(300);
        Text lblText = new Text(label + " :");
        lblText.setFont(Font.font("Arial", 14));
        lblText.setFill(Color.LIGHTGRAY);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Text valText = new Text(valeur);
        valText.setFont(Font.font("Arial Black", 17));
        valText.setFill(couleurValeur);
        ligne.getChildren().addAll(lblText, spacer, valText);
        return ligne;
    }

    private void addMenu(VBox contenu) {
        VBox menu = new VBox(10);
        menu.setAlignment(Pos.CENTER);

        if (niveau == 0) {
            Button editBtn = createButton("EDITER UN AUTRE PLATEAU", "#27ae60");
            editBtn.setOnAction(e -> { stopMusic(); Window.showEditeur(); });
            Button classicBtn = createButton("NIVEAUX CLASSIQUES", "#8e44ad");
            classicBtn.setOnAction(e -> { stopMusic(); Window.showLevelSelect(TOTAL_LEVELS); });
            menu.getChildren().addAll(editBtn, classicBtn);
        } else if (niveau == -1) {
            Button replayParaBtn = createButton("REJOUER PARABOX", "#c0392b");
            replayParaBtn.setOnAction(e -> {
                stopMusic();
                Window.loadLevelRec("plateau_rec/niveau_rec.txt");
            });
            menu.getChildren().add(replayParaBtn);
        } else if (niveau < TOTAL_LEVELS) {
            Button nextBtn = createButton("NIVEAU SUIVANT ", "#c0392b");
            nextBtn.setOnAction(e -> { stopMusic(); Window.loadLevel(niveau + 1); });
            menu.getChildren().add(nextBtn);
        }

        Button replayBtn = createButton("REJOUER", "#0f3460");
        replayBtn.setOnAction(e -> {
            stopMusic();
            if (niveau == 0) Window.showEditeur();
            else if (niveau == -1) Window.loadLevelRec("plateau_rec/niveau_rec.txt");
            else Window.loadLevel(niveau);
        });

        Button menuBtn = createButton("MENU PRINCIPAL", "#555");
        menuBtn.setOnAction(e -> { stopMusic(); Window.showMenu(); });

        Button quitBtn = createButton("QUITTER", "#333");
        quitBtn.setOnAction(e -> System.exit(0));

        menu.getChildren().addAll(replayBtn, menuBtn, quitBtn);
        contenu.getChildren().add(menu);
    }

    private void playMusic() {
        try {
            String uri = resolveUri("/ressources/win.wav", "ressources/win.wav");
            Media media = new Media(uri);
            victoryMusic = new MediaPlayer(media);
            victoryMusic.setVolume(0.5);
            victoryMusic.play();
        } catch (Exception e) {
            System.out.println("musique de victoire introuvable : " + e.getMessage());
        }
    }

    private void stopMusic() {
        if (victoryMusic != null) {
            victoryMusic.stop();
            victoryMusic.dispose();
            victoryMusic = null;
        }
    }

    private String resolveUri(String classpathPath, String filePath) {
        var url = getClass().getResource(classpathPath);
        if (url != null) return url.toExternalForm();
        var file = new java.io.File(filePath);
        if (file.exists()) return file.toURI().toString();
        throw new IllegalStateException("fichier introuvable : " + filePath);
    }

    private static Button createButton(String text, String couleur) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Arial Black", 15));
        btn.setPrefSize(260, 46);
        String normal = "-fx-background-color:" + couleur
                + ";-fx-background-radius:15;-fx-text-fill:white;";
        String hover  = "-fx-background-color:derive(" + couleur + ",25%)"
                + ";-fx-background-radius:15;-fx-text-fill:white;";
        btn.setStyle(normal);
        btn.setEffect(new DropShadow(8, Color.BLACK));
        btn.setOnMouseEntered(e -> { btn.setStyle(hover);  btn.setScaleX(1.04); btn.setScaleY(1.04); });
        btn.setOnMouseExited(e  -> { btn.setStyle(normal); btn.setScaleX(1.0);  btn.setScaleY(1.0);  });
        return btn;
    }
}
