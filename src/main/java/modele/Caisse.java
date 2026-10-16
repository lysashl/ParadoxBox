/*une classe qui représente la caisse à déplacer par le joueur*/
package modele;
public class Caisse extends Mobile{
	
	public Caisse(Configuration conf, Position pos){
		super(Type.CAISSE, conf, pos);
	}
	
	public boolean deplacerVers(Direction d){
		if(conf.estVide(getPosition().add(d))){
			setPosition(this.getPosition().add(d));
			return true;
		}
		return false;
	}
}					
