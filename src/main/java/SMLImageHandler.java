
import java.awt.Frame;
import java.awt.Rectangle;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import utils.INIFile;
import utils.MatStatUtils;
import utils.Multithreader;
import utils.ProgressFrame;
import fit.FitResults;
import fit.Gauss2DFit;
import fit.Gauss2DFit.TFCType;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.Plot;
import ij.gui.PlotWindow;
import ij.gui.Roi;
import ij.measure.ResultsTable;
import ij.plugin.frame.PlugInFrame;
import ij.plugin.frame.RoiManager;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;
import ij.text.TextWindow;

public class SMLImageHandler {
	
	protected ImageProcessor ipp;
	protected ImagePlus imp;
	protected ImagePlus imp_work = null;	
	protected ImagePlus imp_thresholded = null;	
	protected ImagePlus imp_thresholded_cropped = null;
	protected ImageStack ims_work = null;
	protected ImageProcessor ipp_work = null;	
	protected ImageProcessor ipp_work_th = null;	
	protected ImageProcessor ipp_work_th_cropped = null;

	protected int w;
	protected int h;
	protected int selected_slice = 0;

	static protected RoiManager rm = null;
	//protected ResultsTable rt = new ResultsTable();
	protected ResultsTable rt = ij.measure.ResultsTable.getResultsTable();
	protected ResultsTable rts = null;
	protected ResultsTable rtd = null;

	public Gauss2DFit GaussXYFit = null;

	private String progressmessage;	
	private ProgressFrame fitProgress;
	private ProgressFrame selectionProgress;	
	
	protected FitResults fitResults = null;
	SuperResPoint[] superResPoints = null; 

	public int edge_increment = 3;  //enlarge spot by 3 pixels
	public int minSpotRectSize = 12;  //min size of fit rectangle
	public int maxSpotRectSize = 500;  //max size of fit rectangle	
	public double minCircularity = 0.5;  	
	public double maxChi2Value = 100;  	//max value of Chi2; 
	public int multiplier = 4;
	public boolean removeOverlappedROI = true;
	public int ROIsCount = 0;
	public int super_imagePointsCount;
	
	Roi[] selectedRois = null;

	public double minCriterion;
	public double minFWHM_X;
	public double minFWHM_Y;
	public double minGauss;
	public double maxCriterion;
	public double maxFWHM_X;
	public double maxFWHM_Y;
	public double maxGauss;

	public SMLImageHandler(ImageProcessor ipp, ImagePlus imp) {

		this.ipp = ipp;
		this.imp = imp;

		fitProgress = new ProgressFrame();
		selectionProgress = new ProgressFrame();
		
		GaussXYFit = new Gauss2DFit();
		
		GaussXYFit.fitOffset=true;
		GaussXYFit.weightedFit=true;	
		GaussXYFit.maxNumberOfIterations=50;
	}

	public ProgressFrame getFitProgress() {
		return fitProgress;
	}
	
	public ProgressFrame getSelectionProgress() {
		return selectionProgress;
	}
	
	public void linkToActiveImageWindow() {
		imp = IJ.getImage();
		ipp = imp.getProcessor();
		imp_work = imp;
		fitResults = null;
		ipp_work = null;
	}
	
	public boolean checkSourceImage() {	
		if(imp_work == null) 
			return false;
		int type = imp_work.getType();
		if(type != ImagePlus.GRAY8 && type != ImagePlus.GRAY16) {
			IJ.showMessage("Type of the image is not 8 or 16 bit grayscale image.\nPlease convert the image into 8- or 16-bit image to proceed." );
			return false;
		}
		return true;
	}

	//////////////////////////////////////////////////
	//localize section
	//////////////////////////////////////////////////
	protected void createThresholdedImage(ImagePlus imp_th) {	
		IJ.setAutoThreshold(imp_th, "Otsu dark");
		IJ.run(imp_th, "Threshold...", "");
		imp_th.show();
		imp_th.updateAndDraw();
	}

	protected boolean applyThreshold(boolean allFrames) {
		if(allFrames) {
			selected_slice = 0;			
			Roi r = imp_work.getRoi();
			if(r != null) {
				imp_thresholded_cropped = imp_work.duplicate();
				imp_thresholded_cropped.setTitle(imp_work.getTitle() + "_thresholded");
				createThresholdedImage(imp_thresholded_cropped);
				
				imp_work.deleteRoi();
				imp_thresholded = imp_work.duplicate();
				imp_thresholded.setTitle(imp_work.getTitle() + "_thresholded");				
			}
			else {
				imp_thresholded = imp_work.duplicate();
				imp_thresholded.setTitle(imp_work.getTitle() + "_thresholded");				
				createThresholdedImage(imp_thresholded);				
			}
			
			if(imp_thresholded == null) {
				IJ.showMessage("Could not duplicate the image for thresholding. Please reopen the plugin and import data again.");
				return false;
			}
		
			return true;
		}
		else {
			Roi r = imp_work.getRoi();
			
			//SEE HERE FOR STACK
			if(imp_work.getImageStackSize() > 1) {
				ims_work = imp_work.getStack();
				if(ims_work != null) {
					selected_slice = imp_work.getSlice();
					ipp_work = ims_work.getProcessor(selected_slice);
					ipp_work_th = ipp_work.duplicate();
				}
			}
			else {
				selected_slice = 0;
				ipp_work = ipp;
				ipp_work_th = ipp.duplicate();
			}
			
			if(ipp_work_th == null) {
				IJ.showMessage("Could not duplicate the image for thresholding. Please reopen the plugin and import data again.");
				return false;
			}

			if(r != null) {
				ipp_work_th_cropped = ipp_work_th.duplicate();
				ipp_work_th_cropped.setRoi(r);
				ipp_work_th_cropped = ipp_work_th_cropped.crop();
				
				String title = imp_work.getTitle();
				if(imp_work.getImageStackSize() > 1)
					title += "_slice_" + (selected_slice + 1);
				title += "_thresholded";
				imp_thresholded_cropped = new ImagePlus(title, ipp_work_th_cropped);
				createThresholdedImage(imp_thresholded_cropped);
				
				imp_work.deleteRoi();
				imp_thresholded = new ImagePlus(imp.getTitle() + "_thresholded", ipp_work_th);				
			}
			else {
				imp_thresholded = new ImagePlus(imp.getTitle() + "_thresholded", ipp_work_th);
				createThresholdedImage(imp_thresholded);
			}
			
			return true;
		}
	}

