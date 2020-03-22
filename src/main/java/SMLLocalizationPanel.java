import ij.IJ;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SpringLayout;
import javax.swing.border.CompoundBorder;
import javax.swing.border.TitledBorder;

import utils.ProgressFinishedListener;
import utils.SpringUtilities;
import jwizardcomponent.JWizardComponents;
import jwizardcomponent.JWizardPanel;

@SuppressWarnings("serial")
public class SMLLocalizationPanel extends JWizardPanel {
	
	private final static String SELECT_PARTICLES_TEXT = "Localization is performed on a binary image. To get a binary image click Threshold image button. "
			+ "You can change min/max thresholding level by scrollers on the Threshold dialog. Min level defines thresholding level itself. "
			+ "Max level allows to reject particles with high intensity. Select ROI in the original image if you want to estimate the thresholding level incide the ROI. "
			+ "Important. Do not Apply selection in the Threshold dialog. Use this dialog only for the threshold estimation. "
			+ "\nAfter defenition of the threshold click Select particles button to perform localization. Select ROI in the thresholded image if you want to perform localization incide the ROI. "
			+ "\nFinally apply selection by clicking Apply selection button. Note, range of frames becomes active on this step.";
	
	private final static String SELECT_LOCAL_MAX_TEXT = "Localization performed on initial image. Please, set minimal and maximal values for local maximums candidates (try Plot image histogram to see"
			+ " intensities range.";

	SMLImageHandler imagesHandler;

	private JTextField textFieldMinSize;
	private JTextField textFieldMaxSize;
	private JTextField textFieldCircularity;
	private JLabel ROIsCount;

	private JButton applyThresholdBtn;
	private JButton selectParticlesBtn;
	private JButton applySelectionBtn;
	private JButton selectByLocalMinBtn;
	private JButton thresholdMaximumsBtn;
	private JButton plotHistogramBtn;
	
	private JRadioButton rbtnSelectedFrame;
	private JRadioButton rbtnAllFrames;
	private JRadioButton rbtnRangeOfFrames;
	
	private JRadioButton commonSelectionButton;
	private JRadioButton localMaxSelectionButton;
	
	private SpinnerNumberModel spinnerModelFrameFrom;
	private SpinnerNumberModel spinnerModelFrameTo;
	private JSpinner spinnerFrameFrom;
	private JSpinner spinnerFrameTo;
	private Box rangeBox;

	private JTextField textFieldMinInt;
	private JTextField textFieldMaxInt;
	private JTextField textFieldFitRad;
	private JTextArea textArea;
	private JScrollPane scrollPane;
	
	private JPanel selectLocalMaxPanel;
	private JPanel selectParticlesPanel;

