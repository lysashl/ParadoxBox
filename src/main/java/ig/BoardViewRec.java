package ig;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import modele.*;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * vue du plateau recursif
 * backgroundlayer pour elements statiques
 * dynamiclayer translate  pour le joueur
 * Imageview dans dynamiclayer relatif a son parent pas de clipping absolu
 */
public class BoardViewRec extends Pane {

    private static final int    MAX_PROFONDEUR = 4;
    private static final double MARGE_EXT      = 0.25;

    private static final Map<Character, String> COULEURS = new HashMap<>();
    static {
        COULEURS.put('A', "#E74C3C");
        COULEURS.put('B', "#3498DB");
        COULEURS.put('C', "#2ECC71");
        COULEURS.put('D', "#F39C12");
        COULEURS.put('E', "#9B59B6");
        COULEURS.put('F', "#1ABC9C");
    }

    private final Image IMG_JOUEUR;
    private final Image IMG_CAISSE;
    private final Image IMG_MUR;
    private final Image IMG_CIBLE;

    private final Pane backgroundLayer = new Pane();
    private final Pane dynamicLayer    = new Pane();
    private ImageView  joueurNode      = null;

    private ConfigurationRec config;
    private CaisseMonde      caisseCourante;
    private ConfigurationRec configParente;

    public BoardViewRec(ConfigurationRec config,
                        CaisseMonde caisseCourante,
                        ConfigurationRec configParente) {
        this.config         = config;
        this.caisseCourante = caisseCourante;
        this.configParente  = configParente;

        IMG_JOUEUR = loadImage("/ressources/joueur.png", "ressources/joueur.png");
        IMG_CAISSE = loadImage("/ressources/caisse.png", "ressources/caisse.png");
        IMG_MUR    = loadImage("/ressources/mur.png",    "ressources/mur.png");
        IMG_CIBLE  = loadImage("/ressources/cible.png",  "ressources/cible.png");

        getChildren().addAll(backgroundLayer, dynamicLayer);

        // backgroundLayer couvre tout
        backgroundLayer.prefWidthProperty().bind(widthProperty());
        backgroundLayer.prefHeightProperty().bind(heightProperty());

        // dynamicLayer n a pas de taille fixe  il est translate ses enfants sont en coordonnees relatives a lui donc pas de clipping absolu
        dynamicLayer.setMouseTransparent(true);
        dynamicLayer.setManaged(false);

        layoutBoundsProperty().addListener((obs, o, n) -> dessiner());
    }

    public void setConfig(ConfigurationRec config,
                          CaisseMonde caisseCourante,
                          ConfigurationRec configParente) {
        this.config         = config;
        this.caisseCourante = caisseCourante;
        this.configParente  = configParente;
        joueurNode = null;
        dessiner();
    }

    public void dessiner() {
        double w = getWidth();
        double h = getHeight();
        if (w <= 0 || h <= 0) return;

        double margePixels = 0;
        double cs;

        if (caisseCourante != null && configParente != null) {
            cs = Math.min(
                    w / (config.getX() + 2 * MARGE_EXT),
                    h / (config.getY() + 2 * MARGE_EXT));
            margePixels = cs * MARGE_EXT;
        } else {
            cs = Math.min(w / config.getX(), h / config.getY());
        }

        if (cs <= 0) return;

        double totalW = config.getX() * cs + 2 * margePixels;
        double totalH = config.getY() * cs + 2 * margePixels;
        double startX = (w - totalW) / 2 + margePixels;
        double startY = (h - totalH) / 2 + margePixels;

        backgroundLayer.getChildren().clear();
        dynamicLayer.getChildren().clear();

        if (caisseCourante != null && configParente != null)
            dessinerContexteExterieur(startX, startY, cs, margePixels);

        dessinerFond(startX, startY, cs);
        dessinerJoueur(startX, startY, cs);

        // s assurer que dynamicLayer est toujours devant
        dynamicLayer.toFront();
    }
    // fond statique
    
