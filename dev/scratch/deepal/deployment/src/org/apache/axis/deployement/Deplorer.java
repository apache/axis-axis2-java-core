package org.apache.axis.deployement;

import org.apache.axis.deployement.FileLoader.FileWriter;
import org.apache.axis.deployement.FileLoader.utill.WSInfo;

import java.util.Vector;
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
 *         Oct 13, 2004
 *         12:33:17 PM
 *
 */
public class Deplorer {
    /**
     * This will store all the web Services to deploye
     */
    private Vector wsToDeploye = new Vector();
    /**
     * this will store all the web Services to undeploye
     */
    private Vector wsToUnDeploye = new Vector();


    public Deplorer() {
    }

    public void addNewWS(){
        FileWriter writer = new FileWriter();
        writer.writeToFile();
    }

   /**
    *
    * @param file
    */
    public void addtowsToDeploye(File file){
        wsToDeploye.add(file);
    }

    /**
     *
     * @param file
     */
    public void addtowstoUnDeploye(WSInfo file){
        wsToUnDeploye.add(file);
    }

    public void doDeploye(){
        //todo complete this
        if(wsToDeploye.size() >0){
            for (int i = 0; i < wsToDeploye.size(); i++) {
                File fileItem = (File) wsToDeploye.elementAt(i);
               // System.out.println("File" + fileItem.toString());
                System.out.println("Deployement WS Name  "  + fileItem.getName());
            }
            addNewWS();
        }
        wsToDeploye.removeAllElements();
    }

    public void doUnDeploye(){
        //todo complete this
        if(wsToUnDeploye.size()>0){
            for (int i = 0; i < wsToUnDeploye.size(); i++) {
                WSInfo wsInfo = (WSInfo) wsToUnDeploye.elementAt(i);
                System.out.println("UnDeployement WS Name  "  + wsInfo.getFilename());
            }

        }
        wsToUnDeploye.removeAllElements();
    }


}
package org.apache.axis.deployement;

import org.apache.axis.deployement.FileLoader.FileWriter;
import org.apache.axis.deployement.FileLoader.utill.WSInfo;

import java.util.Vector;
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
 *         Oct 13, 2004
 *         12:33:17 PM
 *
 */
public class Deplorer {
    /**
     * This will store all the web Services to deploye
     */
    private Vector wsToDeploye = new Vector();
    /**
     * this will store all the web Services to undeploye
     */
    private Vector wsToUnDeploye = new Vector();


    public Deplorer() {
    }

    public void addNewWS(){
        FileWriter writer = new FileWriter();
        writer.writeToFile();
    }

   /**
    *
    * @param file
    */
    public void addtowsToDeploye(File file){
        wsToDeploye.add(file);
    }

    /**
     *
     * @param file
     */
    public void addtowstoUnDeploye(WSInfo file){
        wsToUnDeploye.add(file);
    }

    public void doDeploye(){
        //todo complete this
        if(wsToDeploye.size() >0){
            for (int i = 0; i < wsToDeploye.size(); i++) {
                File fileItem = (File) wsToDeploye.elementAt(i);
               // System.out.println("File" + fileItem.toString());
                System.out.println("Deployement WS Name  "  + fileItem.getName());
            }
            addNewWS();
        }
        wsToDeploye.removeAllElements();
    }

    public void doUnDeploye(){
        //todo complete this
        if(wsToUnDeploye.size()>0){
            for (int i = 0; i < wsToUnDeploye.size(); i++) {
                WSInfo wsInfo = (WSInfo) wsToUnDeploye.elementAt(i);
                System.out.println("UnDeployement WS Name  "  + wsInfo.getFilename());
            }

        }
        wsToUnDeploye.removeAllElements();
    }


}
