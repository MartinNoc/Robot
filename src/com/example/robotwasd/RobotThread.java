package com.example.robotwasd;

/**
 * Thread class which performs actions in a separate thread.
 * Thus the UI Thread is able to continue obtaining camera images.
 *
 */
public class RobotThread extends Thread {

	private Robot robot;
	
	public RobotThread(Robot robot) {
		this.robot = robot;
		
	}

	public void run() {
		boolean withExplore = true;
		robot.collectBalls(withExplore);
	}
}
