package cam.toaster.lgcamera;

import java.io.IOException;
import java.util.ArrayList;

import cam.toaster.lgcommunication.LGCommController;

import android.util.Log;
import android.view.TextureView;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;

public class CameraImageProvider implements Camera.PreviewCallback,TextureView.SurfaceTextureListener
{
	protected static int BUFFER_COUNT=5;
	protected static int MAX_RECURSIVE_DEPTH=10;
	
	
	protected Camera camera;
	protected SurfaceTexture previewTexture;
	protected int previewWidth,previewHeight;
	protected byte[] flagArr;
	protected int luminanceThreshold;
	protected int maxX,minX,maxY,minY;
	//protected MainActivity mainActivity;
	//protected int areaCount;
	protected String info;
	protected XYStack floodFillStack;
	protected ArrayList<Point> resultList;
	//protected dep_UDPPacketSender sender;
	protected ICameraDataHandler dataHandler;
	protected SurfaceTexture surface;
	protected boolean isCalibrating;
	protected int markedPixelCount;
	protected int maxMarkedPixel;
	
	public CameraImageProvider(ICameraDataHandler handler)
	{
		this.dataHandler=handler;
		this.maxMarkedPixel=5000;
		this.luminanceThreshold=200;
		this.floodFillStack=new XYStack(5000);
		this.resultList=new ArrayList<Point>();
	}
	
	protected static void byteArrFill(byte[] array,byte value)
	{
		int len=array.length;
		array[0]=value;
		for (int i=1;i<len;i+=i)
		{
			System.arraycopy(array, 0, array, i, (len-i<i)?(len-i):i);
		}
	}
	
	protected static int getLuminance(byte[] yuvBuffer,int x,int y,int scanlineLength)
	{
		return (yuvBuffer[(y*scanlineLength)+x])&0xff;
	}
	
	protected boolean floodFillIterative(byte[] buffer,int startX,int startY,int scanlineLength,int width,int height,int threshold)
	{
		floodFillStack.clear();
		floodFillStack.push(startX, startY);
		Point curPoint;
		do
		{
			markedPixelCount++;
			if (markedPixelCount>maxMarkedPixel)
				return false;
			curPoint=floodFillStack.pop();
			if (curPoint!=null)
			{
				int curX=curPoint.x;
				int curY=curPoint.y;
				if (curX<minX)
					minX=curX;
				else if (curX>maxX)
					maxX=curX;
				if (curY<minY)
					minY=curY;
				else if (curY>maxY)
					maxY=curY;
				
				flagArr[(scanlineLength*curY)+curX]=1;
				if (evalPixel(curX, curY+1, width, height, flagArr, buffer, scanlineLength, threshold))
					floodFillStack.push(curX, curY+1);
				if (evalPixel(curX+1, curY+1, width, height, flagArr, buffer, scanlineLength, threshold))
					floodFillStack.push(curX+1, curY+1);
				if (evalPixel(curX+1, curY, width, height, flagArr, buffer, scanlineLength, threshold))
					floodFillStack.push(curX+1, curY);
				if (evalPixel(curX+1, curY-1, width, height, flagArr, buffer, scanlineLength, threshold))
					floodFillStack.push(curX+1, curY-1);
				if (evalPixel(curX, curY-1, width, height, flagArr, buffer, scanlineLength, threshold))
					floodFillStack.push(curX, curY-1);
				if (evalPixel(curX-1, curY-1, width, height, flagArr, buffer, scanlineLength, threshold))
					floodFillStack.push(curX-1, curY-1);
				if (evalPixel(curX-1, curY, width, height, flagArr, buffer, scanlineLength, threshold))
					floodFillStack.push(curX-1, curY);
				if (evalPixel(curX-1, curY+1, width, height, flagArr, buffer, scanlineLength, threshold))
					floodFillStack.push(curX-1, curY+1);
			}
		}
		while (curPoint!=null);
		return true;
	}
	/*
	protected void floodFill(byte[] buffer,int curX,int curY,int scanlineLength,int width,int height,int threshold,int stepCount)
	{
		int nextLuminance;
		int luminance=buffer[(curY*scanlineLength)+curX]&0xff;
		if (curX<minX)
			minX=curX;
		else if (curX>maxX)
			maxX=curX;
		if (curY<minY)
			minY=curY;
		else if (curY>maxY)
			maxY=curY;
		flagArr[(scanlineLength*curY)+curX]=1;
		if (evalPixel(curX, curY+1, width, height, flagArr, buffer, scanlineLength, threshold))
			floodFill(buffer, curX, curY+1, scanlineLength, width, height, threshold, stepCount);
		if (evalPixel(curX+1, curY+1, width, height, flagArr, buffer, scanlineLength, threshold))
			floodFill(buffer, curX+1, curY+1, scanlineLength, width, height, threshold, stepCount);
		if (evalPixel(curX+1, curY, width, height, flagArr, buffer, scanlineLength, threshold))
			floodFill(buffer, curX+1, curY, scanlineLength, width, height, threshold, stepCount);
		if (evalPixel(curX+1, curY-1, width, height, flagArr, buffer, scanlineLength, threshold))
			floodFill(buffer, curX+1, curY-1, scanlineLength, width, height, threshold, stepCount);
		if (evalPixel(curX, curY-1, width, height, flagArr, buffer, scanlineLength, threshold))
			floodFill(buffer, curX, curY-1, scanlineLength, width, height, threshold, stepCount);
		if (evalPixel(curX-1, curY-1, width, height, flagArr, buffer, scanlineLength, threshold))
			floodFill(buffer, curX-1, curY-1, scanlineLength, width, height, threshold, stepCount);
		if (evalPixel(curX-1, curY, width, height, flagArr, buffer, scanlineLength, threshold))
			floodFill(buffer, curX-1, curY, scanlineLength, width, height, threshold, stepCount);
		if (evalPixel(curX-1, curY+1, width, height, flagArr, buffer, scanlineLength, threshold))
			floodFill(buffer, curX-1, curY+1, scanlineLength, width, height, threshold, stepCount);
	}*/
	
