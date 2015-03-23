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
	private ObstacleAvoidance obst;
	private FTDriver driver;
	
	public Robot(FTDriver driver, TextView textLog){
		this.textLog = textLog;
		this.driver = driver;
	}
	
	public void initialize(){
		com = new Communication(driver, textLog);
		odometry = new Odometry();
		obst = new ObstacleAvoidance(this);
		move = new Movement(com, odometry, obst);
		com.connect();
	}
	
	public void connect(){
		com.connect();
		if(com.isConnected()){
			if(!obst.isAlive())
				obst = new ObstacleAvoidance(this);
			//obst.start();
		}
	}
	
	public void disconnect(){
		if(com.isConnected()){
			move.stopRobot();
			obst.stopThread();
			try {
				obst.join();
			} catch (InterruptedException e) {	}
			com.disconnect();
		}
	}	
	
	public void moveForward() {
		textLog.setText("forward");
		move.moveForward();
	}
	
	public void moveBackward() {
		textLog.setText("backward");
		move.moveBackward();
	}
	
	public void stopRobot() {
		textLog.setText("stop");
		move.stopRobot();
	}
	
	public void turnLeft() {
		textLog.setText("turn left");
		move.turnLeft();
	}
	
	public void turnLeft(byte degree) {
		textLog.setText("turn left degrees");
		move.turnLeft(degree);
	}
	
	public void turnRight() {
		textLog.setText("turn right");
		move.turnRight();
	}
	
	public void turnRight(byte degree) {
		textLog.setText("turn right degrees");
		move.turnRight(degree);
	}
	
	public void lowerBar() {
		textLog.setText("lower");
		move.lowerBar();
	}
	
	public void riseBar() {
		textLog.setText("rise");
		move.riseBar();
	}
	
	// fixed position for bar (low)
	public void lowPositionBar() {
		textLog.setText("low Position");
		move.lowPositionBar();
	}
	
	// fixed position for bar (high)
	public void upPositionBar() {
		textLog.setText("up Position");
		move.robotSetBar((byte) 255);
	}
	
	public void LedOn() {
		textLog.setText("all LED on");
		move.LedOn();
	}
	
	public void LedOff() {
		textLog.setText("all LED off");
		move.LedOff();
	}
	
	public String readSensor() {
		textLog.append(move.readSensor());
		return "";
	}
	
	public void driveSquare(double distance_cm){
		textLog.setText("drive Square");
		for(int i = 0; i < 4; i++) {
				move.robotDrive(distance_cm);
				move.robotTurn(90);
		}
	}
	
	public void calibrateSensor() {
		if(com.isConnected()){
			obst.setStopDistance();
			textLog.setText("calibration done");
		} else {
			textLog.setText("not connected");
		}
	}
	
	/**
	 * corrects the actual position of the robot
	 * @param distance_cm
	 * @param alpha
	 */
	public void correctPosition(double distance_cm, double alpha){
		odometry.adjustOdometry(distance_cm, alpha);
	}
}