	protected boolean selectParticles() {
		if(imp_thresholded == null) {
			IJ.showMessage("Please perform thresholding of the image initially");
			return false;
		}
		
		IJ.run("Set Scale...", "distance=0 known=0 pixel=1 unit=pixel global");

		//clear previous selection before running Analyze Particles... second time
		rm = RoiManager.getInstance();
		if(rm != null)  {
			selectedRois = null;
			
			if(rm.getCount() > 0) {
				rm.runCommand("Delete");
				if(rm.getCount() > 0)
					rm.runCommand("Delete");
			}
		}

	 	String params = "size=" + minSpotRectSize + "-" + maxSpotRectSize + " circularity=" + minCircularity + "-1.00 show=Nothing exclude add stack";
		
		if(imp_thresholded_cropped != null) {
			ImageProcessor p = imp_thresholded_cropped.getProcessor();
			if(p != null) {
				double min_th = p.getMinThreshold();
				double max_th = p.getMaxThreshold();
				if(max_th < 0)
					max_th = p.getMax();
				imp_thresholded_cropped.hide();
				imp_thresholded.getProcessor().setThreshold(min_th, max_th, ImageProcessor.BLACK_AND_WHITE_LUT);
				imp_thresholded.show();
				imp_thresholded.updateAndDraw();
			}
		}
	 	
		IJ.run(imp_thresholded, "Analyze Particles...", params);
		
		rm = RoiManager.getInstance();  
		if(rm != null) {
			ROIsCount = rm.getCount();
			selectedRois = rm.getRoisAsArray();
		}
		 
		return true;
	}

	protected boolean getSelectionStatus() {
		rm = RoiManager.getInstance();  
		if(imp_thresholded == null || rm == null) {
			IJ.showMessage("Please perform selection of particles initially");
			return false;
		}
		if(rm.getCount() == 0) {
			IJ.showMessage("Could not get selected ROIs. ROI manager was probably closed. Please repeat thresholding and localization again or reopen the plugin and repeat all actions.");
			return false;
		}
		return true;		
	}
	
	protected boolean applySelection(int selectionMode, int frameFrom, int frameTo) {
		if(getSelectionStatus() == false)
			return false;
		
		ImageProcessor p = null;
		
		if(selectionMode < 2)
			p = imp_thresholded.getStack().getProcessor(1);
		else
			p = imp_thresholded.getProcessor();
		if(p == null) {
			IJ.showMessage("Can not open the thresholded image. Please open the source image again and repeat thresholding.");
			return false;
		}

		int max_threshold = (int) p.getMaxThreshold();
		if(max_threshold < 0 || imp_thresholded_cropped != null)
			max_threshold = (int) p.getMax();
		imp_thresholded.hide();  //close();

		IJ.showStatus("Selection is applied...");
		
		ApplySelection applySelectionThread = new ApplySelection(rm, selectedRois, imp_work, selectionProgress, selected_slice, selectionMode, frameFrom, frameTo);
		applySelectionThread.setOptions(removeOverlappedROI, edge_increment, max_threshold);
		applySelectionThread.start();
		
		return true;		
	}
	
	/**
	 * Select ROIs, add them to RoiManager instance for further fitting.
	 * @return true if succeeded
	 */
	protected boolean selectParticlesByLocalMax(int fittingRad, int minThreshold, int maxThreshold) {
		int width = imp_work.getWidth();
		int height = imp_work.getHeight();
		rm = new RoiManager();
		if(rm != null)  {
			selectedRois = null;
			
			if(rm.getCount() > 0) {
				rm.runCommand("Delete");
				if(rm.getCount() > 0)
					rm.runCommand("Delete");
			}
		}
		LocalizationHelper localizationHelper = new LocalizationHelper(height, width, fittingRad, minThreshold, maxThreshold);
        ArrayList<LocalMaximum> maximums = new ArrayList<LocalMaximum>();
        ims_work = imp_work.getStack();
		if(ims_work != null) {
			int stackSize = ims_work.getSize();
			//selectionProgress.initialize(stackSize);
			try {
				for (int stackIterator = 0; stackIterator < stackSize; stackIterator++) {
					//ImageProcessors are 1-based in stack
					ipp_work = ims_work.getProcessor(stackIterator + 1);
					ArrayList<LocalMaximum> maximumsForSlice = getMaximumsForSlice(localizationHelper, ipp_work);
					maximums.addAll(getMaximumsForSlice(localizationHelper, ipp_work));
					for (LocalMaximum maximum : maximumsForSlice) {
			        	// maximums needs to shift to the left upper corner of rectangle, that's why -fitRad
			        	Rectangle r = new Rectangle(maximum.getRow() - fittingRad, maximum.getColumn() - fittingRad, fittingRad*2, fittingRad*2);
			        	Roi roi = new Roi(r);
						roi.setImage(imp_work);
						roi.setPosition(stackIterator+1);
						rm.addRoi(roi);
			        	//rm.add(imp_work, new Roi(r), stackIterator);
			        }
					//selectionProgress.updateProgress();
				}
			} catch (Exception e) {
				IJ.error("ERROR", e.getMessage());
			}
			//selectionProgress.finalize(stackSize);
		}
		ROIsCount = rm.getCount();
		IJ.showStatus("Selection is applied...");
		//selectionProgress.updateProgress();
		//selectionProgress.finalize(1);
		selectedRois = rm.getRoisAsArray();	
		//rm.runCommand("Show All");
		return true;
	}
	
