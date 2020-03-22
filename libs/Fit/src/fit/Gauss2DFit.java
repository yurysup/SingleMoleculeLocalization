package fit;

import ij.IJ;
import ij.gui.Roi;
import ij.process.ImageProcessor;

import java.awt.Rectangle;
import java.util.ArrayList;

import Jama.Matrix;

public class Gauss2DFit
{
	private final int MeanXIndex = 0;
	private final int MeanYIndex = 1;
	private final int StDevXIndex = 2;
	private final int StDevYIndex = 3;
	private final int LGaussIndex = 4;
	private final int LBgIndex = 5;
	public final int ParametersCount = 6;

	private double[] xData;
	private double[] yData;
	private double[][] srcImage;
	private double[][] varImage;

	private Model model = new Model2DGauss();

	//java.lang.Enum
	public enum TFCType
	{
		ChiSquare{public String toString(){return "Chi-square";}},
		MLEPoissonian{public String toString(){return "MLE Poissonian";}};

		public static String[] AsStringArray()
		{
			TFCType[] values = values();

			String[] strArray = new String[values.length];
			for(int index=0; index<values.length; index++)
				strArray[index] = values[index].toString();

			return strArray;
		}

		@SuppressWarnings("unchecked")
		public static <T extends Enum<T>> T ValueForString(String name)
		{
			TFCType[] values = values();
			for(int index=0; index<values.length; index++)
				if(values[index].toString().equals(name))
					return  (T) values[index];

			throw new IllegalArgumentException();
		}

	};
	
	public TFCType activeTFC = TFCType.MLEPoissonian;//TFCType.ChiSquare;
	public boolean fitOffset=true;
	public boolean weightedFit=false;	
	public boolean estimateBgWeight=false;
	public int maxNumberOfIterations=50;
	public boolean ConvertToFWHM = true;

	/*	static Rectangle plotXRect, plotYRect;*/

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//Model classes	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////	
	interface Model
	{
		//allows to set coordinates where model value will be calculated
		public void SetCoordinate(int index, double value);	

		//returns model value in multidimensional point 
		public double Value(double[] parameters);

		//returns model derivative for parameter with parIndex in multidimensional point
		public double Derivative(double[] parameters, int parIndex);
	} 
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////  
	class Model2DGauss implements Model
	{
		private double[] spacePoint = new double[2]; 

		public void SetCoordinate(int index, double value)
		{
			if(index<2)
				spacePoint[index]=value;  
		}

		//Model2DGauss parameters: [MeanX MeanY StDevX StDevY GaussLevel BgLevel] 	
		public double Value(double[] parameters)
		{
			double vmsdX = (spacePoint[0]+0.5-parameters[MeanXIndex])/parameters[StDevXIndex];
			double vmsdY = (spacePoint[1]+0.5-parameters[MeanYIndex])/parameters[StDevYIndex];

			return parameters[LBgIndex] + parameters[LGaussIndex]*Math.exp(-vmsdX*vmsdX/2 - vmsdY*vmsdY/2);
		}

		public double Derivative(double[] parameters, int parIndex)
		{
			double vmsdX = (spacePoint[0]+0.5-parameters[MeanXIndex])/parameters[StDevXIndex];
			double vmsdY = (spacePoint[1]+0.5-parameters[MeanYIndex])/parameters[StDevYIndex];

			switch(parIndex)
			{
			case MeanXIndex: return parameters[LGaussIndex]*Math.exp(-vmsdX*vmsdX/2 - vmsdY*vmsdY/2)*vmsdX/parameters[StDevXIndex];
			case MeanYIndex: return parameters[LGaussIndex]*Math.exp(-vmsdX*vmsdX/2 - vmsdY*vmsdY/2)*vmsdY/parameters[StDevYIndex];
			case StDevXIndex: return parameters[LGaussIndex]*Math.exp(-vmsdX*vmsdX/2 - vmsdY*vmsdY/2)*vmsdX*vmsdX/parameters[StDevXIndex];
			case StDevYIndex: return parameters[LGaussIndex]*Math.exp(-vmsdX*vmsdX/2 - vmsdY*vmsdY/2)*vmsdY*vmsdY/parameters[StDevYIndex];
			case LGaussIndex: return Math.exp(-vmsdX*vmsdX/2 - vmsdY*vmsdY/2);
			case LBgIndex: return 1;
			}
			return 0;
		}
	}
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////	  	

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//Gauss2DFit	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////	
	public Gauss2DFit()
	{
	}
	
