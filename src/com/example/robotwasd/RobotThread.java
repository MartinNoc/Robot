package com.example.robotwasd;


public class RobotThread extends Thread {

	private Robot robot;
	
	public RobotThread(Robot robot) {
		this.robot = robot;
		
	}

	public void run() {
		System.out.println("Robot:start");
		robot.collectBall(true);
		System.out.println("Robot:stop");
	}
}
