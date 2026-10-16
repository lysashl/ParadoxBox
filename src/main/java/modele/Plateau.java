package modele;
import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class Plateau implements StructurePlateau {

    private Element[][] grille;
    private ArrayList<Position> cibles;
    private Position positionJoueurInitiale;
    private ArrayList<Position> positionsCaisses;

    
    public Plateau(String fichier) throws IOException {
        cibles = new ArrayList<>();
        positionsCaisses = new ArrayList<>();
        chargerDepuisFichier(fichier);
    }

    private void chargerDepuisFichier(String fichier) throws IOException {
        ArrayList<String> lignes = new ArrayList<>();
        InputStream is = Plateau.class.getResourceAsStream("/" + fichier);
	
	if (is == null) {
     	   File file = new File(fichier);
           if (file.exists()) {
           	 is = new FileInputStream(file);
           }
   	}

	if (is == null) {
    		throw new IOException("Fichier niveau introuvable : " + fichier);
	}

	Scanner sc = new Scanner(is);
        while (sc.hasNextLine()) {
            lignes.add(sc.nextLine());
        }
        sc.close();

        int hauteur = lignes.size();
        int largeur = lignes.get(0).length();
        grille = new Element[largeur][hauteur];

        for (int y = 0; y < hauteur; y++) {
            String ligne = lignes.get(y);
            for (int x = 0; x < largeur; x++) {
                char c = ligne.charAt(x);
                Position pos = new Position(x, y);

                switch (c) {
                    case '#':
                        grille[x][y] = new Mur();
                        break;
                    case '.':
                        grille[x][y] = new Case();
                        cibles.add(pos);
                        break;
                    case '@':
                        grille[x][y] = new Case();
                        positionJoueurInitiale = pos;
                        break;
                    case '$':
                        grille[x][y] = new Case();
                        positionsCaisses.add(pos);
                        break;
                    default:
                        grille[x][y] = new Case();
                }
            }
        }
    }
    
    @Override
    public int getX() {
    	return grille.length;
    }
    
    @Override
    public int getY() {
    	return grille[0].length;
    }
	
    @Override
    public boolean estVide(Position pos) {
        return grille[pos.getX()][pos.getY()].getType() != Type.MUR;
    }
    
    @Override
    public boolean estCible(Position pos) { 
    	return cibles.contains(pos);
    }
    
    @Override
    public Element get(Position pos) { 
    	return grille[pos.getX()][pos.getY()];
    }
    
   @Override
    public Position getPositionJoueurInitiale() {
    	return positionJoueurInitiale; 
    }
    
    @Override
    public ArrayList<Position> getPositionsCaisses() {
    	return positionsCaisses;
    }
    
    @Override
    public void addCaisse(Position pos, char mondeNom) {
	    // Ignorer mondeNom, juste ajouter une position
	    if (!positionsCaisses.contains(pos)) {
		positionsCaisses.add(pos);
	    }
    }

    
    /* Objectif: permettre à l'éditeur (rôle Plateau) de*/
    /* créer/modifier un niveau AVANT de jouer.*/
    /* (Le jeu pendant la partie reste géré par Configuration/Joueur/Caisse)*/

    /**
     * Vérifie que p est dans la grille.
     * Utile en édition (clics souris, etc.) pour éviter les crashs.
     */
    @Override
    public boolean inBounds(Position p) {
        return p != null
            && p.getX() >= 0 && p.getX() < getX()
            && p.getY() >= 0 && p.getY() < getY();
    }

    /**
     * Change la cellule du plateau (mur <-> case).
     * Exemple d'usage:
     *  - setElement(p, new Mur())
     *  - setElement(p, new Case())
     */
    @Override
    public void setElement(Position p, Element e) {
        if (!inBounds(p) || e == null) return;
        grille[p.getX()][p.getY()] = e;
    }

    /**
     * Ajoute une cible sur une position (si pas déjà présente).
     * (En édition: poser une cible)
     */
    @Override
    public void addCible(Position p) {
        if (!inBounds(p)) return;
        if (!cibles.contains(p)) cibles.add(p);
    }

    /**
     * Retire une cible si elle existe.
     * (En édition: enlever une cible)
     */
    @Override
    public void removeCible(Position p) {
        if (p == null) return;
        cibles.remove(p);
    }

    /**
     * Définit la position de départ du joueur (niveau initial).
     * (En édition: déplacer le point de spawn du joueur)
     */
    @Override
    public void setPositionJoueurInitiale(Position p) {
        if (!inBounds(p)) return;
        this.positionJoueurInitiale = p;
    }

    /**
     * Ajoute une caisse initiale (si pas déjà présente).
     * (En édition: poser une caisse dans le niveau)
     */
    @Override
    public void addCaisseInitiale(Position p) {
        if (!inBounds(p)) return;
        if (!positionsCaisses.contains(p)) positionsCaisses.add(p);
    }

    /**
     * Retire une caisse initiale si elle existe.
     * (En édition: supprimer une caisse du niveau)
     */
    @Override
    public void removeCaisseInitiale(Position p) {
        if (p == null) return;
        positionsCaisses.remove(p);
    }
}


