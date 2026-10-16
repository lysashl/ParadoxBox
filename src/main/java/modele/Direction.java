/* une classe qui représente la direction */
package modele;
import java.util.ArrayList;

public class Direction{
	
	public static Direction GAUCHE = new Direction(-1, 0);
	public static Direction DROITE = new Direction(1, 0);
	public static Direction HAUT = new Direction(0, -1);
	public static Direction BAS = new Direction(0, 1);
	
	private static final ArrayList<Direction> DIRECTIONS = new ArrayList<>(); /* la liste de directions  gauche droite haut bas*/
	private int dx;
	private int dy;
	
	static{
		DIRECTIONS.add(GAUCHE);
		DIRECTIONS.add(DROITE);
		DIRECTIONS.add(HAUT);
		DIRECTIONS.add(BAS);
	}	
	
	public Direction(int x, int y){
		this.dx = x;
		this.dy = y;
	}
	
	@Override
	public String toString(){
		return "Direction (dx =" + ", dy=" + ")";
	}
	
	public int getDx(){
		return this.dx;
	}
	
	public int getDy(){
		return this.dy;
	}
	
	public static ArrayList<Direction> getDirections(){
		return DIRECTIONS;
	}
	
}						
