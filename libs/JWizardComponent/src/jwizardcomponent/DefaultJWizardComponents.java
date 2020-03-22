package jwizardcomponent;

import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import javax.swing.JButton;
import javax.swing.JPanel;

/**
 * <p>Title: DefaultJWizardComponents</p>
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
 * @version 1.1
 * 
 * Localization and Property Change Listening
 * implemented by Piotr Kamiñski.
 */

public class DefaultJWizardComponents implements JWizardComponents {

	private java.util.ResourceBundle resourceBundle =
		java.util.ResourceBundle.getBundle("jwizardcomponent/i18n");

	JButton backButton;
	JButton nextButton;
	JButton finishButton;
	JButton cancelButton;
	JButton settingsButton;

	FinishAction finishAction;
	CancelAction cancelAction;
	SettingsAction settingsAction;	

	List<JWizardPanel> panelList;
	int currentIndex;
	JPanel wizardPanelsContainer;
	PropertyChangeSupport propertyChangeListeners;

	/**
	 * This class is the "bread and butter" of this framework.  All of these
	 * components can be used visually however you want, as shown in the
	 * frame and example packages, but all a developer really needs is this,
	 * and they can even instead implement JWizard and choose to do this
	 * portion any way they wish.
	 */
	public DefaultJWizardComponents() {
		try {
			init();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void addWizardPanel(JWizardPanel panel) {
		getWizardPanelList().add(panel);
		wizardPanelsContainer.add(panel,
				getWizardPanelList().size() - 1 + "");
	}

	public void addWizardPanel(int index, JWizardPanel panel) {
		getWizardPanelList().add(index, panel);
		wizardPanelsContainer.add(panel, index + "", index);
		if (index < getWizardPanelList().size() - 1) {
			for (int i = index + 1; i < getWizardPanelList().size(); i++) {
				wizardPanelsContainer.add(
						(JWizardPanel)getWizardPanelList().get(i),
						i + "");
			}
		}
	}

	public void addWizardPanelAfter(
			JWizardPanel panelToBePlacedAfter,
			JWizardPanel panel) {
		addWizardPanel(
				getWizardPanelList().indexOf(panelToBePlacedAfter) + 1,
				panel);
	}

	public void addWizardPanelBefore(
			JWizardPanel panelToBePlacedBefore,
			JWizardPanel panel) {
		addWizardPanel(
				getWizardPanelList().indexOf(panelToBePlacedBefore) - 1,
				panel);
	}

	public void addWizardPanelAfterCurrent(JWizardPanel panel) {
		addWizardPanel(getCurrentIndex()+1, panel);
	}

	public JWizardPanel removeWizardPanel(JWizardPanel panel) {
		int index = getWizardPanelList().indexOf(panel);
		getWizardPanelList().remove(panel);
		wizardPanelsContainer.remove(panel);
		for (int i = index; i < getWizardPanelList().size(); i++) {
			wizardPanelsContainer.add(
					(JWizardPanel) getWizardPanelList().get(i),
					i + "");
		}
		return panel;
	}

	public JWizardPanel removeWizardPanel(int index) {
		wizardPanelsContainer.remove(index);
		JWizardPanel panel = (JWizardPanel) getWizardPanelList().remove(index);
		for (int i = index; i < getWizardPanelList().size(); i++) {
			wizardPanelsContainer.add(
					(JWizardPanel) getWizardPanelList().get(i),
					i + "");
		}
		return panel;
	}

	public JWizardPanel removeWizardPanelAfter(JWizardPanel panel) {
		return removeWizardPanel(getWizardPanelList().indexOf(panel) + 1);
	}

	public JWizardPanel removeWizardPanelBefore(JWizardPanel panel) {
		return removeWizardPanel(getWizardPanelList().indexOf(panel) - 1);
	}

	public JWizardPanel getWizardPanel(int index) {
		return (JWizardPanel) getWizardPanelList().get(index);
	}

	public int getIndexOfPanel(JWizardPanel panel) {
		return getWizardPanelList().indexOf(panel);
	}

	public boolean onLastPanel() {
		return (getCurrentIndex() == getWizardPanelList().size() - 1);
	}

	private void init() throws Exception {
		this.propertyChangeListeners = new PropertyChangeSupport(this);

		backButton = new JButton();
		nextButton = new JButton();
		finishButton = new JButton();
		cancelButton = new JButton();
		settingsButton = new JButton();

		panelList = new ArrayList<JWizardPanel>();
		currentIndex = 0;
		wizardPanelsContainer = new JPanel();

		backButton.setText(resourceBundle.getString("L_BackButton"));
		backButton.setMnemonic(resourceBundle.getString("L_BackButtonMnem").charAt(0)); 
		backButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				backButton_actionPerformed(e);
			}
		});

