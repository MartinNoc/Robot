package com.example.robotwasd;


/**
 * Wrapper Class for all movements of the robot
 * 
 * @author Witsch Daniel
 * 
 */
public class Movement {
	private final double COEFFICIENT_LENGTH = 1.355;		// constant to correct length of driving
	private final double COEFFICIENT_LENGTH_TIME = 0.073; 	// seconds per cm
	private final double COEFFICIENT_DEGREE = 1.14;		// constant to correct rotation
	private final double COEFFICIENT_DEGREE_TIME = 0.016111;// seconds per degree
	
	static boolean turnLeft = true;
	
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



	/**
	 * Setting blue and red LED
	 */
	public void robotSetLeds(byte red, byte blue) {
		com.readWriteRobot(new byte[] { 'u', red, blue, '\r', '\n' });
	}

	/**
	 * Driving straight forward/backward. Computes correctedDistance and splits
	 * driving into parts in order to avoid overflows.
	 * 
	 * No Obstacle Avoidance.
	 */
	public void robotDrive(double distance_cm) {
		double correctedDistance = distance_cm * COEFFICIENT_LENGTH;
		int numberRepetitions = (int) (Math.abs(correctedDistance) / Byte.MAX_VALUE);
		double remain = correctedDistance % Byte.MAX_VALUE;
		
		for (int i = 0; i < numberRepetitions; i++) {
			double distance_robot = Math.signum(correctedDistance) * Byte.MAX_VALUE;
			com.readWriteRobot(new byte[] { 'k', (byte) Math.round(distance_robot), '\r', '\n' });
			
			waitForRobotDrive(Byte.MAX_VALUE / COEFFICIENT_LENGTH);
			
		}
		com.readWriteRobot(new byte[] { 'k', (byte) Math.round(remain), '\r', '\n' });
		waitForRobotDrive(remain / COEFFICIENT_LENGTH);
		
		odometry.adjustOdometry(distance_cm, 0);
	}
	
	/**
	 * Driving straight forward/backward. With Obstacle Avoidance
	 * 
	 * @return true if robot hits an obstacle
	 */
	public boolean robotDriveOA(double distance_cm) {		
		double correctedDistance = distance_cm * COEFFICIENT_LENGTH;
		int numberRepetitions = (int) (Math.abs(correctedDistance) / Byte.MAX_VALUE);
		double remain = correctedDistance % Byte.MAX_VALUE;

		for (int i = 0; i < numberRepetitions; i++) {
			double distance_robot = Math.signum(correctedDistance) * Byte.MAX_VALUE;
			if (robotDriveOA_helper(distance_robot)) {
				return true;
			}
		}
		//when robot doesn't drive against an obstacle
		return robotDriveOA_helper(remain);
	}

	/**
	 * Helper for robotDriveOA method.
	 * 
	 * @param distance_cm Real distance robot will drive
	 */
	private boolean robotDriveOA_helper(double distance_cm) {
		double waitingTime;
		boolean robotHit = false;
		
		if (!obst.checkObstacleAhead()) {
			waitingTime = calculateWaitTimeLength(distance_cm / COEFFICIENT_LENGTH);
			com.readWriteRobot(new byte[] { 'k', (byte) Math.round(distance_cm), '\r', '\n' });
			// round in order to get a minimal error by casting to byte
			// only effective when passing remain
		
			robotHit = obst.avoidObstacles(waitingTime, System.currentTimeMillis());
			// correct odometry values if robot doesn't hit an obstacle
			if (!robotHit) {
				odometry.adjustOdometry(distance_cm / COEFFICIENT_LENGTH, 0);
			}
		
			return robotHit;
		}
		else {
			return true;
		}
	}

