package com.example.robotwasd;

import java.util.LinkedList;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

/**
 * Homography class: Mapping from image pixel coordinates to egocentric ground plane coordinates.
 * Provides methods for initializing the homography matrix and calculating ground plane
 * coordinates for given pixel coordinates.
 *
 */
public class Homography {

	private Mat homographyMatrix;

	public Homography() {
	}

	public Mat calibrateHomography() {
		homographyMatrix = getHomographyMatrix(ValueHolder.getRawPicture());
		while (homographyMatrix.empty()) {
			homographyMatrix = getHomographyMatrix(ValueHolder.getRawPicture());
		}
		return homographyMatrix;
	}

	/**
	 * calculates the homography matrix from a picture with a chessboard on it
	 * @param mRgba
	 * @return
	 */
	private Mat getHomographyMatrix(Mat mRgba) {
		final float Y_OFFS =  510.0f;
		final float X_OFFS =  -48.0f;
		
		// number of inner corners in the used chessboard pattern
		final Size mPatternSize = new Size(6, 9);
		float x = X_OFFS; // coordinates of first detected inner corner on chessboard
		float y = Y_OFFS;
		float delta = 12.0f; // size of a single square edge in chessboard
		LinkedList<Point> PointList = new LinkedList<Point>();

		// Define real-world coordinates for given chessboard pattern:
		for (int i = 0; i < mPatternSize.height; i++) {
			y = Y_OFFS;
			for (int j = 0; j < mPatternSize.width; j++) {
				PointList.addLast(new Point(x, y));
				y += delta;
			}
			x += delta;
		}
		MatOfPoint2f RealWorldC = new MatOfPoint2f();
		RealWorldC.fromList(PointList);

		// Detect inner corners of chessboard pattern from image:
		Mat gray = new Mat();
		Imgproc.cvtColor(mRgba, gray, Imgproc.COLOR_RGBA2GRAY); // convert image to grayscale
		MatOfPoint2f mCorners = new MatOfPoint2f();
		boolean mPatternWasFound = Calib3d.findChessboardCorners(gray,
				mPatternSize, mCorners);

		// Calculate homography:
		if (mPatternWasFound) {
			// Calib3d.drawChessboardCorners(mRgba, mPatternSize, mCorners, mPatternWasFound); //for visualization
			return Calib3d.findHomography(mCorners, RealWorldC);
		}
		else {
			return new Mat();
		}
	}

	/**
	 * calculates the position of a pixel in a egocentric position of the robot
	 * @param point pixel position on image
	 * @return egocentric position
	 */
	public Position calcPixelPosition(Point point) {
		Position goal = new Position();
		
		Mat src = new Mat(1, 1, CvType.CV_32FC2);
		Mat dest = new Mat(1, 1, CvType.CV_32FC2);
		src.put(0, 0, new double[] { point.x, point.y }); // point is a point in image coordinates
		Core.perspectiveTransform(src, dest, homographyMatrix);
		Point dest_point = new Point(dest.get(0, 0)[0], dest.get(0, 0)[1]);
		
		// swapping of x and y, dividing by 10 and negate y value in order to
		// transform from image coordinates to egocentric coordinates
		goal.setPosition(dest_point.y/10, -dest_point.x/10, 0);

		return goal;
	}
}
