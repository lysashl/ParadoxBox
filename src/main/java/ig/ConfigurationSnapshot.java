package ig;

import modele.Caisse;
import modele.Configuration;
import modele.Direction;
import modele.Position;

import java.util.ArrayList;
import java.util.List;

/**
 * classe qui permet de sauvegarder et restaurer une configuration du jeu
 * elle contient les informations necessaires pour revenir a un etat precedent
 */
public final class ConfigurationSnapshot {

    /** position du joueur sauvegardee */
    private final Position joueurPos;

    /** liste des positions des caisses sauvegardees */
    private final List<Position> caissesPos;

    /** nombre de coups sauvegardes */
    private final int nbCoups;

    /** Historique des directions du joueur */
    private final List<Direction> historique;

    /**
     * constructeur qui cree une copie de la configuration
     *
     * @param config configuration a sauvegarder
     */
    public ConfigurationSnapshot(Configuration config) {
        Position p = config.getJoueur().getPosition();
        this.joueurPos = new Position(p.getX(), p.getY());
        this.nbCoups = config.getCoups();

        List<Position> positions = new ArrayList<>();
        for (Caisse c : config.getCaisses()) {
            positions.add(new Position(c.getPosition().getX(), c.getPosition().getY()));
        }
        this.caissesPos = List.copyOf(positions);

        this.historique = new ArrayList<>(config.getJoueur().getHistorique());
    }

    /**
     * methode qui restaure la configuration sauvegardee
     *
     * @param config configuration a modifier pour restaurer l etat
     */
    public void restore(Configuration config) {

        List<Caisse> caisses = config.getCaisses();
        for (int i = 0; i < caisses.size(); i++) {
            caisses.get(i).setPositionSansVerfication(caissesPos.get(i));
        }

        config.getJoueur().setPositionSansVerfication(joueurPos);
        config.setCoups(nbCoups);

        config.getJoueur().getHistorique().clear();
        config.getJoueur().getHistorique().addAll(historique);
    }

    /**
     * retourne le nombre de coups sauvegarde
     *
     * @return nombre de coups
     */
    public int getNbCoups() {
        return nbCoups;
    }
}
