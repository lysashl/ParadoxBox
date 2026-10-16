/* une classe pour gérer les cibles on a besoin juste de savoir si elle est occupée ou non */
package modele;
public class Cible {
    private boolean occupee;

    public Cible() {
        occupee = false;
    }

    public boolean estOccupee() {
        return occupee;
    }

    public void setOccupee(boolean b) {
        occupee = b;
    }
}
