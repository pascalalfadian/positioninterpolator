package com.toaster.lgcommunication;


import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import android.util.Log;


import com.toaster.ipcommunication.udppacket.UDPPacketManager;
import com.toaster.ipcommunication.udppacket.UDPPacketReceiver;
import com.toaster.lgcontroller.LaserGunController;

public class LGCommController implements IMessageHandler
{
	protected final static int MSG_REQUESTSTATUS=0;
	protected final static int MSG_REGISTER=1;
	protected final static int MSG_UNREGISTER=2;
	protected final static int MSG_STATUSREPORT=3;
	protected final static int MSG_CALIBRATETHRESHOLD=4;
	protected final static int MSG_POINTS=5;
	
	protected final static int STATUS_REGISTERED=0;
	protected final static int STATUS_UNREGISTERED=1;
	protected ByteArrayReaderWriter byteArrRW;
	protected LaserGunController lgController;
	protected UDPPacketManager udpPCCommManager;
	protected UDPPacketManager udpIRReceiverCommManager;
	protected int pcPort,irReceiverPort;
	
	public LGCommController(LaserGunController lgController,int pcPort,int irReceiverPort)
	{
		this.lgController=lgController;
		this.pcPort=pcPort;
		this.irReceiverPort=irReceiverPort;
		udpPCCommManager=new UDPPacketManager(this, pcPort);
		udpIRReceiverCommManager=new UDPPacketManager(new IRReceiverHandler(this), irReceiverPort);
		byteArrRW=new ByteArrayReaderWriter(250);
	}
	
	
	public void cleanup()
	{
		udpPCCommManager.cleanup();
	}

	public void onIRBroadcastReceived(int id,int count)
	{
		lgController.onIdCountReceived(id, count);
	}

	@Override
	public void onMessageReceived(InetAddress sender, byte[] buffer, int length) 
	{
		int messageType=ByteArrayReaderWriter.byteArrToInt(buffer, 0);
		//Log.v("LGCommCOntroller", "message : "+messageType);
		if (messageType==MSG_REQUESTSTATUS)
		{
			byte[] outBuffer;
			synchronized(byteArrRW)
			{
				byteArrRW.resetOutBuffer();
				byteArrRW.encode(MSG_STATUSREPORT);
				Log.v("LGCommCOntroller", "sending status to : "+sender.getHostAddress());
				if (udpPCCommManager.isAddressRegistered(sender))
				{
					byteArrRW.encode(STATUS_REGISTERED);
				}
				else
				{
					byteArrRW.encode(STATUS_UNREGISTERED);
				}
				outBuffer=byteArrRW.getByteBuffer();
			}
			udpPCCommManager.send(sender,outBuffer , 0, outBuffer.length, 0);
		}
		else if (messageType==MSG_REGISTER)
		{
			udpPCCommManager.addTarget(sender);
			
		}
		else if (messageType==MSG_UNREGISTER)
		{
			udpPCCommManager.removeTarget(sender);
		}
		else if (messageType==MSG_CALIBRATETHRESHOLD)
		{
			byteArrRW.resetOutBuffer();
		}
	}
	
	public void sendPoint(int x,int y,int id,int count)
	{
		byte[] outBuffer;
		synchronized(byteArrRW)
		{
			byteArrRW.resetOutBuffer();
			byteArrRW.encode(MSG_POINTS);
			byteArrRW.encode(x);
			byteArrRW.encode(y);
			byteArrRW.encode(id);
			byteArrRW.encode(count);
			outBuffer=byteArrRW.getByteBuffer();
		}
		udpPCCommManager.send(outBuffer, 0, outBuffer.length, 0);
	}


	@Override
	public int getMessageTag() {
		// TODO Auto-generated method stub
		return 0;
	}
}
