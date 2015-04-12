package com.example.robotwasd;

import android.widget.TextView;

/**
 * Wrapper Class for all movements of the robot
 * 
 * @author Witsch Daniel
 * 
 */
public class Movement {
	private final double COEFFICIENT_LENGTH = 1.352;		// constant to correct length of driving
	private final double COEFFICIENT_LENGTH_TIME = 0.073; 	// seconds per cm
	private final double COEFFICIENT_DEGREE = 1.137;		// constant to correct rotation
	private final double COEFFICIENT_DEGREE_TIME = 0.016111;// seconds per degree
	
	private Communication com;
	private Odometry odometry;
	private ObstacleAvoidance obst;
	private TextView textLog;

	public Movement(Communication com, Odometry odometry, ObstacleAvoidance obst, TextView textLog) {
		this.com = com;
		this.odometry = odometry;
		this.obst = obst;
		this.textLog = textLog;
	}

	/**
	 * read out the sensors
	 */
	public String readSensor() {
		return com.readWriteRobot(new byte[] { 'q', '\r', '\n' });
	}

	/** Movements */
	public void moveForward() {
		com.writeRobot(new byte[] { 'w', '\r', '\n' });
	}

	public void moveBackward() {
		com.writeRobot(new byte[] { 'x', '\r', '\n' });
	}

	public void stopRobot() {
		com.writeRobot(new byte[] { 's', '\r', '\n' });
	}

	public void turnLeft() {
		com.writeRobot(new byte[] { 'a', '\r', '\n' });
	}

	public void turnLeft(double degree) {
		robotTurn(degree);
	}

	public void turnRight() {
		com.writeRobot(new byte[] { 'd', '\r', '\n' });
	}

	public void turnRight(double degree) {
		robotTurn((-1) * degree);
	}

	public void lowerBar() {
		com.writeRobot(new byte[] { '-', '\r', '\n' });
	}

	public void riseBar() {
		com.writeRobot(new byte[] { '+', '\r', '\n' });
	}

	// fixed position for bar (low)
	public void lowPositionBar() {
		robotSetBar((byte) 0);
	}

	// fixed position for bar (high)
	public void upPositionBar() {
		robotSetBar((byte) 255);
	}

	/**
	 * all LED ON
	 */
	public void LedOn() {
		robotSetLeds((byte) 255, (byte) 128);
	}

	/**
	 * all LED OFF
	 */
	public void LedOff() {
		robotSetLeds((byte) 0, (byte) 0);
	}

	/** helper functions */
	/********************************************/

	/**
	 * Setting blue and red LED
	 * 
	 * @param red
	 * @param blue
	 */
	public void robotSetLeds(byte red, byte blue) {
		com.readWriteRobot(new byte[] { 'u', red, blue, '\r', '\n' });
	}

	/**
	 * Driving straight forward/backward Computes correctedDistance and splits
	 * driving into parts in order to avoid overflows
	 * 
	 * @param distance_cm
	 * @return when true then robot hit against an obstacle
	 */
	public boolean robotDrive(double distance_cm) {
		double correctedDistance = distance_cm * COEFFICIENT_LENGTH;
		int numberRepetitions = (int) (correctedDistance / Byte.MAX_VALUE);
		double remain = correctedDistance % Byte.MAX_VALUE;
		boolean robotHit = false;

		for (int i = 0; i < Math.abs(numberRepetitions); i++) {
			if (correctedDistance < 0)
				robotHit = robotDrive_helper(-Byte.MAX_VALUE);
			else
				robotHit = robotDrive_helper(Byte.MAX_VALUE);
			if(robotHit)	//when robot drives against an obstacle
				return true;
		}
		//when robot doesn't drive against an obstacle
		return robotDrive_helper(remain);
	}

	/**
	 * Helper for driving straight forward/backward
	 * 
	 * @param distance_cm
	 *            Real distance robot will drive
	 */
	private boolean robotDrive_helper(double distance_cm) {
		double waitingTime;
		boolean robotHit = false;
		if (!obst.checkObstacleAhead()) {
			waitingTime = calculateWaitTimeLength(distance_cm / COEFFICIENT_LENGTH);
			com.readWriteRobot(new byte[] { 'k', (byte) Math.round(distance_cm), '\r', '\n' }
			// round in order to get a minimal error by casting to byte
			// only effective when passing remain
			);
			robotHit = obst.avoidObstacles(waitingTime, System.currentTimeMillis(), distance_cm / COEFFICIENT_LENGTH);
			// correct odometry values if robot doesn't hit an obstacle
			if (!robotHit) {
				odometry.adjustOdometry(distance_cm / COEFFICIENT_LENGTH, 0);
				textLog.setText(odometry.getPosition().toString());
			}
			return robotHit;
		}else
			return true;
	}

