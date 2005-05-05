package org.apache.axis.wsdl.util;

import org.apache.axis.wsdl.tojava.xslt.XSLTConstants;

import java.io.File;
import java.io.IOException;

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
public class FileWriter {

    public static File createClassFile(File rootLocation,String packageName,String javaFileName,int fileType) throws IOException,Exception{

        File returnFile = null;
        File root = rootLocation;

        if (packageName!=null){
            String directoryNames[] = packageName.split("\\.");
            File tempFile = null;
            int length = directoryNames.length;
            for (int i = 0; i < length; i++) {
                tempFile = new File(root,directoryNames[i]);
                root = tempFile;
                if (!tempFile.exists()){
                    tempFile.mkdir();
                }

            }
        }


        String extension = getExtension(fileType);

        if (!javaFileName.endsWith(extension)){
            javaFileName = javaFileName + extension;
        }

        returnFile = new File(root,javaFileName);

        if (!returnFile.exists()){
            returnFile.createNewFile();
        }



        return returnFile;
    }

    private static String getExtension(int fileType) {
        String extension = "";
        switch (fileType){
            case XSLTConstants.LanguageTypes.JAVA: extension=".java";break;
            case XSLTConstants.LanguageTypes.C_SHARP: extension=".cs";break;
            case XSLTConstants.LanguageTypes.C_PLUS_PLUS: extension=".cpp";break;
            case XSLTConstants.LanguageTypes.VB_DOT_NET: extension=".vb";break;
        }
        return extension;
    }

}
