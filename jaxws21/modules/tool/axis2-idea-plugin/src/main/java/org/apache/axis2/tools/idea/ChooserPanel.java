package org.apache.axis2.tools.idea;

import org.apache.axis2.tools.component.WizardPanel;
import org.apache.axis2.tools.component.WizardComponents;
import org.apache.axis2.tools.wizardframe.CodegenFrame;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * this panel used for as chooser
 *      java2wsdl codegen option
 *      wsdl2java  codegen option
 *
 * extend from wizardPanel calss
 */

public class ChooserPanel  extends WizardPanel {
    /**
     * varialbales
     */
    private JButton btnHint;
    private JLabel lblHint;
    private boolean flag=false;
    private JRadioButton optionJ2WRadioButton;
    private JRadioButton optionW2JRadioButton;
    private ButtonGroup bg;
    private char selectedOption = 'A'; // 'N' is no option selected  'A', 'B' & 'F' stands for options
    final private String hint="You can generate java code from a WSDL or WSDL from a java source file.";
    /**
     * construct method for chooserPanel
     *  @param wizardComponents
     */

    public ChooserPanel(WizardComponents wizardComponents){

        super(wizardComponents, "ChooserPanel");
        setPanelTopTitle("Select the wizard");
        setPanelBottomTitle("Welcome to the Axis2 code generator wizard");
        init();
    }

    /**
     * Panel initial method
     */
    private void init(){

        lblHint =new JLabel("");
        btnHint =new JButton("Hint >>");
        btnHint.setBorder(new EmptyBorder(new Insets(0,0,0,0)));

        optionW2JRadioButton = new JRadioButton("Generate java sorce code from a WSDl file.",true);
        optionJ2WRadioButton = new JRadioButton("Generate a WSDl from a java source file",false);
        ButtonGroup bg = new ButtonGroup();
        bg.add(optionJ2WRadioButton);
        bg.add(optionW2JRadioButton);

        this.setLayout(new GridBagLayout() );


        this.add(new JLabel("Please specify what you want to do.")
                , new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0
                , GridBagConstraints.NORTHWEST , GridBagConstraints.NONE
                , new Insets(10, 20, 0,0), 0, 0));

        // option button for java2wsdl
        optionW2JRadioButton.setEnabled(false);
        this.add(optionW2JRadioButton
                , new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0
                , GridBagConstraints.NORTHWEST , GridBagConstraints.NONE
                , new Insets(10, 20, 0,0), 0, 0));
        optionW2JRadioButton.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    selectedOption = 'A';
                    update();
                }
            }
        });

        // option button for wsdl2java

        this.add(optionJ2WRadioButton
                , new GridBagConstraints(0, 2, 1, 1, 1.0, 0.0
                , GridBagConstraints.NORTHWEST , GridBagConstraints.NONE
                , new Insets(10, 20, 0,0), 0, 0));
        optionJ2WRadioButton.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    selectedOption = 'B';
                    update();
                }
            }
        });

        // hint button

        this.add(btnHint,
                new GridBagConstraints(0,3, 1, 1, 1.0,0.0
                        , GridBagConstraints.NORTHWEST , GridBagConstraints.NONE
                        , new Insets(10, 20, 0,0), 0, 0));
        btnHint.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                if(flag){
                    btnHint.setText("Hint >>");
                    lblHint.setText("");
                    flag=false;
                }else{
                    btnHint.setText("Hint <<");
                    lblHint.setText(hint);
                    flag=true;
                }
                update();
            }
        });

        // hint lable

        this.add(lblHint,
                new GridBagConstraints(0, 4, 1, 1, 1.0, 1.0
                        , GridBagConstraints.NORTHWEST , GridBagConstraints.NONE
                        , new Insets(10, 20, 0,0), 0, 0));


        setNextButtonEnabled((selectedOption == 'A') || (selectedOption == 'B') );

    }

    /**
     * method for update when panel has some change
     */

    public void update() {
        setNextButtonEnabled((selectedOption == 'A') || (selectedOption == 'B') );
        setBackButtonEnabled(false); // there is no way back
        setProgressPanelVisible(false);
        setPageComplete(true);
    }

    /**
     * method for next button
     */
    public void next() {
        if (selectedOption == 'A') {
          //  switchPanel(CodegenFrame.PANEL_FIRST_A) ;

        } else if (selectedOption == 'B') {
            switchPanel(CodegenFrame.PANEL_FIRST_B );
            setNextButtonEnabled(false);
        }
    }

    /**
     * methodd for back button
     */
    public void back() {
    }
}
