package modele;

public class Position {
	private int x;
	private int y; 
	
	public Position(int x, int y){
		this.x = x;
		this.y = y;
	}
	
	public Position(Position pos){
		this(pos.x, pos.y);
	}	
	
	public Position add(Direction d){
		return new Position(this.x + d.getDx(), this.y + d.getDy());
	}
	
	public int getX(){
		return this.x;
	}
	
	public int getY(){
		return this.y;
	}
	
	@Override
	public boolean equals(Object obj){
		if(this == obj){
			return true;
		}
		if(obj == null){
			return false;
		}
		if(! (obj instanceof Position)){
			return false;
		}
		Position other = (Position) obj;
		return x == other.x && y == other.y;
	}
	
	@Override
	public String toString(){
		return "Position (x=" + x + ", y=" + y + ")";
	}
}								
	 
