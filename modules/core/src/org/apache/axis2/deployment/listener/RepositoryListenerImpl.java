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

package org.apache.axis2.deployment.listener;

import org.apache.axis2.deployment.DeploymentConstants;
import org.apache.axis2.deployment.DeploymentEngine;
import org.apache.axis2.deployment.repository.util.ArchiveFileData;
import org.apache.axis2.deployment.repository.util.WSInfoList;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class RepositoryListenerImpl implements RepositoryListener,
        DeploymentConstants {

    protected Log log = LogFactory.getLog(getClass());

    /**
     * to store curreently checking jars
     */
    private List currentJars;
    /**
     * Referance to a WSInfoList
     */
    private WSInfoList wsinfoList;

    private int BUFFER = 2048;

    /**
     * The parent directory of the modules and services directories
     * taht the listentner should listent
     */
    private String folderName;


    private boolean extarctServiceArchive = false;

    /**
     * This constructor take two argumnets folder name and referance to Deployment Engine
     * Fisrt it initilize the syetm , by loading all the modules in the /modules directory
     * and also create a WSInfoList to keep infor about available modules and services
     *
     * @param folderName    path to parent directory that the listener should listent
     * @param deploy_engine refearnce to engine registry  inorder to inform the updates
     */
    public RepositoryListenerImpl(String folderName,
                                  DeploymentEngine deploy_engine, boolean serviceExtarct) {
        this.folderName = folderName;
        wsinfoList = new WSInfoList(deploy_engine);
        this.extarctServiceArchive = serviceExtarct;
        init();
    }

    /**
     * this method ask serachWS to serch for the folder to caheck
     * for updates
     */
    public void checkModules() {
        String modulepath = folderName + MODULE_PATH;
        String files[];
        currentJars = new ArrayList();
        File root = new File(modulepath);
        // adding the root folder to the vector
        currentJars.add(root);

        while (currentJars.size() > 0) {        // loop until empty
            File dir = (File) currentJars.get(0); // get first dir
            currentJars.remove(0);       // remove it
            files = dir.list();              // get list of files
            if (files == null) {
                continue;
            }
            for (int i = 0; i < files.length; i++) { // iterate
                File f = new File(dir, files[i]);
                if (f.isDirectory()) {        // see if it's a directory
                    currentJars.add(0, f);
                } // add dir to start of agenda
                else if (ArchiveFileData.isModuleArchiveFile(f.getName())) {
                    wsinfoList.addWSInfoItem(f, MODULE);
                }
            }
        }
    }

    /**
     * this method ask serachWS to serch for the folder to caheck
     * for updates
     */
    public void checkServices() {
        String modulepath = folderName + SERVICE_PATH;
        if(extarctServiceArchive){
            searchExploedServies(modulepath);
        }else {
            searchWS(modulepath, SERVICE);
        }
    }

    /**
     * call to update method of WSInfoList object
     */
    public void update() {
        //todo completet this
        // this call the update method of WSInfoList
        wsinfoList.update();
    }

    /**
     * First it call to initalize method of WSInfoList to initilizat that
     * then it call to checkModules to load all the module.jar s
     * and then it call to update() method inorder to update the Deployment engine and
     * engine regsitry
     */
    public void init() {
        wsinfoList.init();
        checkModules();
        checkServices();
        update();
    }

    /**
     * this is the actual method that is call from scheduler
     */
    public void startListent() {
        // checkModules();
        checkServices();
        update();
    }

    /**
     * This method is to search a given folder  for jar files
     * and added them to a list wich is in the WSInfolist class
     */
    private void searchWS(String folderName, int type) {
        String files[];
        currentJars = new ArrayList();
        File root = new File(folderName);
        // adding the root folder to the vector
        currentJars.add(root);

        while (currentJars.size() > 0) {        // loop until empty
            File dir = (File) currentJars.get(0); // get first dir
            currentJars.remove(0);       // remove it
            files = dir.list();              // get list of files
            if (files == null) {
                continue;
            }
            for (int i = 0; i < files.length; i++) { // iterate
                File f = new File(dir, files[i]);
                if (f.isDirectory()) {        // see if it's a directory
                    currentJars.add(0, f);
                } // add dir to start of agenda
                else if (ArchiveFileData.isServiceArchiveFile(f.getName())) {
                    wsinfoList.addWSInfoItem(f, type);
                }
            }
        }
    }


    private void searchExploedServies(String rootDirName){
        File rootDir = new File(rootDirName);
        File [] fileList = rootDir.listFiles();
        ArrayList tobedelete = new ArrayList();
        ArrayList tobeextarct = new ArrayList();
        ArrayList allFiles = new ArrayList();
        for (int i = 0; fileList != null && i < fileList.length; i++) {
            File file_first = fileList[i];
            boolean isservice = ArchiveFileData.isServiceArchiveFile(file_first.getName());
            boolean found = false;
            if(isservice){
                for (int j = 0; j < fileList.length; j++) {
                    File file_second = fileList[j];
                    if(file_second.getName().equalsIgnoreCase(
                            getShortFileName(file_first.getName()))){
                        if(file_second.lastModified() >= file_first.lastModified()){
                            found = true;
                            allFiles.add(file_second);
                            break;
                        } else if (file_second.lastModified() < file_first.lastModified()){
                            tobedelete.add(file_second);
                            tobeextarct.add(file_first);
                            found = true;
                        }
                    }
                }
                if(!found){
                    tobeextarct.add(file_first);
                }
            }
        }
        for (int i = 0; i < tobedelete.size(); i++) {
            File file1 = (File) tobedelete.get(i);
            deleteDir(file1);
        }

        for (int i = 0; i < tobeextarct.size(); i++) {
            File file1 = (File) tobeextarct.get(i);
            File outFile = new File(rootDirName,getShortFileName(file1.getName()));
            if(!outFile.exists()){
                outFile.mkdir();
            }
            extarctServiceArchive(file1,outFile);
            allFiles.add(outFile);
        }

        for (int i = 0; fileList != null && i < fileList.length; i++) {
            File file = fileList[i];
            boolean found = false;
            boolean todo = ArchiveFileData.isServiceArchiveFile(file.getName());
            if (!todo) {
                for (int j = 0; j < allFiles.size(); j++) {
                    File file1 = (File) allFiles.get(j);
                    if(file1.getName().equals(file.getName())){
                        found = true;
                    }
                }
                if(!found){
                    allFiles.add(file);
                }
            }
        }

        for (int i = 0; i < allFiles.size(); i++) {
            File file = (File) allFiles.get(i);
            wsinfoList.addWSInfoItem(file, SERVICE);
        }
    }

    /**
     * To delete a given directory with its all childerns
     * @param dir
     * @return  boolean
     */
    private boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i=0; i<children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        // The directory is now empty so delete it
        return dir.delete();
    }


    /**
     * To get the file name without exetension
     * @param fileName
     * @return boolean
     */
    private String getShortFileName(String fileName) {
        char seperator = '.';
        String value;
        int index = fileName.lastIndexOf(seperator);
        if (index > 0) {
            value = fileName.substring(0, index);
            return value;
        }
        return fileName;
    }

    /**
     * Will extarct given file , into same dirctory with the name of archive file (removing file
     * extension)
     * @param infile  <code>java.io.File</code>   Archive file
     * @param outdirctory <code>java.io.File</code>  output file
     */
    public void extarctServiceArchive(File infile , File outdirctory ) {
        try{
            BufferedOutputStream dest;
            FileInputStream fis = new  FileInputStream(infile);
            ZipInputStream zis = new
                    ZipInputStream(new BufferedInputStream(fis));
            ZipEntry entry;

            outdirctory.mkdir();
            File outFile ;
            String outPath =  outdirctory.getAbsolutePath() + "/";
            while((entry = zis.getNextEntry()) != null) {
                int count;
                byte data[] = new byte[BUFFER];
                // write the files to the disk
                outFile = new File(outPath + entry.getName());
                if(entry.isDirectory()){
                    if(!outFile.exists()){
                        outFile.mkdir();
                    }
                    continue;
                }
                FileOutputStream fos = new
                        FileOutputStream(outFile);
                dest = new
                        BufferedOutputStream(fos, BUFFER);
                while ((count = zis.read(data, 0, BUFFER))
                        != -1) {
                    dest.write(data, 0, count);
                }
                dest.flush();
                dest.close();
            }
            zis.close();
        } catch(Exception e) {
            log.error(e);
        }
    }


}
