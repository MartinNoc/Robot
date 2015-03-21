package com.example.robotwasd;

import java.util.ArrayList;
import java.util.List;

public class Odometry {

	private Position p;
	private List<Position> robotPosList;
	
	public Odometry() {
		p = new Position();
		robotPosList = new ArrayList<Position>();
	}public static boolean run = true;
	
	/**
	 * Calculates the new robot position of 
	 * 	linear movement: x'=x+dx=x+a*cos(theta) y'=y+dy=y+a*sin(theta) theta'=theta+0
	 *  rotational movement (on the spot): x'=x+0 y'=y+0 theta'=theta+alpha
	 * via 
	 *  
	 * @param a distance driven [in cm]
	 * @param alpha angle turned (vx = -vy) [in degree] converted to radians within the method
	 */
	public void adjustOdometry(double a, double alpha) {
		p.x += a*Math.cos(p.theta);
		p.y += a*Math.sin(p.theta);
		p.theta += alpha*180/Math.PI;
	}
	
	/**
	 * Methods to save robot's position into the List of positions
	 */
	
	public void saveRobotPosition(double x, double y, double theta){
		robotPosList.add(new Position(x,y,theta));
	}
	
	public void saveRobotPosition(Position p){
		robotPosList.add(p);
	}
}