    private void dessinerFond(double startX, double startY, double cs) {
        for (int y = 0; y < config.getY(); y++) {
            for (int x = 0; x < config.getX(); x++) {
                Position pos = new Position(x, y);
                double px = startX + x * cs;
                double py = startY + y * cs;
                Element elem = config.getMonde().get(pos);
                if (elem == null) continue;

                if (elem.getType() == Type.MUR) {
                    backgroundLayer.getChildren().add(rect(IMG_MUR, "#2C3E50", px, py, cs));
                } else {
                    Rectangle cell = new Rectangle(px, py, cs, cs);
                    cell.setFill(Color.web("#ECEFF4"));
                    cell.setStroke(Color.web("#4C566A", 0.4));
                    cell.setArcWidth(Math.min(12, cs * 0.2));
                    cell.setArcHeight(Math.min(12, cs * 0.2));
                    backgroundLayer.getChildren().add(cell);

                    if (config.estCible(pos))
                        backgroundLayer.getChildren().add(rect(IMG_CIBLE, "#F39C12", px, py, cs));

                    if (config.estCaisse(pos)) {
                        CaisseMonde caisse = config.getCaisse(pos);
                        if (caisse.estCaisseSimple())
                            backgroundLayer.getChildren().add(rect(IMG_CAISSE, "#8B4513", px, py, cs));
                        else
                            dessinerCaisseMonde(caisse, px, py, cs);
                    }
                }
            }
        }

        // highlight caissesmondes adjacentes
        Position pj = config.getJoueur().getPosition();
        for (Direction d : Direction.getDirections()) {
            Position v = pj.add(d);
            if (config.estCaisse(v)) {
                CaisseMonde cm = config.getCaisse(v);
                if (!cm.estCaisseSimple()) {
                    double hpx = startX + v.getX() * cs;
                    double hpy = startY + v.getY() * cs;
                    double ep = Math.max(2, cs * 0.06);
                    Rectangle hl = new Rectangle(hpx+ep, hpy+ep, cs-ep*2, cs-ep*2);
                    hl.setFill(Color.TRANSPARENT);
                    hl.setStroke(Color.GOLD);
                    hl.setStrokeWidth(Math.max(2, cs * 0.06));
                    hl.setArcWidth(Math.min(8, cs*0.15));
                    hl.setArcHeight(Math.min(8, cs*0.15));
                    hl.setEffect(new javafx.scene.effect.Glow(0.8));
                    backgroundLayer.getChildren().add(hl);
                }
            }
        }
    }

    /**joueur dans dynamicLayer translate
    * le dynamicLayer est positionne a startX startY
    * le joueur est en coordonnees relatives a ce layer
    * donc pas de clipping absolu meme principe que BoardView classique
    */
    private void dessinerJoueur(double startX, double startY, double cs) {
        Position pj = config.getJoueur().getPosition();

        // positions arrondies au pixel pres 
        double px   = Math.round(pj.getX() * cs);
        double py   = Math.round(pj.getY() * cs);
        double size = Math.round(cs);

        // positionner et dimensionner le layer
        dynamicLayer.setLayoutX(startX);
        dynamicLayer.setLayoutY(startY);
        dynamicLayer.setPrefSize(
                Math.round(config.getX() * cs),
                Math.round(config.getY() * cs));

        // fallback visible si image absente
        if (IMG_JOUEUR == null) {
            Rectangle r = new Rectangle(px, py, size, size);
            r.setFill(Color.web("#2980B9"));
            dynamicLayer.getChildren().add(r);
            return;
        }

        // recreer le joueur a chaque appel
        joueurNode = new ImageView(IMG_JOUEUR);
        joueurNode.setFitWidth(size);
        joueurNode.setFitHeight(size);
        joueurNode.setLayoutX(px);
        joueurNode.setLayoutY(py);
        joueurNode.setSmooth(false);
        joueurNode.setCache(true);

        dynamicLayer.getChildren().add(joueurNode);
    }

    // contexte exterieur
    
