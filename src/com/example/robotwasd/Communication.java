package com.example.robotwasd;

import jp.ksksue.driver.serial.FTDriver;

/**
 * Class for communication with the robot
 * @author Witsch Daniel
 *
 */
public class Communication {
	private FTDriver driver;
	private static final int BAUDRATE = 9600;
	
	public Communication(FTDriver driver){
		this.driver = driver;
	}
	
	public boolean connect() {
		if (driver.begin(BAUDRATE)){
			return true;
		}
        else{
        	return false;
        }
	}
	
	public boolean disconnect() {
	       driver.end();
	       return !driver.isConnected();
	}
	
	public void reconnect() {
		if (!driver.isConnected()) {
			connect();
		}
	}
	
	public boolean isConnected() {
		return driver.isConnected();
	}
	
	/**Low Level Functions */
	/*****************************/
	
	public boolean writeRobot(byte[] data) {
		if (driver.isConnected()) {
			driver.write(data);
			return true;
		}
		else {
			return false;
		}
	}
	/**
	* reads from the serial buffer. due to buffering, the read command is
	* issued 3 times at minimum and continuously as long as there are bytes to
	* read from the buffer. Note that this function does not block, it might
	* return an empty string if no bytes have been read at all.
	*
	* @return buffer content as string, if there is no connection then return an empty string
	*/
	public String readRobot() {
		String s = "";
		if (isConnected()) {
			int i = 0;
			int n = 0;
			while (i < 3 || n > 0) {
				byte[] buffer = new byte[256];
				n = driver.read(buffer);
				s += new String(buffer, 0, n);
				i++;
			}
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
	public String readWriteRobot(byte[] data) {
		if (isConnected()) {
			driver.write(data);
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// ignore
			}
		}
		return readRobot();
	}
}
