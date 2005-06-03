package sample.google.spellcheck;

import sample.google.spellcheck.FormModel;
import sample.google.spellcheck.Observer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 *  class sample.google.spellcheck.SyncPanel
 * This Impements its own GUI of the Synchronous Client and it send the SOAP request after the mouse event.
 * @author Nadana Gunarathna
 *
 */
public class SyncPanel extends javax.swing.JPanel implements Observer, ActionListener{
    FormModel formModel;
    javax.swing.JTextField tFieldget;
    javax.swing.JTextField tFieldset;
    javax.swing.JButton button;
    public SyncPanel()
    {
        formModel  = new FormModel(this);
        tFieldget = new javax.swing.JTextField(10);
        tFieldset = new javax.swing.JTextField(10);
        button=new javax.swing.JButton("Send");
        JScrollPane scrollPaneget = new JScrollPane(tFieldget);
        JScrollPane scrollPaneset = new JScrollPane(tFieldset);

        setPreferredSize(new Dimension(150, 150));

        tFieldget.setText("Enter a String");

        this.add(tFieldget,BorderLayout.CENTER );
        this.add(scrollPaneget,BorderLayout.CENTER);
        this.add(button);
        this.add(tFieldset ,BorderLayout.CENTER);
        this.add(scrollPaneset,BorderLayout.CENTER);
        //this.setLayout(new GridLayout(0,1));
        button.addActionListener(this);

    }
    public void update(String suggestion)
    {
        tFieldset.setText(suggestion);             // put the suggestion string in the reaponse box along with the misspelt word
    }


    public void actionPerformed(ActionEvent e) {
        String str=tFieldget.getText().trim();
        formModel.doSyncSpellingSuggestion(str);
        //formModel.getResponse();
    }



}


