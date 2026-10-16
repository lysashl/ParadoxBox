package persistance;

import modele.*;
import java.io.*;
import java.util.ArrayList;

/**
 * Responsable de la persistance du jeu Sokoban (version classique).
 *
 * Fonctionnalités :
 *   1. Chargement d'un niveau depuis un fichier texte (format Sokoban standard)
 *   2. Sauvegarde de l'état courant d'une partie dans un fichier texte
 *   3. Sauvegarde du plateau initial (pour l'éditeur)
 *   4. Enregistrement et chargement d'une solution/parcours
 *   5. Affichage texte du plateau (version terminal pour les tests)
 *
 * 
 * FORMAT DE NIVEAU (Sokoban classique) :
 *   ' '  case vide
 *   '#'  mur
 *   '$'  caisse sur case vide
 *   '@'  joueur sur case vide
 *   '.'  cible vide
 *   '*'  caisse sur cible
 *   '+'  joueur sur cible
 *
 * POINTS D'ATTENTION SUR LE MODÈLE :
 *
 *   1. Plateau(String) ne gère que '#' '.' '@' '$' ' '.
 *      Les caractères '*' et '+' sont donc traités ici en 2 étapes :
 *        - fichier temporaire où '*'->'$' et '+'->'@'
 *        - ajout manuel des cibles via plateau.addCible()
 *
 *   2. Configuration.victoire() ET vientDeGagner() ont un EFFET DE BORD :
 *      victoire() ajoute 200 (+100 bonus) au score à chaque appel,
 *      et vientDeGagner() appelle victoire() en interne.
 *      On vérifie la victoire via estVictoireSansEffetDeBord()
 *      qui utilise getCaisses() + estCible() sans toucher au score.
 *
 *   3. Configuration.deplacerJoueur() bloque si perdu==true.
 *      Lors du replay dans encoderSolution(), on vérifie si la config
 *      est bloquée et on la remet à zéro si nécessaire.
 *
 *   4. Direction n'a pas de equals(). On compare par getDx()/getDy().
 *
 * FORMAT SOLUTION (format standard Sokoban) :
 *   u/d/l/r  déplacement sans pousser (haut/bas/gauche/droite)
 *   U/D/L/R  déplacement avec poussée de caisse
 * 
 */
public class PersistanceNiveau {

    
    //  1. CHARGEMENT D'UN NIVEAU
    

    /**
     * Charge un Plateau depuis un fichier texte au format Sokoban classique.
     * Gère tous les caractères : ' ' '#' '$' '@' '.' '*' '+'
     *
     * @param cheminFichier chemin vers le fichier .txt
     * @return le Plateau chargé, prêt à être utilisé dans une Configuration
     * @throws IOException si le fichier est illisible ou le format invalide
     */
    public static Plateau chargerPlateau(String cheminFichier) throws IOException {
        ArrayList<String> lignes = lireLignes(cheminFichier);
        if (lignes.isEmpty()) {
            throw new IOException("Fichier vide : " + cheminFichier);
        }
        return construirePlateau(lignes, cheminFichier);
    }

