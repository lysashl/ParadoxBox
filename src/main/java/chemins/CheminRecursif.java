package chemins;

import modele.ConfigurationRec;
import modele.Direction;
import modele.Position;

import java.util.*;

/**
 * Algorithme BFS oriente (B.2) pour le Sokoban recursif.
 * Trouve le plus court chemin sans bouger de caisse,
 * en tenant compte des mondes imbriques (CaisseMonde).
 *
 * Difference avec CheminClassique :
 * - le joueur peut entrer dans une CaisseMonde
 * - le graphe est oriente : entrer dans une boite ne veut pas
 *   dire qu'on peut en sortir par le meme chemin
 */
public class CheminRecursif implements Chemin {

    // la configuration recursive du jeu
    private ConfigurationRec conf;

    // constructeur : on passe la configuration recursive
    public CheminRecursif(ConfigurationRec conf) {
        this.conf = conf;
    }

    private boolean estLibre(Position p,Direction d){
     return conf.estVide(p.add(d));
    }
    
    private Position suivante(Position p,Direction d){
     return p.add(d);
    }
    
   @Override
   public List<Position> trouverChemin(Position depart, Position arrivee) {

        // file qui contient tous les chemins en cours d'exploration
        Queue<List<Position>> cheminParcourus = new LinkedList<>();

        // ensemble des cases deja visitees pour eviter de repasser dessus
        Set<String> visite = new HashSet<>();

        // on cree le chemin initial qui contient uniquement le depart
        List<Position> cheminInitial = new ArrayList<>();
        cheminInitial.add(depart);
        cheminParcourus.add(cheminInitial);

        // on marque le depart comme visite
        visite.add(cle(depart));

        // tant qu'il reste des chemins a explorer
        while (!cheminParcourus.isEmpty()) {

            // on prend le premier chemin de la file
            List<Position> cheminActuel = cheminParcourus.poll();

            // on recupere la derniere position du chemin
            Position actuelle = cheminActuel.get(cheminActuel.size() - 1);

            // si on est arrive a destination on retourne le chemin
            if (actuelle.equals(arrivee)) {
                return cheminActuel;
            }

            // on regarde les 4 cases voisines (gauche, droite, haut, bas)
            for (Direction d : Direction.getDirections()) {
               
               if(!estLibre(actuelle,d)){
                continue;
               }
               
               Position voisine=suivante(actuelle,d);
               
               if(!conf.getMonde().inBounds(voisine)){
                continue;
               }
                 

                // si case deja visitee on passe
                if (visite.contains(cle(voisine))) {
                    continue;
                }
                
                
                // si case en dehors du plateau on passe
                if (!conf.getMonde().inBounds(voisine)) {
                    continue;
                }

                // case libre : on peut y aller
                visite.add(cle(voisine));
                List<Position> nouveauChemin = new ArrayList<>(cheminActuel);
                nouveauChemin.add(voisine);
                cheminParcourus.add(nouveauChemin);
            }
        }

        // aucun chemin trouve on retourne une liste vide
        return new ArrayList<>();
    }

    // transforme une position en texte unique ex: (3,5) -> "3,5"
    // utilise pour savoir si une case a deja ete visitee
    private String cle(Position pos) {
        return pos.getX() + "," + pos.getY();
    }
}
