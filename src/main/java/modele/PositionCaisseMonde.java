package modele;

/* une classe interne utilisée pour représenter les positions des caisses dans le monde */
public class PositionCaisseMonde {
    private Position pos;
    private char mondeNom;
    public PositionCaisseMonde(Position pos, char mondeNom) {
        this.pos = pos;
        this.mondeNom = mondeNom;
    }
    public Position getPos() {
    	return pos;
    }
    public char getMondeNom() { 
    	return mondeNom;
    }
}	

