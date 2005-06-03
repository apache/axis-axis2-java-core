package sample.google.spellcheck;

import sample.google.spellcheck.AsyncPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * class sample.google.spellcheck.SuggestionForm
 * This is the implementation of the GUI
 * @author Nadana Gunarathna
 *
 */
public class SuggestionForm extends javax.swing.JFrame implements ActionListener {
    AsyncPanel asyncPanel;
    SyncPanel syncPanel;

    public SuggestionForm() throws HeadlessException {
        asyncPanel = new AsyncPanel();
        syncPanel = new SyncPanel();
        JMenuBar menuBar;
        //Create the menu bar.
        menuBar = new JMenuBar();
        JMenu mainMenu = new JMenu("Select");

        mainMenu.setMnemonic(KeyEvent.VK_A);

        JMenuItem syncMenuItem = new JMenuItem("Sync Call", KeyEvent.VK_T);
        syncMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, ActionEvent.ALT_MASK));

        syncMenuItem.addActionListener(this);
        mainMenu.add(syncMenuItem);

        this.setJMenuBar(menuBar);
        menuBar.add(mainMenu);
        //this.getContentPane().add(menuBar);

        //this.getContentPane().add(syncPanel);
        this.getContentPane().add(asyncPanel);
    }

    public static void main(String[] args) {
        SuggestionForm form = new SuggestionForm();
        form.setSize(800, 800);
        form.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        form.pack();
        form.show();
    }

    public void actionPerformed(ActionEvent e) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
