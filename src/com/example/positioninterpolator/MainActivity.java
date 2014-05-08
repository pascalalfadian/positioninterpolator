package com.example.positioninterpolator;

import com.toaster.arduinoandroidlib.ArduinoPacketHandler;
import com.toaster.arduinoandroidlib.ArduinoUSBCommManager;
import com.toaster.arduinoandroidlib.DebugHandler;

import cam.toaster.lgcontroller.IUIHandler;
import cam.toaster.lgcontroller.LaserGunController;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.TextureView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity implements OnClickListener,IUIHandler,DebugHandler,ArduinoPacketHandler
{
	protected TextureView texView;
	protected TextView camOutputView;
	protected EditText editTextThreshold; 
	protected Button btnCalibrate;
	protected Button btnSetThreshold;
	protected LaserGunController controller;
	protected EditText editTextMaxMarkedPixel;
	protected Button btnSetMaxMarkedPixel;
	protected TextView arduinoOutputView;
	protected Button btnArduinoConnect;
	protected ArduinoUSBCommManager arduinoTest;
	protected String debugTextValue;

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		View mainView=this.getLayoutInflater().inflate(R.layout.activity_main, null);
		texView=(TextureView)mainView.findViewById(R.id.camTextureView);
		camOutputView=(TextView)mainView.findViewById(R.id.camOutputTextView);
		btnCalibrate=(Button)mainView.findViewById(R.id.btnAutoThreshold);
		btnSetThreshold=(Button)mainView.findViewById(R.id.btnSetThreshold);
		editTextThreshold=(EditText)mainView.findViewById(R.id.editTextThreshold);
		btnSetMaxMarkedPixel=(Button)mainView.findViewById(R.id.btnSetMaxMarkedPixel);
		editTextMaxMarkedPixel=(EditText)mainView.findViewById(R.id.editTextMaxMarkedPixel);
		arduinoOutputView=(TextView)mainView.findViewById(R.id.arduinoOutputTextView);
		btnArduinoConnect=(Button)mainView.findViewById(R.id.btnArduinoConnect);
		mainView.setKeepScreenOn(true);
		setContentView(mainView);
		
		//controller=new LaserGunController(this,this);
		controller=new LaserGunController(this,this);
		//arduinoTest=new ArduinoUSBCommManager(this, this, this);
		
		btnCalibrate.setOnClickListener(this);
		btnSetThreshold.setOnClickListener(this);
		btnSetMaxMarkedPixel.setOnClickListener(this);
		btnArduinoConnect.setOnClickListener(this);
		//editTextThreshold.setText(Integer.toString(controller.getThreshold()));
		
	}

	public void setOutput(String output)
	{
		camOutputView.setText(output);
	}
	
	
	
	@Override
	protected void onPause() 
	{
		//controller.onPause();
		super.onPause();
		//arduinoTest.onPauseHandler();
		controller.onPause();
	}

	@Override
	protected void onResume() 
	{
		
		//controller.onResume();
		super.onResume();
		//arduinoTest.onResumeHandler();
		texView.setSurfaceTextureListener(controller.getCamImageProvider());
		controller.onResume();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onClick(View v) 
	{
		Button obj=(Button)v;
		if (obj==btnCalibrate)
		{
			controller.calibrateThreshold();
		}
		else if (obj==btnSetThreshold)
		{
			controller.setThreshold(Integer.parseInt(editTextThreshold.getText().toString()));
		}
		else if (obj==btnSetMaxMarkedPixel)
		{
			controller.setMaxMarkedPixel(Integer.parseInt(editTextMaxMarkedPixel.getText().toString()));
		}
		else if (obj==btnArduinoConnect)
		{
			//controller.connectArduino();
		}
	}

	@Override
	public void outputToUI(String output) 
	{
		camOutputView.setText(output);	
		//arduinoOutputView.setText(debugArduinoText);
	}

	@Override
	public void onThresholdChanged(int newThreshold) 
	{
		editTextThreshold.setText(Integer.toString(newThreshold));	
	}

	@Override
	public void debugArduino(String output) {
		 //arduinoOutputView.setText("ABC");
		final String debugArduinoText=output;
		runOnUiThread(new Runnable() 
		{
			public void run() {
				//outputView.setText("EDEF");
			    arduinoOutputView.setText(debugArduinoText);
			}
		});
		
	}

	@Override
	public void onArduinoPacket(int[] packet) {
		debugTextValue="";
		for (int i=0;i<2;i++)
		{
			debugTextValue=debugTextValue+Integer.toString(packet[i])+" ";
		}
		runOnUiThread(new Runnable() 
		{
			
			@Override
			public void run() 
			{
				arduinoOutputView.setText(debugTextValue);
				
			}
		});
		
	}

	@Override
	public void setText(String text) {
		// TODO Auto-generated method stub
		
	}
}
