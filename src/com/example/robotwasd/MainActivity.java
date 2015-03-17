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

public class MainActivity extends ActionBarActivity {
	Robot robot;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		FTDriver com;
		TextView textLog;
		
		textLog = (TextView) findViewById(R.id.textView1);
		com = new FTDriver((UsbManager) getSystemService(USB_SERVICE));
		
		robot = new Robot(com, textLog);
		
		robot.initialize();
		
	}
	
	
	
	/** Button Functions */
	
	public void moveForward(View v) {
		robot.moveForward();
	}
	
	public void moveBackward(View v) {
		robot.moveBackward();
	}
	
	public void stopRobot(View v) {
		robot.stopRobot();
	}
	
	public void turnLeft(View v) {
		robot.turnLeft();
	}
	
	public void turnRight(View w) {
		robot.turnRight();
	}
	
	public void lowerBar(View v) {
		robot.lowerBar();
	}
	
	public void riseBar(View v) {
		robot.riseBar();
	}
	
	// fixed position for bar (low)
	public void lowPositionBar(View v) {
		robot.lowPositionBar();
	}
	
	// fixed position for bar (high)
	public void upPositionBar(View v) {
		robot.upPositionBar();
	}
	
	public void LedOn(View v) {
		robot.LedOn();
	}
	
	public void LedOff(View v) {
		robot.LedOff();
	}
	
	public void readSensor(View v) {
		robot.readSensor();
	}
	
	public void driveSquare(View v) {
		robot.driveSquare((byte) 50);
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
