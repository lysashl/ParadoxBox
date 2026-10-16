package modele;
import java.io.IOException;
import java.util.Scanner;
import java.util.Stack;

public class MainRec {
    
    public static void main(String[] args) throws IOException {
        // Charge le plateau
        PlateauRec plateauRec = new PlateauRec("modele/niveau_rec.txt");
        
        // Récupère le monde principal
        char mondePrincipal = plateauRec.getMondePrincipal();
        Monde monde = plateauRec.getMonde(mondePrincipal);
        
        if (monde == null) {
            System.out.println("ERREUR: Monde principal non trouvé!");
            return;
        }
        
        if (monde.getPositionJoueurInitiale() == null) {
            System.out.println("ERREUR: Pas de joueur dans le monde principal!");
            return;
        }
        
        // Crée la configuration principale
        ConfigurationRec configPrincipale = new ConfigurationRec(monde, plateauRec);
        
        // Pile pour gérer les mondes
        Stack<ConfigurationRec> pileMondes = new Stack<>();
        pileMondes.push(configPrincipale);
        
        Scanner sc = new Scanner(System.in);
        boolean jeuTermine = false;
        
        while (!jeuTermine && !pileMondes.isEmpty()) {
            // Récupère la configuration courante
            ConfigurationRec configActuelle = pileMondes.peek();
            
            // Affiche le monde
            afficherMonde(configActuelle);
            
            // Affiche le nom du monde actuel
            String nomMonde = configActuelle.getMonde().getNom() == ' ' ? 
                              "Principal" : 
                              "Monde " + configActuelle.getMonde().getNom();
            System.out.println("Monde actuel: " + nomMonde);
            System.out.print("Z/Q/S/D, E(entrer dans monde), X(sortir): ");
            
            String input = sc.nextLine();
            if (input.isEmpty()) continue;
            
            char cmd = Character.toUpperCase(input.charAt(0));
            
            // Gestion des commandes
            if (cmd == 'E') {
                // Entrer dans une caisse-monde
                Position posJoueur = configActuelle.getJoueur().getPosition();
                boolean entre = false;
                
                // Vérifie les 4 directions autour du joueur
                Direction[] directions = {
                    Direction.HAUT, Direction.BAS, 
                    Direction.GAUCHE, Direction.DROITE
                };
                
                for (Direction d : directions) {
                    Position posCaisse = posJoueur.add(d);
                    
                    if (configActuelle.estCaisse(posCaisse)) {
                        CaisseMonde caisse = configActuelle.getCaisse(posCaisse);
                        
                        // Vérifie si la caisse est sur une cible ET contient un monde
                        if (configActuelle.estCible(posCaisse) && !caisse.estCaisseSimple()) {
                            ConfigurationRec mondeInterne = caisse.getConfInterne();
                            if (mondeInterne != null) {
                                pileMondes.push(mondeInterne);
                                System.out.println(" Vous entrez dans le monde " + caisse.getMondeNom());
                                entre = true;
                                break;
                            }
                        }
                    }
                }
                
                if (!entre) {
                    System.out.println(" Aucune caisse-monde accessible! (Il faut être à côté d'une caisse sur cible)");
                }
            }
            else if (cmd == 'X') {
                // Sortir du monde actuel
                if (pileMondes.size() > 1) {
                    pileMondes.pop();
                    System.out.println(" Vous retournez au monde précédent");
                } else {
                    System.out.println("Vous êtes déjà dans le monde principal!");
                }
            }
            else {
                // Déplacement du joueur
                Direction d = null;
                switch (cmd) {
                    case 'Z': d = Direction.HAUT; break;
                    case 'S': d = Direction.BAS; break;
                    case 'Q': d = Direction.GAUCHE; break;
                    case 'D': d = Direction.DROITE; break;
                    default:
                        System.out.println("Commande invalide!");
                        continue;
                }
                
                boolean deplace = configActuelle.deplacerJoueur(d);
                if (!deplace) {
                    System.out.println("Impossible de se déplacer dans cette direction!");
                }
            }
            
            // Vérifie la victoire (seulement dans le monde principal)
            if (configPrincipale.victoire()) {
                System.out.println("\nFÉLICITATIONS ! VOUS AVEZ GAGNÉ ! ");
                jeuTermine = true;
            }
        }
        
        sc.close();
        System.out.println("Merci d'avoir joué !");
    }
    
    public static void afficherMonde(ConfigurationRec config) {
        System.out.println();
        for (int y = 0; y < config.getY(); y++) {
            for (int x = 0; x < config.getX(); x++) {
                Position pos = new Position(x, y);
                
                if (config.getJoueur().getPosition().equals(pos)) {
                    System.out.print("@");
                } 
                else if (config.estCaisse(pos)) {
                    CaisseMonde caisse = config.getCaisse(pos);
                    char c = caisse.getMondeNom();
                    
                    if (config.estCible(pos)) {
                        // Caisse sur cible : afficher en minuscule
                        System.out.print(c != ' ' ? Character.toLowerCase(c) : '*');
                    } else {
                        // Caisse normale
                        System.out.print(c != ' ' ? c : '$');
                    }
                }
                else if (config.estCible(pos)) {
                    System.out.print(".");
                }
                else {
                    Element e = config.getMonde().get(pos);
                    System.out.print(e.getType() == Type.MUR ? "#" : " ");
                }
            }
            System.out.println();
        }
        System.out.println();
    }
}
