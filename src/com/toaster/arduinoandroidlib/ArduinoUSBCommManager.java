package com.toaster.arduinoandroidlib;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.os.ParcelFileDescriptor;
import android.util.Log;

public class ArduinoUSBCommManager 
{
	private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
	protected PendingIntent usbPermissionIntent;
	private UsbManager mUsbManager;
	public UsbAccessory accessory;
	protected ParcelFileDescriptor mFileDescriptor;
	protected ArduinoBroadcastReceiver broadcastReceiver;
	protected FileInputStream inputStream;
	protected FileOutputStream outputStream;
	public boolean permissionRequestPending;
	protected Context ownerActivity;
	protected ArduinoUSBCommReceiver commReceiver;
	protected ArduinoPacketHandler handler;
	protected DebugHandler debug;
	
	public ArduinoUSBCommManager(Context ownerActivity,ArduinoPacketHandler handler,DebugHandler debug)
	{
		this.debug=debug;
		this.handler=handler;
		this.ownerActivity=ownerActivity;
		mUsbManager=(UsbManager) ownerActivity.getSystemService(Context.USB_SERVICE);
		broadcastReceiver=new ArduinoBroadcastReceiver(this,ownerActivity);
		//usbPermissionIntent = PendingIntent.getBroadcast(ownerActivity, 0, new Intent(ACTION_USB_PERMISSION), 0);
	}
	

	
	public void cleanup()
	{
		ownerActivity.unregisterReceiver(broadcastReceiver);
	}
	
	public void openConnectedAccessory()
	{
		UsbAccessory[] accessories=mUsbManager.getAccessoryList();
		if ((accessories!=null)&&(accessories.length>0))
		{
			accessory=accessories[0];
			mUsbManager.requestPermission(accessory, broadcastReceiver.getPermissionIntent());
			//debug.setText("reopened");
		}
		else
		{
			//debug.setText("failed");
		}
		
	}
	
	
	public void openAccessory(UsbAccessory accessory) {
	
		
		
			mFileDescriptor = mUsbManager.openAccessory(accessory);
			if (mFileDescriptor != null) {
				this.accessory = accessory;
				FileDescriptor fd = mFileDescriptor.getFileDescriptor();
				inputStream = new FileInputStream(fd);
				outputStream = new FileOutputStream(fd);
				commReceiver=new ArduinoUSBCommReceiver(this,inputStream,handler);
				//Thread thread = new Thread(null, commRunnable, TAG);
				//thread.start();
				//Log.d(TAG, "accessory opened");
			} 
			else 
			{
				//Log.d(TAG, "accessory open fail");
			}
		
	
	}

	public void reopen()
	{
		if (commReceiver!=null)
		{
			try {
				inputStream.close();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				outputStream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			commReceiver.cleanup();
			openAccessory(accessory);
		}
	}
	
	public void onResumeHandler()
	{
		if (inputStream != null && outputStream != null) 
		{
			return;
		}
		UsbAccessory[] accessories = mUsbManager.getAccessoryList();
		//UsbAccessory accessory = (accessories == null ? null : accessories[0]);
		UsbAccessory accessory;
		if (accessories==null)
		{
			accessory=null;
		}
		else
		{
			accessory=accessories[0];
		}
		if (accessory != null) 
		{
			if (mUsbManager.hasPermission(accessory)) 
			{
				openAccessory(accessory);
			} 
			else 
			{
				synchronized (broadcastReceiver) 
				{
					if (!permissionRequestPending) 
					{
						mUsbManager.requestPermission(accessory,usbPermissionIntent);
						permissionRequestPending = true;
					}
				}
			}
		} else 
		{
			Log.d("", "mAccessory is null");
		}
	}
	
	public void closeAccessory() {
		debug.setText("closed");
		try 
		{
			if (mFileDescriptor != null) 
			{
				mFileDescriptor.close();
			}
		} 
		catch (IOException e) 
		{
		} 
		finally 
		{
			mFileDescriptor = null;
			accessory = null;
		}
	}
	
	public void onPauseHandler()
	{
		closeAccessory();
	}
	
}
