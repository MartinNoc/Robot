package com.example.robotwasd;

/**
 * Thread class which performs actions in a separate thread.
 * Thus the UI Thread is able to continue obtaining camera images.
 *
 */
public class RobotThreadOA extends Thread {

	private Robot robot;
	
	public RobotThreadOA(Robot robot) {
		this.robot = robot;
		
	}

	public void run() {
		boolean withExplore = true;
		robot.collectBallsOA(withExplore);
	}
}
