package com.example.robotwasd;

import java.util.Iterator;
import java.util.List;
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
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
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
 * coordinate the camera and color-blob detection
 * @author Witsch Daniel
 *
 */
public class ColorBlobDetection extends ActionBarActivity implements OnTouchListener, CvCameraViewListener2 {
    private static final String  TAG              = "OCVSample::Activity";

    private boolean              mIsColorSelected = false;
    private Mat                  mRgba;
    private Mat					 setPicture;
    private Scalar               mBlobColorRgba;
    private Scalar               mBlobColorHsv;
    private ColorBlobDetector    mDetector;
    private Mat                  mSpectrum;
    private Size                 SPECTRUM_SIZE;
    private Scalar               CONTOUR_COLOR;
    
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
				mOpenCvCameraView
						.setOnTouchListener(ColorBlobDetection.this);
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
        CONTOUR_COLOR = new Scalar(255,0,0,255);
    }

    public void onCameraViewStopped() {
        mRgba.release();
    }

    public boolean onTouch(View v, MotionEvent event) {
        int cols = mRgba.cols();
        int rows = mRgba.rows();

        int xOffset = (mOpenCvCameraView.getWidth() - cols) / 2;
        int yOffset = (mOpenCvCameraView.getHeight() - rows) / 2;

        int x = (int)event.getX() - xOffset;
        int y = (int)event.getY() - yOffset;

        Log.i(TAG, "Touch image coordinates: (" + x + ", " + y + ")");

        if ((x < 0) || (y < 0) || (x > cols) || (y > rows)) return false;

        Rect touchedRect = new Rect();

        touchedRect.x = (x>4) ? x-4 : 0;
        touchedRect.y = (y>4) ? y-4 : 0;

        touchedRect.width = (x+4 < cols) ? x + 4 - touchedRect.x : cols - touchedRect.x;
        touchedRect.height = (y+4 < rows) ? y + 4 - touchedRect.y : rows - touchedRect.y;

        Mat touchedRegionRgba = mRgba.submat(touchedRect);

        Mat touchedRegionHsv = new Mat();
        Imgproc.cvtColor(touchedRegionRgba, touchedRegionHsv, Imgproc.COLOR_RGB2HSV_FULL);

        // Calculate average color of touched region
        mBlobColorHsv = Core.sumElems(touchedRegionHsv);
        int pointCount = touchedRect.width*touchedRect.height;
        for (int i = 0; i < mBlobColorHsv.val.length; i++)
            mBlobColorHsv.val[i] /= pointCount;

        mBlobColorRgba = converScalarHsv2Rgba(mBlobColorHsv);
        
        String rgb = "RGB " + String.format(Locale.US,"(%1$.2f,%2$.2f,%3$.2f)",
        		mBlobColorRgba.val[0],mBlobColorRgba.val[1],mBlobColorRgba.val[2]);
        
        String hsv = "HSV " + String.format(Locale.US,"(%1$.2f,%2$.2f,%3$.2f)",
        		mBlobColorHsv.val[0],mBlobColorHsv.val[1],mBlobColorHsv.val[2]);

        System.out.println("Color: " + hsv + " / " + rgb);
        
        mDetector.setHsvColor(mBlobColorHsv);

        Imgproc.resize(mDetector.getSpectrum(), mSpectrum, SPECTRUM_SIZE);

        mIsColorSelected = true;

        touchedRegionRgba.release();
        touchedRegionHsv.release();
        
        ValueHolder.getRobot().addColorBallDetection(mBlobColorHsv);

        return false; // don't need subsequent touch events
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
    	System.out.println("Robot: new frame");
        mRgba = inputFrame.rgba();
        ValueHolder.setRawPicture(inputFrame.rgba());
       
        if (mIsColorSelected) {
            mDetector.process(mRgba);
            List<MatOfPoint> contours = mDetector.getContours();
            
            
            if(contours.size() > 0){
            	Point lowest = new Point(0,0);
            	
            	for(MatOfPoint matofpoint : contours){
		            List<Point> contList = matofpoint.toList();
		            Iterator<Point> iter = contList.iterator();
		            while(iter.hasNext()){
		            	Point p = iter.next();
		            	if(lowest.y < p.y)
		            		lowest = p;
		            }
            	}
	           	
	           	ValueHolder.setLowestBlobPoint(lowest);
            }
            else {
            	ValueHolder.setLowestBlobPoint(-1, -1);
            }
            
            Core.circle(mRgba, ValueHolder.getLowestBlobPoint(), 15, new Scalar(200.0), -1);
            Log.e(TAG, "Contours count: " + contours.size());
            Imgproc.drawContours(mRgba, contours, -1, CONTOUR_COLOR);

            Mat colorLabel = mRgba.submat(4, 68, 4, 68);
            colorLabel.setTo(mBlobColorRgba);
        }

        return mRgba;
    }

    private Scalar converScalarHsv2Rgba(Scalar hsvColor) {
        Mat pointMatRgba = new Mat();
        Mat pointMatHsv = new Mat(1, 1, CvType.CV_8UC3, hsvColor);
        Imgproc.cvtColor(pointMatHsv, pointMatRgba, Imgproc.COLOR_HSV2RGB_FULL, 4);

        return new Scalar(pointMatRgba.get(0, 0));
    }
        
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		// getMenuInflater().inflate(R.menu.main, menu);
		menu.add("Buttons");
		menu.add("Collect");
		menu.add("Homography");
		menu.add("BeaconDetection");
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
		case "Homography":
			ValueHolder.getRobot().calibrateHomography();
			break;
		case "BeaconDetection":
			ValueHolder.getRobot().startBeaconBallDetection();
			break;
		}
		return false;
	}
}
