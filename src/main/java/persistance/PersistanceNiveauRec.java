package persistance;

import modele.*;
import plateau.ValidationNiveauRec;
import java.io.*;
import java.util.ArrayList;

// je gere ici tout ce qui touche aux fichiers pour la version recursive du jeu
// un niveau recursif contient plusieurs mondes separes par des entetes comme [A] [B] etc
// le monde principal n'a pas d'entete, il est ecrit en premier dans le fichier
// chaque monde contient une grille avec des cases, des murs, des cibles, des caisses et le joueur
// une caisse-monde est representee par une lettre minuscule dans le fichier
// une caisse-monde sur une cible est representee par la meme lettre en MAJUSCULE
// une caisse simple est representee par $, une caisse simple sur cible par *
// le joueur est @, une cible est ., un mur est #, une case vide est un espace

// collaboration avec les autres roles :
// - je ne refais pas la lecture du fichier car PlateauRec de modele le fait deja
// - je ne refais pas la validation car ValidationNiveauRec de plateau le fait deja
// - je ne refais pas la verification de victoire car ConfigurationRec de modele le fait
//   je recrée juste la logique localement pour eviter l'effet de bord sur le score
// - je n'appelle pas deplacerJoueur() pour juste tester car elle modifie le score

public class PersistanceNiveauRec {

    // je charge un PlateauRec depuis un fichier texte au format recursif
    // PlateauRec de modele gere la lecture du fichier, je lui delegue ce travail
    // ValidationNiveauRec de plateau fait la validation, je lui delegue ce travail
    public static PlateauRec chargerPlateauRec(String cheminFichier) throws IOException {
        if (cheminFichier == null) throw new IllegalArgumentException("Chemin null.");

        File f = new File(cheminFichier);
        if (!f.exists()) throw new IOException("Fichier introuvable : " + cheminFichier);

        // PlateauRec lit lui-meme le fichier
        PlateauRec plateau = new PlateauRec(cheminFichier);

        // ValidationNiveauRec fait deja toutes les verifications
        // je delegue pour eviter la duplication
        try {
            ValidationNiveauRec.verifierPlateau(plateau);
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw new IOException("Niveau invalide (" + cheminFichier + ") : " + e.getMessage());
        }

        return plateau;
    }

    // je sauvegarde l'etat courant d'une ConfigurationRec dans un fichier texte
    // je parcours le monde principal et j'ecris chaque case avec le bon caractere
    public static void sauvegarderEtatCourantRec(ConfigurationRec config, String cheminFichier) throws IOException {
        if (config == null) throw new IllegalArgumentException("ConfigurationRec null.");
        ecrireFichier(cheminFichier, mondeVersTexte(config));
    }

    // je sauvegarde le PlateauRec initial dans un fichier texte
    // utile apres une edition via EditeurPlateauRec
    // le fichier produit peut etre rechargé par chargerPlateauRec()
    public static void sauvegarderPlateauRec(PlateauRec plateau, String cheminFichier) throws IOException {
        if (plateau == null) throw new IllegalArgumentException("PlateauRec null.");

        Monde mondePrincipal = plateau.getMonde(plateau.getMondePrincipal());
        if (mondePrincipal == null) throw new IllegalArgumentException("Monde principal introuvable.");

        ecrireFichier(cheminFichier, mondeInitialVersTexte(mondePrincipal));
    }

