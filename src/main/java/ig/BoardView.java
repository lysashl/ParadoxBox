package ig;

import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import modele.*;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * composant graphique du plateau de jeu
 * gere affichage complet du niveau
 * separe affichage en couche statique et dynamique
 * couche statique contient sol murs cibles
 * couche dynamique contient joueur et caisses
 * gere redimensionnement automatique
 * gere animation des deplacements
 */
public class BoardView extends Pane {

    private static final Duration MOVE_DURATION   = Duration.millis(180);
    private static final Duration BOUNCE_DURATION = Duration.millis(120);
    private static final double   BOUNCE_SCALE    = 1.05;

    private final Image IMG_JOUEUR;
    private final Image IMG_CAISSE;
    private final Image IMG_MUR;
    private final Image IMG_CIBLE;

    private final Configuration config;
    private final Pane backgroundLayer = new Pane();
    private final Pane dynamicLayer    = new Pane();
    private final Map<Caisse, ImageView> caisseNodes = new HashMap<>();
    private ImageView joueurNode;

    /**
     * cree une vue du plateau a partir dune configuration
     * initialise images ressources graphiques
     * initialise couches affichage
     * ajoute listeners sur taille pour redessiner plateau
     * effectue premier affichage complet
     * @param config configuration du jeu contenant plateau joueur et caisses
     */
    public BoardView(Configuration config) {
        this.config = config;

        IMG_JOUEUR = loadImage("/ressources/joueur.png", "ressources/joueur.png");
        IMG_CAISSE = loadImage("/ressources/caisse.png", "ressources/caisse.png");
        IMG_MUR    = loadImage("/ressources/mur.png",    "ressources/mur.png");
        IMG_CIBLE  = loadImage("/ressources/cible.png",  "ressources/cible.png");

        getChildren().addAll(backgroundLayer, dynamicLayer);

        widthProperty().addListener((obs, o, n)  -> drawStaticBoard());
        heightProperty().addListener((obs, o, n) -> drawStaticBoard());

        drawStaticBoard();
        drawDynamic(true, null);
    }

    /**
     * met a jour affichage des elements mobiles
     * declenche animation des deplacements
     * execute une action apres fin des animations
     * @param afterAnimation action executee apres animations peut etre null
     */
    public void drawBoard(Runnable afterAnimation) {
        drawDynamic(false, afterAnimation);
    }

    /**
     * dessine tous les elements fixes du plateau
     * cree fond des cases
     * affiche murs
     * affiche cibles
     * repositionne elements mobiles selon nouvelle taille
     */
    public void drawStaticBoard() {
        backgroundLayer.getChildren().clear();
        double cs = cellSize();

        for (int y = 0; y < config.getY(); y++) {
            for (int x = 0; x < config.getX(); x++) {
                Position pos = new Position(x, y);

                Rectangle cell = new Rectangle(x * cs, y * cs, cs, cs);
                cell.setFill(Color.web("#ECEFF4"));
                cell.setStroke(Color.web("#4C566A"));
                cell.setArcWidth(12);
                cell.setArcHeight(12);
                backgroundLayer.getChildren().add(cell);

                Type type = config.getPlateau().get(pos).getType();
                if (type == Type.MUR)
                    backgroundLayer.getChildren().add(makeNode(IMG_MUR, x, y, cs));

                if (config.estCible(pos))
                    backgroundLayer.getChildren().add(makeNode(IMG_CIBLE, x, y, cs));
            }
        }

        repositionDynamicNodes(cs);
    }

    /**
     * gere affichage et animation des elements mobiles
     * cree noeuds si premiere fois
     * anime deplacement si deja existant
     * synchronise fin animations
     * @param init indique si premier affichage
     * @param afterAnimation action executee apres animations peut etre null
     */
    private void drawDynamic(boolean init, Runnable afterAnimation) {
        double cs = cellSize();
        AtomicInteger pending = new AtomicInteger(0);

        for (Caisse c : config.getCaisses()) {
            ImageView node = caisseNodes.get(c);
            if (node == null) {
                node = makeNode(IMG_CAISSE, c.getPosition().getX(), c.getPosition().getY(), cs);
                caisseNodes.put(c, node);
                dynamicLayer.getChildren().add(node);
            } else if (!init) {
                pending.incrementAndGet();
                final ImageView fNode = node;
                animateNode(fNode, c.getPosition(), cs, () -> {
                    if (pending.decrementAndGet() == 0 && afterAnimation != null)
                        afterAnimation.run();
                });
            }
        }

        Joueur joueur = config.getJoueur();
        if (joueurNode == null) {
            joueurNode = makeNode(IMG_JOUEUR, joueur.getPosition().getX(), joueur.getPosition().getY(), cs);
            joueurNode.setEffect(new Glow(0.4));
            dynamicLayer.getChildren().add(joueurNode);
        } else if (!init) {
            pending.incrementAndGet();
            animateNode(joueurNode, joueur.getPosition(), cs, () -> {
                if (pending.decrementAndGet() == 0 && afterAnimation != null)
                    afterAnimation.run();
            });
        }

        if (init || pending.get() == 0) {
            if (afterAnimation != null) afterAnimation.run();
        }
    }

