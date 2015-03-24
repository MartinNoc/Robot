package com.example.robotwasd;

import com.example.robotwasd.R;

import jp.ksksue.driver.serial.FTDriver;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

/**
 * App do controll Robot
 * @author Witsch Daniel, Nocker Martin, Schiborr Mark
 *
 */
public class MainActivity extends ActionBarActivity {
	Robot robot;
	TextView textLog;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		textLog = (TextView) findViewById(R.id.textView1);
		FTDriver driver = new FTDriver((UsbManager) getSystemService(USB_SERVICE));
		
		robot = new Robot(driver, textLog);	
		
		robot.initialize();
	}
	
	@Override
	protected void onDestroy(){
		robot.disconnect();
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
		robot.turnLeft(45);
	}
	
	public void dButton_onClick(View w) {
		robot.turnRight(45);
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
		//robot.driveSquare(30.0);
		robot.OdometryTestMovement();
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
		robot.getOdometryData();
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
