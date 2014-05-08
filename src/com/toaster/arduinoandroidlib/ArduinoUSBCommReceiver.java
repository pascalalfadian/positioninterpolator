package com.toaster.arduinoandroidlib;

import java.io.FileInputStream;
import java.io.IOException;

import android.util.Log;

public class ArduinoUSBCommReceiver implements Runnable
{
	protected byte[] buffer;
	protected int[] sendBuffer;//perlu yang int soalnya klo byte dia signed, sedangkan mau dipakenya 0-255
	protected boolean isAlive;
	protected Thread workerThread;
	protected FileInputStream inputStream;
	protected ArduinoUSBCommManager manager;
	protected ArduinoPacketHandler handler;
	
	public ArduinoUSBCommReceiver(ArduinoUSBCommManager manager,FileInputStream inputStream,ArduinoPacketHandler handler)
	{
		this.handler=handler;
		this.manager=manager;
		this.inputStream=inputStream;
		buffer=new byte[255];
		sendBuffer=new int[255]; 
		isAlive=true;
		workerThread=new Thread(this);
		workerThread.start();
	}
	
	
	public void cleanup()
	{
		isAlive=false;
		try {
			workerThread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() 
	{
		//int ret = 0;
		int byteCount=0;
		//byte[] buffer = new byte[255];
		while (isAlive)
		{
			try 
			{
				byteCount=inputStream.read(buffer);
				
				ArduinoUSBCommCodec.readAsUnsignedByteArr(buffer, sendBuffer, byteCount);
				handler.onArduinoPacket(sendBuffer);
				/*String result="";
				for (int i=0;i<byteCount;i++)
				{
					result=result+Integer.toString(sendBuffer[i])+" ";
				}
				*/
				
				//manager.outputToDebugHandler(result);
				//int result=ArduinoUSBCommCodec.readInt(buffer, 0);
				//manager.outputToDebugHandler(Integer.toString(result));
				//String out="";
				//for (int i=0;i<4;i++)
				//{
				//	out=out+buffer[i]+" ";
				//}
				//manager.outputToDebugHandler(out);
				//manager.outputToDebugHandler("buffer[0]="+buffer[0]);
				//manager.outputToDebugHandler("count="+byteCount);
				//Log.v("", "count="+byteCount);
			} 
			catch (IOException e) 
			{
				
			}
			if (byteCount>0)
			{
				
			}
			
		}
		
		
		
	}
	
}
