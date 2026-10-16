/* cette classe elle représente le plateau à un instant précis du jeu
 * (c'est une classe qui représente le plateau dynamique contrairement à la classe Plateau
 * qui représente le plateau au début du jeu)
 * elle sert à vérifier à chaque instant du jeu et elle sera utilisée dans la résolution automatique
 */
package modele;
import java.util.ArrayList;

public class Configuration implements Configurable {  

    private ArrayList<Caisse> caisses; /* la liste des caisses présente à ce moment du jeu */
    private Joueur joueur;
    private Plateau plateau;  /* un attribut plateau qui nous permettra d'appeler les méthodes de Plateau */
    private int score;
    private int coups;
    private boolean perdu;
    private int caissesSurCible;
    private int meilleurScore;
    private int niveau;
    
    public Configuration(Plateau plateau) {
        this(plateau, 1);
    }
    /* initialisation du plateau en prenant les informations depuis le plateau initial */
    public Configuration(Plateau plateau, int niveau) {
        if (plateau == null) {
            throw new IllegalArgumentException("Plateau null dans Configuration");
        }
        this.plateau = plateau;
        this.score = 0;
        this.coups = 0;
        this.perdu = false;
        this.caissesSurCible = 0;
        this.meilleurScore = 0;
        this.niveau = niveau;
        initialiserDepuisPlateau(); /* crée joueur + caisses depuis les positions initiales */
    }
    
    /*constructeur pour copier une configuration */
    public Configuration(Configuration other){
    	this.plateau = other.plateau;
    	this.score = other.score;
    	this.coups = other.coups;
    	this.perdu = other.perdu;
    	this.caissesSurCible = other.caissesSurCible;
    	this.meilleurScore = other.meilleurScore;
    	this.niveau = other.niveau;
    	this.joueur = new Joueur(this, other.getJoueur().getPosition());
    	this.caisses = new ArrayList<>();
    	for(Caisse c : other.getCaisses()){
    		this.caisses.add(new Caisse(this, c.getPosition()));
    	    }
    }	    		

    /* on factorise l'initialisation pour pouvoir la réutiliser après reset / édition */
    private void initialiserDepuisPlateau() {

        /* on remet le joueur au départ */
        this.joueur = new Joueur(this, plateau.getPositionJoueurInitiale());

        /* on remet toutes les caisses aux positions initiales */
        this.caisses = new ArrayList<>();
        for (Position pos : plateau.getPositionsCaisses()) {
            this.caisses.add(new Caisse(this, pos));
        }
    }

    
    /* Pour l'ig (RESET)*/
    

    /* reset complet : remet joueur + caisses comme au début du niveau */
     public void resetFromPlateau() {
        coups = 0;
        score = 0;
        perdu = false;
        caissesSurCible = 0;
        initialiserDepuisPlateau();
    }

    /* (optionnel) si l'IG veut charger un autre niveau sans recréer Configuration
     * ex: bouton "charger niveau" -> on change le plateau puis reset
     */
    public void setPlateau(Plateau nouveauPlateau) {
        if (nouveauPlateau == null) {
            throw new IllegalArgumentException("Nouveau plateau null");
        }
        this.plateau = nouveauPlateau;
        initialiserDepuisPlateau();
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
        for (Caisse c : caisses) {
            if (estCible(c.getPosition())) {
                count++;
            }
        }
        return count;
    }
    
    public boolean estVide(Position pos) {

        /* mur (et si jamais Plateau gère hors plateau, sinon il faudra inBounds côté Plateau) */
        if (!plateau.estVide(pos))
            return false;

        /* caisse */
        if (estCaisse(pos))
            return false;

        /* joueur */
        if (joueur.getPosition().equals(pos))
            return false;

        return true;
    }

    public boolean estCible(Position pos) {
        return plateau.estCible(pos);
    }

    public boolean estCaisse(Position pos) {
        for (Caisse c : caisses) {
            if (c.getPosition().equals(pos)) {
                return true;
            }
        }
        return false;
    }

    public boolean victoire() {
        /* vérifier que toutes les caisses sont sur des cibles */
        for (Caisse c : caisses) {
            if (!estCible(c.getPosition())) {
                return false;
            }
        } 
        return true;
    }
    
    
    
    private boolean caisseEstBloquee(Caisse caisse) {
	    Position pos = caisse.getPosition();
	    if (estCible(pos)) return false;
	   
	    boolean hautBloque   = plateau.get(pos.add(Direction.HAUT)).getType()   == Type.MUR;
	    boolean basBloque    = plateau.get(pos.add(Direction.BAS)).getType()    == Type.MUR;
	    boolean gaucheBloque = plateau.get(pos.add(Direction.GAUCHE)).getType() == Type.MUR;
	    boolean droiteBloque = plateau.get(pos.add(Direction.DROITE)).getType() == Type.MUR;

	    return (hautBloque && gaucheBloque) ||
		   (hautBloque && droiteBloque) ||
		   (basBloque  && gaucheBloque) ||
		   (basBloque  && droiteBloque);
   }
    
       
    public boolean verifierDefaite() {
	    for (Caisse c : caisses) {
		if (!estCible(c.getPosition()) && caisseEstBloquee(c)) {
		    perdu = true;
		    return true;
		}
	    }
	    
	    return false;
    }
    
    public void setCoups(int coups){
    	this.coups = coups;
    }	
    	
    public Element get(Position pos) {
        if (joueur.getPosition().equals(pos)) {
            return joueur;
        }
        for (Caisse c : caisses) {
            if (c.getPosition().equals(pos)) {
                return c;
            }
        }
        return plateau.get(pos);
    }

    public int getX() {
        return plateau.getX();
    }

    public int getY() {
        return plateau.getY();
    }

    public Joueur getJoueur() {
        return joueur;
    }

    public ArrayList<Caisse> getCaisses() {
        return caisses;
    }

    public Plateau getPlateau() {
        return plateau;
    }
    
    public int getScore() {
    	return score; //-1 par coups *50 par nombre de caisses sur cible + 200 bonus de victoire + 100 si nombre de coups < 20
    }
    
    public int getCoups() {
    	return coups;
    }
    
    public boolean estPerdu() {
    	return perdu;
    }
    
    public boolean vientDeGagner(){
    	return victoire() && !perdu;
    }
    
    public int getDernierScoreAjoute(){
    	return 0;
    }
    public int getNiveau() { 
    	return niveau;
    }		
}
