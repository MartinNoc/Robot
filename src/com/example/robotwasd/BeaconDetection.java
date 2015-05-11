package com.example.robotwasd;

import org.opencv.core.Scalar;

/**
 * BeaconDetection.
 * Implements Color encoding, Color recognition and Beacon detection, which means
 * detection of beacon in space, and updating odometry based on the known beacon positions.
 *
 */
public class BeaconDetection {
	
	/*	 Blue		Yellow		Red
	 *    Red _ _ _ _Green_ _ _ Yellow
	 * 		 |				   |
	 * 		 |				   |
	 * 		 |		 250	   |
	 * 	Green|_ _ _ _ x _ _ _ _|Blue
	 *   Blue|		 250	   |Green
	 * 		 |	 world-space   |
	 * 		 |		  	 	   |
	 * 		 |_ _ _ _ _ _ _ _ _|
	 * Yellow		 Blue		Red
	 *    Red		Yellow		Green
	 */	
	
	private Position BlueRed 	 = new Position(-125, 125,0);
	private Position YellowGreen = new Position(   0, 125,0);
	private Position RedYellow 	 = new Position( 125, 125,0);
	private Position BlueGreen 	 = new Position( 125,   0,0);
	private Position RedGreen 	 = new Position( 125,-125,0);
	private Position BlueYellow  = new Position(   0,-125,0);
	private Position YellowRed 	 = new Position(-125,-125,0);
	private Position GreenBlue	 = new Position(-125,   0,0);
	
	private Scalar hsvRED = new Scalar(246,180, 178);
	private Scalar hsvBLUE = new Scalar(156,234, 200);
	private Scalar hsvGREEN = new Scalar(125,214,105);
	private Scalar hsvYELLOW = new Scalar(38, 70, 207);
	
	private Scalar hsvRadius = new Scalar(10, 20, 20);
	
	private enum Colors {
		Red, Blue, Green, Yellow, NoColor
	}

	public Colors detectColor(Scalar hsvValue){
		final double hsvThreashold = calcEuclidianDistance(hsvRadius, new Scalar(0,0,0));
		
		if (calcEuclidianDistance(hsvValue, hsvRED) < hsvThreashold) {
			return Colors.Red;
		}
		if (calcEuclidianDistance(hsvValue, hsvBLUE) < hsvThreashold) {
			return Colors.Blue;
		}
		if (calcEuclidianDistance(hsvValue, hsvGREEN) < hsvThreashold) {
			return Colors.Green;
		}
		if (calcEuclidianDistance(hsvValue, hsvYELLOW) < hsvThreashold) {
			return Colors.Yellow;
		}
		return Colors.NoColor;
	}
	
	public double calcEuclidianDistance(Scalar color1, Scalar color2) {
		return Math.sqrt( Math.pow(color1.val[0]-color2.val[0],2) +
				Math.pow(color1.val[1]-color2.val[1],2) + Math.pow(color1.val[2]-color2.val[2],2) );
	}
	
}
