package org.apache.ideaplugin.plugin;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.ActionPlaces;

import javax.swing.*;

import org.apache.ideaplugin.frames.Axi2PluginPage;
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
 * Author: Deepal Jayasinghe
 * Date: Sep 24, 2005
 * Time: 10:22:08 AM
 */
public class Axis2PluginAction extends AnAction {

    private ImageIcon myIcon;

    public Axis2PluginAction() {
        super("GC", "Axis2 plugings", null);
    }

    public void actionPerformed(AnActionEvent anActionEvent) {
        Axi2PluginPage axis2lugin= new Axi2PluginPage();
        axis2lugin.showUI();
    }

    public void update(AnActionEvent event) {
        super.update(event);
        Presentation presentation = event.getPresentation();
        if (ActionPlaces.MAIN_TOOLBAR.equals(event.getPlace())) {
            if (myIcon == null) {
                java.net.URL resource = Axis2PluginAction.class.getResource("/icons/icon.png");
                myIcon = new ImageIcon(resource);
            }
            presentation.setIcon(myIcon);
        }
    }


}
