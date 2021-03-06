package com.example.robotwasd;

import java.util.List;

import org.opencv.core.Mat;

/**
 * Common Access Point for shared objects and values
 *
 */
public class ValueHolder {
	private static Robot robot;
    private	static Mat rawPicture;
    private static List<Contour> detectedBeacons;
    private static List<Contour> detectedBalls;

	public static Mat getRawPicture() {
		return rawPicture;
	}

	public static void setRawPicture(Mat rawPicture) {
		ValueHolder.rawPicture = rawPicture;
	}

	public static Robot getRobot() {
		return robot;
	}

	public static void setRobot(Robot robot) {
		ValueHolder.robot = robot;
	}
	
    public static List<Contour> getDetectedBeacons() {
		return detectedBeacons;
	}

	public static void setDetectedBeacons(List<Contour> detectedBeacons) {
		ValueHolder.detectedBeacons = detectedBeacons;
	}

	public static List<Contour> getDetectedBalls() {
		return detectedBalls;
	}

	public static void setDetectedBalls(List<Contour> detectedBalls) {
		ValueHolder.detectedBalls = detectedBalls;
	}
}