	public SMLLocalizationPanel(JWizardComponents wizardComponents, final SMLImageHandler imagesHandler) {
		super(wizardComponents, "Localize particles");
		this.imagesHandler = imagesHandler;

		JPanel panelCountPartOption = new JPanel(new SpringLayout());
		Font normalFont = javax.swing.UIManager.getDefaults().getFont("TextField.font");
		Color borderTitleColor = new Color(0, 70, 213);
		panelCountPartOption.setBorder(new CompoundBorder(BorderFactory.createTitledBorder(null, " Select particles options: ", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, normalFont, borderTitleColor), BorderFactory.createEmptyBorder(2,10,0,10)));		
		JLabel l = new JLabel("Min size (pixels^2):");
		//l.setAlignmentX(RIGHT_ALIGNMENT);
		panelCountPartOption.add(l);
		textFieldMinSize = new JTextField("12", 10);
		l.setLabelFor(textFieldMinSize);
		panelCountPartOption.add(textFieldMinSize);

		l = new JLabel("Max size (pixels^2):");
		//l.setFont(normalFont);
		//l.setAlignmentX(RIGHT_ALIGNMENT);		
		panelCountPartOption.add(l);		
		textFieldMaxSize = new JTextField("92", 10);
		l.setLabelFor(textFieldMaxSize);
		panelCountPartOption.add(textFieldMaxSize);

		l = new JLabel("Circularity from:");
		//l.setFont(normalFont);
		//l.setAlignmentX(RIGHT_ALIGNMENT);		
		panelCountPartOption.add(l);		
		textFieldCircularity = new JTextField("0.8", 10);
		l.setLabelFor(textFieldCircularity);
		panelCountPartOption.add(textFieldCircularity);

		l = new JLabel("Process type:");
		//l.setFont(normalFont);
		l.setAlignmentX(LEFT_ALIGNMENT);		
		panelCountPartOption.add(l);		
		l = new JLabel("");
		//l.setFont(normalFont);
		l.setAlignmentX(LEFT_ALIGNMENT);		
		panelCountPartOption.add(l);		
		
		rbtnSelectedFrame = new JRadioButton("selected frame");
		//rbtnSelectedFrame.setFont(normalFont);		
		rbtnSelectedFrame.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				spinnerFrameFrom.setEnabled(rbtnRangeOfFrames.isSelected());
				spinnerFrameTo.setEnabled(rbtnRangeOfFrames.isSelected());
			}
		});
		panelCountPartOption.add(rbtnSelectedFrame);
		
		rbtnAllFrames = new JRadioButton("all frames");
		rbtnAllFrames.setSelected(true);
		//rbtnAllFrames.setFont(normalFont);
		rbtnAllFrames.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				spinnerFrameFrom.setEnabled(rbtnRangeOfFrames.isSelected());
				spinnerFrameTo.setEnabled(rbtnRangeOfFrames.isSelected());
			}
		});
		panelCountPartOption.add(rbtnAllFrames);

		rbtnRangeOfFrames = new JRadioButton("range of frames");
		//rbtnRangeOfFrames.setFont(normalFont);
		rbtnRangeOfFrames.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				spinnerFrameFrom.setEnabled(rbtnRangeOfFrames.isSelected());
				spinnerFrameTo.setEnabled(rbtnRangeOfFrames.isSelected());
			}
		});

		panelCountPartOption.add(rbtnRangeOfFrames);
		
		ButtonGroup processTypeRadioButtons = new ButtonGroup();
		processTypeRadioButtons.add(rbtnSelectedFrame);
		processTypeRadioButtons.add(rbtnAllFrames);
		processTypeRadioButtons.add(rbtnRangeOfFrames);		
		
		rangeBox = Box.createHorizontalBox();
		l = new JLabel("from: ");
		//l.setFont(normalFont);
		//l.setAlignmentX(RIGHT_ALIGNMENT);		
		rangeBox.add(l);
		spinnerModelFrameFrom = new SpinnerNumberModel(1,1,100,1);
		spinnerFrameFrom = new JSpinner(spinnerModelFrameFrom);
		spinnerFrameFrom.setMaximumSize(spinnerFrameFrom.getPreferredSize());
		l.setLabelFor(spinnerFrameFrom);
		spinnerFrameFrom.setEnabled(false);		
		rangeBox.add(spinnerFrameFrom);
		
		rangeBox.add(Box.createHorizontalStrut(10));
		
		l = new JLabel("to: ");
		//l.setFont(normalFont);
		//l.setAlignmentX(RIGHT_ALIGNMENT);		
		rangeBox.add(l);
		spinnerModelFrameTo = new SpinnerNumberModel(1,1,100,1);
		spinnerFrameTo = new JSpinner(spinnerModelFrameTo);
		spinnerFrameTo.setMaximumSize(spinnerFrameTo.getPreferredSize());
		l.setLabelFor(spinnerFrameTo);
		spinnerFrameTo.setEnabled(false);
		rangeBox.add(spinnerFrameTo);
		
		panelCountPartOption.add(rangeBox);
		
		//Lay out the panel.
		SpringUtilities.makeCompactGrid(panelCountPartOption,
				6, 2, //rows, cols
				6, 6,        //initX, initY
				6, 6);       //xPad, yPad

		JPanel buttonsBox = new JPanel(new GridLayout(6,1,8,8));

		applyThresholdBtn = new JButton("Threshold image");
		//applyThresholdBtn.setEnabled(false);
		//applyThresholdBtn.setFont(normalFont);
		applyThresholdBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				applyThreshold();
			}
		});
		buttonsBox.add(applyThresholdBtn);

		selectParticlesBtn = new JButton("Select particles");
		selectParticlesBtn.setEnabled(false);
		//selectParticlesBtn.setFont(normalFont);
		selectParticlesBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectParticles();
			}
		});
		buttonsBox.add(selectParticlesBtn);				

		applySelectionBtn = new JButton("Apply selection");
		applySelectionBtn.setEnabled(false);
		//applySelectionBtn.setFont(normalFont);
		applySelectionBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				applySelection();
			}
		});
		buttonsBox.add(applySelectionBtn);	
		

		Box countROIBox = Box.createHorizontalBox();
		l = new JLabel("    ROIs count: ");
		//l.setFont(normalFont);
		//l.setAlignmentX(RIGHT_ALIGNMENT);		
		countROIBox.add(l);
		countROIBox.add(Box.createHorizontalStrut(10));		
		ROIsCount = new JLabel("");
		//ROIsCount.setFont(normalFont);
		//ROIsCount.setAlignmentX(RIGHT_ALIGNMENT);
		countROIBox.add(ROIsCount);
		buttonsBox.add(countROIBox);
		
		selectParticlesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT,5,5));
		selectParticlesPanel.add(panelCountPartOption);
		selectParticlesPanel.add(buttonsBox);
		
		//Choose method
		JPanel selectMethodPanel = new JPanel(new FlowLayout(FlowLayout.LEFT,5,5));
		commonSelectionButton = new JRadioButton("Common selection");
		commonSelectionButton.setSelected(true);
		localMaxSelectionButton = new JRadioButton("Local maximums selection");
		ButtonGroup localizationMethodRadioButtons = new ButtonGroup();
		localizationMethodRadioButtons.add(commonSelectionButton);
		localizationMethodRadioButtons.add(localMaxSelectionButton);
		selectMethodPanel.add(commonSelectionButton);
		selectMethodPanel.add(localMaxSelectionButton);
		
		commonSelectionButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				if (commonSelectionButton.isSelected()) {
					selectLocalMaxPanel.setVisible(false);
					selectParticlesPanel.setVisible(true);
					textArea.setText(SELECT_PARTICLES_TEXT);
				}				
			}
		} );
		
		localMaxSelectionButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				if (localMaxSelectionButton.isSelected()) {
					selectLocalMaxPanel.setVisible(true);
					selectParticlesPanel.setVisible(false);
					textArea.setText(SELECT_LOCAL_MAX_TEXT);
				}
			}
		} );
		selectMethodPanel.setBorder(null);
		
		//Local Max selection Panel
		JPanel maxThresholdPanel = new JPanel(new SpringLayout());
		//Font normalFont = javax.swing.UIManager.getDefaults().getFont("TextField.font");
		//Color borderTitleColor = new Color(0, 70, 213);
		maxThresholdPanel.setBorder(new CompoundBorder(BorderFactory.createTitledBorder(null, " Local max selection options: ", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, normalFont, borderTitleColor), BorderFactory.createEmptyBorder(2,10,0,10)));		
		
		JLabel minIntensityL = new JLabel("Min intensity:");
		maxThresholdPanel.add(minIntensityL);
		textFieldMinInt = new JTextField("0", 10);
		textFieldMinInt.setEditable(true);
		minIntensityL.setLabelFor(textFieldMinInt);
		maxThresholdPanel.add(textFieldMinInt);

		JLabel maxIntensityL = new JLabel("Max intensity:");
		maxThresholdPanel.add(maxIntensityL);
		textFieldMaxInt = new JTextField("65535", 10);
		maxIntensityL.setLabelFor(textFieldMaxInt);
		textFieldMaxInt.setEditable(true);
		maxThresholdPanel.add(textFieldMaxInt);
		
		JLabel fittingRadL = new JLabel("Radius of ROI:");
		maxThresholdPanel.add(fittingRadL);
		textFieldFitRad = new JTextField("3", 10);
		fittingRadL.setLabelFor(textFieldFitRad);
		textFieldFitRad.setEditable(true);
		maxThresholdPanel.add(textFieldFitRad);
		
		//Lay out the panel.
		SpringUtilities.makeCompactGrid(maxThresholdPanel,
				3, 2, //rows, cols
				6, 6,        //initX, initY
				6, 6);       //xPad, yPad

		JPanel localMaxButtonsBox = new JPanel(new GridLayout(2,1,8,8));
		
		//Plot histogram for image
		plotHistogramBtn = new JButton("Plot image histogram");
		plotHistogramBtn.setEnabled(true);
		plotHistogramBtn.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			plotImageHistogram();
		}
		});
		localMaxButtonsBox.add(plotHistogramBtn);
		
		//FOR dSTORM LOCALIZATION
		selectByLocalMinBtn = new JButton("Select (local max)");
		selectByLocalMinBtn.setEnabled(true);
		//applySelectionBtn.setFont(normalFont);
		selectByLocalMinBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mySelection();
			}
		});
		localMaxButtonsBox.add(selectByLocalMinBtn);	

		Box countLocalMaxROIBox = Box.createHorizontalBox();
		l = new JLabel("    ROIs count: ");
		//l.setFont(normalFont);
		//l.setAlignmentX(RIGHT_ALIGNMENT);		
		countLocalMaxROIBox.add(l);
		countLocalMaxROIBox.add(Box.createHorizontalStrut(10));		
		ROIsCount = new JLabel("");
		//ROIsCount.setFont(normalFont);
		//ROIsCount.setAlignmentX(RIGHT_ALIGNMENT);
		countLocalMaxROIBox.add(ROIsCount);
		localMaxButtonsBox.add(countLocalMaxROIBox);
		
		selectLocalMaxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT,5,5));
		selectLocalMaxPanel.add(maxThresholdPanel);
		selectLocalMaxPanel.add(localMaxButtonsBox);
		selectLocalMaxPanel.setVisible(false);
		/////////////////////////////////////////////

		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		setBorder(BorderFactory.createEmptyBorder(20,20,20,20));

		textArea = new JTextArea(SELECT_PARTICLES_TEXT);
		//textArea.setFont(normalFont);
		textArea.setEditable(false);
		textArea.setBackground(getBackground());
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		scrollPane = new JScrollPane(textArea); 
		scrollPane.setBorder(null);
		add(selectMethodPanel);
		add(scrollPane);
  
		add(selectParticlesPanel);
		add(selectLocalMaxPanel);
		
		imagesHandler.getSelectionProgress().addProgressFinishedListener(new ProgressFinishedListener() {
			@Override
			public void ProgressFinished(int processedCount) {
				IJ.showStatus("Selection is finished");
				ROIsCount.setText(Integer.toString(processedCount));				
				setNextButtonEnabled(imagesHandler.getSelectionStatus());
			}
		});
	}

	protected void applyThreshold() {
		if(imagesHandler.applyThreshold(!rbtnSelectedFrame.isSelected())) {
			selectParticlesBtn.setEnabled(true);
		}
	}

	protected void selectParticles() {

		try {
			imagesHandler.minSpotRectSize = Integer.parseInt(textFieldMinSize.getText());
		}
		catch(Exception ex) {
			IJ.showMessage("Could not convert the field Min Size to the number");
			return;
		}

		try {
			imagesHandler.maxSpotRectSize = Integer.parseInt(textFieldMaxSize.getText());
		}
		catch(Exception ex) {
			IJ.showMessage("Could not convert the field Max Size to the number");
			return;
		}

		try {
			imagesHandler.minCircularity = Double.parseDouble(textFieldCircularity.getText());
		}
		catch(Exception ex) {
			IJ.showMessage("Could not convert the field Circularity to the number");
			return;
		}

		if(imagesHandler.selectParticles()) {
			ROIsCount.setText(Integer.toString(imagesHandler.ROIsCount));
			applySelectionBtn.setEnabled(true);
			setNextButtonEnabled(false);
		}
	}
	
	protected void mySelection() {
		int fittingRad, minThreshold, maxThreshold;
		try {
			fittingRad = Integer.parseInt(textFieldFitRad.getText());
			minThreshold = Integer.parseInt(textFieldMinInt.getText());
			maxThreshold = Integer.parseInt(textFieldMaxInt.getText());
			if (minThreshold > maxThreshold) {
				throw new Exception("Min threshold more than max!");
			}
		}
		catch(Exception ex) {
			IJ.showMessage("Could not convert parameters to the number");
			return;
		}
		
		if(imagesHandler.selectParticlesByLocalMax(fittingRad,minThreshold,maxThreshold)) {
			ROIsCount.setText(Integer.toString(imagesHandler.ROIsCount));
			applySelectionBtn.setEnabled(true);
			setNextButtonEnabled(true);
		}
	}
	
	private void applyThresholdToRois() {
		imagesHandler.applyThresholdToRois();
	}
	
	//Plot image histogram to set thresholds correctly
	private void plotImageHistogram() {
		IJ.run("Histogram", "stack");
	}

	private void applySelection() {
		int selectionMode = 0;  //0 - all, range 1, selected 2
		
		int frameFrom = (Integer)spinnerFrameFrom.getValue();
		int frameTo = (Integer)spinnerFrameTo.getValue();
		
		if(rbtnRangeOfFrames.isSelected()) {
			selectionMode = 1;
			if(frameTo < frameFrom)  {
				IJ.showMessage("Frame to is less than frame from.");
				return;
			}
		}
		else if(rbtnSelectedFrame.isSelected())
			selectionMode = 2;			
			
		imagesHandler.applySelection(selectionMode, frameFrom, frameTo);
	}

	public void update() { 
		setNextButtonEnabled(imagesHandler.checkROIBeforeFitWithoutMessage());
		/*textFieldMinSize.setText(Integer.toString(imagesHandler.minSpotRectSize));
		textFieldMaxSize.setText(Integer.toString(imagesHandler.maxSpotRectSize));
		textFieldCircularity.setText(Double.toString(imagesHandler.minCircularity));
		
		spinnerModelFrameFrom.setMaximum(imagesHandler.imp_work.getImageStackSize());
		spinnerModelFrameTo.setMaximum(imagesHandler.imp_work.getImageStackSize());*/
    }

}
