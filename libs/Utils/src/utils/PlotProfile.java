package utils;

import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Plot;
import ij.gui.Roi;

public class PlotProfile {
	
	private int xStart, yStart, xEnd, yEnd;
	private int w, h, size;
	private Roi roi;
	float[] pixels;
	ImageStack ims_profile;
	ImageStack ims;
	double[][] X;
	double[][] Y;			
	double[] maxY;
	double[] minY;
	private String title;
	private String direction;  // X, Y
	private boolean isXDir; 
	
	public PlotProfile(ImagePlus imp, String t, String dir) {
		title = t;
		direction = dir;
		isXDir = direction.equals("X");
		ims = imp.getStack();
		
		size = ims.getSize();
		roi = imp.getRoi();
		w = ims.getWidth();
		h = ims.getHeight();
		
		if(isXDir) {
			if(roi != null) {
				xStart = roi.getBounds().x;
				yStart = roi.getBounds().y;
				xEnd = roi.getBounds().x + roi.getBounds().width;
				yEnd = roi.getBounds().y + roi.getBounds().height;
			}
			else {
				xStart = 0;
				yStart = 0;
				xEnd = w;
				yEnd = h;
			}
		}
		else {
			if(roi != null) {
				xStart = roi.getBounds().y;
				yStart = roi.getBounds().x;
				xEnd = roi.getBounds().y + roi.getBounds().height;
				yEnd = roi.getBounds().x + roi.getBounds().width;
			}
			else {
				xStart = 0;
				yStart = 0;
				xEnd = h;
				yEnd = w;
			}
		}
			
		ims_profile = new ImageStack(500, 300);
		X = new double[size][xEnd-xStart];
		Y = new double[size][xEnd-xStart];			
		maxY = new double[size];
		minY = new double[size];		
	}
	
	public void calcProfile() {
		for(int i = 0; i < size; i++) {
			pixels = (float[]) ims.getProcessor(i+1).getPixels();
			
			for(int xIndex=xStart; xIndex<xEnd; xIndex++)
			{
				X[i][xIndex-xStart] = xIndex-xStart;
				if(isXDir) {
					for(int yIndex=yStart; yIndex<yEnd; yIndex++) 
						Y[i][xIndex-xStart] += pixels[yIndex*w+xIndex];
				}
				else {
					for(int yIndex=yStart; yIndex<yEnd; yIndex++)
						Y[i][xIndex-xStart] += pixels[xIndex*w+yIndex];						
				}
				maxY[i] = MatStatUtils.getMaxOfArray(Y[i]);
				minY[i] = MatStatUtils.getMinOfArray(Y[i]);
			}
		}
	}

	public void showProfile() {
		calcProfile();
		

		for(int i = 0; i < size; i++) {		
			Plot pw=new Plot(direction + " projection histogram","Pixels", "Counts", X[i], Y[i]);
			pw.setSize(500, 300);
			//min = getMinOfArray(minY);
			//max = getMaxOfArray(maxY);
			double[] minmax = ij.util.Tools.getMinMax(Y[i]);
			//pw.setLimits(0, xEnd-xStart, min, (max-min)*1.1+min);
			pw.setLimits(0, xEnd-xStart, minmax[0], (minmax[1]-minmax[0])*1.05+minmax[0]);
			
			ims_profile.addSlice(title + " " + direction + " projection " + String.valueOf(i+1), pw.getProcessor());
		}
		
		ImagePlus sacf_profile = new ImagePlus(title + " " + direction + " profile", ims_profile);
		sacf_profile.show();
	}

}
