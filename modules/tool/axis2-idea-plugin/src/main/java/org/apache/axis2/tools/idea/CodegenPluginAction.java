package org.apache.axis2.tools.idea;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;

import javax.swing.*;


/**
 * Created by IntelliJ IDEA.
 * User: shivantha
 * Date: 9/07/2007
 * Time: 17:40:25
 * To change this template use File | Settings | File Templates.
 */
public class CodegenPluginAction extends AnAction {

    private ImageIcon myIcon;

    public CodegenPluginAction() {
        super("GC", "Axis2 Code Generator", null);
    }

    public void actionPerformed(AnActionEvent anActionEvent) {
        Application application =
                ApplicationManager.getApplication();
        Project project = (Project) anActionEvent.getDataContext().getData(DataConstants.PROJECT);

        CodegenPlugin axis2component =
                (CodegenPlugin) application.getComponent(CodegenPlugin.class);
        axis2component.showTool(project);
    }

    public void update(AnActionEvent event) {
        super.update(event);
        Presentation presentation = event.getPresentation();
        if (ActionPlaces.MAIN_TOOLBAR.equals(event.getPlace())) {
            if (myIcon == null) {
                java.net.URL resource = CodegenPluginAction.class.getResource("/icons/icon.png");
                myIcon = new ImageIcon(resource);
            }
            presentation.setIcon(myIcon);
        }
    }

}

