package com.example.robotwasd;

/**
 * Class for observation of objects around the robot and avoiding the contact
 * between an obstacle and the robot
 * 
 * @author daniel
 * 
 */
public class ObstacleAvoidance {
	private Robot robot;
	private Odometry odometry;

	// cm per second
	private final double coefficient_length_time = 13.698630137;
	// initial value for stop distance
	private double stopDistance = 20;
	private final long SLEEPING_TIME = 50;

	public ObstacleAvoidance(Robot robot, Odometry odometry) {
		this.robot = robot;
		this.odometry = odometry;
	}

	/**
	 * search the smallest number of the sensor data
	 * 
	 * @param sensor
	 *            data of the sensor from the robot
	 * @return
	 */
	private int minDistance(String sensor) {
		String str_sensorData = sensor.substring(sensor.indexOf("sensor: 0x"));

		Integer[] sensorData = new Integer[3];
		sensorData[0] = Integer.parseInt(str_sensorData.substring(20, 22), 16);
		sensorData[1] = Integer.parseInt(str_sensorData.substring(25, 27), 16);
		sensorData[2] = Integer.parseInt(str_sensorData.substring(40, 42), 16);
		int minimum = sensorData[0];
		for (int i = 1; i < 3; i++) {
			if (sensorData[i] < minimum) {
				minimum = sensorData[i];
			}
		}

		return minimum;
	}

	
	/** public methods **/

	/**
	 * 
	 * @param waitingTime
	 * @return true, if obstacle is detected, false otherwise
	 */
	public boolean avoidObstacles(double waitingTime, long startTime, double distance) {
		long driveTime = 0;
		Position initialPosition = odometry.getPosition();
		Position livePosition = odometry.getPosition();
		// wait till robot stops after driving the desired distance
		while(driveTime < waitingTime) {
			driveTime = System.currentTimeMillis() - startTime;
			// hit obstacle -> stop robot
			if (minDistance(robot.readSensor()) < stopDistance) {
				robot.stopRobot();
				driveTime = System.currentTimeMillis() - startTime;
				double driveTimeSec = (double) driveTime / 1000;

				// calculate drived distance and correct odometry
				double driveDistance = driveTimeSec * coefficient_length_time;
				odometry.adjustOdometry(driveDistance, 0);
				robot.textLog.setText(odometry.getPosition().toString());
				return true;
			}
			
			// live odometry
			livePosition.x = initialPosition.x + driveTime/waitingTime * distance * Math.cos(initialPosition.theta);
			livePosition.y = initialPosition.y + driveTime/waitingTime * distance * Math.sin(initialPosition.theta);
			robot.textLog.setText(livePosition.toString());
			
			try {
				Thread.sleep(SLEEPING_TIME);
			} catch (InterruptedException e) {
				// ignore
			}
		}
		return false;
	}

	/**
	 * calibrate the stop distance with the actual shortest distance to an
	 * object
	 */
	public double setStopDistance() {
		this.stopDistance = minDistance(robot.readSensor());
		return stopDistance;
	}

	/**
	 * checks if an obstacle is in front of the robot
	 * 
	 * @return true when there is an obstacle, false if not
	 */
	public boolean checkObstacleAhead() {
		return (stopDistance > minDistance(robot.readSensor()));
	}

}
