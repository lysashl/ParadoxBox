package ig;

import javafx.stage.Stage;
import modele.*;

/**
 * gestionnaire central de navigation
 * controle les transitions entre les differentes scenes
 */
public final class Window {

    private static Stage stage;

    private Window() {}

    public static void start(Stage primaryStage) {
        stage = primaryStage;
        showMenu();
        stage.show();
    }

    public static void showMenu() {
        PremierEcran.stopMusic();
        stage.setScene(PremierEcran.createScene(stage));
    }

    public static void showLevelSelect(int totalLevels) {
        stage.setScene(SelectionNiveau.createScene(stage, totalLevels));
    }

    public static void showEditeur() {
        stage.setScene(new EditeurScene(stage, null).build());
    }

    public static void loadLevel(int niveau) {
        try {
            String path = "plateau/niveau" + niveau + ".txt";
            Plateau plateau = new Plateau(path);
            Configuration config = new Configuration(plateau, niveau);
            stage.setScene(new GameScene(stage, config, niveau).build());
        } catch (Exception e) {
            e.printStackTrace();
            showLevelSelect(5);
        }
    }

    public static void loadLevelRec(String cheminFichier) {
        try {
            PlateauRec plateauRec = new PlateauRec(cheminFichier);
            // passer le chemin au constructeur pour la sauvegarde
            stage.setScene(new GameSceneRec(stage, plateauRec, cheminFichier).build());
        } catch (Exception e) {
            e.printStackTrace();
            showLevelSelect(5);
        }
    }

    /**
     * ouvre l editeur de niveau recursif
     */
    public static void showEditeurRec() {
        try {
            PlateauRec plateauRec = new PlateauRec("plateau/niveauRec1.txt");
            stage.setScene(new EditeurSceneRec(stage, plateauRec).build());
        } catch (Exception e) {
            e.printStackTrace();
            showLevelSelect(5);
        }
    }

    /**
     * affiche l ecran de victoire avec score et coups
     */
    public static void showVictory(int niveau, int score, int coups) {
        stage.setScene(new Victory(stage, niveau, score, coups).createScene());
    }

    public static void showDefaite(int niveau) {
        stage.setScene(new Defaite(stage, niveau).createScene());
    }

    public static Stage getStage() {
        return stage;
    }
}
