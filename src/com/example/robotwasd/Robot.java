package com.example.robotwasd;

import jp.ksksue.driver.serial.FTDriver;
import android.widget.TextView;

/**
 * Class to controll the functions of the robot
 * 
 * @author Witsch Daniel
 * 
 */
public class Robot {
	public TextView textLog;
	private Communication com;
	private Movement move;
	private Odometry odometry;
	private ObstacleAvoidance obst;
	private FTDriver driver;

	public Robot(FTDriver driver, TextView textLog) {
		this.textLog = textLog;
		this.driver = driver;
	}

	public void initialize() {
		com = new Communication(driver, textLog);
		odometry = new Odometry();
		obst = new ObstacleAvoidance(this, odometry);
		move = new Movement(com, odometry, obst);
		connect();
	}


	 public void connect() { 
		 com.connect();
	 }
	 

	 public void disconnect() { 
		 if (com.isConnected()) { 
			 move.stopRobot();
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

	public void turnLeft(double degree) {
		textLog.setText("turn left degrees");
		move.turnLeft(degree);
	}

	public void turnRight() {
		textLog.setText("turn right");
		move.turnRight();
	}

	public void turnRight(double degree) {
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
		move.upPositionBar();
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
		return move.readSensor();
	}

	public void driveSquare(double distance_cm) {
		textLog.setText("drive Square");
		for (int i = 0; i < 4; i++) {
			move.robotDrive(distance_cm);
			move.robotTurn(90);
		}
	}

	public void calibrateSensor() {
		textLog.setText("calibration done");
		textLog.setText(Double.toString(obst.setStopDistance()));
		
	}

	public void getOdometryData() {
		textLog.setText(odometry.getPosition().toString());
	}

	public void OdometryTestMovement() {
		move.testMovement();
	}
	
	public void initOdometryPosition(){
		odometry.setPosition(0, 0, 0);
	}
	
	public void navigateToPosition(Position goal, int depth){
		/**
		 *  calc angle and distance towards goal considering the robot's current position
		 *  
		 *  therefore we subtract the robot-position from the goal-position to "project" the
		 *  robot into the origin (of the coordinate system), also the goal is projected but now
		 *  the calculation is easy
		 *  
		 *  change x and y arguments in atan2-method because atan2 calculates with the y-axis as 0 degree
		 *  but we need the x-axis as 0 degree
		 */
		
		Position robotPos = odometry.getPosition();
		double angle = Math.atan2(goal.y - robotPos.y, goal.x - robotPos.x)*180/Math.PI;
		double distance = Math.hypot(goal.x - robotPos.x, goal.y - robotPos.y);
		
		//easy obstacle avoidance
		move.setRobotOrientation(angle);
		//obstacle ahead, drive around and then drive again to position
		if(obst.checkObstacleAhead()){
			move.turnLeft(90 + depth * 5);
			move.robotDrive(50 + depth * 5);
			navigateToPosition(goal, depth + 1);
		}else{
			//no obstacle ahead
			if(move.robotDrive(distance)){
				//during driving robot drive against an obstacle, drive around and then drive again to position
				move.turnLeft(90);
				move.robotDrive(50);
				navigateToPosition(goal, 0);
			}else
				//driving worked perfect, no obstacles
				move.setRobotOrientation(goal.theta);
		}
	}
}
