package org.apache.axis2.tools.idea;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import org.apache.axis2.tools.bean.CodegenBean;
import org.apache.axis2.tools.bean.SrcCompiler;
import org.apache.ideaplugin.bean.JarFileWriter;

import javax.swing.*;
import java.awt.*;
import java.io.*;

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
 * Date: Jul 19, 2005
 * Time: 2:26:15 PM
 */
public class Java2CodeFrame extends JFrame {
    ImagePanel panel_3;
    JPanel plMiddle;
    BottomPanel lblBottom;
    //    SecondPanel secondPanel;
    SecondFrame secondPanel;
    OutPutPane outputpane;
    OptionPane optionPane;
    private int panleID = 0;
    private ClassLoader classLoader;

    // To keep the value of wsdl wizzard
    private CodegenBean codegenBean;

    public Java2CodeFrame() {
        windowLayout customLayout = new windowLayout(1);

        getContentPane().setFont(new Font("Helvetica", Font.PLAIN, 12));
        getContentPane().setLayout(customLayout);

        codegenBean = new CodegenBean();

        panel_3 = new ImagePanel();
        panel_3.setCaptions("  WSDL selection page"
                , "  Welcome to the Axis2 code generation wizard. Select the WSDL file");

        getContentPane().add(panel_3);

        plMiddle = new FirstPanel(codegenBean);
        getContentPane().add(plMiddle);

        lblBottom = new BottomPanel(this);
        BottomPanel.setEnable(false, false, true);
        getContentPane().add(lblBottom);

        optionPane = new OptionPane();
        optionPane.setVisible(false);
        getContentPane().add(optionPane);

        secondPanel = new SecondFrame();
        secondPanel.setVisible(false);
        getContentPane().add(secondPanel);

        outputpane = new OutPutPane(codegenBean);
        outputpane.setVisible(false);
        getContentPane().add(outputpane);

        Dimension dim = new Dimension(450, 350);
        setSize(dim);
        setBounds(200, 200, dim.width, dim.height);
        this.setResizable(false);
    }

