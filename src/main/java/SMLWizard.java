
import ij.IJ;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import jwizardcomponent.CancelAction;
import jwizardcomponent.FinishAction;
import jwizardcomponent.JWizardComponents;
import jwizardcomponent.SettingsAction;
import jwizardcomponent.Utilities;
import jwizardcomponent.example.SimpleLabelWizardPanel;
import jwizardcomponent.frame.JWizardFrame;

@SuppressWarnings("serial")
public class SMLWizard extends JWizardFrame{

	JWizardComponents components;
	SMLImageHandler imageHandler;
	
	public SMLWizard(SMLImageHandler handler) {
		this.imageHandler = handler;
		
		try {
			
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			UIManager.getDefaults().put("TextArea.font", UIManager.getFont("TextField.font"));
			SwingUtilities.updateComponentTreeUI(this);

			setTitle("SM Localization");

			components = getWizardComponents();

			components.addWizardPanel(
					new SMLLoadingImagesPanel(components, imageHandler));

			components.addWizardPanel(
					new SMLLocalizationPanel(components, imageHandler));

			components.addWizardPanel(
					new SMLFitPanel(components, imageHandler));
			
			components.addWizardPanel(
					new SMSuperImagePanel(components, imageHandler));

			components.addWizardPanel(new SimpleLabelWizardPanel(components, new JLabel("Click Finish button to finalize the wizard")));			
			
			components.setFinishAction(new FinishAction(components) {
				public void performAction() {
					setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
					imageHandler.closeOpenedWindows(false);
					setVisible(false);					
					dispose();
				}
			});
			
			components.setCancelAction(new CancelAction(components) {
				public void performAction() {
					setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);					
					imageHandler.closeOpenedWindows(true);					
					setVisible(false);
					dispose();
				}
			});

			components.getSettingsButton().setVisible(true);
			components.setSettingsAction(new SettingsAction(components) {
				public void performAction() {
					imageHandler.advancedSettings();
				}
			});
			
			imageHandler.readSettingsFromFile();

			setSize(580, 512);
			Utilities.centerComponentOnScreen(this);
			showWizard();

		} catch (Exception e) {
			e.printStackTrace();
			IJ.showMessage(e.getMessage());
		}	
	}

	
	final Action aboutAction = new AbstractAction("About") { 
		public void actionPerformed(ActionEvent event) {
			IJ.showMessage("Video SM Localisation. dSTORM.\nby Victor Skakun, BSU, Minsk, Belarus and EuroPhoton GmbH, Germany.");
		}
	};

	
 }
