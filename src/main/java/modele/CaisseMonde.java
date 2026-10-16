/* une classe qui représente une caisse qui contient un monde */
package modele;
public class CaisseMonde extends Mobile {
	private char mondeNom;
	private ConfigurationRec confInterne; /* un attribut qui définit l'état du monde intérieur de la caisse */
	
	public CaisseMonde(Configurable conf, Position pos, char mondeNom, PlateauRec plateauRec) { 	/*...*/
		super(Type.CAISSE, conf, pos); /* ce constructeur hérite du constructeur de la classe Mobile pour créer une caisse mobile puis on ajoute le nom du monde et la configuration interne */
		this.mondeNom = mondeNom;
		
		if(mondeNom != ' '){ /* caisse contenant un monde */
			Monde monde = plateauRec.getMonde(mondeNom); /* récuperer le monde depuis le plateau */
			this.confInterne = new ConfigurationRec(monde, plateauRec);
		}
	}
	
	public boolean deplacerVers (Direction d){
		Position nouvellePos = getPosition().add(d);
		if(conf.estVide(nouvellePos)){
			setPosition(nouvellePos);
			return true;
		}
		return false;
	}
	
	public ConfigurationRec getConfInterne(){
		return this.confInterne;
	}		
	
	public char getMondeNom(){
		return mondeNom;
	}
	
	public boolean estCaisseSimple(){
		return mondeNom == ' ';
	}
	
	/* setter pour la config interne */
	public void setConfInterne(ConfigurationRec confInterne){
	    this.confInterne = confInterne;
	}
}			
			 
