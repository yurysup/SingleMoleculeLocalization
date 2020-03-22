package fit;

import ij.IJ;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.TreeMap;

import javax.swing.JFileChooser;

import Jama.Matrix;

public class CACorrectionEstimator
{
 public double[] PolyCoefficientsX = null; 
 public double[] PolyCoefficientsY = null;
 private String path = ".";
//---------------------------------------------------------------------------------------------------------
 public CACorrectionEstimator()
 {
  //readSettings();  	 
 }
//---------------------------------------------------------------------------------------------------------

 public void setPath(String path) {
		this.path = path;
	}
//---------------------------------------------------------------------------------------------------------
 
public boolean isUpdated()
 {
  return PolyCoefficientsX != null && PolyCoefficientsY != null; 
 }
//---------------------------------------------------------------------------------------------------------
 public int getPolyDegree()
 {
  int xDegree = (PolyCoefficientsX != null ? PolyCoefficientsX.length : 0) -1;	 
  int yDegree = (PolyCoefficientsY != null ? PolyCoefficientsY.length : 0) -1;
  return Math.min(xDegree, yDegree);
 }
//---------------------------------------------------------------------------------------------------------
 protected double[] Double2doubleArray(Double[] values)
 {
  double[] result = new double[values.length];
  for(int i=0; i<values.length; i++)
   result[i] = values[i].doubleValue();
  
  return result;
 }
//---------------------------------------------------------------------------------------------------------
 public boolean Update(double[] x, double[] dx, double[] y, double[] dy, int polyDegree)
 {
  if(x.length != dx.length || y.length != dy.length )
   {	  
    IJ.showMessage("Cannot estimate the chromatic aberration.\n The length of the coordinates array is not equal to the length of the distances array.");
    return false;
   } 
  
  if(x.length <= polyDegree || y.length <= polyDegree)
   {	  
	IJ.showMessage("Cannot estimate the chromatic aberration.\n The length of the coordinates array should be more than polynomial degree.");
	return false;
   } 
	  
  
//Sorting X data by arguments   
  TreeMap<Double, Double> BasePoints = new TreeMap<Double, Double>();
  for(int i=0; i<x.length; i++)
    BasePoints.put(x[i], dx[i]);	  
  
//Performing fit and creating polynomial coefficients array for X data  
  PolyCoefficientsX = Polyfit(Double2doubleArray(BasePoints.keySet().toArray(new Double[0])), Double2doubleArray(BasePoints.values().toArray(new Double[0])), polyDegree);

  
//Sorting Y data by arguments   
  BasePoints.clear();
  for(int i=0; i<y.length; i++)
    BasePoints.put(y[i], dy[i]);	  
  
//Performing fit and creating polynomial coefficients array for X data  
  PolyCoefficientsY = Polyfit(Double2doubleArray(BasePoints.keySet().toArray(new Double[0])), Double2doubleArray(BasePoints.values().toArray(new Double[0])), polyDegree);

  saveSettings();
  
  return true;
 }
//---------------------------------------------------------------------------------------------------------  
 protected double[] Polyfit(double[] x, double[] y, int polyDegree)
 {
  Matrix VDMM = new Matrix(x.length, polyDegree+1, 1);
  Matrix X = new Matrix(x, x.length);
  Matrix VDMM_Col = X.copy();
  for(int i=1; i<=polyDegree; i++)
  {
   VDMM.setMatrix(0, x.length-1, i, i, VDMM_Col);
   VDMM_Col.arrayTimesEquals(X);
  }
  	 
  return VDMM.solve(new Matrix(y,y.length)).getColumnPackedCopy();
 }
//---------------------------------------------------------------------------------------------------------
 public double XCorrection(double x)
 {
  return Correction(x, PolyCoefficientsX); 
 }
//---------------------------------------------------------------------------------------------------------
 public double YCorrection(double y)
 {
  return Correction(y, PolyCoefficientsY); 
 }
 //---------------------------------------------------------------------------------------------------------
 private double Correction(double arg, double[] polyCoefficients)
 {
  if(!isUpdated())	
  {
   IJ.showMessage("Correction coefficients are not defined. Please load correction coefficient from the previously stored file.");
   return 0;
  }
  
  double correction=polyCoefficients[0];
  double argMultiplier = arg;
  for(int i=1; i<polyCoefficients.length; i++)
   {	  
    correction += polyCoefficients[i]*argMultiplier;
    argMultiplier *= arg;
   } 
  
  return correction;
 }
//---------------------------------------------------------------------------------------------------------
 protected void saveSettings() 
 { 
  try 
   {
	JFileChooser fileChooser = new JFileChooser();
	fileChooser.setCurrentDirectory(new File(path));
 
	if(fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION)
	{
		File file = fileChooser.getSelectedFile();
		PrintStream out = new PrintStream(file);
		//PrintStream out = new PrintStream(new File("LCACorrSettings.ini"));
		out.println("arg power:\tX:\tY:");
		for (int power=0; power<=getPolyDegree(); power++) 
			out.println("" + power + "\t" + PolyCoefficientsX[power] + "\t" + PolyCoefficientsY[power]); 
		out.close();
	}
   }
   catch (FileNotFoundException e) 
    {}
 }
//---------------------------------------------------------------------------------------------------------
 public void readSettings() 
 {
  try 
   {
	ArrayList<String> linesList = new ArrayList<String>();  
  
	JFileChooser fileChooser = new JFileChooser();
	fileChooser.setCurrentDirectory(new File(path));
 
	if(fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
	{
		File file = fileChooser.getSelectedFile();
		BufferedReader in = new BufferedReader(new FileReader(file));	
		//BufferedReader in = new BufferedReader(new FileReader("LCACorrSettings.ini"));

		in.readLine();
		while(in.ready())
			linesList.add(in.readLine());	

		PolyCoefficientsX = new double[linesList.size()];
		PolyCoefficientsY = new double[linesList.size()];

		String[] lineValues;    
		for(int i=0; i<linesList.size(); i++)
		{
			lineValues = linesList.get(i).split("\t");
			PolyCoefficientsX[i] = Double.parseDouble(lineValues[1]);
			PolyCoefficientsY[i] = Double.parseDouble(lineValues[2]);
		} 

		in.close();
	}
   }
  catch (FileNotFoundException ex)
   {
    return;
   }
  catch (IOException ex) 
   {
    return;
   }
 }
}
