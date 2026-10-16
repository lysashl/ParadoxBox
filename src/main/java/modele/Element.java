package modele;
public abstract class Element{
	protected Type type;
	
	public Element(Type type){
		this.type = type;
	}
	
	public Type getType(){
		return type;
	}
	
	public abstract boolean deplacerVers(Direction d);
}				
