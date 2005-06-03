/*
* Copyright 2004,2005 The Apache Software Foundation.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package sample.amazon.amazonSimpleQueueService;

import javax.swing.*;
import java.awt.*;

/**
 * GUI which handles the IN operations of the queue
 * @author Saminda Abeyruwan <saminda@opensource.lk>
 *
 */
public class AmazonSimpleQueueServiceIn extends JFrame{
    JTextField createQueue;
    JTextField queueCode;
    JTextField enqueue;
    JTextArea resuts;
    public AmazonSimpleQueueServiceIn(){
        this.setBounds(200,200,450,500);
        this.setTitle("Amazon Simple Queue WS - In");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.guiInit();
    }
    private void guiInit(){
        getContentPane().setLayout(new GridBagLayout());
        GridBagConstraints cons = new GridBagConstraints();
        cons.fill = GridBagConstraints.BOTH;
        JLabel lable1 = new JLabel("Create Queue");
        cons.insets = new Insets(5,5,5,5);
        this.add(lable1,cons,0,0,1,1);
        JLabel lable2 = new JLabel("Queue Code");
        this.add(lable2, cons, 1,0,1,1);
        cons.weightx = 100;
        createQueue = new JTextField("Test Queue LSF2");
        this.add(createQueue,cons,0,1,1,1);
        queueCode = new JTextField();
        queueCode.setEditable(false);
        this.add(queueCode,cons,1,1,1,1);
        JLabel lable3 = new JLabel("Enqueue");
        this.add(lable3,cons,0,2,1,1);
        enqueue = new JTextField();
        enqueue.setEditable(false);
        this.add(enqueue,cons,0,3,2,1);
        JLabel label4 = new JLabel("Results");
        this.add(label4,cons,0,5,1,1);
        cons.weighty = 100;
        resuts = new JTextArea();
        resuts.setEditable(false);
        resuts.setLineWrap(true);
        resuts.setWrapStyleWord(true);
        JScrollPane resultpane = new JScrollPane(resuts);
        this.add(resultpane,cons,0,6,2,2);
        createQueue.addKeyListener(new ListenersIn(createQueue,queueCode,enqueue,resuts));
        enqueue.addKeyListener(new ListenersIn(createQueue,queueCode,enqueue,resuts));
    }
    private void add(Component c, GridBagConstraints cons, int x, int y, int w, int h){
        cons.gridx = x;
        cons.gridy = y;
        cons.gridheight = h;
        cons.gridwidth = w;
        this.getContentPane().add(c,cons);
    }

}
