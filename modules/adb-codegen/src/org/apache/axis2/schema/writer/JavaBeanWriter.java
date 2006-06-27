package org.apache.axis2.schema.writer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

import org.apache.axis2.schema.BeanWriterMetaInfoHolder;
import org.apache.axis2.schema.CompilerOptions;
import org.apache.axis2.schema.SchemaCompilationException;
import org.apache.axis2.schema.SchemaCompiler;
import org.apache.axis2.schema.i18n.SchemaCompilerMessages;
import org.apache.axis2.schema.typemap.JavaTypeMap;
import org.apache.axis2.schema.util.PrimitiveTypeFinder;
import org.apache.axis2.schema.util.SchemaPropertyLoader;
import org.apache.axis2.util.JavaUtils;
import org.apache.axis2.util.PrettyPrinter;
import org.apache.axis2.util.URLProcessor;
import org.apache.axis2.util.XSLTTemplateProcessor;
import org.apache.axis2.util.XSLTUtils;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaSimpleType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

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

/**
 * Java Bean writer for the schema compiler.
 */
public class JavaBeanWriter implements BeanWriter {

    public static final String WRAPPED_DATABINDING_CLASS_NAME = "WrappedDatabinder";

    private String javaBeanTemplateName = null;

    private boolean templateLoaded = false;

    private Templates templateCache;

    private List namesList;

    private static int count = 0;

    private boolean wrapClasses = false;

    private boolean writeClasses = false;

    private String packageName = null;

    private File rootDir;

    private Document globalWrappedDocument;

    private Map modelMap = new HashMap();

    private static final String DEFAULT_PACKAGE = "adb";

    private Map baseTypeMap = new JavaTypeMap().getTypeMap();

    private Map ns2packageNameMap = new HashMap();

    private boolean isHelperMode = false;

    /**
     * Default constructor
     */
    public JavaBeanWriter() {
    }

    /**
     * This returns a map of Qnames vs DOMDocument models. One can use this
     * method to obtain the raw DOMmodels used to write the classes. This has no
     * meaning when the classes are supposed to be wrapped so the
     * 
     * @see org.apache.axis2.schema.writer.BeanWriter#getModelMap()
     * @return Returns Map.
     * @throws SchemaCompilationException
     */
    public Map getModelMap() {
        return modelMap;
    }

    public void init(CompilerOptions options) throws SchemaCompilationException {
        try {
            initWithFile(options.getOutputLocation());
            packageName = options.getPackageName();
            writeClasses = options.isWriteOutput();
            if (!writeClasses) {
                wrapClasses = false;
            } else {
                wrapClasses = options.isWrapClasses();
            }

            // if the wrap mode is set then create a global document to keep the
            // wrapped
            // element models
            if (options.isWrapClasses()) {
                globalWrappedDocument = XSLTUtils.getDocument();
                Element rootElement = XSLTUtils.getElement(
                        globalWrappedDocument, "beans");
                globalWrappedDocument.appendChild(rootElement);
                XSLTUtils.addAttribute(globalWrappedDocument, "name",
                        WRAPPED_DATABINDING_CLASS_NAME, rootElement);
                String tempPackageName;

                if (packageName != null && packageName.endsWith(".")) {
                    tempPackageName = this.packageName.substring(0,
                            this.packageName.lastIndexOf("."));
                } else {
                    tempPackageName = DEFAULT_PACKAGE;
                }

                XSLTUtils.addAttribute(globalWrappedDocument, "package",
                        tempPackageName, rootElement);
            }

            // add the ns mappings
            this.ns2packageNameMap = options.getNs2PackageMap();

            this.isHelperMode = options.isHelperMode();

        } catch (IOException e) {
            throw new SchemaCompilationException(e);
        } catch (ParserConfigurationException e) {
            throw new SchemaCompilationException(e); // todo need to put
                                                        // correct error
                                                        // messages
        }
    }

    /**
     * @param element
     * @param typeMap
     * @param metainf
     * @return Returns String.
     * @throws SchemaCompilationException
     */
    public String write(XmlSchemaElement element, Map typeMap,
            BeanWriterMetaInfoHolder metainf) throws SchemaCompilationException {

        try {
            QName qName = element.getQName();

            return process(qName, metainf, typeMap, true, null);
        } catch (Exception e) {
            throw new SchemaCompilationException(e);
        }

    }

