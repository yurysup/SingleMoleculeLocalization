import ij.*;
import ij.process.*;
import ij.plugin.filter.PlugInFilter;

// STORM
// by Victor Skakun skakun@sstcenter.com January 2016

public class SM_Localization implements PlugInFilter {
 
	//FLINMainFrame frm;
	SMLWizard wizard;
	ImagePlus imp;	
	ImageProcessor ipp;
	
	static int signoff=0;
	static String maintitle;
	static ij.measure.Calibration cal;
	static double cala;
	static double calb;
	
	public int setup(String arg, ImagePlus imp) {
	
		if(imp == null) {
			IJ.showMessage("There is no Image of Interest.");
			return DONE;
		}
		this.imp = imp;
		
		maintitle=imp.getTitle();
		maintitle="- "+maintitle;
		cal = imp.getCalibration(); // take calibration.  This takes care of signed/unsigned problems
		if (cal.calibrated()) {
			double[] calcoeffs=cal.getCoefficients();
			cala=calcoeffs[0];
			calb=calcoeffs[1];
		} else {
			cala=0;
			calb=0;
		}
		signoff = 0-(int)cala;
	
		return DOES_ALL+NO_CHANGES;
	}
	
	public void run(ImageProcessor ip) {
		this.ipp = ip;
		
		if(ipp == null) {
			IJ.showMessage("There is no Image of Interest.");
			return;
		}
		else {
			SMLImageHandler handler = new SMLImageHandler(ipp, imp);
			wizard = new SMLWizard(handler);
		}
	}
} 