	public void robotTurn(double degree) {
		double correctedDegree = degree * COEFFICIENT_DEGREE;
		int numberRepetitions = (int) (correctedDegree / Byte.MAX_VALUE);
		double remain = correctedDegree % Byte.MAX_VALUE;
		for (int i = 0; i < Math.abs(numberRepetitions); i++) {	//abs for minus degrees
			if(correctedDegree < 0)	//when degree is negative
				robotTurn_helper(-Byte.MAX_VALUE);
			else
				robotTurn_helper(Byte.MAX_VALUE);
		}
		robotTurn_helper(remain);
	}

	/**
	 * Turning the robot on the spot, counter-clockwise (left)
	 * 
	 * @param degree
	 */
	private void robotTurn_helper(double degree) {
		com.readWriteRobot(new byte[] { 'l', (byte) Math.round(degree), '\r', '\n' }
		// round in order to get a minimal error by casting to byte
		// only effective when passing remain
		);

		waitForRobotDegree(degree / COEFFICIENT_DEGREE);
		odometry.adjustOdometry(0, degree / COEFFICIENT_DEGREE);
		textLog.setText(odometry.getPosition().toString());
	}

	/**
	 * Driving by velocity
	 * 
	 * @param left
	 * @param right
	 */
	public void robotSetVelocity(byte left, byte right) {
		com.readWriteRobot(new byte[] { 'i', left, right, '\r', '\n' });
	}

	/**
	 * Using the bar
	 * 
	 * @param value
	 */
	public void robotSetBar(byte value) {
		com.readWriteRobot(new byte[] { 'o', value, '\r', '\n' });
	}

	/**
	 * calculates the time till the movement is over
	 * 
	 * @param distance_cm
	 *            distance to drive
	 */
	private double calculateWaitTimeLength(double distance_cm) {
		return (Math.abs(distance_cm) * COEFFICIENT_LENGTH_TIME * 1.1 * 1000);
	}

	/**
	 * calculates the time till the roation is over and sleeps until rotation is done.
	 * performs the rotation-live-odometry
	 * 
	 * @param degree amount of degree to rotate
	 */
	private void waitForRobotDegree(double degree) {
		
		double initialTheta = odometry.getPosition().theta;
		Position livePosition = odometry.getPosition();
		
		long execTime = 0;
		long startTime = System.currentTimeMillis();
		double waitingTime = Math.abs(degree) * COEFFICIENT_DEGREE_TIME * 1.1 * 1000;
		
		while (execTime < waitingTime) {
			execTime = System.currentTimeMillis() - startTime;
			
			/** calculates live orientation theta */
			livePosition.theta = initialTheta + execTime/waitingTime * degree;
			
			textLog.setText(livePosition.toString());
			
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				// ignore
			}
		}
	}

	/**
	 * Test method which moves the robot and calls driveToOrigin at the end.
	 */
	public void testMovement() {
		robotDrive(140);
		turnLeft(60);
		robotDrive(100);
		turnRight(90);
		robotDrive(30);

		driveToOrigin();
	}

	/**
	 * moves the robot back to origin position. x = y = theta = 0.
	 */
	public void driveToOrigin() {
		Position p = odometry.getPosition();

		/**
		 * turn robot parallel to y-axis facing down the negative y-axis. no
		 * case analysis whether p.y is positive or not because if... y > 0:
		 * robot is facing down towards x-axis and moving forward because y is
		 * positive y < 0: robot is facing down, x-axis is behind(!) but y is
		 * negative so the robot moves backwards
		 */
		setRobotOrientation(270);
		robotDrive(p.y);

		// now the robot is in y-position zero at some x-position facing
		// straight down (theta = 3/2*PI)

		/**
		 * turn robot parallel to x-axis facing down the negative x-axis. no
		 * case analysis due to same reasons as mentioned above
		 */
		setRobotOrientation(180);
		robotDrive(p.x);

		// now the robot is in (x/y) position (0/0) at angle theta = PI

		setRobotOrientation(0);
	}
	
	/**
	 * Turns robot to a specific angle alpha
	 * 
	 * @param alpha
	 *            Desired orientation-angle the robot will be turned to: [0,360)
	 *            [degree]
	 */
	public void setRobotOrientation(double alpha) {
		// convert angle alpha to number within interval [0,360)
		alpha = alpha % 360;
		if (alpha < 0) {
			alpha = 360 + alpha;
		}

		double theta = odometry.getPosition().theta * 180 / Math.PI;

		robotTurn(alpha - theta);
	}
}
