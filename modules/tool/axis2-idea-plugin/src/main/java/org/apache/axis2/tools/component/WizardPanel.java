package org.apache.axis2.tools.component;

import javax.swing.*;

/**
 * this is wizardPanel it is extends from Jpanel
 */
public class WizardPanel extends JPanel {

    //variables

    private WizardComponents wizardComponents;
    private String panelTopTitle;
    private ImageIcon panelImage;
    private String  panelBottomTitle;
    private String error;
    private boolean flag;
    private boolean progressFlag;
   // private ProgressBarPanel progressBar;

    public WizardPanel(WizardComponents wizardComponents) {
        this(wizardComponents, null);
    }

    public WizardPanel(WizardComponents wizardComponents, String title) {
        this.wizardComponents = wizardComponents;
        this.panelTopTitle = title;
        java.net.URL resource = WizardPanel.class.getResource("/icons/asf-feather.png");
        setPanelImage(new ImageIcon(resource));
    }

    public void update() {
    }

    public void next() {
        goNext();
    }

    public void back() {
        goBack();
    }

    public WizardComponents getWizardComponents(){
        return wizardComponents;
    }

    public void setWizardComponents(WizardComponents awizardComponents){
        wizardComponents = awizardComponents;
    }
    // Title
    public String getPanelTopTitle() {
        return panelTopTitle;
    }

    public void setPanelTopTitle(String title) {
        panelTopTitle = title;
    }
    public String getPanelBottomTitle() {
        return panelBottomTitle;
    }

    public void setPanelBottomTitle(String title) {
        panelBottomTitle = title;
    }
    // Image
    public ImageIcon getPanelImage(){
        return panelImage ;
    }

    public void setPanelImage(ImageIcon image){
        panelImage = image;
    }
    //error
     public String  getError(){
        return error ;
    }

     public boolean  getErrorFlag(){
        return flag ;
    }
    public void setError(String  error,boolean flag){
        this.error=error;
        this.flag=flag;
    }
    // progress panel visible flag
    public void setProgressPanelVisible(boolean flag) {
        this.progressFlag = flag;
    }
    public boolean getProgressPanelVisible() {
        return progressFlag;
    }
   /* public void setProgressPanel(ProgressBarPanel panel) {
        this.progressBar = panel;
    }
    public ProgressBarPanel getProgressPanel() {
        return (ProgressBarPanel)progressBar;
    }*/

    // next
    protected boolean goNext() {
        if (wizardComponents.getWizardPanelList().size() > wizardComponents.getCurrentIndex()+1 ) {
            wizardComponents.setCurrentIndex(wizardComponents.getCurrentIndex()+1);
            wizardComponents.updateComponents();
            return true;
        } else {
            return false;
        }
    }
    //back
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
