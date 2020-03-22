package fit;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.Plot;
import ij.measure.ResultsTable;
import ij.process.ImageProcessor;

import java.awt.Rectangle;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;

public class SingleROIGauss2DFit extends Gauss2DFit {
	
	private ImageProcessor ipp;
	private Rectangle roi;
	private String title, titleExtention;
	private FitResults fitResults;
	ResultsTable rt;
	public double maxChi2Value = 10;
	double[] fit = null;
	private ResultsTableViewer resTableViewer = null;

	public SingleROIGauss2DFit(ImageProcessor ipp, Rectangle roi, String title, String titleExtention) {
		this.ipp = ipp;
		this.roi = roi;
		this.title = title;
		this.titleExtention = titleExtention;
		fitResults = new FitResults(1);
		rt = new ResultsTable();
		rt.setPrecision(5);
	}

	public void setResTableViewer(ResultsTableViewer resTableViewer) {
		this.resTableViewer = resTableViewer;
	}

	public void doGaussFitAndShowResults() {
		doGaussFit();
		showFitResults();
		showResultsInStatus();
	}
		
	public void doGaussFit() {
		if(ipp == null)
		{  
			IJ.showMessage("There is no access to the image. Reference is NULL.");
			return;
		}
		try
		{
			fit = fit(roi, ipp);
		}
		catch(Exception ex) {
			fit=null;
			System.out.println("Fit was failed: " + ex.getMessage());
			IJ.showMessage(ex.getMessage());
			return;
		}
		storeFitResult(fitResults, fit, 0, title + " " + titleExtention);
	}

	public void setMaxChi2Value(double maxChi2Value) {
		this.maxChi2Value = maxChi2Value;
	}
	
	public double[] getFitParameters() {
		return fit;
	}
	
	public FitResults getFitResults() {
		return fitResults;
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////	
	//Procedures for displaying results	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////	
	void showResultsInStatus()
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
		message += activeTFC.toString() + ": " + formatter.format(fit[6]);		
		IJ.showStatus(message);		
	}
	
	//---------------------------------------------------------------------------------------------------------	 
	public void showFitResults()
	{
		if(fit==null)
		{	  
			IJ.showMessage("Fit was failed or results were out of range (physically not acceptable or criterion value was more than the entered limit)");
			return;
		}	

		if(resTableViewer != null) {
			resTableViewer.addResultsTableRecord(rt, fit, title + " " + titleExtention);
			rt.show(title);
		}
		
		DisplayResult2DGraphs();
	}
	//---------------------------------------------------------------------------------------------------------
	
	private void DisplayResult2DGraphs()
	{
		//X projection graph
		DisplayGraph(title + " X", fitResults.getXdataX(0), fitResults.getYdataX(0), fitResults.getXfitX(0), fitResults.getYfitX(0));

		//Y projection graph
		DisplayGraph(title + " Y", fitResults.getXdataY(0), fitResults.getYdataY(0), fitResults.getXfitY(0), fitResults.getYfitY(0));
	}	
	//---------------------------------------------------------------------------------------------------------		
	
	public static void DisplayGraph(String title, double[] xMeasured, double[] yMeasured, double[] xFitted, double[]yFitted)
	{
		ImagePlus planeWindow = WindowManager.getImage(title + " projection histogram");
		if (planeWindow!=null)
			planeWindow.hide();

		double[] xMinMax = ij.util.Tools.getMinMax(xMeasured);
		double[] yMinMaxMeasured=ij.util.Tools.getMinMax(yMeasured);
		double[] yMinMaxFitted=ij.util.Tools.getMinMax(yFitted);

		Plot pwX=new Plot(title + " projection histogram", "Pixels", "Counts", xMeasured, yMeasured);
		double min = Math.min(yMinMaxMeasured[0],yMinMaxFitted[0]);
		double max = Math.max(yMinMaxMeasured[1],yMinMaxFitted[1]);
		pwX.setLimits(xMinMax[0],xMinMax[1], min, (max-min)*1.05+min);
		pwX.addPoints(xFitted, yFitted, Plot.CIRCLE);
		pwX.show();
	}
	//---------------------------------------------------------------------------------------------------------

}
