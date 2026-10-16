package chemins;

import modele.Configuration;
import modele.Direction;
import modele.Position;

import java.util.*;

/** Algorithme B1 trouver le plus court chemin sur une grille 2D sans bouger de caisse*/

public class CheminClassique implements Chemin{

private Configuration conf;

public CheminClassique (Configuration conf){
 this.conf = conf;
 }
 
 
@Override
public List<Position> trouverChemin(Position depart, Position arrivee){
 
 // file qui contient tous les chemins parcourus
 Queue<List<Position>> CheminParcourus = new LinkedList<>();
 
 //ensemble des cases deja visitees
 Set<String> visite = new HashSet<>();
 
 //on cree le chemin initial qui pour linstant contient uniquement le depart
 List<Position> CheminInitial = new ArrayList<>();
 CheminInitial.add(depart);
 CheminParcourus.add(CheminInitial);
 //on marque depart comme visitee
 visite.add(cle(depart));
 
while(!CheminParcourus.isEmpty()){
 
  //on prend le premier chemin de la file  
  List<Position> CheminActuel = CheminParcourus.poll();
  
  //on recupere la derniere position du chemin
  Position actuelle = CheminActuel.get(CheminActuel.size()-1);
 
  //si on est arriver a l'arrivee on retourne le chemin 
 if(actuelle.equals(arrivee)){
  return CheminActuel;
 }
 
  //on regarde les 4 cases voisines
 for(Direction d : Direction.getDirections()){
  Position voisine = actuelle.add(d);
  
  //si case deja viitee on continue
 if(visite.contains(cle(voisine))){
   continue;
 }
 
 //si case en dehors du plateau on passe
 if(voisine.getX() <0 || voisine.getX() >= conf.getX() || voisine.getY() < 0 || voisine.getY() >= conf.getY()){
  continue;
 }
 
 //si la case est un mur une caisse ou c'est pas l'arrivee on passe
 if(!conf.estVide(voisine) && !voisine.equals(arrivee)){
  continue;
 }  
 
 //on marque la case voisine comme visitee
 visite.add(cle(voisine));

  // on cree un nouveau chemin en copiant le chemin actuel
 // et on ajoute la case voisine a la fin
  List<Position> NouveauChemin = new ArrayList<>(CheminActuel);
  NouveauChemin.add(voisine);
  
  //on ajoute ce nouveau chemin a notre chemin parcours
  CheminParcourus.add(NouveauChemin);
  }
 }
 
 //si aucun chemin trouver on retourne une liste vide
 return new ArrayList<>();
 }
     private String cle(Position pos) {
        return pos.getX() + "," + pos.getY();
    }
} 
