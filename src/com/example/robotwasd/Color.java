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
	
	// initial : Scalar(10, 20, 20);
	private static Scalar hsvRadius = new Scalar(20, 50, 50);
	
	public static Scalar getHsvColor(Color c) {
		switch (c) {
			case Red: return new Scalar(246,180, 178);
			case Blue: return new Scalar(158,234, 200);
			case Green: return new Scalar(120,214,105);
			case Yellow: return new Scalar(39, 70, 207);
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
