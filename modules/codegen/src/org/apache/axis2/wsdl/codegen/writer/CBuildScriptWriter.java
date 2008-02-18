package org.apache.axis2.wsdl.codegen.writer;

import java.io.File;
import java.io.FileOutputStream;

public class CBuildScriptWriter extends FileWriter {

       public CBuildScriptWriter(File outputFileLocation,String language) {
        this.outputFileLocation = outputFileLocation;
           this.language = language;
    }

    public void createOutFile(String packageName, String fileName) throws Exception {
        outputFile = org.apache.axis2.util.FileWriter.createClassFile(outputFileLocation,
                                                "",
                                                "build",
                                                ".sh");
        //set the existing flag
        fileExists = outputFile.exists();
        if (!fileExists) {
            this.stream = new FileOutputStream(outputFile);
        }
    }
}
