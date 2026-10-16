package ig;

import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.util.Random;

/**
 * ecran de defaite du jeu
 * affiche une scene lorsque le joueur bloque une caisse
 * propose de rejouer ou de revenir au menu
 */
public class Defaite {

    private final Stage stage;
    private final int niveau;
private MediaPlayer defaiteSound;
    /**
     * constructeur de l ecran de defaite
     * @param stage fenetre principale
     * @param niveau niveau en cours
     */
    public Defaite(Stage stage, int niveau) {
        this.stage  = stage;
        this.niveau = niveau;
    }

    /**
     * cree la scene de defaite complete
     * @return scene prete a etre affichee
     */
    public Scene createScene() {
        StackPane root = new StackPane();

        addBackground(root);
        addParticules(root);
        addTitre(root);
        addMenu(root);
playSound(); 
        return new Scene(root, 900, 600);
    }
    private void playSound() {
    try {
        String uri = Defaite.class.getResource("/ressources/defaite.wav").toExternalForm();
        Media media = new Media(uri);
        defaiteSound = new MediaPlayer(media);
        defaiteSound.setVolume(0.5);
        defaiteSound.play();
    } catch (Exception e) {
        System.out.println("Son defaite introuvable : " + e.getMessage());
    }
}

    /**
     * ajoute le fond de la scene
     * fond sombre si image absente
     */
    private void addBackground(StackPane root) {
        try {
            ImageView bg = new ImageView(new Image(Defaite.class.getResource("/ressources/background.png").toExternalForm()));
            bg.setFitWidth(900);
            bg.setFitHeight(600);
            bg.setPreserveRatio(false);
            // teinte rouge sombre par dessus
            Rectangle overlay = new Rectangle(900, 600, Color.color(0.5, 0, 0, 0.6));
            root.getChildren().addAll(bg, overlay);
        } catch (Exception e) {
            root.setStyle("-fx-background-color: #2d0000;");
        }
    }

    /**
     * ajoute des particules qui tombent en rouge
     */
    private void addParticules(StackPane root) {
        Pane pane = new Pane();
        Random rng = new Random();

        for (int i = 0; i < 30; i++) {
            Rectangle p = new Rectangle(
                    6 + rng.nextInt(8), 6 + rng.nextInt(8),
                    Color.color(0.8 + rng.nextDouble() * 0.2,
                                rng.nextDouble() * 0.2,
                                rng.nextDouble() * 0.1,
                                0.7 + rng.nextDouble() * 0.3));
            p.setLayoutX(rng.nextInt(900));
            p.setLayoutY(-30);

            TranslateTransition fall = new TranslateTransition(
                    Duration.seconds(2 + rng.nextDouble() * 3), p);
            fall.setFromY(-50);
            fall.setToY(650);
            fall.setCycleCount(Animation.INDEFINITE);
            fall.setDelay(Duration.millis(rng.nextInt(2000)));
            fall.play();

            pane.getChildren().add(p);
        }

        root.getChildren().add(pane);
    }

    /**
     * ajoute le titre game over avec animations
     */
    private void addTitre(StackPane root) {
        VBox titreBox = new VBox(10);
        titreBox.setAlignment(Pos.TOP_CENTER);
        titreBox.setPadding(new Insets(60, 0, 0, 0));

        Text titre = new Text("GAME OVER");
        titre.setFont(Font.font("Arial Black", 90));
        titre.setFill(Color.RED);
        titre.setEffect(new DropShadow(30, Color.DARKRED));

        
        TranslateTransition shake = new TranslateTransition(Duration.millis(80), titre);
        shake.setFromX(-8);
        shake.setToX(8);
        shake.setCycleCount(6);
        shake.setAutoReverse(true);
        shake.play();

        Text sousTitre = new Text("Une caisse est coincée...");
        sousTitre.setFont(Font.font("Arial", 24));
        sousTitre.setFill(Color.LIGHTGRAY);

        titreBox.getChildren().addAll(titre, sousTitre);
        StackPane.setAlignment(titreBox, Pos.TOP_CENTER);
        root.getChildren().add(titreBox);
    }

    /**
     * ajoute les boutons rejouer et menu
     */
    private void addMenu(StackPane root) {
        Button rejouerBtn = createButton("REJOUER", "#c0392b");
        Button menuBtn    = createButton("MENU",    "#555555");

        rejouerBtn.setOnAction(e -> {
    if (defaiteSound != null) defaiteSound.stop();
    if (niveau == 0) Window.showMenu(); 
    else Window.loadLevel(niveau);
});
menuBtn.setOnAction(e -> {
    if (defaiteSound != null) defaiteSound.stop();
    Window.showMenu();
});
        VBox menu = new VBox(20, rejouerBtn, menuBtn);
        menu.setAlignment(Pos.CENTER);

        root.getChildren().add(menu);
    }

    /**
     * cree un bouton pour l ecran de defaite
     * @param text texte du bouton
     * @param couleur couleur de fond
     * @return bouton configure
     */
    private static Button createButton(String text, String couleur) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Arial Black", 22));
        btn.setPrefSize(260, 60);

        String normal = "-fx-background-color: " + couleur + "; -fx-background-radius: 16; -fx-text-fill: white;";
        String hover  = "-fx-background-color: derive(" + couleur + ", 30%); -fx-background-radius: 16; -fx-text-fill: white;";

        btn.setStyle(normal);
        btn.setOnMouseEntered(e -> btn.setStyle(hover));
        btn.setOnMouseExited(e  -> btn.setStyle(normal));

        return btn;
    }
}
