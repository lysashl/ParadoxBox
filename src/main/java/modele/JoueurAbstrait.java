package modele;
import java.util.ArrayList;
/* une intérface pour définir les méthodes de Joueur et JoueurRec */
public abstract class JoueurAbstrait extends Mobile {
    protected ArrayList<Direction> historique;
    
    public JoueurAbstrait(Configurable conf, Position pos, Type type) {
        super(type, conf, pos);
        this.historique = new ArrayList<>();
    }
    
        public abstract boolean deplacerVers(Direction d);
    
    public ArrayList<Direction> getHistorique() {
        return historique;
    }
}