    /**
     * @param complexType
     * @param typeMap
     * @param metainf
     * @param fullyQualifiedClassName
     *            the name returned by makeFullyQualifiedClassName() or null if
     *            it wasn't called
     * @throws org.apache.axis2.schema.SchemaCompilationException
     * 
     * @see BeanWriter#write(org.apache.ws.commons.schema.XmlSchemaComplexType,
     *      java.util.Map, org.apache.axis2.schema.BeanWriterMetaInfoHolder)
     */
    public String write(XmlSchemaComplexType complexType, Map typeMap,
            BeanWriterMetaInfoHolder metainf, String fullyQualifiedClassName)
            throws SchemaCompilationException {

        try {
            // determine the package for this type.
            QName qName = complexType.getQName();
            return process(qName, metainf, typeMap, false,
                    fullyQualifiedClassName);

        } catch (SchemaCompilationException e) {
            throw e;
        } catch (Exception e) {
            throw new SchemaCompilationException(e);
        }

    }

    /**
     * @throws Exception
     * @see BeanWriter#writeBatch()
     */
    public void writeBatch() throws SchemaCompilationException {
        try {
            if (wrapClasses) {
                String tempPackage;
                if (packageName == null) {
                    tempPackage = DEFAULT_PACKAGE;
                } else {
                    tempPackage = packageName;
                }
                File out = createOutFile(tempPackage,
                        WRAPPED_DATABINDING_CLASS_NAME);
                // parse with the template and create the files

                parse(globalWrappedDocument, out);
            }
        } catch (Exception e) {
            throw new SchemaCompilationException(e);
        }
    }

    /**
     * @param simpleType
     * @param typeMap
     * @param metainf
     * @return Returns String.
     * @throws SchemaCompilationException
     * @see BeanWriter#write(org.apache.ws.commons.schema.XmlSchemaSimpleType,
     *      java.util.Map, org.apache.axis2.schema.BeanWriterMetaInfoHolder)
     */
    public String write(XmlSchemaSimpleType simpleType, Map typeMap,
            BeanWriterMetaInfoHolder metainf) throws SchemaCompilationException {
        throw new SchemaCompilationException(SchemaCompilerMessages
                .getMessage("schema.notimplementedxception"));
    }

    /**
     * @param rootDir
     * @throws IOException
     * @see BeanWriter#init(java.io.File)
     */
    private void initWithFile(File rootDir) throws IOException {
        if (rootDir == null) {
            this.rootDir = new File(".");
        } else if (!rootDir.isDirectory()) {
            throw new IOException(SchemaCompilerMessages
                    .getMessage("schema.rootnotfolderexception"));
        } else {
            this.rootDir = rootDir;
        }

        namesList = new ArrayList();
        javaBeanTemplateName = SchemaPropertyLoader.getBeanTemplate();
    }

    /**
     * Make the fully qualified class name for an element or named type
     * 
     * @param qName
     *            the qualified Name for this element or type in the schema
     * @return the appropriate fully qualified class name to use in generated
     *         code
     */
    public String makeFullyQualifiedClassName(QName qName) {

        String namespaceURI = qName.getNamespaceURI();
        String basePackageName;

        if (ns2packageNameMap.containsKey(namespaceURI)) {
            basePackageName = (String) ns2packageNameMap.get(namespaceURI);
        } else {
            basePackageName = URLProcessor.makePackageName(namespaceURI);
        }

        String packageName = this.packageName == null ? basePackageName
                : this.packageName + basePackageName;

        String originalName = qName.getLocalPart();
        String className = makeUniqueJavaClassName(this.namesList, originalName);

        String packagePrefix = null;

        String fullyqualifiedClassName;

        if (wrapClasses)
            packagePrefix = (this.packageName == null ? DEFAULT_PACKAGE + "."
                    : this.packageName)
                    + WRAPPED_DATABINDING_CLASS_NAME;
        else if (writeClasses)
            packagePrefix = packageName;
        if (packagePrefix != null)
            fullyqualifiedClassName = packagePrefix
                    + (packagePrefix.endsWith(".") ? "" : ".") + className;
        else
            fullyqualifiedClassName = className;
        // return the fully qualified class name
        return fullyqualifiedClassName;
    }

