package org.apache.axis2.tools.idea;

import javax.swing.*;
import java.awt.*;

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
*
*
*/

/**
 * Author : Deepal Jayasinghe
 * Date: Jul 20, 2005
 * Time: 9:33:14 PM
 */
public class ImagePanel extends JPanel {
    JLabel lblImage;
    JLabel labTop;
    JLabel lblBottom;
    ImageIcon apachelogo;

    public ImagePanel() {
        ImageLayout customLayout = new ImageLayout();
        setLayout(customLayout);

        java.net.URL resource = ImagePanel.class.getResource("/icons/asf-feather.png");
        apachelogo = new ImageIcon(resource);

        lblImage = new JLabel(apachelogo);
        add(lblImage);
        lblImage.setBackground(Color.white);

        labTop = new JLabel();
        labTop.setBackground(Color.white);
        add(labTop);
        labTop.setFont(new Font("Helvetica", Font.BOLD, 12));

        lblBottom = new JLabel();
        add(lblBottom);
        lblBottom.setBackground(Color.white);
        lblBottom.setFont(new Font("Helvetica", Font.PLAIN, 10));

        setSize(getPreferredSize());
        this.setBackground(Color.white);
    }

    public void setCaptions(String lbl1, String lbl2) {
        labTop.setText(lbl1);
        lblBottom.setText(lbl2);
    }
}

class ImageLayout implements LayoutManager {

    public ImageLayout() {
    }

    public void addLayoutComponent(String name, Component comp) {
    }

    public void removeLayoutComponent(Component comp) {
    }

    public Dimension preferredLayoutSize(Container parent) {
        Dimension dim = new Dimension(0, 0);

        Insets insets = parent.getInsets();
        dim.width = 535 + insets.left + insets.right;
        dim.height = 77 + insets.top + insets.bottom;

        return dim;
    }

    public Dimension minimumLayoutSize(Container parent) {
        return new Dimension(0, 0);
    }

    public void layoutContainer(Container parent) {
        Insets insets = parent.getInsets();

        Component c;
        c = parent.getComponent(0);
        if (c.isVisible()) {
            c.setBounds(insets.left + 368, insets.top, 168, 80);
        }
        c = parent.getComponent(1);
        if (c.isVisible()) {
            c.setBounds(insets.left, insets.top, 368, 40);
        }
        c = parent.getComponent(2);
        if (c.isVisible()) {
            c.setBounds(insets.left, insets.top + 40, 368, 40);
        }
    }
}
