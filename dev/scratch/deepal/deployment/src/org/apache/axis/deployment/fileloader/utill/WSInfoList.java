package org.apache.axis.deployment.fileloader.utill;

import org.apache.axis.deployment.DeploymentEngine;
import org.apache.axis.deployment.DeployCons;

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
public class WSInfoList implements DeployCons{
    /**
     * This is to store all the jar files in a specified folder (WEB_INF)
     */
    private static Vector jarlist = new Vector() ;
    public Vector currentjars = new Vector();

    private DeploymentEngine deplorer;

    public WSInfoList() {
        deplorer = new DeploymentEngine();
    }

    public static Vector getJarlist() {
        return jarlist;
    }

    /**
     * This method is used to initialize the vector
     */
    public void init(){
        jarlist.removeAllElements();
    }

    public void addWSInfoItem(File file , int type){
        switch(type){
            case SERVICE: {
                if(! isFileExist(file.getName())){
                    WSInfo wsInfo = new WSInfo(file.getName(),file.lastModified(),SERVICE);
                    jarlist.add(wsInfo);
                    HDFileItem hdFileItem = new HDFileItem(file,SERVICE);
                    deplorer.addtowsToDeploy(hdFileItem);//to inform that new web service is deployed
                }else{
                    WSInfo tempWSInfo = getFileItem(file.getName());
                    if(isModified(file ,tempWSInfo)){
                        tempWSInfo.setLastmodifieddate(file.lastModified());
                        WSInfo wsInfo = new WSInfo(tempWSInfo.getFilename(),tempWSInfo.getLastmodifieddate(),SERVICE);
                        deplorer.addtowstoUnDeploy(wsInfo);
                        HDFileItem hdFileItem = new HDFileItem(file,SERVICE);
                        deplorer.addtowsToDeploy(hdFileItem);

                    }
                }
            }
            case  MODULE :{
                if(! isFileExist(file.getName())){
                    WSInfo wsInfo = new WSInfo(file.getName(),file.lastModified(),MODULE);
                    jarlist.add(wsInfo);
                    HDFileItem hdFileItem = new HDFileItem(file,MODULE);
                    deplorer.addtowsToDeploy(hdFileItem);//to inform that new web service is deployed
                }else{
                    WSInfo tempWSInfo = getFileItem(file.getName());
                    if(isModified(file ,tempWSInfo)){
                        tempWSInfo.setLastmodifieddate(file.lastModified());
                        WSInfo wsInfo = new WSInfo(tempWSInfo.getFilename(),tempWSInfo.getLastmodifieddate(),MODULE);
                        deplorer.addtowstoUnDeploy(wsInfo);
                        HDFileItem hdFileItem = new HDFileItem(file,MODULE);
                        deplorer.addtowsToDeploy(hdFileItem);

                    }
                }
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
                    deplorer.addtowstoUnDeploy(wsInfo);//this is to be undeploye
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
