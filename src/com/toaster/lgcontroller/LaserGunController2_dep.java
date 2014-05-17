package com.toaster.lgcontroller;

import android.content.Context;
import android.util.Log;


import com.toaster.arduinoandroidlib.ArduinoPacketHandler;
import com.toaster.arduinoandroidlib.ArduinoUSBCommManager;
import com.toaster.lgcamera.CameraImageProvider;
import com.toaster.lgcamera.ICameraDataHandler;
import com.toaster.lgcommunication.LGCommController;

public class LaserGunController2_dep implements ICameraDataHandler,ArduinoPacketHandler
{
	protected ArduinoUSBCommManager arduinoManager;
	protected IUIHandler uiHandler;
	
	public LaserGunController2_dep(Context ownerActivity,IUIHandler uiHandler)
	{
		this.uiHandler=uiHandler;
		arduinoManager=new ArduinoUSBCommManager(ownerActivity, this,null);

		
	}


	@Override
	public void onArduinoPacket(int[] packet) {
		
		uiHandler.debugArduino(packet[0]+" "+packet[1]);
		
	}

	@Override
	public void onLaserPointDetected(int x, int y) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onCalibrationCompleted() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void debug(String output) {
		// TODO Auto-generated method stub
		
	}
	
	public void onPause()
	{
		arduinoManager.onPauseHandler();
	}
	
	public void onResume()
	{
		arduinoManager.onResumeHandler();
	}

}
