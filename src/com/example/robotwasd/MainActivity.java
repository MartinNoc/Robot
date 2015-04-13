package com.example.robotwasd;

import com.example.robotwasd.R;

import jp.ksksue.driver.serial.FTDriver;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

/**
 * App do controll Robot
 * @author Witsch Daniel, Nocker Martin, Schiborr Mark
 *
 */
public class MainActivity extends ActionBarActivity {
	private Robot robot;
	private TextView textLog;
	
	private EditText editTextX;
	private EditText editTextY;
	private EditText editTextTheta;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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
	}
	
	@Override
	protected void onDestroy(){
		robot.disconnect();
		super.onDestroy();
	}
	
	
	
	/** Button Functions */
	
	public void wButton_onClick(View v) {
		robot.moveForward();
	}
	
	public void xButton_onClick(View v) {
		robot.moveBackward();
	}
	
	public void sButton_onClick(View v) {
		robot.stopRobot();
	}
	
	public void aButton_onClick(View v) {
		robot.turnLeft(90);
	}
	
	public void dButton_onClick(View w) {
		robot.turnRight(90);
	}
	
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
	
	public void LEDonButton_onClick(View v) {
		robot.LedOn();
	}
	
	public void LEDoffButton_onClick(View v) {
		robot.LedOff();
	}
	
	public void readSensorButton_onClick(View v) {
		textLog.setText(robot.readSensor());
	}
	
	public void SquareButton_onClick(View v) {
		robot.driveSquare(90.0);
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
