package com.example.robotwasd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;

/**
 * Implements Beacon detection.
 *
 */
public class BeaconDetection {

	private Collection<Beacon> detectedBeacons = new ArrayList<Beacon>();
	private Collection<Position> detectedBalls = new ArrayList<Position>();

	/**
	 * Takes the current image and analyzes it for beacons.
	 * All found beacons are stored in the beacon-collection.
	 */
	public void startBeaconDetection() {
		detectedBeacons.clear();
		detectedBalls.clear();
		ColorBlobDetector blobDetector = ValueHolder.getBlobDetector();
		Mat cameraImage = ValueHolder.getRawPicture();
		List<Contour> contours = new ArrayList<Contour>();
		
		// set Radius
		blobDetector.setColorRadius(Color.getHsvRadius());
		
		// one iteration for every basic color (R, G, B, Y)
		for (Color color : Color.values()) {
			// setHSV	
			blobDetector.setHsvColor(Color.getHsvColor(color));
			
			// process
			blobDetector.process(cameraImage);
			
			// getcontours
			List<MatOfPoint> imageContours = blobDetector.getContours();
			
			// process contours and save interesting pixel-positions
			contours.addAll(Contour.processContours(imageContours, color));
		}
		
		Contour[] contourArray = contours.toArray(new Contour[contours.size()]);
		
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
						//beacon.setImagePos(ValueHolder.getHomography().calcPixelPosition(contourArray[j].getLowestPoint()));
					}
					else {
						beacon.setBottomColor(contourArray[i].getColor());
						beacon.setTopColor(contourArray[j].getColor());
						/* needs homography */
						//beacon.setImagePos(ValueHolder.getHomography().calcPixelPosition(contourArray[i].getLowestPoint()));
					}
					beacon.setBeaconPos();
					detectedBeacons.add(beacon);
					
					System.out.println("Beacon found: " + beacon.toString());
				}
				
			}
		}
		
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
