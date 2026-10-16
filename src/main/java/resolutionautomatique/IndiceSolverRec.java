 /*
 * IndiceSolverRec.java
 * donne un indice pour la version recursive
 */
package resolutionautomatique;

public class IndiceSolverRec {

    private SolverRec solver;

    public IndiceSolverRec(SolverRec solver){
        this.solver = solver;
    }

    public Character getIndice(){

        /* on calcule toute la solution */
        SolutionRec sol = solver.resoudre();

        /* si pas de solution */
        if(sol.getChemin().isEmpty()){
            return null;
        }

        /* on retourne juste la premiere action */
        return sol.getChemin().get(0);
    }
}