	/**
	 * Allows to get list of 'LocalMaximum' (local maximums) for particular slice
	 * @param localizationHelper
	 * @param imageProcessor responsible for that slice
	 * @return list of 'LocalMaximum'
	 * @throws Exception if can't get pixels for slice
	 */
	private ArrayList<LocalMaximum> getMaximumsForSlice(LocalizationHelper localizationHelper, ImageProcessor imageProcessor) throws Exception {
		int[][] pixels = imageProcessor.getIntArray();
		if(pixels == null) {
			IJ.showMessage("Could not get pixels array");
			throw new Exception("Could not get pixels array");
		}
		return localizationHelper.getLocalMaximumsForSingleImage(pixels);
	}
	
	protected void applyThresholdToRois() {
		Roi[] rois = rm.getRoisAsArray();
		//getResult("Area",0);
		IJ.run("Set Measurements...", "area mean standard min centroid center median display redirect=None decimal=3");
		rm.runCommand("Measure"); //add measurements to Results
		//run("Distribution...", "parameter=StdDev automatic"); //show histogram
		//ResultsTable table = ij.measure.ResultsTable.getResultsTable(); //getResults
		//System.out.println(table.getHeadings()[3]);
		
		
		//IJ.run("")
		//rm.select(1);
		//rm.runCommand("Delete");
		
		
		/*ImageStack ims = imp_work.getStack();
		int[] maximums = new int[rois.length];
		int max = 0;
		int min = Integer.MAX_VALUE;
		for (int iter = 0; iter < rois.length; iter++) {
			Roi roi = rois[iter];
			Rectangle rect = roi.getBounds();
			ImageProcessor ip = ims.getProcessor(roi.getPosition());
			System.out.println(roi.getPosition());
			int x = rect.x + rect.width / 2;
			int y = rect.y + rect.height / 2;
			maximums[iter] = ip.get(x, y);
			if (maximums[iter] > max) {
				max = maximums[iter];
			}
			if (maximums[iter] < min) {
				min = maximums[iter];
			}
		}
		int bins = 20;
		
		ImagePlus planeWindow = WindowManager.getImage("Maximums projection histogram");
		if (planeWindow!=null)
			planeWindow.hide();

		double[] xValues = new double[bins];
		double[] yValues = {1.0, 2.0, 3.0, 4.0};
		Plot plot = new Plot("Maximum's intensities histogram", "intensity","count");
		plot.add("bars", xValues, yValues);
		plot.show();*/
		
	}
	
		//////////////////////////////////////////////////////////////////////////////////////////////////////////
	//Fit procedures
	//////////////////////////////////////////////////////////////////////////////////////////////////////////
	private void resizingROI(Rectangle r, int trial) {
		switch(trial) {
			case 1: r.height--; break;
			case 2: r.width--; break;
			case 3: r.x++; break;
			case 4: r.y++; break;
			case 5: r.height--; break;
			case 6: r.width--; break;
			case 7: r.x++; break;
			case 8: r.y++; break;
		}
	}
	//---------------------------------------------------------------------------------------------------------	
	public boolean setFitSettings(String TFC, boolean weighedFit, String maxTFC)
	{
		try {
			maxChi2Value = Double.parseDouble(maxTFC);
		}
		catch(Exception ex) {
			IJ.showMessage("Could not convert the field Max fit criterion value to the number");
			return false;
		}
		
		GaussXYFit.activeTFC = Gauss2DFit.TFCType.ValueForString(TFC);
		GaussXYFit.weightedFit = weighedFit;	
		//GaussXYFit.estimateBgWeight = estimateBgWeight;
		
		return true;
	}
	//---------------------------------------------------------------------------------------------------------
	
	public boolean checkROIManagerBeforeFit()
	{
		rm = RoiManager.getInstance();
		if(rm == null)
			IJ.showMessage("Could not get ROIs. Please perform localization.");

		if(imp_work == null)
		{
			IJ.showMessage("There is no access to the image window. Repeat localization again please.");
			return false;
		}
		
		return true;
	}
	//---------------------------------------------------------------------------------------------------------
	
	public boolean checkROIBeforeFitWithoutMessage()
	{
		rm = RoiManager.getInstance();
		if(rm == null)
			return false;

		if(imp_work == null)
			return false;

		return true;
	}	
	//---------------------------------------------------------------------------------------------------------
	
