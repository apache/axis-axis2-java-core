package org.apache.axis2.tools.idea;

import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;

import javax.swing.ImageIcon;

public class ServiceArchiveCreator extends AnAction {
    private ImageIcon myIcon;

    public ServiceArchiveCreator() {
        super("GC", "Create Service Archive File", null);
    }

    public void actionPerformed(AnActionEvent event) {
        Java2CodeFrame win = new Java2CodeFrame();
        win.showUI();
    }

    public void update(AnActionEvent event) {
        super.update(event);
        Presentation presentation = event.getPresentation();
        if (ActionPlaces.MAIN_TOOLBAR.equals(event.getPlace())) {
            if (myIcon == null) {
                java.net.URL resource = ServiceArchiveCreator.class.getResource("/icons/garbage.png");
                myIcon = new ImageIcon(resource);
            }
            presentation.setIcon(myIcon);
        }
    }
}
