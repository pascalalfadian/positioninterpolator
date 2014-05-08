package cam.toaster.lgcamera;

public interface ICameraDataHandler 
{
	public void onLaserPointDetected(int x,int y);
	public void onCalibrationCompleted();
	public void debug(String output);
}
