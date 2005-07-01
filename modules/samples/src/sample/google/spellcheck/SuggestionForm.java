package sample.google.spellcheck;

import sample.google.common.util.PropertyLoader;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;

/**
 * class sample.google.spellcheck.SuggestionForm
 * This is the implementation of the GUI
 *
 * @author Nadana Gunarathna
 */
public class SuggestionForm extends javax.swing.JFrame implements HyperlinkListener {
    private AsyncPanel asyncPanel;
    private SyncPanel syncPanel;
    private JEditorPane helpDisplayPane;

    private JMenuItem syncMenuItem;
    private JMenuItem asyncMenuItem;
    private static final String HELP_FILE_NAME = "/docs/GoogleSpellCheck.html";


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
        syncMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setSyncPanel();
            }
        });
        asyncMenuItem = new JMenuItem("ASync Mode", KeyEvent.VK_A);
        asyncMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));
        asyncMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setAsyncPanel();
            }
        });
        modeMenu.add(syncMenuItem);
        modeMenu.add(asyncMenuItem);

        JMenu settingsMenu = new JMenu("Settings");
        settingsMenu.setMnemonic(KeyEvent.VK_S);
        JMenuItem googleKeyMenu = new JMenuItem("Set Google Key", KeyEvent.VK_G);
        googleKeyMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, ActionEvent.CTRL_MASK));
        googleKeyMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setKey();
            }
        });
        settingsMenu.add(googleKeyMenu);

        JMenu clearMenu = new JMenu("Clear");
        clearMenu.setMnemonic(KeyEvent.VK_C);
        JMenuItem clearMenuItem = new JMenuItem("Clear text boxes");
        clearMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));
        clearMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                asyncPanel.clear();
                syncPanel.clear();
            }
        });
        clearMenu.add(clearMenuItem);

        JMenu helpMenu = new JMenu("Help");
        JMenuItem mnuItemHelp = new JMenuItem("Show Help");
        helpMenu.add(mnuItemHelp);

        mnuItemHelp.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showHelp();
            }
        });

        menuBar.add(modeMenu);
        menuBar.add(settingsMenu);
        menuBar.add(clearMenu);
        menuBar.add(helpMenu);

        this.setJMenuBar(menuBar);

        this.getContentPane().setLayout(new GridLayout(1, 1));
        setAsyncPanel();


    }

    public static void main(String[] args) {
        SuggestionForm form = new SuggestionForm();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        form.setLocation(screenSize.width / 4,
                screenSize.height / 4);
        form.setSize(screenSize.width / 2, screenSize.height / 2);
        form.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        //form.setResizable(false);
        //form.pack();
        form.show();
    }

    private void setAsyncPanel() {
        this.getContentPane().removeAll();
        this.getContentPane().add(asyncPanel);
        this.syncMenuItem.setEnabled(true);
        this.asyncMenuItem.setEnabled(false);
        this.getContentPane().repaint();
        this.setTitle("Google Spell checker - Async Mode");
        this.show();

    }

    private void setSyncPanel() {
        this.getContentPane().removeAll();
        this.getContentPane().add(syncPanel);
        this.syncMenuItem.setEnabled(false);
        this.asyncMenuItem.setEnabled(true);
        this.getContentPane().repaint();
        this.setTitle("Google Spell checker - Sync Mode");
        this.show();
    }

    private void setKey() {
        String key = JOptionPane.showInputDialog(this, "Set the Google Key",
                PropertyLoader.getGoogleKey());
        if (key != null && !key.trim().equals("")) {
            PropertyLoader.setGoogleKey(key);
        }
    }

    /**
     * method showHelp
     */
    private void showHelp() {

        JFrame frame = new JFrame();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation(screenSize.width / 5,
                screenSize.height / 5);
        frame.setSize(screenSize.width / 2, screenSize.height / 2);

        BorderLayout layout = new BorderLayout();

        JScrollPane jsp;


        helpDisplayPane = new JEditorPane();
        helpDisplayPane.addHyperlinkListener(this);
        helpDisplayPane.setEditable(false);
        helpDisplayPane.setContentType("text/html");

        jsp = new JScrollPane(helpDisplayPane);

        Container contentPane = frame.getContentPane();
        contentPane.setLayout(layout);
        contentPane.add(jsp, BorderLayout.CENTER);
        String helpDoc = System.getProperty("user.dir") + HELP_FILE_NAME;

        try {
            helpDisplayPane.setPage(new File(helpDoc).toURL());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Help file not detected", "Help file error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        frame.setVisible(true);
    }


    public void hyperlinkUpdate(HyperlinkEvent e) {
        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            try {
                String url = e.getURL().toString();
                helpDisplayPane.setPage(url);
//                
            } catch (Exception err) {
                JOptionPane.showMessageDialog(this, "Help file not detected", err.getMessage(),
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

        }
    }
}
