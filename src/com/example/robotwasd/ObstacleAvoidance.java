package com.example.robotwasd;

/**
 * Class for observation of objects around the robot and avoiding the contact
 * between an obstacle and the robot
 * 
 * @author Daniel Witsch
 * 
 */
public class ObstacleAvoidance {
	private Robot robot;
	private Odometry odometry;

	// cm per second
	private final double coefficient_length_time = 13.698630137;
	// initial value for stop distance
	private double stopDistance = 15;
	//sleeping time between the loops for checking of an obstacle
	private final long SLEEPING_TIME = 50;

	public ObstacleAvoidance(Robot robot, Odometry odometry) {
		this.robot = robot;
		this.odometry = odometry;
	}

	/**
	 * search the smallest number of the sensor data
	 * 
	 * @param sensor data of the sensor from the robot (string with sensor values)
	 * @return
	 */
	private int minDistance(String sensor) {
		/**
		 * sensor: 0x01 0x02 0x03 0x04 0x05 0x06 0x07 0x08
		 * Linker Sensor: 0x03
		 * Mittlerer Sensor: 0x07
		 * Rechter Sensor: 0x04
		 */
		
		String str_sensorData = sensor.substring(sensor.indexOf("sensor: 0x"));

		Integer[] sensorData = new Integer[3];
		sensorData[0] = Integer.parseInt(str_sensorData.substring(20, 22), 16);	// linker Sensor
		sensorData[1] = Integer.parseInt(str_sensorData.substring(25, 27), 16);	// rechter Sensor
		sensorData[2] = Integer.parseInt(str_sensorData.substring(40, 42), 16); // mittlerer Sensor

		// ACHTUNG: Änderung! Mittlerer Sensor wird zur Zeit nicht verwendet
		int minimum = Math.min(sensorData[0], sensorData[1]);
		/*
		int minimum = sensorData[0];
		for (int i = 1; i < 3; i++) {
			if (sensorData[i] < minimum) {
				minimum = sensorData[i];
			}
		}
		*/

		return minimum;
	}

	
	/** public methods **/

	/**
	 * 
	 * @param waitingTime
	 * @return true, if obstacle is detected, false otherwise
	 */
	public boolean avoidObstacles(double waitingTime, long startTime) {
		long driveTime = 0;

		// wait till robot stops after driving the desired distance
		while(driveTime < waitingTime) {
			driveTime = System.currentTimeMillis() - startTime;
			// hit obstacle -> stop robot
			if (minDistance(robot.readSensor()) < stopDistance) {
				robot.stopRobot();
				driveTime = System.currentTimeMillis() - startTime;
				double driveTimeSec = (double) driveTime / 1000;

				// calculate driven distance and correct odometry
				double driveDistance = driveTimeSec * coefficient_length_time;
				odometry.adjustOdometry(driveDistance, 0);
				robot.textLog.setText(odometry.getPosition().toString());
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
				}
				return true;
			}
			
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
