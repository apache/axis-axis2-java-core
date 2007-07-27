package org.apache.ideaplugin.frames;

import org.apache.ideaplugin.bean.ParameterObj;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class ParameterDialog extends JDialog {
    private static ParameterDialog dialog;
    private static ParameterObj para =new ParameterObj("","");
    private JTextField txtName;
    private JTextField txtValue;

    public static void initialize(Component comp,
                                  String title) {
        Frame frame = JOptionPane.getFrameForComponent(comp);
        dialog = new ParameterDialog(frame,title);
        dialog.setResizable(false);
        dialog.setSize(250,150);
    }

    public static ParameterObj showDialog(String title) {
        if (dialog != null) {
            dialog.setTitle(title);
            dialog.setVisible(true);
        }
        return para;
    }


    private ParameterDialog(Frame frame,  String title) {
        super(frame, title, true);

        //buttons
        final JButton cancelButton = new JButton("Cancel");
        final JButton setButton = new JButton(" OK ");
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ParameterDialog .dialog.setVisible(false);
            }
        });
        setButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ParameterDialog.para.setName(txtName.getText());
                ParameterDialog.para.setValue(txtValue.getText());
                ParameterDialog.dialog.setVisible(false);
            }
        });
        getRootPane().setDefaultButton(setButton);
        getRootPane().setDefaultButton(cancelButton);
        //main part of the dialog

        txtName =new JTextField();
        txtValue =new JTextField();

        JPanel listPane = new JPanel();
        listPane.setLayout(new BoxLayout(listPane, BoxLayout.PAGE_AXIS));
        listPane.add(Box.createRigidArea(new Dimension(10,0)));
        listPane.add(new JLabel("Name:"));
        listPane.add(txtName);
        listPane.add(new JLabel("Value:"));
        listPane.add(txtValue );
        listPane.setBorder(BorderFactory.createEmptyBorder(0,10,10,10));

        //Lay out the buttons from left to right.
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.X_AXIS));
        buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        buttonPane.add(Box.createHorizontalGlue());
        buttonPane.add(cancelButton);
        buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPane.add(setButton);

        //Put everything together, using the content pane's BorderLayout.
        Container contentPane = getContentPane();
        contentPane.add(listPane, BorderLayout.CENTER);
        contentPane.add(buttonPane, BorderLayout.SOUTH);

        pack();
    }

}
