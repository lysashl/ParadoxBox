package ig;

import chemins.CheminClassique;
import javafx.animation.PauseTransition;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.util.Duration;
import modele.Configuration;
import modele.Direction;
import modele.Position;
import resolutionautomatique.IndiceSolver;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * gestionnaire des entrees utilisateur
 * controle interactions clavier souris et boutons
 * gere deplacement joueur historique victoire et defaite
 * touche I : affiche un indice via IndiceSolver
 */
public class InputHandler {

    private final Configuration config;
    private final int niveau;
    private final BoardView boardView;
    private final Label coupsLabel;
    private final Label scoreLabel;
    private final Label progressLabel;
    private final javafx.scene.shape.Rectangle barreRemplie;
    private final javafx.scene.control.Label   barreLabel;
    private final int                          totalCaisses;
    private final Pane root;

    private final Deque<ConfigurationSnapshot> history = new ArrayDeque<>();
    private boolean busy  = false;
    private boolean pause = false;
    private Runnable onVictory;
    private Runnable onDefaite;

    // label d indice affiche temporairement
    private Label indiceLabel;

    public InputHandler(Configuration config,
                        int niveau,
                        BoardView boardView,
                        Button resetBtn,
                        Button undoBtn,
                        Button quitBtn,
                        Label coupsLabel,
                        Label scoreLabel,
                        Label progressLabel,
                        javafx.scene.shape.Rectangle barreRemplie,
                        javafx.scene.control.Label barreLabel,
                        int totalCaisses,
                        Pane root) {

        this.config     = config;
        this.niveau     = niveau;
        this.boardView  = boardView;
        this.coupsLabel    = coupsLabel;
        this.scoreLabel    = scoreLabel;
        this.progressLabel = progressLabel;
        this.barreRemplie  = barreRemplie;
        this.barreLabel    = barreLabel;
        this.totalCaisses  = totalCaisses;
        this.root          = root;

        saveSnapshot();

        boardView.setFocusTraversable(true);
        boardView.requestFocus();
        root.setOnMouseClicked(e -> boardView.requestFocus());

        boardView.setOnMouseClicked(this::handleClick);
        boardView.addEventHandler(KeyEvent.KEY_PRESSED, this::handleKey);

        resetBtn.setOnAction(e -> { resetToFirst(); boardView.requestFocus(); });
        undoBtn.setOnAction(e  -> { undo();         boardView.requestFocus(); });
        quitBtn.setOnAction(e  -> System.exit(0));
    }

    public void setOnVictory(Runnable onVictory) { this.onVictory = onVictory; }
    public void setPause(boolean pause) { this.pause = pause; }
    public void setOnDefaite(Runnable onDefaite)  { this.onDefaite = onDefaite; }

    private void handleClick(MouseEvent event) {
        if (busy || pause) return;

        double cs = Math.min(
            boardView.getWidth()  / config.getX(),
            boardView.getHeight() / config.getY()
        );

        int cx = (int) (event.getX() / cs);
        int cy = (int) (event.getY() / cs);

        if (cx < 0 || cx >= config.getX() || cy < 0 || cy >= config.getY()) return;

        Position cible = new Position(cx, cy);
        if (cible.equals(config.getJoueur().getPosition())) return;
        if (!config.estVide(cible)) return;

        CheminClassique bfs = new CheminClassique(config);
        List<Position> chemin = bfs.trouverChemin(config.getJoueur().getPosition(), cible);
        if (chemin.isEmpty()) return;

        animerChemin(chemin, 1);
    }

    private void animerChemin(List<Position> chemin, int index) {
        if (index >= chemin.size()) {
            busy = false;
            boardView.requestFocus();
            return;
        }

        busy = true;

        Position actuelle = config.getJoueur().getPosition();
        Position suivante  = chemin.get(index);

        Direction dir = directionVers(actuelle, suivante);
        if (dir == null) { busy = false; return; }

        boolean moved = config.deplacerJoueur(dir);
        if (!moved) { busy = false; return; }

        saveSnapshot();

        boardView.drawBoard(() -> {
            updateLabels();

            if (config.victoire() && onVictory != null) {
                busy = false;
                updateLabels();
                PauseTransition wait = new PauseTransition(Duration.millis(600));
                wait.setOnFinished(ev -> onVictory.run());
                wait.play();
                return;
            }

            if (config.estPerdu() && onDefaite != null) {
                busy = false;
                onDefaite.run();
                return;
            }

            PauseTransition pause = new PauseTransition(Duration.millis(80));
            pause.setOnFinished(e -> animerChemin(chemin, index + 1));
            pause.play();
        });
    }

    private void handleKey(KeyEvent event) {
        if (busy || pause) return;

        // ctrl+z undo
        if (event.getCode() == KeyCode.Z && event.isControlDown()) {
            undo();
            boardView.requestFocus();
            return;
        }

        // touche I afficher un indice
        if (event.getCode() == KeyCode.I) {
            afficherIndice();
            boardView.requestFocus();
            return;
        }

        Direction dir = directionFromKey(event.getCode());
        if (dir == null) return;

        boolean moved = config.deplacerJoueur(dir);
        if (!moved) return;

        saveSnapshot();
        busy = true;

        boardView.drawBoard(() -> {
            updateLabels();
            busy = false;
            boardView.requestFocus();

            if (config.victoire() && onVictory != null) {
                updateLabels();
                PauseTransition wait = new PauseTransition(Duration.millis(600));
                wait.setOnFinished(ev -> onVictory.run());
                wait.play();
                return;
            }

            if (config.estPerdu() && onDefaite != null) {
                onDefaite.run();
            }
        });
    }

