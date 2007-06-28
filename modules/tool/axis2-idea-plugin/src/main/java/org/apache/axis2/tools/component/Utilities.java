package org.apache.axis2.tools.component;

import java.awt.*;

/**
 * this class used for set frame in center of the current desktop
 */
public class Utilities {

    public static void centerComponentOnScreen(Component component) {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension d = toolkit.getScreenSize();

        Point p = new Point();
        p.x += ((d.width - component.getWidth()) / 2);
        p.y += ((d.height - component.getHeight()) / 2);

        if (p.x < 0) {
            p.x = 0;
        }

        if (p.y < 0) {
            p.y = 0;
        }

        component.setLocation(p);
    }
}
