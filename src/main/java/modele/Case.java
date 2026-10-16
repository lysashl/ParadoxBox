/* une classe qui représente la case */
package modele;
public class Case extends Immobile {
    public Case(){
        super(Type.CASE);
    }

    public boolean deplacerVers(Direction direction) {
        return true;
    }
}
