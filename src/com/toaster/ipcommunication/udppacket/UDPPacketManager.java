package com.toaster.ipcommunication.udppacket;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.jar.Pack200.Packer;

import com.toaster.lgcommunication.IMessageHandler;

import android.graphics.Point;
import android.util.Log;



public class UDPPacketManager
{
	protected UDPPacketReceiver receiver;
	protected UDPPacketSender sender;
	protected DatagramSocket socket;
	protected boolean isIntentionallyClosed;
	protected int port;
	protected ArrayList<InetAddress> targetSocketList;

	public UDPPacketManager(IMessageHandler handler,int port)
	{
		this.port=port;
		try 
		{
		
			
			socket=new DatagramSocket(null);
			socket.setReuseAddress(true);
			socket.setBroadcast(true);
			socket.bind(new InetSocketAddress(port));
		} 
		catch (SocketException e) 
		{
			Log.v("udp", e.toString());
		} 
		
		targetSocketList=new ArrayList<InetAddress>();
		receiver=new UDPPacketReceiver(this, socket, handler);
		sender=new UDPPacketSender(this, socket);
	}
	
	public void onSocketClosedHandler()
	{
		//gk ngapa2in..da udp..
	}

	public void addTarget(InetAddress address)
	{
		//harusnya disynchronize
		boolean found=false;
		for (int i=0;i<targetSocketList.size();i++)
		{
			if (targetSocketList.get(i).equals(address))
			{
				found=true;
				break;
			}
		}
		if (!found)
		{

			targetSocketList.add(address);

		}
		Log.v("addtarget", "count="+targetSocketList.size());
	}
	
	public void removeTarget(InetAddress address)
	//harusnya disynchronize
	{
		for (int i=0;i<targetSocketList.size();i++)
		{
			if (targetSocketList.get(i).equals(address))
			{
				targetSocketList.remove(i);
				return;
			}
		}
	}
	
	public boolean isAddressRegistered(InetAddress address)
	{
		for (int i=0;i<targetSocketList.size();i++)
		{
			if (targetSocketList.get(i).equals(address))
			{
				return true;
			}
		}
		return false;
	}
	
	public void cleanup()
	{
		isIntentionallyClosed=true;
		socket.close();
		Log.v("udp", "socket closed");
		sender.cleanup();
		receiver.cleanup();
	}
	
	public boolean send(byte[] buffer, int offset, int length, int option) 
	{
		for (int i=0;i<targetSocketList.size();i++)
		{
			sender.send(new DatagramPacket(buffer, length, targetSocketList.get(i), port));
		}
		return true;
	}
	
	public boolean send(InetAddress target,byte[] buffer, int offset, int length, int option)
	{
		DatagramPacket packet=new DatagramPacket(buffer, length, target, port);
		sender.send(packet);
		return true;
	}
}
