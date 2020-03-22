/*
 * Created on Oct 15, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package jwizardcomponent;

import java.util.List;
import java.beans.PropertyChangeListener;

import javax.swing.JButton;
import javax.swing.JPanel;

/**
 * <p>Title: JWizardComponents.java</p>
 * <p>Project: JWizardComponent</p>
 * <p>Copyright (C) 2003 William Ready
 * 
 * <br>This library is free software; you can redistribute it and/or
 * <br>modify it under the terms of the GNU Lesser General Public
 * <br>License as published by the Free Software Foundation; either
 * <br>version 2.1 of the License, or (at your option) any later version.
 *
 * <br>This library is distributed in the hope that it will be useful,
 * <br>but WITHOUT ANY WARRANTY; without even the 
 * <br>implied warranty of MERCHANTABILITY or 
 * <br>FITNESS FOR A PARTICULAR PURPOSE. 
 * <br>See the GNU Lesser General Public License for more details.
 *
 * <br>To receive a copy of the GNU Lesser General Public License 
 * <br>write to:  The Free Software Foundation, Inc., 
 * <br>59 Temple Place, Suite 330 
 * <br>Boston, MA 02111-1307 USA</p>
 * @author William Ready
 * @version 1.1
 * 
 * Property Change Listening implemented by Piotr Kamiñski.
 */
public interface JWizardComponents extends JWizard {

	public void addWizardPanel(JWizardPanel panel);
	
	public void addWizardPanel(int index, JWizardPanel panel);

	public void addWizardPanelAfter(
		JWizardPanel panelToBePlacedAfter,
		JWizardPanel panel);

	public void addWizardPanelBefore(
		JWizardPanel panelToBePlacedBefore,
		JWizardPanel panel);
		
	public void addWizardPanelAfterCurrent(JWizardPanel panel);
	
	public JWizardPanel removeWizardPanel(JWizardPanel panel);

	public JWizardPanel removeWizardPanel(int index);

	public JWizardPanel removeWizardPanelAfter(JWizardPanel panel);

	public JWizardPanel removeWizardPanelBefore(JWizardPanel panel);

	public JWizardPanel getWizardPanel(int index);

	public int getIndexOfPanel(JWizardPanel panel);	

	public void updateComponents();

	public JWizardPanel getCurrentPanel() throws Exception;

	public FinishAction getFinishAction();

	public void setFinishAction(FinishAction aFinishAction);

	public CancelAction getCancelAction();
	
	public void setCancelAction(CancelAction aCancelAction);

	public SettingsAction getSettingsAction();
	
	public void setSettingsAction(SettingsAction aSettingsAction);
	
	public int getCurrentIndex();

	public void setCurrentIndex(int aCurrentIndex);

	public JPanel getWizardPanelsContainer();

	public void setWizardPanelsContainer(JPanel aWizardPanelsContainer);

	public JButton getBackButton();

	public void setBackButton(JButton aBackButton);

	public JButton getNextButton();

	public void setNextButton(JButton aNextButton);

	public JButton getCancelButton();

	public void setCancelButton(JButton aCancelButton);

	public JButton getFinishButton();

	public void setFinishButton(JButton button);
	
	public JButton getSettingsButton();

	public void setSettingsButton(JButton button);
	
	public List<JWizardPanel> getWizardPanelList();

	public void setWizardPanelList(List<JWizardPanel> panelList);
	
	public boolean onLastPanel();		
	
        public final static String CURRENT_PANEL_PROPERTY = "currentPanel";
        
        public void addPropertyChangeListener(PropertyChangeListener listener);
        
        public void removePropertyChangeListener(PropertyChangeListener listener);
        
}
