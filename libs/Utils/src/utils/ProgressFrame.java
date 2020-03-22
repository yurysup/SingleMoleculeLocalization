package utils;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import ij.IJ;

public class ProgressFrame extends JFrame implements Runnable {

	private static final long serialVersionUID = 1345879368221L;

	ArrayList<ProgressFinishedListener> progressFinishListeners = new ArrayList<ProgressFinishedListener>();
	
	JProgressBar progressBar;
	private int progress, total;
	boolean active;
	boolean stopped;
	boolean visible;
	
	public ProgressFrame() {
		super("Progress dialog");

		int WIDTH = 320;
		int HEIGHT = 88;
		setSize(WIDTH, HEIGHT);
		setMinimumSize(new Dimension(WIDTH - 50, HEIGHT - 15));
		Toolkit kit = Toolkit.getDefaultToolkit();
		setLocation((kit.getScreenSize().width - WIDTH)/2, (kit.getScreenSize().height - HEIGHT)/2);

		Font normalFont = new Font("Tahoma", Font.PLAIN, 11);
		
		progressBar = new JProgressBar();
		progressBar.setStringPainted(true);
		
		JButton stopBtn = new JButton("Stop");
		stopBtn.setFont(normalFont);
		stopBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				active = false;
				stopped = true;
			}
		});
		
		Box p = Box.createHorizontalBox(); 
		p.add(Box.createHorizontalStrut(6));
		p.add(progressBar);
		p.add(Box.createHorizontalStrut(12));
		p.add(stopBtn);				
		p.add(Box.createHorizontalStrut(6));		
		add(BorderLayout.CENTER, p);
		add(BorderLayout.NORTH, new JLabel("Progress..."));

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
			IJ.showMessage(e.getMessage());
		}
		SwingUtilities.updateComponentTreeUI(this);
		
		active = false;
		visible = false;
	}

	public synchronized boolean updateProgress() {
		progress++;
		return active;
	}

	public void addProgressFinishedListener(ProgressFinishedListener l) {
		progressFinishListeners.add(l);
	}

	public void removeProgressFinishedListener(ProgressFinishedListener l) {
		progressFinishListeners.remove(l);		
	}

	public void notifyProgressFinishedListeners(int processedCount) {
		Iterator<ProgressFinishedListener> itr = progressFinishListeners.iterator();
	    while (itr.hasNext()) {
	    	ProgressFinishedListener l = itr.next();
	    	l.ProgressFinished(processedCount); 
	    }
	}
	
	public void initialize(int total) {
		this.total = total;		
		active = true;
		stopped = false;
		
		progress = 0;
		
		progressBar.setMinimum(0);
		progressBar.setMaximum(total);
		progressBar.setValue(0);

		Thread t = new Thread(this);
		t.start();
	}

	public void finalize(int processedCount) {
		active = false;
		if(!stopped)
			progress = total;
		
		String progressmessage = stopped? "Progress: " + (int)(((double)progress)/total*100) + " %" : "Progress: 100 %";
		System.out.println(progressmessage);
		IJ.showStatus(progressmessage);
		if(visible) {
			progressBar.setValue(progress);
		}
		setVisible(false);
		visible = false;
		
		notifyProgressFinishedListeners(processedCount);
	}
	
	@Override
	public void run() {
		try {		
			Thread.sleep(100);
			if(!visible && active) {
				setVisible(true);
				visible = true;
				Thread.sleep(20);				
			}
		
			while(active) {
				Thread.sleep(100);
				
				if(active) {
					double vprogress = ((double)(progress+1))/total;
					String progressmessage = "Progress: " + String.valueOf((int)(vprogress*100)) + " %";
					System.out.println(progressmessage);
					IJ.showStatus(progressmessage);
					progressBar.setValue(progress);
				}
			}
			
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
