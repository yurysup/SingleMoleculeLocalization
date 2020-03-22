package jwizardcomponent;

import javax.swing.JPanel;

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
 * @version 1.1
 * 
 * Minor modifications made by Piotr Kamiñski.
 */

@SuppressWarnings("serial")
public abstract class JWizardPanel extends JPanel {
    
    private JWizardComponents wizardComponents;
    private String panelTitle;
    
    public JWizardPanel(JWizardComponents wizardComponents) {
        this(wizardComponents, null);
    }
    
    public JWizardPanel(JWizardComponents wizardComponents, String title) {
        this.wizardComponents = wizardComponents;
        this.panelTitle = title;
    }
    
    public void update() {
    }
    
    public void next() {
        goNext();
    }
    
    public void back() {
        goBack();
    }
    
    public JWizardComponents getWizardComponents(){
        return wizardComponents;
    }
    
    public void setWizardComponents(JWizardComponents awizardComponents){
        wizardComponents = awizardComponents;
    }
    
    public String getPanelTitle() {
        return panelTitle;
    }
    
    public void setPanelTitle(String title) {
        panelTitle = title;
    }
    
    protected boolean goNext() {
        if (wizardComponents.getWizardPanelList().size() > wizardComponents.getCurrentIndex()+1 ) {
            wizardComponents.setCurrentIndex(wizardComponents.getCurrentIndex()+1);
            wizardComponents.updateComponents();
            return true;
        } else {
            return false;
        }
    }
    
    protected boolean goBack() {
        if (wizardComponents.getCurrentIndex()-1 >= 0) {
            wizardComponents.setCurrentIndex(wizardComponents.getCurrentIndex()-1);
            wizardComponents.updateComponents();
            return true;
        } else {
            return false;
        }
    }
    
    protected void switchPanel(int panelIndex) {
        getWizardComponents().setCurrentIndex(panelIndex);
        getWizardComponents().updateComponents();
    }
    
    protected void setBackButtonEnabled(boolean set) {
        wizardComponents.getBackButton().setEnabled(set);
    }
    
    protected void setNextButtonEnabled(boolean set) {
        wizardComponents.getNextButton().setEnabled(set);
    }
    
    protected void setFinishButtonEnabled(boolean set) {
        wizardComponents.getFinishButton().setEnabled(set);
    }
}