import ij.IJ;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.TitledBorder;

import utils.SpringUtilities;
import jwizardcomponent.JWizardComponents;
import jwizardcomponent.JWizardPanel;

@SuppressWarnings("serial")
public class SMSuperImagePanel extends JWizardPanel {

	SMLImageHandler imagesHandler;

	private JButton plorSuperImageBtn;
	private JButton exportToASCIIBtn;
	private JLabel pointsCountLabel;
	private JTextField textFieldFilerMaxCriterion;
	private JTextField textFieldFilterMinCriterion;
	private JTextField textFieldFilterMaxFWHM_X;
	private JTextField textFieldFilterMaxFWHM_Y;
	private JTextField textFieldFilterMaxGauss;
	private JTextField textFieldFilterMinGauss;
	private JCheckBox checkBoxDoFilter;
	private JLabel maxCritLabel;
	private JLabel maxFWHMXLabel;
	private JLabel maxFWHMYLabel;
	private JLabel maxCritLabelS;
	private JLabel maxFWHMXLabelS;
	private JLabel maxFWHMYLabelS;
	private JLabel minCritLabel;
	private JLabel minCritLabelS;
	private JLabel minGaussLabel;
	private JLabel minGaussLabelS;
	private JLabel maxGaussLabel;
	private JLabel maxGaussLabelS;
	private JButton plotCritHistogramBtn;
	private JButton plotGaussHistogramBtn;
	private JButton plotFWHM_XHistogramBtn;
	private JButton plotFWHM_YHistogramBtn;
	
