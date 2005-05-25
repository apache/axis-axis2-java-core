package org.apache.axis.tool.service.swing.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.apache.axis.tool.bean.Page1Bean;
import org.apache.axis.tool.bean.WizardBean;
import org.apache.axis.tool.util.Constants;

/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class WizardPane1 extends WizardPane{

    private Page1Bean myBean = null;

    private JLabel classFileLocationLabel;
    private JTextField classFileLocationTextBox;
    private JButton browseButton;

    public WizardPane1(WizardBean wizardBean, JFrame ownerFrame) {

        super(ownerFrame);

        init();

        if (wizardBean.getPage1bean()!= null){
            myBean = wizardBean.getPage1bean();
            this.classFileLocationTextBox.setText(myBean.getFileLocation());
        }else{
            myBean = new Page1Bean();
            wizardBean.setPage1bean(myBean);
        }


    }

    public boolean validateValues() {
        String text = myBean.getFileLocation();
        return (text!=null && text.trim().length()>0);
    }

    private void init(){
        this.setLayout(null);
        this.setSize(width,height);

        initDescription("Welcome to the new AXIS Service packager Wizard Interface.\n\n " +
                        "Insert the location for the class files here.This should be a folder with \n" +
                        " the compiled classes");


        this.classFileLocationLabel = new JLabel("class file location");
        this.add(this.classFileLocationLabel);
        this.classFileLocationLabel.setBounds(hgap,descHeight,Constants.UIConstants.LABEL_WIDTH,Constants.UIConstants.GENERAL_COMP_HEIGHT);

        this.classFileLocationTextBox = new JTextField();
        this.add(this.classFileLocationTextBox);
        this.classFileLocationTextBox.setBounds(Constants.UIConstants.LABEL_WIDTH + 2*hgap ,descHeight,Constants.UIConstants.TEXT_BOX_WIDTH,Constants.UIConstants.GENERAL_COMP_HEIGHT);
        this.classFileLocationTextBox.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                handleTextBoxChange();
            }
        });
        this.classFileLocationTextBox.addKeyListener(new KeyListener(){
            public void keyTyped(KeyEvent e) { }
            public void keyPressed(KeyEvent e) { }
            public void keyReleased(KeyEvent e) { handleTextBoxChange();}
        });

        this.browseButton = new JButton(".");
        this.add(this.browseButton);
        this.browseButton.setBounds(Constants.UIConstants.LABEL_WIDTH + 2*hgap +Constants.UIConstants.TEXT_BOX_WIDTH,descHeight,Constants.UIConstants.BROWSE_BUTTON_WIDTH,Constants.UIConstants.GENERAL_COMP_HEIGHT);
        this.browseButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                classFileLocationTextBox.setText(browseForAFolder());
                handleTextBoxChange();
            }
        });
    }


    private void handleTextBoxChange() {
        String text = classFileLocationTextBox.getText();
        if (text!=null){
            this.myBean.setFileLocation(text);
        }
    }


}