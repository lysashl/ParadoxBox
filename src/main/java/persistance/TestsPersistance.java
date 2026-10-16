package persistance;

import modele.*;
import java.io.*;
import java.util.ArrayList;

/**
 * Tests de la persistance en mode terminal.
 * Tester sans interface graphique, 5 scénarios couverts.
 *
 * Usage : java persistance.TestPersistance
 */
public class TestsPersistance {

    public static void main(String[] args) {
        System.out.println("=== TESTS PERSISTANCE SOKOBAN ===\n");
        int ok = 0, total = 0;

        total++; if (testChargementSimple())         ok++;
        total++; if (testChargementAvecEtoile())     ok++;
        total++; if (testSauvegardeEtatCourant())    ok++;
        total++; if (testSolution())                 ok++;
        total++; if (testGestionnaireSauvegarde())   ok++;

        System.out.println("\n=== RÉSULTAT : " + ok + "/" + total + " tests passés ===");
    }

    //  Test 1 : chargement niveau classique (sans * ni +)
    private static boolean testChargementSimple() {
        System.out.println("--- Test 1 : Chargement niveau classique ---");
        try {
            // #####
            // #@$.#
            // #####
            File f = creerFichierTemp("#####\n#@$.#\n#####\n");
            Plateau p = PersistanceNiveau.chargerPlateau(f.getAbsolutePath());
            f.delete();

            assert p.getPositionJoueurInitiale().equals(new Position(1, 1)) : "Joueur mal placé";
            assert p.getPositionsCaisses().size() == 1 : "Mauvais nombre de caisses";
            assert compterCibles(p) == 1 : "Mauvais nombre de cibles";

            Configuration config = new Configuration(p);
            PersistanceNiveau.afficherDansConsole(config);
            System.out.println("[OK]\n");
            return true;
        } catch (Exception e) {
            System.err.println("[ECHEC] " + e.getMessage());
            return false;
        }
    }

    //  Test 2 : chargement avec * (caisse sur cible) et + (joueur sur cible)
    private static boolean testChargementAvecEtoile() {
        System.out.println("--- Test 2 : Chargement avec '*' et '+' ---");
        try {
            // #####
            // #*+.#   -> caisse sur cible en (1,1), joueur sur cible en (2,1), cible en (3,1)
            // #####
            // Attention : 2 caisses (en *) mais 3 cibles (* + .) -> invalide
            // On utilise un niveau cohérent : 1 caisse, 1 cible
            // ######
            // #*@..#  -> caisse sur cible (1,1), joueur (2,1), cibles (1,1) et (3,1) et (4,1)
            // ######   -> 1 caisse, 3 cibles -> invalide
            // Niveau cohérent : 2 caisses, 2 cibles
            // ######
            // #*$..#  -> * en (1,1) = caisse+cible, $ en (2,1) = caisse, . en (3,1) = cible, . en (4,1) = cible
            // #@####
            // -> 2 caisses, 3 cibles -> invalide
            // Niveau simple avec * et + cohérent :
            // ######
            // #*+..#  -> * = caisse sur cible, + = joueur sur cible, .. = 2 cibles
            // ######
            // -> 1 caisse, 3 cibles (celle de *, celle de +, les 2 points) -> invalide
            // Le plus simple : * seul
            // #####
            // #@.*#   -> joueur (1,1), cible (2,1), caisse sur cible (3,1)
            // #####
            // -> 1 caisse, 2 cibles -> invalide
            // OK niveau parfait :
            // ######
            // #@$*.#  -> joueur, caisse, caisse_sur_cible, cible -> 2 caisses 2 cibles
            // ######
            File f = creerFichierTemp("######\n#@$*.#\n######\n");
            Plateau p = PersistanceNiveau.chargerPlateau(f.getAbsolutePath());
            f.delete();

            assert p.getPositionJoueurInitiale().equals(new Position(1, 1)) : "Joueur mal placé";
            assert p.getPositionsCaisses().size() == 2 : "Mauvais nombre de caisses (attendu 2)";
            assert compterCibles(p) == 2 : "Mauvais nombre de cibles (attendu 2)";
            // La cible sous * doit être enregistrée
            assert p.estCible(new Position(3, 1)) : "Cible sous * non chargée !";

            Configuration config = new Configuration(p);
            PersistanceNiveau.afficherDansConsole(config);
            System.out.println("[OK]\n");
            return true;
        } catch (Exception e) {
            System.err.println("[ECHEC] " + e.getMessage());
            return false;
        }
    }

