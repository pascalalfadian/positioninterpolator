package cam.toaster.lgcontroller;

import java.io.IOException;

import android.content.Context;
import android.util.Log;

import com.toaster.arduinoandroidlib.ArduinoAndroidDataPairer;
import com.toaster.arduinoandroidlib.ArduinoPacketHandler;
import com.toaster.arduinoandroidlib.ArduinoUSBCommManager;


import cam.toaster.lgcamera.CameraImageProvider;
import cam.toaster.lgcamera.ICameraDataHandler;
import cam.toaster.lgcommunication.LGCommController;

public class LaserGunController implements ICameraDataHandler
{
	protected final static int PC_COMM_PORT=21123;
	protected final static int IRRECV_COMM_PORT=21124;
	
	protected LGCommController commController;
	protected CameraImageProvider camProvider;
	protected IUIHandler uiHandler;
	//protected ArduinoUSBCommManager arduinoManager;
	protected ArduinoAndroidDataPairer pairer;
	
	public LaserGunController(Context ownerActivity,IUIHandler uiHandler)
	{
		this.uiHandler=uiHandler;
		commController=new LGCommController(this,PC_COMM_PORT,IRRECV_COMM_PORT);
		camProvider=new CameraImageProvider(this);
		//arduinoManager=new ArduinoUSBCommManager(ownerActivity, this,null);
		pairer=new ArduinoAndroidDataPairer(this);
		
	}
	
	public void onDataPaired(int x,int y,int count,int id)
	{
		//gw tuker x dgn y nya soalnya si wahyu assume hpnya portrait, bukan landscape
		//Log.v("out", "sent: y="+x+" x="+x+" id="+id+" count="+count);
		commController.sendPoint(y, x, id, count);
	}
	
	//public void connectArduino()
	//{
		//arduinoManager.openConnectedAccessory();
	//}
	
	@Override
	public void onLaserPointDetected(int x, int y) 
	{
		pairer.onCamEvent(x, y);
		//commController.sendPoint(y, x, 234,1);
	}
	
	public CameraImageProvider getCamImageProvider()
	{
		return camProvider;
	}
	
	public void onResume()
	{
		try 
		{
			camProvider.startPreview();
		} 
		catch (IOException e) 
		{
			Log.v("ERROR", e.toString());
		}
		//arduinoManager.onResumeHandler();
	}
	
	public void onPause()
	{
		camProvider.stopPreview();
		//arduinoManager.onPauseHandler();
	}

	@Override
	public void debug(String output) {
		uiHandler.outputToUI(output);
		
	}
	

	
	public void calibrateThreshold()
	{
		camProvider.requestCalibration();
	}

	@Override
	public void onCalibrationCompleted() {
		uiHandler.onThresholdChanged(camProvider.getThreshold());
		
	}
	
	public int getThreshold()
	{
		//return 0;
		return camProvider.getThreshold();
	}
	
	public void setThreshold(int newThreshold)
	{
		camProvider.setThreshold(newThreshold);
	}
	
	public void setMaxMarkedPixel(int newMax)
	{
		camProvider.setMaxMarkedPixel(newMax);
	}

	public void onIdCountReceived(int id,int count) 
	{
		//uiHandler.debugArduino(packet[0]+" "+packet[1]);
		pairer.onArdEvent(count, id);
		Log.v("OUT", "got broadcast id="+id+" count="+count);
		//uiHandler.outputToUI("ert");
		//uiHandler.debugArduino("xyz");
	}
	
}
