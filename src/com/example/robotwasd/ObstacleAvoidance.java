package com.example.robotwasd;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Class for observation of objects around the robot and avoiding the contact between an obstacle and the robot
 * @author daniel
 *
 */
public class ObstacleAvoidance extends Thread {
	private Robot robot;
	
	private final double coefficient_length_time = 13.698630137;	//cm per second
	private final double coefficient_degree_time = 62.069393582;	//degrees per second
	private double stopDistance = 20;	//initial value for stop distance
	
	private long time = 0;
	private boolean driveRotate = false; //false = drive forward, true = rotate
	private boolean run = false;		//start stop detecting obstacles
	private boolean alive = true;	//start stop thread
	
	public ObstacleAvoidance(Robot robot){
		this.robot = robot;
	}
	
	public void run(){
		while(alive){
			while(run){
				String sensor = robot.readSensor();
				if(minDistance(sensor) < stopDistance){
					robot.stopRobot();
					long driveTime = System.currentTimeMillis() - time;
					double driveTimeSec = (double)driveTime/1000;
					if(driveRotate){
						//rotate
						double rotateDegrees = driveTimeSec * coefficient_degree_time;
						robot.correctPosition(0, rotateDegrees);
					}else {
						//drive forward
						double driveDistance = driveTimeSec * coefficient_length_time;
						robot.correctPosition(driveDistance, 0);
					}
					run = false;
				}
			}
		}
	}
	
	/**
	 * search the smallest number of the sensor data
	 * @param sensor
	 * @return
	 */
	private int minDistance(String sensor) {
		Integer[] sensorData = new Integer[8];
		for(int i = 0; i < 8; i++)
			sensorData[i] = Integer.parseInt(sensor.substring(i*5+10, i*5+12), 16);
		int minimum = sensorData[0];
		for(int i = 1; i < 8; i++)
			if(sensorData[i] < minimum)
				minimum = sensorData[i];
		
		return minimum;
	}

	/**
	 * start the detection of a collision
	 * set actual time and action what to do (false = drive forward, true = rotate)
	 * @param driveRotate
	 */
	synchronized public void startMovement(boolean driveRotate){
		this.driveRotate = driveRotate;
		time = System.currentTimeMillis();
		this.run = true;
	}
	
	/**
	 * stop the detection of a collision
	 */
	synchronized public void stopMovement(){
		this.run = false;
	}
		
	/**
	 * calibrate the stop distance with the actual shortest distance to an object
	 */
	synchronized public void setStopDistance(){
		this.stopDistance = minDistance(robot.readSensor());
	}
	
	/**
	 * stop thread
	 */
	synchronized public void stopThread(){
		this.run = false;
		this.alive = false;
	}

}
