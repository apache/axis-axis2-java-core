package org.apache.axis2.databinding.schema;

import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.axis2.util.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


import javax.xml.namespace.QName;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.stream.StreamSource;
import java.util.Iterator;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
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

    private List namesList;
    private static int count = 0;



    private File rootDir;

    public JavaBeanWriter(File rootDir) throws IOException {
        if (rootDir ==null){
            this.rootDir = new File(".");
        }else if (!rootDir.isDirectory()){
            throw new IOException("Root location needs to be a directory!");
        } else{
            this.rootDir = rootDir;
        }

        namesList = new ArrayList();
    }

    /**
     *
     * @param complexType
     * @param typeMap
     * @param metainf
     * @throws SchemaCompilationException
     */
    public String write(XmlSchemaComplexType complexType, Map typeMap, BeanWriterMetaInfoHolder metainf) throws SchemaCompilationException{

        try {
            //determine the package for this type.
            QName qName = complexType.getQName();
            return process(qName, metainf, typeMap, false);

        }catch (SchemaCompilationException e) {
            throw e;
        }catch (Exception e) {
            throw new SchemaCompilationException(e);
        }


    }

    private String process(QName qName, BeanWriterMetaInfoHolder metainf, Map typeMap, boolean isElement) throws Exception {
        String packageName = URLProcessor.getNameSpaceFromURL(qName.getNamespaceURI());
        String originalName = qName.getLocalPart();
        String className = getNonConflictingName(this.namesList,originalName);

        ArrayList propertyNames = new ArrayList();

        if (!templateLoaded){
            loadTemplate();
        }

        //create the model
        Document model= XSLTUtils.getDocument();

        //make the XML
        Element rootElt = XSLTUtils.addChildElement(model,"bean",model);
        XSLTUtils.addAttribute(model,"name",className,rootElt);
        XSLTUtils.addAttribute(model,"originalName",originalName,rootElt);
        XSLTUtils.addAttribute(model,"package",packageName,rootElt);
        XSLTUtils.addAttribute(model,"nsuri",qName.getNamespaceURI(),rootElt);
        XSLTUtils.addAttribute(model,"nsprefix",qName.getPrefix(),rootElt);
        if (!isElement){
            XSLTUtils.addAttribute(model,"type","yes",rootElt);
        }

        if (metainf.isExtension()){
            XSLTUtils.addAttribute(model,"extension",metainf.getExtensionClassName(),rootElt);
        }
        // go in the loop and add the part elements
        QName[] qNames = null;
        if (metainf.isOrdered()){
            qNames = metainf.getOrderedQNameArray();
        }else{
            qNames = metainf.getQNameArray();
        }

        QName name;
        for (int i = 0; i < qNames.length; i++) {
            Element property = XSLTUtils.addChildElement(model,"property",rootElt);
            name = qNames[i];
            String xmlName = name.getLocalPart();
            System.out.println(xmlName);
            XSLTUtils.addAttribute(model,"name",xmlName,property);

            String javaName = "";
            if (JavaUtils.isJavaKeyword(xmlName)){
                javaName = JavaUtils.makeNonJavaKeyword(xmlName);
            }else{
                javaName = JavaUtils.xmlNameToJava(xmlName,false);
            }

            javaName = getNonConflictingName(propertyNames,javaName);
            XSLTUtils.addAttribute(model,"name",xmlName,property);
            XSLTUtils.addAttribute(model,"javaname",javaName,property);
            String javaClassNameForElement = metainf.getJavaClassNameForQName(name);
            String shortTypeName = "";
            if (metainf.getSchemaQNameForQName(name)!=null){
                shortTypeName = metainf.getSchemaQNameForQName(name).getLocalPart();
            }

            if (javaClassNameForElement==null){
                throw new SchemaCompilationException("Type missing!");
            }
            XSLTUtils.addAttribute(model,"type",javaClassNameForElement,property);
            if (typeMap.containsKey(metainf.getSchemaQNameForQName(name))){
                XSLTUtils.addAttribute(model,"ours","yes",property); //todo introduce a better name for this
            }

            if (metainf.getAttributeStatusForQName(name)){
                XSLTUtils.addAttribute(model,"attribute","yes",property);
            }

            XSLTUtils.addAttribute(model,"shorttypename",shortTypeName,property);

            if (metainf.getAnyStatusForQName(name)){
                XSLTUtils.addAttribute(model,"any","yes",property);
            }

            if (metainf.getAnyAttributeStatusForQName(name)){
                 XSLTUtils.addAttribute(model,"anyAtt","yes",property); 
            }
            if (metainf.getArrayStatusForQName(name)){
                XSLTUtils.addAttribute(model,"array","yes",property);
                XSLTUtils.addAttribute(
                        model,
                        "arrayBaseType",
                        javaClassNameForElement.substring(0,javaClassNameForElement.indexOf("[")),
                        property);


                long minOccurs = metainf.getMinOccurs(name);

                if (minOccurs >0){
                  XSLTUtils.addAttribute(model,"minOccurs",minOccurs +"",property);
                }

                long maxOccurs = metainf.getMaxOccurs(name);
                if (maxOccurs==Long.MAX_VALUE){
                     XSLTUtils.addAttribute(model,"unbound","yes",property);
                }else{
                    XSLTUtils.addAttribute(model,"maxOccurs",maxOccurs +"",property);
                }
            }
        }

        //create the file
        OutputStream out = createOutFile(packageName,className);
        //parse with the template and create the files
        parse(model,out);
        //return the fully qualified class name
        return packageName+"."+className;
    }

    /**
     *
     * @param listOfNames
     * @param nameBase
     * @return
     */
    private String getNonConflictingName(List listOfNames,String nameBase){
        String nameToReturn = nameBase;
        while (listOfNames.contains(nameToReturn)){
            nameToReturn = nameToReturn + count++;
        }

        listOfNames.add(nameToReturn);
        return nameToReturn;
    }

    /**
     *
     * @param element
     * @param typeMap
     * @param metainf
     * @return
     * @throws SchemaCompilationException
     */
    public String write(XmlSchemaElement element, Map typeMap, BeanWriterMetaInfoHolder metainf) throws SchemaCompilationException{

        try {
            //determine the package for this type.
            QName qName = element.getQName();
            return process(qName, metainf, typeMap, true);
        } catch (Exception e) {
            throw new SchemaCompilationException(e);
        }


    }




    /**
     *  A bit of code from the old code generator. We are better off using the template
     * engines and such stuff that's already there. But the class writers are hard to be
     * reused so some code needs to be repeated (atleast a bit)
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
        File outputFile = org.apache.axis2.util.FileWriter.createClassFile(this.rootDir,
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