    /**
     * A util method that holds common code for the complete schema that the
     * generated XML complies to look under other/beanGenerationSchema.xsd
     * 
     * @param qName
     * @param metainf
     * @param typeMap
     * @param isElement
     * @param fullyQualifiedClassName
     *            the name returned by makeFullyQualifiedClassName() or null if
     *            it wasn't called
     * @return Returns String.
     * @throws Exception
     */
    private String process(QName qName, BeanWriterMetaInfoHolder metainf,
            Map typeMap, boolean isElement, String fullyQualifiedClassName)
            throws Exception {

        if (fullyQualifiedClassName == null)
            fullyQualifiedClassName = makeFullyQualifiedClassName(qName);
        String className = fullyQualifiedClassName
                .substring(1 + fullyQualifiedClassName.lastIndexOf('.'));
        String basePackageName;
        if (fullyQualifiedClassName.lastIndexOf('.') == -1) {// no 'dots' so
                                                                // the package
                                                                // is not there
            basePackageName = "";
        } else {
            basePackageName = fullyQualifiedClassName.substring(0,
                    fullyQualifiedClassName.lastIndexOf('.'));
        }

        String originalName = qName.getLocalPart();
        ArrayList propertyNames = new ArrayList();

        if (!templateLoaded) {
            loadTemplate();
        }

        // if wrapped then do not write the classes now but add the models to a
        // global document. However in order to write the
        // global class that is generated, one needs to call the writeBatch()
        // method
        if (wrapClasses) {
            globalWrappedDocument.getDocumentElement().appendChild(
                    getBeanElement(globalWrappedDocument, className,
                            originalName, basePackageName, qName, isElement,
                            metainf, propertyNames, typeMap));

        } else {
            // create the model
            Document model = XSLTUtils.getDocument();
            // make the XML
            model.appendChild(getBeanElement(model, className, originalName,
                    basePackageName, qName, isElement, metainf, propertyNames,
                    typeMap));

            if (writeClasses) {
                // create the file
                File out = createOutFile(basePackageName, className);
                // parse with the template and create the files
   
                if (isHelperMode) {

                    XSLTUtils.addAttribute(model, "helperMode", "yes", model.getDocumentElement());
                    
                    // Generate bean classes
                    parse(model, out);

                    // Generating the helper classes
                    out = createOutFile(basePackageName, className + "Helper");
                    XSLTUtils.addAttribute(model, "helper", "yes", model
                            .getDocumentElement());
                    parse(model, out);

                } else {
                    parse(model, out);
                }
            }

            // add the model to the model map
            modelMap.put(new QName(qName.getNamespaceURI(), className), model);

        }

        // return the fully qualified class name
        return fullyQualifiedClassName;

    }

    /**
     * @param model
     * @param className
     * @param originalName
     * @param packageName
     * @param qName
     * @param isElement
     * @param metainf
     * @param propertyNames
     * @param typeMap
     * @return Returns Element.
     * @throws SchemaCompilationException
     */
    private Element getBeanElement(Document model, String className,
            String originalName, String packageName, QName qName,
            boolean isElement, BeanWriterMetaInfoHolder metainf,
            ArrayList propertyNames, Map typeMap)
            throws SchemaCompilationException {

        Element rootElt = XSLTUtils.getElement(model, "bean");
        XSLTUtils.addAttribute(model, "name", className, rootElt);
        XSLTUtils.addAttribute(model, "originalName", originalName, rootElt);
        XSLTUtils.addAttribute(model, "package", packageName, rootElt);
        XSLTUtils
                .addAttribute(model, "nsuri", qName.getNamespaceURI(), rootElt);
        XSLTUtils.addAttribute(model, "nsprefix", getPrefixForURI(qName
                .getNamespaceURI(), qName.getPrefix()), rootElt);

        if (!wrapClasses) {
            XSLTUtils.addAttribute(model, "unwrapped", "yes", rootElt);
        }

        if (!writeClasses) {
            XSLTUtils.addAttribute(model, "skip-write", "yes", rootElt);
        }

        if (!isElement) {
            XSLTUtils.addAttribute(model, "type", "yes", rootElt);
        }

        if (metainf.isAnonymous()) {
            XSLTUtils.addAttribute(model, "anon", "yes", rootElt);
        }

        if (metainf.isExtension()) {
            XSLTUtils.addAttribute(model, "extension", metainf
                    .getExtensionClassName(), rootElt);
        }

        if (metainf.isChoice()) {
            XSLTUtils.addAttribute(model, "choice", "yes", rootElt);
        }

        if (metainf.isOrdered()) {
            XSLTUtils.addAttribute(model, "ordered", "yes", rootElt);
        }

        if (isElement && metainf.isNillable(qName)) {
            XSLTUtils.addAttribute(model, "nillable", "yes", rootElt);
        }

        // populate all the information
        populateInfo(metainf, model, rootElt, propertyNames, typeMap, false);

        return rootElt;
    }

    /**
     * 
     * @param metainf
     * @param model
     * @param rootElt
     * @param propertyNames
     * @param typeMap
     * @throws SchemaCompilationException
     */
    private void populateInfo(BeanWriterMetaInfoHolder metainf, Document model,
            Element rootElt, ArrayList propertyNames, Map typeMap,
            boolean isInherited) throws SchemaCompilationException {
        if (metainf.getParent() != null) {
            populateInfo(metainf.getParent(), model, rootElt, propertyNames,
                    typeMap, true);
        }
        addPropertyEntries(metainf, model, rootElt, propertyNames, typeMap,
                isInherited);

    }

