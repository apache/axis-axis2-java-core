package org.apache.axis2.tools.component;

import javax.swing.*;
import java.util.List;
import java.beans.PropertyChangeListener;

/**
 * this interface extends from wizard interface
 */

public interface WizardComponents  extends Wizard {

    public void addWizardPanel(WizardPanel panel);

    public void addWizardPanel(int index, WizardPanel panel);

    public void addWizardPanelAfter(
            WizardPanel panelToBePlacedAfter,
            WizardPanel panel);

    public void addWizardPanelBefore(
            WizardPanel panelToBePlacedBefore,
            WizardPanel panel);

    public void addWizardPanelAfterCurrent(WizardPanel panel);

    public WizardPanel removeWizardPanel(WizardPanel panel);

    public WizardPanel removeWizardPanel(int index);

    public WizardPanel removeWizardPanelAfter(WizardPanel panel);

    public WizardPanel removeWizardPanelBefore(WizardPanel panel);

    public WizardPanel getWizardPanel(int index);

    public int getIndexOfPanel(WizardPanel panel);

    public void updateComponents();

    public WizardPanel getCurrentPanel() throws Exception;

    public FinishAction getFinishAction();

    public void setFinishAction(FinishAction aFinishAction);

    public CancelAction getCancelAction();

    public void setCancelAction(CancelAction aCancelAction);

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

    public List getWizardPanelList();

    public void setWizardPanelList(List panelList);

    public boolean onLastPanel();

    public final static String CURRENT_PANEL_PROPERTY = "currentPanel";

    public void addPropertyChangeListener(PropertyChangeListener listener);

    public void removePropertyChangeListener(PropertyChangeListener listener);

}
