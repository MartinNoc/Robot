package com.example.robotwasd;

import java.util.ArrayList;
import java.util.Collection;
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
	
	private List<Scalar> ballColors = new LinkedList();
	
	private Collection<Beacon> detectedBeacons = new ArrayList<Beacon>();
	private Collection<Position> detectedBalls = new ArrayList<Position>();

	public BeaconBallDetection(Homography homography, Odometry odometry){
		this.homography = homography;
		this.odometry = odometry;
	}
	
	/**
	 * Takes the current image and analyzes it for beacons.
	 * All found beacons are stored in the beacon-collection.
	 */
	public void startBeaconBallDetection() {
		double thresholdBall = 20;
		detectedBeacons.clear();
		detectedBalls.clear();
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
			contoursBeacons.addAll(Contour.processContours(imageContours, color));
		}
		
		Contour[] contourArray = contoursBeacons.toArray(new Contour[contoursBeacons.size()]);
		
		System.out.println("Beacon: contourArray has size " + contourArray.length);
		// check for beacon by comparing pixel-positions
		for (int i = 0; i < contourArray.length; i++) {
			for (int j = i + 1; j < contourArray.length; j++) {
				
				int isBeacon = isBeacon(contourArray[i],contourArray[j]);
				if (isBeacon != 0) {
					// beacon found! saving into object and finally into list
					Beacon beacon = new Beacon();
					if (isBeacon == 1) {
						beacon.setTopColor(contourArray[i].getColor());
						beacon.setBottomColor(contourArray[j].getColor());
						/* needs homography */
						//beacon.setImagePos(homography.calcPixelPosition(contourArray[j].getLowestPoint()));
					}
					else {
						beacon.setBottomColor(contourArray[i].getColor());
						beacon.setTopColor(contourArray[j].getColor());
						/* needs homography */
						//beacon.setImagePos(homography.calcPixelPosition(contourArray[i].getLowestPoint()));
					}
					beacon.setBeaconPos();
					detectedBeacons.add(beacon);
					
					System.out.println("Beacon found: " + beacon.toString());
				}
			}
		}	
		
		
		//detect the contours for the balls
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
		
		for(Contour cont : contoursBalls){
			for(Beacon bea : detectedBeacons){
				if(Math.abs(bea.getImagePos().x - cont.getLowestPoint().x) <= thresholdBall){
					//muss man noch machen
				}
			}
		}
	}
	
	/**
	 * noch nicht implementiert
	 */
	public void adjustOdometryWithBeacons(){
		//startBeaconBallDetection();
		//weiteres
		//odometry.adjustOdometry();
	}
	
	/**
	 * add a new Colour for the ball detection
	 * @param color
	 */
	public void addBallColor(Scalar color){
		ballColors.add(color.clone());
	}
	
	/**
	 * delete all Colors for ball detection
	 */
	public void deleteBallColors() {
		ballColors.clear();
	}
	
	/**
	 * Checks if two contours make up one beacon
	 * 
	 * @param contourA
	 * @param contourB
	 * @return 0 if contours don't make up a beacon. If they make up a beacon
	 * 1 if contourA is on top, 2 if contourB is on top.
	 */
	private int isBeacon(Contour contourA, Contour contourB) {
		// pixels
		final double Threshold = 60.0;
		
		// contours must not have same colour
		if (contourA.getColor() == contourB.getColor()) {
			return 0;
		}
		
		// checks if the contours are aligned vertically
		if ( Math.abs(contourA.getMiddlePointX() - contourB.getMiddlePointX()) > Threshold) {
			double delta = Math.abs(contourA.getMiddlePointX() - contourB.getMiddlePointX());
			System.out.println("Beacon: Failed - Middle Point " + delta);
			return 0;
		}
		
		// checks if one contour is stacked right above the other
		if ( Math.abs(contourA.getLowestPoint().y - contourB.getTopmostPoint().y) <= Threshold) {
			// A on top of B
			return 1;
		}
		if ( Math.abs(contourA.getTopmostPoint().y - contourB.getLowestPoint().y) <= Threshold ) {
			// B on top of A
			return 2;
		}
		
		System.out.println("Beacon: Failed - Top/Bottom");
		return 0;
	}
	
}
