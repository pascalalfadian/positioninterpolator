package com.toaster.lgcommunication;

import java.net.InetAddress;

public class IRReceiverHandler implements IMessageHandler
{
	protected LGCommController lgCommController;
	
	public IRReceiverHandler(LGCommController lgCommController)
	{
		this.lgCommController=lgCommController;
	}

	@Override
	public void onMessageReceived(InetAddress sender, byte[] buffer, int length) 
	{		
		int id=buffer[1]&0xFF;
		int count=buffer[0]&0xFF;
		lgCommController.onIRBroadcastReceived(id, count);
			
	}

	@Override
	public int getMessageTag() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	
}