    /**
     * Construit un Plateau depuis les lignes lues.
     *
     * Étape 1 : fichier temporaire normalisé pour Plateau(String) :
     *   - '*' -> '$'  (Plateau chargera la caisse correctement)
     *   - '+' -> '@'  (Plateau chargera le joueur correctement)
     *   - lignes normalisées à la même largeur
     *
     * Étape 2 : ajout manuel des cibles que Plateau a manquées :
     *   - pour chaque '*' ou '+' dans les lignes originales,
     *     on appelle plateau.addCible()
     */
    private static Plateau construirePlateau(ArrayList<String> lignes, String source) throws IOException {
        int largeur = 0;
        for (String l : lignes) if (l.length() > largeur) largeur = l.length();
        if (largeur == 0) throw new IOException("Fichier de niveau invalide (largeur nulle) : " + source);

        //  Étape 1 : fichier temporaire 
        File tmp = File.createTempFile("sokoban_load_", ".txt");
        tmp.deleteOnExit();

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(tmp))) {
            for (String ligne : lignes) {
                StringBuilder sb = new StringBuilder();
                for (int x = 0; x < largeur; x++) {
                    char c = (x < ligne.length()) ? ligne.charAt(x) : ' ';
                    switch (c) {
                        case '*': sb.append('$'); break;
                        case '+': sb.append('@'); break;
                        default:  sb.append(c);   break;
                    }
                }
                bw.write(sb.toString());
                bw.newLine();
            }
        }

        Plateau plateau;
        try {
            plateau = new Plateau(tmp.getAbsolutePath());
        } finally {
            tmp.delete();
        }

        //  Étape 2 : cibles manquantes sous '*' et '+' 
        for (int y = 0; y < lignes.size(); y++) {
            String ligne = lignes.get(y);
            for (int x = 0; x < ligne.length(); x++) {
                char c = ligne.charAt(x);
                if (c == '*' || c == '+') {
                    plateau.addCible(new Position(x, y));
                }
            }
        }

        validerPlateau(plateau, source);
        return plateau;
    }

    /** Valide qu'un plateau est jouable avant de le retourner. */
    private static void validerPlateau(Plateau plateau, String source) throws IOException {
        if (plateau.getPositionJoueurInitiale() == null) {
            throw new IOException("Niveau invalide (" + source + ") : aucun joueur '@' ou '+' trouvé.");
        }
        if (plateau.getPositionsCaisses().isEmpty()) {
            throw new IOException("Niveau invalide (" + source + ") : aucune caisse '$' ou '*' trouvée.");
        }
        int nbCaisses = plateau.getPositionsCaisses().size();
        int nbCibles  = compterCibles(plateau);
        if (nbCaisses != nbCibles) {
            throw new IOException("Niveau invalide (" + source + ") : "
                    + nbCaisses + " caisse(s) pour " + nbCibles + " cible(s).");
        }
    }

    /**
     * Compte les cibles en parcourant la grille via plateau.estCible().
     * Nécessaire car Plateau n'expose pas directement sa liste de cibles.
     */
    private static int compterCibles(Plateau plateau) {
        int count = 0;
        for (int y = 0; y < plateau.getY(); y++)
            for (int x = 0; x < plateau.getX(); x++)
                if (plateau.estCible(new Position(x, y))) count++;
        return count;
    }

    //  2. SAUVEGARDE DE L'ÉTAT COURANT

    /**
     * Sauvegarde l'état courant d'une Configuration dans un fichier texte.
     * Utilise les 7 caractères Sokoban standard (y compris '*' et '+').
     * Le fichier produit peut être rechargé par chargerPlateau().
     *
     * @param config        la configuration courante du jeu
     * @param cheminFichier chemin de destination (créé ou écrasé)
     * @throws IOException si l'écriture échoue
     */
    public static void sauvegarderEtatCourant(Configuration config, String cheminFichier) throws IOException {
        if (config == null) throw new IllegalArgumentException("Configuration null.");
        StringBuilder sb = new StringBuilder();

        for (int y = 0; y < config.getY(); y++) {
            for (int x = 0; x < config.getX(); x++) {
                sb.append(charPourPositionConfig(config, new Position(x, y)));
            }
            sb.append('\n');
        }
        ecrireFichier(cheminFichier, sb.toString());
    }

    
    //  3. SAUVEGARDE DU PLATEAU INITIAL (pour l'éditeur)
    

    /**
     * Sauvegarde le plateau initial dans un fichier texte.
     * Utile après une édition via EditeurPlateau.
     * Le fichier produit peut être rechargé par chargerPlateau().
     *
     * @param plateau       le plateau à sauvegarder
     * @param cheminFichier chemin de destination
     * @throws IOException si l'écriture échoue
     */
    public static void sauvegarderPlateau(Plateau plateau, String cheminFichier) throws IOException {
        if (plateau == null) throw new IllegalArgumentException("Plateau null.");
        StringBuilder sb = new StringBuilder();
        Position posJoueur = plateau.getPositionJoueurInitiale();

        for (int y = 0; y < plateau.getY(); y++) {
            for (int x = 0; x < plateau.getX(); x++) {
                sb.append(charPourPositionPlateau(plateau, posJoueur, new Position(x, y)));
            }
            sb.append('\n');
        }
        ecrireFichier(cheminFichier, sb.toString());
    }

   
    //  4. SOLUTION / PARCOURS
   

    /**
     * Encode l'historique des déplacements du joueur en encodant chaque mouvement par une lettre (u/d/l/r ou U/D/L/R).
     *
     * On rejoue la partie depuis le début avec une Configuration fraîche
     * pour détecter à chaque coup si une caisse était poussée.
     *
     * ATTENTION : Configuration.deplacerJoueur() bloque si perdu==true.
     * Si la configuration devient perdue pendant le replay, on la remet
     * à zéro via resetFromPlateau() pour continuer le replay correctement.
     *
     *   u/d/l/r = déplacement simple
     *   U/D/L/R = déplacement avec poussée de caisse
     *
     * @param config la Configuration après la partie
     * @return la chaîne encodée (u/d/l/r sans pousser, U/D/L/R avec poussée)
     */
    public static String encoderSolution(Configuration config) {
        if (config == null) throw new IllegalArgumentException("Configuration null.");

        ArrayList<Direction> historique = config.getJoueur().getHistorique();

        // On travaille sur une configuration fraîche pour ne pas altérer l'originale
        Configuration replay = new Configuration(config.getPlateau());
        StringBuilder sb = new StringBuilder();

        for (Direction d : historique) {
            // Si la config est bloquée (perdu), on ne peut pas continuer le replay.
            // Cela ne devrait pas arriver sur un historique valide, mais on se protège.
            if (replay.estPerdu()) {
                replay.resetFromPlateau();
            }

            Position nextPos = replay.getJoueur().getPosition().add(d);
            boolean poussait = replay.estCaisse(nextPos);
            replay.deplacerJoueur(d);
            sb.append(directionVersLettre(d, poussait));
        }
        return sb.toString();
    }

    /**
     * Sauvegarde la solution encodée dans un fichier texte.
     *
     * @param config        la configuration après la partie
     * @param cheminFichier chemin de destination
     * @throws IOException si l'écriture échoue
     */
    public static void sauvegarderSolution(Configuration config, String cheminFichier) throws IOException {
        ecrireFichier(cheminFichier, encoderSolution(config) + "\n");
    }

    /**
     * Charge une solution depuis un fichier et retourne la liste de Direction.
     *
     * @param cheminFichier chemin vers le fichier solution
     * @return liste ordonnée de Direction
     * @throws IOException si le fichier contient des caractères invalides
     */
    public static ArrayList<Direction> chargerSolution(String cheminFichier) throws IOException {
        ArrayList<String> lignes = lireLignes(cheminFichier);
        StringBuilder sb = new StringBuilder();
        for (String l : lignes) sb.append(l.trim());

        ArrayList<Direction> directions = new ArrayList<>();
        for (char c : sb.toString().toCharArray()) {
            Direction d = lettreVersDirection(c);
            if (d == null) throw new IOException("Caractère invalide dans la solution : '" + c + "'");
            directions.add(d);
        }
        return directions;
    }

    /**
     * Applique une liste de directions sur une Configuration.
     * Utile pour rejouer automatiquement une solution sauvegardée.
     *
     * @param config     la Configuration (dans son état initial)
     * @param directions la liste de directions à appliquer
     */
    public static void appliquerSolution(Configuration config, ArrayList<Direction> directions) {
        if (config == null || directions == null) return;
        for (Direction d : directions) config.deplacerJoueur(d);
    }

    
    //  5. AFFICHAGE TERMINAL (pour les tests sans IG)
   

    /**
     * Retourne une représentation textuelle du plateau courant.
     *
     * ATTENTION : on n'appelle NI victoire() NI vientDeGagner() car les deux
     * ont un effet de bord (victoire() ajoute 200+100 pts au score).
     * La victoire est vérifiée via estVictoireSansEffetDeBord().
     *
     * @param config la configuration courante
     * @return chaîne multi-lignes représentant le plateau
     */
    public static String afficherTerminal(Configuration config) {
        if (config == null) return "[Configuration null]";
        int largeur = config.getX();
        int hauteur = config.getY();
        String sep = "+" + "-".repeat(largeur) + "+";
        StringBuilder sb = new StringBuilder();

        sb.append(sep).append('\n');
        for (int y = 0; y < hauteur; y++) {
            sb.append('|');
            for (int x = 0; x < largeur; x++) {
                sb.append(charPourPositionConfig(config, new Position(x, y)));
            }
            sb.append("|\n");
        }
        sb.append(sep).append('\n');
        sb.append("Score : ").append(config.getScore())
          .append("   Coups : ").append(config.getCoups()).append('\n');

        // On n'appelle NI victoire() NI vientDeGagner() ici.
        // Les deux ont un effet de bord : victoire() ajoute 200+100 pts au score.
        // On vérifie la victoire manuellement via getCaisses() + estCible().
        if (config.estPerdu()) {
            sb.append(">>> PERDU <<<\n");
        } else if (estVictoireSansEffetDeBord(config)) {
            sb.append(">>> VICTOIRE ! <<<\n");
        }

        return sb.toString();
    }

    /** Affiche directement le plateau dans System.out. */
    public static void afficherDansConsole(Configuration config) {
        System.out.print(afficherTerminal(config));
    }

    

    /**
     * Renvoie le caractère Sokoban pour une position dans la Configuration courante.
     * Ordre de priorité : mur > joueur > caisse > cible > vide
     */
    private static char charPourPositionConfig(Configuration config, Position pos) {
        boolean estMur    = config.getPlateau().get(pos).getType() == Type.MUR;
        boolean estJoueur = config.getJoueur().getPosition().equals(pos);
        boolean estCaisse = config.estCaisse(pos);
        boolean estCible  = config.estCible(pos);

        if (estMur)    return '#';
        if (estJoueur) return estCible ? '+' : '@';
        if (estCaisse) return estCible ? '*' : '$';
        if (estCible)  return '.';
        return ' ';
    }

    /**
     * Renvoie le caractère Sokoban pour une position dans le Plateau initial.
     */
    private static char charPourPositionPlateau(Plateau plateau, Position posJoueur, Position pos) {
        boolean estMur    = plateau.get(pos).getType() == Type.MUR;
        boolean estJoueur = posJoueur != null && posJoueur.equals(pos);
        boolean estCaisse = plateau.getPositionsCaisses().contains(pos);
        boolean estCible  = plateau.estCible(pos);

        if (estMur)    return '#';
        if (estJoueur) return estCible ? '+' : '@';
        if (estCaisse) return estCible ? '*' : '$';
        if (estCible)  return '.';
        return ' ';
    }

    /**
     * Vérifie si toutes les caisses sont sur des cibles SANS appeler victoire().
     *
     * Pourquoi ne pas utiliser victoire() ou vientDeGagner() ?
     *   - victoire() modifie le score (+200, +100 bonus) à chaque appel.
     *   - vientDeGagner() = victoire() && !perdu -> même effet de bord.
     * On recrée donc la logique de victoire() avec getCaisses() + estCible(),
     * sans toucher au score.
     */
    private static boolean estVictoireSansEffetDeBord(Configuration config) {
        if (config.getCaisses().isEmpty()) return false;
        for (Caisse c : config.getCaisses()) {
            if (!config.estCible(c.getPosition())) return false;
        }
        return true;
    }

    /**
     * Direction -> lettre de mouvement.
     * Comparaison par getDx()/getDy() car Direction n'a pas de equals()
     * et les instances sont créées avec new (== non fiable).
     *   HAUT   : dx=0,  dy=-1
     *   BAS    : dx=0,  dy=+1
     *   GAUCHE : dx=-1, dy=0
     *   DROITE : dx=+1, dy=0
     */
    private static char directionVersLettre(Direction d, boolean poussait) {
        if (d.getDx() ==  0 && d.getDy() == -1) return poussait ? 'U' : 'u';
        if (d.getDx() ==  0 && d.getDy() ==  1) return poussait ? 'D' : 'd';
        if (d.getDx() == -1 && d.getDy() ==  0) return poussait ? 'L' : 'l';
        if (d.getDx() ==  1 && d.getDy() ==  0) return poussait ? 'R' : 'r';
        return '?';
    }

    /** Lettre de mouvement -> Direction (insensible à la casse). */
    private static Direction lettreVersDirection(char c) {
        switch (Character.toLowerCase(c)) {
            case 'u': return Direction.HAUT;
            case 'd': return Direction.BAS;
            case 'l': return Direction.GAUCHE;
            case 'r': return Direction.DROITE;
            default:  return null;
        }
    }

    /** Lit toutes les lignes d'un fichier texte. */
    private static ArrayList<String> lireLignes(String cheminFichier) throws IOException {
        ArrayList<String> lignes = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(cheminFichier))) {
            String ligne;
            while ((ligne = br.readLine()) != null) lignes.add(ligne);
        }
        return lignes;
    }

    /** Écrit une chaîne dans un fichier (crée les dossiers parents si nécessaire). */
    private static void ecrireFichier(String cheminFichier, String contenu) throws IOException {
        File fichier = new File(cheminFichier);
        if (fichier.getParentFile() != null) fichier.getParentFile().mkdirs();
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fichier))) {
            bw.write(contenu);
        }
    }
}
