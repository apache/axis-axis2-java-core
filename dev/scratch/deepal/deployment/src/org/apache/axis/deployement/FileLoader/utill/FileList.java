package org.apache.axis.deployement.FileLoader.utill;

import org.apache.axis.deployement.Deplorer;
import org.apache.axis.deployement.FileLoader.FileReader;
import org.apache.axis.deployement.FileLoader.FileWriter;

import java.util.Iterator;
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
 *         12:13:11 PM
 *
 */
public class FileList {
    /**
     * This is to store all the jar files in a specified folder (WEB_INF)
     */
    private static Vector jarlist = new Vector() ;
    public Vector currentjars = new Vector();

    private Deplorer deplorer;



    public FileList() {
        deplorer = new Deplorer();
    }

    public static Vector getJarlist() {
        return jarlist;
    }

    /**
     * This method is used to initialize the vector
     */
    public void init(){
        try{
            jarlist.removeAllElements();
            FileReader fileReader = new FileReader();
            Vector fiels = fileReader.getDeployedJars();
            System.out.println("Initilize-Start");
            int size = fiels.size();
            for (int i = 0; i < size; i++) {
                WSInfo tempfileItem = (WSInfo)fiels.get(i);
                WSInfo fileItem = new WSInfo(tempfileItem.getFilename(),tempfileItem.getLastmodifieddate());
                System.out.println("item name" + fileItem.getFilename());
                jarlist.add(fileItem);
            }
            System.out.println("Initilize-end");
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     *
     * @param file
     */
    public void addFile(File file){
        if(! isFileExist(file.getName())){
            WSInfo wsInfo = new WSInfo(file.getName(),file.lastModified());
            jarlist.add(wsInfo);
            deplorer.addtowsToDeploye(file);//to inform that new web service is deployed
        }else{
            WSInfo tempWSInfo = getFileItem(file.getName());
            if(isModified(file ,tempWSInfo)){
                tempWSInfo.setLastmodifieddate(file.lastModified());
                WSInfo wsInfo = new WSInfo(tempWSInfo.getFilename(),tempWSInfo.getLastmodifieddate());
                deplorer.addtowstoUnDeploye(wsInfo);
                deplorer.addtowsToDeploye(file);

            }
        }
        String jarname= file.getName();
        currentjars.add(jarname);
    }

    public WSInfo getFileItem(String filename){
        int sise = jarlist.size();
        for (int i = 0; i < sise; i++) {
            WSInfo wsInfo = (WSInfo) jarlist.elementAt(i);
            if(wsInfo.getFilename().equals(filename)){
                return wsInfo;
            }
        }
        return null;
    }

    public boolean isModified(File file , WSInfo wsInfo ){
        if(wsInfo.getLastmodifieddate() != file.lastModified() ){
            return true;
        }
        return false;
    }

    public boolean isFileExist(String filename){
        if(getFileItem(filename)== null){
            return false;
        }else
            return true;
    }

    /**
     * this is to check , undeploye WS
     */
    public void checkForUndeploye(){
        Iterator iter = jarlist.listIterator();
        int size = currentjars.size();
        Vector tempvector = new Vector();
        tempvector.removeAllElements();
        String  filename ="";
        boolean exist = false;
        try{
            while (iter.hasNext()) {
                WSInfo fileitem = (WSInfo) iter.next();
                exist = false;
                for (int i = 0; i < size; i++) {
                    filename = (String) currentjars.elementAt(i);
                    if(filename.equals(fileitem.getFilename())){
                        exist = true;
                        break;
                    }
                }

                if(! exist){
                    tempvector.add(fileitem);
                    WSInfo wsInfo = new WSInfo(fileitem.getFilename(),fileitem.getLastmodifieddate()) ;
                    deplorer.addtowstoUnDeploye(wsInfo);//this is to be undeploye
                }

            }
        }catch(Exception e){
            //todo handle exc
        }

        for (int i = 0; i < tempvector.size(); i++) {
            WSInfo fileItem = (WSInfo) tempvector.elementAt(i);
            jarlist.removeElement(fileItem);
        }
        tempvector.removeAllElements();

        FileWriter fileWriter = new FileWriter();
        fileWriter.writeToFile();
        currentjars.removeAllElements();
    }

    /**
     *
     */
    public void update(){
        checkForUndeploye();
        deplorer.doUnDeploye();
        deplorer.doDeploye();

    }

}
package org.apache.axis.deployement.FileLoader.utill;

import org.apache.axis.deployement.Deplorer;
import org.apache.axis.deployement.FileLoader.FileReader;
import org.apache.axis.deployement.FileLoader.FileWriter;

import java.util.Iterator;
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
 *         12:13:11 PM
 *
 */
public class FileList {
    /**
     * This is to store all the jar files in a specified folder (WEB_INF)
     */
    private static Vector jarlist = new Vector() ;
    public Vector currentjars = new Vector();

    private Deplorer deplorer;



    public FileList() {
        deplorer = new Deplorer();
    }

    public static Vector getJarlist() {
        return jarlist;
    }

    /**
     * This method is used to initialize the vector
     */
    public void init(){
        try{
            jarlist.removeAllElements();
            FileReader fileReader = new FileReader();
            Vector fiels = fileReader.getDeployedJars();
            System.out.println("Initilize-Start");
            int size = fiels.size();
            for (int i = 0; i < size; i++) {
                WSInfo tempfileItem = (WSInfo)fiels.get(i);
                WSInfo fileItem = new WSInfo(tempfileItem.getFilename(),tempfileItem.getLastmodifieddate());
                System.out.println("item name" + fileItem.getFilename());
                jarlist.add(fileItem);
            }
            System.out.println("Initilize-end");
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     *
     * @param file
     */
    public void addFile(File file){
        if(! isFileExist(file.getName())){
            WSInfo wsInfo = new WSInfo(file.getName(),file.lastModified());
            jarlist.add(wsInfo);
            deplorer.addtowsToDeploye(file);//to inform that new web service is deployed
        }else{
            WSInfo tempWSInfo = getFileItem(file.getName());
            if(isModified(file ,tempWSInfo)){
                tempWSInfo.setLastmodifieddate(file.lastModified());
                WSInfo wsInfo = new WSInfo(tempWSInfo.getFilename(),tempWSInfo.getLastmodifieddate());
                deplorer.addtowstoUnDeploye(wsInfo);
                deplorer.addtowsToDeploye(file);

            }
        }
        String jarname= file.getName();
        currentjars.add(jarname);
    }

    public WSInfo getFileItem(String filename){
        int sise = jarlist.size();
        for (int i = 0; i < sise; i++) {
            WSInfo wsInfo = (WSInfo) jarlist.elementAt(i);
            if(wsInfo.getFilename().equals(filename)){
                return wsInfo;
            }
        }
        return null;
    }

    public boolean isModified(File file , WSInfo wsInfo ){
        if(wsInfo.getLastmodifieddate() != file.lastModified() ){
            return true;
        }
        return false;
    }

    public boolean isFileExist(String filename){
        if(getFileItem(filename)== null){
            return false;
        }else
            return true;
    }

    /**
     * this is to check , undeploye WS
     */
    public void checkForUndeploye(){
        Iterator iter = jarlist.listIterator();
        int size = currentjars.size();
        Vector tempvector = new Vector();
        tempvector.removeAllElements();
        String  filename ="";
        boolean exist = false;
        try{
            while (iter.hasNext()) {
                WSInfo fileitem = (WSInfo) iter.next();
                exist = false;
                for (int i = 0; i < size; i++) {
                    filename = (String) currentjars.elementAt(i);
                    if(filename.equals(fileitem.getFilename())){
                        exist = true;
                        break;
                    }
                }

                if(! exist){
                    tempvector.add(fileitem);
                    WSInfo wsInfo = new WSInfo(fileitem.getFilename(),fileitem.getLastmodifieddate()) ;
                    deplorer.addtowstoUnDeploye(wsInfo);//this is to be undeploye
                }

            }
        }catch(Exception e){
            //todo handle exc
        }

        for (int i = 0; i < tempvector.size(); i++) {
            WSInfo fileItem = (WSInfo) tempvector.elementAt(i);
            jarlist.removeElement(fileItem);
        }
        tempvector.removeAllElements();

        FileWriter fileWriter = new FileWriter();
        fileWriter.writeToFile();
        currentjars.removeAllElements();
    }

    /**
     *
     */
    public void update(){
        checkForUndeploye();
        deplorer.doUnDeploye();
        deplorer.doDeploye();

    }

}