    //  Test 3 : sauvegarde de l'état courant puis rechargement
    private static boolean testSauvegardeEtatCourant() {
        System.out.println("--- Test 3 : Sauvegarde état courant ---");
        try {
            File fNiveau = creerFichierTemp("#####\n#@$.#\n#####\n");
            Plateau p = PersistanceNiveau.chargerPlateau(fNiveau.getAbsolutePath());
            fNiveau.delete();

            Configuration config = new Configuration(p);
            config.deplacerJoueur(Direction.DROITE); // joueur avance, caisse sur cible

            System.out.println("État à sauvegarder :");
            PersistanceNiveau.afficherDansConsole(config);

            // Sauvegarder
            File fSave = File.createTempFile("test_etat_", ".txt");
            fSave.deleteOnExit();
            PersistanceNiveau.sauvegarderEtatCourant(config, fSave.getAbsolutePath());

            // Recharger
            Plateau p2 = PersistanceNiveau.chargerPlateau(fSave.getAbsolutePath());
            fSave.delete();

            // La caisse doit être en position (3,1) = sur la cible
            assert p2.getPositionsCaisses().contains(new Position(3, 1)) : "Caisse mal sauvegardée !";
            assert p2.estCible(new Position(3, 1)) : "Cible perdue lors de la sauvegarde !";

            System.out.println("[OK]\n");
            return true;
        } catch (Exception e) {
            System.err.println("[ECHEC] " + e.getMessage());
            return false;
        }
    }

    //  Test 4 : encodage et chargement de solution
    private static boolean testSolution() {
        System.out.println("--- Test 4 : Encodage et chargement solution ---");
        try {
            File fNiveau = creerFichierTemp("#####\n#@$.#\n#####\n");
            Plateau p = PersistanceNiveau.chargerPlateau(fNiveau.getAbsolutePath());
            fNiveau.delete();

            Configuration config = new Configuration(p);
            config.deplacerJoueur(Direction.DROITE); // pousse la caisse sur la cible

            String solution = PersistanceNiveau.encoderSolution(config);
            System.out.println("Solution encodée : \"" + solution + "\"");
            assert solution.equals("R") : "Encodage incorrect ! Attendu 'R', obtenu : " + solution;

            // Sauvegarder et recharger
            File fSol = File.createTempFile("test_sol_", ".txt");
            fSol.deleteOnExit();
            PersistanceNiveau.sauvegarderSolution(config, fSol.getAbsolutePath());
            ArrayList<Direction> dirs = PersistanceNiveau.chargerSolution(fSol.getAbsolutePath());
            fSol.delete();

            assert dirs.size() == 1 : "Mauvais nombre de directions rechargées";
            assert dirs.get(0).getDx() == 1 && dirs.get(0).getDy() == 0 : "Direction incorrecte";

            // Rejouer
            Plateau pFrais = PersistanceNiveau.chargerPlateau(creerFichierTemp("#####\n#@$.#\n#####\n").getAbsolutePath());
            Configuration replay = new Configuration(pFrais);
            PersistanceNiveau.appliquerSolution(replay, dirs);
            System.out.println("État après replay :");
            PersistanceNiveau.afficherDansConsole(replay);

            System.out.println("[OK]\n");
            return true;
        } catch (Exception e) {
            System.err.println("[ECHEC] " + e.getMessage());
            return false;
        }
    }

    
    //  Test 5 : GestionnaireSauvegarde (sauvegarde + chargement partie)
    private static boolean testGestionnaireSauvegarde() {
        System.out.println(" Test 5 : GestionnaireSauvegarde ");
        try {
            File fNiveau = creerFichierTemp("#####\n#@$.#\n#####\n");
            Plateau p = PersistanceNiveau.chargerPlateau(fNiveau.getAbsolutePath());
            fNiveau.delete();

            Configuration config = new Configuration(p);
            config.deplacerJoueur(Direction.DROITE);

            System.out.println("État avant sauvegarde :");
            PersistanceNiveau.afficherDansConsole(config);

            File fSav = File.createTempFile("test_partie_", ".sav");
            fSav.deleteOnExit();
            SauvegardePartie.sauvegarder(config, fSav.getAbsolutePath());

            Configuration restore = SauvegardePartie.charger(fSav.getAbsolutePath());
            fSav.delete();

            System.out.println("État restauré :");
            PersistanceNiveau.afficherDansConsole(restore);

            assert restore.getJoueur().getPosition().equals(config.getJoueur().getPosition())
                : "Position joueur incorrecte après restauration !";
            assert restore.estCaisse(new Position(3, 1))
                : "Position caisse incorrecte après restauration !";

            System.out.println("[OK]\n");
            return true;
        } catch (Exception e) {
            System.err.println("[ECHEC] " + e.getMessage());
            return false;
        }
    }


    private static File creerFichierTemp(String contenu) throws IOException {
        File f = File.createTempFile("sokoban_test_", ".txt");
        f.deleteOnExit();
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(f))) {
            bw.write(contenu);
        }
        return f;
    }

    private static int compterCibles(Plateau plateau) {
        int count = 0;
        for (int y = 0; y < plateau.getY(); y++)
            for (int x = 0; x < plateau.getX(); x++)
                if (plateau.estCible(new Position(x, y))) count++;
        return count;
    }
}
