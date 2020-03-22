package jwizardcomponent.frame;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

import jwizardcomponent.CancelAction;
import jwizardcomponent.FinishAction;
import jwizardcomponent.JWizardComponents;
import jwizardcomponent.DefaultJWizardComponents;
import jwizardcomponent.JWizardPanel;

/**
 * <p>Title: JWizardComponent</p>
 * <p>Description: Swing-Based Wizard Framework for Wizards</p>
 * <p>Copyright (C) 2003 William Ready
 *
 * <br>This library is free software; you can redistribute it and/or
 * <br>modify it under the terms of the GNU Lesser General Public
 * <br>License as published by the Free Software Foundation; either
 * <br>version 2.1 of the License, or (at your option) any later version.
 *
 * <br>This library is distributed in the hope that it will be useful,
 * <br>but WITHOUT ANY WARRANTY; without even the implied warranty of
 * <br>MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * <br>See the GNU Lesser General Public License for more details.
 *
 * <br>To receive a copy of the GNU Lesser General Public License
 * <br>write to:  The Free Software Foundation, Inc.,
 * <br>59 Temple Place, Suite 330
 * <br>Boston, MA 02111-1307 USA</p>
 * @author Piotr Kamiñski
 * @version 1.0
 */

@SuppressWarnings("serial")
public class JWizardFrame extends JFrame {
    
    private JWizardComponents wizardComponents;
    
    private JPanel buttonPanel;
    private JLabel panelTitleLabel;

    public JWizardFrame() {
        init();
    }
    
    private void init() {
        wizardComponents = new DefaultJWizardComponents();
        wizardComponents.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent event) {
                setPanelTitle(((JWizardPanel)event.getNewValue()).getPanelTitle());
            }
        });
        
        this.getContentPane().setLayout(new GridBagLayout());
        this.getContentPane().add(createTitlePanel()
        , new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0
        , GridBagConstraints.CENTER, GridBagConstraints.BOTH
        , new Insets(5, 5, 5, 5), 0, 0));
        
        this.getContentPane().add(new JSeparator()
        , new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0
        , GridBagConstraints.WEST, GridBagConstraints.BOTH,
        new Insets(1, 1, 1, 1), 0, 0));
        
        this.getContentPane().add(wizardComponents.getWizardPanelsContainer()
        , new GridBagConstraints(0, 2, 1, 1, 1.0, 1.0
        , GridBagConstraints.CENTER, GridBagConstraints.BOTH
        , new Insets(0, 0, 0, 0), 0, 0));
        
        this.getContentPane().add(new JSeparator()
        , new GridBagConstraints(0, 3, 1, 1, 1.0, 0.0
        , GridBagConstraints.WEST, GridBagConstraints.BOTH
        , new Insets(1, 1, 1, 1), 0, 0));

        createButtonPanel();
       // buttonPanel = new SimpleButtonPanel(wizardComponents);
        this.getContentPane().add(buttonPanel,
        new GridBagConstraints(0, 4, 1, 1, 1.0, 0.0
        ,GridBagConstraints.EAST, GridBagConstraints.NONE,
        new Insets(5, 5, 5, 5), 0, 0));
        
        wizardComponents.setFinishAction(createFinishAction());
        wizardComponents.setCancelAction(createCancelAction());
        handleWindowClosing();
    }
    
    public JWizardComponents getWizardComponents(){
        return wizardComponents;
    }
    
    public void setWizardComponents(JWizardComponents aWizardComponents){
        wizardComponents = aWizardComponents;
    }
    
    protected void setPanelTitle(String title) {
        panelTitleLabel.setText(title);
    }
    
    protected JPanel createTitlePanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelTitleLabel = new JLabel();
        panelTitleLabel.setHorizontalAlignment(SwingConstants.LEADING);
        Font boldFont = new Font("Tahoma", Font.BOLD, 12);
        panelTitleLabel.setFont(boldFont);
        panel.add(panelTitleLabel);
        return panel;
    }
    
    protected void createButtonPanel() {
        buttonPanel = new JPanel(new GridLayout());
        buttonPanel.add(wizardComponents.getSettingsButton());
        buttonPanel.add(new JLabel());        
        buttonPanel.add(wizardComponents.getBackButton());
        buttonPanel.add(wizardComponents.getNextButton());
        buttonPanel.add(wizardComponents.getFinishButton());
        buttonPanel.add(wizardComponents.getCancelButton());
    }
    
    protected FinishAction createFinishAction() {
        return new FinishAction(wizardComponents) {
            public void performAction() {
                System.out.println("FinishAction performed");
                dispose();
            }
        };
    }
    
    protected CancelAction createCancelAction() {
        return new CancelAction(wizardComponents) {
            public void performAction() {
                System.out.println("CancelAction performed");
                dispose();
            }
        };
    }
    
    protected void handleWindowClosing() {
        this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                wizardComponents.getCancelAction().performAction();
            }
        });
    }
    
	public void showWizard() {
		wizardComponents.updateComponents();
		setVisible(true);
	}
}