package com.example.robotwasd;


public class RobotThread extends Thread {

	private Robot robot;
	
	public RobotThread(Robot robot) {
		this.robot = robot;
		
	}

	public void run() {
		robot.collectBall(true);
	}
}
