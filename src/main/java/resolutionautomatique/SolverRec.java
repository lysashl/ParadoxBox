/*
 * SolverRec.java
 * resolution automatique recursive (version simple et heuristique)
 */
package resolutionautomatique;

import modele.ConfigurationRec;
import modele.Direction;
import modele.Position;
import modele.CaisseMonde;
import chemins.Chemin;
import chemins.CheminRecursif;

import java.util.*;

/*
 * Cette classe essaie de trouver une solution automatiquement
 * dans le cas récursif (avec les mondes).
 */
public class SolverRec {

    private ConfigurationRec conf;

    /* limite pour éviter boucle infinie */
    private int profondeurMax = 40;

    public SolverRec(ConfigurationRec conf){
        this.conf = conf;
    }

    /*
     * méthode principale appelée par IndiceSolverRec
     */
    public SolutionRec resoudre(){

        /* pile des mondes (comme dans MainRec) */
        Stack<ConfigurationRec> pileInitiale = new Stack<>();

        /* on commence avec la config de départ */
        pileInitiale.push(new ConfigurationRec(conf));

        /* liste des actions (Z, Q, S, D, E, X) */
        List<Character> chemin = new ArrayList<>();

        /* ensemble des états déjà visités */
        Set<String> visites = new HashSet<>();

        /* lancer la recherche */
        boolean trouve = chercher(pileInitiale, chemin, visites, 0);

        if(trouve){
            return new SolutionRec(chemin);
        }

        /* aucune solution trouvée */
        return new SolutionRec(new ArrayList<>());
    }

    /*
     * méthode récursive qui cherche une solution
     */
    private boolean chercher(Stack<ConfigurationRec> pile,
                             List<Character> chemin,
                             Set<String> visites,
                             int profondeur){

        /* limite pour éviter les boucles infinies */
        if(profondeur > profondeurMax){
            return false;
        }

        /* condition de victoire (monde principal) */
        if(pile.firstElement().victoire()){
            return true;
        }

        /* clé unique pour éviter de revisiter un état */
        String cleEtat = clePile(pile);

        if(visites.contains(cleEtat)){
            return false;
        }

        visites.add(cleEtat);

        /* monde actuel (le sommet de la pile) */
        ConfigurationRec current = pile.peek();

        /* réutilisation de CheminRecursif */
        Chemin cheminLocal = new CheminRecursif(current);

        /*
         * 1. essayer les déplacements (Z Q S D)
         */
        for(Direction d : Direction.getDirections()){

            Position posJoueur = current.getJoueur().getPosition();
            Position posSuivante = posJoueur.add(d);

            /* vérifier que le joueur peut aller là-bas */
            if(current.estVide(posSuivante)){
                List<Position> petitChemin =
                    cheminLocal.trouverChemin(posJoueur, posSuivante);

                if(petitChemin.isEmpty()){
                    continue;
                }
            }

            /* copier la pile des mondes */
            Stack<ConfigurationRec> copiePile = copierPile(pile);
            ConfigurationRec copie = copiePile.peek();

            /* tester le déplacement */
            if(copie.deplacerJoueur(d)){

                List<Character> nouveauChemin = new ArrayList<>(chemin);

                /* ajouter la direction */
                nouveauChemin.add(directionToChar(d));

                /* appel récursif */
                if(chercher(copiePile, nouveauChemin,
                            new HashSet<>(visites),
                            profondeur + 1)){

                    chemin.clear();
                    chemin.addAll(nouveauChemin);
                    return true;
                }
            }
        }

        /*
         * 2. essayer d'entrer dans un monde (E)
         */
        Stack<ConfigurationRec> pileEntree = copierPile(pile);
        ConfigurationRec confEntree = pileEntree.peek();

        ConfigurationRec mondeInterne = mondeAccessible(confEntree);

        if(mondeInterne != null){

            pileEntree.push(mondeInterne);

            List<Character> nouveauChemin = new ArrayList<>(chemin);
            nouveauChemin.add('E');

            if(chercher(pileEntree, nouveauChemin,
                        new HashSet<>(visites),
                        profondeur + 1)){

                chemin.clear();
                chemin.addAll(nouveauChemin);
                return true;
            }
        }

        /*
         * 3. essayer de sortir du monde (X)
         */
        if(pile.size() > 1){

            Stack<ConfigurationRec> pileSortie = copierPile(pile);
            pileSortie.pop();

            List<Character> nouveauChemin = new ArrayList<>(chemin);
            nouveauChemin.add('X');

            if(chercher(pileSortie, nouveauChemin,
                        new HashSet<>(visites),
                        profondeur + 1)){

                chemin.clear();
                chemin.addAll(nouveauChemin);
                return true;
            }
        }

        return false;
    }

    /*
     * trouver un monde dans lequel on peut entrer
     */
    private ConfigurationRec mondeAccessible(ConfigurationRec conf){

        Position posJoueur = conf.getJoueur().getPosition();

        for(Direction d : Direction.getDirections()){

            Position posCaisse = posJoueur.add(d);

            if(conf.estCaisse(posCaisse)){

                CaisseMonde caisse = conf.getCaisse(posCaisse);

                /* entrer seulement si caisse sur cible */
                if(conf.estCible(posCaisse) &&
                   !caisse.estCaisseSimple()){

                    return caisse.getConfInterne();
                }
            }
        }

        return null;
    }

    /*
     * copier la pile des mondes
     */
    private Stack<ConfigurationRec> copierPile(Stack<ConfigurationRec> pileOriginale){

        Stack<ConfigurationRec> nouvellePile = new Stack<>();

        for(ConfigurationRec c : pileOriginale){
            nouvellePile.add(new ConfigurationRec(c));
        }

        return nouvellePile;
    }

    /*
     * convertir direction en caractère
     */
    private char directionToChar(Direction d){
        if(d == Direction.HAUT) return 'Z';
        if(d == Direction.BAS) return 'S';
        if(d == Direction.GAUCHE) return 'Q';
        return 'D';
    }

    /*
     * clé unique pour un état (éviter doublons)
     */
    private String clePile(Stack<ConfigurationRec> pile){

        StringBuilder sb = new StringBuilder();

        for(ConfigurationRec conf : pile){
            sb.append(cleConf(conf)).append("||");
        }

        return sb.toString();
    }

    /*
     * clé d’une configuration
     */
    private String cleConf(ConfigurationRec conf){

        StringBuilder sb = new StringBuilder();

        Position posJoueur = conf.getJoueur().getPosition();

        sb.append(posJoueur.getX())
          .append(",")
          .append(posJoueur.getY());

        for(CaisseMonde caisse : conf.getCaisses()){
            Position p = caisse.getPosition();

            sb.append("|")
              .append(p.getX())
              .append(",")
              .append(p.getY());
        }

        return sb.toString();
    }
}