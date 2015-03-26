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
	private final long SLEEPING_TIME = 100;

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

	/********************************/
	/** public methods **/

	/**
	 * 
	 * @param waitingTime
	 * @return true, if obstacle is detected, false otherwise
	 */
	public boolean avoidObstacles(double waitingTime, long startTime) {
		boolean robotHit = false;
		for (int i = 0; i < Math.ceil(waitingTime / SLEEPING_TIME); i++) {
			if (minDistance(robot.readSensor()) < stopDistance) {
				long driveTime = System.currentTimeMillis() - startTime;
				robot.stopRobot();
				robotHit = true;
				double driveTimeSec = (double) driveTime / 1000;

				// drive forward
				double driveDistance = driveTimeSec * coefficient_length_time;
				odometry.adjustOdometry(driveDistance, 0);
				break;
			}
			try {
				Thread.sleep(SLEEPING_TIME);
			} catch (InterruptedException e) {
				// ignore
			}
		}
		return robotHit;
	}

	/**
	 * calibrate the stop distance with the actual shortest distance to an
	 * object
	 */
	public void setStopDistance() {
		this.stopDistance = minDistance(robot.readSensor());
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
