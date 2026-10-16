package ig;

import javafx.animation.*;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;

/**
 * ecran principal du jeu
 * menu avec logo, boutons et musique
 */
public class PremierEcran {

    private static final int TOTAL_LEVELS = 5;
    private static MediaPlayer musicPlayer;

    private PremierEcran() {}

    public static Scene createScene(Stage stage) {
        StackPane root = new StackPane();

        // image de fond avec zoom lent
        try {
            ImageView bg = new ImageView(new Image(PremierEcran.class.getResource("/ressources/background.png").toExternalForm()));
            bg.setPreserveRatio(false);
            bg.fitWidthProperty().bind(root.widthProperty());
            bg.fitHeightProperty().bind(root.heightProperty());
            root.getChildren().add(bg);

            ScaleTransition zoom = new ScaleTransition(Duration.seconds(20), bg);
            zoom.setFromX(1.0); zoom.setFromY(1.0);
            zoom.setToX(1.06);  zoom.setToY(1.06);
            zoom.setAutoReverse(true);
            zoom.setCycleCount(Animation.INDEFINITE);
            zoom.play();
        } catch (Exception ignored) {
            root.setStyle("-fx-background-color: #1a1a2e;");
        }

        // overlay sombre subtil
        Rectangle overlay = new Rectangle(900, 650, Color.color(0, 0, 0, 0.45));
        overlay.widthProperty().bind(root.widthProperty());
        overlay.heightProperty().bind(root.heightProperty());
        root.getChildren().add(overlay);

        // contenu principal
        VBox contenu = new VBox(0);
        contenu.setAlignment(Pos.CENTER);

        // logo
        try {
            ImageView logo =  new ImageView(new Image(PremierEcran.class.getResource("/ressources/sokoban.png").toExternalForm()));
            logo.setPreserveRatio(true);
            logo.setFitWidth(420);
            VBox.setMargin(logo, new javafx.geometry.Insets(0, 0, 30, 0));

            // animation pulse sur le logo
            ScaleTransition pulse = new ScaleTransition(Duration.seconds(2), logo);
            pulse.setFromX(1.0); pulse.setFromY(1.0);
            pulse.setToX(1.04);  pulse.setToY(1.04);
            pulse.setAutoReverse(true);
            pulse.setCycleCount(Animation.INDEFINITE);
            pulse.play();

            contenu.getChildren().add(logo);
        } catch (Exception ignored) {
            // fallback texte si image absente
            Text titre = new Text("SOKOBAN");
            titre.setFont(Font.font("Arial Black", 72));
            titre.setFill(Color.GOLD);
            titre.setEffect(new DropShadow(30, Color.web("#0f3460")));
            VBox.setMargin(titre, new javafx.geometry.Insets(0, 0, 30, 0));
            contenu.getChildren().add(titre);
        }

        // boutons
        VBox boutons = new VBox(14);
        boutons.setAlignment(Pos.CENTER);
        contenu.setTranslateY(-60);

        Button jouerBtn    = createButton("JOUER",           "#0f3460", "white", true);
        Button editBtn     = createButton("EDITER CLASSIQUE","rgba(255,215,0,0.85)", "black", false);
        Button editRecBtn  = createButton("EDITER PARABOX",  "rgba(231,76,60,0.85)", "white", false);
        Button quitBtn     = createButton("QUITTER",         "rgba(60,60,60,0.85)", "white", false);

        jouerBtn.setOnAction(e -> {
            stopMusic();
            Window.showLevelSelect(TOTAL_LEVELS);
        });
        editBtn.setOnAction(e -> {
            stopMusic();
            Window.showEditeur();
        });
        editRecBtn.setOnAction(e -> {
            stopMusic();
            Window.showEditeurRec();
        });
        quitBtn.setOnAction(e -> System.exit(0));

        boutons.getChildren().addAll(jouerBtn, editBtn, editRecBtn, quitBtn);
        contenu.getChildren().add(boutons);

        root.getChildren().add(contenu);

        playMusic();

        return new Scene(root, 900, 650);
    }

    public static void stopMusic() {
        if (musicPlayer != null) {
            musicPlayer.stop();
            musicPlayer.dispose();
            musicPlayer = null;
        }
    }

    private static Button createButton(String text, String bgColor,
                                        String textColor, boolean principal) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Arial Black", principal ? 22 : 18));
        btn.setPrefSize(principal ? 300 : 260, principal ? 62 : 52);

        String normal = "-fx-background-color: " + bgColor + ";" +
                        "-fx-background-radius: 16;" +
                        "-fx-text-fill: " + textColor + ";";
        String hover  = "-fx-background-color: derive(" + bgColor + ", 20%);" +
                        "-fx-background-radius: 16;" +
                        "-fx-text-fill: " + textColor + ";";

        btn.setStyle(normal);
        btn.setEffect(new DropShadow(12, Color.web("#000000", 0.5)));

        btn.setOnMouseEntered(e -> {
            btn.setStyle(hover);
            btn.setScaleX(1.05); btn.setScaleY(1.05);
        });
        btn.setOnMouseExited(e -> {
            btn.setStyle(normal);
            btn.setScaleX(1.0); btn.setScaleY(1.0);
        });

        return btn;
    }

    private static void playMusic() {
        try {
            String uri = PremierEcran.class.getResource("/ressources/menu.wav").toExternalForm();
            Media media = new Media(uri);
            musicPlayer = new MediaPlayer(media);
            musicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            musicPlayer.setVolume(0.4);
            musicPlayer.play();
        } catch (Exception ignored) {}
    }
}
