package com.example.robotwasd;

import java.util.Locale;

public class Position {

	public double x;
	public double y;
	public double theta;	// orientation [radians]
	
	public Position() {
		this.x = 0;
		this.y = 0;
		this.theta = 0;
	}
	
	public Position(double x, double y, double theta) {
		this.x = x;
		this.y = y;
		this.theta = theta;
	}
	
	public void setPosition(double x, double y, double theta) {
		this.x = x;
		this.y = y;
		this.theta = theta;
	}
	

	public String toString(){
		return String.format(Locale.US,"(x,y,theta) = (%1$.2f,%2$.2f,%3$.2f)",x,y,180*theta/Math.PI);
	}
}
