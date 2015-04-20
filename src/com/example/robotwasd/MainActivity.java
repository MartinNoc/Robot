package com.example.robotwasd;

import java.util.Iterator;
import java.util.List;

import jp.ksksue.driver.serial.FTDriver;

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

import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.EditText;
import android.widget.TextView;

import com.example.OpenCV.ColorBlobDetector;

/**
 * App do controll Robot
 * @author Witsch Daniel, Nocker Martin, Schiborr Mark
 *
 */
public class MainActivity extends ActionBarActivity implements OnTouchListener, CvCameraViewListener2 {
	private Robot robot;
	private TextView textLog;
	
	private EditText editTextX;
	private EditText editTextY;
	private EditText editTextTheta;
	
    private static final String  TAG              = "OCVSample::Activity";

    private boolean              mIsColorSelected = false;
    private Mat                  mRgba;
    private Scalar               mBlobColorRgba;
    private Scalar               mBlobColorHsv;
    private ColorBlobDetector    mDetector;
    private Mat                  mSpectrum;
    private Size                 SPECTRUM_SIZE;
    private Scalar               CONTOUR_COLOR;

    private CameraBridgeViewBase mOpenCvCameraView;

    private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                    mOpenCvCameraView.setOnTouchListener(MainActivity.this);
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_main);
		
		textLog = (TextView) findViewById(R.id.textView1);
		editTextX = (EditText) findViewById(R.id.edit_inputX);
		editTextY = (EditText) findViewById(R.id.edit_inputY);
		editTextTheta = (EditText) findViewById(R.id.edit_inputTheta);
		
		FTDriver driver = new FTDriver((UsbManager) getSystemService(USB_SERVICE));
		
		robot = new Robot(driver, textLog);	
		
		robot.initialize();
		
		// Put initial default values into navigation goal-position input boxes
		editTextX.setText("85");
		editTextY.setText("0");
		editTextTheta.setText("45");

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.color_blob_detection_activity_surface_view);
        mOpenCvCameraView.setCvCameraViewListener(this);
	}
	
    @Override
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
    }
    
    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }
    
	@Override
	protected void onDestroy(){
		robot.disconnect();
		super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
	}
	
	
	
	/** Button Functions */
		
	public void minusButton_onClick(View v) {
		robot.lowerBar();
	}
	
	public void plusButton_onClick(View v) {
		robot.riseBar();
	}
	
	// fixed position for bar (low)
	public void downButton_onClick(View v) {
		robot.lowPositionBar();
	}
	
	// fixed position for bar (high)
	public void upButton_onClick(View v) {
		robot.upPositionBar();
	}
		
	public void connectButton_onClick(View v) {
		robot.connect();
	}
	
	public void disconnectButton_onClick(View v) {
		robot.disconnect();
	}
	
	public void CalibrateButton_onClick(View v) {
		robot.calibrateSensor();
	}
	
	public void odometryButton_onClick(View v){
		robot.printOdometryData();
	}
	
	public void initOdometryButton_onClick(View v){
		robot.initOdometryPosition();
	}

	public void navigationButton_onClick(View v){
		double x = Double.parseDouble(editTextX.getText().toString());
		double y = Double.parseDouble(editTextY.getText().toString());
		double theta = Double.parseDouble(editTextTheta.getText().toString());
		Position p = new Position(x,y,theta);
		robot.navigateToPosition(p);
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mDetector = new ColorBlobDetector();
        mSpectrum = new Mat();
        mBlobColorRgba = new Scalar(255);
        mBlobColorHsv = new Scalar(255);
        SPECTRUM_SIZE = new Size(200, 64);
        CONTOUR_COLOR = new Scalar(255,0,0,255);
		
	}

	@Override
	public void onCameraViewStopped() {
		mRgba.release();
		
	}

	@Override
	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		mRgba = inputFrame.rgba();
		Mat rotMat = Imgproc.getRotationMatrix2D(new Point(mRgba.cols()/2,mRgba.rows()/2), -90.0, 1);
		Imgproc.warpAffine(mRgba, mRgba, rotMat, mRgba.size());

        if (mIsColorSelected) {
            mDetector.process(mRgba);
            List<MatOfPoint> contours = mDetector.getContours();
            Point lowest = new Point(0,0);
            if(contours.size() > 0){
            	for(MatOfPoint matofpoint : contours){
		            List<Point> contList = matofpoint.toList();
		            Iterator<Point> iter = contList.iterator();
		            while(iter.hasNext()){
		            	Point x = iter.next();
		            	if(lowest.x < x.x)
		            		lowest = x;
		            }
            	}
	           	System.out.println("ROBOT: " + lowest.x);
            }
            Core.circle(mRgba, lowest, 20, new Scalar(200.0), -1);
            Log.e(TAG, "Contours count: " + contours.size());
            Imgproc.drawContours(mRgba, contours, -1, CONTOUR_COLOR);

            Mat colorLabel = mRgba.submat(4, 68, 4, 68);
            colorLabel.setTo(mBlobColorRgba);

            Mat spectrumLabel = mRgba.submat(4, 4 + mSpectrum.rows(), 70, 70 + mSpectrum.cols());
            mSpectrum.copyTo(spectrumLabel);
        }

        return mRgba;
	}

	@Override
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

        Log.i(TAG, "Touched rgba color: (" + mBlobColorRgba.val[0] + ", " + mBlobColorRgba.val[1] +
                ", " + mBlobColorRgba.val[2] + ", " + mBlobColorRgba.val[3] + ")");

        mDetector.setHsvColor(mBlobColorHsv);

        Imgproc.resize(mDetector.getSpectrum(), mSpectrum, SPECTRUM_SIZE);

        mIsColorSelected = true;

        touchedRegionRgba.release();
        touchedRegionHsv.release();

        return false; // don't need subsequent touch events
    }
	
	private Scalar converScalarHsv2Rgba(Scalar hsvColor) {
        Mat pointMatRgba = new Mat();
        Mat pointMatHsv = new Mat(1, 1, CvType.CV_8UC3, hsvColor);
        Imgproc.cvtColor(pointMatHsv, pointMatRgba, Imgproc.COLOR_HSV2RGB_FULL, 4);

        return new Scalar(pointMatRgba.get(0, 0));
    }
}
