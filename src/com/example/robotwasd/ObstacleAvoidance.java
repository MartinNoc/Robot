package com.example.robotwasd;

public class ObstacleAvoidance extends Thread {
	Robot robot;
	
	public ObstacleAvoidance(Robot robot){
		this.robot = robot;
		start();
	}
	public void run(){
		String data;
		while(true){
			data = robot.readSensor();
			try {
				sleep(100);
			} catch (InterruptedException e) {}
		}
	}
}
