package com.example.robotwasd;


/**
 * Odometry class.
 * Keep track of the robot's position via maintaining Position instance
 * including x-coordinate, y-coordinate and orientation 'theta'
 */
public class Odometry {

	private Position p;
	
	private final double ACCURACY_THREASHOLD = 1e-3;
	
	public Odometry() {
		p = new Position();
	}
	
	public Position getPosition(){
		return p;
	}
	
	public void setPosition(double x, double y, double theta){
		this.p.x = x;
		this.p.y = y;		
		this.p.theta = theta;
		
		checkTheta();
		checkAccuracy();
	}
	
	public void setPosition(Position p) {
		setPosition(p.x, p.y, p.theta);
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
		
		checkTheta();
		checkAccuracy();
	}
	
	private void checkTheta() {
		if (p.theta < 0)
			p.theta = 2*Math.PI + p.theta;
	}
	
	private void checkAccuracy() {
		/** Set odometry properties to 0 if values are under the threashold */
		if (Math.abs(p.theta) < ACCURACY_THREASHOLD) p.theta = 0;
		if (Math.abs(p.x) < ACCURACY_THREASHOLD) p.x = 0;
		if (Math.abs(p.y) < ACCURACY_THREASHOLD) p.y = 0;
	}
}
