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
import org.apache.ideaplugin.frames.Axi2PluginPage;

import javax.swing.*;
import javax.wsdl.WSDLException;
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
    FirstPanel plMiddle;
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

        setTitle("Axis2 Code Generation Wizard");


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
        BottomPanel.setEnable(false,false, false, true);
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

        Dimension dim = new Dimension(450, 600);
        setSize(dim);
        setBounds(200, 200, dim.width, dim.height);
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
            throw e1;
        }
        finally {

            deleteDirectory(temp);
        }
    }

    public void generateDefaultServerCodeCustomLocation(String output) throws Exception {


        try {

            codegenBean.setOutput(output);
            codegenBean.generate();

        } catch (Exception e1) {
            throw e1;
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
            final String name = wsdl.substring(wsdl.lastIndexOf(File.separatorChar) + 1, wsdl.lastIndexOf(".")) + "-stub";
            final File lib = new File(codegenBean.getActiveProject().getProjectFilePath() + File.separator + "lib");
            if (!lib.isDirectory()) {
                lib.mkdir();
            }
            JarFileWriter jarFileWriter = new JarFileWriter();
            jarFileWriter.writeJarFile(lib, name + ".jar", new File(temp + File.separator + "classes"));
            Project project = codegenBean.getActiveProject();

            final LibraryTable table = (LibraryTable) project.getComponent(LibraryTable.class);




            ApplicationManager.getApplication().runWriteAction(new
                    Runnable() {
                        public void run() {

                            String url = VirtualFileManager.constructUrl(JarFileSystem.PROTOCOL, lib.getAbsolutePath() + File.separator + name + ".jar") + JarFileSystem.JAR_SEPARATOR;



                            VirtualFile jarVirtualFile = VirtualFileManager.getInstance().findFileByUrl(url);
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
                try {
                    codegenBean.readWSDL();
                } catch (WSDLException e) {
                    JOptionPane.showMessageDialog(this, "An error occured while parsing the " +
                            "specified WSDL. Please make sure that the selected file is a valid WSDL.",
                            "Error!", JOptionPane.ERROR_MESSAGE);
                    panleID--;
                    break;
                }
                panel_3.setCaptions("  Options"
                        , " Select from custom or default");
                this.secondPanel.setVisible(false);
                this.plMiddle.setVisible(false);
                if (this.optionPane.codegenBean == null)
                    this.optionPane.setCodeGenBean(codegenBean);
                this.optionPane.setVisible(true);
                this.outputpane.setVisible(false);
                BottomPanel.setEnable(true,true, false, true);
                break;
            }
            case 2: {
                panel_3.setCaptions("  Custom Options"
                        , "  Set the options for the code generation");

                this.secondPanel.setVisible(true);
                if(this.secondPanel.codegenBean == null)
                    this.secondPanel.setCodeGenBean(codegenBean);
                this.secondPanel.setStatus();
                this.plMiddle.setVisible(false);
                this.optionPane.setVisible(false);
                this.outputpane.setVisible(false);
                BottomPanel.setEnable(true,true, false, true);
                break;
            }
            case 3: {
                String result;
                if (this.optionPane.radCustom.isSelected() && (result = validatePackageNames()) != null)
                {
                    JOptionPane.showMessageDialog(this, "The package name " + result + " is not a valid package name",
                            "Error!!!", JOptionPane.INFORMATION_MESSAGE);
                    panleID--;
                    break;
                }

                panel_3.setCaptions("  Set the output location for the generated code"
                        , "  set the output project for the generated code");
                this.secondPanel.setVisible(false);
                this.plMiddle.setVisible(false);
                this.optionPane.setVisible(false);
                outputpane.loadCmbCurrentProject();
                outputpane.loadcmbModuleSrcProject();
                this.outputpane.setVisible(true);
                BottomPanel.setEnable(true,false, true, true);
                break;
            }
        }
    }

    public String validatePackageNames(){
        if (!validatePackageName(this.secondPanel.txtPacakgeName.getText()))
            return this.secondPanel.txtPacakgeName.getText();

        for(int count=0;count<this.secondPanel.table.getRowCount();count++){
            if(!validatePackageName((String)this.secondPanel.table.getValueAt(count,1)))
                return (String)this.secondPanel.table.getValueAt(count,1);
        }
        return null;
    }

    public boolean validatePackageName(String name){

        if(name.matches("[a-z]([a-z0-9_]+\\.?)+[a-z0-9_]"))
            return true;

        return false;
    }

    public void backButtonImpl(){
        panleID--;
        switch (panleID) {
            case 0: {
                panel_3.setCaptions("  WSDL selection page"
                        , "  Welcome to the Axis2 code generation wizard. Select the WSDL file");
                this.secondPanel.setVisible(false);
                this.plMiddle.setVisible(true);

                this.optionPane.setVisible(false);
                this.outputpane.setVisible(false);
                BottomPanel.setEnable(false,true, false, true);
                break;
            }
            case 1: {

                panel_3.setCaptions("  Options"
                        , " Select from custom or default");
                this.secondPanel.setVisible(false);
                this.plMiddle.setVisible(false);
                this.outputpane.setVisible(false);
                this.optionPane.setVisible(true);
                BottomPanel.setEnable(true,true, false, true);
                break;
            }
            case 2: {
                if (!this.optionPane.radCustom.isSelected())
                {

                    this.backButtonImpl();
                    break;
                }
                panel_3.setCaptions("  Custom  Options"
                        , "  Set the options for the code generation");

                this.secondPanel.setVisible(true);
                this.plMiddle.setVisible(false);
                this.optionPane.setVisible(false);
                this.outputpane.setVisible(false);
                BottomPanel.setEnable(true,true, false, true);
                break;
            }
            case 3: {
                panel_3.setCaptions("  Set the output location for the generated code"
                        , "  set the output project for the generated code");
                this.secondPanel.setVisible(false);
                this.plMiddle.setVisible(false);
                this.optionPane.setVisible(false);

                this.outputpane.setVisible(true);
                BottomPanel.setEnable(true,false, true, true);
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
            BottomPanel.setEnable(true,true, true, true);
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
        dim.height = 600 + insets.top + insets.bottom;

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
            c.setBounds(insets.left, insets.top, 600, 80);
        }
        c = parent.getComponent(1);
        if (c.isVisible()) {
            c.setBounds(insets.left, insets.top + 80, 600, 480);
        }
        c = parent.getComponent(3);
        if (c.isVisible()) {
            c.setBounds(insets.left, insets.top + 80, 600, 480);
        }
        c = parent.getComponent(4);
        if (c.isVisible()) {
            c.setBounds(insets.left, insets.top + 80, 600, 480);
        }
        c = parent.getComponent(5);
        if (c.isVisible()) {
            c.setBounds(insets.left, insets.top + 80, 600, 480);
        }
        c = parent.getComponent(2);
        if (c.isVisible()) {
            c.setBounds(insets.left, insets.top + 550, 600, 50);
        }
    }
}
