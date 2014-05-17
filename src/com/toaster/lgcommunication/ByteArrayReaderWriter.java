package com.toaster.lgcommunication;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

public class ByteArrayReaderWriter 
{
	//gk thread safe
	
	public static int DATALENGTH_BYTE=1;
	public static int DATALENGTH_INT=4;
	public static int DATALENGTH_FLOAT=4;
	public static int DATALENGTH_LONG=8;
	public static String STRING_ENCODING="US-ASCII";
	
	public ByteArrayOutputStream outStream; //public biar bisa dipk YuvImage pas dia lg ngecompress
	protected DataOutputStream dataOutStream;
	
	public int getOutBufferLength()
	{
		return outStream.size();
	}
	
	public byte[] getByteBuffer()
	{
		return outStream.toByteArray();
		//System.arraycopy(outStream.toByteArray(),0 , outBuffer, outBufferLength, length);
	}
	
	public static int byteArrToUnsignedByte(byte[] input,int offset)
	{
		return 0xFF&input[offset];
	}
	
	public static int byteArrToInt(byte[] input,int offset)
	{
		//int value = ((input[offset] & 0xFF) << 24) | ((input[offset+1] & 0xFF) << 16)
		//        | ((input[offset+2] & 0xFF) << 8) | (input[offset+3] & 0xFF);
		//return value;
		int result;
		int i;
		result=0;
		for (i=0;i<4;i++)
		{
			result=(result<<8)|(((int)input[offset+i])&0xff);
		}
		return result;
	}
	
	public static long byteArrToLong(byte[] input,int offset)
	{
		long result;
		int i;
		result=0;
		for (i=0;i<8;i++)
		{
			result=(result<<8)|(((int)input[offset+i])&0xff);
		}
		return result;
	}
	
	public static float byteArrToFloat(byte[] input,int offset)
	{
		//new Float(Float.intBitsToFloat(byteArrToInt(input, offset)));
		
		return Float.intBitsToFloat(byteArrToInt(input, offset));
	}
	
	public static String byteArrToString(byte[] input,int offset)
	{
		int length=byteArrToInt(input, offset);
		try 
		{
			return new String(input,offset+DATALENGTH_INT,length,STRING_ENCODING);
		} 
		catch (UnsupportedEncodingException e) 
		{
			e.printStackTrace();
			return null;
		}
	}
	
	
	public ByteArrayReaderWriter(int outBufferSize)
	{
		outStream=new ByteArrayOutputStream();
		dataOutStream=new DataOutputStream(outStream);
	}

	public int encode(byte[] data)
	{
		try
		{
			outStream.write(data);
		}
		catch (IOException e) 
		{	
		}
		return outStream.size();
	}
	
	public int encode(int data)
	{
		try 
		{
			dataOutStream.writeInt(data);
		} 
		catch (IOException e) 
		{	
		}
		return outStream.size();
	}
	
	public int encode(long data)
	{
		try 
		{
			dataOutStream.writeLong(data);
		} 
		catch (IOException e) 
		{
			
		}
		return outStream.size();
	}
	
	public int encode(float data)
	{
		try 
		{
			dataOutStream.writeInt(Float.floatToIntBits(data));
		} 
		catch (IOException e) 
		{
		}
		return outStream.size();
	}
	
	public int encode(String data)
	{
		try 
		{
			dataOutStream.writeInt(data.length());
			dataOutStream.write(data.getBytes(STRING_ENCODING));
		} 
		catch (IOException e) 
		{
		}
		return outStream.size();
	}
	
	public int encodeIntAsUnsignedByte(int byteData)
	{
		try 
		{
			dataOutStream.writeByte(byteData);
		} 
		catch (IOException e) 
		{
		}
		return outStream.size();
	}
	
	public void resetOutBuffer()
	{
		outStream.reset();
	}
		
	
		
}
