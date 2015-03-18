package com.example.robotwasd;

import java.util.List;

import jp.ksksue.driver.serial.FTDriver;
import android.widget.TextView;

/**
 * Class to controll the functions of the robot
 * @author Witsch Daniel
 *
 */
public class Robot {
	private TextView textLog;
	private Communication com;
	private Movement move;
	private Odometry odometry;
	
	public Robot(FTDriver driver, TextView textLog){
		com = new Communication(driver, textLog);
		move = new Movement(com, odometry);
		this.textLog = textLog;
	}
	
	public void initialize(){
		com.connect();
		ObstacleAvoidance obst;
		if(com.isConnected())
			obst = new ObstacleAvoidance(this);
	}
	
	public void connect(){
		com.connect();
	}
	
	public void disconnect(){
		com.disconnect();
	}
	
	
	
	public void moveForward() {
		textLog.setText("forward\n");
		move.moveForward();
	}
	
	public void moveBackward() {
		textLog.setText("backward\n");
		move.moveBackward();
	}
	
	public void stopRobot() {
		textLog.setText("stop\n");
		move.stopRobot();
	}
	
	public void turnLeft() {
		textLog.setText("turn left\n");
		move.turnLeft();
	}
	
	public void turnLeft(byte degree) {
		textLog.setText("turn left degrees\n");
		move.turnLeft(degree);
	}
	
	public void turnRight() {
		textLog.setText("turn right\n");
		move.turnRight();
	}
	
	public void turnRight(byte degree) {
		textLog.setText("turn right degrees\n");
		move.turnRight(degree);
	}
	
	public void lowerBar() {
		textLog.setText("lower\n");
		move.lowerBar();
	}
	
	public void riseBar() {
		textLog.setText("rise\n");
		move.riseBar();
	}
	
	// fixed position for bar (low)
	public void lowPositionBar() {
		textLog.setText("low Position\n");
		move.lowPositionBar();
	}
	
	// fixed position for bar (high)
	public void upPositionBar() {
		textLog.setText("up Position\n");
		move.robotSetBar((byte) 255);
	}
	
	public void LedOn() {
		textLog.setText("all LED on\n");
		move.LedOn();
	}
	
	public void LedOff() {
		textLog.setText("all LED off\n");
		move.LedOff();
	}
	
	public String readSensor() {
		return move.readSensor();
	}
	
	public void driveSquare(byte distance_cm){
		textLog.setText("drive Square\n");
		for(int i = 0; i < 4; i++) {
				move.robotDrive(distance_cm);
				move.robotTurn((byte)90);
		}
	}
}
