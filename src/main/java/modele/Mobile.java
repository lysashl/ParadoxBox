package modele;
public abstract class Mobile extends Element {
    protected Configurable conf;  /* ça peut etre la configuration classique ou recursive c'est pour ça qu'on a utilisé une interface */
    private Position pos;
    
    public Mobile(Type type, Configurable conf, Position pos){
        super(type);
        this.conf = conf;
        this.pos = pos;
    }
    
    public boolean setPosition(Position posi){
        if(conf.estVide(posi)){
            this.pos = new Position(posi);
            return true;
        }
        return false; 
    }
    
    public boolean setPositionSansVerfication(Position posi){
    	this.pos = new Position(posi);
    	return true;
    }	
    
    public Position getPosition(){
        return this.pos;
    }
}
