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

//MainActivity
public class MainActivity extends ActionBarActivity {

	private FTDriver com;
	private TextView textLog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		textLog = (TextView) findViewById(R.id.textView1);
		com = new FTDriver((UsbManager) getSystemService(USB_SERVICE));
		
		connect();
		
	}
	
	public void connect() {
		if (com.begin(9600))
			textLog.append("connected\n");
        else
        	textLog.append("could not connect\n");
	}
	
	public void disconnect() {
	       com.end();
	       if (!com.isConnected()) {
	          textLog.append("disconnected\n");
	       }
	}
	
	/** Button Functions */
	
	public void reconnect(View v) {
		if (!com.isConnected()) {
			connect();
		}
	}
	
	public void moveForward(View v) {
		textLog.append("forward\n");
		comWrite(new byte[] {'w','\r','\n'});
	}
	
	public void moveBackward(View v) {
		textLog.append("backward\n");
		comWrite(new byte[] {'x','\r','\n'});
	}
	
	public void stopRobot(View v) {
		textLog.append("stop\n");
		comWrite(new byte[] {'s','\r','\n'});
	}
	
	public void turnLeft(View v) {
		textLog.append("turn left\n");
		comWrite(new byte[] {'a','\r','\n'});
	}
	
	public void turnRight(View w) {
		textLog.append("turn right\n");
		comWrite(new byte[] {'d','\r','\n'});
	}
	
	public void lowerBar(View v) {
		textLog.append("lower\n");
		comWrite(new byte[] {'-','\r','\n'});
	}
	
	public void riseBar(View v) {
		textLog.append("rise\n");
		comWrite(new byte[] {'+','\r','\n'});
	}
	
	// fixed position for bar (low)
	public void lowPositionBar(View v) {
		robotSetBar((byte) 0);
	}
	
	// fixed position for bar (high)
	public void upPositionBar(View v) {
		robotSetBar((byte) 255);
	}
	
	public void LedOn(View v) {
		robotSetLeds((byte) 255, (byte) 128);
	}
	
	public void LedOff(View v) {
		robotSetLeds((byte) 0, (byte) 0);
	}
	
	public void readSensor(View v) {
		textLog.append(comReadWrite(new byte[] { 'q', '\r', '\n' })+"\n");
	}
	
	/*****************************/
		
	public void comWrite(byte[] data) {
		if (com.isConnected()) {
			com.write(data);
		}
		else {
			textLog.append("not connected\n");
		}
	}
	
	/**
	* reads from the serial buffer. due to buffering, the read command is
	* issued 3 times at minimum and continuously as long as there are bytes to
	* read from the buffer. Note that this function does not block, it might
	* return an empty string if no bytes have been read at all.
	*
	* @return buffer content as string
	*/
	public String comRead() {
		String s = "";
		int i = 0;
		int n = 0;
		while (i < 3 || n > 0) {
			byte[] buffer = new byte[256];
			n = com.read(buffer);
			s += new String(buffer, 0, n);
			i++;
		}
		return s;
	}
	
	/**
	* write data to serial interface, wait 100 ms and read answer.
	*
	* @param data
	* to write
	* @return answer from serial interface
	*/
	public String comReadWrite(byte[] data) {
		com.write(data);
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// ignore
		}
		return comRead();
	}
	
	/**
	 * Setting blue and red LED
	 * @param red
	 * @param blue
	 */
	public void robotSetLeds(byte red, byte blue) {
		comReadWrite(
			new byte[] { 'u', red, blue, '\r', '\n' }
		);
	}
	
	/**
	 * Driving forward/backward
	 * @param distance_cm
	 */
	public void robotDrive(byte distance_cm) {
		comReadWrite(
			new byte[] { 'k', distance_cm, '\r', '\n' }
		);
	}
	
	/**
	 * Turning the robot
	 * @param degree
	 */
	public void robotTurn(byte degree) {
		comReadWrite(
			new byte[] { 'l', degree, '\r', '\n' }
		);
	}
	
	/**
	 * Driving by velocity
	 * @param left
	 * @param right
	 */
	public void robotSetVelocity(byte left, byte right) {
		comReadWrite(
			new byte[] { 'i', left, right, '\r', '\n' }
		);
	}
	
	/**
	 * Using the bar
	 * @param value
	 */
	public void robotSetBar(byte value) {
		comReadWrite(
			new byte[] { 'o', value, '\r', '\n' }
		);
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
