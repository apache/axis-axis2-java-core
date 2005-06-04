package sample.google.spellcheck;

import sample.google.spellcheck.AsyncPanel;
import sample.google.common.util.PropertyLoader;

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
public class SuggestionForm extends javax.swing.JFrame {
    private AsyncPanel asyncPanel;
    private SyncPanel syncPanel;

    private JMenuItem syncMenuItem;
    private JMenuItem asyncMenuItem;


    public SuggestionForm() throws HeadlessException {
        asyncPanel = new AsyncPanel();
        syncPanel = new SyncPanel();

        JMenuBar menuBar;
        //Create the menu bar.
        menuBar = new JMenuBar();

        JMenu modeMenu = new JMenu("Mode");
        modeMenu.setMnemonic(KeyEvent.VK_M);
        syncMenuItem = new JMenuItem("Sync Mode", KeyEvent.VK_S);
        syncMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
        syncMenuItem.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                setSyncPanel();
            }
        });
        asyncMenuItem = new JMenuItem("ASync Mode", KeyEvent.VK_A);
        asyncMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));
        asyncMenuItem.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                setAsyncPanel();
            }
        });
        modeMenu.add(syncMenuItem);
        modeMenu.add(asyncMenuItem);

        JMenu settingsMenu =  new JMenu("Settings");
        settingsMenu.setMnemonic(KeyEvent.VK_S);
        JMenuItem googleKeyMenu = new JMenuItem("Google Key",KeyEvent.VK_G);
        googleKeyMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, ActionEvent.CTRL_MASK));
        googleKeyMenu.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                setKey();
            }
        });
        settingsMenu.add(googleKeyMenu);

        menuBar.add(modeMenu);
        menuBar.add(settingsMenu);

        this.setJMenuBar(menuBar);

        this.getContentPane().setLayout(new GridLayout(1,1));
        setAsyncPanel();



    }

    public static void main(String[] args) {
        SuggestionForm form = new SuggestionForm();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        form.setLocation(screenSize.width/4,
                screenSize.height/4);
        form.setSize(screenSize.width/2,screenSize.height/2);
        form.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        //form.setResizable(false);
        //form.pack();
        form.show();
    }

    private void setAsyncPanel(){
        this.getContentPane().removeAll();
        this.getContentPane().add(asyncPanel);
        this.syncMenuItem.setEnabled(true);
        this.asyncMenuItem.setEnabled(false);
        this.getContentPane().repaint();
        this.setTitle("Google Spell checker - Async Mode");
        this.show();

    }

    private void setSyncPanel(){
        this.getContentPane().removeAll();
        this.getContentPane().add(syncPanel);
        this.syncMenuItem.setEnabled(false);
        this.asyncMenuItem.setEnabled(true);
        this.getContentPane().repaint();
        this.setTitle("Google Spell checker - Sync Mode");
        this.show();
    }

    private void setKey(){
        String key = JOptionPane.showInputDialog(this,"Set the Google Key",PropertyLoader.getGoogleKey());
        if (key!=null && !key.trim().equals("")){
            PropertyLoader.setGoogleKey(key);
        }
    }

}
