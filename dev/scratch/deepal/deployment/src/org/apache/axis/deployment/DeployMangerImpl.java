package org.apache.axis.deployment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;


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
 *         Nov 2, 2004
 *         5:02:37 PM
 *
 */
public class DeployMangerImpl implements DeployManger , DeployCons{

    /**
     *  This method can used to Remotely deploy web servises eithre asa j2ee like or
     * .jws , if it is j2ee like wwb service this directly copy inputsream to correct
     * directory , then hot deployemnt module will identify that new ws has deployed
     * then it will do the real Deployment
     * @param wsin  InputStream
     * @param fileName name of the .aar file to be upload
     */
    public void deployWS(InputStream wsin, String fileName) {
        String resolved_filename =resolveFileName(fileName);
        boolean isJWS = isJWS(resolved_filename);
        if(isJWS){
            //todo do wt wvere has to do when .jws is deployed
        }  else {
            /**
             * I have assumed that user only upload or try to remote deploy .aar file
             */
            //todo this only fine for .aar file bt I have to check taht
            String newFileNam = SERVICE_PATH + resolved_filename;
            ZipInputStream zin = new ZipInputStream(wsin);
            byte b[] = new byte[1024];
            ZipEntry inentry,outentry ;
            try {
                ZipOutputStream zout= new ZipOutputStream(new FileOutputStream(newFileNam));
                int len=0;
                while ( ( inentry = zin.getNextEntry()) != null ) {
                    outentry = new ZipEntry(inentry.getName());
                    zout.putNextEntry(outentry);
                    try{
                        while((len=zin.read(b)) != -1) {
                            zout.write(b,0,len);
                        }
                    }catch(Exception ex){
                        ex.printStackTrace();
                    }
                }
                zin.closeEntry();
                zin.close();
                wsin.close();
                zout.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     *
     * @param wsName
     */
    public void undeployeWS(String wsName) {
        String fileName = SERVICE_PATH + wsName;

        try{
            File newfile = new File(fileName);
            newfile.delete();
        }   catch(Exception fnot){
            fnot.printStackTrace();
        }
    }

    public void listAllWS() {

    }

    /**
     * This method is used to resolve the file name when user given file name as a url
     * http://anv/ws.aar
     * @param fileName
     * @return  short file name
     */
    private String resolveFileName(String fileName){
        fileName = fileName.trim();
        int namelen = fileName.length();
        char fws = '/';   // forward seperator
        char bws = '\\'; // backword seperator
        int index = 0 ; // index of the seperator

        /**
         * following if condition is needed bcos of the os
         */
        index = fileName.lastIndexOf(fws, namelen -1);
        if(index == -1){
            index = fileName.lastIndexOf(bws, namelen -1);
        }
        fileName = fileName.substring(index+1,namelen);
// fileName.
        return fileName;
    }

    private boolean isJWS(String fileName){
        //JWS_EXTENSION
        if(fileName.indexOf(JWS_EXTENSION)>0){
            // if it is a jws file this will return true
            return true;
        }   else
            return false;
    }
}

