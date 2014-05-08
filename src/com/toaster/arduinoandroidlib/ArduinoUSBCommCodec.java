package com.toaster.arduinoandroidlib;

public class ArduinoUSBCommCodec 
{
	public static int DATALENGTH_BYTE=1;
	public static int DATALENGTH_INT=4;
	public static int DATALENGTH_FLOAT=4;
	public static int DATALENGTH_LONG=8;
	
	public static int readInt(byte[] input,int offset)
	{
		int result;
		//int i;
		result=0;
		for (int i=0;i<4;i++)
		//for (int i=7;i>=0;i--)
		{
			result=(result<<8)|(((int)input[offset+i])&0xff);
		}
		return result;
	}
	
	public static void readAsUnsignedByteArr(byte[] input, int[] output,int byteCount)
	{
		for (int i=0;i<byteCount;i++)
		{
			output[i]=input[i]&0xFF;
		}
	}
	
	public static long readLong(byte[] input,int offset)
	{
		long result;
		//int i;
		result=0;
		for (int i=0;i<8;i++)
		//for (int i=7;i>=0;i--)
		{
			result=(result<<8)|(((int)input[offset+i])&0xff);
		}
		return result;
	}
	
}
