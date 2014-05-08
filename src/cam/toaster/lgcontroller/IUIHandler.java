package cam.toaster.lgcontroller;

public interface IUIHandler {
	public void outputToUI(String output);
	public void debugArduino(String output);
	public void onThresholdChanged(int newThreshold);
}
