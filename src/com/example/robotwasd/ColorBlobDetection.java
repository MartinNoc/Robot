package com.example.robotwasd;

import java.util.Locale;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;

/**
 * camera activity with touch function
 * 
 * @author Witsch Daniel
 * 
 */
public class ColorBlobDetection extends ActionBarActivity implements
		OnTouchListener, CvCameraViewListener2 {
	private static final String TAG = "OCVSample::Activity";

	private boolean mIsColorSelected = false;
	private Mat mRgba;
	private Scalar mBlobColorRgba;
	private Scalar mBlobColorHsv;
	private ColorBlobDetector mDetector;
	private Mat mSpectrum;
	private Size SPECTRUM_SIZE;
	private Scalar CONTOUR_COLOR;

	// variable for deciding what should happen if the user touch on the screen
	// 0 => add a ballColor, 1 => change Red Beacon Color, 2 => Blue Beacon
	// Color
	// 3 => Green Beacon Color, 4 => Yellow Beacon Color
	private int touchFunction = -1;

	public CameraBridgeViewBase mOpenCvCameraView;

	/**
	 * load the OpenCV camera
	 */
	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS: {
				mOpenCvCameraView.enableView();
				mOpenCvCameraView.setOnTouchListener(ColorBlobDetection.this);
			}
				break;
			default: {
				super.onManagerConnected(status);
			}
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.color_blob_detection_surface_view);

		// initialize the ColorBlobDetection and Camera
		mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.color_blob_detection_activity_surface_view);
		mOpenCvCameraView.setCvCameraViewListener(this);
	}

	@Override
	public void onResume() {
		super.onResume();
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this,
				mLoaderCallback);
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
	}

	public void onCameraViewStarted(int width, int height) {
		mRgba = new Mat(height, width, CvType.CV_8UC4);
		mDetector = new ColorBlobDetector();
		mSpectrum = new Mat();
		mBlobColorRgba = new Scalar(255);
		mBlobColorHsv = new Scalar(255);
		SPECTRUM_SIZE = new Size(200, 64);
		CONTOUR_COLOR = new Scalar(255, 0, 0, 255);
	}

	public void onCameraViewStopped() {
		mRgba.release();
	}

	/**
	 * callback function, is called when you touch on the camera picture then
	 * add the touched color to the ball detection colors
	 */
	public boolean onTouch(View v, MotionEvent event) {
		int cols = mRgba.cols();
		int rows = mRgba.rows();

		int xOffset = (mOpenCvCameraView.getWidth() - cols) / 2;
		int yOffset = (mOpenCvCameraView.getHeight() - rows) / 2;

		int x = (int) event.getX() - xOffset;
		int y = (int) event.getY() - yOffset;

		Log.i(TAG, "Touch image coordinates: (" + x + ", " + y + ")");

		if ((x < 0) || (y < 0) || (x > cols) || (y > rows))
			return false;

		Rect touchedRect = new Rect();

		touchedRect.x = (x > 4) ? x - 4 : 0;
		touchedRect.y = (y > 4) ? y - 4 : 0;

		touchedRect.width = (x + 4 < cols) ? x + 4 - touchedRect.x : cols
				- touchedRect.x;
		touchedRect.height = (y + 4 < rows) ? y + 4 - touchedRect.y : rows
				- touchedRect.y;

		Mat touchedRegionRgba = mRgba.submat(touchedRect);

		Mat touchedRegionHsv = new Mat();
		Imgproc.cvtColor(touchedRegionRgba, touchedRegionHsv,
				Imgproc.COLOR_RGB2HSV_FULL);

		// Calculate average color of touched region
		mBlobColorHsv = Core.sumElems(touchedRegionHsv);
		int pointCount = touchedRect.width * touchedRect.height;
		for (int i = 0; i < mBlobColorHsv.val.length; i++)
			mBlobColorHsv.val[i] /= pointCount;

		mBlobColorRgba = converScalarHsv2Rgba(mBlobColorHsv);

		String rgb = "RGB "
				+ String.format(Locale.US, "(%1$.2f,%2$.2f,%3$.2f)",
						mBlobColorRgba.val[0], mBlobColorRgba.val[1],
						mBlobColorRgba.val[2]);

		String hsv = "HSV "
				+ String.format(Locale.US, "(%1$.2f,%2$.2f,%3$.2f)",
						mBlobColorHsv.val[0], mBlobColorHsv.val[1],
						mBlobColorHsv.val[2]);

		System.out.println("Color: " + hsv + " / " + rgb);

		mDetector.setHsvColor(mBlobColorHsv);

		Imgproc.resize(mDetector.getSpectrum(), mSpectrum, SPECTRUM_SIZE);

		mIsColorSelected = true;

		touchedRegionRgba.release();
		touchedRegionHsv.release();

		switch (touchFunction) {
		case 0:
			ValueHolder.getRobot().addColorBallDetection(mBlobColorHsv.clone());
			touchFunction = -1;
			break;
		case 1:
			Color.setRed(mBlobColorHsv.clone());
			touchFunction++;
			break;
		case 2:
			Color.setBlue(mBlobColorHsv.clone());
			touchFunction++;
			break;
		case 3:
			Color.setGreen(mBlobColorHsv.clone());
			touchFunction++;
			break;
		case 4:
			Color.setYellow(mBlobColorHsv.clone());
			touchFunction = -1;
			System.out.println("Robot: calibration beacon colors done");
			break;
		}

		return false; // don't need subsequent touch events
	}

	/**
	 * callback function, is called when a new camera frame arrived
	 */
	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		mRgba = inputFrame.rgba();
		ValueHolder.setRawPicture(mRgba);

		
		/*if (ValueHolder.getDetectedBalls() != null
				&& ValueHolder.getDetectedBeacons() != null) {
			for (Contour ball : ValueHolder.getDetectedBalls()) {
				Core.circle(mRgba, ball.getLowestPoint(), 5, new Scalar(200.0),
						-1);
				Core.putText(mRgba, "ball", ball.getLowestPoint(),
						Core.FONT_HERSHEY_SIMPLEX, 1, new Scalar(200.0));
			}

			for (Contour beacon : ValueHolder.getDetectedBeacons()) {
				Core.circle(mRgba, beacon.getLowestPoint(), 5,
						new Scalar(100.0), -1);
				Core.putText(mRgba, "beacon", beacon.getLowestPoint(),
						Core.FONT_HERSHEY_SIMPLEX, 1, new Scalar(100.0));
			}
		}*/
		
		// Imgproc.drawContours(mRgba, contours, -1, CONTOUR_COLOR);
		 
		return mRgba;
	}

	/**
	 * convert a HSV color to a RGB color
	 * 
	 * @param hsvColor
	 * @return RGB color
	 */
	private Scalar converScalarHsv2Rgba(Scalar hsvColor) {
		Mat pointMatRgba = new Mat();
		Mat pointMatHsv = new Mat(1, 1, CvType.CV_8UC3, hsvColor);
		Imgproc.cvtColor(pointMatHsv, pointMatRgba, Imgproc.COLOR_HSV2RGB_FULL,
				4);

		return new Scalar(pointMatRgba.get(0, 0));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		// getMenuInflater().inflate(R.menu.main, menu);
		menu.add("Buttons");
		menu.add("Collect");
		menu.add("Collect with Obstacle Avoidance");
		menu.add("SelfLocalization");
		menu.add("Detect Beacon and Balls");
		menu.add("Homography");
		menu.add("Calibrate Beacon Color (Red, Blue, Green, Yellow)");
		menu.add("Add ball color");
		menu.add("Delete all ball colors");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		CharSequence itemSelected = item.getTitle();
		switch (itemSelected.toString()) {
		case "Buttons":
			finish();
			break;
		case "Collect":
			RobotThread thread = new RobotThread(ValueHolder.getRobot());
			thread.start();
			break;
		case "Collect with Obstacle Avoidance":
			RobotThreadOA threadOA = new RobotThreadOA(ValueHolder.getRobot());
			threadOA.start();
			break;
		case "Homography":
			ValueHolder.getRobot().calibrateHomography();
			break;
		case "Calibrate Beacon Color (Red, Blue, Green, Yellow)":
			touchFunction = 1;
			break;
		case "Add ball color":
			touchFunction = 0;
			break;
		case "Delete all ball colors":
			ValueHolder.getRobot().deleteAllColorBallDetection();
			break;
		case "SelfLocalization":
			SelfLocalizeThread threadLocalize = new SelfLocalizeThread(ValueHolder.getRobot());
			threadLocalize.start();
			break;
		case "Detect Beacon and Balls":
			ValueHolder.getRobot().detectBeaconBalls();
			break;
		}
		return false;
	}
}
