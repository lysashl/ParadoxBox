package modele;

import java.io.IOException;
import java.util.Scanner;
import java.io.File;
import java.util.Arrays;

public class Main {

    public static void main(String[] args) throws IOException {
        Scanner sc = new Scanner(System.in);
        
        // Chercher dans le dossier modele/
        File dossier = new File("modele");
        File[] fichiers = dossier.listFiles((dir, name) -> name.endsWith(".txt"));
        
        if (fichiers == null || fichiers.length == 0) {
            System.out.println("Aucun fichier .txt trouvé dans modele/ !");
            return;
        }
        
        // Trier les fichiers par ordre alphabétique
        Arrays.sort(fichiers, (f1, f2) -> f1.getName().compareTo(f2.getName()));
        
        System.out.println("=== NIVEAUX DISPONIBLES ===");
        for (int i = 0; i < fichiers.length; i++) {
            System.out.println((i+1) + ". " + fichiers[i].getName());
        }
        System.out.print("Choix : ");
        
        int choix = Integer.parseInt(sc.nextLine()) - 1;
        if (choix < 0 || choix >= fichiers.length) {
            System.out.println("Choix invalide, premier niveau par défaut");
            choix = 0;
        }
        
        String fichierNiveau = "modele/" + fichiers[choix].getName();
        System.out.println("Chargement de : " + fichierNiveau);
        
        // Charger et jouer
        Plateau plateau = new Plateau(fichierNiveau);
        Configuration config = new Configuration(plateau);
        
        boolean finJeu = false;

        while (!finJeu) {
            // Afficher le plateau
            for (int y = 0; y < config.getY(); y++) {
                for (int x = 0; x < config.getX(); x++) {
                    Position pos = new Position(x, y);
                    if (config.getJoueur().getPosition().equals(pos)) {
                        System.out.print("@");
                    } else if (config.estCaisse(pos)) {
                        System.out.print(config.estCible(pos) ? "*" : "$");
                    } else if (config.estCible(pos)) {
                        System.out.print(".");
                    } else {
                        System.out.print(config.getPlateau().get(pos).getType() == Type.MUR ? "#" : " ");
                    }
                }
                System.out.println();
            }
            
            System.out.println("Score: " + config.getScore() + "  Coups: " + config.getCoups());

            // Vérifier défaite
            if (config.estPerdu()) {
                System.out.println("PERDU !");
                System.out.print("Recommencer ? (o/n) : ");
                if (sc.nextLine().equalsIgnoreCase("o")) {
                    config.resetFromPlateau();
                } else {
                    finJeu = true;
                }
                continue;
            }

            // Vérifier victoire
            if (config.victoire()) {
                System.out.println("VICTOIRE ! Score: " + config.getScore());
                finJeu = true;
                continue;
            }

            // Demander déplacement
            System.out.print("Z/S/Q/D : ");
            String line = sc.nextLine();
            if (line.isEmpty()) continue;
            
            char move = Character.toUpperCase(line.charAt(0));
            Direction d = null;

            switch (move) {
                case 'Z': d = Direction.HAUT; break;
                case 'S': d = Direction.BAS; break;
                case 'Q': d = Direction.GAUCHE; break;
                case 'D': d = Direction.DROITE; break;
                default:
                    System.out.println("Touche invalide !");
                    continue;
            }

            config.deplacerJoueur(d);
        }
        
        sc.close();
    }
}