    /**
     * calcule taille dune case en fonction de la taille du composant
     * conserve proportion carre
     * retourne valeur par defaut si taille invalide
     * @return taille dune case
     */
    private double cellSize() {
        double w = getWidth()  / config.getX();
        double h = getHeight() / config.getY();
        double cs = Math.min(w, h);
        return cs <= 0 ? 50 : cs;
    }

    /**
     * met a jour position et taille des elements mobiles
     * utilise lors du redimensionnement
     * @param cs taille des cases
     */
    private void repositionDynamicNodes(double cs) {
        caisseNodes.forEach((caisse, node) -> {
            node.setFitWidth(cs);
            node.setFitHeight(cs);
            node.setLayoutX(caisse.getPosition().getX() * cs);
            node.setLayoutY(caisse.getPosition().getY() * cs);
        });
        if (joueurNode != null) {
            Position jp = config.getJoueur().getPosition();
            joueurNode.setFitWidth(cs);
            joueurNode.setFitHeight(cs);
            joueurNode.setLayoutX(jp.getX() * cs);
            joueurNode.setLayoutY(jp.getY() * cs);
        }
    }

    /**
     * cree un noeud graphique pour un element du jeu
     * applique taille position et effet visuel
     * @param img image utilisee
     * @param x position horizontale
     * @param y position verticale
     * @param cs taille case
     * @return noeud graphique pret a etre ajoute
     */
    private ImageView makeNode(Image img, int x, int y, double cs) {
        ImageView node = new ImageView(img);
        node.setFitWidth(cs);
        node.setFitHeight(cs);
        node.setLayoutX(x * cs);
        node.setLayoutY(y * cs);

        DropShadow shadow = new DropShadow();
        shadow.setRadius(10);
        shadow.setOffsetY(3);
        node.setEffect(shadow);
        return node;
    }

    /**
     * anime deplacement dun element vers une nouvelle position
     * applique translation puis effet rebond
     * execute action a la fin
     * @param node element graphique
     * @param target position cible
     * @param cs taille case
     * @param onFinished action executee apres animation peut etre null
     */
    private void animateNode(ImageView node, Position target, double cs, Runnable onFinished) {
        double targetX = target.getX() * cs;
        double targetY = target.getY() * cs;

        TranslateTransition move = new TranslateTransition(MOVE_DURATION, node);
        move.setToX(targetX - node.getLayoutX());
        move.setToY(targetY - node.getLayoutY());
        move.setOnFinished(e -> {
            node.setLayoutX(targetX);
            node.setLayoutY(targetY);
            node.setTranslateX(0);
            node.setTranslateY(0);

            ScaleTransition bounce = new ScaleTransition(BOUNCE_DURATION, node);
            bounce.setFromX(BOUNCE_SCALE);
            bounce.setFromY(BOUNCE_SCALE);
            bounce.setToX(1.0);
            bounce.setToY(1.0);
            bounce.setOnFinished(ev -> {
                if (onFinished != null) onFinished.run();
            });
            bounce.play();
        });
        move.play();
    }

    /**
     * charge une image depuis ressources internes ou fichier local
     * lance erreur si image introuvable
     * @param classpathPath chemin dans ressources
     * @param filePath chemin fichier local
     * @return image chargee
     */
    private Image loadImage(String classpathPath, String filePath) {
        InputStream stream = getClass().getResourceAsStream(classpathPath);
        if (stream != null) return new Image(stream);
        File file = new File(filePath);
        if (file.exists()) return new Image(file.toURI().toString());
        throw new IllegalStateException("Image introuvable : " + classpathPath + " / " + filePath);
    }
}
