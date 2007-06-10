package org.apache.axis2.tools.idea;

import javax.swing.*;
import java.awt.*;

public class ProgressBarPanel extends JPanel {
    protected JLabel lbltitle;
    protected JProgressBar progressBar;
    protected JLabel lblprogress;

    public ProgressBarPanel(){

        ProgressLayout customLayout = new ProgressLayout();

        setLayout(customLayout);

        lbltitle =new JLabel("Scanning files .......");
        add(lbltitle );

        progressBar =new JProgressBar(0,100);
        progressBar.setBorderPainted(true);
        progressBar.setStringPainted(true);
        Dimension dim=new Dimension();
        dim.setSize(440,24);
        progressBar.setPreferredSize(dim);
        add(progressBar );

        lblprogress=new JLabel();
        add(lblprogress);



        setSize(getPreferredSize());

    }
    public JProgressBar getProgressBar(){
        return progressBar;
    }
     public JLabel getLabelProgress(){
        return lblprogress;
    }
     public JLabel getLabelTitle(){
        return lblprogress;
    }

}
class ProgressLayout implements LayoutManager {
    public ProgressLayout() {
    }

    public void addLayoutComponent(String name, Component comp) {
    }

    public void removeLayoutComponent(Component comp) {
    }

    public Dimension preferredLayoutSize(Container parent) {
        Dimension dim = new Dimension(0, 0);

        Insets insets = parent.getInsets();
        dim.width = 500 + insets.left + insets.right;
        dim.height = 100 + insets.top + insets.bottom;

        return dim;
    }

    public Dimension minimumLayoutSize(Container parent) {
        return new Dimension(0, 0);
    }

    public void layoutContainer(Container parent) {
        Insets insets = parent.getInsets();

        Component c;
        c = parent.getComponent(1);
        if (c.isVisible()) {
            c.setBounds(insets.left +8, insets.top + 30, 440, 24);
        }
        c = parent.getComponent(2);
        if (c.isVisible()) {
            c.setBounds(insets.left + 460, insets.top + 30, 30, 24);
        }
        c = parent.getComponent(0);
        if (c.isVisible()) {
            c.setBounds(insets.left +8, insets.top , 200, 24);
        }
    }
}
