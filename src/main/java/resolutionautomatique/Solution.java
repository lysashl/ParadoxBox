/*
 * Solution.java
 * represente le chemin du joueur
 */
package resolutionautomatique;

import modele.Direction;
import java.util.List;

public class Solution {

    private List<Direction> chemin;

    
    public Solution(List<Direction> chemin){
        this.chemin = chemin;
    }

    /* retourne la liste */
    public List<Direction> getChemin(){
        return chemin;
    }

    /* afficher solution Z S Q D */
    public void afficher(){
        System.out.print("Solution: ");
        for(Direction d : chemin){
            if(d == Direction.HAUT) System.out.print("Z ");
            else if(d == Direction.BAS) System.out.print("S ");
            else if(d == Direction.GAUCHE) System.out.print("Q ");
            else if(d == Direction.DROITE) System.out.print("D ");
        }
        System.out.println();
    }
}