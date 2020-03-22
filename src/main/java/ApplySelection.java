import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Roi;
import ij.plugin.frame.RoiManager;
import ij.process.ImageProcessor;

import java.awt.Rectangle;
import utils.ProgressFrame;

public class ApplySelection extends Thread {
	private RoiManager rm;
	Roi[] selectedRois;
	private int selectionMode;
	private int selected_slice;
	private int fromFrame;
	private int toFrame;
	private ImagePlus imp_work;
	private ImageStack ims_work;
	private boolean removeOverlappedROI;
	private int edge_increment;
	private int max_threshold;
	private ProgressFrame progressTh;
	
	public ApplySelection(RoiManager rm, Roi[] selectedRois, ImagePlus imp_work, ProgressFrame progressTh, int selected_slice, int selectionMode, int fromFrame, int toFrame) {
		this.rm = rm;
		this.selectedRois = selectedRois; 
		this.selectionMode = selectionMode;  //0 - all, range 1, selected 2
		this.imp_work = imp_work;
		this.selected_slice = selected_slice;
		this.progressTh = progressTh;
		this.fromFrame = fromFrame; 
		this.toFrame = toFrame;
	}

	public void setOptions(boolean removeOverlappedROI, int edge_increment, int max_threshold) {
		this.removeOverlappedROI = removeOverlappedROI;
		this.edge_increment = edge_increment;
		this.max_threshold = max_threshold;
	}

	public void run() {
		int count = selectedRois.length;
		int[] positions = new int[count];
		if(selectionMode < 2) {
			for(int i = 0; i < count; i++) {
				positions[i] = rm.getRoi(i).getPosition();
			}
		}
		else {
			for(int i = 0; i < count; i++) {
				positions[i] = selected_slice;
			}
		}

		imp_work.lock();
		rm.runCommand("Show None");
		//roiManager("Show None");
		//roiManager("Select", 0);
		//roiManager("Show All");
		
		if(imp_work.getImageStackSize() > 1) 
			ims_work = imp_work.getStack();
		
		rm.runCommand("Delete");
		if(rm.getCount() > 0)
			rm.runCommand("Delete");

		int w_th = imp_work.getWidth();
		int h_th = imp_work.getHeight();
		int h_edge_increment, w_edge_increment;

		ImageProcessor p = null;
		if(imp_work.getImageStackSize() == 1)
			p = imp_work.getProcessor();
		else if(selectionMode == 2 && selected_slice > 0)
			p = ims_work.getProcessor(selected_slice);

		progressTh.initialize(count);
		
		int current_slice = 0;
		for(int i = 0; i < count; i++) {
			if(selectionMode < 2 && positions[i] != current_slice) {
				p = ims_work.getProcessor(positions[i]);
			}
			current_slice = positions[i];
			
			if(selectionMode == 1 && current_slice < fromFrame) 			
				continue;
			else if(selectionMode == 1 && current_slice > toFrame) 			
				break;
			
			Rectangle r = new Rectangle(selectedRois[i].getBounds());
			if(p == null || r == null)
				continue;

			int maxValue = getMaxValue(p, r);				
			if(maxValue <= max_threshold)  {
				h_edge_increment = (r.height + 2*edge_increment) < 12? (int) Math.ceil((12.0 - r.height)/2): edge_increment;
				w_edge_increment = (r.width + 2*edge_increment) < 12? (int) Math.ceil((12.0 - r.width)/2): edge_increment;					
				r.x -= w_edge_increment;
				r.y -= h_edge_increment;
				r.width += 2*w_edge_increment;
				r.height += 2*h_edge_increment;

				if(removeOverlappedROI && isROIOverlapped(r, i, positions[i], positions, selectedRois))   //removes overlapped roi
					continue;

				if(r.x < 0)  r.x = 0;
				else if((r.x + r.width) >= w_th)  r.width = w_th - r.x - 1;
				if(r.y < 0)  r.y = 0;
				else if((r.y + r.height) >= h_th)  r.height = h_th - r.y - 1;

				if(positions[i] > 0) {
					Roi roi = new Roi(r);
					roi.setImage(imp_work);
					roi.setPosition(positions[i]);
					rm.addRoi(roi);
				}
				else
					rm.add(imp_work, new Roi(r), -1);
			}
			
			if(!progressTh.updateProgress())
				break;
		}
		progressTh.finalize(rm.getCount());		
		
		rm.runCommand("Show All");
		imp_work.unlock();
	}

	private boolean isROIOverlapped(Rectangle currentROI, int index, int position, int[] positions, Roi[] rois) {
		boolean res = false;
		Rectangle r = new Rectangle();
		r.x = currentROI.x - edge_increment;
		r.y = currentROI.y - edge_increment;
		r.width = currentROI.width + 2*edge_increment;
		r.height = currentROI.height + 2*edge_increment;
		
		for(int j = index+1; j < rois.length; j++) {
			if(positions[j] > position)
				break;
			if(r.intersects(rois[j].getBounds()))  {
				res = true;
				break;
			}
		}
		return res; 
	}
	
	private int getMaxValue(ImageProcessor ip, Rectangle r) {
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

	
}