    public void setProject(Project project) {
        codegenBean.setProject(project);
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public void generatecode() throws Exception {
        secondPanel.fillBean();
        codegenBean.generate();
    }

    public void generateDefaultServerCode(File temp, String output) throws Exception {


        temp.mkdir();
        try {
            codegenBean.generate();
            copyDirectory(new File(temp + File.separator + "src"), new File(output));
            copyDirectory(new File(temp + File.separator + "resources"), new File(output + File.separator + ".." + File.separator + "resources"));

        } catch (Exception e1) {
            e1.printStackTrace();
        }
        finally {

            deleteDirectory(temp);
        }
    }

    public void copyDirectory(File srcDir, File destDir) throws IOException {
        if (srcDir.isDirectory()) {
            if (!destDir.isDirectory()) {
                destDir.mkdir();
            }
            String[] children = srcDir.list();
            for (int count = 0; count < children.length; count++) {
                copyDirectory(new File(srcDir, children[count]), new File(destDir, children[count]));
            }
        } else {
            copyFiles(srcDir, destDir);
        }
    }

    public void copyFiles(File src, File dest) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dest);
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();

    }

    public void deleteDirectory(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int count = 0; count < children.length; count++) {
                deleteDirectory(new File(dir, children[count]));
            }
        }
        dir.delete();
    }

    public void generateDefaultClientCode(File temp) throws Exception {


        temp.mkdir();
        try {
            codegenBean.generate();
            SrcCompiler compiler = new SrcCompiler();
            compiler.compileSource(temp.getAbsolutePath());
            String wsdl = codegenBean.getWSDLFileName();
            final String name = wsdl.substring(wsdl.lastIndexOf(File.separatorChar) + 1, wsdl.lastIndexOf(".")) + "-stub.jar";
            System.out.println(name);
            File lib = new File(codegenBean.getActiveProject().getProjectFile().getParent().getPath() + File.separator + "lib");
            if (!lib.isDirectory()) {
                lib.mkdir();
            }
            JarFileWriter jarFileWriter = new JarFileWriter();
            jarFileWriter.writeJarFile(lib, name, new File(temp + File.separator + "classes"));
            Project project = codegenBean.getActiveProject();

            final LibraryTable table = (LibraryTable) project.getComponent(LibraryTable.class);


            String url = VirtualFileManager.constructUrl(JarFileSystem.PROTOCOL, lib.getAbsolutePath() + File.separator + name) + JarFileSystem.JAR_SEPARATOR;

            final VirtualFile jarVirtualFile = VirtualFileManager.getInstance().findFileByUrl(url);

            ApplicationManager.getApplication().runWriteAction(new
                    Runnable() {
                        public void run() {
                            Library myLibrary = table.createLibrary(name);

                            Library.ModifiableModel libraryModel = myLibrary.getModifiableModel();
                            libraryModel.addRoot(jarVirtualFile, OrderRootType.CLASSES);
                            libraryModel.commit();

                        }
                    });
        } catch (Exception e1) {
           throw e1;
        }
        finally {

            deleteDirectory(temp);
        }
    }


    public void setPane() {
        panleID++;
        switch (panleID) {
            case 1: {
                panel_3.setCaptions("  Options"
                        , " Select from custom or default");
                this.secondPanel.setVisible(false);
                this.plMiddle.setVisible(false);
                this.optionPane.setCodeGenBean(codegenBean);
                this.optionPane.setVisible(true);
                BottomPanel.setEnable(true, false, true);
                break;
            }
            case 2: {
                panel_3.setCaptions("  Options"
                        , "  Set the options for the code generation");
                this.secondPanel.setVisible(true);
                this.secondPanel.setCodeGenBean(codegenBean);
                this.plMiddle.setVisible(false);
                this.optionPane.setVisible(false);
                BottomPanel.setEnable(true, false, true);
                break;
            }
            case 3: {
                panel_3.setCaptions("  Output"
                        , "  set the output project for the generated code");
                this.secondPanel.setVisible(false);
                this.plMiddle.setVisible(false);
                this.optionPane.setVisible(false);
                outputpane.loadCmbCurrentProject();
                outputpane.loadcmbModuleSrcProject();
                this.outputpane.setVisible(true);
                BottomPanel.setEnable(false, true, true);
                break;
            }
        }
    }

    public void increasePanelID() {
        panleID++;
    }

    public void setMiddlerPanel(int panel) {
        this.panleID = panel;
        if (panleID == 2) {
            panel_3.setCaptions("  Options"
                    , "  Set the options for the code generation");
            this.secondPanel.setVisible(true);
            this.plMiddle.setVisible(false);
            BottomPanel.setEnable(true, true, true);
        }
        this.pack();
        this.show();
    }


}

class windowLayout implements LayoutManager {

    int paneID;

    public windowLayout(int panelid) {
        paneID = panelid;
    }

    public void addLayoutComponent(String name, Component comp) {
    }

    public void removeLayoutComponent(Component comp) {
    }

    public Dimension preferredLayoutSize(Container parent) {
        Dimension dim = new Dimension(0, 0);

        Insets insets = parent.getInsets();
        dim.width = 550 + insets.left + insets.right;
        dim.height = 460 + insets.top + insets.bottom;

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
            c.setBounds(insets.left, insets.top, 550, 80);
        }
        c = parent.getComponent(1);
        if (c.isVisible()) {
            c.setBounds(insets.left, insets.top + 80, 550, 330);
        }
        c = parent.getComponent(3);
        if (c.isVisible()) {
            c.setBounds(insets.left, insets.top + 80, 550, 330);
        }
        c = parent.getComponent(4);
        if (c.isVisible()) {
            c.setBounds(insets.left, insets.top + 80, 550, 330);
        }
        c = parent.getComponent(5);
        if (c.isVisible()) {
            c.setBounds(insets.left, insets.top + 80, 550, 330);
        }
        c = parent.getComponent(2);
        if (c.isVisible()) {
            c.setBounds(insets.left, insets.top + 410, 550, 50);
        }
    }
}
