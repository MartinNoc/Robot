package com.example.robotwasd;

/**
 * Wrapper Class for all movements of the robot
 * 
 * @author Witsch Daniel
 * 
 */
public class Movement {
	private final double coefficient_length = 1.352; // constant to correct
														// length of driving
	private final double coefficient_length_time = 0.073; // seconds per cm
	private final double coefficient_degree = 1.137; // constant to correct
														// rotation
	private final double coefficient_degree_time = 0.016111; // seconds per
																// degree
	private final double SIZE_BYTE = 127;

	private Communication com;
	private Odometry odometry;
	private ObstacleAvoidance obst;

	public Movement(Communication com, Odometry odometry, ObstacleAvoidance obst) {
		this.com = com;
		this.odometry = odometry;
		this.obst = obst;
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
	 */
	public void robotDrive(double distance_cm) {
		double correctedDistance = distance_cm * coefficient_length;
		int numberRepetitions = (int) (correctedDistance / SIZE_BYTE);
		double remain = correctedDistance % SIZE_BYTE;
		boolean robotHit = false;

		for (int i = 0; i < numberRepetitions; i++) {
			robotHit = robotDrive_helper(SIZE_BYTE);
			if(robotHit)	//when robot drives against a obstacle
				break;
		}
		if(!robotHit)
			robotDrive_helper(remain);
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
			waitingTime = calculateWaitTimeLength(distance_cm / coefficient_length);
			com.readWriteRobot(new byte[] { 'k',
					(byte) Math.round(distance_cm), '\r', '\n' }
			// round in order to get a minimal error by casting to byte
			// only effective when passing remain
			);
			robotHit = obst.avoidObstacles(waitingTime,
					System.currentTimeMillis());
			// correct odometry values if robot doesn't hit an obstacle
			if (!robotHit) {
				odometry.adjustOdometry(distance_cm / coefficient_length, 0);
			}
		}
		return robotHit;
	}

	public void robotTurn(double degree) {
		double correctedDegree = degree * coefficient_degree;
		int numberRepetitions = (int) (correctedDegree / SIZE_BYTE);
		double remain = correctedDegree % SIZE_BYTE;
		for (int i = 0; i < Math.abs(numberRepetitions); i++) {	//abs for minus degrees
			if(correctedDegree < 0)	//when degree is negative
				robotTurn_helper(-SIZE_BYTE);
			else
				robotTurn_helper(SIZE_BYTE);
		}
		robotTurn_helper(remain);
	}

	/**
	 * Turning the robot on the spot, counter-clockwise (left)
	 * 
	 * @param degree
	 */
	private void robotTurn_helper(double degree) {
		com.readWriteRobot(new byte[] { 'l', (byte) Math.round(degree), '\r',
				'\n' }
		// round in order to get a minimal error by casting to byte
		// only effective when passing remain
		);

		waitForRobotDegree(degree / coefficient_degree);
		odometry.adjustOdometry(0, degree / coefficient_degree);
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
		return (Math.abs(distance_cm) * coefficient_length_time * 1.1 * 1000);
	}

	/**
	 * calculates the time till the roation is over
	 * 
	 * @param degree
	 *            how much degree to rotate
	 */
	private void waitForRobotDegree(double degree) {
		try {
			Thread.sleep((long) (Math.abs(degree) * coefficient_degree_time
					* 1.1 * 1000));
		} catch (InterruptedException e) {
			// ignore
		}
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
			alpha = 360 - alpha;
		}

		double theta = odometry.getPosition().theta * 180 / Math.PI;

		robotTurn(alpha - theta);
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
}
