import ij.IJ;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import jwizardcomponent.JWizardComponents;
import jwizardcomponent.JWizardPanel;

@SuppressWarnings("serial")
public class SMLLoadingImagesPanel extends JWizardPanel {

	SMLImageHandler imagesHandler;
	private JButton openImageButton;
	private JButton importImageStackButton;
	private JButton linkToImageButton;
	
	public SMLLoadingImagesPanel(JWizardComponents wizardComponents, SMLImageHandler imagesHandler) {
		super(wizardComponents, "Loading images");
		this.imagesHandler = imagesHandler;
		
		openImageButton = new JButton(openImageAction);
		importImageStackButton = new JButton(importImageStackAction);
		linkToImageButton = new JButton(linkToImageAction);
		
		JPanel openImagesButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER,8,8));
		openImagesButtonsPanel.add(openImageButton);				
		openImagesButtonsPanel.add(importImageStackButton);		
		openImagesButtonsPanel.add(linkToImageButton);
		
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		setBorder(BorderFactory.createEmptyBorder(20,20,20,20));

		JTextArea textArea = new JTextArea("This plugin works with a stack of 8- or 16-bit grayscale images. Click Load image sequence button to create the image stack. If you already have a stack go to the next page.");
		//textArea.setFont(normalFont);
		textArea.setEditable(false);
		textArea.setBackground(getBackground());
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		JScrollPane scrollPane = new JScrollPane(textArea); 
		scrollPane.setBorder(null);
		add(scrollPane);
		
		add(scrollPane);
		add(openImagesButtonsPanel);		
	}

	//actions		
	final Action openImageAction = new AbstractAction("Open Image") { 
		public void actionPerformed(ActionEvent event) {
			IJ.open();
			imagesHandler.linkToActiveImageWindow();
		}
	};

	final Action importImageStackAction = new AbstractAction("Import Stack of Images") { 
		public void actionPerformed(ActionEvent event) {
			IJ.run("Image Sequence...");
			IJ.wait(3000);
			imagesHandler.linkToActiveImageWindow();				
		}
	};

	final Action linkToImageAction = new AbstractAction("Link to active Image Window") { 
		public void actionPerformed(ActionEvent event) {
			imagesHandler.linkToActiveImageWindow();
		}
	};

	public void next() {
		if(imagesHandler.imp_work == null) {
			imagesHandler.linkToActiveImageWindow();
		}
		if(imagesHandler.checkSourceImage())
			super.next();
	}
	
    public void update() {
    }
	
}
