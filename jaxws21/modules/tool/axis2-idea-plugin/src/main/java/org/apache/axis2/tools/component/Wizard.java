package org.apache.axis2.tools.component;

import java.util.List;

/**
 * interface for wizard
 */
public interface Wizard {

    public List getWizardPanelList();

    public void setWizardPanelList(List panelList);

    public void addWizardPanel(WizardPanel panel);

    public void addWizardPanel(int index, WizardPanel panel);

    public WizardPanel removeWizardPanel(WizardPanel panel);

    public WizardPanel removeWizardPanel(int index);

    public WizardPanel getWizardPanel(int index);
}
