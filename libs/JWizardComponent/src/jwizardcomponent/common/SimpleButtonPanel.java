package jwizardcomponent.common;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;

import jwizardcomponent.JWizardComponents;

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
public class SimpleButtonPanel extends JPanel {

  JLabel statusLabel = new JLabel();

  public SimpleButtonPanel(JWizardComponents wizardComponents) {
    this.setLayout(new GridBagLayout());
    this.add(statusLabel,  new GridBagConstraints(0, 0, 1, 1, 0.7, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(2, 0, 2, 0), 0, 0));
    this.add(wizardComponents.getBackButton(), new GridBagConstraints(1, 0, 1, 1, 0.1, 0.0
            ,GridBagConstraints.EAST, GridBagConstraints.BOTH, new Insets(2, 0, 2, 0), 0, 0));
    this.add(wizardComponents.getNextButton(), new GridBagConstraints(2, 0, 1, 1, 0.1, 0.0
            ,GridBagConstraints.EAST, GridBagConstraints.BOTH, new Insets(2, 0, 2, 0), 0, 0));
	this.add(wizardComponents.getFinishButton(), new GridBagConstraints(3, 0, 1, 1, 0.1, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.BOTH, new Insets(2, 0, 2, 0), 0, 0));    
    this.add(wizardComponents.getCancelButton(), new GridBagConstraints(4, 0, 1, 1, 0.1, 0.0
            ,GridBagConstraints.EAST, GridBagConstraints.BOTH, new Insets(2, 3, 2, 2), 0, 0));

  }

}