	public double[] fit(Rectangle roi, ImageProcessor ip) {
		return fit(roi, null, ip);
	}
	
	//-------------------------------------------------------------------------------------------------------------
	public double[] fit(Rectangle roi, Roi mask, ImageProcessor ip)
	// roi (region of interest) - is a rectangle, with one spot in it.  The rectangle does not
	//   contain any mask, so cannot be rotated from the X and Y axes.  It should be
	//   big enough to just contain all of the pixels associated with a spot.  A smaller
	//   rectangle will give an incorrectly small FWHM
	// ImageProcessor ip - is the main image containing all the spots
	//
	// Returns:
	// 0 Estimation for Mean X in pixels
	// 1 Estimation for Mean Y in pixels
	// 2 Estimation for FWHM X in pixels
	// 3 Estimation for FWHM Y in pixels
	// 4 Estimation for Gauss Level
	// 5 Estimation for Bg  Level
	// 6 Value of chi2 after the fit
	// 7 Value of ML method lambda parameter
	{
		//Checking for non-empty rectangle		
		if (roi.width==0 && roi.height==0)
		{
			IJ.showMessage("There is no Region of Interest.");
			return new double[7];
		}

		//Preparing source data	  
		xData = new double[roi.width];
		yData = new double[roi.height] ;
		srcImage = new double[roi.width][roi.height];

		for (int xIndex=0; xIndex<roi.width; xIndex++)
		{
			xData[xIndex] = roi.x + xIndex; 
			for (int yIndex=0; yIndex<roi.height; yIndex++)
			{
				yData[yIndex] = roi.y + yIndex;
				if(mask == null)
					srcImage[xIndex][yIndex] = ip.getPixelValue((int)xData[xIndex],(int)yData[yIndex]);
				else {
					if(mask.contains((int)xData[xIndex], (int)yData[yIndex]))
						srcImage[xIndex][yIndex] = ip.getPixelValue((int)xData[xIndex],(int)yData[yIndex]);
					else
						srcImage[xIndex][yIndex] = 0;
				}
			}
		}  

		if(weightedFit && !estimateBgWeight)
		{
			varImage = srcImage;
		}
		else if(weightedFit)
		{
			varImage = new double[roi.width][roi.height];
		}
		else
		{
			varImage = new double[roi.width][roi.height];
			for (int xIndex=0; xIndex<roi.width; xIndex++)
				for (int yIndex=0; yIndex<roi.height; yIndex++)
					varImage[xIndex][yIndex] = 1;
		}


		//Analyzing	  
		double[] Parameters = new double[ParametersCount];
		ArrayList<Integer> freeParameterIndexes = new ArrayList<Integer>(ParametersCount);
		freeParameterIndexes.add((Integer)MeanXIndex);
		freeParameterIndexes.add((Integer)MeanYIndex);
		freeParameterIndexes.add((Integer)StDevXIndex);
		freeParameterIndexes.add((Integer)StDevYIndex);
		freeParameterIndexes.add((Integer)LGaussIndex);
		if(fitOffset)
			freeParameterIndexes.add((Integer)LBgIndex);

		//Generating IG
		GenerateInitialGuesses(Parameters);//no info about par fixing because user cannot set parameter values

		/*	  Parameters[MeanXIndex] = 40;//50;
	  	Parameters[MeanYIndex] = 40;//50;
	  	Parameters[StDevXIndex] = 5;
	  	Parameters[StDevYIndex] = 5;
	  	Parameters[LGaussIndex] = 100;//2000;//160;
	  	Parameters[LBgIndex] = 0;//100;*/

		//Performing iterative fit
		double lambda = IterativeFitML(Parameters, freeParameterIndexes);

		//Preparing results	
		double[] results = new double[ParametersCount+2];
		results[0]=Parameters[0];//MeanX
		results[1]=Parameters[1];//MeanY
		if(ConvertToFWHM) {
			results[2]=Parameters[2]*2.35482;//convert std dev X to to fwhm
			results[3]=Parameters[3]*2.35482;//convert std dev Y to to fwhm
		}
		else {
			results[2]=Parameters[2];
			results[3]=Parameters[3];
		}
		results[4]=Parameters[4];//2D gauss level
		results[5]=Parameters[5];//Bg level
		results[6]=TFCValue(Parameters, freeParameterIndexes.size());
		results[7]=lambda;

		return results;
	}
	//-------------------------------------------------------------------------------------------------------------
	private void GenerateInitialGuesses(double[] parameters)
	//For means and standard deviations by method of moments
	//For 2d Gaussian level as maximum of the srcImage minus bg level  	  
	//For bg level as minimum of the srcImage
	{
		double TotalEventsCount=0, maxPixelData=0, minPixelData = srcImage[0][0];
		double XNx=0, YNy=0, X2Nx=0, Y2Ny=0;
		double x, y, currentPixelData;

		//estimation of background from perimeter values
		double bg = 0;
		double std_bg = 0;
		if(weightedFit && estimateBgWeight)
		{
			/* int count = xData.length*2 + (yData.length-2)*2;
		  for (int i=0; i<xData.length; i++)
		  {
			  bg += srcImage[i][0];
			  bg += srcImage[i][yData.length-1];
		  }
		  for (int i=1; i<yData.length-1; i++)
		  {
			  bg += srcImage[0][i];
			  bg += srcImage[xData.length-1][i];		  
		  }
		  bg /= count;

		  for (int i=0; i<xData.length; i++)
		  {
			  std_bg += (srcImage[i][0]-bg)*(srcImage[i][0]-bg);
			  std_bg += (srcImage[i][yData.length-1]-bg)*(srcImage[i][yData.length-1]-bg);
		  }
		  for (int i=1; i<yData.length-1; i++)
		  {
			  std_bg += (srcImage[0][i]-bg)*(srcImage[0][i]-bg);
			  std_bg += (srcImage[xData.length-1][i]-bg)*(srcImage[xData.length-1][i]-bg);
		  }
		  std_bg /= count-1;*/

			int count = 20;
			bg += srcImage[0][0];
			bg += srcImage[0][1];		  
			bg += srcImage[1][0];
			bg += srcImage[0][2];		  
			bg += srcImage[2][0];

			bg += srcImage[xData.length-1][0];
			bg += srcImage[xData.length-2][0];		  
			bg += srcImage[xData.length-1][1];		  
			bg += srcImage[xData.length-3][0];		  
			bg += srcImage[xData.length-1][2];		  

			bg += srcImage[0][yData.length-1];
			bg += srcImage[0][yData.length-2];		  
			bg += srcImage[1][yData.length-1];
			bg += srcImage[0][yData.length-3];		  
			bg += srcImage[2][yData.length-1];

			bg += srcImage[xData.length-1][yData.length-1];
			bg += srcImage[xData.length-2][yData.length-1];		  
			bg += srcImage[xData.length-1][yData.length-2];
			bg += srcImage[xData.length-3][yData.length-1];		  
			bg += srcImage[xData.length-1][yData.length-3];

			bg /= count;

			std_bg += (srcImage[0][0])*(srcImage[0][0]);
			std_bg += (srcImage[0][1])*(srcImage[0][1]);		  
			std_bg += (srcImage[1][0])*(srcImage[1][0]);
			std_bg += (srcImage[0][2])*(srcImage[0][2]);		  
			std_bg += (srcImage[2][0])*(srcImage[2][0]);

			std_bg += (srcImage[xData.length-1][0])*(srcImage[xData.length-1][0]);
			std_bg += (srcImage[xData.length-2][0])*(srcImage[xData.length-2][0]);		  
			std_bg += (srcImage[xData.length-1][1])*(srcImage[xData.length-1][1]);		  
			std_bg += (srcImage[xData.length-3][0])*(srcImage[xData.length-3][0]);		  
			std_bg += (srcImage[xData.length-1][2])*(srcImage[xData.length-1][2]);		  

			std_bg += (srcImage[0][yData.length-1])*(srcImage[0][yData.length-1]);
			std_bg += (srcImage[0][yData.length-2])*(srcImage[0][yData.length-2]);		  
			std_bg += (srcImage[1][yData.length-1])*(srcImage[1][yData.length-1]);
			std_bg += (srcImage[0][yData.length-3])*(srcImage[0][yData.length-3]);		  
			std_bg += (srcImage[2][yData.length-1])*(srcImage[2][yData.length-1]);

			std_bg += (srcImage[xData.length-1][yData.length-1])*(srcImage[xData.length-1][yData.length-1]);
			std_bg += (srcImage[xData.length-2][yData.length-2])*(srcImage[xData.length-2][yData.length-2]);		  
			std_bg += (srcImage[xData.length-1][yData.length-2])*(srcImage[xData.length-1][yData.length-2]);
			std_bg += (srcImage[xData.length-3][yData.length-1])*(srcImage[xData.length-3][yData.length-1]);		  
			std_bg += (srcImage[xData.length-1][yData.length-3])*(srcImage[xData.length-1][yData.length-3]);

			std_bg /= count-1;
		}

		for (int xIndex=0; xIndex<xData.length; xIndex++)
		{
			x = xData[xIndex]+0.5; 
			for (int yIndex=0; yIndex<yData.length; yIndex++)
			{
				y = yData[yIndex]+0.5;
				currentPixelData = srcImage[xIndex][yIndex];
				if(estimateBgWeight)
					varImage[xIndex][yIndex] = srcImage[xIndex][yIndex] - bg + std_bg;

				TotalEventsCount += currentPixelData;
				XNx += x*currentPixelData;
				YNy += y*currentPixelData;
				X2Nx += x*x*currentPixelData;
				Y2Ny += y*y*currentPixelData;
				if(maxPixelData<currentPixelData)
					maxPixelData = currentPixelData;
				if(minPixelData>currentPixelData)
					minPixelData = currentPixelData;	 
			}
		}	

		parameters[MeanXIndex] = XNx/TotalEventsCount;
		parameters[MeanYIndex] = YNy/TotalEventsCount;
		parameters[StDevXIndex] = Math.sqrt(X2Nx/TotalEventsCount - parameters[MeanXIndex]*parameters[MeanXIndex]);
		parameters[StDevYIndex] = Math.sqrt(Y2Ny/TotalEventsCount - parameters[MeanYIndex]*parameters[MeanYIndex]);
		if(estimateBgWeight)
		{
			parameters[LGaussIndex] = maxPixelData - bg;
			parameters[LBgIndex] = fitOffset ? bg : 0;
		}
		else
		{
			parameters[LGaussIndex] = maxPixelData - minPixelData;
			parameters[LBgIndex] = fitOffset ? minPixelData : 0;
		}
	}
	//-------------------------------------------------------------------------------------------------------------
	private double TFCValue(double[] parameters, int fitParametersCount)
	{
		if(activeTFC == TFCType.ChiSquare)
			return ChiSquare(parameters, fitParametersCount);
		else
			return MLEPoissonian(parameters, fitParametersCount);  
	}
	//-------------------------------------------------------------------------------------------------------------
	private double ChiSquare(double[] parameters, int fitParametersCount)
	{
		double result = 0.;
		int num_degrees_of_freedom = -fitParametersCount-1; 
		double dev;
		for(int xIndex=0; xIndex<xData.length; xIndex++)
		{
			model.SetCoordinate(0, xData[xIndex]);
			for(int yIndex=0; yIndex<yData.length; yIndex++)
			{
				model.SetCoordinate(1, yData[yIndex]);
				if(varImage[xIndex][yIndex] > 0)
				{
					dev = (srcImage[xIndex][yIndex] - model.Value(parameters));	 
					result += dev*dev/varImage[xIndex][yIndex];
					num_degrees_of_freedom++;
				}
			}
		}

		if(num_degrees_of_freedom>0)
			result /= num_degrees_of_freedom;

		return result;
	}
	//-------------------------------------------------------------------------------------------------------------    
	private double MLEPoissonian(double[] parameters, int fitParametersCount)
	{
		double result = 0.;
		int num_degrees_of_freedom = -fitParametersCount-1; 
		double srcValue, thValue;
		for(int xIndex=0; xIndex<xData.length; xIndex++)
		{
			model.SetCoordinate(0, xData[xIndex]);
			for(int yIndex=0; yIndex<yData.length; yIndex++)
			{
				model.SetCoordinate(1, yData[yIndex]);

				srcValue = srcImage[xIndex][yIndex];
				thValue = model.Value(parameters);

				if(srcValue > 0. && thValue > 0.)
				{
					result +=  srcValue*Math.log(srcValue/thValue) + thValue - srcValue;
					num_degrees_of_freedom++;
				}
			}
		}

		if(num_degrees_of_freedom>0)
			result = 2*result/num_degrees_of_freedom;

		return result;
	}
	//-------------------------------------------------------------------------------------------------------------
	private void GenerateMLMatrices(double[][] SDM, double[] FMV, double[] parameters, ArrayList<Integer> freeParameterIndexes)
	{
		if(activeTFC == TFCType.ChiSquare)
			ChiSquareGenerateMLMatrices(SDM, FMV, parameters, freeParameterIndexes);
		else
			MLEPoissonianGenerateMLMatrices(SDM, FMV, parameters, freeParameterIndexes);
	}
	//-------------------------------------------------------------------------------------------------------------
	private void ChiSquareGenerateMLMatrices(double[][] SDM, double[] FMV, double[] parameters, ArrayList<Integer> freeParameterIndexes)
	{
		for(int i=0;i<SDM.length;i++)
			for(int j=i;j<SDM.length;j++)
			{
				SDM[i][j] = 0;  
				for(int xIndex=0; xIndex<xData.length; xIndex++)
				{
					model.SetCoordinate(0, xData[xIndex]); 
					for(int yIndex=0; yIndex<yData.length; yIndex++)
					{  
						model.SetCoordinate(1, yData[yIndex]);  
						if(varImage[xIndex][yIndex]>0)
							SDM[i][j] += model.Derivative(parameters, freeParameterIndexes.get(i))*model.Derivative(parameters, freeParameterIndexes.get(j))/varImage[xIndex][yIndex];
					} 
				}

				if(i == j)
				{
					FMV[i] = 0;	 
					for(int xIndex=0; xIndex<xData.length; xIndex++)
					{
						model.SetCoordinate(0, xData[xIndex]); 
						for(int yIndex=0; yIndex<yData.length; yIndex++)
						{  
							model.SetCoordinate(1, yData[yIndex]);  
							if(varImage[xIndex][yIndex]>0)
								FMV[i] += model.Derivative(parameters, freeParameterIndexes.get(i))*(srcImage[xIndex][yIndex] - model.Value(parameters))/varImage[xIndex][yIndex];   
						} 
					}
				}
				else
					SDM[j][i] = SDM[i][j]; 
			}
	}
	//-------------------------------------------------------------------------------------------------------------
	private void MLEPoissonianGenerateMLMatrices(double[][] SDM, double[] FMV, double[] parameters, ArrayList<Integer> freeParameterIndexes)
	{
		double thValue;
		for(int i=0;i<SDM.length;i++)
			for(int j=i;j<SDM.length;j++)
			{
				SDM[i][j] = 0;  
				for(int xIndex=0; xIndex<xData.length; xIndex++)
				{
					model.SetCoordinate(0, xData[xIndex]); 
					for(int yIndex=0; yIndex<yData.length; yIndex++)
					{  
						model.SetCoordinate(1, yData[yIndex]);
						thValue = model.Value(parameters);
						if(thValue > 0.)
							SDM[i][j] += model.Derivative(parameters, freeParameterIndexes.get(i))*model.Derivative(parameters, freeParameterIndexes.get(j))*srcImage[xIndex][yIndex]/(thValue*thValue); 	 
					} 
				}

				if(i == j)
				{
					FMV[i] = 0;	 
					for(int xIndex=0; xIndex<xData.length; xIndex++)
					{
						model.SetCoordinate(0, xData[xIndex]); 
						for(int yIndex=0; yIndex<yData.length; yIndex++)
						{  
							model.SetCoordinate(1, yData[yIndex]);
							thValue = model.Value(parameters);
							if(thValue > 0.)
								FMV[i] += -model.Derivative(parameters, freeParameterIndexes.get(i))*(1-srcImage[xIndex][yIndex]/thValue);   
						} 
					}
				}
				else
					SDM[j][i] = SDM[i][j]; 
			}
	}
	//-------------------------------------------------------------------------------------------------------------  
	private double IterativeFitML(double[] parameters, ArrayList<Integer> freeParameterIndexes)
	//Iterative fit procedure by Marquardt-Levenberg method    
	{
		double lambda = 0.01;

		double[][] SDM = new double[freeParameterIndexes.size()][freeParameterIndexes.size()];
		double[] FMV = new double [freeParameterIndexes.size()];

		double[] parOffsets;
		double[] parTrial = parameters.clone();

		double tfcValue = TFCValue(parameters, freeParameterIndexes.size());
		double tfcValueTrial;

		GenerateMLMatrices(SDM, FMV, parameters, freeParameterIndexes);
		double diagSDMMultiplier = 1+lambda;

		int IterationCount=0;
		int pIndex;
		do
		{
			if(IterationCount == maxNumberOfIterations)
				break;

			IterationCount++;

			for(int i=0;i<SDM.length;i++)  
				SDM[i][i] *= diagSDMMultiplier;  

			parOffsets = (new Matrix(SDM)).solve(new Matrix(FMV, FMV.length)).getColumnPackedCopy();
			for(int i=0; i<parOffsets.length; i++)
			{
				pIndex = freeParameterIndexes.get(i); 	
				parTrial[pIndex] = parameters[pIndex] + parOffsets[i];
				if(parTrial[pIndex] < 0)
					parTrial[pIndex] = 0;
			}

			tfcValueTrial = TFCValue(parTrial, freeParameterIndexes.size());

			if(tfcValueTrial < tfcValue)
			{
				for(int i=0; i<freeParameterIndexes.size(); i++)
					parameters[freeParameterIndexes.get(i)] = parTrial[freeParameterIndexes.get(i)];

				if(Math.abs(tfcValue - tfcValueTrial)/tfcValueTrial < 1.0e-6)
					break;

				tfcValue = tfcValueTrial;

				lambda /= 10;

				GenerateMLMatrices(SDM, FMV, parameters, freeParameterIndexes);
				diagSDMMultiplier = 1+lambda;
			}
			else
			{
				lambda *= 10;

				if(lambda > 1.0e+10)
					break;  

				diagSDMMultiplier = (1+lambda)/(1+lambda/10);
			}
		}while(true);

		//System.out.println("terminating after " + IterationCount + " iterations");

		return lambda;
	}
	//-------------------------------------------------------------------------------------------------------------    
	public double[][] GetMeasuredXProjection()
	{
		double[][] result = new double[2][xData.length];

		for(int xIndex=0; xIndex<xData.length; xIndex++)
		{
			result[0][xIndex] = xData[xIndex];
			for(int yIndex=0; yIndex<yData.length; yIndex++)
				result[1][xIndex] += srcImage[xIndex][yIndex];  
		}

		return result;
	}
	//-------------------------------------------------------------------------------------------------------------    
	public double[][] GetMeasuredYProjection()
	{
		double[][] result = new double[2][yData.length];

		for(int yIndex=0; yIndex<yData.length; yIndex++)
		{
			result[0][yIndex] = yData[yIndex];
			for(int xIndex=0; xIndex<xData.length; xIndex++)
				result[1][yIndex] += srcImage[xIndex][yIndex];  
		}

		return result;
	}
	//-------------------------------------------------------------------------------------------------------------    
	public double[][] GetFittedXProjection(double[] parameters, int pointCount)
	{
		if(ConvertToFWHM) {
			parameters[2]=parameters[2]/2.35482;//convert fwhm to std dev X
			parameters[3]=parameters[3]/2.35482;//convert fwhm to std dev Y
		}

		double[][] result = new double[2][pointCount];

		double fitstepX=(xData[xData.length-1]-xData[0])/pointCount; 
		for(int xIndex=0; xIndex<pointCount; xIndex++)
		{
			result[0][xIndex] = xData[0] + xIndex*fitstepX;
			model.SetCoordinate(0, result[0][xIndex]);
			for(int yIndex=0; yIndex<yData.length; yIndex++)
			{
				model.SetCoordinate(1, yData[yIndex]);  
				result[1][xIndex] += model.Value(parameters);
			} 
		}

		if(ConvertToFWHM) {		
			parameters[2]=parameters[2]*2.35482;//convert std dev X to fwhm 
			parameters[3]=parameters[3]*2.35482;//convert std dev Y to fwhm
		}

		return result;
	}
	//-------------------------------------------------------------------------------------------------------------    
	public double[][] GetFittedYProjection(double[] parameters, int pointCount)
	{
		if(ConvertToFWHM) {		
			parameters[2]=parameters[2]/2.35482;//convert fwhm to std dev X
			parameters[3]=parameters[3]/2.35482;//convert fwhm to std dev Y
		}

		double[][] result = new double[2][pointCount];

		double fitstepY=(yData[yData.length-1]-yData[0])/pointCount; 
		for(int yIndex=0; yIndex<pointCount; yIndex++)
		{
			result[0][yIndex] = yData[0] + yIndex*fitstepY;
			model.SetCoordinate(1, result[0][yIndex]);
			for(int xIndex=0; xIndex<xData.length; xIndex++)
			{
				model.SetCoordinate(0, xData[xIndex]);  
				result[1][yIndex] += model.Value(parameters);
			} 
		}

		if(ConvertToFWHM) {		
			parameters[2]=parameters[2]*2.35482;//convert std dev X to fwhm 
			parameters[3]=parameters[3]*2.35482;//convert std dev Y to fwhm
		}

		return result;
	}
	//-------------------------------------------------------------------------------------------------------------
		
