/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
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
