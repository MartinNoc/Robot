package com.example.robotwasd;

import org.opencv.core.Scalar;

/**
 * Color enumeration class.
 * 
 * 0 Red,
 * 1 Blue,
 * 2 Green,
 * 3 Yellow.
 * 
 */
public enum Color {
	Red, Blue, Green, Yellow;
	
	private static Scalar hsvRadius = new Scalar(8,50,50);//(20, 75, 75);
	private static Scalar red = new Scalar(12, 190, 170);
	private static Scalar blue = new Scalar(147,220, 130);
	private static Scalar green = new Scalar(93,180,100);
	private static Scalar yellow = new Scalar(40, 210, 180);
	
	public static void setRed(Scalar red) {
		Color.red = red;
	}

	public static void setBlue(Scalar blue) {
		Color.blue = blue;
	}

	public static void setGreen(Scalar green) {
		Color.green = green;
	}

	public static void setYellow(Scalar yellow) {
		Color.yellow = yellow;
	}
	
	public static Scalar getHsvColor(Color c) {
		switch (c) {
			case Red: return red;
			case Blue: return blue;
			case Green: return green;
			case Yellow: return yellow;
		}
		return new Scalar(0,0,0);
	}
	
	/**
	 * get the HSV value for the color
	 * @return HSV value
	 */
	public static Scalar getHsvRadius() {
		return hsvRadius;
	}
	
	public static void setHsvRadius(Scalar hsvRadius) {
		Color.hsvRadius = hsvRadius;
	}
	
	/**
	 * Detects if passed color-vector [hsv] is one of the four basic colors
	 * red, blue, green or yellow.
	 * 
	 * @param hsvValue color-vector to be examined
	 * @return Color.Red if hsvValue appears to be red. Color.Blue if hsvValue appears to be blue.
	 * Color.Green if hsvValue appears to be green, Color.Yellow if hsvValue appears to be yellow.
	 * Color.Unknown if the color can't be mapped to one of the above mentioned.
	 */
	@Deprecated
	public static Color detectColor(Scalar hsvValue){
		final double hsvThreashold = calcEuclidianDistance(hsvRadius, new Scalar(0,0,0));
		
		if (calcEuclidianDistance(hsvValue, getHsvColor(Red)) < hsvThreashold) {
			return Color.Red;
		}
		if (calcEuclidianDistance(hsvValue, getHsvColor(Blue)) < hsvThreashold) {
			return Color.Blue;
		}
		if (calcEuclidianDistance(hsvValue, getHsvColor(Green)) < hsvThreashold) {
			return Color.Green;
		}
		if (calcEuclidianDistance(hsvValue, getHsvColor(Yellow)) < hsvThreashold) {
			return Color.Yellow;
		}
		// Color.NoColor, Color.Unknown -> but then problems if iterating through all colors
		return Color.Blue;
	}
	
	/**
	 * Calculates the euclidian distance between two color-vectors c = (h,s,v).
	 * d = sqrt( (h1-h2)^2 + (s1-s2)^2 + (v1-v2)^2 ) 
	 * 
	 * @param color1 first color-vector
	 * @param color2 second color-vector
	 * @return euclidian distance between vectors color1 and color2 [double]
	 */
	private static double calcEuclidianDistance(Scalar color1, Scalar color2) {
		return Math.sqrt( Math.pow(color1.val[0]-color2.val[0],2) +
				Math.pow(color1.val[1]-color2.val[1],2) + Math.pow(color1.val[2]-color2.val[2],2) );
	}
}
