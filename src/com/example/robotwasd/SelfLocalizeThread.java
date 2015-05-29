package com.example.robotwasd;

public class SelfLocalizeThread extends Thread{
	
	private Robot robot;
	
	public SelfLocalizeThread(Robot robot){
		this.robot = robot;
	}
	
	public void run() {
		robot.startSelfLocalization();
	}
}
