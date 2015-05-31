package com.example.robotwasd;

import java.util.ArrayList;
import java.util.List;

import jp.ksksue.driver.serial.FTDriver;

import org.opencv.core.Scalar;

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
	private BeaconBallDetection beaconBallDetection;

	public Robot(FTDriver driver) {
		this.driver = driver;
	}

	/**
	 * create all helper classes
	 * @return true if connecting to robot was successful
	 */
	public boolean initialize() {
		com = new Communication(driver);
		odometry = new Odometry();
		obst = new ObstacleAvoidance(this, odometry);
		move = new Movement(com, odometry, obst);
		homography = new Homography();
		beaconBallDetection = new BeaconBallDetection(homography, odometry, this);
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

	public void turnLeft(double degree) {
		move.turnLeft(degree);
	}

	public void turnRight() {
		move.turnRight();
	}
	
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
	 * @param distance_cm side length of square.
	 */
	public void driveSquare(double distance_cm) {
		for (int i = 0; i < 4; i++) {
			move.robotDriveOA(distance_cm);
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
	public void navigateToPosition(Position goal) {
		/**
		 *  calc angle and distance towards goal considering the robot's current position
		 *  
		 *  therefore we subtract the robot-position from the goal-position to "project" the
		 *  robot into the origin (of the coordinate system), also the goal is projected but now
		 *  the calculation is easy
		 */
		
		move.turnTowardsPosition(goal);
		
		Position robotPos = odometry.getPosition();
		double distance = Math.hypot(goal.x - robotPos.x, goal.y - robotPos.y);
		
		move.robotDrive(distance);
		
		move.setRobotOrientation(goal.theta);
	
	}
	
	/**
	 * robot drive to the goal position from his actual position with Obstacle Avoidance
	 */
	public void navigateToPositionOA(Position goal) {
		move.turnTowardsPosition(goal);
		
		while (true) {
			Position robotPos = odometry.getPosition();
			double distance = Math.hypot(goal.x - robotPos.x, goal.y - robotPos.y);
			
			if (move.robotDriveOA(distance)) {
				//during driving robot hit an obstacle, drive around and then head towards goal
				move.avoidanceAlgorithm(goal);
			}
			else {
				//driving worked perfect, no obstacles
				move.setRobotOrientation(goal.theta);
				return;
			}
		}
	}
	
	/**
	 * navigates to the egocentric goal position, image coordinates
	 * @param goal image goal position
	 */
	public void navigateToEgocentricPosition(Position goal) {
		double angle = Math.atan2(goal.y, goal.x) * 180 / Math.PI;
		move.robotTurn(angle);
		
		double distance = goal.calcHypotenuse();
		
		move.robotDrive(distance);
	}
	
	/**
	 * collect all balls on the image, with or without exploring the workspace
	 * @param withExplore
	 */
	public void collectBalls(boolean withExplore) {		
		State state = State.LOCALIZATION_START;
		
		while(state != State.FINISHED){
			System.out.println("Robot: state " + state);
			switch(state) {
				case LOCALIZATION_START:
					lowPositionBar();
					startSelfLocalization();
					state = State.DRIVING_HOME;
					break;
					
				case LOCALIZATION_CATCHED:
					startSelfLocalization();
					state = State.DRIVING_TARGET;
					break;
					
				case DRIVING_HOME:
					upPositionBar();
					navigateToPosition(new Position(0,0,0));
					state = State.EXPLORING;
					break;
					
				case DRIVING_TARGET:
					navigateToPosition(new Position(0,0,0));
					state = State.DRIVING_HOME;
					break;
					
				case EXPLORING:
					if (withExplore) {
						if(explore()){
							state = State.FINISHED;
						} else {
							state = State.BALL_CATCHING;
						}
					} else {
						beaconBallDetection.startBeaconBallDetection();
						if(beaconBallDetection.detectedBalls.size() > 0){
							state = State.BALL_CATCHING;
						} else {
							state = State.FINISHED;
						}
					}
					break;
					
				case BALL_CATCHING:
					// choose the ball which is nearest to the robot (highest Y-pixel value = lowest on image)
					int ballIndex = 0;
					double lowestDistance = Math.hypot(beaconBallDetection.detectedBalls.get(0).x, beaconBallDetection.detectedBalls.get(0).y);
					for (int i = 1; i < beaconBallDetection.detectedBalls.size(); i++) {
						if (Math.hypot(beaconBallDetection.detectedBalls.get(i).x, beaconBallDetection.detectedBalls.get(i).y) < lowestDistance) {
							lowestDistance = Math.hypot(beaconBallDetection.detectedBalls.get(0).x, beaconBallDetection.detectedBalls.get(0).y);
							ballIndex = i;
						}
					}
					Position ballPosition = beaconBallDetection.detectedBalls.get(ballIndex);
					
					// adaption from camera-ball-position to robot-ball-position
					ballPosition.y += 4;
					ballPosition.x -= 22; 
					
					navigateToEgocentricPosition(ballPosition);
					
					lowPositionBar();
					
					state = State.LOCALIZATION_CATCHED;
					break;
			}
		}
		
		
	}
	
	/*  _____________________(125/125)
	 * |          |          |
	 * |  p3      |     p2   |
	 * |          |          |
	 * |          |          |
	 * |--------p0,7---p1,6--|
	 * |          |          |
	 * |          |          |
	 * |  p4      |     p5   |
	 * |__________|__________|
	 * (-125/-125)           (125/-125)
	 * 
	 */
	public boolean explore() {
		List<Position> positions = new ArrayList<Position>();
		Position p1 = new Position( 60,  0,  0);
		Position p2 = new Position( 60, 60, 90);
		Position p3 = new Position(-60, 60,180);
		Position p4 = new Position(-60,-60,-90);
		Position p5 = new Position( 60,-60,  0);
		Position p6 = new Position( 60,  0, 90);
		Position p7 = new Position(  0,  0,180);
		positions.add(p1);
		positions.add(p2);
		positions.add(p3);
		positions.add(p4);
		positions.add(p5);
		positions.add(p6);
		positions.add(p7);
		
		for (Position p : positions) {
			beaconBallDetection.startBeaconBallDetection();
			for (int i=0; i < 8 && beaconBallDetection.detectedBalls.size() == 0; i++) {
				move.robotTurn(45);
				beaconBallDetection.startBeaconBallDetection();
			}
			if (beaconBallDetection.detectedBalls.size() > 0) {
				return false;
			}
			navigateToPosition(p);
		}
		
		return true;
	}
	
	/**
	 * Calibrates the homography matrix by analyzing the camera image.
	 * Image should show the homography chess board.
	 */
	public void calibrateHomography() {
		homography.calibrateHomography();
		System.out.println("Robot: calibration of homography matrix done");
	}
	
	/**
	 * starts to detect beacons in the actual image
	 */
	public void startSelfLocalization(){
		beaconBallDetection.adjustOdometryWithBeacons();
		System.out.println("Robot: Localization with beacons done");
	}
	
	/**
	 * detects beacon and balls at the actual image
	 */
	public void detectBeaconBalls(){
		beaconBallDetection.startBeaconBallDetection();
	}
	
	/**
	 * add a new Color for a ball
	 * @param color
	 */
	public void addColorBallDetection(Scalar color){
		beaconBallDetection.addBallColor(color);
		System.out.println("Robot: ball color added");
	}
	
	/**
	 * delete all ball colors
	 * @param color
	 */
	public void deleteAllColorBallDetection(){
		beaconBallDetection.clearBallColors();
		System.out.println("Robot: ball colors are deleted");
	}
}
