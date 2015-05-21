package com.example.robotwasd;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;

/**
 * Implements Beacon detection.
 * 
 */
public class BeaconBallDetection {

	private Homography homography;
	private Odometry odometry;
	private Robot robot;

	private static final double THRESHOLD_BALL = 40;
	private static final double THRESHOLD_BEACON = 40;

	private List<Scalar> ballColors = new LinkedList();

	private List<Beacon> detectedBeacons = new ArrayList<Beacon>();
	private List<Position> detectedBalls = new ArrayList<Position>();

	// is needed for displaying the detected balls/beacons on the image
	private List<Contour> detectedBeaconContours = new ArrayList<>();
	private List<Contour> detectedBallContours = new ArrayList<>();

	public BeaconBallDetection(Homography homography, Odometry odometry, Robot robot) {
		this.homography = homography;
		this.odometry = odometry;
		this.robot = robot;
	}

	/**
	 * Takes the current image and analyzes it for beacons and balls. All found
	 * beacons are stored in the beacon-collection. All found balls are stored
	 * in the balls-colleciotn.
	 */
	public void startBeaconBallDetection() {
		detectedBeacons.clear();
		detectedBalls.clear();
		detectedBeaconContours.clear();
		detectedBallContours.clear();
		ColorBlobDetector blobDetector = new ColorBlobDetector();
		Mat cameraImage = ValueHolder.getRawPicture();
		List<Contour> contoursBeacons = new ArrayList<Contour>();
		List<Contour> contoursBalls = new ArrayList<Contour>();

		// set Radius
		blobDetector.setColorRadius(Color.getHsvRadius());

		// one iteration for every basic color (R, G, B, Y) => beacons
		for (Color color : Color.values()) {
			// setHSV
			blobDetector.setHsvColor(Color.getHsvColor(color));

			// process
			blobDetector.process(cameraImage);

			// getcontours
			List<MatOfPoint> imageContours = blobDetector.getContours();

			// process contours and save interesting pixel-positions
			contoursBeacons.addAll(Contour
					.processContours(imageContours, color));
		}

		Contour[] contourArray = contoursBeacons
				.toArray(new Contour[contoursBeacons.size()]);

		System.out.println("Beacon: contourArray has size "
				+ contourArray.length);
		// check for beacon by comparing pixel-positions
		for (int i = 0; i < contourArray.length; i++) {
			for (int j = i + 1; j < contourArray.length; j++) {

				int isBeacon = isBeacon(contourArray[i], contourArray[j]);
				if (isBeacon != 0) {
					// beacon found! saving into object and finally into list
					Beacon beacon = new Beacon();
					if (isBeacon == 1) {
						beacon.setTopColor(contourArray[i].getColor());
						beacon.setBottomColor(contourArray[j].getColor());
						/* needs homography */
						beacon.setImagePos(homography.calcPixelPosition(contourArray[j].getLowestPoint()));
					} else {
						beacon.setBottomColor(contourArray[i].getColor());
						beacon.setTopColor(contourArray[j].getColor());
						/* needs homography */
						beacon.setImagePos(homography.calcPixelPosition(contourArray[i].getLowestPoint()));
					}
					beacon.setBeaconPos();
					detectedBeacons.add(beacon);
					detectedBeaconContours.add(contourArray[i]);
					detectedBeaconContours.add(contourArray[j]);

					System.out.println("Beacon found: " + beacon.toString());
				}
			}
		}

		// detect the contours for the balls
		for (Scalar color : ballColors) {
			// setHSV
			blobDetector.setHsvColor(color);

			// process
			blobDetector.process(cameraImage);

			// getcontours
			List<MatOfPoint> imageContours = blobDetector.getContours();

			// process contours and save interesting pixel-positions
			contoursBalls.addAll(Contour.processContours(imageContours, color));
		}

		for (Contour cont : contoursBalls) {
			boolean isBall = true;
			for (Contour bea : detectedBeaconContours) {
				if (Math.abs(bea.getLowestPoint().x - cont.getLowestPoint().x) <= THRESHOLD_BALL && Math.abs(bea.getLowestPoint().y - cont.getLowestPoint().y) <= THRESHOLD_BALL) {
					isBall = false;
				}
			}
			if (isBall) {
				Position ball = new Position();
				ball.x = homography.calcPixelPosition(cont.getLowestPoint()).x;
				ball.y = homography.calcPixelPosition(cont.getLowestPoint()).y;
				ball.theta = 0;
				detectedBalls.add(ball);
				detectedBallContours.add(cont);
				System.out.println("Ball found");
			}
		}
		// for displaying the points on the image
		ValueHolder.setDetectedBalls(detectedBallContours);
		ValueHolder.setDetectedBeacons(detectedBeaconContours);
	}