    /**
     * 
     * @param metainf
     * @param model
     * @param rootElt
     * @param propertyNames
     * @param typeMap
     * @throws SchemaCompilationException
     */
    private void addPropertyEntries(BeanWriterMetaInfoHolder metainf,
            Document model, Element rootElt, ArrayList propertyNames,
            Map typeMap, boolean isInherited) throws SchemaCompilationException {
        // go in the loop and add the part elements
        QName[] qNames;
        if (metainf.isOrdered()) {
            qNames = metainf.getOrderedQNameArray();
        } else {
            qNames = metainf.getQNameArray();
        }

        QName name;
        for (int i = 0; i < qNames.length; i++) {
            Element property = XSLTUtils.addChildElement(model, "property",
                    rootElt);
            name = qNames[i];
            String xmlName = name.getLocalPart();
            XSLTUtils.addAttribute(model, "name", xmlName, property);
            XSLTUtils.addAttribute(model, "nsuri", name.getNamespaceURI(),
                    property);
            String javaName = makeUniqueJavaClassName(propertyNames, xmlName);
            XSLTUtils.addAttribute(model, "javaname", javaName, property);

            String javaClassNameForElement = metainf.getClassNameForQName(name);

            if (javaClassNameForElement == null) {
                throw new SchemaCompilationException(SchemaCompilerMessages
                        .getMessage("schema.typeMissing"));
            }

            XSLTUtils.addAttribute(model, "type", javaClassNameForElement,
                    property);

            if (PrimitiveTypeFinder.isPrimitive(javaClassNameForElement)) {
                XSLTUtils.addAttribute(model, "primitive", "yes", property);
            }
            // add an attribute that says the type is default
            if (isDefault(javaClassNameForElement)) {
                XSLTUtils.addAttribute(model, "default", "yes", property);
            }

            if (typeMap.containsKey(metainf.getSchemaQNameForQName(name))) {
                XSLTUtils.addAttribute(model, "ours", "yes", property);
            }

            if (metainf.getAttributeStatusForQName(name)) {
                XSLTUtils.addAttribute(model, "attribute", "yes", property);
            }

            if (metainf.isNillable(name)) {
                XSLTUtils.addAttribute(model, "nillable", "yes", property);
            }

            String shortTypeName;
            if (metainf.getSchemaQNameForQName(name) != null) {
                // see whether the QName is a basetype
                if (baseTypeMap.containsKey(metainf
                        .getSchemaQNameForQName(name))) {
                    shortTypeName = metainf.getSchemaQNameForQName(name)
                            .getLocalPart();
                } else {
                    shortTypeName = getShortTypeName(javaClassNameForElement);
                }
            } else {
                shortTypeName = getShortTypeName(javaClassNameForElement);
            }
            XSLTUtils.addAttribute(model, "shorttypename", shortTypeName,
                    property);

            if (isInherited) {
                XSLTUtils.addAttribute(model, "inherited", "yes", property);
            }

            if (metainf.getAnyStatusForQName(name)) {
                XSLTUtils.addAttribute(model, "any", "yes", property);
            }

            if (metainf.getBinaryStatusForQName(name)) {
                XSLTUtils.addAttribute(model, "binary", "yes", property);
            }
            // put the min occurs count irrespective of whether it's an array or
            // not
            long minOccurs = metainf.getMinOccurs(name);
            XSLTUtils
                    .addAttribute(model, "minOccurs", minOccurs + "", property);

            if (metainf.getArrayStatusForQName(name)) {

                XSLTUtils.addAttribute(model, "array", "yes", property);
                XSLTUtils
                        .addAttribute(model, "arrayBaseType",
                                javaClassNameForElement.substring(0,
                                        javaClassNameForElement.indexOf("[")),
                                property);

                long maxOccurs = metainf.getMaxOccurs(name);
                if (maxOccurs == Long.MAX_VALUE) {
                    XSLTUtils.addAttribute(model, "unbound", "yes", property);
                } else {
                    XSLTUtils.addAttribute(model, "maxOccurs", maxOccurs + "",
                            property);
                }
            }
        }
    }

    /**
     * Test whether the given class name matches the default
     * 
     * @param javaClassNameForElement
     * @return
     */
    private boolean isDefault(String javaClassNameForElement) {
        return SchemaCompiler.DEFAULT_CLASS_NAME
                .equals(javaClassNameForElement)
                || SchemaCompiler.DEFAULT_CLASS_ARRAY_NAME
                        .equals(javaClassNameForElement);
    }

