package com.toaster.ipcommunication.udppacket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Semaphore;

import android.util.Log;

public class UDPPacketSender implements Runnable
{
	protected Semaphore semaphore;
	protected Thread workerThread;
	protected Queue<DatagramPacket> outQueue;
	protected UDPPacketManager udpPacketManager;
	//protected ArrayList<DatagramSocket> socketList;
	protected boolean isAlive;
	protected DatagramSocket outSocket;
	
	public UDPPacketSender(UDPPacketManager packetManager,DatagramSocket outSocket)
	{
		this.outSocket=outSocket;
		//this.socketList=socketList;
		outQueue=new ArrayBlockingQueue<DatagramPacket>(50);
		semaphore=new Semaphore(0);
		isAlive=true;
		workerThread=new Thread(this);
		workerThread.start();
	}

	public void send(DatagramPacket dataPacket)
	{
		outQueue.add(dataPacket);
		semaphore.release();
	}
	
//	public void send(DatagramSocket target, DatagramPacket dataPacket)
//	{
//		//rada jelek. jadinya bisa ada 2 packet keluar sekaligus. lupa gw apa efeknya klo entar sampe kejadian
//		//tapi rasanya dulu ngaconya itu 
//		//1. di lego
//		//2. ada n packet per 30 ms
//		//jd ini rasanya gpp...semoga
//		try 
//		{
//			target.send(dataPacket);
//		} 
//		catch (IOException e) 
//		{
//		
//		}
//	}
	
	@Override
	public void run() 
	{
		DatagramPacket packet;
		while (isAlive)
		{
			try 
			{
				semaphore.acquire();
			} 
			catch (InterruptedException e) 
			{
				
			}
			if (outQueue.isEmpty())
				continue;
			packet=outQueue.remove();
			try 
			{
					outSocket.send(packet);
			} 
			catch (IOException e) 
			{
					e.printStackTrace();
			}
			Log.v("sender", "sent");
			
	
		}
	}
	
	public void cleanup()
	{
		isAlive=false;
		outQueue.clear();
		semaphore.release();
		try 
		{
			workerThread.join();
		} 
			catch (InterruptedException e) 
		{
			e.printStackTrace();
		}	
	}
}