	public boolean doGaussFit()
	{
		if(checkROIManagerBeforeFit())
		{
			w = imp_work.getWidth();
			h = imp_work.getHeight();
			Roi[] rois = rm.getRoisAsArray();
			fitResults = new FitResults(rois.length);
			
			if(imp_work.getImageStackSize() > 1) { 
				ims_work = imp_work.getStack();
				doFit(ims_work, fitResults);
				//return checkFitResults();
				return true;
			}
			else {
				ipp_work = imp_work.getProcessor();
				if(ipp_work == null) {
					IJ.showMessage("Could not get image processor");
					return false;
				}
				else {
					doFit(ipp_work, rois, fitResults);
					return checkFitResults();
				}
			}
		}
		else
			return false;
	}
	//---------------------------------------------------------------------------------------------------------	
	void doFit(ImageProcessor ip, Roi[] rois, FitResults fitResults)  //for a single image
	{
		Roi roi;
		double fit[];
		int maxTrials = 8;	
		int trial;
		Rectangle r = null;
		
		for(int i = 0; i < rois.length; i++)
		{
			roi = rois[i];
			fit = null;
			trial = 0;
			r = roi.getBounds();
			int initX = 0;
			int initY = 0;
			
			while(trial < maxTrials && fit == null)  {  //try maxTrials iterations with decreasing size of ROI
				try
				{
					fit = GaussXYFit.fit(r, ip);
					initX = r.x + r.width / 2;
					initY = r.y + r.height / 2;
				}
				catch(Exception ex) {
					fit=null;
					resizingROI(r, trial);
					trial++;
			    	System.out.println("Fit was failed: " + r.toString() + ": " + ex.getMessage());
					//IJ.showMessage(ex.getMessage());
			    	//return;
				}
			}
			
			StoreFitResult(fitResults, GaussXYFit, i, 1, roi.getName(), fit, initX, initY);
			
			progressmessage="Analysing spot " + String.valueOf(i);
			IJ.showStatus(progressmessage);
		}
	}
	//---------------------------------------------------------------------------------------------------------
	void doFit(final ImageStack ims, final FitResults fitResults)  //for stack
	{
		final int maxTrials = 8;		
		final int count = rm.getCount();
		//total = count;
		//progress = 0;
		System.out.println("ROI count: " + count);
		IJ.showStatus("Fit is started...");
		
		final boolean fitOffset = GaussXYFit.fitOffset;
		final boolean weightedFit = GaussXYFit.weightedFit;
		//final boolean estimateBgWeight = GaussXYFit.estimateBgWeight;
		final TFCType activeTFC = GaussXYFit.activeTFC;
		final int maxNumberOfIterations = GaussXYFit.maxNumberOfIterations;

		Thread t = new Thread(new Runnable() {	//must be run in separate thread from progressTh. Otherwise join will block progressTh.  

			public void run() {
				fitProgress.initialize(count);				
				
				final AtomicInteger ai=new AtomicInteger(0);
				Thread[] threads=Multithreader.newThreads();
				for (int thread=0; thread < threads.length; thread++) {
					threads[thread]=new Thread(new Runnable() {
						public void run(){

							for (int roi_no = ai.getAndIncrement(); roi_no < count; roi_no = ai.getAndIncrement()) {
								double fit[] = null;

								Gauss2DFit gaussFit = new Gauss2DFit(); 
								gaussFit.fitOffset=fitOffset;
								gaussFit.weightedFit=weightedFit;	
								gaussFit.activeTFC = activeTFC;
								gaussFit.maxNumberOfIterations = maxNumberOfIterations;
								//gaussFit.estimateBgWeight = estimateBgWeight;

								//System.out.println("ROI: " + roi_no);

								Roi roi = rm.getRoi(roi_no);
								if(roi == null)
									System.out.println("ROI null");
								else {
									int trial = 0;
									Rectangle r = roi.getBounds();
									int initX = 0;
									int initY = 0;
									int pos = roi.getPosition();
									ImageProcessor ip = ims.getProcessor(pos);
									if(ip == null || r == null)
										System.out.println("Processor null");
									else {
										while(trial < maxTrials && fit == null)  {  //try maxTrials iterations with decreasing size of ROI
											try
											{
												fit = gaussFit.fit(r, ip);
											}
											catch(Exception ex) {
												fit=null;
												resizingROI(r, trial);
												trial++;
												System.out.println("Fit was failed: " + r.toString() + ": " + ex.getMessage());
											}
										}
										initX = r.x + r.width / 2;
										initY = r.y + r.height / 2;
									}
									StoreFitResult(fitResults, gaussFit, roi_no, pos, roi.getName(), fit, initX, initY);
								}
								if(!fitProgress.updateProgress())
									break;
							}
						}
					} );
				}

				Multithreader.startAndJoin(threads);
				
				int fitOK = 0;
				
				minCriterion = Float.MAX_VALUE;
				minFWHM_X = Float.MAX_VALUE;
				minFWHM_Y = Float.MAX_VALUE;	
				minGauss = Float.MAX_VALUE;
				
				maxCriterion = 0;
				maxFWHM_X = 0;
				maxFWHM_Y = 0;
				maxGauss = 0;
				
				for(int i=0; i<fitResults.getCount(); i++)  {
					double[] fitRes = fitResults.getFit(i);
					if(fitRes != null) {
						if(minCriterion > fitRes[6])
							minCriterion = fitRes[6];
						
						if(minFWHM_X > fitRes[2])
							minFWHM_X = fitRes[2];
						
						if(minFWHM_Y > fitRes[3])
							minFWHM_Y = fitRes[3];
						
						if(maxCriterion < fitRes[6])
							maxCriterion = fitRes[6];
						
						if(maxFWHM_X < fitRes[2])
							maxFWHM_X = fitRes[2];
						
						if(maxFWHM_Y < fitRes[3])
							maxFWHM_Y = fitRes[3];
						
						if(maxGauss < fitRes[4])
							maxGauss = fitRes[4];
						
						if(minGauss > fitRes[4])
							minGauss = fitRes[4];
						
						fitOK++;
					}
				}
				fitProgress.finalize(fitOK);
			}
		});
		t.start();
		
		/*Roi roi;
		double fit[];
		int maxTrials = 8;	
		int trial;
		Rectangle r = null;
		int current_slice = 0; 
		ImageProcessor ip = null;
		
		for(int i = 0; i < rm.getCount(); i++)
		{
			roi = rm.getRoi(i);
			fit = null;
			trial = 0;
			r = roi.getBounds();
			
			int pos = roi.getPosition();
			if(pos > 0 && pos != current_slice) {
				ip = ims_work.getProcessor(pos);
			}
			current_slice = pos;
			
			while(trial < maxTrials && fit == null)  {  //try maxTrials iterations with decreasing size of ROI
				try
				{
					fit = getGaussXYFit().fit(r, ip);
				}
				catch(Exception ex) {
					fit=null;
					resizingROI(r, trial);
					trial++;
			    	System.out.println("Fit was failed: " + r.toString() + ": " + ex.getMessage());
					//IJ.showMessage(ex.getMessage());
			    	//return;
				}
			}
			
			StoreFitResult(fitResults, getGaussXYFit(), i, pos, roi.getName(), fit);

			progressmessage="Analysing spot " + String.valueOf(i);
			IJ.showStatus(progressmessage);
		}*/
	}
	//---------------------------------------------------------------------------------------------------------
	
