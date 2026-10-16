package plateau;
import modele.*;

/*
 * Vérifie qu'un monde récursif est valide avant de jouer.
 * on vérifie soit un monde précis soit le monde principal
 * infos pour IG et persistance:
 * si vous voulez vérifier plusieurs mondes,vous pouvez appeler verifierMonde(...) sur chacun.
 */
public class ValidationNiveauRec {

    /*
     * Vérifie le monde principal du plateau récursif.
     */
    public static void verifierPlateau(PlateauRec plateauRec) {

        if (plateauRec == null) {
            throw new IllegalArgumentException("PlateauRec null.");
        }

        Monde mondePrincipal = plateauRec.getMonde(plateauRec.getMondePrincipal());

        if (mondePrincipal == null) {
            throw new IllegalStateException("Monde principal introuvable.");
        }

        verifierMonde(mondePrincipal, plateauRec);
    }

    /*
     * Vérifie un monde précis en donnant son nom.
     */
    public static void verifierMonde(PlateauRec plateauRec, char nomMonde) {

        if (plateauRec == null) {
            throw new IllegalArgumentException("PlateauRec null.");
        }

        Monde monde = plateauRec.getMonde(nomMonde);

        if (monde == null) {
            throw new IllegalArgumentException("Le monde " + nomMonde + " n'existe pas.");
        }

        verifierMonde(monde, plateauRec);
    }

    /*
     * Vérifie qu'un monde est valide.
     */
    public static void verifierMonde(Monde monde, PlateauRec plateauRec) {

        if (monde == null) {
            throw new IllegalArgumentException("Monde null.");
        }

        if (plateauRec == null) {
            throw new IllegalArgumentException("PlateauRec null.");
        }

        /*
         * vérifier s'il y a un joueur inital
         */
        if (monde.getPositionJoueurInitiale() == null) {
            throw new IllegalStateException(
                "Aucun joueur placé dans le monde " + monde.getNom()
            );
        }

        /*
         * au moins une caisse.
         */
        if (monde.getPositionsCaisses() == null ||
            monde.getPositionsCaisses().size() == 0) {

            throw new IllegalStateException(
                "Aucune caisse placée dans le monde " + monde.getNom()
            );
        }

        /*
         * Le nombre de caisses doit être égal au nombre de cibles.
         */
        int nbCaisses = monde.getPositionsCaisses().size();
        int nbCibles = compterCibles(monde);

        if (nbCaisses != nbCibles) {
            throw new IllegalStateException(
                "Dans le monde " + monde.getNom() +
                " : " + nbCaisses + " caisse(s) pour " +
                nbCibles + " cible(s)."
            );
        }

        /*
         * Le joueur ne doit pas être dans un mur.
         */
        if (!monde.estVide(monde.getPositionJoueurInitiale())) {
            throw new IllegalStateException(
                "Le joueur est placé sur un mur dans le monde " + monde.getNom()
            );
        }
    }

    /*
     * Compte le nombre de cibles dans un monde.
     */
    private static int compterCibles(Monde monde) {

        int count = 0;

        for (int y = 0; y < monde.getY(); y++) {
            for (int x = 0; x < monde.getX(); x++) {

                if (monde.estCible(new Position(x, y))) {
                    count++;
                }
            }
        }

        return count;
    }
}
