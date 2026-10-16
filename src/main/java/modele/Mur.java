package modele;

public class Mur extends Immobile {
	public Mur(){
		super(Type.MUR);
	}
	
	public boolean deplacerVers(Direction d){
		return false;
	}
}			
