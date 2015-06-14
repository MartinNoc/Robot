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
	
	private static Scalar hsvRadiusBeacon = new Scalar(10,70,90);//(25,50,50);
	private static Scalar hsvRadiusBall = new Scalar(20,50,50);
	private static Scalar red = new Scalar(0,190,190);//(8, 200, 170);
	private static Scalar blue = new Scalar(146,225,120);//(142,234, 96);
	private static Scalar green = new Scalar(95,145,110);//(90,185,69);
	private static Scalar yellow = new Scalar(37,160,180);//(39, 170, 198);
	
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
	public static Scalar getHsvRadiusBeacon() {
		return hsvRadiusBeacon;
	}
	
	public static void setHsvRadiusBeacon(Scalar hsvRadius) {
		Color.hsvRadiusBeacon = hsvRadius;
	}
	
	public static Scalar getHsvRadiusBall() {
		return hsvRadiusBall;
	}
	
	public static void setHsvRadiusBall(Scalar hsvRadius) {
		Color.hsvRadiusBall = hsvRadius;
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
		final double hsvThreashold = calcEuclidianDistance(hsvRadiusBeacon, new Scalar(0,0,0));
		
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
