package ig;

import modele.Configuration;
import modele.Plateau;
import persistance.PersistanceNiveau;
import persistance.SauvegardePartie;

import java.io.File;

/**
 * gestionnaire de sauvegarde et chargement de partie
 * fait le lien entre l'IG et le package persistance
 * centralise la logique de sauvegarde pour respecter le MVC
 * les vues ne manipulent jamais directement SauvegardePartie
 */
public class SauvegardeManager {

    private static final String DOSSIER = "sauvegardes";

    private SauvegardeManager() {}

    /**
     * sauvegarde la partie en cours
     * cree le dossier sauvegardes si necessaire
     * nomme le fichier selon le numero de niveau
     * @param config configuration courante du jeu
     * @param niveau numero du niveau en cours
     * @return true si sauvegarde reussie false sinon
     */
    public static boolean sauvegarder(Configuration config, int niveau) {
        try {
            String chemin = DOSSIER + "/niveau" + niveau + ".sav";
            SauvegardePartie.sauvegarder(config, chemin);
            return true;
        } catch (Exception e) {
            System.err.println("Erreur sauvegarde : " + e.getMessage());
            return false;
        }
    }

    /**
     * charge une partie sauvegardee pour un niveau donne
     * retourne null si aucune sauvegarde trouvee ou erreur
     * @param niveau numero du niveau a charger
     * @return configuration restauree ou null
     */
    public static Configuration charger(int niveau) {
        try {
            String chemin = DOSSIER + "/niveau" + niveau + ".sav";
            File f = new File(chemin);
            if (!f.exists()) return null;
            return SauvegardePartie.charger(chemin);
        } catch (Exception e) {
            System.err.println("Erreur chargement : " + e.getMessage());
            return null;
        }
    }

    /**
     * verifie si une sauvegarde existe pour un niveau donne
     * @param niveau numero du niveau
     * @return true si une sauvegarde existe
     */
    public static boolean sauvegardeExiste(int niveau) {
        return new File(DOSSIER + "/niveau" + niveau + ".sav").exists();
    }
}