    private void dessinerContexteExterieur(double startX, double startY,
                                            double cs, double marge) {
        Position posC = caisseCourante.getPosition();
        int cx = posC.getX();
        int cy = posC.getY();
        char nom = caisseCourante.getMondeNom();
        String couleur = COULEURS.getOrDefault(nom, "#95A5A6");

        double totalW = config.getX() * cs + 2 * marge;
        double totalH = config.getY() * cs + 2 * marge;

        Rectangle bordure = new Rectangle(startX-marge, startY-marge, totalW, totalH);
        bordure.setFill(Color.web(couleur, 0.3));
        bordure.setStroke(Color.web(couleur));
        bordure.setStrokeWidth(Math.max(2, marge * 0.3));
        bordure.setArcWidth(Math.min(12, marge));
        bordure.setArcHeight(Math.min(12, marge));
        backgroundLayer.getChildren().add(bordure);

        int cols = config.getX();
        int rows = config.getY();

        for (int x = 0; x < cols; x++) {
            Position pos = new Position(cx-1+x, cy-1);
            if (configParente.getMonde().inBounds(pos))
                dessinerCaseParente(pos, startX+x*cs, startY-marge, cs, marge);
        }
        for (int x = 0; x < cols; x++) {
            Position pos = new Position(cx-1+x, cy+1);
            if (configParente.getMonde().inBounds(pos))
                dessinerCaseParente(pos, startX+x*cs, startY+rows*cs, cs, marge);
        }
        for (int y = 0; y < rows; y++) {
            Position pos = new Position(cx-1, cy-1+y);
            if (configParente.getMonde().inBounds(pos))
                dessinerCaseParente(pos, startX-marge, startY+y*cs, marge, cs);
        }
        for (int y = 0; y < rows; y++) {
            Position pos = new Position(cx+1, cy-1+y);
            if (configParente.getMonde().inBounds(pos))
                dessinerCaseParente(pos, startX+cols*cs, startY+y*cs, marge, cs);
        }
    }

    private void dessinerCaseParente(Position pos, double px, double py,
                                      double w, double h) {
        Element elem = configParente.getMonde().get(pos);
        if (elem == null) return;
        if (elem.getType() == Type.MUR) {
            Rectangle r = new Rectangle(px, py, w, h);
            r.setFill(IMG_MUR != null
                    ? new ImagePattern(IMG_MUR, 0, 0, 1, 1, true)
                    : Color.web("#2C3E50"));
            backgroundLayer.getChildren().add(r);
        } else {
            Rectangle cell = new Rectangle(px, py, w, h);
            cell.setFill(Color.web("#ECEFF4", 0.7));
            backgroundLayer.getChildren().add(cell);
            if (configParente.estCible(pos)) {
                double r = Math.min(w, h) * 0.18;
                backgroundLayer.getChildren().add(
                        new Circle(px+w/2, py+h/2, r, Color.web("#F39C12")));
            }
            if (configParente.estCaisse(pos)) {
                CaisseMonde c = configParente.getCaisse(pos);
                double b = Math.max(1, Math.min(w,h)*0.06);
                Rectangle rc = new Rectangle(px+b, py+b, w-b*2, h-b*2);
                rc.setFill(Color.web(c.estCaisseSimple()
                        ? "#8B4513"
                        : COULEURS.getOrDefault(c.getMondeNom(), "#95A5A6")));
                backgroundLayer.getChildren().add(rc);
            }
            if (configParente.getJoueur().getPosition().equals(pos)) {
                double r = Math.min(w, h) * 0.35;
                backgroundLayer.getChildren().add(
                        new Circle(px+w/2, py+h/2, r, Color.web("#2980B9")));
            }
        }
    }

    private void dessinerCaisseMonde(CaisseMonde caisse, double px, double py, double cs) {
        char nom = caisse.getMondeNom();
        String couleurStr = COULEURS.getOrDefault(nom, "#95A5A6");
        double b = Math.max(2, cs * 0.06);

        Rectangle fondCaisse = new Rectangle(px+b, py+b, cs-b*2, cs-b*2);
        fondCaisse.setFill(Color.web(couleurStr));
        fondCaisse.setArcWidth(Math.min(8, cs*0.15));
        fondCaisse.setArcHeight(Math.min(8, cs*0.15));
        if (config.estCible(caisse.getPosition())) {
            fondCaisse.setStroke(Color.GOLD);
            fondCaisse.setStrokeWidth(Math.max(1.5, cs*0.04));
        }
        backgroundLayer.getChildren().add(fondCaisse);

        if (caisse.getConfInterne() != null) {
            double marge = b + Math.max(1, cs*0.04);
            double zW    = cs - marge*2;
            double zH    = cs - marge*2;
            ConfigurationRec ci = caisse.getConfInterne();
            double csI = Math.floor(Math.min(zW/ci.getX(), zH/ci.getY()));
            if (csI < 1) csI = 1;
            double mox = px + marge + (zW - ci.getX()*csI)/2;
            double moy = py + marge + (zH - ci.getY()*csI)/2;

            Pane fils = new Pane();
            fils.setLayoutX(px + b);
            fils.setLayoutY(py + b);
            fils.setPrefWidth(cs - b*2);
            fils.setPrefHeight(cs - b*2);
            fils.setClip(new Rectangle(0, 0, cs-b*2, cs-b*2));
            dessinerMondeMiniature(fils, ci, mox-(px+b), moy-(py+b), csI, 1);
            backgroundLayer.getChildren().add(fils);
        } else {
            Text t = new Text(String.valueOf(nom));
            t.setFont(Font.font("Arial", FontWeight.BOLD, Math.max(8, cs*0.4)));
            t.setFill(Color.WHITE);
            t.setX(px + cs/2 - cs*0.14);
            t.setY(py + cs/2 + cs*0.16);
            backgroundLayer.getChildren().add(t);
        }
    }

