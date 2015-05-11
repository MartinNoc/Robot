package com.example.robotwasd;

import jp.ksksue.driver.serial.FTDriver;
import android.content.Intent;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;


/**
 * App to controll Robot
 * 
 * @author Witsch Daniel, Nocker Martin, Schiborr Mark
 * 
 */
public class MainActivity extends ActionBarActivity {
	private Robot robot;
	private TextView textLog;

	private EditText editTextX;
	private EditText editTextY;
	private EditText editTextTheta;

	/**
	 * iniatilize the robot
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		textLog = (TextView) findViewById(R.id.textView1);
		editTextX = (EditText) findViewById(R.id.edit_inputX);
		editTextY = (EditText) findViewById(R.id.edit_inputY);
		editTextTheta = (EditText) findViewById(R.id.edit_inputTheta);
		
		editTextX.setText("40");
		editTextY.setText("0");
		editTextTheta.setText("0");

		// load FTDriver
		FTDriver driver = new FTDriver(
				(UsbManager) getSystemService(USB_SERVICE));

		// initialize robot functions
		robot = new Robot(driver);
		ValueHolder.setRobot(robot);
		if (robot.initialize()) {
			textLog.setText("connected");
		} else {
			textLog.setText("not connected");
		}
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (robot.disconnect()) {
			textLog.setText("disconnected");
		} else {
			textLog.setText("not disconnected");
		}
	}

	/** Button Functions */

	public void minusButton_onClick(View v) {
		robot.lowerBar();
		textLog.setText("lower Bar");
	}

	public void plusButton_onClick(View v) {
		robot.riseBar();
		textLog.setText("rise Bar");
	}

	// fixed position for bar (low)
	public void downButton_onClick(View v) {
		robot.lowPositionBar();
		textLog.setText("lowest Position Bar");
	}

	// fixed position for bar (high)
	public void upButton_onClick(View v) {
		robot.upPositionBar();
		textLog.setText("highest Position Bar");
	}

	public void connectButton_onClick(View v) {
		if (robot.connect()) {
			textLog.setText("connected");
		} else {
			textLog.setText("not connected");
		}
	}

	public void disconnectButton_onClick(View v) {
		if (robot.disconnect()) {
			textLog.setText("disconnected");
		} else {
			textLog.setText("not disconnected");
		}
	}

	public void CalibrateButton_onClick(View v) {
		double distance = robot.calibrateSensor();
		textLog.setText("New Stop Distance: " + distance + "cm");
	}

	public void odometryButton_onClick(View v) {
		/*
		textLog.setText("Odometry: (" + robot.getOdometryPosition().x + ", "
				+ robot.getOdometryPosition().y + ", "
				+ robot.getOdometryPosition().theta + ")");
		 */
		textLog.setText("Odometry: " + robot.getOdometryPosition().toString());
	}

	public void initOdometryButton_onClick(View v) {
		robot.initOdometryPosition();
		odometryButton_onClick(v);
	}

	// with Obstacle Avoidance ("OA")
	public void navigationButton_onClick(View v) {
		double x = Double.parseDouble(editTextX.getText().toString());
		double y = Double.parseDouble(editTextY.getText().toString());
		double theta = Double.parseDouble(editTextTheta.getText().toString());
		Position p = new Position(x, y, theta);
		robot.navigateToPosition(p);
		/*
		textLog.setText("Navigate To: (" + robot.getOdometryPosition().x + ", "
				+ robot.getOdometryPosition().y + ", "
				+ robot.getOdometryPosition().theta + ")");
		*/
		textLog.setText("Navigate To: " + robot.getOdometryPosition().toString());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		// getMenuInflater().inflate(R.menu.main, menu);
		menu.add("Camera");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		CharSequence itemSelected = item.getTitle();
		switch (itemSelected.toString()) {
		case "Camera":
			Intent intent = new Intent();
			intent.setClassName("com.example.robotwasd", "com.example.robotwasd.ColorBlobDetection");
			//startActivity(new Intent(this,ColorBlobDetection.class));
			startActivity(intent);
			break;
		}
		return false;
	}
}
