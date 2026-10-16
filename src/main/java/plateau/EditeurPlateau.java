package plateau;
import modele.*;

/*
 * Cette classe sert à modifier un Plateau.
 * Elle est utilisée en mode édition (avant de jouer).
 *
 * A SAVOIR :
 *  on modifie le niveau (Plateau)
 *  on ne gère pas les déplacements du jeu
 */
public class EditeurPlateau {

    private Plateau plateau;

    public EditeurPlateau(Plateau plateau) {

        if (plateau == null) {
            throw new IllegalArgumentException("Le plateau ne peut pas être null.");
        }

        this.plateau = plateau;
    }

    public Plateau getPlateau() {
        return plateau;
    }

    /*
     * Méthode principale d'édition.
     * Applique un outil sur une position.
     */
    public void appliquer(Position p, OutilEdition outil) {

        if (p == null) {
            throw new IllegalArgumentException("La position ne peut pas être null.");
        }

        if (outil == null) {
            throw new IllegalArgumentException("L'outil ne peut pas être null.");
        }

        if (!plateau.inBounds(p)) {
            throw new IndexOutOfBoundsException("Position hors du plateau.");
        }

        switch (outil) {

            case MUR:
                placerMur(p);
                break;

            case VIDE:
                mettreVide(p);
                break;

            case CIBLE_ON:
                ajouterCible(p);
                break;

            case CIBLE_OFF:
                retirerCible(p);
                break;

            case JOUEUR:
                placerJoueur(p);
                break;

            case CAISSE:
                ajouterCaisse(p);
                break;

            case EFFACER:
                effacer(p);
                break;
        }
    }

    /* On commence  */

    public void placerMur(Position p) {

        if (!plateau.inBounds(p)) {
            throw new IndexOutOfBoundsException("Position hors du plateau.");
        }

        plateau.removeCible(p);
        plateau.removeCaisseInitiale(p);
        plateau.setElement(p, new Mur());
    }

    public void mettreVide(Position p) {

        if (!plateau.inBounds(p)) {
            throw new IndexOutOfBoundsException("Position hors du plateau.");
        }

        plateau.setElement(p, new Case());
    }

    public void ajouterCible(Position p) {

        if (!plateau.inBounds(p)) {
            throw new IndexOutOfBoundsException("Position hors du plateau.");
        }

        plateau.setElement(p, new Case());
        plateau.addCible(p);
    }

    public void retirerCible(Position p) {

        if (!plateau.inBounds(p)) {
            throw new IndexOutOfBoundsException("Position hors du plateau.");
        }

        plateau.removeCible(p);
    }

    public void placerJoueur(Position p) {

        if (!plateau.inBounds(p)) {
            throw new IndexOutOfBoundsException("Position hors du plateau.");
        }

        plateau.setElement(p, new Case());
        plateau.setPositionJoueurInitiale(p);
    }

    public void ajouterCaisse(Position p) {

        if (!plateau.inBounds(p)) {
            throw new IndexOutOfBoundsException("Position hors du plateau.");
        }

        plateau.setElement(p, new Case());
        plateau.addCaisseInitiale(p);
    }

    public void effacer(Position p) {

        if (!plateau.inBounds(p)) {
            throw new IndexOutOfBoundsException("Position hors du plateau.");
        }

        plateau.removeCible(p);
        plateau.removeCaisseInitiale(p);
        plateau.setElement(p, new Case());
    }
}
