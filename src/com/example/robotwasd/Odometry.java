package com.example.robotwasd;

import java.util.ArrayList;
import java.util.List;

/**
 * Odometry class.
 * Keep track of the robot's position via maintaining Position instance
 * including x-coordinate, y-coordinate and orientation 'theta'
 *
 */
public class Odometry {

	private Position p;
	private List<Position> robotPosList;
	
	public Odometry() {
		p = new Position();
		robotPosList = new ArrayList<Position>();
	}
	
	public Position getPosition(){
		return p;
	}
	
	public void setPosition(double x, double y, double theta){
		p.setPosition(x, y, theta);
	}
	
	/**
	 * Calculates the new robot position of 
	 * 	linear movement: x'=x+dx=x+a*cos(theta) y'=y+dy=y+a*sin(theta) theta'=theta+0
	 *  rotational movement (on the spot): x'=x+0 y'=y+0 theta'=theta+alpha 
	 *  
	 * @param a distance driven [in cm]
	 * @param alpha angle turned (vx = -vy) [degree] converted to radians within the method
	 */
	public void adjustOdometry(double a, double alpha) {
		/** calculate new position cooridnates x,y */
		p.x += a*Math.cos(p.theta);
		p.y += a*Math.sin(p.theta);
		
		/** calculate new orientation theta [radians]
		 *  modulo 2PI to keep angle within interval [0,2PI)
		 *  if theta is negative, convert it to positive angle
		 */
		p.theta = (p.theta + alpha*Math.PI/180) % (2*Math.PI);
		if (p.theta < 0)
			p.theta = 2*Math.PI + p.theta;
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
