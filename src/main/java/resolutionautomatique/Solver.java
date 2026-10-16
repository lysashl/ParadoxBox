/*
 * Solver.java
 * resolution automatique avec BFS
 */
package resolutionautomatique;

import modele.Configuration;
import modele.Direction;
import modele.Position;
import modele.Caisse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class Solver {

    private Configuration conf;

    public Solver(Configuration conf) {
        this.conf = conf;
    }

    public Solution resoudre() {

        /* file pour BFS */
        Queue<Configuration> queue = new LinkedList<>();

        /* map pour garder le chemin jusqu à chaque etat */
        Map<String, List<Direction>> chemins = new HashMap<>();

        /* on copie la config de depart */
        Configuration debut = new Configuration(conf);
        queue.add(debut);
        chemins.put(cle(debut), new ArrayList<>());

        int max = 10000; /* limite pour eviter boucle infinie */
        int etatsExplores = 0;

        while (!queue.isEmpty()) {

            /* si trop d'etats on stop */
            if (etatsExplores++ > max) {
                return new Solution(new ArrayList<>());
            }

            Configuration current = queue.poll();

            String cleCurrent = cle(current);
            List<Direction> cheminActuel = chemins.get(cleCurrent);

            /* verifier si victoire */
            if (current.victoire()) {
                return new Solution(cheminActuel);
            }

            /* tester les 4 directions */
            for (Direction d : Direction.getDirections()) {

                /* on copie pour ne pas casser l original */
                Configuration copie = new Configuration(current);

                /* on utilise le modele pour deplacer */
                if (copie.deplacerJoueur(d)) {

                    String cleCopie = cle(copie);

                    /* eviter de revisiter meme etat */
                    if (!chemins.containsKey(cleCopie)) {

                        /* on ajoute le mouvement au chemin */
                        List<Direction> nouveauChemin = new ArrayList<>(cheminActuel);
                        nouveauChemin.add(d);

                        queue.add(copie);
                        chemins.put(cleCopie, nouveauChemin);
                    }
                }
            }
        }

        /* si pas de solution */
        return new Solution(new ArrayList<>());
    }

    /*
     * creer une cle unique pour reconnaitre un etat
     * (joueur + positions des caisses)
     */
    private String cle(Configuration conf) {
        StringBuilder sb = new StringBuilder();

        Position posJoueur = conf.getJoueur().getPosition();
        sb.append(posJoueur.getX()).append(",").append(posJoueur.getY()).append("|");

        for (Caisse caisse : conf.getCaisses()) {
            Position p = caisse.getPosition();
            sb.append(p.getX()).append(",").append(p.getY()).append(";");
        }

        return sb.toString();
    }
}