	/**
	 * Turning the robot on the spot, counter-clockwise (left)
	 * @param degree
	 */
	public void robotTurn(double degree) {
		double correctedDegree = degree * COEFFICIENT_DEGREE;
		int numberRepetitions = (int) (Math.abs(correctedDegree) / Byte.MAX_VALUE);
		double remain = correctedDegree % Byte.MAX_VALUE;
		
		for (int i = 0; i < numberRepetitions; i++) {
			double degree_robot = Math.signum(correctedDegree) * Byte.MAX_VALUE;
			robotTurn_helper(degree_robot);
		}
		robotTurn_helper(remain);
		
		odometry.adjustOdometry(0, degree);
	}

	/**
	 * Helper for robotTurn method.
	 */
	private void robotTurn_helper(double degree) {
		com.readWriteRobot(new byte[] { 'l', (byte) Math.round(degree), '\r', '\n' }
		// round in order to get a minimal error by casting to byte
		// only effective when passing remain
		);

		waitForRobotDegree(degree / COEFFICIENT_DEGREE);
	}

	/**
	 * Driving by velocity
	 */
	public void robotSetVelocity(byte left, byte right) {
		com.readWriteRobot(new byte[] { 'i', left, right, '\r', '\n' });
	}

	/**
	 * Using the bar
	 */
	public void robotSetBar(byte value) {
		com.readWriteRobot(new byte[] { 'o', value, '\r', '\n' });
	}

	/**
	 * Calculates the time till the movement is over [ms]
	 * 
	 * @param distance_cm distance to drive
	 */
	private double calculateWaitTimeLength(double distance_cm) {
		return (Math.abs(distance_cm) * COEFFICIENT_LENGTH_TIME * 1.1 * 1000);
	}
	
	private void waitForRobotDrive(double distance_cm) {
		try {
			Thread.sleep((long)(Math.abs(distance_cm) * COEFFICIENT_LENGTH_TIME * 1.1 * 1000));
		} catch (InterruptedException e) {
			// ignore
		}
	}

	/**
	 * Calculates the time till the roation is over and sleeps until rotation is done.
	 * 
	 * @param degree amount of degree to rotate
	 */
	private void waitForRobotDegree(double degree) {
		
		try {
			Thread.sleep((long)(Math.abs(degree) * COEFFICIENT_DEGREE_TIME * 1.1 * 1000));
		} catch (InterruptedException e) {
			// ignore
		}
		
	}

	/**
	 * Moves the robot back to origin position. x = y = theta = 0.
	 */
	@Deprecated 
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
		robotDriveOA(p.y);

		// now the robot is in y-position zero at some x-position facing
		// straight down (theta = 3/2*PI)

		/**
		 * turn robot parallel to x-axis facing down the negative x-axis. no
		 * case analysis due to same reasons as mentioned above
		 */
		setRobotOrientation(180);
		robotDriveOA(p.x);

		// now the robot is in (x/y) position (0/0) at angle theta = PI

		setRobotOrientation(0);
	}
	
	/**
	 * Turns robot to a specific angle alpha
	 * 
	 * @param alpha Desired orientation-angle the robot will be turned to: [0,360) [degree]
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
	
	/**
	 * Called if robot faces an obstacle.
	 * Tries to go round the obstacle until the way to the goal is free.
	 * 
	 * @param goal Position of the goal
	 */
	public void avoidanceAlgorithm(Position goal) {
		// turn robot until it does not face an obstacle
		while(obst.checkObstacleAhead()) {
			if (turnLeft) 
				robotTurn(90);
			else 
				robotTurn(-90);
		}
		robotDriveOA(90);
		turnTowardsPosition(goal);
		
		if (obst.checkObstacleAhead())
			avoidanceAlgorithm(goal);
		else
			turnLeft = !turnLeft;
	}
	
	/**
	 * Rotates the robot towards the goal position
	 * @param goal remote point the robot is going to face
	 */
	public void turnTowardsPosition(Position goal) {
		Position robotPos = odometry.getPosition();
		double angle = Math.atan2(goal.y - robotPos.y, goal.x - robotPos.x) * 180 / Math.PI;
		setRobotOrientation(angle);
	}
}
