package cam.toaster.lgcontroller;

import android.content.Context;
import android.util.Log;
import cam.toaster.lgcamera.CameraImageProvider;
import cam.toaster.lgcamera.ICameraDataHandler;
import cam.toaster.lgcommunication.LGCommController;


import com.toaster.arduinoandroidlib.ArduinoPacketHandler;
import com.toaster.arduinoandroidlib.ArduinoUSBCommManager;

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