	public static boolean evalPixel(int x,int y,int width,int height,byte[] flagArr,byte[] yuvBuffer,int scanlineLength,int threshold)
	{
		if ((x<0)||(y<0)||(x>width-1)||(y>height-1))
			return false;
		if (flagArr[(scanlineLength*y)+x]!=1)
		{
			int luminance=yuvBuffer[(y*scanlineLength)+x]&0xff;
			if (luminance>threshold)
				return true;
			else 
				return false;
		}
		else
			return false;
	}
	
	

	public static float guessAvgLumi(byte[] buffer,int width,int height,int scanlineSize)
	{
		return (getLuminance(buffer,0,0,scanlineSize)+getLuminance(buffer,width-1,0,scanlineSize)+getLuminance(buffer,0,height-1,scanlineSize)+getLuminance(buffer,width-1,height-1,scanlineSize))/4.0f;
	}

	public void requestCalibration()
	{
		this.isCalibrating=true;
	}
	
	public int calculateThreshold(byte[] buffer,int width,int height,int scanlineSize)
	{
		int maxLumi=0;
		int curLumi=0;
		for (int i=0;i<height;i++)
		{
			for (int j=0;j<width;j++)
			{
				curLumi=getLuminance(buffer, j, i, scanlineSize);
				if (curLumi>maxLumi)
					maxLumi=curLumi;
			}
		}
		return maxLumi+30;
	}
	
	public void setThreshold(int newThreshold)
	{
		this.luminanceThreshold=newThreshold;
	}
		
	public int getThreshold()
	{
		return this.luminanceThreshold;
	}
	
	public void setMaxMarkedPixel(int newMax)
	{
		maxMarkedPixel=newMax;
	}

