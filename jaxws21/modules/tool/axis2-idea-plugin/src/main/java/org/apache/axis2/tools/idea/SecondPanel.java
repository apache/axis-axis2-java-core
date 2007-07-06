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

import java.awt.*;

class SecondPanelLayout implements LayoutManager {

    public SecondPanelLayout() {
    }

    public void addLayoutComponent(String name, Component comp) {
    }

    public void removeLayoutComponent(Component comp) {
    }

    public Dimension preferredLayoutSize(Container parent) {
        Dimension dim = new Dimension(0, 0);

        Insets insets = parent.getInsets();
        dim.width = 565 + insets.left + insets.right;
        dim.height = 300 + insets.top + insets.bottom;

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
            c.setBounds(insets.left, insets.top + 5, 200, 20);
        }
        c = parent.getComponent(1);
        if (c.isVisible()) {
            c.setBounds(insets.left + 210, insets.top + 5, 160, 20);
        }
        c = parent.getComponent(2);
        if (c.isVisible()) {
            c.setBounds(insets.left + 8, insets.top + 30, 250, 24);
        }
        c = parent.getComponent(3);
        if (c.isVisible()) {
            c.setBounds(insets.left + 280, insets.top + 30, 150, 24);
        }
        c = parent.getComponent(4);
        if (c.isVisible()) {
            c.setBounds(insets.left + 8, insets.top + 55, 150, 24);
        }
        c = parent.getComponent(5);
        if (c.isVisible()) {
            c.setBounds(insets.left + 8, insets.top + 80, 200, 24);
        }
        c = parent.getComponent(6);
        if (c.isVisible()) {
            c.setBounds(insets.left + 210, insets.top + 80, 160, 24);
        }
        c = parent.getComponent(7);
        if (c.isVisible()) {
            c.setBounds(insets.left + 8, insets.top + 110, 200, 24);
        }
        c = parent.getComponent(8);
        if (c.isVisible()) {
            c.setBounds(insets.left + 210, insets.top + 110, 200, 24);
        }
    }
}

