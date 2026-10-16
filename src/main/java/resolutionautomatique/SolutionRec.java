/*
 * SolutionRec.java
 * stocke la solution du jeu sous forme d'actions (Z Q S D E X)
 */
package resolutionautomatique;

import java.util.List;

public class SolutionRec {

    /* liste des actions a faire pour gagner */
    private List<Character> chemin;

    public SolutionRec(List<Character> chemin){
        this.chemin = chemin;
    }

    /* retourne toute la solution */
    public List<Character> getChemin(){
        return chemin;
    }

    /* affiche la solution dans le terminal */
    public void afficher(){
        System.out.print("Solution recursive: ");
        for(char c : chemin){
            System.out.print(c + " ");
        }
        System.out.println();
    }
}