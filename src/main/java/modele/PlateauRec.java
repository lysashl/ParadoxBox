package modele;

import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class PlateauRec {
    private HashMap<Character, Monde> mondes;
    private char mondePrincipal;
    
    public PlateauRec(String fichier) throws IOException {
        mondes = new HashMap<>();
        charger(fichier);
    }
    private void charger(String fichier) throws IOException {
        InputStream is = PlateauRec.class.getResourceAsStream("/" + fichier);

    	if (is == null) {
        	throw new IOException("Fichier niveau récursif introuvable : " + fichier);
    	}

    	Scanner sc = new Scanner(is);
        ArrayList<String> lignes = new ArrayList<>();
        char mondeCourant = ' '; /* le monde principal n'est pas contenu dans une caisse il est representé pas un espace*/
        
       while (sc.hasNextLine()) { /* une boucle sur chaque ligne du fichier */
            String ligne = sc.nextLine(); /* on lit la prochaine ligne du fichier et on la stocke dans ligne */
            if (ligne.startsWith("[") && ligne.endsWith("]")) { /* ça veut dire qu'on est à la ligne contenant le caractere signifiant le nom du monde */
                if (!lignes.isEmpty()) { /* on a des lignes en attente dans notre liste */
                    construireMonde(mondeCourant, lignes); /* on construit le monde précédent */
                    lignes.clear();  /* on vide la liste pour le prochain monde */
                }
                mondeCourant = ligne.charAt(1); /* on recupere le caractere contenant le nom du monde */
            } else if (!ligne.trim().isEmpty()) {  /* si ce n'est pas une ligne qui représente l'entete du monde et qu'elle n'est pas vide apres avoir enlevé les espaces */
                lignes.add(ligne); /* on ajoute la ligne à la liste */
            }
        }
        if (!lignes.isEmpty()) construireMonde(mondeCourant, lignes); /* construire le tout dernier monde */
        sc.close();
        
        if (mondes.containsKey(' ')) {
    		mondePrincipal = ' '; /* recuperer le monde principale (qui n'a pas un nom) s'il existe */
	} else {
    		mondePrincipal = mondes.keySet().iterator().next();  /* sinon le premier monde rencontré */
	}
    }
    
    
    private void construireMonde(char nom, ArrayList<String> lignes) {
	    Monde monde = new Monde(nom);
	    int h = lignes.size(), w = lignes.get(0).length();
	    Element[][] grille = new Element[w][h];
	    
	    monde.setGrille(grille);  
	    
	    for (int y = 0; y < h; y++) {
		String ligne = lignes.get(y);
		for (int x = 0; x < w; x++) {
		    char c = ligne.charAt(x);
		    Position pos = new Position(x, y);
		    
		    if (c == '#') {
		        grille[x][y] = new Mur();
		    } else {
		        grille[x][y] = new Case();
		        if (c == '.') {
		            monde.addCible(pos);          
		        } else if (c == '@') {
		            monde.setPositionJoueurInitiale(pos); 
		        } else if (c >= 'A' && c <= 'Z') {
		            monde.addCaisse(pos, c);      
		        } else if (c == '$') {
		            monde.addCaisse(pos, ' ');    
		        }
		    }
		}
	    }
	    
	    // Plus besoin de setGrille ici (déjà fait)
	    mondes.put(nom, monde);
    }
    
    public Monde getMonde(char nom) {
    	return mondes.get(nom);
    }
    
    public char getMondePrincipal() {
    	return mondePrincipal;
    }
}
    
