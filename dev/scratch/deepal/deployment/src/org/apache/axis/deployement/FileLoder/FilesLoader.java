package org.apache.axis.deployement.FileLoder;

import org.apache.axis.deployement.FileLoder.utill.UnZipJAR;

import java.util.Vector;
import java.util.Date;
import java.io.File;

/**
 * Copyright 2001-2004 The Apache Software Foundation.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @author Deepal Jayasinghe
 *         Oct 5, 2004
 *         11:31:48 AM
 *
 */
public class FilesLoader {
    /**
     * This is to store all the jar files in a specified folder (WEB_INF)
     */
    private static Vector jarlist = new Vector();

    private String foldername="D:/Axis 2.0/projects/Deployement/test-data/" ;

    public void searchFolder(){
        String files[];
        Vector currentjars = new Vector();
        File root = new File(foldername);
        // adding the root folder to the vector
        currentjars.addElement(root);

        while (currentjars.size() > 0) {         // loop until empty
            File dir = (File)currentjars.elementAt(0); // get first dir
            currentjars.remove(0);       // remove it
            files = dir.list();              // get list of files

            for (int i = 0; i < files.length; i++) { // iterate
                File f = new File(dir, files[i]);
                if (f.isDirectory()) {        // see if it's a directory
                    currentjars.insertElementAt(f, 0);
                } // add dir to start of agenda
                else if (! FilesLoader.isFileExist(f.getName())){
                    addNewWS(f);
                }
            }
        }
    }

    public void init(){
        try{
            jarlist.removeAllElements();
            initDeployedWS();
        }   catch(Exception e) {
            e.printStackTrace();
        }
    }



    private static void addNewWS(File file){
        String filename = file.getName();
        int size = jarlist.size();
        boolean exist = false;

        for (int i = 0; i < size; i++) {
            String s = (String) jarlist.elementAt(i);
            if(s.equals(filename)){
                exist = true;
                break;
            }
        }

        if(! exist){
            jarlist.add(filename) ;
            FileWriter writer = new FileWriter();
            writer.writeToFile(jarlist);
            //todo write a triger
            System.out.println("New Web service is deployed   "+   filename);
        }
    }


    private void initDeployedWS() {
        FileReader fileReader = new FileReader();
        Vector fiels = fileReader.getDeployedJars();
        int size = fiels.size();
        for (int i = 0; i < size-1; i++) {
            jarlist.add((String)fiels.get(i));
        }
    }

    private static boolean isFileExist(String filename) {
        // to check whether the file is  a jar file
        if(! filename.endsWith(".jar")){
            return true;
        }
        int vetsize = jarlist.size();
        String file;
        for (int i = 0; i < vetsize - 1; i++) {
            file = (String) jarlist.get(i);
            if(file.equals(filename)){
                return true;
            }

        }
        return false;
    }
}