	/**
	 * noch nicht implementiert
	 */
	public void adjustOdometryWithBeacons() {
		startBeaconBallDetection();
		while(detectedBeacons.size() < 2){
			robot.turnLeft(45);
			startBeaconBallDetection();
		}
		Position beaconImage1 = detectedBeacons.get(0).getImagePos();
		Position beaconImage2 = detectedBeacons.get(1).getImagePos();
		double angle_beacon1 = Math.atan2(beaconImage1.y, beaconImage1.x) * 180 / Math.PI;
		double distance_beacon1 = Math.hypot(beaconImage1.x, beaconImage1.y);
		double angle_beacon2 = Math.atan2(beaconImage2.y, beaconImage2.x) * 180 / Math.PI;
		double distance_beacon2 = Math.hypot(beaconImage2.x, beaconImage2.y);
		
		Position beaconWorld1 = detectedBeacons.get(0).getBeaconPos();
		Position beaconWorld2 = detectedBeacons.get(1).getBeaconPos();
		
		double distanceBetweenBeacons = Math.sqrt(Math.pow(beaconWorld1.x-beaconWorld2.x,2) + Math.pow(beaconWorld1.y-beaconWorld2.y,2));
		double angle = 0;
		if(angle_beacon1 > angle_beacon2){
			angle = Math.acos((Math.pow(distance_beacon2,2) - Math.pow(distanceBetweenBeacons,2) - Math.pow(distance_beacon1, 2))/(-2*distanceBetweenBeacons*distance_beacon1));
		}else
		{
			angle = Math.acos((Math.pow(distance_beacon1,2) - Math.pow(distanceBetweenBeacons,2) - Math.pow(distance_beacon2, 2))/(-2*distanceBetweenBeacons*distance_beacon2));
		}
	}

	/**
	 * add a new Colour for the ball detection
	 * 
	 * @param color
	 */
	public void addBallColor(Scalar color) {
		ballColors.add(color.clone());
	}

	/**
	 * delete all Colors for ball detection
	 */
	public void clearBallColors() {
		ballColors.clear();
	}

	/**
	 * Checks if two contours make up one beacon
	 * 
	 * @param contourA
	 * @param contourB
	 * @return 0 if contours don't make up a beacon. If they make up a beacon 1
	 *         if contourA is on top, 2 if contourB is on top.
	 */
	private int isBeacon(Contour contourA, Contour contourB) {
		// contours must not have same colour
		if (contourA.getColor() == contourB.getColor()) {
			return 0;
		}

		// checks if the contours are aligned vertically
		if (Math.abs(contourA.getMiddlePointX() - contourB.getMiddlePointX()) > THRESHOLD_BEACON) {
			double delta = Math.abs(contourA.getMiddlePointX()
					- contourB.getMiddlePointX());
			System.out.println("Beacon: Failed - Middle Point " + delta);
			return 0;
		}

		// checks if one contour is stacked right above the other
		if (Math.abs(contourA.getLowestPoint().y - contourB.getTopmostPoint().y) <= THRESHOLD_BEACON) {
			// A on top of B
			return 1;
		}
		if (Math.abs(contourA.getTopmostPoint().y - contourB.getLowestPoint().y) <= THRESHOLD_BEACON) {
			// B on top of A
			return 2;
		}

		System.out.println("Beacon: Failed - Top/Bottom");
		return 0;
	}

}
