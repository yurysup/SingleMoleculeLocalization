package utils;

import ij.IJ;

public class ProgressThread extends Thread {

	private int progress, total;
	boolean active;
	
	public ProgressThread() {
		active = false;
	}
	
	public synchronized void updateProgress() {
		progress++;
	}
	
	public synchronized void initialize(int total) {
		this.total = total;		
		active = true;
		start();
	}

	public synchronized void finalize() {
		active = false;
		progress = total-1;
		
		String progressmessage = "Progress: 100 %";
		IJ.showStatus(progressmessage);
		IJ.showProgress(1);
		System.out.println(progressmessage);
	}
	
	@Override
	public void run() {	
		while(active) {
			try {
				sleep(100);
				
				if(active) {
					double vprogress = ((double)(progress+1))/total;
					String progressmessage = "Progress: " + String.valueOf((int)(vprogress*100)) + " %";
					IJ.showStatus(progressmessage + " ... (Press ESC to Cancel)");
					System.out.println(progressmessage);				
					IJ.showProgress(vprogress);
				}
			
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