    /**
     * Given the xml name, make a unique class name taking into account that
     * some file systems are case sensitive and some are not. -Consider the
     * Jax-WS spec for this
     * 
     * @param listOfNames
     * @param xmlName
     * @return Returns String.
     */
    private String makeUniqueJavaClassName(List listOfNames, String xmlName) {
        String javaName;
        if (JavaUtils.isJavaKeyword(xmlName)) {
            javaName = JavaUtils.makeNonJavaKeyword(xmlName);
        } else {
            javaName = JavaUtils.capitalizeFirstChar(JavaUtils
                    .xmlNameToJava(xmlName));
        }

        while (listOfNames.contains(javaName.toLowerCase())) {
            javaName = javaName + count++;
        }

        listOfNames.add(javaName.toLowerCase());
        return javaName;
    }

    /**
     * A bit of code from the old code generator. We are better off using the
     * template engines and such stuff that's already there. But the class
     * writers are hard to be reused so some code needs to be repeated (atleast
     * a bit)
     */
    private void loadTemplate() throws SchemaCompilationException {

        // first get the language specific property map
        Class clazz = this.getClass();
        InputStream xslStream;
        String templateName = javaBeanTemplateName;
        if (templateName != null) {
            try {
                xslStream = clazz.getResourceAsStream(templateName);
                templateCache = TransformerFactory.newInstance().newTemplates(
                        new StreamSource(xslStream));
                templateLoaded = true;
            } catch (TransformerConfigurationException e) {
                throw new SchemaCompilationException(SchemaCompilerMessages
                        .getMessage("schema.templateLoadException"), e);
            }
        } else {
            throw new SchemaCompilationException(SchemaCompilerMessages
                    .getMessage("schema.templateNotFoundException"));
        }
    }

    /**
     * Creates the output file
     * 
     * @param packageName
     * @param fileName
     * @throws Exception
     */
    private File createOutFile(String packageName, String fileName)
            throws Exception {
        return org.apache.axis2.util.FileWriter.createClassFile(this.rootDir,
                packageName, fileName, ".java");
    }

    /**
     * Writes the output file
     * 
     * @param doc
     * @param outputFile
     * @throws Exception
     */
    private void parse(Document doc, File outputFile) throws Exception {
        OutputStream outStream = new FileOutputStream(outputFile);
        XSLTTemplateProcessor.parse(outStream, doc, this.templateCache
                .newTransformer());
        outStream.flush();
        outStream.close();

        PrettyPrinter.prettify(outputFile);
    }

    /**
     * Get a prefix for a namespace URI. This method will ALWAYS return a valid
     * prefix - if the given URI is already mapped in this serialization, we
     * return the previous prefix. If it is not mapped, we will add a new
     * mapping and return a generated prefix of the form "ns<num>".
     * 
     * @param uri
     *            is the namespace uri
     * @return Returns prefix.
     */
    public String getPrefixForURI(String uri) {
        return getPrefixForURI(uri, null);
    }

    /**
     * Last used index suffix for "ns"
     */
    private int lastPrefixIndex = 1;

    /**
     * Map of namespaces URI to prefix(es)
     */
    HashMap mapURItoPrefix = new HashMap();

    HashMap mapPrefixtoURI = new HashMap();

    /**
     * Get a prefix for the given namespace URI. If one has already been defined
     * in this serialization, use that. Otherwise, map the passed default prefix
     * to the URI, and return that. If a null default prefix is passed, use one
     * of the form "ns<num>"
     */
    public String getPrefixForURI(String uri, String defaultPrefix) {
        if ((uri == null) || (uri.length() == 0))
            return null;
        String prefix = (String) mapURItoPrefix.get(uri);
        if (prefix == null) {
            if (defaultPrefix == null || defaultPrefix.length() == 0) {
                prefix = "ns" + lastPrefixIndex++;
                while (mapPrefixtoURI.get(prefix) != null) {
                    prefix = "ns" + lastPrefixIndex++;
                }
            } else {
                prefix = defaultPrefix;
            }
            mapPrefixtoURI.put(prefix, uri);
            mapURItoPrefix.put(uri, prefix);
        }
        return prefix;
    }

    private String getShortTypeName(String typeClassName) {
        if (typeClassName.endsWith("[]")) {
            typeClassName = typeClassName.substring(0, typeClassName
                    .lastIndexOf("["));
        }

        return typeClassName.substring(typeClassName.lastIndexOf(".") + 1,
                typeClassName.length());

    }
}
