package com.example.robotwasd;

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
}
