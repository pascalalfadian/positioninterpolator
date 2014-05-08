package cam.toaster.lgcommunication;

import java.net.InetAddress;

public interface IMessageHandler 
{
	void onMessageReceived(InetAddress sender,byte[] buffer,int length);
	int getMessageTag();
}