	protected void doSingleGaussFit()
	{
		if(checkROIManagerBeforeFit()) {
			w = imp_work.getWidth();
			h = imp_work.getHeight();
			
			Roi[] selrois = rm.getSelectedRoisAsArray();
			if(selrois.length != 1)
			{  
				IJ.showMessage("Please select just one emitter");
				return;
			}	 

			ImageProcessor activeProc = null;
			Roi roi = selrois[0];
			int pos = roi.getPosition();
			if(imp_work.getImageStackSize() > 1 && pos > 0) 
				activeProc = imp_work.getStack().getProcessor(pos);
			else
				activeProc = imp_work.getProcessor();
			
			if(activeProc == null)
			{  
				IJ.showMessage("There is no access to the image. Please open the image if it was closed then link the plugin to it.");
				return;
			}

			double[] fit = null;
			Rectangle r = roi.getBounds();
			//Roi mask = selectedRois[pos];
			int initX = 0;
			int initY = 0;
			try
			{
				fit = GaussXYFit.fit(r, activeProc);
				initX = r.x + r.width / 2;
				initY = r.y + r.height / 2;
			}
			catch(Exception ex) {
				fit=null;
				System.out.println("Fit was failed: " + ex.getMessage());
				IJ.showMessage(ex.getMessage());
				return;
			}
			
			Roi[] rois = rm.getRoisAsArray();
			if(fitResults == null || fitResults.getCount() != rois.length)
				fitResults = new FitResults(rois.length);

			int index;
			for(index = 0; index < rois.length; index++)
				if(roi.equals(rois[index]))
					break;

			StoreFitResult(fitResults, GaussXYFit, index, roi.getPosition(), roi.getName(), fit, initX, initY);

			showSelectedFitResults();
			showResultsInStatus(fit);
		}
	}
	//---------------------------------------------------------------------------------------------------------
	
