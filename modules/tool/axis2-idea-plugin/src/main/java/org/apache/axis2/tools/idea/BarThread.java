package org.apache.axis2.tools.idea;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;


public class BarThread extends Thread {

    private  volatile boolean stop = false;
    private static int DELAY = 100;
    public volatile String val=null;
    protected JProgressBar progressBar;
    protected JLabel lblprogress ;
    protected JLabel lbltitle;

    public BarThread(ProgressBarPanel bar) {
        progressBar = bar.getProgressBar();
        lblprogress =bar.getLabelProgress();
        lbltitle= bar.getLabelTitle();
    }

    public  void requestStop() {
        stop = true;
    }

    public void run() {
        int minimum = progressBar.getMinimum();
        int maximum = progressBar.getMaximum();
        Runnable runner = new Runnable() {
            public void run() {
                if(stop && progressBar.getValue()<100){

                    progressBar.setIndeterminate(false);
                    int value = progressBar.getValue();
                    progressBar.setValue(value+10);
                    lblprogress.setText(String.valueOf(value+9)+" %");
                    lbltitle.setText("Genarate Code. Please wait.....");
                } else if(!stop){

                    progressBar.setIndeterminate(true);

                }
            }
        };
        for (int i=minimum; i<maximum; i++) {
            try {
                SwingUtilities.invokeAndWait(runner);
                // Our task for each step is to just sleep
                Thread.sleep(DELAY);
            } catch (InterruptedException ignoredException) {
            } catch (InvocationTargetException ignoredException) {
            }
        }
    }
}
