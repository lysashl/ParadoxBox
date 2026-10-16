package ig;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Label;
import javafx.util.Duration;

/**
 * gestion du temps de jeu
 * met a jour un affichage texte chaque seconde
 * calcule temps ecoule depuis debut
 * utilise pour afficher duree dune partie
 */
public class GameTimer {

    private final Label display;
    private Timeline timeline;
    private int secondsElapsed = 0;

    /**
     * cree un timer associe a un affichage
     * @param display composant affichage du temps
     */
    public GameTimer(Label display) {
        this.display = display;
    }

    /**
     * demarre le timer
     * reinitialise temps ecoule
     * met a jour affichage chaque seconde
     */
    public void start() {
        stop();
        secondsElapsed = 0;
        display.setText("00:00");

        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            secondsElapsed++;
            display.setText(format(secondsElapsed));
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    /**
     * arrete le timer
     * stop mise a jour du temps
     */
    public void stop() {
        if (timeline != null) {
            timeline.stop();
            timeline = null;
        }
    }

    /**
     * reinitialise le timer
     * relance a zero
     */
    public void reset() {
        start();
    }

    /**
     * retourne temps ecoule en secondes
     * @return temps en secondes
     */
    public int getSecondsElapsed() {
        return secondsElapsed;
    }

    /**
     * convertit temps en secondes en format minute seconde
     * @param seconds temps en secondes
     * @return chaine formattee minute seconde
     */
    private static String format(int seconds) {
        return String.format("%02d:%02d", seconds / 60, seconds % 60);
    }
}
