package org.apache.axis2.tools.idea;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;

import javax.swing.*;
import java.net.URL;
import java.net.MalformedURLException;
import java.io.File;

public class ServiceArchiveCreator extends AnAction {
    private ImageIcon myIcon;

    public ServiceArchiveCreator() {
        super("GC", "Create Service Archive File", null);
    }

    public void actionPerformed(AnActionEvent event) {
//        Project project = (Project)event.getDataContext().getData(DataConstants.PROJECT);
//        java.net.URL resource = ServiceArchiveCreator.class.getResource("/icons/garbage.png");
//        String fileName = resource.toString();
//        fileName =  fileName.replaceAll("%20"," ");
//        fileName = fileName.replaceAll("jar:file:/","");
//        int end_index = fileName.indexOf("!");
//        fileName = fileName.substring(0,end_index);
//        ClassLoader contextClasLoader =
//        URL[] urlsToLoadFrom = new URL[0];
//        try {
//            File file = new File(fileName);
//            urlsToLoadFrom = new URL[]{file.toURL()};
//            PluginClassLoader clasloader = new PluginClassLoader(
//                    urlsToLoadFrom,contextClasLoader);
//            contextClasLoader = clasloader;
//        } catch (MalformedURLException e) {
//
//        }
        Window win = new Window();
    //    win.setClassLoader(contextClasLoader);
        win.showUI();
//        System.gc();
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