    /**
     * calcule et affiche un indice via IndiceSolver
     * montre la prochaine direction a prendre pendant 2 secondes
     */
    private void afficherIndice() {
        // afficher immediatement "calcul en cours" pour le retour visuel
        afficherMessage("Calcul de l'indice...");

        // lancer le solver dans un thread separe pour ne pas bloquer JavaFX
        new Thread(() -> {
            try {
                IndiceSolver solver = new IndiceSolver(config);
                Direction indice = solver.getIndice();

                String msg;
                if (indice == null) {
                    msg = "Aucune solution trouvee !";
                } else if (indice == Direction.HAUT)   {
                    msg = "Indice : allez vers le HAUT (Z)";
                } else if (indice == Direction.BAS)    {
                    msg = "Indice : allez vers le BAS (S)";
                } else if (indice == Direction.GAUCHE) {
                    msg = "Indice : allez vers la GAUCHE (Q)";
                } else {
                    msg = "Indice : allez vers la DROITE (D)";
                }

                // revenir sur le thread javafx pour mettre a jour l interface
                final String finalMsg = msg;
                javafx.application.Platform.runLater(() -> afficherMessage(finalMsg));

            } catch (Exception e) {
                javafx.application.Platform.runLater(() ->
                        afficherMessage("Calcul de l'indice impossible"));
            }
        }).start();
    }

    /**
     * affiche un message temporaire sur le plateau pendant 2 secondes
     */
    private void afficherMessage(String msg) {
        // supprimer l ancien indice si present
        if (indiceLabel != null) {
            boardView.getChildren().remove(indiceLabel);
            indiceLabel = null;
        }

        indiceLabel = new Label(msg);
        indiceLabel.setStyle(
            "-fx-background-color: rgba(0,0,0,0.75);" +
            "-fx-text-fill: gold;" +
            "-fx-font-size: 16px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 10 20 10 20;" +
            "-fx-background-radius: 12;"
        );
        indiceLabel.setLayoutX(20);
        indiceLabel.setLayoutY(20);
        // ajouter dans boardView qui est un Pane setLayoutX/Y fonctionne
        boardView.getChildren().add(indiceLabel);

        PauseTransition pause = new PauseTransition(Duration.seconds(3));
        pause.setOnFinished(e -> {
            boardView.getChildren().remove(indiceLabel);
            indiceLabel = null;
        });
        pause.play();
    }

    private void undo() {
        if (history.size() <= 1) return;
        history.pollLast();
        history.peekLast().restore(config);
        refreshAfterHistoryChange();
    }

    private void resetToFirst() {
        if (history.isEmpty()) return;
        ConfigurationSnapshot first = history.peekFirst();
        history.clear();
        history.addFirst(first);
        first.restore(config);
        refreshAfterHistoryChange();
    }

    private void saveSnapshot() {
        history.addLast(new ConfigurationSnapshot(config));
    }

    private void refreshAfterHistoryChange() {
        boardView.drawBoard(null);
        updateLabels();
    }

    private void updateLabels() {
        coupsLabel.setText("Coups : " + config.getCoups());
        scoreLabel.setText("Score : " + config.getScore());

        // progression caisses sur cibles
        int surCible = 0;
        for (modele.Caisse c : config.getCaisses()) {
            if (config.estCible(c.getPosition())) surCible++;
        }
        int total = config.getCaisses().size();

        // texte
        progressLabel.setText("Caisses : " + surCible + "/" + total);
        progressLabel.setTextFill(surCible == total
                ? javafx.scene.paint.Color.web("#2ecc71")
                : javafx.scene.paint.Color.WHITE);

        // barre visuelle 
        double ratio = total > 0 ? (double) surCible / total : 0;
        double largeurMax = 166;
        barreRemplie.setWidth(ratio * largeurMax);
        barreRemplie.setFill(surCible == total
                ? javafx.scene.paint.Color.web("#2ecc71")
                : javafx.scene.paint.Color.web("#3498db"));
        barreLabel.setText(surCible + "/" + total);
    }

    private static Direction directionVers(Position de, Position vers) {
        int dx = vers.getX() - de.getX();
        int dy = vers.getY() - de.getY();
        if (dx ==  1 && dy == 0) return Direction.DROITE;
        if (dx == -1 && dy == 0) return Direction.GAUCHE;
        if (dx == 0  && dy == 1) return Direction.BAS;
        if (dx == 0  && dy ==-1) return Direction.HAUT;
        return null;
    }

    private static Direction directionFromKey(KeyCode code) {
        return switch (code) {
            case Z, UP    -> Direction.HAUT;
            case S, DOWN  -> Direction.BAS;
            case Q, LEFT  -> Direction.GAUCHE;
            case D, RIGHT -> Direction.DROITE;
            default       -> null;
        };
    }
}
