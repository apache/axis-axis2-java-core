package org.apache.axis.deployement.FileLoader;

import org.apache.axis.deployement.FileLoader.utill.WSInfo;

import java.util.Vector;
import java.io.FileInputStream;
import java.io.DataInputStream;

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
 *         2:54:57 PM
 *
 */
public class FileReader {
    private String filename = "./resource/Deploye.txt";
    public Vector getDeployedJars(){
        Vector files = new Vector();
        {
            char seprator = ':';
            try{
                FileInputStream fstream = new  FileInputStream(filename);
                DataInputStream in = new DataInputStream(fstream);
                while (in.available() !=0)
                {
                    String line = in.readLine();
                    int charpos = line.indexOf(seprator);
                    String jarname= line.substring(0,charpos);
                    String lastmodified =line.substring(charpos + 1, line.length());
                    WSInfo fileItem = new WSInfo(jarname , Long.parseLong(lastmodified));
                    files.add(fileItem);
                }

                in.close();
            }
            catch (Exception e)
            {
                System.err.println("File input error");
            }
        }
        return files;
    }

}
package org.apache.axis.deployement.FileLoader;

import org.apache.axis.deployement.FileLoader.utill.WSInfo;

import java.util.Vector;
import java.io.FileInputStream;
import java.io.DataInputStream;

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
 *         2:54:57 PM
 *
 */
public class FileReader {
    private String filename = "./resource/Deploye.txt";
    public Vector getDeployedJars(){
        Vector files = new Vector();
        {
            char seprator = ':';
            try{
                FileInputStream fstream = new  FileInputStream(filename);
                DataInputStream in = new DataInputStream(fstream);
                while (in.available() !=0)
                {
                    String line = in.readLine();
                    int charpos = line.indexOf(seprator);
                    String jarname= line.substring(0,charpos);
                    String lastmodified =line.substring(charpos + 1, line.length());
                    WSInfo fileItem = new WSInfo(jarname , Long.parseLong(lastmodified));
                    files.add(fileItem);
                }

                in.close();
            }
            catch (Exception e)
            {
                System.err.println("File input error");
            }
        }
        return files;
    }

}
