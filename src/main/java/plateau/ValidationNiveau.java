package plateau;
import modele.*;
import java.util.ArrayList;
import java.util.List;

/*
 * Vérifie que le niveau est valide avant de jouer.
 */
public class ValidationNiveau {

    public static void verifierOuException(Plateau plateau) {

        if (plateau == null) {
            throw new IllegalArgumentException("Plateau null.");
        }

        if (plateau.getPositionJoueurInitiale() == null) {
            throw new IllegalStateException("Aucun joueur initial placé.");
        }

        if (plateau.getPositionsCaisses().size() == 0) {
            throw new IllegalStateException("Aucune caisse placée.");
        }

        for (Position p : plateau.getPositionsCaisses()) {

            if (!plateau.inBounds(p)) {
                throw new IllegalStateException("Caisse hors plateau.");
            }

            if (!plateau.estVide(p)) {
                throw new IllegalStateException("Caisse sur un mur.");
            }
        }
    }
}
