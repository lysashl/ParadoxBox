/*une classe qui représente l'état du plateau recursif à chaque moment du jeu et l'état des mondes */
package modele;
import java.util.ArrayList;

public class ConfigurationRec implements Configurable{
	private ArrayList<CaisseMonde> caisses;
	private JoueurRec joueur;
	private Monde monde;
	private PlateauRec plateauRec;
	private ConfigurationRec parent; /* le monde parent du monde actuel */
	private boolean perdu;
	private int coups;
	private int score;
	
	/* constructeur pour le monde principla */
	public ConfigurationRec(Monde monde, PlateauRec plateauRec){
		this(monde, plateauRec, null); /* null -> y a pas de parent */
	}
	
	public ConfigurationRec(Monde monde, PlateauRec plateauRec, ConfigurationRec parent){
		this.monde = monde;
		this.plateauRec = plateauRec;
		this.parent = parent; 
		this.coups = 0;
		this.score = 0;
		
		
		/* initialisation du joueur */
		Position posJoueur = monde.getPositionJoueurInitiale();
		if (posJoueur == null){
			this.joueur = new JoueurRec(this, new Position(1,1));
		}
		else{
			this.joueur = new JoueurRec(this, posJoueur);
		}
		
		/* initialisation des caisses */
		this.caisses = new ArrayList<>();
		if (monde.getPositionsCaisses() != null){
			for(PositionCaisseMonde posCaisse : monde.getPositionsCaisses()){
				if(posCaisse  != null && posCaisse.getPos() != null){
					this.caisses.add(new CaisseMonde(this, posCaisse.getPos(), posCaisse.getMondeNom(), plateauRec));
				}
			}
		}
	}
	
	/* contructeur pour copier une configurationt */
	public ConfigurationRec(ConfigurationRec other){
		this.monde = other.monde;
		this.plateauRec = other.plateauRec;
		this.parent = other.parent;
		this.perdu = other.perdu; 
		this.coups = other.coups;
		this.score = other.score;
		 
		this.joueur = new JoueurRec(this, other.getJoueur().getPosition());
		this.caisses = new ArrayList<>();
		for(CaisseMonde c : other.getCaisses()){
			this.caisses.add(new CaisseMonde(this, c.getPosition(), c.getMondeNom(), this.plateauRec));
		}
	}		
			
	public boolean deplacerJoueur(Direction d) {
    	    if (perdu) return false;

	    int avant = compterCaissesSurCible();
	    boolean deplace = joueur.deplacerVers(d);

	    if (deplace) {
		coups++;
		score--; // pénalité par coup

		int apres = compterCaissesSurCible();
		if (apres > avant) {
		    int nouvelles = apres - avant;
		    score += nouvelles * 50; // 50 points par nouvelle caisse sur cible
		}

		verifierDefaite();

		// Vérifier la victoire et appliquer les bonus
		if (victoire()) {
		    score += 200; // bonus de base
		    if (coups < 20) {
		        score += 100; // bonus rapidité
		    }
		}
	    }

	    return deplace;
	}
	
	
	private int compterCaissesSurCible() {
	    int count = 0;
	    for (CaisseMonde c : caisses) {
		if (estCible(c.getPosition())) {
		    count++;
		}
	    }
	    return count;
	}
	
	public boolean estVide(Position pos) {
        	if (pos == null){
        		return false;
        	}	
        	if (!monde.estVide(pos)){
        		return false;
        	}	
        	if (estCaisse(pos)){
        		return false;
        	}	
        	if (joueur != null && joueur.getPosition() != null && joueur.getPosition().equals(pos)){
        		return false;
        	}	
        	return true;
    	}
    	
    	public boolean estCible(Position pos) {
        	if (pos == null) return false;
        	return monde.estCible(pos);
    	}
    	
    	public boolean estCaisse(Position pos) {
		if (pos == null || caisses == null) return false;
		for (CaisseMonde c : caisses) {
		    if (c != null && c.getPosition() != null && c.getPosition().equals(pos)) return true;
		}
		return false;
	}
	
