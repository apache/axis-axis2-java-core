package org.apache.axis2.databinding.schema;

import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.axis2.util.FileWriter;
import org.apache.axis2.util.URLProcessor;
import org.apache.axis2.util.XSLTUtils;
import org.apache.axis2.util.XSLTTemplateProcessor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


import javax.xml.namespace.QName;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.Map;
import java.util.Iterator;
import java.io.*;

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
 */

public class JavaBeanWriter {

    private static final String JAVA_BEAN_TEMPLATE = "/org/apache/axis2/databinding/schema/template/BeanTemplate.xsl";
    private boolean templateLoaded = false;
    private Templates templateCache;


    private File rootDir;

    public JavaBeanWriter(File rootDir) throws IOException {
        if (rootDir ==null){
            this.rootDir = new File(".");
        }else if (!rootDir.isDirectory()){
            throw new IOException("Root location needs to be a directory!");
        } else{
            this.rootDir = rootDir;
        }

    }

    public void write(XmlSchemaComplexType complexType, Map typeMap,Map currentTypeMap) throws SchemaCompilationException{

        try {
            //determine the package for this type.
            QName qName = complexType.getQName();
            String packageName = URLProcessor.getNameSpaceFromURL(qName.getNamespaceURI());
            String className = qName.getLocalPart();

            if (!templateLoaded){
                loadTemplate();
            }

            //create the model
            Document model= XSLTUtils.getDocument();

            //make the XML
            Element rootElt = XSLTUtils.addChildElement(model,"bean",model);
            XSLTUtils.addAttribute(model,"name",className,rootElt);
            XSLTUtils.addAttribute(model,"package",packageName,rootElt);
            // go in the loop and add the part elements
            if (currentTypeMap != null && !currentTypeMap.isEmpty()){

                Iterator it = currentTypeMap.keySet().iterator();
                QName name;
                while (it.hasNext()) {
                    Element property = XSLTUtils.addChildElement(model,"property",rootElt);
                    name = (QName)it.next();
                    XSLTUtils.addAttribute(model,"name",name.getLocalPart(),property);
                    XSLTUtils.addAttribute(model,"type",currentTypeMap.get(name).toString(),property);

                }
            }
            //create the file
            OutputStream out = createOutFile(packageName,className);
            //parse with the template and create the files
            parse(model,out);
        } catch (Exception e) {
            throw new SchemaCompilationException(e);
        }


    }



    /** A bit of code from the code generator. We are better off using the template
     * engines and such stuff that's already there. But the class writers are hard to be
     * reused so some code needs to be repeated
     *
     */
    private  void loadTemplate() throws SchemaCompilationException {

        //first get the language specific property map
        Class clazz = this.getClass();
        InputStream xslStream;
        String templateName = JAVA_BEAN_TEMPLATE;
        if (templateName!=null){
            try {
                xslStream = clazz.getResourceAsStream(templateName);
                templateCache = TransformerFactory.newInstance().newTemplates(new StreamSource(xslStream));
                templateLoaded = true;
            } catch (TransformerConfigurationException e) {
                throw new SchemaCompilationException("Error loading the template",e);
            }
        }else{
            throw new SchemaCompilationException("template for this writer is not found");
        }
    }


     /**
     * Creates the output file
     *
     * @param packageName
     * @param fileName
     * @throws Exception
     */
    private OutputStream createOutFile(String packageName, String fileName) throws Exception {
        File outputFile = FileWriter.createClassFile(this.rootDir,
                packageName,
                fileName,
                ".java");
           return new FileOutputStream(outputFile);

    }

     /**
     * Writes the output file
     *
     * @param documentStream
     * @throws Exception
     */
    private void parse(Document doc,OutputStream outStream) throws Exception {

            XSLTTemplateProcessor.parse(outStream,
                    doc,
                    this.templateCache.newTransformer());
            outStream.flush();
            outStream.close();

    }
}
