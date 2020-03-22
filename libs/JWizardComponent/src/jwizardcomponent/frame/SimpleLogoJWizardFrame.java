package jwizardcomponent.frame;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import jwizardcomponent.CancelAction;
import jwizardcomponent.FinishAction;
import jwizardcomponent.DefaultJWizardComponents;
import jwizardcomponent.common.*;

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
public class SimpleLogoJWizardFrame extends JFrame {

  DefaultJWizardComponents wizardComponents;

  JPanel buttonPanel;
  JLabel statusLabel = new JLabel();

  ImageIcon logo;

  public SimpleLogoJWizardFrame(ImageIcon logo) {
    this.logo = logo;
    wizardComponents = new DefaultJWizardComponents();
    init();
  }

  private void init() {
    this.getContentPane().setLayout(new GridBagLayout());

    JPanel logoPanel = new JPanel();

    String fileString;
    if (logo.toString().indexOf("file:") < 0 &&
        logo.toString().indexOf("http:") < 0) {
      fileString = "file:///" +System.getProperty("user.dir") +"/"
                        +logo.toString();
      fileString = fileString.replaceAll("\\\\", "/");
    } else {
      fileString = logo.toString();
    }
    logoPanel.add(new JLabel("<html><img src='" +fileString +"'></html>"),
                  BorderLayout.CENTER);
    logoPanel.setBackground(Color.WHITE);
    this.getContentPane().add(logoPanel,
                              new GridBagConstraints(0, 0, 1, 1, 0.3, 0.9
                              , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                              new Insets(0, 0, 0, 0), 0, 0));
    this.getContentPane().add(wizardComponents.getWizardPanelsContainer(),
                              new GridBagConstraints(1, 0, 1, 1, 0.7, 0.9
                              , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                              new Insets(0, 0, 0, 0), 0, 0));

    this.getContentPane().add(new JSeparator(),
                              new GridBagConstraints(0, 1, 2, 0, 1.0, 0.01
                              ,GridBagConstraints.WEST, GridBagConstraints.BOTH,
                              new Insets(1, 1, 1, 1), 0, 0));

    buttonPanel = new SimpleButtonPanel(wizardComponents);
    this.getContentPane().add(buttonPanel,
                              new GridBagConstraints(0, 2, 2, 1, 1.0, 0.09
                              ,GridBagConstraints.WEST, GridBagConstraints.BOTH,
                              new Insets(0, 0, 0, 0), 0, 0));

    wizardComponents.setFinishAction(new FinishAction(wizardComponents) {
      public void performAction() {
        dispose();
      }
    });
    wizardComponents.setCancelAction(new CancelAction(wizardComponents) {
      public void performAction() {
        dispose();
      }
    });
  }

  public DefaultJWizardComponents getWizardComponents(){
    return wizardComponents;
  }

  public void setWizardComponents(DefaultJWizardComponents aWizardComponents){
    wizardComponents = aWizardComponents;
  }

  public void showWizard() {
    wizardComponents.updateComponents();
    setVisible(true);
  }

}