	public CaisseMonde getCaisse(Position pos) {
		if (pos == null || caisses == null) return null;
		for (CaisseMonde c : caisses) {
		    if (c != null && c.getPosition() != null && c.getPosition().equals(pos)) return c;
		}
		return null;
    	}	
    	
    	
    	public boolean victoire() {
		// Vérifie que toutes les caisses sont sur des cibles
		if (caisses != null) {
		    for (CaisseMonde c : caisses) {
		        if (c == null) continue;
		        if (!estCible(c.getPosition())) {
		            return false;
		        }
		    }
		    
		    // Vérifie récursivement les mondes internes
		    for (CaisseMonde c : caisses) {
		        if (c == null) continue;
		        if (!c.estCaisseSimple() && c.getConfInterne() != null) {
		            if (!c.getConfInterne().victoire()) { /* la récursivité */
		                return false;
		            }
		        }
		    }
		}
		
		return true;
    	}
    	
    	
    	/*private boolean caisseEstBloquee(CaisseMonde caisse) {
    		Position pos = caisse.getPosition();
    
		// Si la caisse est sur une cible, elle n'est pas bloquée
		if (estCible(pos)) {
			return false;
		}
		    
		// Vérifier les 4 directions
		boolean hautBloque = !estVide(pos.add(Direction.HAUT));
		boolean basBloque = !estVide(pos.add(Direction.BAS));
		boolean gaucheBloque = !estVide(pos.add(Direction.GAUCHE));
		boolean droiteBloque = !estVide(pos.add(Direction.DROITE));
		    
		// Bloquée si dans un coin (deux directions adjacentes bloquées)
		if ((hautBloque && gaucheBloque) || (hautBloque && droiteBloque) ||
			(basBloque && gaucheBloque) || (basBloque && droiteBloque)) {
			return true;
		}
		    
		// Bloquée si entourée de tous les côtés
		if (hautBloque && basBloque && gaucheBloque && droiteBloque) {
			return true;
		    }
		    
		return false;
	} 
	*/
	
	private boolean caisseEstBloquee(CaisseMonde caisse) {
	    Position pos = caisse.getPosition();
	    
	    // Si la caisse est sur une cible, elle n'est pas bloquée
	    if (estCible(pos)) {
		return false;
	    }
	    
	    // Vérifier les 4 directions (en utilisant monde au lieu de plateau)
	    boolean hautBloque = monde.get(pos.add(Direction.HAUT)).getType() == Type.MUR;
	    boolean basBloque = monde.get(pos.add(Direction.BAS)).getType() == Type.MUR;
	    boolean gaucheBloque = monde.get(pos.add(Direction.GAUCHE)).getType() == Type.MUR;
	    boolean droiteBloque = monde.get(pos.add(Direction.DROITE)).getType() == Type.MUR;
	    
	    // Une caisse est bloquée si elle est dans un coin (deux directions adjacentes bloquées)
	    return (hautBloque && gaucheBloque) ||
		   (hautBloque && droiteBloque) ||
		   (basBloque && gaucheBloque) ||
		   (basBloque && droiteBloque);
	}
	

	public boolean verifierDefaite() {
	    for (CaisseMonde c : caisses) {
		if (!estCible(c.getPosition()) && caisseEstBloquee(c)) {
		    perdu = true;
		    return true;
		}
	    }
	    return false;
	}
    
	
	public Element get(Position pos) {
		if (pos == null) return null;
		
		if (joueur != null && joueur.getPosition() != null && joueur.getPosition().equals(pos)) {
		    return joueur;
		}
		
		if (caisses != null) {
		    for (CaisseMonde c : caisses) {
		        if (c != null && c.getPosition() != null && c.getPosition().equals(pos)) {
		            return c;
		        }
		    }
		}
		
		if (monde != null) {
		    return monde.get(pos);
		}
		
		return null;
    	}    	
    	
    	public int getX() { 
	    if (monde != null) {
		return monde.getX();
	    } else {
		return 0;
	    }
	}

	public int getY() { 
	    if (monde != null) {
		return monde.getY();
	    } else {
		return 0;
	    }
	}				
	
	
	public JoueurRec getJoueur() { 
        	return joueur; 
    	}
    
    	public ArrayList<CaisseMonde> getCaisses() { 
        	return caisses; 
    	}
    
        public Monde getMonde() { 
        	return monde; 
    	}
    
        public ConfigurationRec getParent() { 
        	return parent; 
    	}
    	public boolean estPerdu() { 
    		return perdu;
    	}
    	
    	public int getCoups() {
    		return coups;
	}
	
	public int getScore() {
   		 return score;
	}	
    	
}    			 
