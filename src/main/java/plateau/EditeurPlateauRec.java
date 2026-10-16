package plateau;
import modele.*;

/*
 * Cette classe sert à modifier un niveau récursif.
 * Contrairement à la version classique, on n'édite pas directement PlateauRec
 * case par case, car PlateauRec contient les mondes et  Monde est la vraie stru *cture qu'on modifie
 * Donc la logique de la classe est d'édite un monde courant(qui est un plateau aussi bien sur).
 */
public class EditeurPlateauRec {

    private PlateauRec plateauRec;
    private Monde mondeCourant;

    /* premier constructeur on prend le plateau récursif(les mondes) et on se place par défaut  sur le monde principal.
     */
    public EditeurPlateauRec(PlateauRec plateauRec) {

        if (plateauRec == null) {
            throw new IllegalArgumentException("Le plateau récursif ne peut pas être null.");
        }

        this.plateauRec = plateauRec;
        this.mondeCourant = plateauRec.getMonde(plateauRec.getMondePrincipal());

        if (this.mondeCourant == null) {
            throw new IllegalStateException("Le monde principal est introuvable.");
        }
    }

    /*
     *deusièmz constructeur utile si l'IG veut ouvrir directement un monde précis coucou Ines */
    public EditeurPlateauRec(PlateauRec plateauRec, char nomMonde) {

        if (plateauRec == null) {
            throw new IllegalArgumentException("Le plateau récursif ne peut pas être null.");
        }

        this.plateauRec = plateauRec;
        this.mondeCourant = plateauRec.getMonde(nomMonde);

        if (this.mondeCourant == null) {
            throw new IllegalArgumentException("Le monde " + nomMonde + " n'existe pas.");
        }
    }

    public PlateauRec getPlateauRec() {
        return plateauRec;
    }

    public Monde getMondeCourant() {
        return mondeCourant;
    }

    public char getNomMondeCourant() {
        return mondeCourant.getNom();
    }

    /*
     * Permet de changer de monde à éditer.
     */
    public void choisirMonde(char nomMonde) {

        Monde nouveauMonde = plateauRec.getMonde(nomMonde);

        if (nouveauMonde == null) {
            throw new IllegalArgumentException("Le monde " + nomMonde + " n'existe pas.");
        }

        this.mondeCourant = nouveauMonde;
    }

    /*
     Même logique que la  version classique sauf que ici une caisse simple si  mondeNomCaisse = ' ' et caisse-monde sinon
     */
    public void appliquer(Position p, OutilEdition outil) {
        appliquer(p, outil, ' ');
    }

   
     /* un petit changememnt par apport à la version classique on utilise mondeNomCaisse Si outil = CAISSE et mondeNomCaisse = 'A' alors on place une caisse-monde A.*/

    public void appliquer(Position p, OutilEdition outil, char mondeNomCaisse) {

        if (p == null) {
            throw new IllegalArgumentException("La position ne peut pas être null.");
        }

        if (outil == null) {
            throw new IllegalArgumentException("L'outil ne peut pas être null.");
        }

        if (!mondeCourant.inBounds(p)) {
            throw new IndexOutOfBoundsException("Position hors du monde courant.");
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
                ajouterCaisse(p, mondeNomCaisse);
                break;

            case EFFACER:
                effacer(p);
                break;
        }
    }

    /* get to work */

    public void placerMur(Position p) {

        verifierPosition(p);

        mondeCourant.removeCible(p);
        mondeCourant.removeCaisseInitiale(p);
        mondeCourant.setElement(p, new Mur());

        /*
         * Si on met un mur là ou il était le joueur initial,
         * on enleve aussi le joueur initial.
         */
        if (p.equals(mondeCourant.getPositionJoueurInitiale())) {
            mondeCourant.setPositionJoueurInitiale(null);
        }
    }

    public void mettreVide(Position p) {

        verifierPosition(p);

        mondeCourant.setElement(p, new Case());
    }

    public void ajouterCible(Position p) {

        verifierPosition(p);

        mondeCourant.setElement(p, new Case());
        mondeCourant.addCible(p);
    }

    public void retirerCible(Position p) {

        verifierPosition(p);

        mondeCourant.removeCible(p);
    }

    public void placerJoueur(Position p) {

        verifierPosition(p);

        mondeCourant.setElement(p, new Case());
        mondeCourant.setPositionJoueurInitiale(p);
    }

    /*
     *Si mondeNom = ' ', on place une caisse simple
     * Sinon on place une caisse-monde.
     */
    public void ajouterCaisse(Position p, char mondeNom) {

        verifierPosition(p);

        mondeCourant.setElement(p, new Case());

        if (mondeNom == ' ') {
            mondeCourant.addCaisseInitiale(p);
            return;
        }

        if (plateauRec.getMonde(mondeNom) == null) {
            throw new IllegalArgumentException("Le monde " + mondeNom + " n'existe pas .");
        }

        mondeCourant.addCaisseInitiale(p, mondeNom);
    }

    public void ajouterCaisse(Position p) {
        ajouterCaisse(p, ' ');
    }

    public void effacer(Position p) {

        verifierPosition(p);

        mondeCourant.removeCible(p);
        mondeCourant.removeCaisseInitiale(p);
        mondeCourant.setElement(p, new Case());

        
        /* Si on efface la case du joueur initial,on enlève aussi le joueur initial.*/
         
        if (p.equals(mondeCourant.getPositionJoueurInitiale())) {
            mondeCourant.setPositionJoueurInitiale(null);
        }
    }

    /*
     * ça évite d'appeler la méthode verification de la classe Monde et de vérifier à chaque fois que p est diffrent de null*/
     
    private void verifierPosition(Position p) {

        if (p == null) {
            throw new IllegalArgumentException("La position ne peut pas être null.");
        }

        if (!mondeCourant.inBounds(p)) {
            throw new IndexOutOfBoundsException("Position hors du monde courant.");
        }
    }
}
