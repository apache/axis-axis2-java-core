package sample.google.spellcheck;

import org.apache.axis.clientapi.Call;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.addressing.EndpointReference;
import org.apache.axis.addressing.AddressingConstants;
import org.apache.axis.soap.SOAPEnvelope;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLStreamException;
import javax.swing.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.awt.*;

/**
 *  class sample.google.spellcheck.AsyncPanel
 * This Impements its own GUI of the Asynchronous Client and it updates the string after getting the response to textarea
 * @author Nadana Gunarathna
 *
 */
public class AsyncPanel extends javax.swing.JPanel implements Observer,KeyListener{
    FormModel formModel;
    JTextArea writingTextArea;
    JTextArea displayTextArea;
    JTextField errorMessageField;
    public AsyncPanel()
    {
        GridBagLayout gbLayout = new GridBagLayout();
        GridBagConstraints constraint = new GridBagConstraints();
        this.setLayout(gbLayout);

        formModel  = new FormModel(this);

        writingTextArea  = new JTextArea();
        writingTextArea.setLineWrap(true);

        displayTextArea = new JTextArea();
        displayTextArea.setEditable(false);
        displayTextArea.setLineWrap(true);

        errorMessageField = new JTextField();
        errorMessageField.setEditable(false);
        errorMessageField.setBackground(Color.LIGHT_GRAY);
        errorMessageField.setForeground(Color.RED);

        JScrollPane scrollPaneGet = new JScrollPane(writingTextArea);
        JScrollPane scrollPaneSet = new JScrollPane(displayTextArea);

        writingTextArea.setText("Enter a String");
        writingTextArea.addKeyListener(this);

        constraint.fill = GridBagConstraints.BOTH;
        constraint.gridx=0;
        constraint.weightx=1;
        constraint.weighty=8;
        gbLayout.setConstraints(scrollPaneGet,constraint);
        this.add(scrollPaneGet);
        gbLayout.setConstraints(scrollPaneSet,constraint);
        this.add(scrollPaneSet);
        constraint.weighty=1;
        gbLayout.setConstraints(errorMessageField,constraint);
        this.add(errorMessageField);


    }

    public void update(String message)
    {
        displayTextArea.setText(displayTextArea.getText() + " " + message);
    }

    //updates the error message to the error message display area
    public void updateError(String message) {
        errorMessageField.setText(message);
    }


    public void keyPressed(KeyEvent e) {
        int key=e.getKeyChar();
        if((key==KeyEvent.VK_SPACE)||(key==KeyEvent.VK_ENTER)){
            String[] words=writingTextArea.getText().split("\\s");
            if (words.length > 0)
            formModel.doAsyncSpellingSuggestion(words[words.length-1]);
        }
    }

    public void keyReleased(KeyEvent e) {
    }

    public void keyTyped(KeyEvent e) {
    }

    public void clear() {
        displayTextArea.setText("");
        writingTextArea.setText("");
        errorMessageField.setText("");
    }
}
