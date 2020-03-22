package utils;
/**
 * MultiThreading copyright 2007 Stephan Preibisch 
 *  
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 2 
 * as published by the Free Software Foundation. 
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
 * GNU General Public License for more details. 
 * 
 * You should have received a copy of the GNU General Public License 
 * along with this program; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA. 
 */ 


/**
 * Multithreader utility class for convenient multithreading of ImageJ plugins 
 *  
 * @author Stephan Preibisch 
 * @author Michael Doube 
 *  
 * @see <p> 
 *      <a href="http://repo.or.cz/w/trakem2.git?a=blob;f=mpi/fruitfly/general/MultiThreading.java;hb=HEAD" 
 *      >http://repo.or.cz/w/trakem2.git?a=blob;f=mpi/fruitfly/general/ 
 *      MultiThreading.java;hb=HEAD</a> 
 */ 
public class Multithreader { 
	public static void startTask(Runnable run) { 
		Thread[] threads = newThreads(); 

		for (int ithread = 0; ithread < threads.length; ++ithread) 
			threads[ithread] = new Thread(run); 
		startAndJoin(threads); 
	} 

	public static void startTask(Runnable run, int numThreads) { 
		Thread[] threads = newThreads(numThreads); 

		for (int ithread = 0; ithread < threads.length; ++ithread) 
			threads[ithread] = new Thread(run); 
		startAndJoin(threads); 
	} 

	public static Thread[] newThreads() {
		int nthread = Runtime.getRuntime().availableProcessors();
		return new Thread[nthread]; 
	} 

	public static Thread[] newThreads(int numThreads) { 
		return new Thread[numThreads]; 
	} 

	public static void startJob(Thread[] threads) { 
		for (int ithread = 0; ithread < threads.length; ++ithread) { 
			threads[ithread].setPriority(Thread.NORM_PRIORITY); 
			threads[ithread].start(); 
		} 
	} 
	
	public static void startAndJoin(Thread[] threads) { 
		for (int ithread = 0; ithread < threads.length; ++ithread) { 
			threads[ithread].setPriority(Thread.NORM_PRIORITY); 
			threads[ithread].start(); 
		} 

		try { 
			for (int ithread = 0; ithread < threads.length; ++ithread) 
				threads[ithread].join(); 
		} catch (InterruptedException ie) { 
			throw new RuntimeException(ie); 
		} 
	} 
}

/*private ImageStack thresholdStack(ImagePlus imp,final double threshold){
final int w=imp.getWidth();
final int h=imp.getHeight();
final int d=imp.getStackSize();
final ImageStack stack=imp.getImageStack();
final ImageStack stack2=new ImageStack(w,h,d);
final AtomicInteger ai=new AtomicInteger(1);
Thread[] threads=Multithreader.newThreads();
for (int thread=0; thread < threads.length; thread++) {
  threads[thread]=new Thread(new Runnable(){
    public void run(){
      for (int z=ai.getAndIncrement(); z <= d; z=ai.getAndIncrement()) {
        ImageProcessor ip=stack.getProcessor(z);
        ByteProcessor bp=new ByteProcessor(w,h);
        for (int y=0; y < h; y++) {
          for (int x=0; x < w; x++) {
            final double pixel=(double)ip.get(x,y);
            if (pixel > threshold) {
              bp.set(x,y,255);
            }
else {
              bp.set(x,y,0);
            }
          }
        }
        stack2.setPixels(bp.getPixels(),z);
      }
    }
  }
);
}
Multithreader.startAndJoin(threads);
return stack2;
}*/