	public void storeFitResult(FitResults fitResults, double[] fit, int index, String fitName)
	{
		boolean is_good_fit = (fit != null); 
		/*is_good_fit = is_good_fit && results[0] > 0 && results[0]<w && results[1]>0 && results[1]<h;//for means
		is_good_fit = is_good_fit && results[2]>1 && results[2]<w/2 && results[3]>1 && results[3]<h/2;//for FWHM
		is_good_fit = is_good_fit && fit[6]<maxChi2Value;*/

		fitResults.setName(fitName, index);	  
		if(is_good_fit)
		{ 
			fitResults.setFit(fit, index);

			double[][] projection;

			projection = GetMeasuredXProjection();
			fitResults.setXdataX(projection[0], index);
			fitResults.setYdataX(projection[1], index);

			projection = GetMeasuredYProjection();
			fitResults.setXdataY(projection[0], index);
			fitResults.setYdataY(projection[1], index);

			double[] parameters = new double[ParametersCount];
			for(int pIndex=0; pIndex<parameters.length; pIndex++)
				parameters[pIndex] = fit[pIndex];

			projection = GetFittedXProjection(parameters, 100);
			fitResults.setXfitX(projection[0], index);
			fitResults.setYfitX(projection[1], index);

			projection = GetFittedYProjection(parameters, 100);
			fitResults.setXfitY(projection[0], index);
			fitResults.setYfitY(projection[1], index);
		}
		else
		{
			fitResults.setFit(null, index);
			fitResults.setXdataX(null, index);
			fitResults.setYdataX(null, index);
			fitResults.setXdataY(null, index);
			fitResults.setYdataY(null, index);
			fitResults.setXfitX(null, index);
			fitResults.setYfitX(null, index);
			fitResults.setXfitY(null, index);
			fitResults.setYfitY(null, index);
		}
	}
	
}