package persistance;

import modele.*;
import java.io.*;
import java.util.ArrayList;

// je gere la sauvegarde et le chargement d'une partie recursive en cours
// je sauvegarde le chemin du fichier de niveau original et l'historique des coups
// au chargement je relis le fichier de niveau via PersistanceNiveauRec
// et je rejoue tous les coups pour retrouver exactement l'etat de la partie
// je ne refais pas la logique de chargement, je delegue a PersistanceNiveauRec

public class SauvegardePartieRec {

    private static final String MARQUEUR_FICHIER    = "FICHIER_NIVEAU:";
    private static final String MARQUEUR_HISTORIQUE = "HISTORIQUE:";

    // je sauvegarde une partie recursive en cours dans un fichier .sav
    // je delegue l'encodage de l'historique a PersistanceNiveauRec.encoderSolutionRec()
    public static void sauvegarder(ConfigurationRec config, PlateauRec plateau, String cheminNiveau, String cheminFichier) throws IOException {
        if (config == null) throw new IllegalArgumentException("ConfigurationRec null.");
        if (plateau == null) throw new IllegalArgumentException("PlateauRec null.");
        if (cheminNiveau == null) throw new IllegalArgumentException("Chemin du niveau null.");

        StringBuilder sb = new StringBuilder();
        sb.append(MARQUEUR_FICHIER).append(cheminNiveau).append('\n');
        sb.append(MARQUEUR_HISTORIQUE).append(PersistanceNiveauRec.encoderSolutionRec(config, plateau)).append('\n');

        ecrireFichier(cheminFichier, sb.toString());
    }

    // je charge une partie recursive depuis un fichier .sav
    // je delegue le rechargement du plateau a PersistanceNiveauRec.chargerPlateauRec()
    // qui fait deja la lecture et la validation via ValidationNiveauRec
    public static ConfigurationRec charger(String cheminFichier) throws IOException {
        ArrayList<String> lignes = lireLignes(cheminFichier);

        String cheminNiveau = null;
        String historiqueStr = "";

        for (String ligne : lignes) {
            if (ligne.startsWith(MARQUEUR_FICHIER)) {
                cheminNiveau = ligne.substring(MARQUEUR_FICHIER.length()).trim();
            }
            if (ligne.startsWith(MARQUEUR_HISTORIQUE)) {
                historiqueStr = ligne.substring(MARQUEUR_HISTORIQUE.length()).trim();
            }
        }

        if (cheminNiveau == null || cheminNiveau.isEmpty()) {
            throw new IOException("Fichier de sauvegarde invalide : chemin du niveau manquant.");
        }

        // je delegue a PersistanceNiveauRec qui fait deja la lecture + validation
        PlateauRec plateau = PersistanceNiveauRec.chargerPlateauRec(cheminNiveau);
        Monde mondePrincipal = plateau.getMonde(plateau.getMondePrincipal());
        ConfigurationRec config = new ConfigurationRec(mondePrincipal, plateau);

        // je delegue l'application de la solution a PersistanceNiveauRec
        ArrayList<Direction> directions = decoderHistorique(historiqueStr);
        PersistanceNiveauRec.appliquerSolutionRec(config, directions);

        return config;
    }

    // je decode une chaine d'historique en liste de Direction
    private static ArrayList<Direction> decoderHistorique(String historique) {
        ArrayList<Direction> directions = new ArrayList<>();
        for (char c : historique.toCharArray()) {
            Direction d = lettreVersDirection(c);
            if (d != null) directions.add(d);
        }
        return directions;
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
