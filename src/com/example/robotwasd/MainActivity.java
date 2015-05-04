package com.example.robotwasd;

import jp.ksksue.driver.serial.FTDriver;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.example.OpenCV.ColorBlobDetection;

/**
 * App to controll Robot
 * @author Witsch Daniel, Nocker Martin, Schiborr Mark
 *
 */
public class MainActivity extends ActionBarActivity {
	private Robot robot;
	private ColorBlobDetection blobDetection;
	private TextView textLog;
	
	private EditText editTextX;
	private EditText editTextY;
	private EditText editTextTheta;
	private ViewSwitcher switcher;
	Animation slide_in_left, slide_out_right;

	/**
	 * load the OpenCV camera 
	 */
    private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    blobDetection.mOpenCvCameraView.enableView();
                    blobDetection.mOpenCvCameraView.setOnTouchListener(blobDetection);
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };
	
    /**
     * iniatilize the robot and camera
     */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_main); 
		
		switcher = (ViewSwitcher) findViewById(R.id.viewswitcher);
		textLog = (TextView) findViewById(R.id.textView1);
		editTextX = (EditText) findViewById(R.id.edit_inputX);
		editTextY = (EditText) findViewById(R.id.edit_inputY);
		editTextTheta = (EditText) findViewById(R.id.edit_inputTheta);
		
		slide_in_left = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
		slide_out_right = AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right);

		switcher.setInAnimation(slide_in_left);
		switcher.setOutAnimation(slide_out_right);
		
		//load FTDriver
		FTDriver driver = new FTDriver((UsbManager) getSystemService(USB_SERVICE));
		
		//initialize the ColorBlobDetection and Camera
		blobDetection = new ColorBlobDetection(findViewById(R.id.color_blob_detection_activity_surface_view)); 
		
		//initialize robot functions
		robot = new Robot(driver, textLog, blobDetection);	
		robot.initialize();
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
        if (blobDetection.mOpenCvCameraView != null)
        	blobDetection.mOpenCvCameraView.disableView();
    }
    
	@Override
	protected void onDestroy(){
		robot.disconnect();
		super.onDestroy();
        if (blobDetection.mOpenCvCameraView != null)
        	blobDetection.mOpenCvCameraView.disableView();
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
		robot.navigateToPosition(p, true, false);
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.main, menu);
		menu.add("Buttons");
		menu.add("Camera");
		menu.add("Collect");
		menu.add("Homography");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		CharSequence itemSelected = item.getTitle();
		switch(itemSelected.toString()){
			case "Buttons":
				switcher.showPrevious();
				break;
			case "Camera":
				switcher.showNext();
				break;
			case "Collect":
				RobotThread thread = new RobotThread(robot);
				thread.start();
				// robot.collectBall(false);
				break;
			case "Homography":
				robot.calibrateHomography();
				break;
		}
		return false;
	}
}
