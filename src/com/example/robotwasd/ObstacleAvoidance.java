package com.example.robotwasd;

/**
 * Class for observation of objects around the robot and avoiding the contact between an obstacle and the robot
 * @author daniel
 *
 */
public class ObstacleAvoidance extends Thread {
	private Robot robot;
	private Odometry odometry;
	
	private final double coefficient_length_time = 13.698630137;	//cm per second
	private double stopDistance = 20;	//initial value for stop distance
	
	private long time = 0;
	private boolean run = false;		//start stop detecting obstacles
	private boolean alive = true;	//start stop thread
	private boolean robotHit = false;
	
	public ObstacleAvoidance(Robot robot, Odometry odometry){
		this.robot = robot;
		this.odometry = odometry;
	}
	
	public void run(){
		while(alive){
			while(run){
				String sensor = robot.readSensor();
				if(minDistance(sensor) < stopDistance){
					robot.stopRobot();
					long driveTime = System.currentTimeMillis() - time;
					double driveTimeSec = (double)driveTime/1000;
					
					//drive forward
					double driveDistance = driveTimeSec * coefficient_length_time;
					odometry.adjustOdometry(driveDistance, 0);
					
					robotHit = true;
					run = false;
					
					try {
						Thread.sleep(500);	
					} catch (InterruptedException e) {	}
				}
			}
		}
	}
	
	/**
	 * search the smallest number of the sensor data
	 * @param sensor data of the sensor from the robot
	 * @return
	 */
	private int minDistance(String sensor) {
		Integer[] sensorData = new Integer[3];
		sensorData[0] = Integer.parseInt(sensor.substring(20, 22), 16);
		sensorData[1] = Integer.parseInt(sensor.substring(25, 27), 16);
		sensorData[2] = Integer.parseInt(sensor.substring(40, 42), 16);
		int minimum = sensorData[0];
		for(int i = 1; i < 3; i++)
			if(sensorData[i] < minimum)
				minimum = sensorData[i];
		
		return minimum;
	}
	
	/********************************/
	/**public methods**/

	/**
	 * start the detection of a collision
	 * set actual time and action what to do
	 */
	public void startMovement(){
			time = System.currentTimeMillis();
			this.run = true;
			this.robotHit = false;
	}
	
	/**
	 * stop the detection of a collision
	 * @return true if robot stops because of obstacle
	 */
	public boolean stopMovement(){
		this.run = false;
		return robotHit;
	}
		
	/**
	 * calibrate the stop distance with the actual shortest distance to an object
	 */
	public void setStopDistance(){
		String data = robot.readSensor();
		this.stopDistance = minDistance(data);
	}
	
	/**
	 * stop thread
	 */
	public void stopThread(){
		this.run = false;
		this.alive = false;
	}
	/**
	 * checks if a obstacle is in front of the robot
	 * @return true when there is a obstacle, false if not
	 */
	public boolean checkObstacleAhead(){
		return (stopDistance > minDistance(robot.readSensor()));
	}

}
