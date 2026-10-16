package modele;
import java.util.ArrayList;
public interface StructurePlateau {
    boolean estVide(Position pos);
    boolean estCible(Position pos);
    Element get(Position pos);
    int getX();
    int getY();
    Position getPositionJoueurInitiale();
    boolean inBounds(Position pos);
    void setElement(Position pos, Element e);
    void addCible(Position pos);
    void removeCible(Position pos);
    void setPositionJoueurInitiale(Position pos);
    public ArrayList<?> getPositionsCaisses();
    public void addCaisseInitiale(Position p);
    public void removeCaisseInitiale(Position p);
    public void addCaisse(Position pos, char mondeNom);
}
