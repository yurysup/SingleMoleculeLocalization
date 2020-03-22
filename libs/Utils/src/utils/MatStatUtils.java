package utils;

import ij.process.ImageProcessor;

import java.awt.Point;
import java.awt.Rectangle;

public class MatStatUtils {

	public static int getMaxOfArray(int[] array)
	{
		int max = 0;
		for(int i = 0; i < array.length; i++) {
			if(max < array[i])
				max = array[i];
		}
		return max;
	}
	
	public static double getMaxOfArray(double[] array)
	{
		double max = 0;
		for(int i = 0; i < array.length; i++) {
			if(max < array[i])
				max = array[i];
		}
		return max;
	}

	public static int getMinOfArray(int[] array)
	{
		int min = Integer.MAX_VALUE;
		for(int i = 0; i < array.length; i++) {
			if(min > array[i])
				min = array[i];
		}
		return min;
	}
	
	public static double getMinOfArray(double[] array)
	{
		double min = Double.MAX_VALUE;
		for(int i = 0; i < array.length; i++) {
			if(min > array[i])
				min = array[i];
		}
		return min;
	}
	
	public static double getMean(short[] arr, int w, int h) {
		double avg = 0;
		int val;
		for (int i = 0; i < h; i++) {
			for (int j = 0; j < w; j++)  {
				val = arr[i*w+j] & 0xffff;
				avg += val;
			}
		}
		return avg/(w*h);
	}

	public static double getMean(byte[] arr, int w, int h) {
		double avg = 0;
		int val;
		for (int i = 0; i < h; i++) {
			for (int j = 0; j < w; j++)  {
				val = arr[i*w+j] & 0xff;
				avg += val;
			}
		}
		return avg/(w*h);
	}
	
	public static double getMean(float[] arr, int w, int h) {
		double avg = 0;
		for (int i = 0; i < h; i++) {
			for (int j = 0; j < w; j++)  {
				avg += arr[i*w+j];
			}
		}
		return avg/(w*h);
	}
	
	public static int getMaxValue(ImageProcessor ip, Rectangle r) {
		int max = 0;
		int pixel;
		for (int i = r.y; i < r.y+r.height; i++) {
			for (int j = r.x; j < r.x+r.width; j++)  {
				pixel = ip.getPixel(j, i);
				if(pixel > max)
					max = pixel;
			}
		}
		return max;
	}

	public static Point findMaxValue(short[] arr, int w, int h) {  //min size is 32 x 32
		int max = 0;
		int pixel;
		Point p = new Point(0,0);
		int minx = 0; 
		int miny = 0;
		int maxx = w; 
		int maxy = h;
		if(w*h >= 1024) {
			minx = w/2 - 16; 
			miny = h/2 - 16;
			maxx = w/2 + 16; 
			maxy = h/2 - 16;
		}
		for (int i = miny; i < maxy; i++) {
			for (int j = minx; j < maxx; j++)  {
				pixel = arr[i*w+j];
				if(pixel > max) {
					max = pixel;
					p.x = j;
					p.y = i;
				}
			}
		}
		return p;
	}

	public static double[] calcMoments(float[] sacf, int w, int h, Rectangle window) {  	
		double m00=0; double m10=0; double m01=0; double m20=0; double m02=0; double m11=0;
		int ry; int rx;
		double xCoord; double yCoord; double currentPixel;
		int minx = window.x; 
		int miny = window.y;
		int maxx = window.x + window.width; 
		int maxy = window.y + window.height;
		int count = 0;

		for (ry=miny; ry<maxy; ry++) {
			for (rx=minx; rx<maxx; rx++) {
				xCoord = rx+0.5;	//add half of pixel
				yCoord = ry+0.5;	//add half of pixel
				currentPixel=sacf[ry*w+rx];
				if(currentPixel < 0)
					currentPixel = 0;
				m00+=currentPixel;
				m10+=currentPixel*xCoord;
				m01+=currentPixel*yCoord;
				count++;
			}
		}
		double meanInt = m00/count;
		double xC = m10/m00;
		double yC = m01/m00;

		for (ry=miny; ry<maxy; ry++) {
			for (rx=minx; rx<maxx; rx++) {
				xCoord = rx+0.5;	//add half of pixel
				yCoord = ry+0.5;	//add half of pixel
				currentPixel=sacf[ry*w+rx];
				if(currentPixel < 0)
					currentPixel = 0;
				m20+=currentPixel*(xCoord-xC)*(xCoord-xC);
				m02+=currentPixel*(yCoord-yC)*(yCoord-yC);
				m11+=currentPixel*(xCoord-xC)*(yCoord-yC);
			}
		}
		double xxVar=m20/m00;
		double yyVar=m02/m00;
		double xyVar=m11/m00;

		double[] res = new double[6];
		res[0] = meanInt;
		res[1] = xC; 
		res[2] = yC;
		res[3] = Math.sqrt(xxVar); 
		res[4] = Math.sqrt(yyVar);
		res[5] = Math.sqrt(xyVar);
		return res; 
	}

	public static void extractColorFromRawImage(short[] dest_arr, short[] src_arr, int w, int h, int r, int c) {
		int pos, offset;
		int w_ = w/2;
		int h_ = h/2;		
		for (int i=0; i<h_; i++) {
			offset = 2*i*w+r;
			for (int j=0; j<w_; j++)  {
				pos = offset+2*j+c;
				dest_arr[i*w_+j] = src_arr[pos];
			}
		}
	}	

}
