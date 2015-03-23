package com.example.robotwasd;

/**
 * Wrapper Class for all movements of the robot
 * @author Witsch Daniel
 *
 */
public class Movement {
	private final double coefficient_length = 1.355; //constant to correct length of driving
	private final double coefficient_length_time = 0.073;	//seconds per cm
	private final double coefficient_degree = 1.145; //constant to correct rotation
	private final double coefficient_degree_time = 0.016111;	//seconds per degree
	private final int SIZE_BYTE = 127;
	
	private Communication com;
	private Odometry odometry;
	private ObstacleAvoidance obst;
	
	public Movement(Communication com, Odometry odometry, ObstacleAvoidance obst){
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
		com.writeRobot(new byte[] {'w','\r','\n'});
	}
	
	public void moveBackward() {
		com.writeRobot(new byte[] {'x','\r','\n'});
	}
	
	public void stopRobot() {
		com.writeRobot(new byte[] {'s','\r','\n'});
	}
	
	public void turnLeft() {
		com.writeRobot(new byte[] {'a','\r','\n'});
	}
	
	public void turnLeft(double degree) {
		robotTurn(degree);
	}
	
	public void turnRight() {
		com.writeRobot(new byte[] {'d','\r','\n'});
	}
	
	public void turnRight(double degree) {
		robotTurn((-1)*degree);
	}
	
	public void lowerBar() {
		com.writeRobot(new byte[] {'-','\r','\n'});
	}
	
	public void riseBar() {
		com.writeRobot(new byte[] {'+','\r','\n'});
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
	 * @param red
	 * @param blue
	 */
	public void robotSetLeds(byte red, byte blue) {
		com.readWriteRobot(
			new byte[] { 'u', red, blue, '\r', '\n' }
		);
	}
	
	/**
	 * Driving straight forward/backward
	 * Computes correctedDistance and splits driving into parts in order to avoid overflows
	 * @param distance_cm
	 */
	public void robotDrive(double distance_cm){
		int correctedDistance = (int)(distance_cm * coefficient_length);
		int numberRepetitions = correctedDistance/SIZE_BYTE;
		int remain = correctedDistance%SIZE_BYTE;
		for (int i = 0; i < numberRepetitions; i++) {
			robotDrive_helper(SIZE_BYTE);
		}
		robotDrive_helper(remain);
	}
	
	/**
	 * Helper for driving straight forward/backward
	 * @param correctedDistance
	 */
	private void robotDrive_helper(int distance_cm) {
		if(!obst.checkObstacleAhead()) {
			com.readWriteRobot(
				new byte[] { 'k', (byte)distance_cm, '\r', '\n' }
			);
				
			obst.startMovement();	
			waitForRobotLength((double)distance_cm/coefficient_length);
			//correct odometry values if robot doesn't hit a obstacle
			if(!obst.stopMovement())	
				odometry.adjustOdometry(distance_cm/coefficient_length, 0);
		}
	}
	
	public void robotTurn(double degree){
		int correctedDegree = (int)(degree * coefficient_degree);
		int numberRepetitions = correctedDegree/SIZE_BYTE;
		int remain = correctedDegree%SIZE_BYTE;
		for (int i = 0; i < numberRepetitions; i++) {
			robotTurn_helper(SIZE_BYTE);
		}
		robotTurn_helper(remain);		
	}
	
	/**
	 * Turning the robot on the spot, counter-clockwise (left)
	 * @param degree
	 */
	private void robotTurn_helper(int degree) {		
		com.readWriteRobot(
			new byte[] { 'l', (byte)degree, '\r', '\n' }
		);
		
		waitForRobotDegree((double)degree/coefficient_degree);
		odometry.adjustOdometry(0, degree/coefficient_degree);
	}
	
	/**
	 * Driving by velocity
	 * @param left
	 * @param right
	 */
	public void robotSetVelocity(byte left, byte right) {
		com.readWriteRobot(
			new byte[] { 'i', left, right, '\r', '\n' }
		);
	}
	
	/**
	 * Using the bar
	 * @param value
	 */
	public void robotSetBar(byte value) {
		com.readWriteRobot(
			new byte[] { 'o', value, '\r', '\n' }
		);
	}
	
	/**
	 * calculates the time till the movement is over
	 * @param distance_cm distance to drive
	 */
	private void waitForRobotLength(double distance_cm){
		try {
			Thread.sleep((long)(distance_cm * coefficient_length_time * 1.1 * 1000));
		} catch (InterruptedException e) {
			// ignore
		}
	}
	
	/**
	 * calculates the time till the roation is over
	 * @param degree how much degree to rotate
	 */
	private void waitForRobotDegree(double degree){
		try {
			Thread.sleep((long)(degree * coefficient_degree_time * 1.1 * 1000));
		} catch (InterruptedException e) {
			// ignore
		}
	}
	
	
	/**
	 * Turns robot to a specific angle alpha
	 * @param alpha Desired orientation-angle the robot will be turned to: [0,360) [degree]
	 */
	public void setRobotOrientation(double alpha){
		// convert angle alpha to number within interval [0,360)
		alpha = alpha % 360;
		if (alpha < 0)
			alpha = 360 - alpha;
		
		double theta = odometry.getPosition().theta * 180 / Math.PI;
		
		robotTurn(alpha-theta);
	}
	
	/**
	 * moves the robot back to origin position.
	 * x = y = theta = 0.
	 */
	public void driveToOrigin(){
		Position p = odometry.getPosition();
		
		// robot is in quadrant I or II
		if (p.y > 0){
			// turn robot towards origin, parallel to y-axis, drive until y=0
			setRobotOrientation(270);
		}
		// robot is in quadrant III or IV
		else if (p.y < 0){
			// turn robot towards origin, parallel to y-axis, drive until y=0
			setRobotOrientation(90);
		}
		
		robotDrive(p.y);
		
		// now the robot is in y-position zero at some x-position facing either
		// straight down (theta = 3/2*PI) or straight up (theta = PI/2)
		
		if (p.x > 0){
			// turn robot directly towards origin
			setRobotOrientation(180);
		}
		else if (p.x < 0){
			// turn robot directly towards origin
			setRobotOrientation(0);
		}
		robotDrive(p.x);
		setRobotOrientation(0);
	}
}
