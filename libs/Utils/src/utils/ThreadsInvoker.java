package utils;

import ij.IJ;

public abstract class ThreadsInvoker extends Thread {

	protected int processorsCount;
	protected boolean stopCalculation;
	protected ThreadWorker[] workers;
	protected int size;
	protected int startFrom;
	
	public boolean showProgress;
	
	public ThreadsInvoker() {
		stopCalculation = false;
		showProgress = false;
		processorsCount = Runtime.getRuntime().availableProcessors();
		workers = new ThreadWorker[processorsCount];
		startFrom = 0;
	}

	public abstract void doInitialWork();	
	public abstract void setResults();
	public abstract ThreadWorker newThread(int start, int stop, int size, boolean mainTh);

	public synchronized void showProgress() {
		notify();		
	}

	public synchronized void checkProgress() {	
		boolean active;
		int progress;
		while(true) {
			try {
				wait(100);
				
				if(showProgress) {
					progress = workers[0].progress;
					String progressmessage = "Calculation progress: " + String.valueOf(progress) + " %";
					IJ.showStatus(progressmessage);
					IJ.showProgress(progress);				
				}
				
				active = workers[0].isAlive();
				for(int th = 1; th < processorsCount; th++)
					active = active || workers[th].isAlive();
				if(!active)
					break;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void run() {	
		try {		
			doInitialWork();
			if(stopCalculation) return;

			int calculatorsPerThread = (int) Math.ceil((double)size/processorsCount);

			workers[0] = newThread(startFrom, calculatorsPerThread, size, true);
			workers[0].start();

			for(int th = 1; th < processorsCount; th++) {
				workers[th] = newThread(calculatorsPerThread*th, calculatorsPerThread*(th+1), size, false);
				workers[th].start();
			}

			checkProgress();

			setResults();
		}
		catch(Exception ex) {
			IJ.showMessage("Could not perform the calculations.\n\n" + ex.getMessage());
		}
	}


	public abstract class ThreadWorker extends Thread {

		protected int start, stop, size, count;
		boolean mainThread = false;
		public int progress;

		public ThreadWorker(int start, int stop, int size, boolean mainTh) {
			mainThread = mainTh;

			this.start = start;
			this.stop = stop;
			this.size = size;
			count = size < stop? size-start: stop-start;
		}

		public abstract void doWork(int pos, int slice); //pos is the index of the processed slice incite the current thread, slice is the actual clice number
		
		@Override
		public void run() {
			int sliceNumber;
			for(int i = 0; i < count; i++) {
				sliceNumber = i+start; //starts from 0
				doWork(i, sliceNumber);

				progress = (int) (((double)(i+1))/count*100);
				if(mainThread)
					showProgress();
			}
			showProgress();
		}
	}

}