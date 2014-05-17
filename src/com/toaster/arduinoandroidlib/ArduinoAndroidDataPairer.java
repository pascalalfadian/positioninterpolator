package com.toaster.arduinoandroidlib;

import com.toaster.lgcontroller.LaserGunController;

import android.util.Log;

public class ArduinoAndroidDataPairer 
{
	protected int prevX,prevY;
	protected int prevId,prevCount;
	protected long prevCamEvent,prevArdEvent;
	protected long EVENT_THRESHOLD=200;
	protected LaserGunController controller;
	
	public ArduinoAndroidDataPairer(LaserGunController controller)
	{
		this.controller=controller;
	}
	
	public void onArdEvent(int count,int id)
	{
		Log.v("OUT", "ard event");
		if (Math.abs(prevCamEvent-System.currentTimeMillis())<EVENT_THRESHOLD)
		{
			//Log.v("OUT", "paired"+Math.abs(prevCamEvent-System.currentTimeMillis()));
			prevArdEvent=Long.MIN_VALUE;
			prevCamEvent=Long.MIN_VALUE;
			controller.onDataPaired(prevX, prevY, count, id);
		}
		else
		{
			
			//Log.v("OUT", "not paired cam="+Math.abs(prevCamEvent-System.currentTimeMillis()));
			prevCount=count;
			prevId=id;
			prevArdEvent=System.currentTimeMillis();		
		}
	}
	
	public void onCamEvent(int x,int y)
	{
		Log.v("OUT", "cam event");
		if (Math.abs(prevArdEvent-System.currentTimeMillis())<EVENT_THRESHOLD)
		{
			//Log.v("OUT", "paired "+ Math.abs(prevArdEvent-System.currentTimeMillis()));
			prevArdEvent=Long.MIN_VALUE;
			prevCamEvent=Long.MIN_VALUE;
			controller.onDataPaired(x, y, prevCount, prevId);
		}
		else
		{
			//Log.v("OUT", "not paired");
			prevX=x;
			prevY=y;
			prevCamEvent=System.currentTimeMillis();		
		}
	}
}
