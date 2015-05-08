package com.example.robotwasd;

import java.util.ArrayList;
import java.util.List;

import jp.ksksue.driver.serial.FTDriver;

/**
 * Class to controll the functions of the robot
 * 
 * @author Witsch Daniel
 * 
 */
public class Robot {
	private Communication com;
	private Movement move;
	private Odometry odometry;
	private ObstacleAvoidance obst;
	private FTDriver driver;
	private Homography homography;

	public Robot(FTDriver driver) {
		this.driver = driver;
	}

	/**
	 * create all helper classes
	 * @return true if connecting to robot was successfully
	 */
	public boolean initialize() {
		com = new Communication(driver);
		odometry = new Odometry();
		obst = new ObstacleAvoidance(this, odometry);
		move = new Movement(com, odometry, obst);
		homography = new Homography();
		return connect();
	}

	/**
	 * create the usb connection to the robot
	 * @return true if robot is connected otherwise false
	 */
	public boolean connect() { 
		 return com.connect();
	}
	
	/**
	 * disconnect from robot
	 * @return true if disconnection was succesfully
	 */
	public boolean disconnect() { 
		boolean ret = false; 
		if (com.isConnected()) { 
			 //stop robot
			 move.stopRobot();
			 ret = com.disconnect(); 
		} 
		return ret;
	}

	public void moveForward() {
		move.moveForward();
	}

	public void moveBackward() {
		move.moveBackward();
	}

	public void stopRobot() {
		move.stopRobot();
	}

	public void turnLeft() {
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
		move.lowerBar();
	}

	public void riseBar() {
		move.riseBar();
	}

	/**
	 * low fix position for bar
	 */
	public void lowPositionBar() {
		move.lowPositionBar();
	}

	/**
	 * high fix position for bar
	 */
	public void upPositionBar() {
		move.upPositionBar();
	}

	/**
	 * all LEDs on
	 */
	public void LedOn() {
		move.LedOn();
	}

	/**
	 * all LEDs off
	 */
	public void LedOff() {
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
		for (int i = 0; i < 4; i++) {
			move.robotDrive(distance_cm, true);
			move.robotTurn(90);
		}
	}

	/**
	 * calibrate the distance for obstacle avoidance
	 * @return measured stop distance
	 */
	public double calibrateSensor() {
		return obst.setStopDistance();	
	}
	
	/**
	 * set odometry to (0,0,0)
	 */
	public void initOdometryPosition(){
		odometry.setPosition(0, 0, 0);
	}
	
	/**
	 * get odometry position
	 * @return actual odometry position from the robot
	 */
	public Position getOdometryPosition(){
		return odometry.getPosition();
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
			for (int i=0; i < 4 && !ValueHolder.existslowestPoint(); i++) {
				move.robotTurn(90);
			}
			if (ValueHolder.existslowestPoint()) {
				return;
			}
			navigateToPosition(p, true, false);
		}
	}
	
	public void calibrateHomography() {
		homography.calibrateHomography();
	}
}
