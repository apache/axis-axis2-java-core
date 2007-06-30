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
package org.apache.ideaplugin.frames;

import javax.swing.*;
import java.awt.*;

/**
 * Author: Deepal Jayasinghe
 * Date: Sep 24, 2005
 * Time: 11:04:25 AM
 */
public class LogoPage extends JPanel {
    JLabel imagelbl;
    ImageIcon apachelogo;

    public LogoPage() {
        LogoPageLayout customLayout = new LogoPageLayout();
        setFont(new Font("Helvetica", Font.PLAIN, 12));
        setLayout(customLayout);

        java.net.URL resource = LogoPage.class.getResource("/icons/asf-feather.png");
        apachelogo = new ImageIcon(resource);
        imagelbl = new JLabel(apachelogo);
        add(imagelbl);
        setSize(getPreferredSize());
        setBackground(Color.white);
    }
}

class LogoPageLayout implements LayoutManager {

    public LogoPageLayout() {
    }

    public void addLayoutComponent(String name, Component comp) {
    }

    public void removeLayoutComponent(Component comp) {
    }

    public Dimension preferredLayoutSize(Container parent) {
        Dimension dim = new Dimension(0, 0);

        Insets insets = parent.getInsets();
        dim.width = 320 + insets.left + insets.right;
        dim.height = 76 + insets.top + insets.bottom;

        return dim;
    }

    public Dimension minimumLayoutSize(Container parent) {
        Dimension dim = new Dimension(0, 0);
        return dim;
    }

    public void layoutContainer(Container parent) {
        Insets insets = parent.getInsets();

        Component c;
        c = parent.getComponent(0);
        if (c.isVisible()) {
            c.setBounds(insets.left + 8, insets.top + 8, 304, 64);
        }
    }
}
