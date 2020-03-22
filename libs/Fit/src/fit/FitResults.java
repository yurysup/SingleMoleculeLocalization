package fit;

public class FitResults {
	
	private String[] name;
	private int slice[];	
	private double[][] fit;
	private double[][] xdataX;
	private double[][] ydataX;
	private double[][] xfitX;
	private double[][] yfitX;
	
	private double[][] xdataY;
	private double[][] ydataY;
	private double[][] xfitY;
	private double[][] yfitY;

	private double[][] aguessX;
	private double[][] aguessY;
	
	private int count = 0;

	public FitResults(int count) {
		this.count = count;
		
		name = new String[count];
		slice = new int[count];
				
		fit = new double[count][];		
		xdataX = new double[count][];
		ydataX = new double[count][];
		
		xfitX = new double[count][];
		yfitX = new double[count][];
		
		xdataY = new double[count][];
		ydataY = new double[count][];
		
		xfitY = new double[count][];
		yfitY = new double[count][];
		
		aguessX = new double[count][];
		aguessY = new double[count][];
	}

	public String getName(int index) {return name[index];}
	public void setName(String name, int index) {this.name[index] = name;}
	
	public int getSlice(int index) {return slice[index];}
	public void setSlice(int val, int index) {this.slice[index] = val;}
		
	public double[] getFit(int index) {return fit[index];}
	public void setFit(double[] fit, int index) {this.fit[index] = fit;}

	public double[] getXdataX(int index) {return xdataX[index];}
	public void setXdataX(double[] array, int index) {this.xdataX[index] = array;}
	public double[] getYdataX(int index) {return ydataX[index];}
	public void setYdataX(double[] array, int index) {this.ydataX[index] = array;}
	
	public double[] getXfitX(int index) {return xfitX[index];}
	public void setXfitX(double[] xfitX, int index) {this.xfitX[index] = xfitX;}
	public double[] getYfitX(int index) {return yfitX[index];}
	public void setYfitX(double[] yfitX, int index) {this.yfitX[index] = yfitX;}

	public double[] getXdataY(int index) {return xdataY[index];}
	public void setXdataY(double[] array, int index) {this.xdataY[index] = array;}
	public double[] getYdataY(int index) {return ydataY[index];}
	public void setYdataY(double[] array, int index) {this.ydataY[index] = array;}

	public double[] getXfitY(int index) {return xfitY[index];}
	public void setXfitY(double[] xfitY, int index) {this.xfitY[index] = xfitY;}
	public double[] getYfitY(int index) {return yfitY[index];}
	public void setYfitY(double[] yfitY, int index) {this.yfitY[index] = yfitY;}


	public double[] getAguessX(int index) {return aguessX[index];}
	public void setAguessX(double[] array, int index) {this.aguessX[index] = array;}
	
	public double[] getAguessY(int index) {return aguessY[index];}
	public void setAguessY(double[] array, int index) {this.aguessY[index] = array;}

	public int getCount() {return count;}
}
