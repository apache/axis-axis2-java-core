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

import sample.amazon.amazonSimpleQueueService.util.RunnableDeleteQueue;
import sample.amazon.amazonSimpleQueueService.util.RunnableListMyQueues;
import sample.amazon.amazonSimpleQueueService.util.RunnableReadQueue;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

/**
 * This will create the OMElement needed to be used in invokeNonBlocking() method
 */
public class ListenersOut implements KeyListener,
        ActionListener,
        MouseMotionListener {
    JTextField createQueue;
    JTextArea result;
    JTextField queueCode;
    JTextField read;
    JButton buttonLoad;
    JButton buttonDelete;
    Runnable runableCodeListMyQueues;
    Runnable runnableCodeDequeue;

    public ListenersOut(JTextField createQueue,
                        JTextField queueCode,
                        JTextField read,
                        JTextArea result,
                        JButton buttonLoad,
                        JButton buttonDelete) {
        this.queueCode = queueCode;
        this.createQueue = createQueue;
        this.read = read;
        this.result = result;
        this.buttonLoad = buttonLoad;
        this.buttonDelete = buttonDelete;
    }

    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            this.result.setText("");
            this.createQueue.setEditable(false);
            this.runableCodeListMyQueues =
                    new RunnableReadQueue(this.createQueue,
                            this.queueCode, this.read, this.result);
            Thread thread = new Thread(this.runableCodeListMyQueues);
            thread.start();
        }
    }

    public void keyReleased(KeyEvent e) {
    }

    public void keyTyped(KeyEvent e) {
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("1")) {
            this.runableCodeListMyQueues =
                    new RunnableListMyQueues(this.createQueue, this.queueCode, this.read,
                            this.result, this.buttonLoad);
            Thread thread1 = new Thread(this.runableCodeListMyQueues);
            thread1.start();
            this.createQueue.setEditable(true);
            this.buttonLoad.setText("Running..");
        }
        if (e.getActionCommand().equals("2")) {
            this.buttonDelete.setText("Running");
            this.runnableCodeDequeue =
                    new RunnableDeleteQueue(this.createQueue, this.queueCode, this.read,
                            this.result, this.buttonDelete);
            Thread thread2 = new Thread(this.runnableCodeDequeue);
            thread2.start();
        }
    }

    public void mouseDragged(MouseEvent e) {
        String selectedText = this.result.getSelectedText();
        this.createQueue.setText(selectedText);
    }

    public void mouseMoved(MouseEvent e) {
    }
}
