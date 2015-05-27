package com.example.robotwasd;

/**
 * represents one beacon
 * @author daniel
 *
 */
public class Beacon {
	
	private Color topColor;
	private Color bottomColor;
	
	private Position beaconPos;// known world coordinate position
	private Position imagePos; // robot's egocentric position
	
	public Beacon() {
		beaconPos = new Position();
		imagePos = new Position();
	}
	
	public Color getTopColor() {
		return topColor;
	}
	public Color getBottomColor() {
		return bottomColor;
	}
	public Position getBeaconPos() {
		return beaconPos;
	}
	public Position getImagePos() {
		return imagePos;
	}
	public void setTopColor(Color topColor) {
		this.topColor = topColor;
	}
	public void setBottomColor(Color bottomColor) {
		this.bottomColor = bottomColor;
	}
	public void setImagePos(Position imagePos) {
		this.imagePos = imagePos;
	}
	
	/* Beacon Position:
	 * 
	 * 	 Blue		Yellow		Red
	 *    Red _ _ _ _Green_ _ _ Yellow
	 * 		 |				   |
	 * 		 |				   |
	 * 		 |		 250	   |
	 * 	Green|_ _ _ _ x _ _ _ _|Blue
	 *   Blue|		 250	   |Green
	 * 		 |	 world-space   |
	 * 		 |		  	 	   |
	 * 		 |_ _ _ _ _ _ _ _ _|
	 * Yellow		 Blue		Red
	 *    Red		Yellow		Green
	 */		
	
	/**
	 * Sets the world-coordinate beacon position depending on
	 * top and bottom colors.
	 */
	public void setBeaconPos() {
		if (topColor == Color.Blue) {
			if (bottomColor == Color.Red) {
				beaconPos.setPosition(-125, 125, 0);
			}
			else if (bottomColor == Color.Green) {
				beaconPos.setPosition(125, 0, 0);
			}
			else if (bottomColor == Color.Yellow) {
				beaconPos.setPosition(0, -125, 0);
			}
		}
		else if (topColor == Color.Yellow) {
			if (bottomColor == Color.Green) {
				beaconPos.setPosition(0, 125, 0);
			}
			else if (bottomColor == Color.Red) {
				beaconPos.setPosition(-125, -125, 0);
			}
		}
		else if (topColor == Color.Red) {
			if (bottomColor == Color.Yellow) {
				beaconPos.setPosition(125, 125, 0);
			}
			else if (bottomColor == Color.Green) {
				beaconPos.setPosition(125, -125, 0);
			}
		}
		else if (topColor == Color.Green) {
			if (bottomColor == Color.Blue) {
				beaconPos.setPosition(-125, 0, 0);
			}
		}
	}
	
	public String toString() {
		return "[" + topColor.toString() + "/" + bottomColor.toString() + "]";
	}

}