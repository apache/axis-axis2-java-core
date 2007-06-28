package org.apache.axis2.tools.component;

/**
 * abstract class for FinishAction
 */
public abstract class FinishAction implements Action{
    WizardComponents wizardComponents;

    public FinishAction(WizardComponents wizardComponents) {
        this.wizardComponents = wizardComponents;
    }
}
