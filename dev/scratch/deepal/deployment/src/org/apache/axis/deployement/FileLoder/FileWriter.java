package org.apache.axis.deployement.FileLoder;

import org.apache.axis.deployement.FileLoder.utill.FileList;
import org.apache.axis.deployement.FileLoder.utill.FileItem;

import java.util.Vector;
import java.io.PrintStream;
import java.io.FileOutputStream;

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
 *         2:55:31 PM
 *
 */
public class FileWriter {

    private String filename = "./resource/Deploye.txt";
    public void writeToFile(){
        {
            FileOutputStream out; // declare a file output object
            PrintStream p; // declare a print stream object
            try
            {
                // Create a new file output stream
                out = new FileOutputStream(filename);
                p = new PrintStream( out );
                Vector invect = FileList.getJarlist();
                int size = invect.size();
                FileItem tempnaem;
                for (int i = 0; i < size; i++) {
                    tempnaem = (FileItem) invect.get(i);
                    p.println(tempnaem.getFilename());
                }
                p.close();
            }
            catch (Exception e)
            {
                System.err.println ("Error writing to file");
            }
        }

    }

}