	@Override
	public void onPreviewFrame(byte[] buffer, Camera arg1) 
	{
		//areaCount=0;
		boolean shouldContinue=true;
		markedPixelCount=0;
		byteArrFill(flagArr, (byte)0);
		if (isCalibrating)
		{
			setThreshold(this.calculateThreshold(buffer, previewWidth, previewHeight, previewWidth));
			dataHandler.onCalibrationCompleted();
			isCalibrating=false;
		}
		
		
		
		int scanlineSize=previewWidth;
		int maxLuminance=0;
		int previewTotalSize=previewWidth*previewHeight;
		maxX=Integer.MIN_VALUE;
		minX=Integer.MAX_VALUE;
		maxY=Integer.MIN_VALUE;
		minY=Integer.MAX_VALUE;
		resultList.clear();
		//Log.v("previewcam", Float.toString(guessAvgLumi(buffer,previewWidth,previewHeight,scanlineSize)));
		//if (guessAvgLumi(buffer,previewWidth,previewHeight,scanlineSize)<luminanceThreshold)
		//{
		long startTime=System.nanoTime();
		for (int y=0;(y<previewHeight)&&(shouldContinue);y+=2)
		{
			int scanlineStart=y*scanlineSize;
			for (int x=0;(x<previewWidth)&&(shouldContinue);x+=2)
			{
				int luminance=buffer[scanlineStart+x]&0xff;
				//if ((luminance>luminanceThreshold)&&(flagArr[(previewWidth*i)+j]!=1))
				if (luminance>maxLuminance)
					maxLuminance=luminance;
				if (evalPixel(x, y, previewWidth, previewHeight, flagArr, buffer, scanlineSize, luminanceThreshold))
				{
					//areaCount++;
					minX=Integer.MAX_VALUE;
					minY=Integer.MAX_VALUE;
					maxX=Integer.MIN_VALUE;
					maxY=Integer.MIN_VALUE;
					
					shouldContinue=floodFillIterative(buffer, x, y, scanlineSize, previewWidth, previewHeight, luminanceThreshold);
					if ((minX>=0)&&(minX<=640)&&(maxX>=0)&&(maxX<=640)&&(minY>=0)&&(minY<=640)&&(maxY>=0)&&(maxY<=640))
						resultList.add(new Point((minX+maxX)/2,(minY+maxY)/2 ));
				}
			}
		}
		String output="";
		if (!shouldContinue)
		{
			output="too bright, recalibrate! "+markedPixelCount+" ";
		}
		else
		{
			long endTime=System.nanoTime();
			int maxBoxSize=Math.max(maxX-minX, maxY-minY);
			output=info+" max lumi="+maxLuminance+" process time= "+((endTime-startTime)/1000000)+" ms\n";
			for (int k=0;k<resultList.size();k++)
			{
				if (resultList.get(k).x<0)
					continue;
				else if (resultList.get(k).x>640)
					continue;
				else if (resultList.get(k).y<0)
					continue;
				else if (resultList.get(k).y>640)
					continue;
				output=output+resultList.get(k).x+","+resultList.get(k).y+"\n";
			}
		}
		
				
		
		dataHandler.debug(output);
	
		
		if (dataHandler!=null)
		{
			for (int i=0;i<resultList.size();i++)
			{
				dataHandler.onLaserPointDetected(resultList.get(i).x, resultList.get(i).y);
			}
		}		
		camera.addCallbackBuffer(buffer);
	}

	@Override
	public void onSurfaceTextureAvailable(SurfaceTexture surface, int width,
			int height) 
	{
		try 
		{
			//camera.setPreviewTexture(surface);
			this.surface=surface;
			startPreview(surface);
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		
	}

	@Override
	public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width,
			int height) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSurfaceTextureUpdated(SurfaceTexture surface) {
		// TODO Auto-generated method stub
		
	}

	public void startPreview() throws IOException
	{
		if (this.surface!=null)
			startPreview(this.surface);
	}
	
	public void startPreview(SurfaceTexture surface) throws IOException
	{
		if(camera != null){
	        camera.release();
	        camera = null;          
	    }
		if (camera==null)
		{
			camera=Camera.open();
			camera.setPreviewTexture(surface);
			camera.setDisplayOrientation(90);
			previewWidth=640;
			previewHeight=480;
			flagArr=new byte[previewWidth*previewHeight];
			Camera.Parameters camParams=camera.getParameters();
			//info=""+camParams.getPreviewFormat();
			camParams.setPreviewSize(previewWidth, previewHeight);
			camParams.setPreviewFormat(ImageFormat.NV21);
			
			camera.setParameters(camParams);
			int bufferSize=(int)Math.ceil(ImageFormat.getBitsPerPixel(ImageFormat.NV21)/8.0*camParams.getPreviewSize().width*camParams.getPreviewSize().height);
			camera.setPreviewCallbackWithBuffer(this);
			//camera.setPreviewCallback(this);
			
			for (int i=0;i<BUFFER_COUNT;i++)
			{
				camera.addCallbackBuffer(new byte[bufferSize]);
			}
			camera.startPreview();
			Log.v("OUT", "start");
		}
	}
	
	
	public void stopPreview()
	{
		if (camera!=null)
		{
			camera.stopPreview();
			camera.setPreviewCallbackWithBuffer(null);
			
			camera.release();
			camera=null;
			Log.v("OUT", "stopped");
		}
		 
	}
}
