package com.example.robotwasd;

import android.os.AsyncTask;

/**
 * Class for observation of objects around the robot and avoiding the contact between an obstacle and the robot
 * @author daniel
 *
 */
public class ObstacleAvoidance extends AsyncTask<Void, String, Void> {
	private Robot robot;
	private Odometry odometry;
	
	private final double coefficient_length_time = 13.698630137;	//cm per second
	private double stopDistance = 20;	//initial value for stop distance
	private String sensor = "";			//"global" sensor return-string, from publishProgress, used in doInBackground
	
	private long time = 0;
	private boolean alive = true;		//start stop thread
	private boolean run = false;		//start stop detecting obstacles
	private boolean robotHit = false;
	
	public ObstacleAvoidance(Robot robot, Odometry odometry){
		this.robot = robot;
		this.odometry = odometry;
	}
	
	@Override
	protected Void doInBackground(Void... params) {
		while(alive){
			while(run){
				publishProgress("Thread running...");
				//String sensor = robot.readSensor();
				sensor = "";
				publishProgress("readSensor");
				while(sensor.equals("")) {};
				if(minDistance(sensor) < stopDistance){
					//robot.stopRobot();
					publishProgress("stopRobot");
					long driveTime = System.currentTimeMillis() - time;
					double driveTimeSec = (double)driveTime/1000;
					
					//drive forward
					double driveDistance = driveTimeSec * coefficient_length_time;
					//odometry.adjustOdometry(driveDistance, 0);
					publishProgress("adjustOdometry",String.valueOf(driveDistance));
					
					robotHit = true;
					run = false;
					publishProgress("obstacle ahead");
				}
				try {
					Thread.sleep(100);	
				} catch (InterruptedException e) {	}
			}
			try {
				Thread.sleep(50);	
			} catch (InterruptedException e) {	}
		}
		return null;
	}
	
	@Override
	protected void onProgressUpdate(String... progress) {
		for (int i=0; i < progress.length; i++){
			if (progress[i].equals("readSensor"))
				this.sensor = robot.readSensor();
			else if (progress[i].equals("stopRobot"))
				robot.stopRobot();
			else if (progress[i].equals("adjustOdometry"))
				odometry.adjustOdometry(Double.parseDouble(progress[++i]), 0);
			else
				robot.textLog.setText(progress[i]);
		}
	}
	
	/**
	 * search the smallest number of the sensor data
	 * @param sensor data of the sensor from the robot
	 * @return
	 */
	private int minDistance(String sensor) {
		String str_sensorData = sensor.substring(sensor.indexOf("sensor: 0x"));
		
		Integer[] sensorData = new Integer[3];
		sensorData[0] = Integer.parseInt(str_sensorData.substring(20, 22), 16);
		sensorData[1] = Integer.parseInt(str_sensorData.substring(25, 27), 16);
		sensorData[2] = Integer.parseInt(str_sensorData.substring(40, 42), 16);
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
	public void startObstacleAvoidance(){
			time = System.currentTimeMillis();
			this.run = true;
			this.robotHit = false;
	}
	
	/**
	 * stop the detection of a collision
	 * @return true if robot stops because of obstacle
	 */
	public boolean stopObstacleAvoidance(){
		this.run = false;
		return robotHit;
	}
		
	/**
	 * calibrate the stop distance with the actual shortest distance to an object
	 */
	public void setStopDistance(){
		this.stopDistance = minDistance(robot.readSensor());
	}
	
	/**
	 * stop thread
	 */
	public void stopThread(){
		this.run = false;
		this.alive = false;
	}
	/**
	 * checks if an obstacle is in front of the robot
	 * @return true when there is an obstacle, false if not
	 */
	public boolean checkObstacleAhead(){
		return (stopDistance > minDistance(robot.readSensor()));
	}
	
	public boolean getRun(){
		return run;
	}

}
