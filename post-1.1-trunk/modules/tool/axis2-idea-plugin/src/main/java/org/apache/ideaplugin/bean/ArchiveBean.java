package org.apache.ideaplugin.bean;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
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
*
*
*/

/**
 * Author: Deepal Jayasinghe
 * Date: Sep 17, 2005
 * Time: 11:40:40 PM
 */
public class ArchiveBean {

    private boolean singleService = false;
    private boolean generetServiceDesc = false;
    private File classLocation;
    private ClassLoader classLoader;
    private String ServiceXML;

    public String fileSeparator = System.getProperty("file.separator");

    private ArrayList libs;
    private ArrayList wsdls;

    private String outPath;
    private String archiveName;

    public boolean isSingleService() {
        return singleService;
    }

    public void setSingleService(boolean singleService) {
        this.singleService = singleService;
    }

    public boolean isGeneretServiceDesc() {
        return generetServiceDesc;
    }

    public void setGeneretServiceDesc(boolean generetServiceDesc) {
        this.generetServiceDesc = generetServiceDesc;
    }

    public File getClassLocation() {
        return classLocation;
    }

    public void setClassLocation(File classLocation) {
        this.classLocation = classLocation;
    }

    public String getServiceXML() {
        return ServiceXML;
    }

    public void setServiceXML(String serviceXML) {
        ServiceXML = serviceXML;
    }

    public ArrayList getLibs() {
        return libs;
    }

    public void setLibs(ArrayList libs) {
        this.libs = libs;
    }

    public ArrayList getWsdls() {
        return wsdls;
    }

    public void setWsdls(ArrayList wsdls) {
        this.wsdls = wsdls;
    }

    public String getOutPath() {
        return outPath;
    }

    public void setOutPath(String outPath) {
        this.outPath = outPath;
    }

    public String getArchiveName() {
        return archiveName;
    }

    public void setArchiveName(String archiveName) {
        this.archiveName = archiveName;
    }

    public void finsh() {
        //Creating out File
        try {
            File outFile = new File(getOutPath());
            File tempfile = new File(outFile, "temp");
            if (!tempfile.exists()) {
                tempfile.mkdirs();
            }
            //creating META-INF
            File metainf = new File(tempfile, "META-INF");
            if (!metainf.exists()) {
                metainf.mkdir();
            }

            // Writing services.xml
            File servicexml = new File(metainf, "services.xml");
            FileWriter writer = new FileWriter(servicexml);
            writer.write(getServiceXML());
            writer.flush();
            writer.close();

            //Coping class files
            FileCopier fc = new FileCopier();
            fc.copyFiles(getClassLocation(), tempfile, null);

            // Coping wsdl files
            File lib = new File(tempfile, "lib");
            if (!lib.exists()) {
                lib.mkdir();
            }
            for (int i = 0; i < libs.size(); i++) {
                String libname = (String) libs.get(i);
                fc.copyFiles(new File(libname), lib, null);
            }

            //coping wsdl files
            for (int i = 0; i < wsdls.size(); i++) {
                String libname = (String) wsdls.get(i);
                fc.copyFiles(new File(libname), metainf, null);
            }

            String arcivename = getArchiveName();
            if (arcivename.indexOf(".aar") < 0) {
                arcivename = arcivename + ".aar";
            }
            JarFileWriter jwriter = new JarFileWriter();
            jwriter.writeJarFile(outFile, arcivename, tempfile);
            //craeting the jar file
            deleteDir(tempfile);
//
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }

        // The directory is now empty so delete it
        return dir.delete();
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }


}