	// x, y - initial coordinates of ROI center (to filter out shifted maximums)
	void StoreFitResult(FitResults container, Gauss2DFit fitObject, int index, int position, String fitName, double[] fit, int x, int y)
	{
		boolean is_good_fit = (fit != null); 
		is_good_fit = is_good_fit && Math.abs(fit[0] - x) < 2 && Math.abs(fit[1] - y) < 2 && fit[0] > 0 && fit[0]<w && fit[1]>0 && fit[1]<h;//for means
		is_good_fit = is_good_fit && fit[2]>1 && fit[2]<w/2 && fit[3]>1 && fit[3]<h/2;//for FWHM
		//if(fitObject.activeTFC == TFCType.ChiSquare && fitObject.weightedFit)
		is_good_fit = is_good_fit && fit[6]<maxChi2Value;

		container.setName(fitName, index);	 
		container.setSlice(position, index);
		
		if(is_good_fit)
		{ //if good X and Y fit..
			container.setFit(fit, index);

			double[][] projection;

			projection = fitObject.GetMeasuredXProjection();
			container.setXdataX(projection[0], index);
			container.setYdataX(projection[1], index);

			projection = fitObject.GetMeasuredYProjection();
			container.setXdataY(projection[0], index);
			container.setYdataY(projection[1], index);

			double[] parameters = new double[fitObject.ParametersCount];
			for(int pIndex=0; pIndex<parameters.length; pIndex++)
				parameters[pIndex] = fit[pIndex];

			projection = fitObject.GetFittedXProjection(parameters, 100);
			container.setXfitX(projection[0], index);
			container.setYfitX(projection[1], index);

			projection = fitObject.GetFittedYProjection(parameters, 100);
			container.setXfitY(projection[0], index);
			container.setYfitY(projection[1], index);
		}
		else
		{
			container.setFit(null, index);
			container.setXdataX(null, index);
			container.setYdataX(null, index);
			container.setXdataY(null, index);
			container.setYdataY(null, index);
			container.setXfitX(null, index);
			container.setYfitX(null, index);
			container.setXfitY(null, index);
			container.setYfitY(null, index);
		}
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////	
	//Procedures for displaying results	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////	
	void addResultsTableRecord(ResultsTable rt, String name, int position, double[] fit)
	{
		rt.incrementCounter();
		rt.addLabel(name);
		rt.addValue("Slice", position);

		if(fit==null)
			return;  

		rt.addValue("X center",fit[0]);
		rt.addValue("Y center",fit[1]);
		rt.addValue("X FWHM",fit[2]);
		rt.addValue("Y FWHM",fit[3]);	
		rt.addValue("Gauss level",fit[4]);
		rt.addValue("Bg level",fit[5]);
		rt.addValue(GaussXYFit.activeTFC.toString(),fit[6]);
		rt.addValue("ML lambda",fit[7]);
	} // addResults
	//---------------------------------------------------------------------------------------------------------
	
	void showResultsInStatus(double[] fit)
	{
		DecimalFormat formatter = (DecimalFormat)NumberFormat.getInstance();
		formatter.setMaximumFractionDigits(2);
		formatter.setGroupingUsed(false);
		DecimalFormatSymbols dottedDouble = formatter.getDecimalFormatSymbols();
		dottedDouble.setDecimalSeparator('.');
		formatter.setDecimalFormatSymbols(dottedDouble);

		String message="Fit:";
		message += " XY [" + formatter.format(fit[0]) + ", " + formatter.format(fit[1]) + "]";
		message += " FWHM [" + formatter.format(fit[2]) + ", " + formatter.format(fit[3]) + "]";		
		message += GaussXYFit.activeTFC.toString() + ": " + formatter.format(fit[6]);		
		IJ.showStatus(message);		
	}
	//---------------------------------------------------------------------------------------------------------
	
	protected void showFitResults()
	{
		rt.reset();

		if(fitResults == null)
		{	  
			IJ.showMessage("Nothing to show");
			return;
		}	

		for(int i=0; i<fitResults.getCount(); i++)
		{
			if(fitResults.getFit(i)==null)
				continue;

			addResultsTableRecord(rt, fitResults.getName(i), fitResults.getSlice(i), fitResults.getFit(i));
		}

		rt.show("Results");
	}
	//---------------------------------------------------------------------------------------------------------
	protected void showSelectedFitResults()
	{
		if(rts == null)
			rts = new ResultsTable();	  
		rts.reset();

		if(rm == null)
			return;

		if(fitResults == null)
		{	  
			IJ.showMessage("Nothing to show");
			return;
		}

		int[] indexes = rm.getSelectedIndexes();
		if(indexes.length == 0)
		{
			IJ.showMessage("Please perform selection initially");
			return;
		}
		else if(indexes.length > 1) {
			IJ.showMessage("Please select just one emitter");
			return;			
		}
		
		int index = indexes[0];	
		if(index >= 0)
		{
			if(fitResults.getFit(index) == null)
				IJ.showMessage("Fit was failed or results were out of range (physically not acceptable or criterion value was more than the entered limit)");
			else {
				addResultsTableRecord(rts, fitResults.getName(index), fitResults.getSlice(index), fitResults.getFit(index));
				rts.show("Single Emitter Fit Results");
				DisplayResult2DGraphs(fitResults, index);
			}
		}
	}
	//---------------------------------------------------------------------------------------------------------
	private void DisplayResult2DGraphs(FitResults results, int rsltIndex)
	{
		//X projection graph
		DisplayGraph("X", results.getXdataX(rsltIndex), results.getYdataX(rsltIndex), results.getXfitX(rsltIndex), results.getYfitX(rsltIndex));

		//Y projection graph
		DisplayGraph("Y", results.getXdataY(rsltIndex), results.getYdataY(rsltIndex), results.getXfitY(rsltIndex), results.getYfitY(rsltIndex));
	}	
	//---------------------------------------------------------------------------------------------------------		
	private void DisplayGraph(String dirName, double[] xMeasured, double[] yMeasured, double[] xFitted, double[]yFitted)
	{
		ImagePlus planeWindow = WindowManager.getImage(dirName + " projection histogram");
		if (planeWindow!=null)
			planeWindow.hide();

		double[] xMinMax = ij.util.Tools.getMinMax(xMeasured);
		double[] yMinMaxMeasured=ij.util.Tools.getMinMax(yMeasured);
		double[] yMinMaxFitted=ij.util.Tools.getMinMax(yFitted);

		Plot pwX=new Plot(dirName + " projection histogram","Pixels", "Counts", xMeasured, yMeasured);
		pwX.setLimits(xMinMax[0],xMinMax[1], 0, Math.max(yMinMaxMeasured[1],yMinMaxFitted[1])*1.1);
		pwX.addPoints(xFitted, yFitted, Plot.CIRCLE);
		pwX.show();
	}
	//---------------------------------------------------------------------------------------------------------
	
	public boolean checkFitResults()
	{
		boolean res = fitResults != null && fitResults.getCount() > 0;
		if(res)
			return true;
		else  {
			IJ.showMessage("Fit results are not accessible. Please perform fitting.");			
			return false;
		}
	}
	//---------------------------------------------------------------------------------------------------------
	
	public boolean checkFitResultsWithoutMessage()
	{
		return fitResults != null && fitResults.getCount() > 0;
	}
	//---------------------------------------------------------------------------------------------------------
	
	protected void plotSuperImage(boolean filter, double minCrit, double maxCrit, double maxFWHM_X, double maxFWHM_Y, double minGauss, double maxGauss) {
		rm = RoiManager.getInstance();  
		if(rm == null) {
			IJ.showMessage("Please perform selection of particles first");
			return;
		}
		if(rm.getCount() == 0) {
			IJ.showMessage("Could not get selected ROIs. ROI manager was probably closed. Information about ROI bounds can not be exported to the ASCII file.");
			return;
		}
		
		if(fitResults == null) {
			IJ.showMessage("Please perform selection of spots and fit them to Gaussian first");
			return;
		}
		
		if(imp_work.getImageStackSize() > 1) { 
			IJ.run(imp_work, "Z Project...", "projection=[Sum Slices]");
			IJ.run(imp_work, "8-bit", "");
		}
		
		int count = fitResults.getCount();
		if(count > 0) {
			if(rtd == null)
				rtd = new ResultsTable();
			rtd.reset();

			w = imp_work.getWidth();
			h = imp_work.getHeight();
			int w_super = w*multiplier;
			int h_super = h*multiplier;
			ImageProcessor superResImage = new ByteProcessor(w_super, h_super);
			int[] superResArray = new int[h_super*w_super];
			
			double x_prev = 0, y_prev = 0;
			int slice_prev = 0;
			
			super_imagePointsCount = 0;
			superResPoints = new SuperResPoint[count];
			
			for(int i = 0; i < count; i++) 	{
				double[] fitRes = fitResults.getFit(i);
				if(fitRes != null) {
					if(filter) {
						if(fitRes[6] > maxCrit || fitRes[6] < minCrit || fitRes[2] > maxFWHM_X || fitRes[3] > maxFWHM_Y || fitRes[4] < minGauss || fitRes[4] > maxGauss) {
							continue;
						}
					}
					
					double x = fitRes[0]; 
					double y = fitRes[1];
					int slice = fitResults.getSlice(i);
					
					if(slice == slice_prev && (Math.abs(x - x_prev) < 0.01 && Math.abs(y - y_prev) < 0.01))  {
						slice_prev = slice;
						x_prev = x;
						y_prev = y;
					}
					else  {
						superResPoints[super_imagePointsCount] = new SuperResPoint(x, y, slice);						
						
						Roi roi = rm.getRoi(i);
						if(roi != null) {
							Rectangle r = roi.getBounds();
							superResPoints[super_imagePointsCount].setBounds(r);
						}
						
						rtd.incrementCounter();
						rtd.addValue("Slice", slice);				
						rtd.addValue("X", x);
						rtd.addValue("Y", y);
						
						int pos = w_super*(int)Math.floor(y*multiplier) + (int)Math.floor(x*multiplier);
						superResArray[pos]++; 
						//superResImage.putPixelValue((int)Math.round(x), (int)Math.round(y), 255.0);
						
						slice_prev = slice;
						x_prev = x;
						y_prev = y;
						
						super_imagePointsCount++;
					}
				}
			}

			double maxValue = MatStatUtils.getMaxOfArray(superResArray);
			for(int i = 0; i < h_super; i++) for(int j = 0; j < w_super; j++) {
				double val = 255*(superResArray[w_super*i + j]/maxValue); 
				superResImage.putPixelValue(j, i, val);				
			}
			
			rtd.show("List of SM positions");
			
			ImagePlus res_image = new ImagePlus("Super resolution image", superResImage);
			res_image.show();
			//IJ.run("Enhance Contrast", "normalize");			
		}
		else {
			IJ.showMessage("There is no data to plot the super resolution image");
			return;
		}
	}

	public void exportROIsToASCIIFile() {
		if(superResPoints == null) {
			IJ.showMessage("Please plot the superresolution image initially");
			return;
		}

		/*rm = RoiManager.getInstance();  
		if(rm == null) {
			IJ.showMessage("Please perform selection of particles first");
			return;
		}
		if(rm.getCount() == 0) {
			IJ.showMessage("Could not get selected ROIs. ROI manager was probably closed. Please repeat thresholding and localization again or reopen the plugin and repeat all actions.");
			return;
		}*/
		
		
		JFileChooser c = new JFileChooser() {
			
			private static final long serialVersionUID = 2134897367479823L;

			@Override
		    public void approveSelection(){
		        File f = getSelectedFile();
		        if(f.exists() && getDialogType() == SAVE_DIALOG){
		            int result = JOptionPane.showConfirmDialog(this,"The file exists, overwrite?","Existing file",JOptionPane.YES_NO_CANCEL_OPTION);
		            switch(result){
		                case JOptionPane.YES_OPTION:
		                    super.approveSelection();
		                    return;
		                case JOptionPane.NO_OPTION:
		                    return;
		                case JOptionPane.CLOSED_OPTION:
		                    return;
		                case JOptionPane.CANCEL_OPTION:
		                    cancelSelection();
		                    return;
		            }
		        }
		        super.approveSelection();
		    }        
		};
		
		FileNameExtensionFilter filter = new FileNameExtensionFilter("TXT", "txt");
		c.setFileFilter(filter);		
		c.setDialogTitle("Export ROIs to ASCII file");
		int returnVal = c.showSaveDialog(null);
		
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File writeFile = c.getSelectedFile();
			if(writeFile != null)  {
				try {
					/*DataOutputStream out = new DataOutputStream(new FileOutputStream(writeFile));
					
					int count = rm.getCount();
					out.writeChars("ROI");
					out.writeInt(count);
					
					for(int i = 0; i < count; i++) {
						Roi roi = rm.getRoi(i); 
						if(rm == null)
							continue;
						int pos = roi.getPosition();
						out.writeInt(pos);
						out.writeInt(0);
					}
					
					for(int i = 0; i < count; i++) {
						Roi roi = rm.getRoi(i); 
						if(rm == null)
							continue;
						Rectangle r = roi.getBounds();
						int x = (int)r.getX();
						int y = (int)r.getY();
						int w = (int)r.getWidth();						
						int h = (int)r.getHeight();
						
						out.writeInt(x);
						out.writeInt(x+w);
						out.writeInt(y);
						out.writeInt(y+h);
					}			
					out.close();*/
					
					FileWriter fw = new FileWriter(writeFile);
					BufferedWriter bw = new BufferedWriter(fw);
					bw.append("ROI\tcount:\tframes:\n");
					
					/*int count = rm.getCount();
					
					double x_prev = 0, y_prev = 0;
					int slice_prev = 0;

					int k = 0;
					for(int i = 0; i < count; i++) {
						
						Roi roi = rm.getRoi(i); 
						if(roi != null && fitResults.getFit(i) != null) {
							double x = fitResults.getFit(i)[0]; 
							double y = fitResults.getFit(i)[1];
							int slice = fitResults.getSlice(i);
							
							if((slice == slice_prev && (Math.abs(x - x_prev) < 0.01 && Math.abs(y - y_prev) < 0.01)))  {
								slice_prev = slice;
								x_prev = x;
								y_prev = y;
							}
							else  {
								k++;
								
								slice_prev = slice;
								x_prev = x;
								y_prev = y;
							}
						}
					}*/			

					int frames_count = imp_work.getImageStackSize();
					bw.append("\t" + String.valueOf(super_imagePointsCount) + "\t" + String.valueOf(frames_count) + "\n");
					bw.append("Frame\tx\ty\tw\th\n");
					
					for(int i = 0; i < super_imagePointsCount; i++) {
						bw.append(String.valueOf(superResPoints[i].getSlice()) + "\t" + superResPoints[i].getLeftX() + "\t" + superResPoints[i].getLeftY() + "\t" + superResPoints[i].getWidth() + "\t" + superResPoints[i].getHeight() + "\n");						
					}
					
					/*x_prev = 0;
					y_prev = 0;
					slice_prev = 0;
					for(int i = 0; i < count; i++) {
						
						Roi roi = rm.getRoi(i); 
						if(roi != null && fitResults.getFit(i) != null) {
							double x = fitResults.getFit(i)[0]; 
							double y = fitResults.getFit(i)[1];
							int slice = fitResults.getSlice(i);
							
							if(slice == slice_prev && (Math.abs(x - x_prev) < 0.01 && Math.abs(y - y_prev) < 0.01))  {
								slice_prev = slice;
								x_prev = x;
								y_prev = y;
							}
							else  {
								Rectangle r = roi.getBounds();
								int pos = roi.getPosition();
								if(pos == 0) pos++;
								bw.append(String.valueOf(pos) + "\t" + (int)r.getX() + "\t" + (int)r.getY() + "\t" + (int)r.getWidth() + "\t" + (int)r.getHeight() + "\n");
								
								slice_prev = slice;
								x_prev = x;
								y_prev = y;
							}
						}
					}	*/		
					
					bw.close();
					fw.close();
				}
				catch (Exception exc) {
					System.out.println(exc);
				}
			}
		}
	}
	
