package com.example.robotwasd;

import java.text.DecimalFormat;

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
	
	public String toString(){
		DecimalFormat df = new DecimalFormat("#.00");
		return "(x,y,theta) = (" + df.format(x) + "," + df.format(y) + "," + df.format(theta) + ")";
	}
}
