import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.TitledBorder;

import fit.Gauss2DFit.TFCType;
import utils.ProgressFinishedListener;
import utils.SpringUtilities;
import jwizardcomponent.JWizardComponents;
import jwizardcomponent.JWizardPanel;

import ij.IJ;

@SuppressWarnings("serial")
public class SMLFitPanel extends JWizardPanel {
	SMLImageHandler imagesHandler;
	
	private JButton fitAllSpotsBtn;
	private JButton fitSelectedSpotBtn;
	private JButton showFitResultsBtn;
	private JButton showSelectedFitResultsBtn;
	private JRadioButton chi2RadioButton;
	private JRadioButton MLERadioButton;
	private JCheckBox enableWeightCheckBox;
	private JTextField textFieldMaxFitCriterionValue;
	String strTFC = "Chi-square";
	private JLabel fitCountLabel;

	public SMLFitPanel(JWizardComponents wizardComponents, final SMLImageHandler imagesHandler) {
		super(wizardComponents, "2D Gauss Fitting");
		this.imagesHandler = imagesHandler;
		
		chi2RadioButton = new JRadioButton("Chi-square (least-squares fit)   ");
		chi2RadioButton.setSelected(true);
		MLERadioButton = new JRadioButton("MLE Poissonian");
		ButtonGroup fitTypeRadioButtons = new ButtonGroup();
		fitTypeRadioButtons.add(chi2RadioButton);
		fitTypeRadioButtons.add(MLERadioButton);
		
		enableWeightCheckBox = new JCheckBox("Weighted Chi-square?");
		enableWeightCheckBox.setEnabled(chi2RadioButton.isSelected());
		
		chi2RadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				enableWeightCheckBox.setEnabled(chi2RadioButton.isSelected());
				//enableBgWeightCheckBox.setEnabled(chi2RadioButton.isSelected() && enableWeightCheckBox.isSelected());
				strTFC = "Chi-square";
			}
		} );
		MLERadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				enableWeightCheckBox.setEnabled(chi2RadioButton.isSelected());
				//enableBgWeightCheckBox.setEnabled(chi2RadioButton.isSelected() && enableWeightCheckBox.isSelected());
				strTFC = "MLE Poissonian";
			}
		} ); 
		
		JLabel l2 = new JLabel("    Max fit criterion value:");
		//l.setFont(normalFont);
		l2.setAlignmentX(RIGHT_ALIGNMENT);		
		textFieldMaxFitCriterionValue = new JTextField("100", 10);
		textFieldMaxFitCriterionValue.setMaximumSize(textFieldMaxFitCriterionValue.getPreferredSize());
		//textFieldMaxFitCriterionValue(normalFont);
		l2.setLabelFor(textFieldMaxFitCriterionValue);
		
		JPanel panelFitOptions = new JPanel(new SpringLayout());
		Font normalFont = javax.swing.UIManager.getDefaults().getFont("TextField.font");
		Color borderTitleColor = new Color(0, 70, 213);
		panelFitOptions.setBorder(new CompoundBorder(BorderFactory.createTitledBorder(null, " Fit options: ", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, normalFont, borderTitleColor), BorderFactory.createEmptyBorder(0,5,5,0)));
		panelFitOptions.add(chi2RadioButton);
		panelFitOptions.add(enableWeightCheckBox);
		panelFitOptions.add(new JLabel(""));
		
		panelFitOptions.add(MLERadioButton);
		panelFitOptions.add(l2);		
		panelFitOptions.add(textFieldMaxFitCriterionValue);
		
		//Lay out the panel.
		SpringUtilities.makeCompactGrid(panelFitOptions,
		                                2, 3, //rows, cols
		                                6, 6,        //initX, initY
		                                6, 6);       //xPad, yPad
		
		//fit panel
		fitAllSpotsBtn = new JButton("Fit all spots");
		//fitAllSpotsBtn.setEnabled(false);
		//fitAllSpotsBtn.setFont(normalFont);
		fitAllSpotsBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doGaussFit();
			}
		});
		
		fitSelectedSpotBtn = new JButton("Fit selected");
		//fitSelectedSpotBtn.setEnabled(false);
		//fitSelectedSpotBtn.setFont(normalFont);
		fitSelectedSpotBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doSingleGaussFit();
			}
		});
		
		showFitResultsBtn = new JButton("Show fit results");
		//showFitResultsBtn.setEnabled(false);
		//showFitResultsBtn.setFont(normalFont);
		showFitResultsBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showFitResults();
			}
		});
		
		showSelectedFitResultsBtn = new JButton("Show selected");
		//showSelectedFitResultsBtn.setEnabled(false);
		//showSelectedFitResultsBtn.setFont(normalFont);
		showSelectedFitResultsBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showSelectedFitResults();
			}
		});

		JPanel fitPanel = new JPanel(new FlowLayout(FlowLayout.CENTER,10,2));
		fitPanel.add(fitAllSpotsBtn);				
		fitPanel.add(showFitResultsBtn);		
		fitPanel.add(fitSelectedSpotBtn);
		fitPanel.add(showSelectedFitResultsBtn);
		
		JLabel l3 = new JLabel("      Succesful fit count:");
		//l.setFont(normalFont);
		l3.setAlignmentX(RIGHT_ALIGNMENT);		
		fitCountLabel = new JLabel("");
		l3.setLabelFor(fitCountLabel);

		JPanel statPanel = new JPanel(new FlowLayout(FlowLayout.LEFT,10,2));
		statPanel.add(l3);
		statPanel.add(fitCountLabel);		
		
		//main papel layout
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		setBorder(BorderFactory.createEmptyBorder(10,20,10,20));

		JTextArea textArea = new JTextArea("Perform 2D Gausss fit of localized emmiters. You can preview fit results and perform a fit of a selected spot. To preview graphical fit results click Show selected button. Use ROI manager to select an emitter.");
		//textArea.setFont(normalFont);
		textArea.setEditable(false);
		textArea.setBackground(getBackground());
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		JScrollPane scrollPane = new JScrollPane(textArea); 
		scrollPane.setBorder(null);
		add(scrollPane);
		
		add(panelFitOptions);
		add(fitPanel);
		add(statPanel);
		
		imagesHandler.getFitProgress().addProgressFinishedListener(new ProgressFinishedListener() {
			@Override
			public void ProgressFinished(int processedCount) {
				IJ.showStatus("Fit is finished");
				fitCountLabel.setText(Integer.toString(processedCount));
				setNextButtonEnabled(imagesHandler.checkFitResults());
			}
		});
	}

	protected void setCurrentColor(int index) {
		imagesHandler.imp_work.setSlice(index);
	}

	protected void showSelectedFitResults() {
		imagesHandler.showSelectedFitResults();
		
	}

	protected void showFitResults() {
		imagesHandler.showFitResults();
		
	}

	protected void doSingleGaussFit() {
		if(imagesHandler.setFitSettings(strTFC, enableWeightCheckBox.isSelected(), textFieldMaxFitCriterionValue.getText()))		
			imagesHandler.doSingleGaussFit();
	}

	protected boolean doGaussFit() {
		if(imagesHandler.setFitSettings(strTFC, enableWeightCheckBox.isSelected(), textFieldMaxFitCriterionValue.getText()))
			return imagesHandler.doGaussFit();
		else
			return false;
		
	}

	public void next() {
		if(imagesHandler.checkFitResults())
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
	
    public void update() {
		setNextButtonEnabled(imagesHandler.checkFitResultsWithoutMessage());
    	chi2RadioButton.setSelected(imagesHandler.GaussXYFit.activeTFC == TFCType.ChiSquare);
    	textFieldMaxFitCriterionValue.setText(Double.toString(imagesHandler.maxChi2Value));
    	enableWeightCheckBox.setSelected(imagesHandler.GaussXYFit.weightedFit);
    	//enableBgWeightCheckBox.setSelected(imagesHandler.GaussXYFit.estimateBgWeight);    
		enableWeightCheckBox.setEnabled(chi2RadioButton.isSelected());
		//enableBgWeightCheckBox.setEnabled(chi2RadioButton.isSelected() && enableWeightCheckBox.isSelected());
    }

    public void setNextButtonEnabled() {
        setNextButtonEnabled(true);    	
    }
    
}

