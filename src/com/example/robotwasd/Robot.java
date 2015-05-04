package com.example.robotwasd;

import java.util.ArrayList;
import java.util.List;

import jp.ksksue.driver.serial.FTDriver;
import android.widget.TextView;

import com.example.OpenCV.ColorBlobDetection;

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
	private ColorBlobDetection blobDetection;
	private Homography homography;

	public Robot(FTDriver driver, TextView textLog, ColorBlobDetection blobDetection) {
		this.textLog = textLog;
		this.driver = driver;
		this.blobDetection = blobDetection;
	}

	/**
	 * create all helper classes
	 */
	public void initialize() {
		com = new Communication(driver, textLog);
		odometry = new Odometry();
		obst = new ObstacleAvoidance(this, odometry);
		move = new Movement(com, odometry, obst, textLog);
		homography = new Homography(blobDetection);
		connect();
	}

	/**
	 * create the usb connection to the robot
	 */
	public void connect() { 
		 com.connect();
	}
	
	/**
	 * disconnect from robot
	 */
	public void disconnect() { 
		 if (com.isConnected()) { 
			 //stop robot
			 move.stopRobot();
			 com.disconnect(); 
		} 
	}

	public void moveForward() {
		// textLog.setText("forward");
		move.moveForward();
	}

	public void moveBackward() {
		// textLog.setText("backward");
		move.moveBackward();
	}

	public void stopRobot() {
		// textLog.setText("stop");
		move.stopRobot();
	}

	public void turnLeft() {
		// textLog.setText("turn left");
		move.turnLeft();
	}

	/**
	 * turning with obstacle avoidance
	 * @param degree
	 */
	public void turnLeft(double degree) {
		move.turnLeft(degree);
	}

	public void turnRight() {
		// textLog.setText("turn right");
		move.turnRight();
	}
	
	/**
	 * turning with obstacle avoidance
	 * @param degree
	 */
	public void turnRight(double degree) {
		move.turnRight(degree);
	}

	public void lowerBar() {
		// textLog.setText("lower");
		move.lowerBar();
	}

	public void riseBar() {
		// textLog.setText("rise");
		move.riseBar();
	}

	/**
	 * low fix position for bar
	 */
	public void lowPositionBar() {
		// textLog.setText("low Position");
		move.lowPositionBar();
	}

	/**
	 * high fix position for bar
	 */
	public void upPositionBar() {
		// textLog.setText("up Position");
		move.upPositionBar();
	}

	/**
	 * all LEDs on
	 */
	public void LedOn() {
		// textLog.setText("all LED on");
		move.LedOn();
	}

	/**
	 * all LEDs off
	 */
	public void LedOff() {
		// 	textLog.setText("all LED off");
		move.LedOff();
	}

	/**
	 * read out the sensor data (range sensors)
	 * @return the data of the range sensors
	 */
	public String readSensor() {
		return move.readSensor();
	}

	/**
	 * drive a square
	 * @param distance_cm
	 */
	public void driveSquare(double distance_cm) {
		// textLog.setText("drive Square");
		for (int i = 0; i < 4; i++) {
			move.robotDrive(distance_cm, true);
			move.robotTurn(90);
		}
	}

	/**
	 * calibrate the distance for obstacle avoidance
	 */
	public void calibrateSensor() {
		obst.setStopDistance();
		// textLog.setText("calibration done: " + Double.toString(obst.setStopDistance()) + "cm");		
	}

	/**
	 * print out the actual odometry position to the textView
	 */
	public void printOdometryData() {
		// textLog.setText(odometry.getPosition().toString());
	}
	
	/**
	 * set odometry to (0,0,0)
	 */
	public void initOdometryPosition(){
		odometry.setPosition(0, 0, 0);
		printOdometryData();
	}
	
	/**
	 * robot drive to the goal position from his actual position
	 * @param goal goal position
	 */
	public void navigateToPosition(Position goal, boolean withOrientation, boolean withAvoidance) {
		/**
		 *  calc angle and distance towards goal considering the robot's current position
		 *  
		 *  therefore we subtract the robot-position from the goal-position to "project" the
		 *  robot into the origin (of the coordinate system), also the goal is projected but now
		 *  the calculation is easy
		 */
		
		move.turnTowardsPosition(goal);

		while(true) {
			Position robotPos = odometry.getPosition();
			double distance = Math.hypot(goal.x - robotPos.x, goal.y - robotPos.y);
			if(!withAvoidance)
				distance = distance - 10;
			if (move.robotDrive(distance, withAvoidance)) {
				//during driving robot drive against an obstacle, drive around and then drive again to position
				move.avoidanceAlgorithm(goal);
			}
			else {
				//driving worked perfect, no obstacles
				if (withOrientation) {
					move.setRobotOrientation(goal.theta);
				}
				return;
			}
		}
		
	}
	
	public void collectBall(boolean withExplore) {
		if (withExplore) {
			explore();
		}
		
		Position ballPosition = homography.collectBall();
		
		ballPosition.x += odometry.getPosition().x;
		ballPosition.y += odometry.getPosition().y;
		
		navigateToPosition(ballPosition, false, false);
		
		lowPositionBar();
		
		navigateToPosition(new Position(150,150,0), false, false);
		
		navigateToPosition(new Position(0,0,0), true, false);
		
	}
	
	public void explore() {
		List<Position> positions = new ArrayList<Position>();
		Position p1 = new Position( 1, 1, 45);
		Position p2 = new Position(-1, 1,180);
		Position p3 = new Position(-1,-1,-90);
		Position p4 = new Position( 1,-1,  0);
		Position p5 = new Position( 0, 0,  0);
		positions.add(p1);
		positions.add(p2);
		positions.add(p3);
		positions.add(p4);
		positions.add(p5);
		
		for (Position p : positions) {
			for (int i=0; i < 4 && !blobDetection.existslowestPoint(); i++) {
				move.robotTurn(90);
			}
			if (blobDetection.existslowestPoint()) {
				return;
			}
			navigateToPosition(p, true, false);
		}
	}
	
	public void calibrateHomography() {
		homography.calibrateHomography();
	}
}
