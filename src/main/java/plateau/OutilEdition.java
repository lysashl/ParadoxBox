package plateau;
import modele.*;

/*
 * Cette classe représente les outils que l'on peut utiliser
 * dans l'éditeur de plateau.
 */
public enum OutilEdition {

    MUR,        /* poser un mur */
    VIDE,       /* remettre une case vide */
    CIBLE_ON,   /* ajouter une cible */
    CIBLE_OFF,  /* supprimer une cible */
    JOUEUR,     /* placer le joueur initial */
    CAISSE,     /* ajouter une caisse initiale */
    EFFACER     /* gomme simple */
}
