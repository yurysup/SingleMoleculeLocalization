package jwizardcomponent.example;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import jwizardcomponent.JWizardComponents;
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
 * @author William Ready
 * @version 1.0
 */

@SuppressWarnings("serial")
public class SimpleLabelWizardPanel extends JWizardPanel {

  public SimpleLabelWizardPanel(JWizardComponents wizardComponents, JLabel label) {
    super(wizardComponents);
    this.setLayout(new GridBagLayout());
    this.add(label,
             new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
             ,GridBagConstraints.CENTER, GridBagConstraints.BOTH,
             new Insets(0, 0, 0, 0), 0, 0));
	if (!getWizardComponents().onLastPanel()) {
		getWizardComponents().getFinishButton().setVisible(false);
	}
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