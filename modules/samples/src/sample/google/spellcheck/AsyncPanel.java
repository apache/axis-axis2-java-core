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
    javax.swing.JTextArea textAreaget;
    javax.swing.JTextArea textAreaset;
    public AsyncPanel()
    {
        formModel  = new FormModel(this);
        textAreaget  = new javax.swing.JTextArea(15,10);
        textAreaset = new javax.swing.JTextArea(15,10);
        JScrollPane scrollPaneGet = new JScrollPane(textAreaget);
        JScrollPane scrollPaneSet = new JScrollPane(textAreaset);
        setPreferredSize(new Dimension(450, 460));

        textAreaget.setText("Enter a String");
        textAreaget.addKeyListener(this);


        this.add(scrollPaneGet,BorderLayout.NORTH);
        this.add(scrollPaneSet,BorderLayout.SOUTH);


    }
    public void update(String suggestion)
    {
        textAreaset.setText(suggestion);

        // put the suggestion string in the reaponse box along with the misspelt word
    }


    public void keyPressed(KeyEvent e) {
        //To change body of implemented methods use File | Settings | File Templates.
        int key=e.getKeyChar();
        if((key==32)||(key==10)){
            String suggesion=textAreaget.getText().trim();
            //suggesion.trim();
            formModel.doAsyncSpellingSuggestion(suggesion);
        }
    }

    public void keyReleased(KeyEvent e) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void keyTyped(KeyEvent e) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
