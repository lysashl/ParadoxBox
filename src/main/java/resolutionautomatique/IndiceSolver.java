/*
 * IndiceSolver.java
 * donne un indice simple au joueur
 */
package resolutionautomatique;

import modele.Configuration;
import modele.Direction;
import java.util.List;

public class IndiceSolver {

    private Solver solver;

    public IndiceSolver(Configuration conf) {
        this.solver = new Solver(conf);
    }

    public Direction getIndice() {

        /* on calcule toute la solution */
        Solution sol = solver.resoudre();
        List<Direction> chemin = sol.getChemin();

        /* si pas de solution */
        if (chemin.isEmpty()) {
            return null;
        }

        /* on retourne juste le premier mouvement */
        return chemin.get(0);
    }
}