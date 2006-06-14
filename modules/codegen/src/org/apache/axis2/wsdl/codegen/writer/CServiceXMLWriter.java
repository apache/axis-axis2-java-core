package org.apache.axis2.wsdl.codegen.writer;

import org.apache.axis2.util.FileWriter;

import java.io.File;
import java.io.FileOutputStream;

public class CServiceXMLWriter extends ClassWriter {


    public CServiceXMLWriter(String outputFileLocation) {
        this.outputFileLocation = new File(outputFileLocation);
    }

    public CServiceXMLWriter(File outputFileLocation, String language) {
        this.outputFileLocation = outputFileLocation;
        this.language = language;
    }


    public void createOutFile(String packageName, String fileName) throws Exception {
        outputFile = FileWriter.createClassFile(outputFileLocation,
                "",
                "services",
                ".xml");
        //set the existing flag
        fileExists = outputFile.exists();
        if (!fileExists) {
            this.stream = new FileOutputStream(outputFile);
        }
    }
}
