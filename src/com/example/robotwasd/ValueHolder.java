package com.example.robotwasd;

import org.opencv.core.Mat;
import org.opencv.core.Point;

/**
 * Common Access Point for shared objects and values
 *
 */
public class ValueHolder {
	private static Robot robot;
    public	static Mat rawPicture;
    public  static Point lowestBlobPoint;

	public static Mat getRawPicture() {
		return rawPicture;
	}

	public static void setRawPicture(Mat rawPicture) {
		ValueHolder.rawPicture = rawPicture;
	}

	public static Point getLowestBlobPoint() {
		return lowestBlobPoint;
	}

	public static void setLowestBlobPoint(Point lowestBlobPoint) {
		ValueHolder.lowestBlobPoint = lowestBlobPoint;
	}

	public static Robot getRobot() {
		return robot;
	}

	public static void setRobot(Robot robot) {
		ValueHolder.robot = robot;
	}
	
    public static boolean existslowestPoint() {
    	return !(lowestBlobPoint.x == -1 && lowestBlobPoint.y == -1);
    }
}
