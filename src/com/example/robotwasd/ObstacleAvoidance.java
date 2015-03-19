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
	
	private static final double coefficient_length_time = 13.698630137;	//cm per second
	private static final double coefficient_degree_time = 62.069393582;	//degrees per second
	private static double stopDistance = 20;
	
	private static long time = 0;
	private static boolean driveRotate = false; //false = drive forward, true = rotate
	public static boolean run = true;
	
	public ObstacleAvoidance(Robot robot){
		this.robot = robot;
	}
	
	public void run(){
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
	 * set actual time and action what to do (false = drive forward, true = rotate)
	 * @param driveRotate
	 */
	public void setTime(boolean driveRotate){
		this.driveRotate = driveRotate;
		time = System.currentTimeMillis();
	}
	
	/**
	 * calibrate the stop distance with the actual shortest distance to an object
	 */
	public void setStopDistance(){
		stopDistance = minDistance(robot.readSensor());
	}

}