    private void dessinerMondeMiniature(Pane conteneur, ConfigurationRec conf,
                                         double offX, double offY,
                                         double cs, int profondeur) {
        if (conf == null || profondeur > MAX_PROFONDEUR) return;
        for (int y = 0; y < conf.getY(); y++) {
            for (int x = 0; x < conf.getX(); x++) {
                Position pos = new Position(x, y);
                double px = offX + x * cs;
                double py = offY + y * cs;
                Element elem = conf.getMonde().get(pos);
                if (elem == null) continue;
                if (elem.getType() == Type.MUR) {
                    Rectangle r = new Rectangle(px, py, cs, cs);
                    r.setFill(Color.web("#2C3E50"));
                    conteneur.getChildren().add(r);
                } else {
                    Rectangle cell = new Rectangle(px, py, cs, cs);
                    cell.setFill(Color.web("#ECEFF4"));
                    cell.setStroke(Color.web("#4C566A", 0.2));
                    conteneur.getChildren().add(cell);
                    if (conf.estCible(pos))
                        conteneur.getChildren().add(
                                new Circle(px+cs/2, py+cs/2, cs*0.18, Color.web("#F39C12")));
                    if (conf.estCaisse(pos)) {
                        CaisseMonde c = conf.getCaisse(pos);
                        char nom = c.getMondeNom();
                        double b = Math.max(1, cs*0.06);
                        Rectangle rc = new Rectangle(px+b, py+b, cs-b*2, cs-b*2);
                        rc.setFill(Color.web(c.estCaisseSimple()
                                ? "#8B4513" : COULEURS.getOrDefault(nom, "#95A5A6")));
                        conteneur.getChildren().add(rc);
                        if (!c.estCaisseSimple() && c.getConfInterne() != null
                                && profondeur < MAX_PROFONDEUR) {
                            ConfigurationRec ci = c.getConfInterne();
                            double marge = b + Math.max(0.5, cs*0.04);
                            double zW = cs - marge*2;
                            double zH = cs - marge*2;
                            double csI = Math.floor(Math.min(zW/ci.getX(), zH/ci.getY()));
                            if (csI < 1) csI = 1;
                            Pane fils = new Pane();
                            fils.setLayoutX(px + b);
                            fils.setLayoutY(py + b);
                            fils.setPrefWidth(cs - b*2);
                            fils.setPrefHeight(cs - b*2);
                            fils.setClip(new Rectangle(0, 0, cs-b*2, cs-b*2));
                            double mox = marge - b + (zW - ci.getX()*csI)/2;
                            double moy = marge - b + (zH - ci.getY()*csI)/2;
                            dessinerMondeMiniature(fils, ci, mox, moy, csI, profondeur+1);
                            conteneur.getChildren().add(fils);
                        }
                    }
                    if (conf.getJoueur().getPosition().equals(pos))
                        conteneur.getChildren().add(
                                new Circle(px+cs/2, py+cs/2, cs*0.35, Color.web("#2980B9")));
                }
            }
        }
    }

    private Rectangle rect(Image img, String fallback,
                            double px, double py, double cs) {
        Rectangle r = new Rectangle(px, py, cs, cs);
        r.setFill(img != null ? new ImagePattern(img, 0, 0, 1, 1, true)
                              : Color.web(fallback));
        return r;
    }

    public static Color getCouleurMonde(char nom) {
        return Color.web(COULEURS.getOrDefault(nom, "#95A5A6"));
    }

    private Image loadImage(String classpathPath, String filePath) {
        try {
            InputStream s = getClass().getResourceAsStream(classpathPath);
            if (s != null) return new Image(s);
            File f = new File(filePath);
            if (f.exists()) return new Image(f.toURI().toString());
        } catch (Exception ignored) {}
        return null;
    }
}