    // je convertis l'etat courant d'un ConfigurationRec en texte
    // je regarde ce qui se trouve a chaque position et j'ecris le bon caractere
    private static String mondeVersTexte(ConfigurationRec config) {
        StringBuilder sb = new StringBuilder();
        int largeur = config.getX();
        int hauteur = config.getY();

        for (int y = 0; y < hauteur; y++) {
            for (int x = 0; x < largeur; x++) {
                sb.append(charPourPositionRec(config, new Position(x, y)));
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    // je convertis un monde initial depuis le PlateauRec en texte
    // sans les positions actuelles du joueur et des caisses, juste les positions initiales
    private static String mondeInitialVersTexte(Monde monde) {
        StringBuilder sb = new StringBuilder();
        Position posJoueur = monde.getPositionJoueurInitiale();

        for (int y = 0; y < monde.getY(); y++) {
            for (int x = 0; x < monde.getX(); x++) {
                Position pos = new Position(x, y);
                boolean estMur = monde.get(pos) != null && monde.get(pos).getType() == Type.MUR;
                boolean estJoueur = posJoueur != null && posJoueur.equals(pos);
                boolean estCible = monde.estCible(pos);

                // je cherche s'il y a une caisse a cette position dans le monde initial
                char nomCaisse = ' ';
                boolean estCaisse = false;
                if (monde.getPositionsCaisses() != null) {
                    for (PositionCaisseMonde pcm : monde.getPositionsCaisses()) {
                        if (pcm != null && pcm.getPos() != null && pcm.getPos().equals(pos)) {
                            estCaisse = true;
                            nomCaisse = pcm.getMondeNom();
                            break;
                        }
                    }
                }

                if (estMur) sb.append('#');
                else if (estJoueur) sb.append(estCible ? '+' : '@');
                else if (estCaisse) {
                    if (nomCaisse == ' ') {
                        sb.append(estCible ? '*' : '$');
                    } else {
                        // caisse-monde : lettre minuscule, majuscule si sur une cible
                        sb.append(estCible ? Character.toUpperCase(nomCaisse) : Character.toLowerCase(nomCaisse));
                    }
                } else if (estCible) sb.append('.');
                else sb.append(' ');
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    // je retourne le bon caractere pour une position dans la ConfigurationRec courante
    // j'utilise les methodes publiques de ConfigurationRec au lieu de refaire la logique
    // ordre de priorite : mur > joueur > caisse-monde > caisse simple > cible > vide
    private static char charPourPositionRec(ConfigurationRec config, Position pos) {
        boolean estMur = config.getMonde().get(pos) != null && config.getMonde().get(pos).getType() == Type.MUR;
        boolean estJoueur = config.getJoueur() != null
                && config.getJoueur().getPosition() != null
                && config.getJoueur().getPosition().equals(pos);
        boolean estCible = config.estCible(pos);

        if (estMur) return '#';
        if (estJoueur) return estCible ? '+' : '@';

        // j'utilise getCaisse() de ConfigurationRec au lieu de parcourir la liste moi-meme
        CaisseMonde caisse = config.getCaisse(pos);
        if (caisse != null) {
            if (caisse.estCaisseSimple()) {
                return estCible ? '*' : '$';
            } else {
                char nom = caisse.getMondeNom();
                return estCible ? Character.toUpperCase(nom) : Character.toLowerCase(nom);
            }
        }

        if (estCible) return '.';
        return ' ';
    }

    // j'encode l'historique des deplacements du joueur en lettres
    // je rejoue la partie depuis le debut sur une configuration fraiche
    // pour savoir si chaque coup a pousse une caisse ou non
    // minuscule = sans pousser, majuscule = en poussant une caisse
    public static String encoderSolutionRec(ConfigurationRec config, PlateauRec plateau) {
        if (config == null) throw new IllegalArgumentException("ConfigurationRec null.");
        if (plateau == null) throw new IllegalArgumentException("PlateauRec null.");

        ArrayList<Direction> historique = config.getJoueur().getHistorique();
        // je cree une configuration fraiche pour rejouer depuis le debut
        // je passe le PlateauRec pour que les caisses-mondes puissent se construire
        ConfigurationRec replay = new ConfigurationRec(config.getMonde(), plateau);
        StringBuilder sb = new StringBuilder();

        for (Direction d : historique) {
            if (replay.estPerdu()) break;
            Position nextPos = replay.getJoueur().getPosition().add(d);
            boolean poussait = replay.estCaisse(nextPos);
            replay.deplacerJoueur(d);
            sb.append(directionVersLettre(d, poussait));
        }
        return sb.toString();
    }

    // je sauvegarde la solution encodee dans un fichier texte
    public static void sauvegarderSolutionRec(ConfigurationRec config, PlateauRec plateau, String cheminFichier) throws IOException {
        ecrireFichier(cheminFichier, encoderSolutionRec(config, plateau) + "\n");
    }

    // je charge une solution depuis un fichier et retourne la liste de Direction
    public static ArrayList<Direction> chargerSolutionRec(String cheminFichier) throws IOException {
        ArrayList<String> lignes = lireLignes(cheminFichier);
        StringBuilder sb = new StringBuilder();
        for (String l : lignes) sb.append(l.trim());

        ArrayList<Direction> directions = new ArrayList<>();
        for (char c : sb.toString().toCharArray()) {
            Direction d = lettreVersDirection(c);
            if (d == null) throw new IOException("Caractere invalide dans la solution : '" + c + "'");
            directions.add(d);
        }
        return directions;
    }

    // j'applique une liste de directions sur une ConfigurationRec
    // j'utilise deplacerJoueur() de ConfigurationRec, je ne refais pas la logique
    public static void appliquerSolutionRec(ConfigurationRec config, ArrayList<Direction> directions) {
        if (config == null || directions == null) return;
        for (Direction d : directions) {
            if (config.estPerdu()) break;
            config.deplacerJoueur(d);
        }
    }

    // j'affiche le monde principal en texte dans le terminal pour les tests
    // je n'appelle pas victoire() pour tester car deplacerJoueur l'appelle deja
    // et ajoute 200+100 au score si victoire, donc je recrée la logique localement
    public static String afficherTerminalRec(ConfigurationRec config) {
        if (config == null) return "[ConfigurationRec null]";
        int largeur = config.getX();
        int hauteur = config.getY();
        String sep = "+" + "-".repeat(largeur) + "+";
        StringBuilder sb = new StringBuilder();

        sb.append(sep).append('\n');
        for (int y = 0; y < hauteur; y++) {
            sb.append('|');
            for (int x = 0; x < largeur; x++) {
                sb.append(charPourPositionRec(config, new Position(x, y)));
            }
            sb.append("|\n");
        }
        sb.append(sep).append('\n');
        sb.append("Score : ").append(config.getScore())
          .append("   Coups : ").append(config.getCoups()).append('\n');

        if (config.estPerdu()) {
            sb.append(">>> PERDU <<<\n");
        } else if (estVictoireRecSansEffetDeBord(config)) {
            sb.append(">>> VICTOIRE ! <<<\n");
        }
        return sb.toString();
    }

    // j'affiche directement dans System.out
    public static void afficherDansConsoleRec(ConfigurationRec config) {
        System.out.print(afficherTerminalRec(config));
    }

    // je verifie si toutes les caisses sont sur des cibles sans appeler victoire()
    // car deplacerJoueur() appelle victoire() et ajoute 200+100 au score si victoire
    // je recrée la logique manuellement avec getCaisses() et estCible() de ConfigurationRec
    private static boolean estVictoireRecSansEffetDeBord(ConfigurationRec config) {
        if (config.getCaisses() == null || config.getCaisses().isEmpty()) return false;
        for (CaisseMonde c : config.getCaisses()) {
            if (c == null) continue;
            if (!config.estCible(c.getPosition())) return false;
        }
        return true;
    }

    // je convertis une Direction en lettre de mouvement
    // je compare par getDx/getDy car Direction n'a pas de equals()
    // HAUT dx=0 dy=-1, BAS dx=0 dy=1, GAUCHE dx=-1 dy=0, DROITE dx=1 dy=0
    private static char directionVersLettre(Direction d, boolean poussait) {
        if (d.getDx() ==  0 && d.getDy() == -1) return poussait ? 'U' : 'u';
        if (d.getDx() ==  0 && d.getDy() ==  1) return poussait ? 'D' : 'd';
        if (d.getDx() == -1 && d.getDy() ==  0) return poussait ? 'L' : 'l';
        if (d.getDx() ==  1 && d.getDy() ==  0) return poussait ? 'R' : 'r';
        return '?';
    }

    // je convertis une lettre de mouvement en Direction
    private static Direction lettreVersDirection(char c) {
        switch (Character.toLowerCase(c)) {
            case 'u': return Direction.HAUT;
            case 'd': return Direction.BAS;
            case 'l': return Direction.GAUCHE;
            case 'r': return Direction.DROITE;
            default:  return null;
        }
    }

    // je lis toutes les lignes d'un fichier texte
    private static ArrayList<String> lireLignes(String cheminFichier) throws IOException {
        ArrayList<String> lignes = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(cheminFichier))) {
            String ligne;
            while ((ligne = br.readLine()) != null) lignes.add(ligne);
        }
        return lignes;
    }

    // j'ecris une chaine dans un fichier et je cree les dossiers parents si necessaire
    private static void ecrireFichier(String cheminFichier, String contenu) throws IOException {
        File fichier = new File(cheminFichier);
        if (fichier.getParentFile() != null) fichier.getParentFile().mkdirs();
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fichier))) {
            bw.write(contenu);
        }
    }
}
