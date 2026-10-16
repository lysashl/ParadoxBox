package modele;

/* la classe qui représente un joueur dans la version classique */
public class Joueur extends JoueurAbstrait {

    public Joueur(Configuration conf, Position pos) {
        super(conf, pos, Type.JOUEUR);
    }

    @Override
    public boolean deplacerVers(Direction d) {
        Position nouvellePosition = this.getPosition().add(d);

        if (conf.estCaisse(nouvellePosition)) {
            if (!conf.estCaisse(nouvellePosition.add(d)) && conf.estVide(nouvellePosition.add(d))) {
                conf.get(nouvellePosition).deplacerVers(d);
                setPosition(nouvellePosition);
                historique.add(d);
                return true;
            }
        } else if (conf.estVide(nouvellePosition)) {
            setPosition(nouvellePosition);
            historique.add(d);
            return true;
        }
        return false;
    }

    /** Nombre de coups effectuees par le joueur */
    public int getNbCoups() {
        return historique.size();
    }

    /** reinitialiser le joueur */
    public void reset(Position posInitiale) {
        setPosition(posInitiale);
        historique.clear();
    }
}

