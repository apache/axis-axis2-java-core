/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.axis2.tools.wizardframe;

import org.apache.axis2.tools.component.*;

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
     /**
     * variable
     */

    private JLabel panelImageLabel;
    private JLabel panelTopTitleLabel;
    private JLabel panelBottomTitleLabel;
    private JLabel errorLabel;
    private JPanel errorPanel;
    private WizardComponents wizardComponents;

    public WizardFrame() {
        init();
    }

    private void init() {

        wizardComponents = new DefaultWizardComponents();

        this.getContentPane().setLayout(new GridBagLayout());
        this.getContentPane().add(createTitlePanel()
                , new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0
                , GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL
                , new Insets(0, 0, 0, 0), 0, 0));

        this.getContentPane().add(new JSeparator()
                , new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0
                , GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                new Insets(0, 0, 0, 0), 0, 0));

        this.getContentPane().add(wizardComponents.getWizardPanelsContainer()
                , new GridBagConstraints(0, 2, 1, 1, 1.0, 1.0
                , GridBagConstraints.NORTHWEST , GridBagConstraints.NONE
                , new Insets(10, 0, 0, 0), 0, 0));

       /* this.getContentPane().add(new ProgressBarPanel(wizardComponents)
                , new GridBagConstraints(0, 3, 1, 1, 1.0, 0.0
                , GridBagConstraints.CENTER, GridBagConstraints.NONE
                , new Insets(10, 0, 0, 0), 0, 0));  */

        this.getContentPane().add(new JSeparator()
                , new GridBagConstraints(0, 4, 1, 1, 1.0, 0.0
                , GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL
                , new Insets(1, 1, 1, 1), 0, 0));

        this.getContentPane().add(createButtonPanel(),
                new GridBagConstraints(0, 5, 1, 1, 1.0, 0.0
                        ,GridBagConstraints.EAST, GridBagConstraints.NONE,
                        new Insets(20, 20, 20, 20), 0, 0));

        ImageIcon  img=new ImageIcon("icons/icon.png");
        this.setIconImage(img.getImage());

        wizardComponents.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent event) {
                setPanelTopTitle(((WizardPanel)event.getNewValue()).getPanelTopTitle());
                setPanelTitleImage(((WizardPanel)event.getNewValue()).getPanelImage());
                setPanelBottomTitle(((WizardPanel)event.getNewValue()).getPanelBottomTitle());
                setErrorVisible(false
                        ,((WizardPanel)event.getNewValue()).getError());
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
      // set error message
    protected void setErrorVisible(boolean flag ,String  error){
        errorLabel.setText(error);
        errorPanel.setVisible(flag);

    }
    protected void setBottomVisible(boolean flag){
        panelBottomTitleLabel.setVisible(flag);
    }
    //Set Title Panel
    protected void setPanelTopTitle(String title) {
        panelTopTitleLabel.setText(title);
    }

    protected void setPanelBottomTitle(String title) {
        panelBottomTitleLabel.setText(title);
    }
    // set title image
    protected void setPanelTitleImage(ImageIcon image){
        panelImageLabel.setIcon(image );
    }

     protected JPanel createTitlePanel() {

        JPanel panel = new JPanel(new GridBagLayout() );

        panel.setBackground(Color.white );

        panelTopTitleLabel = new JLabel();

        panelBottomTitleLabel=new JLabel();

        panelImageLabel= new JLabel();

        errorLabel = new JLabel();
        errorLabel.setVisible(false);
        errorLabel.setBackground(Color.blue);

        errorPanel =new JPanel();
        errorPanel .setLayout(new GridBagLayout());
        errorPanel .setBorder(BorderFactory.createLineBorder(Color.black));

        errorPanel.add(errorLabel,
                new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH
                , new Insets(10, 10, 0, 10), 0, 0));

        panel.add(panelTopTitleLabel
                , new GridBagConstraints(0, 0, 1, 1, 0.5, 0.0
                , GridBagConstraints.LINE_START , GridBagConstraints.BOTH
                , new Insets(10, 20, 0, 0), 0, 0));

        panel.add(panelBottomTitleLabel
                , new GridBagConstraints(0, 1, 1, 1, 0.5, 0.0
                , GridBagConstraints.LINE_START, GridBagConstraints.BOTH
                , new Insets(10,20, 0, 0), 0, 0));

        panel.add(panelImageLabel
                , new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH
                , new Insets(0, 0, 0, 0), 0, 0));


        panel.add(errorPanel
                , new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
                , GridBagConstraints.SOUTH, GridBagConstraints.BOTH
                , new Insets(20, 0, 0, 0), 0, 0));

        return panel;

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
