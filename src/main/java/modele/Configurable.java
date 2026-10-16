/* cette interface facilite l'utilisation des méthodes définies dedans pour les deux versions classique et récursive comme ça on aura pas besoin de les implémenter à chaque fois */
package modele;
public interface Configurable {
    /* ces méthodes sont définies dans cofiguration et configurationRec */
    boolean estVide(Position pos);
    boolean estCible(Position pos);
    boolean estCaisse(Position pos);
    Element get(Position pos);
    int getX();
    int getY();
}
