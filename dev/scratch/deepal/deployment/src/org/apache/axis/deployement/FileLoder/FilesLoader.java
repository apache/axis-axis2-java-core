package org.apache.axis.deployement.FileLoder;

import org.apache.axis.deployement.FileLoder.utill.UnZipJAR;
import org.apache.axis.deployement.FileLoder.utill.FileList;
import org.apache.axis.deployement.FileLoder.utill.FileItem;

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
     * to store curreently checking jars
     */
    private Vector current_jars;

    private FileList fileList;

    private String foldername;

    public FilesLoader(String foldername) {
        this.foldername = foldername;
        this.fileList = new FileList();
        fileList.init();
    }

    public void searchFolder(){
        String files[];
        current_jars = new Vector();
        File root = new File(foldername);
        // adding the root folder to the vector
        current_jars.addElement(root);

        while (current_jars.size() > 0) {         // loop until empty
            File dir = (File)current_jars.elementAt(0); // get first dir
            current_jars.remove(0);       // remove it
            files = dir.list();              // get list of files

            for (int i = 0; i < files.length ; i++) { // iterate
                File f = new File(dir, files[i]);
                if (f.isDirectory()) {        // see if it's a directory
                    current_jars.insertElementAt(f, 0);
                } // add dir to start of agenda
                else if (isJarFile(f.getName())){
                    FileItem fileItem = new FileItem(f,f.getName(),true);
                    fileList.addFile(fileItem);
                }
            }
        }
       fileList.update();
    }

    private boolean isJarFile(String filename) {
        // to check whether the file is  a jar file
        if(! filename.endsWith(".jar")){
            return false;
        }else
            return true;
    }


}



