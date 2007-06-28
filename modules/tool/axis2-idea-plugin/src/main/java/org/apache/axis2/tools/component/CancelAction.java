package org.apache.axis2.tools.component;

/**
 * abstract class for CanselAction
 */
public abstract class CancelAction  implements Action{
  WizardComponents wizardComponents;

  public CancelAction(WizardComponents wizardComponents) {
    this.wizardComponents = wizardComponents;
  }
}