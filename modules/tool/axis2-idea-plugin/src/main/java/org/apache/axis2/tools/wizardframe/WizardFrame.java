package org.apache.axis2.tools.wizardframe;

import org.apache.axis2.tools.component.CancelAction;
import org.apache.axis2.tools.component.WizardComponents;
import org.apache.axis2.tools.component.FinishAction;
import org.apache.axis2.tools.component.DefaultWizardComponents;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * wizardFrame class
 */
public class WizardFrame extends JFrame {
    private WizardComponents wizardComponents;

    public WizardFrame() {
        init();
    }

    private void init() {

        wizardComponents = new DefaultWizardComponents();

        this.getContentPane().setLayout(new GridBagLayout());
       /* this.getContentPane().add(new ImagePanel(wizardComponents)
                , new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0
                , GridBagConstraints.NORTH, GridBagConstraints.BOTH
                , new Insets(0, 0, 0, 0), 0, 0));

        this.getContentPane().add(new JSeparator()
                , new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0
                , GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                new Insets(1, 1, 1, 1), 0, 0));

        this.getContentPane().add(wizardComponents.getWizardPanelsContainer()
                , new GridBagConstraints(0, 2, 1, 1, 1.0, 1.0
                , GridBagConstraints.PAGE_START , GridBagConstraints.BOTH
                , new Insets(10, 0, 0, 0), 0, 0));

        this.getContentPane().add(new ProgressBarPanel(wizardComponents)
                , new GridBagConstraints(0, 3, 1, 1, 1.0, 0.0
                , GridBagConstraints.CENTER, GridBagConstraints.NONE
                , new Insets(10, 0, 0, 0), 0, 0));

        this.getContentPane().add(new JSeparator()
                , new GridBagConstraints(0, 4, 1, 1, 1.0, 0.0
                , GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL
                , new Insets(1, 1, 1, 1), 0, 0));

        this.getContentPane().add(createButtonPanel(),
                new GridBagConstraints(0, 5, 1, 1, 1.0, 0.0
                        ,GridBagConstraints.EAST, GridBagConstraints.NONE,
                        new Insets(20, 20, 20, 20), 0, 0));*/

        ImageIcon  img=new ImageIcon("icons/icon.png");
        this.setIconImage(img.getImage());

        wizardComponents.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent event) {

            }
        });
        wizardComponents.setFinishAction(createFinishAction());
        wizardComponents.setCancelAction(createCancelAction());
        handleWindowClosing();
    }

    public WizardComponents getWizardComponents(){
        return wizardComponents;
    }

    public void setWizardComponents(WizardComponents aWizardComponents){
        wizardComponents = aWizardComponents;
    }

    public void show() {
        wizardComponents.updateComponents();
        super.show();
    }

    protected JPanel createButtonPanel() {
        JPanel panel = new JPanel(new GridLayout());
        panel.add(wizardComponents.getBackButton());
        panel.add(wizardComponents.getNextButton());
        panel.add(wizardComponents.getFinishButton());
        panel.add(wizardComponents.getCancelButton());
        return panel;
    }

    protected FinishAction createFinishAction() {
        return new FinishAction(wizardComponents) {
            public void performAction() {
                System.out.println("FinishAction performed");
                dispose();
            }
        };
    }

    protected CancelAction createCancelAction() {
        return new CancelAction(wizardComponents) {
            public void performAction() {
                System.out.println("CancelAction performed");
                dispose();
            }
        };
    }

    protected void handleWindowClosing() {
        this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                wizardComponents.getCancelAction().performAction();
            }
        });
    }
}
