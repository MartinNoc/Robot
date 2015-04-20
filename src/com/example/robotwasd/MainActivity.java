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
import android.widget.EditText;
import android.widget.TextView;

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
		
		//initialize the ColorBlobDetection and Camera
		blobDetection = new ColorBlobDetection(findViewById(R.id.color_blob_detection_activity_surface_view)); 
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
}
