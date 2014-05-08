package com.toaster.arduinoandroidlib;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.util.Log;

public class ArduinoBroadcastReceiver extends BroadcastReceiver {
	private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
	protected PendingIntent mPermissionIntent;
	protected ArduinoUSBCommManager manager;
	
	public ArduinoBroadcastReceiver(ArduinoUSBCommManager manager,Context context)
	{
		this.manager=manager;
		//IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
		//filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
		//context.registerReceiver(this, filter);
		mPermissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), 0);
		IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
		context.registerReceiver(this, filter);
		context.registerReceiver(this, new IntentFilter(UsbManager.ACTION_USB_ACCESSORY_DETACHED));
	}
	
	public PendingIntent getPermissionIntent()
	{
		return mPermissionIntent;
	}
	
	@Override
	public void onReceive(Context context, Intent intent) 
	{
		String action = intent.getAction();
		if (ACTION_USB_PERMISSION.equals(action)) 
		{
			synchronized (this) 
			{
				UsbAccessory accessory = (UsbAccessory) intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
				if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) 
				{
					if (accessory!=null)
						manager.openAccessory(accessory);
				} 
				else 
				{
					//Log.d(TAG, "permission denied for accessory "	+ accessory);
				}
				manager.permissionRequestPending = false;
			}
		} 
		else if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)) 
		{
            UsbAccessory accessory = (UsbAccessory)intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
            if (accessory != null) {
                manager.closeAccessory();
            }
        }
	}

}