	public SMSuperImagePanel(JWizardComponents wizardComponents, final SMLImageHandler imagesHandler) {
		super(wizardComponents, "Super-resolution");
		this.imagesHandler = imagesHandler;

		JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER,2,2));
		
		JPanel panelFilterOptions = new JPanel(new SpringLayout());
		Font normalFont = javax.swing.UIManager.getDefaults().getFont("TextField.font");
		Color borderTitleColor = new Color(0, 70, 213);
		panelFilterOptions.setBorder(new CompoundBorder(BorderFactory.createTitledBorder(null, " Filter: ", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, normalFont, borderTitleColor), BorderFactory.createEmptyBorder(4,15,4,15)));

		JLabel l = new JLabel("Filter fit results?:");
		panelFilterOptions.add(l);
		checkBoxDoFilter = new JCheckBox("");
		checkBoxDoFilter.setSelected(false);
		checkBoxDoFilter.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				boolean en = checkBoxDoFilter.isSelected();  
				setEnabledFilterOptions(en);				
			}
		});
		l.setLabelFor(checkBoxDoFilter);
		panelFilterOptions.add(checkBoxDoFilter);
		l = new JLabel("");
		panelFilterOptions.add(l);
		
		maxCritLabel = new JLabel("Max criterion value:");
		panelFilterOptions.add(maxCritLabel);
		textFieldFilerMaxCriterion = new JTextField("10", 8);
		textFieldFilerMaxCriterion.setMinimumSize(textFieldFilerMaxCriterion.getPreferredSize());
		maxCritLabel.setLabelFor(textFieldFilerMaxCriterion);
		panelFilterOptions.add(textFieldFilerMaxCriterion);
		maxCritLabelS = new JLabel("[0.00 .. 0.00]");
		panelFilterOptions.add(maxCritLabelS);
		
		minCritLabel = new JLabel("Min criterion value:");
		panelFilterOptions.add(minCritLabel);
		textFieldFilterMinCriterion = new JTextField("10", 8);
		textFieldFilterMinCriterion.setMinimumSize(textFieldFilterMinCriterion.getPreferredSize());
		minCritLabel.setLabelFor(textFieldFilterMinCriterion);
		panelFilterOptions.add(textFieldFilterMinCriterion);
		minCritLabelS = new JLabel("[0.00 .. 0.00]");
		panelFilterOptions.add(minCritLabelS);

		maxFWHMXLabel = new JLabel("Max FWHM X (pixels):");
		panelFilterOptions.add(maxFWHMXLabel);		
		textFieldFilterMaxFWHM_X = new JTextField("10", 8);
		maxFWHMXLabel.setLabelFor(textFieldFilterMaxFWHM_X);
		panelFilterOptions.add(textFieldFilterMaxFWHM_X);
		maxFWHMXLabelS = new JLabel("[0.00 .. 0.00]");
		panelFilterOptions.add(maxFWHMXLabelS);	
		

		maxFWHMYLabel = new JLabel("Max FWHM Y (pixels):");
		panelFilterOptions.add(maxFWHMYLabel);		
		textFieldFilterMaxFWHM_Y = new JTextField("10", 8);
		maxFWHMYLabel.setLabelFor(textFieldFilterMaxFWHM_Y);
		panelFilterOptions.add(textFieldFilterMaxFWHM_Y);
		maxFWHMYLabelS = new JLabel("[0.00 .. 0.00]");
		panelFilterOptions.add(maxFWHMYLabelS);
		
		minGaussLabel = new JLabel("Min Gauss level:");
		panelFilterOptions.add(minGaussLabel);		
		textFieldFilterMinGauss = new JTextField("10", 8);
		minGaussLabel.setLabelFor(textFieldFilterMinGauss);
		panelFilterOptions.add(textFieldFilterMinGauss);
		minGaussLabelS = new JLabel("[0.00 .. 0.00]");
		panelFilterOptions.add(minGaussLabelS);
		
		maxGaussLabel = new JLabel("Max Gauss level:");
		panelFilterOptions.add(maxGaussLabel);		
		textFieldFilterMaxGauss = new JTextField("10", 8);
		maxGaussLabel.setLabelFor(textFieldFilterMaxGauss);
		panelFilterOptions.add(textFieldFilterMaxGauss);
		maxGaussLabelS = new JLabel("[0.00 .. 0.00]");
		panelFilterOptions.add(maxGaussLabelS);	
		
		//JPanel histButtonsBox = new JPanel(new GridLayout(2,2,8,8));
		JPanel histButtonsBox = new JPanel(new FlowLayout(FlowLayout.CENTER,2,2));
		JPanel resButtonsBox = new JPanel(new FlowLayout(FlowLayout.CENTER,2,2));
		
		plotCritHistogramBtn = new JButton("Criterion Histogram");
		plotCritHistogramBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				plotHistogram("Criterion");
			}
		});
		histButtonsBox.add(plotCritHistogramBtn);
		
		plotGaussHistogramBtn = new JButton("Gauss lvl Histogram");
		plotGaussHistogramBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				plotHistogram("Gauss level");
			}
		});
		histButtonsBox.add(plotGaussHistogramBtn);
		
		plotFWHM_XHistogramBtn = new JButton("FWHM X Histogram");
		plotFWHM_XHistogramBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				plotHistogram("X FWHM");
			}
		});
		histButtonsBox.add(plotFWHM_XHistogramBtn);
		
		plotFWHM_YHistogramBtn = new JButton("FWHM Y Histogram");
		plotFWHM_YHistogramBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				plotHistogram("Y FWHM");
			}
		});
		histButtonsBox.add(plotFWHM_YHistogramBtn);
		
		setEnabledFilterOptions(false);
		
		//Lay out the panel.
		SpringUtilities.makeCompactGrid(panelFilterOptions,
				7, 3, //rows, cols
				6, 6,        //initX, initY
				6, 6);       //xPad, yPad
		panel.add(panelFilterOptions);
		panel.add(histButtonsBox);
		
		plorSuperImageBtn = new JButton("Plot super-resolution and summed images");
		plorSuperImageBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				double maxCrit;
				double minCrit;
				double maxFWHM_X;
				double maxFWHM_Y;
				double maxGauss;
				double minGauss;
				try {
					maxCrit = Double.parseDouble(textFieldFilerMaxCriterion.getText());
					minCrit = Double.parseDouble(textFieldFilterMinCriterion.getText());
				}
				catch(Exception ex) {
					IJ.showMessage("Could not convert Max/Min criterions to the number");
					return;
				}
				
				try {
					maxFWHM_X = Double.parseDouble(textFieldFilterMaxFWHM_X.getText());
					maxFWHM_Y = Double.parseDouble(textFieldFilterMaxFWHM_Y.getText());
				}
				catch(Exception ex) {
					IJ.showMessage("Could not convert max FWHM X/Y to the number");
					return;
				}
				
				try {
					maxGauss = Double.parseDouble(textFieldFilterMaxGauss.getText());
					minGauss = Double.parseDouble(textFieldFilterMinGauss.getText());
				}
				catch(Exception ex) {
					IJ.showMessage("Could not convert Min/Max Gauss to the number");
					return;
				}
				imagesHandler.plotSuperImage(checkBoxDoFilter.isSelected(), minCrit, maxCrit, maxFWHM_X, maxFWHM_Y, minGauss, maxGauss);
				
				pointsCountLabel.setText(Integer.toString(imagesHandler.super_imagePointsCount));
			}
		});
		resButtonsBox.add(plorSuperImageBtn);
		//panel.add(plorSuperImageBtn);
		
		exportToASCIIBtn = new JButton("Export ROIs to ASCII file");
		exportToASCIIBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				exportToASCII();
			}
		});
		resButtonsBox.add(exportToASCIIBtn);
		//panel.add(exportToASCIIBtn);				
		panel.add(resButtonsBox);
		JLabel l3 = new JLabel("Super image points count:");
		//l.setFont(normalFont);
		l3.setAlignmentX(RIGHT_ALIGNMENT);		
		pointsCountLabel = new JLabel("");
		l3.setLabelFor(pointsCountLabel);

		JPanel statPanel = new JPanel(new FlowLayout(FlowLayout.CENTER,2,2));
		statPanel.add(l3);
		statPanel.add(pointsCountLabel);

		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

		JTextArea textArea = new JTextArea("Click Plot super-resolution and summed images button to plot the super-resolution and the summed images and prepare a list of SM positions for the export to ASCII file");
		textArea.setFont(normalFont);
		textArea.setEditable(false);
		textArea.setBackground(getBackground());
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		JScrollPane scrollPane = new JScrollPane(textArea); 
		scrollPane.setBorder(null);
		add(scrollPane);
  
		add(panel);
		add(statPanel);
	}

	protected void exportToASCII() {
		imagesHandler.exportROIsToASCIIFile();
	}

	public void setEnabledFilterOptions(boolean en) {
		maxCritLabel.setEnabled(en);
		textFieldFilerMaxCriterion.setEnabled(en);
		minCritLabel.setEnabled(en);
		textFieldFilterMinCriterion.setEnabled(en);
		maxFWHMXLabel.setEnabled(en);
		textFieldFilterMaxFWHM_X.setEnabled(en);
		maxFWHMYLabel.setEnabled(en);
		textFieldFilterMaxFWHM_Y.setEnabled(en);
		maxCritLabelS.setEnabled(en);
		minCritLabelS.setEnabled(en);
		maxFWHMXLabelS.setEnabled(en);
		maxFWHMYLabelS.setEnabled(en);
		minGaussLabel.setEnabled(en);
		minGaussLabelS.setEnabled(en);
		maxGaussLabel.setEnabled(en);
		maxGaussLabelS.setEnabled(en);
		textFieldFilterMinGauss.setEnabled(en);
		textFieldFilterMaxGauss.setEnabled(en);
		plotCritHistogramBtn.setEnabled(en);
		plotFWHM_XHistogramBtn.setEnabled(en);
		plotFWHM_YHistogramBtn.setEnabled(en);
		plotGaussHistogramBtn.setEnabled(en);
	}
	
	private void plotHistogram(String parameter) {
		imagesHandler.showFitResults();
		if (parameter.equals("Criterion")) {
			parameter = ij.measure.ResultsTable.getResultsTable().getColumnHeading(7);
			System.out.println(parameter);
		}
		IJ.run("Distribution...", "parameter=[" + parameter + "] or=100 and=0-0");
	}
	
	public void update() { 
		DecimalFormat df = (DecimalFormat)NumberFormat.getInstance();
		df.setMaximumFractionDigits(2);
		df.setGroupingUsed(false);
		DecimalFormatSymbols dottedDouble = df.getDecimalFormatSymbols();
		dottedDouble.setDecimalSeparator('.');
		df.setDecimalFormatSymbols(dottedDouble);
		
		textFieldFilerMaxCriterion.setText(df.format(imagesHandler.maxCriterion));
		textFieldFilterMinCriterion.setText(df.format(imagesHandler.minCriterion));
		textFieldFilterMaxFWHM_X.setText(df.format(imagesHandler.maxFWHM_X));		
		textFieldFilterMaxFWHM_Y.setText(df.format(imagesHandler.maxFWHM_Y));
		textFieldFilterMinGauss.setText(df.format(imagesHandler.minGauss));
		textFieldFilterMaxGauss.setText(df.format(imagesHandler.maxGauss));
		

		minCritLabelS.setText("[" + df.format(imagesHandler.minCriterion) + " .. " + df.format(imagesHandler.maxCriterion) + "]");
		maxCritLabelS.setText("[" + df.format(imagesHandler.minCriterion) + " .. " + df.format(imagesHandler.maxCriterion) + "]");
		maxFWHMXLabelS.setText("[" + df.format(imagesHandler.minFWHM_X) + " .. " + df.format(imagesHandler.maxFWHM_X) + "]");		
		maxFWHMYLabelS.setText("[" + df.format(imagesHandler.minFWHM_Y) + " .. " + df.format(imagesHandler.maxFWHM_Y) + "]");
		minGaussLabelS.setText("[" + df.format(imagesHandler.minGauss) + " .. " + df.format(imagesHandler.maxGauss) + "]");
		maxGaussLabelS.setText("[" + df.format(imagesHandler.minGauss) + " .. " + df.format(imagesHandler.maxGauss) + "]");
    }

	public void next() {
			super.next();
		
		if (getWizardComponents().onLastPanel()) {
			getWizardComponents().getNextButton().setVisible(false);
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					getWizardComponents().getFinishButton().setVisible(true);
					getWizardComponents().getFinishButton().requestFocus();
				}
			});
		} else {
			getWizardComponents().getNextButton().setVisible(true);
			getWizardComponents().getFinishButton().setVisible(false);  		
		}
	}

	public void back() {
		super.back();
		if (getWizardComponents().onLastPanel()) {
			getWizardComponents().getNextButton().setVisible(false);
			getWizardComponents().getFinishButton().setVisible(true);
		} else {
			getWizardComponents().getNextButton().setVisible(true);
			getWizardComponents().getFinishButton().setVisible(false);  		
		}
	}
	
}
