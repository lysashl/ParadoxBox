package modele;

public class JoueurRec extends JoueurAbstrait {
	public JoueurRec(ConfigurationRec conf, Position pos) {
		super(conf, pos, Type.JOUEUR);
	    }
	    
	    @Override
	    public boolean deplacerVers(Direction d) {
		Position nouvellePosition = this.getPosition().add(d);
		
		if (conf.estCaisse(nouvellePosition)) {
		    CaisseMonde caisse = ((ConfigurationRec)conf).getCaisse(nouvellePosition);
		    if (conf.estVide(nouvellePosition.add(d))) {
		        if (caisse.deplacerVers(d)) {
		            setPosition(nouvellePosition);
		            historique.add(d);
		            return true;
		        }
		    }
		} else if (conf.estVide(nouvellePosition)) {
		    setPosition(nouvellePosition);
		    historique.add(d);
		    return true;
		}
		return false;
	    }
}
