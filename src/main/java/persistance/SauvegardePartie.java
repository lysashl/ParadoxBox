package persistance;

import modele.*;
import java.io.*;
import java.util.ArrayList;

/**
 * Gère la sauvegarde et le chargement d'une PARTIE EN COURS.
 *
 * Une sauvegarde contient :
 *   - Le plateau initial (inline dans le fichier .sav)
 *   - L'historique des coups joués encodé (u/d/l/r sans pousser, U/D/L/R avec poussée)
 *
 * Au chargement, on repart du plateau initial et on rejoue
 * l'historique pour retrouver l'état exact de la partie sauvegardée.
 *
 * POINTS D'ATTENTION SUR LE MODÈLE :
 *   - Configuration.deplacerJoueur() bloque si perdu==true.
 *     Lors du replay on surveille cet état et on remet à zéro si besoin.
 *   - On n'appelle jamais victoire() directement (effet de bord sur le score).
 *
 */
public class SauvegardePartie {

    private static final String MARQUEUR_DEBUT    = "PLATEAU_DEBUT";
    private static final String MARQUEUR_FIN      = "PLATEAU_FIN";
    private static final String MARQUEUR_HISTORIQUE = "HISTORIQUE:";

    //  SAUVEGARDE

    /**
     * Sauvegarde une partie en cours dans un fichier .sav.
     *
     * @param config        la Configuration courante
     * @param cheminFichier chemin de destination (ex: "sauvegardes/partie1.sav")
     * @throws IOException si l'écriture échoue
     */
    public static void sauvegarder(Configuration config, String cheminFichier) throws IOException {
        if (config == null) throw new IllegalArgumentException("Configuration null.");

        StringBuilder sb = new StringBuilder();

        // Plateau initial
        sb.append(MARQUEUR_DEBUT).append('\n');
        sb.append(plateauVersTexte(config.getPlateau()));
        sb.append(MARQUEUR_FIN).append('\n');

        // Historique encodé
        sb.append(MARQUEUR_HISTORIQUE).append(PersistanceNiveau.encoderSolution(config)).append('\n');

        ecrireFichier(cheminFichier, sb.toString());
    }

    //  CHARGEMENT

    /**
     * Charge une partie depuis un fichier .sav.
     * Repart du plateau initial et rejoue l'historique.
     *
     * ATTENTION : Configuration.deplacerJoueur() est bloqué si perdu==true.
     * On surveille cet état pendant le replay et on remet la configuration
     * à zéro via resetFromPlateau() si nécessaire pour ne pas perdre de coups.
     *
     * @param cheminFichier chemin du fichier .sav
     * @return la Configuration restaurée dans l'état exact de la sauvegarde
     * @throws IOException si le fichier est illisible ou le format invalide
     */
    public static Configuration charger(String cheminFichier) throws IOException {
        ArrayList<String> lignes = lireLignes(cheminFichier);

        ArrayList<String> lignesPlateau = new ArrayList<>();
        String historiqueStr = "";
        boolean dansPlateau = false;

        for (String ligne : lignes) {
            if (ligne.equals(MARQUEUR_DEBUT)) {
                dansPlateau = true;
                continue;
            }
            if (ligne.equals(MARQUEUR_FIN)) {
                dansPlateau = false;
                continue;
            }
            if (dansPlateau) {
                lignesPlateau.add(ligne);
                continue;
            }
            if (ligne.startsWith(MARQUEUR_HISTORIQUE)) {
                historiqueStr = ligne.substring(MARQUEUR_HISTORIQUE.length()).trim();
            }
        }

        if (lignesPlateau.isEmpty()) {
            throw new IOException("Fichier de sauvegarde invalide : plateau manquant.");
        }

        // Reconstruire le plateau via fichier temporaire
        Plateau plateau = construirePlateauDepuisLignes(lignesPlateau);
        Configuration config = new Configuration(plateau);

        // Rejouer l'historique pour retrouver l'état sauvegardé
        for (char c : historiqueStr.toCharArray()) {
            Direction d = lettreVersDirection(c);
            if (d == null) continue; // caractère invalide ignoré

            // Si perdu, on remet à zéro pour ne pas bloquer le replay
            if (config.estPerdu()) {
                config.resetFromPlateau();
            }
            config.deplacerJoueur(d);
        }

        return config;
    }

    

    /**
     * Convertit un Plateau initial en texte Sokoban pour le stocker dans le .sav.
     * Utilise les 7 caractères standard (y compris '*' et '+').
     */
    private static String plateauVersTexte(Plateau plateau) {
        StringBuilder sb = new StringBuilder();
        Position posJoueur = plateau.getPositionJoueurInitiale();

        for (int y = 0; y < plateau.getY(); y++) {
            for (int x = 0; x < plateau.getX(); x++) {
                Position pos = new Position(x, y);
                boolean estMur    = plateau.get(pos).getType() == Type.MUR;
                boolean estJoueur = posJoueur != null && posJoueur.equals(pos);
                boolean estCaisse = plateau.getPositionsCaisses().contains(pos);
                boolean estCible  = plateau.estCible(pos);

                if (estMur)         sb.append('#');
                else if (estJoueur) sb.append(estCible ? '+' : '@');
                else if (estCaisse) sb.append(estCible ? '*' : '$');
                else if (estCible)  sb.append('.');
                else                sb.append(' ');
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    /**
     * Reconstruit un Plateau depuis des lignes Sokoban via un fichier temporaire.
     * Délègue à PersistanceNiveau.chargerPlateau() pour centraliser la logique
     * (gestion de '*', '+', validation).
     */
    private static Plateau construirePlateauDepuisLignes(ArrayList<String> lignes) throws IOException {
        File tmp = File.createTempFile("sokoban_save_load_", ".txt");
        tmp.deleteOnExit();

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(tmp))) {
            for (String l : lignes) {
                bw.write(l);
                bw.newLine();
            }
        }

        try {
            return PersistanceNiveau.chargerPlateau(tmp.getAbsolutePath());
        } finally {
            tmp.delete();
        }
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

    /** Lit toutes les lignes d'un fichier. */
    private static ArrayList<String> lireLignes(String cheminFichier) throws IOException {
        ArrayList<String> lignes = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(cheminFichier))) {
            String ligne;
            while ((ligne = br.readLine()) != null) lignes.add(ligne);
        }
        return lignes;
    }

    /** Écrit dans un fichier (crée les dossiers parents si besoin). */
    private static void ecrireFichier(String cheminFichier, String contenu) throws IOException {
        File fichier = new File(cheminFichier);
        if (fichier.getParentFile() != null) fichier.getParentFile().mkdirs();
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fichier))) {
            bw.write(contenu);
        }
    }
}
