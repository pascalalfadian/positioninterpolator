package com.toaster.lgcamera;

import android.graphics.Point;
import android.util.Log;

public class XYStack {
	protected Point[] stack;
	protected int idxTop;
	
	public XYStack(int size)
	{
		stack=new Point[size];
		for (int i=0;i<size;i++)
		{
			stack[i]=new Point();
		}
	}
	
	public void push(int x,int y)
	{
		try
		{
			stack[idxTop].x=x;
			stack[idxTop].y=y;
			//System.out.println(stack[idxTop].x+" "+stack[idxTop].y);
			idxTop++;
		}
		catch (ArrayIndexOutOfBoundsException e)
		{
			Log.v("camera", "daerah terangnya terlalu luas");
		}
	}
	
	public void clear()
	{
		idxTop=0;
	}
	
	public Point pop()
	{
		if (idxTop==0)
			return null;
		else
		{
			idxTop--;
			return stack[idxTop];
		}
	}
}