	public void plotHist() {
		//System.out.println(rt.getHeadings()[0]);
	}
	
	public void closeOpenedWindows(boolean all) {
		rm = RoiManager.getInstance();
		if(rm != null)
			rm.close();
		
		ArrayList<String> list = new ArrayList<String>();
		if(all) list.add("FLIN image");
		list.add("Threshold");
		if(all) list.add("Single Emitter Fit Results");
		if(all) list.add("X projection histogram");
		if(all) list.add("Y projection histogram");
		if(all) list.add("Fit results");
	
		Frame FR;
		for(int i = 0; i < list.size(); i++) {
			FR = WindowManager.getFrame(list.get(i));
			if (FR instanceof TextWindow)
				((TextWindow)FR).close();
			else if (FR instanceof PlugInFrame)
				((PlugInFrame)FR).close();
			else if (FR instanceof PlotWindow)
				((PlotWindow)FR).close();
		}
	}

	public void advancedSettings()
	 {
		GenericDialog as = new GenericDialog("Advanced Settings");
		as.addMessage("Don't alter these numbers \nunless you know what you're doing.\n");
		
		as.addCheckbox("Fit background (offset)?", GaussXYFit.fitOffset);
		as.addCheckbox("Estimate Background weight?", GaussXYFit.estimateBgWeight);
		as.addCheckbox("Remove overlapped ROI?", removeOverlappedROI);		
		as.addNumericField("Selection increment (pixels)", edge_increment, 0); 
		as.addNumericField("Max fit iterations",GaussXYFit.maxNumberOfIterations,0);
		as.addNumericField("Super res image multiplier",multiplier,0);
		
		as.showDialog();
		if (as.wasCanceled()) {
			return;
		}
		else
		{
			GaussXYFit.fitOffset = as.getNextBoolean();
			GaussXYFit.estimateBgWeight = as.getNextBoolean();
			removeOverlappedROI = as.getNextBoolean();			
			edge_increment = (int)as.getNextNumber();
			GaussXYFit.maxNumberOfIterations = (int)as.getNextNumber();
			multiplier = (int)as.getNextNumber();
			
			writeSettingsToFile();			
		}
	}

