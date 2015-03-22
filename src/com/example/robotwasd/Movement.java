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
	private final int SIZE_BYTE = 128;
	
	private Communication com;
	private Odometry odometry;
	private ObstacleAvoidance obst;
	
	public Movement(Communication com, Odometry odometry, ObstacleAvoidance obst){
		this.com = com;
		this.odometry = odometry;
		this.obst = obst;
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
	public void robotDrive(int distance_cm){
		int correctedDistance = (int)((double)distance_cm * coefficient_length);
		int numberRepetitions = correctedDistance/SIZE_BYTE;
		int remain = correctedDistance%SIZE_BYTE;
		for (int i = 0; i < numberRepetitions; i++) {
			robotDrive_helper((byte)SIZE_BYTE);
		}
		robotDrive_helper((byte)remain);
	}
	
	/**
	 * Helper for driving straight forward/backward
	 * @param correctedDistance
	 */
	private void robotDrive_helper(byte correctedDistance) {
		String ok = com.readWriteRobot(
			new byte[] { 'k', (byte)correctedDistance, '\r', '\n' }
		);
		
		//when robot is connected
		if(!ok.equals("")){
			obst.startMovement(false); //false for driving straight ahead
			waitForRobotLength(correctedDistance);
			odometry.adjustOdometry(correctedDistance, 0);
			obst.stopMovement();
		}
	}
	
	public void robotTurn(int degree){
		int correctedDegree = (int)((double) degree * coefficient_degree);
		int numberRepetitions = correctedDegree/SIZE_BYTE;
		int remain = correctedDegree%SIZE_BYTE;
		for (int i = 0; i < numberRepetitions; i++) {
			robotTurn_helper((byte)SIZE_BYTE);
		}
		robotTurn_helper((byte)remain);
		
	}
	
	/**
	 * Turning the robot on the spot, counter-clockwise (left)
	 * @param degree
	 */
	public void robotTurn_helper(byte correctedDegree) {		
		String ok = com.readWriteRobot(
			new byte[] { 'l', (byte)correctedDegree, '\r', '\n' }
		);
		
		// when robot is connected
		if(!ok.equals("")){
			obst.startMovement(true); //true for rotation
			waitForRobotDegree(correctedDegree);
			odometry.adjustOdometry(0, correctedDegree);
			obst.stopMovement();
		}
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
	private void waitForRobotLength(byte distance_cm){
		try {
			Thread.sleep((long)((double)distance_cm * coefficient_length_time * 1.1 * 1000));
		} catch (InterruptedException e) {
			// ignore
		}
	}
	
	/**
	 * calculates the time till the roation is over
	 * @param degree how much degree to rotate
	 */
	private void waitForRobotDegree(byte degree){
		try {
			Thread.sleep((long)((double)degree * coefficient_degree_time * 1000));
		} catch (InterruptedException e) {
			// ignore
		}
	}
	
	/**
	 * read out the sensors
	 */
	public String readSensor() {
		String data = com.readWriteRobot(new byte[] { 'q', '\r', '\n' });
		return data;
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
	
	public void turnLeft(byte degree) {
		robotTurn(degree);
	}
	
	public void turnRight() {
		com.writeRobot(new byte[] {'d','\r','\n'});
	}
	
	public void turnRight(byte degree) {
		robotTurn((byte)((-1)*degree));
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
}
