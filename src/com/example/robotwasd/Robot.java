package com.example.robotwasd;

import java.util.List;

import jp.ksksue.driver.serial.FTDriver;
import android.widget.TextView;

public class Robot {
	private FTDriver com;
	private TextView textLog;
	private static double coefficient_length = 1.355;
	private static double coefficient_length_time = 0.073;	//seconds per cm
	private static double coefficient_degree = 1.145;
	private static double coefficient_degree_time = 0.016111;	//seconds per degree
	
	private Odometry odometry;
	
	public Robot(FTDriver com, TextView textLog){
		this.com = com;
		this.textLog = textLog;
	}
	
	public void initialize(){
		connect();
		ObstacleAvoidance obst = new ObstacleAvoidance(this);
	}
	
	public void connect() {
		if (com.begin(9600))
			textLog.append("connected\n");
        else
        	textLog.append("could not connect\n");
	}
	
	public void disconnect() {
	       com.end();
	       if (!com.isConnected()) {
	          textLog.append("disconnected\n");
	       }
	}
	
	public void reconnect() {
		if (!com.isConnected()) {
			connect();
		}
	}
	
	public void moveForward() {
		textLog.setText("forward\n");
		comWrite(new byte[] {'w','\r','\n'});
	}
	
	public void moveBackward() {
		textLog.setText("backward\n");
		comWrite(new byte[] {'x','\r','\n'});
	}
	
	public void stopRobot() {
		textLog.setText("stop\n");
		comWrite(new byte[] {'s','\r','\n'});
	}
	
	public void turnLeft() {
		textLog.setText("turn left\n");
		comWrite(new byte[] {'a','\r','\n'});
	}
	
	public void turnLeft(byte degree) {
		textLog.setText("turn left degrees\n");
		robotTurn(degree);
	}
	
	public void turnRight() {
		textLog.setText("turn right\n");
		comWrite(new byte[] {'d','\r','\n'});
	}
	
	public void turnRight(byte degree) {
		textLog.setText("turn right degrees\n");
		robotTurn((byte)((-1)*degree));
	}
	
	public void lowerBar() {
		textLog.setText("lower\n");
		comWrite(new byte[] {'-','\r','\n'});
	}
	
	public void riseBar() {
		textLog.setText("rise\n");
		comWrite(new byte[] {'+','\r','\n'});
	}
	
	// fixed position for bar (low)
	public void lowPositionBar() {
		textLog.setText("low Position\n");
		robotSetBar((byte) 0);
	}
	
	// fixed position for bar (high)
	public void upPositionBar() {
		textLog.setText("up Position\n");
		robotSetBar((byte) 255);
	}
	
	public void LedOn() {
		textLog.setText("all LED on\n");
		robotSetLeds((byte) 255, (byte) 128);
	}
	
	public void LedOff() {
		textLog.setText("all LED off\n");
		robotSetLeds((byte) 0, (byte) 0);
	}
	
	public String readSensor() {
		String data = comReadWrite(new byte[] { 'q', '\r', '\n' });
		//textLog.setText(data +"\n");
		return data;
	}
	
	public void driveSquare(byte distance_cm){
		textLog.setText("drive Square\n");
		for(int i = 0; i < 4; i++) {
				robotDrive(distance_cm);
				robotTurn((byte)90);
		}
	}
		
		
		
	
	
	/** helper functions */
	/********************************************/
		

	
	/**
	 * Setting blue and red LED
	 * @param red
	 * @param blue
	 */
	private void robotSetLeds(byte red, byte blue) {
		comReadWrite(
			new byte[] { 'u', red, blue, '\r', '\n' }
		);
	}
	
	/**
	 * Driving straight forward/backward
	 * @param distance_cm
	 */
	private void robotDrive(byte distance_cm) {
		
		int correctedDistance = (int)((double)distance_cm * coefficient_length);
		comReadWrite(
			new byte[] { 'k', (byte)correctedDistance, '\r', '\n' }
		);
		waitForRobotLength(distance_cm);
		odometry.adjustOdometry(distance_cm, 0);
	}
	
	/**
	 * Turning the robot on the spot, counter-clockwise (left)
	 * @param degree
	 */
	private void robotTurn(byte degree) {
		int correctedDegree = (int)((double) degree * coefficient_degree);
		comReadWrite(
			new byte[] { 'l', (byte)correctedDegree, '\r', '\n' }
		);
		waitForRobotDegree(degree);
		odometry.adjustOdometry(0, degree);
	}
	
	/**
	 * Driving by velocity
	 * @param left
	 * @param right
	 */
	private void robotSetVelocity(byte left, byte right) {
		comReadWrite(
			new byte[] { 'i', left, right, '\r', '\n' }
		);
	}
	
	/**
	 * Using the bar
	 * @param value
	 */
	private void robotSetBar(byte value) {
		comReadWrite(
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
			Thread.sleep((long)((double)degree * coefficient_degree_time * 1.1 * 1000));
		} catch (InterruptedException e) {
			// ignore
		}
	}
	
	
	/**Low Level Functions */
	/*****************************/
	
	synchronized private void comWrite(byte[] data) {
		if (com.isConnected()) {
			com.write(data);
		}
		else {
			textLog.append("not connected\n");
		}
	}
	/**
	* reads from the serial buffer. due to buffering, the read command is
	* issued 3 times at minimum and continuously as long as there are bytes to
	* read from the buffer. Note that this function does not block, it might
	* return an empty string if no bytes have been read at all.
	*
	* @return buffer content as string
	*/
	synchronized private String comRead() {
		String s = "";
		int i = 0;
		int n = 0;
		while (i < 3 || n > 0) {
			byte[] buffer = new byte[256];
			n = com.read(buffer);
			s += new String(buffer, 0, n);
			i++;
		}
		return s;
	}
	
	/**
	* write data to serial interface, wait 100 ms and read answer.
	*
	* @param data
	* to write
	* @return answer from serial interface
	*/
	synchronized private String comReadWrite(byte[] data) {
		com.write(data);
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// ignore
		}
		return comRead();
	}

}
