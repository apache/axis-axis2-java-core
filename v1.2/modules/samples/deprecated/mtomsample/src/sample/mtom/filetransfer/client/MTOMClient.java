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

package sample.mtom.filetransfer.client;

import javax.swing.*;
import java.awt.*;

public class MTOMClient extends JFrame {

    public MTOMClient(String title) throws HeadlessException {
        super(title);

        this.getContentPane().add(new UserInterface(this));
        this.setVisible(true);
    }

    public static void main(String[] args) {
        MTOMClient form = new MTOMClient("Axis2 Attachment Sample Client");
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int left = (screenSize.width - UserInterface.WIDTH) / 2;
        int top = (screenSize.height - UserInterface.HEIGHT) / 2;
        form.setLocation(left, top);
        form.setSize(UserInterface.WIDTH, UserInterface.HEIGHT);
        form.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        form.setVisible(true);
    }
}