		nextButton.setText(resourceBundle.getString("L_NextButton"));
		nextButton.setMnemonic(resourceBundle.getString("L_NextButtonMnem").charAt(0));
		nextButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				nextButton_actionPerformed(e);
			}
		});

		cancelButton.setText(resourceBundle.getString("L_CancelButton"));
		cancelButton.setMnemonic(resourceBundle.getString("L_CancelButtonMnem").charAt(0));
		cancelButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cancelButton_actionPerformed(e);
			}
		});

		finishButton.setText(resourceBundle.getString("L_FinishButton"));
		finishButton.setMnemonic(resourceBundle.getString("L_FinishButtonMnem").charAt(0));
		finishButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				finishButton_actionPerformed(e);
			}
		});

		settingsButton.setText("Settings");
		settingsButton.setVisible(false);
		settingsButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				settingsButton_actionPerformed(e);
			}
		});

		wizardPanelsContainer.setLayout(new CardLayout());
	}

	void settingsButton_actionPerformed(ActionEvent e) {
		getSettingsAction().performAction();
	}

	void cancelButton_actionPerformed(ActionEvent e) {
		getCancelAction().performAction();
	}

	void finishButton_actionPerformed(ActionEvent e) {
		getFinishAction().performAction();
	}

	void nextButton_actionPerformed(ActionEvent e) {
		try {
			getCurrentPanel().next();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	void backButton_actionPerformed(ActionEvent e) {
		try {
			getCurrentPanel().back();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public JWizardPanel getCurrentPanel() throws Exception {
		if (getWizardPanelList().get(currentIndex) != null) {
			return (JWizardPanel) getWizardPanelList().get(currentIndex);
		} else {
			throw new Exception("No panels in panelList");
		}
	}

	public void updateComponents() {
		try {
			CardLayout cl = (CardLayout) (wizardPanelsContainer.getLayout());
			cl.show(wizardPanelsContainer, currentIndex + "");

			if (currentIndex == 0) {
				backButton.setEnabled(false);
			} else {
				backButton.setEnabled(true);
			}

			if (onLastPanel()) {
				nextButton.setEnabled(false);
				finishButton.setEnabled(true);
			} else {
				finishButton.setEnabled(false);
				nextButton.setEnabled(true);
			}
			// let panel to update itself
			getCurrentPanel().update();

			// inform PropertyChangeListeners
			PropertyChangeEvent event = new PropertyChangeEvent(this, CURRENT_PANEL_PROPERTY
					, null,  getCurrentPanel());
			propertyChangeListeners.firePropertyChange(event);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Getters and Setters from here on ...

	public List<JWizardPanel> getWizardPanelList() {
		return this.panelList;
	}

	public void setWizardPanelList(List<JWizardPanel> panelList) {
		this.panelList = panelList;
	}

	public FinishAction getFinishAction() {
		return finishAction;
	}

	public void setFinishAction(FinishAction aFinishAction) {
		finishAction = aFinishAction;
	}

	public CancelAction getCancelAction() {
		return cancelAction;
	}

	public void setCancelAction(CancelAction aCancelAction) {
		cancelAction = aCancelAction;
	}

	public SettingsAction getSettingsAction() {
		return settingsAction;
	}

	public void setSettingsAction(SettingsAction aSettingsAction) {
		settingsAction = aSettingsAction;
	}
	
	public int getCurrentIndex() {
		return currentIndex;
	}

	public void setCurrentIndex(int aCurrentIndex) {
		currentIndex = aCurrentIndex;
	}

	public JPanel getWizardPanelsContainer() {
		return wizardPanelsContainer;
	}

	public void setWizardPanelsContainer(JPanel aWizardPanelsContainer) {
		wizardPanelsContainer = aWizardPanelsContainer;
	}

	public JButton getBackButton() {
		return backButton;
	}

	public void setBackButton(JButton aBackButton) {
		backButton = aBackButton;
	}

	public JButton getNextButton() {
		return nextButton;
	}

	public void setNextButton(JButton aNextButton) {
		nextButton = aNextButton;
	}

	public JButton getCancelButton() {
		return cancelButton;
	}

	public void setCancelButton(JButton aCancelButton) {
		cancelButton = aCancelButton;
	}

	public JButton getFinishButton() {
		return finishButton;
	}

	public void setFinishButton(JButton button) {
		finishButton = button;
	}

	public JButton getSettingsButton() {
		return settingsButton;
	}

	public void setSettingsButton(JButton button) {
		settingsButton = button;
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeListeners.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeListeners.removePropertyChangeListener(listener);
	}

	/**
	 * @return Returns the resourceBundle.
	 */
	 public java.util.ResourceBundle getResourceBundle() {
		 return resourceBundle;
	 }

	 /**
	  * @param resourceBundle The resourceBundle to set.
	  */
	 public void setResourceBundle(
			 java.util.ResourceBundle resourceBundle) {
		 this.resourceBundle = resourceBundle;
	 }

}