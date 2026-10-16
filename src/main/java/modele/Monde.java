package modele;

import java.util.ArrayList;

public class Monde implements StructurePlateau{
	private Element [][] grille;
	private ArrayList<Position> cibles;
	private Position positionJoueurInitiale;
	private ArrayList<PositionCaisseMonde> positionsCaisses; /* une liste de classes PositionCaisseMonde qui représente la position d'une caisse contenant un monde donc elle est caractérisée par un nom et une position */
	private char nom;
	
	public Monde(char nom){
		this.nom = nom; 
		this.cibles = new ArrayList<>(); /*les cibles contenues dans le monde*/
		this.positionsCaisses = new ArrayList<>(); /* les positions des caisses contenus dans le monde */
	}
	
	@Override
	public boolean estVide(Position pos){
		return grille[pos.getX()][pos.getY()].getType() != Type.MUR;
	}
	
	@Override
	public boolean estCible(Position pos){
		return 	cibles.contains(pos);
	}
	
	@Override
	public Position getPositionJoueurInitiale(){
		return positionJoueurInitiale;
	}
	
	@Override
	public ArrayList<PositionCaisseMonde> getPositionsCaisses(){
		return positionsCaisses;
	}
	
	public void setGrille(Element [][] grille){
		this.grille = grille;
	}
	
	@Override
	public void addCible(Position pos) {
		if (!inBounds(pos)) return;
		if (!cibles.contains(pos)) cibles.add(pos);
        }
	
	@Override
	public void setPositionJoueurInitiale(Position pos) {
		if (!inBounds(pos)) return;
		this.positionJoueurInitiale = pos;
        }
	
	@Override
	public Element get(Position pos) {
		if (!inBounds(pos)) return null;
		return grille[pos.getX()][pos.getY()];
        }
	
	public char getNom(){
		return this.nom;
	}
	
	@Override
	public int getX(){
		return grille.length; /*nombre de colonnes*/
	}
	
	@Override
	public int getY(){ 
		return grille[0].length;  /* nombre de lignes */
	}
	
        @Override
        public boolean inBounds(Position p) {
		return p != null
		    && p.getX() >= 0 && p.getX() < getX()
		    && p.getY() >= 0 && p.getY() < getY();
        }
        
        @Override
        public void setElement(Position p, Element e) {
		if (!inBounds(p) || e == null) return;
		grille[p.getX()][p.getY()] = e;
	}
	
	@Override
        public void addCaisse(Position pos, char mondeNom) {
	    // Vérifier que la position est dans les limites
	    if (!inBounds(pos)) {
		return;
	    }
	    
	    // Vérifier si une caisse existe déjà à cette position
	    for (PositionCaisseMonde pc : positionsCaisses) {
		if (pc.getPos().equals(pos)) {
		    return; // Une caisse existe déjà, on ne fait rien
		}
	    }
	    
	    // Ajouter la nouvelle caisse avec son nom de monde
	    positionsCaisses.add(new PositionCaisseMonde(pos, mondeNom));
	}
	
	@Override
        public void removeCible(Position p) {
		if (p == null) return;
		cibles.remove(p);
        }	   
        
        public void addCaisseInitiale(Position p, char mondeNom) {
		if (!inBounds(p)) return;
		
		// Vérifier si une caisse existe déjà à cette position
		for (PositionCaisseMonde pc : positionsCaisses) {
		    if (pc.getPos().equals(p)) {
		        return; // Une caisse existe déjà
		    }
		}
		
		positionsCaisses.add(new PositionCaisseMonde(p, mondeNom));
        }
        
        // Version simplifiée pour caisse simple ($)
        @Override
        public void addCaisseInitiale(Position p) {
        	addCaisseInitiale(p, ' ');
       }
        @Override
        public void removeCaisseInitiale(Position p) {
		if (p == null) return;
		positionsCaisses.removeIf(pc -> pc.getPos().equals(p));
	}

	   
    	
	



}	
											
