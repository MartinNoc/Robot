package com.example.robotwasd;

import java.text.DecimalFormat;
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

	private static final double THRESHOLD_BALL = 20;
	private static final double THRESHOLD_BEACON = 20;

	private List<Scalar> ballColors = new LinkedList();

	private List<Beacon> detectedBeacons = new ArrayList<Beacon>();
	private List<Position> detectedBalls = new ArrayList<Position>();

	// is needed for displaying the detected balls/beacons on the image
	private List<Contour> detectedBeaconContours = new ArrayList<>();
	private List<Contour> detectedBallContours = new ArrayList<>();

	public BeaconBallDetection(Homography homography, Odometry odometry,
			Robot robot) {
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
						beacon.setImagePos(homography
								.calcPixelPosition(contourArray[j]
										.getLowestPoint()));
					} else {
						beacon.setBottomColor(contourArray[i].getColor());
						beacon.setTopColor(contourArray[j].getColor());
						/* needs homography */
						beacon.setImagePos(homography
								.calcPixelPosition(contourArray[i]
										.getLowestPoint()));
					}
					beacon.setBeaconPos();
					if(!detectedBeacons.contains(beacon))
					{
						detectedBeacons.add(beacon);
						detectedBeaconContours.add(contourArray[i]);
						detectedBeaconContours.add(contourArray[j]);
						DecimalFormat df = new DecimalFormat("#.0");
						System.out.println("Robot: Beacon found " + beacon.toString() + " with distance: "
								+ df.format(Math.hypot(beacon.getImagePos().x, beacon.getImagePos().y)) + " x:" + df.format(beacon.getImagePos().x) + " y: "
								+ df.format(beacon.getImagePos().y));
					}
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
				if (Math.abs(bea.getLowestPoint().x - cont.getLowestPoint().x) <= THRESHOLD_BALL
						&& Math.abs(bea.getLowestPoint().y
								- cont.getLowestPoint().y) <= THRESHOLD_BALL) {
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
				
				DecimalFormat df = new DecimalFormat("#.0"); 
				System.out.println("Robot: Ball found with distance: "
						+ df.format(Math.hypot(ball.x, ball.y)) + " x:" + df.format(ball.x) + " y: "
						+ df.format(ball.y));
			}
		}
		// for displaying the points on the image
		ValueHolder.setDetectedBalls(detectedBallContours);
		ValueHolder.setDetectedBeacons(detectedBeaconContours);
	}

	/**
	 * Self Localization.
	 * 
	 * Looks for two beacons to calculate robot's position.
	 * 
	 * X/Y calculation via 2 circle intersection:
	 * http://de.wikipedia.org/wiki/Schnittpunkt#Schnittpunkte_zweier_Kreise
	 * 
	 * Orientation calculation via vector dot product (robot->beacon-vector and
	 * x-axis-vector)
	 */
	public void adjustOdometryWithBeacons() {
		startBeaconBallDetection();
		while (detectedBeacons.size() < 2) {
			robot.turnLeft(30);
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			startBeaconBallDetection();
		}

		// chose 2 beacons to calculate with
		Beacon beacon0 = detectedBeacons.get(0);
		Beacon beacon1 = detectedBeacons.get(1);

		// init beacon-position variables for short variable-names
		double x0 = beacon0.getBeaconPos().x;
		double y0 = beacon0.getBeaconPos().y;

		double x1 = beacon1.getBeaconPos().x;
		double y1 = beacon1.getBeaconPos().y;

		// distances from robot to beacons
		double d0 = Math
				.hypot(beacon0.getImagePos().x, beacon0.getImagePos().y);
		double d1 = Math
				.hypot(beacon1.getImagePos().x, beacon1.getImagePos().y);

		/* X Y Calculation */
		double a = 2 * (beacon1.getBeaconPos().x - beacon0.getBeaconPos().x);
		double b = 2 * (beacon1.getBeaconPos().y - beacon0.getBeaconPos().y);
		double c = d0 * d0 - x0 * x0 - y0 * y0 - d1 * d1 + x1 * x1 + y1 * y1;
		double d = c - a * x0 - b * y0;

		double DET = Math.sqrt(d0 * d0 * (a * a + b * b) - d * d);

		Position p0 = new Position();
		Position p1 = new Position();

		// 2 possible positions (because there are 2 intersections of the
		// circles)
		p0.x = x0 + (a * d + b * DET) / (a * a + b * b);
		p0.y = y0 + (b * d - a * DET) / (a * a + b * b);

		p1.x = x0 + (a * d - b * DET) / (a * a + b * b);
		p1.y = y0 + (b * d + a * DET) / (a * a + b * b);

		/* Check which intersection is within the workspace */
		if (isPositionInWorkspace(125, p0)) {
			p0.theta = calcOrientation(p0, beacon0);
			odometry.setPosition(p0);
		} else {
			p1.theta = calcOrientation(p1, beacon0);
			odometry.setPosition(p1);
		}

	}

	/**
	 * Checks for a given position if it is within the workspace.
	 * 
	 * @param maxXY
	 * @param p
	 * @return
	 */
	private boolean isPositionInWorkspace(double maxXY, Position p) {
		if (Math.abs(p.x) > maxXY || Math.abs(p.y) > maxXY) {
			return false;
		}
		return true;
	}

	/**
	 * calculates for given beacon and given robot position the orientation of
	 * the robot.
	 * 
	 * Assumption: robot faces beacon straight forward Vector: robot -> beacon
	 * Vector: x-axis dot-product = cosine of enclosed angle between the two
	 * vectors
	 * 
	 * add egocentric orientation alpha to the dot-product to get robot's real
	 * orientation
	 * 
	 * if robot is above the beacon, the real angle is 360° (2PI) minus
	 * dot-product.
	 * 
	 * @param p
	 * @param b
	 * @return
	 */
	private double calcOrientation(Position p, Beacon b) {
		double alpha = Math.atan2(b.getImagePos().y, b.getImagePos().x);
		double xVecBeaconRobot = b.getBeaconPos().x - p.x;
		double yVecBeaconRobot = b.getBeaconPos().y - p.y;

		double angle = Math.acos(xVecBeaconRobot
				/ (Math.sqrt(Math.pow(xVecBeaconRobot, 2)
						+ Math.pow(yVecBeaconRobot, 2))));
		if (b.getBeaconPos().y < p.y)
			angle = 2 * Math.PI - angle;
		double rightAngle = angle - alpha;
		return rightAngle;
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
			if (checkBeaconColorCombination(contourA.getColor(),
					contourB.getColor())) {
				return 1;
			}
		}
		if (Math.abs(contourA.getTopmostPoint().y - contourB.getLowestPoint().y) <= THRESHOLD_BEACON) {
			// B on top of A
			if (checkBeaconColorCombination(contourB.getColor(),
					contourA.getColor())) {
				return 2;
			}
		}

		System.out.println("Beacon: Failed - Top/Bottom");
		return 0;
	}

	/**
	 * checks if the combination of beacon color is correct and exists
	 * @param topColor
	 * @param bottomColor
	 * @return true when beacon exists, otherwise false
	 */
	private boolean checkBeaconColorCombination(Color topColor,
			Color bottomColor) {
		if (topColor == Color.Blue) {
			if (bottomColor == Color.Red) {
				return true;
			} else if (bottomColor == Color.Green) {
				return true;
			} else if (bottomColor == Color.Yellow) {
				return true;
			}
		} else if (topColor == Color.Yellow) {
			if (bottomColor == Color.Green) {
				return true;
			} else if (bottomColor == Color.Red) {
				return true;
			}
		} else if (topColor == Color.Red) {
			if (bottomColor == Color.Yellow) {
				return true;
			} else if (bottomColor == Color.Green) {
				return true;
			}
		} else if (topColor == Color.Green) {
			if (bottomColor == Color.Blue) {
				return true;
			}
		}

		return false;
	}

}
