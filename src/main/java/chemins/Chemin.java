package chemins;

import modele.Position;
import java.util.List;

/*Interface commune pour les algorithmes de chemin. Implementee par CheminClassique et CheminRecursif.*/
public interface Chemin {

    /**
     * @param depart   la position de depart du joueur
     * @param arrivee  la position d'arrivee 
     * @return une liste de positions representant le chemin,
     *         ou une liste vide si aucun chemin n'existe
     */
    List<Position> trouverChemin(Position depart, Position arrivee);
}
