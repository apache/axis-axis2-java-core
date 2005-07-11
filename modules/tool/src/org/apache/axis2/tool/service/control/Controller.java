package org.apache.axis.tool.service.control;

import org.apache.axis.tool.core.ClassFileHandler;
import org.apache.axis.tool.core.FileCopier;
import org.apache.axis.tool.core.JarFileWriter;
import org.apache.axis.tool.core.ServiceFileCreator;
import org.apache.axis.tool.service.bean.Page1Bean;
import org.apache.axis.tool.service.bean.Page2Bean;
import org.apache.axis.tool.service.bean.Page3Bean;
import org.apache.axis.tool.service.bean.WizardBean;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

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
 */

public class Controller {

    public ArrayList getMethodList(WizardBean bean) throws ProcessException {
        ArrayList returnList = null;
        try {
            returnList = new ClassFileHandler().getMethodNamesFromClass(bean.getPage2bean().getAutomaticClassName(),
                                                                        bean.getPage1bean().getFileLocation());
        } catch (IOException e) {
            throw new ProcessException("IO Error, The class file location may be faulty!", e);
        } catch (ClassNotFoundException e) {
            throw new ProcessException(" The specified class does not exist!!!");
        } catch (Exception e) {
            throw new ProcessException("Unknown Error! See whether all parameters are available");
        }
        return returnList;
    }


    public void process(WizardBean bean) throws ProcessException, Exception {

        Page1Bean page1Bean = bean.getPage1bean();
        Page2Bean page2Bean = bean.getPage2bean();
        Page3Bean page3Bean = bean.getPage3bean();

        File serviceFile = null;
        File classFileFolder = null;
        File outputFolder = null;
        String outputFileName = null;
        boolean isServiceCreated = false;

        //see if the class file location is valid
        classFileFolder = new File(page1Bean.getFileLocation());
        if (!classFileFolder.exists()) {
            throw new ProcessException("Specified Class file location is empty!!");
        }
        if (!classFileFolder.isDirectory()) {
            throw new ProcessException("The class file location must be a folder!");
        }

        //see if the  service.xml file is valid
        if (page2Bean.isManual()) {
            serviceFile = new File(page2Bean.getManualFileName());
            if (!serviceFile.exists()) {
                throw new ProcessException("Specified Service XML file is missing!");
            }
        } else {
            ArrayList methodList = page2Bean.getSelectedMethodNames();
            if (methodList.isEmpty()) {
                throw new ProcessException("There are no methods selected to generate the service!!");
            }
            serviceFile = new ServiceFileCreator().createServiceFile(page2Bean.getProviderClassName(),
                                                                     page2Bean.getAutomaticClassName(),
                                                                     page2Bean.getSelectedMethodNames());//create the file here
            isServiceCreated = true;
        }

        outputFolder = new File(page3Bean.getOutputFolderName());
        outputFileName = page3Bean.getOutputFileName();
        if (!outputFileName.toLowerCase().endsWith(".jar")) {
            outputFileName = outputFileName + ".jar";
        }

        File tempFileFolder = null;

        try {
            //create a temporary directory and copy the files
            tempFileFolder = new File("Service-copy");
            tempFileFolder.mkdir();
            File metaInfFolder = new File(tempFileFolder, "META-INF");
            metaInfFolder.mkdir();

            new FileCopier().copyFiles(classFileFolder, tempFileFolder);
            new FileCopier().copyFiles(serviceFile, metaInfFolder);

            //jar the temp directory. the output folder will be created if missing
            new JarFileWriter().writeJarFile(outputFolder, outputFileName, tempFileFolder);
        } catch (Exception e) {
            throw new ProcessException(e);
        } finally {
            tempFileFolder.delete();
            if (isServiceCreated)
                serviceFile.delete();


        }

    }
}
