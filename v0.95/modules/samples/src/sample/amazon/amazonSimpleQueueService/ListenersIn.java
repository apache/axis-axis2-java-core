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

import sample.amazon.amazonSimpleQueueService.util.RunnableCreateQueue;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * Listeners for relevent Components in the IN operation
 */
public class ListenersIn implements KeyListener {
    JTextField createQueue;
    JTextArea result;
    JTextField queueCode;
    JTextField enqueue;
    RunnableCreateQueue runnable;

    public ListenersIn(JTextField createQueue, JTextField queueCode, JTextField enqueue,
                       JTextArea result) {
        this.queueCode = queueCode;
        this.createQueue = createQueue;
        this.enqueue = enqueue;
        this.result = result;
    }

    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            runnable =
                    new RunnableCreateQueue(this.createQueue, this.queueCode, this.enqueue,
                            this.result);
            Thread tread = new Thread(runnable);
            tread.start();
        }
    }

    public void keyReleased(KeyEvent e) {

    }

    public void keyTyped(KeyEvent e) {

    }
}