	protected void readSettingsFromFile() {
		INIFile iniFile = new INIFile("SMLocalSettings.ini");
		String tfc = iniFile.getStringProperty("Fit", "Target Fit criterion", GaussXYFit.activeTFC.toString());   //GaussXYFit.activeTFC == Gauss2DFit.TFCType.ChiSquare ? "Chi-square" : "MLE Poissonian");
		GaussXYFit.activeTFC = Gauss2DFit.TFCType.ValueForString(tfc);  //GaussXYFit.activeTFC = tfc.equals("Chi-square")? TFCType.ChiSquare: TFCType.MLEPoissonian; 
		GaussXYFit.fitOffset = iniFile.getBooleanProperty("Fit", "Fit offset", GaussXYFit.fitOffset);
		GaussXYFit.weightedFit = iniFile.getBooleanProperty("Fit", "Weighted fit", GaussXYFit.weightedFit);
		GaussXYFit.estimateBgWeight = iniFile.getBooleanProperty("Fit", "Estimate Background Weight", GaussXYFit.estimateBgWeight);		
		GaussXYFit.maxNumberOfIterations = iniFile.getIntegerProperty("Fit", "Max number of iterations", GaussXYFit.maxNumberOfIterations);
		maxChi2Value = iniFile.getDoubleProperty("Fit", "Max criterion value", maxChi2Value);
		edge_increment = iniFile.getIntegerProperty("Segmentation", "ROI edge increment", edge_increment); 
		minSpotRectSize = iniFile.getIntegerProperty("Segmentation", "Min ROI size", minSpotRectSize);
		maxSpotRectSize = iniFile.getIntegerProperty("Segmentation", "Max ROI size", maxSpotRectSize);
		minCircularity = iniFile.getDoubleProperty("Segmentation", "Min circularity", minCircularity);
		removeOverlappedROI = iniFile.getBooleanProperty("Segmentation", "Remove overlapped ROI", removeOverlappedROI);
		multiplier = iniFile.getIntegerProperty("Output", "Super res. multiplier", multiplier);		
	}
	
	protected void writeSettingsToFile() {
		INIFile iniFile = new INIFile("SMLocalSettings.ini");
		
		iniFile.addSection("Segmentation", null);
		iniFile.setIntegerProperty("Segmentation", "ROI edge increment", edge_increment, null);
		iniFile.setIntegerProperty("Segmentation", "Min ROI size", minSpotRectSize, null);
		iniFile.setIntegerProperty("Segmentation", "Max ROI size", maxSpotRectSize, null);		
		iniFile.setDoubleProperty("Segmentation", "Min circularity", minCircularity, null);		
		iniFile.setBooleanProperty("Segmentation", "Remove overlapped ROI", removeOverlappedROI, null);		

		iniFile.addSection("Fit", null);			
		iniFile.setStringProperty("Fit", "Target Fit criterion", GaussXYFit.activeTFC.toString(), null);
		iniFile.setBooleanProperty("Fit", "Fit offset", GaussXYFit.fitOffset, null);
		iniFile.setBooleanProperty("Fit", "Weighted fit", GaussXYFit.weightedFit, null);
		iniFile.setBooleanProperty("Fit", "Estimate Background Weight", GaussXYFit.estimateBgWeight, null);		
		iniFile.setIntegerProperty("Fit", "Max number of iterations", GaussXYFit.maxNumberOfIterations, null);
		iniFile.setDoubleProperty("Fit", "Max criterion value", maxChi2Value, null);
		
		iniFile.addSection("Output", null);		
		iniFile.setIntegerProperty("Output", "Super res. multiplier", multiplier, null);
		
		iniFile.save();
	}

}
