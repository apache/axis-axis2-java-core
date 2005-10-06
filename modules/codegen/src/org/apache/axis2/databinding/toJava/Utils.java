/*
 * Copyright 2001-2004 The Apache Software Foundation.
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
package org.apache.axis2.databinding.toJava;

//import org.apache.axis.Constants;
//import org.apache.axis.components.logger.LogFactory;
//import org.apache.axis.constants.Style;
//import org.apache.axis.constants.Use;
//import org.apache.axis.utils.JavaUtils;
//import org.apache.axis.utils.Messages;
//import org.apache.axis.wsdl.symbolTable.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.axis2.databinding.symbolTable.*;
import org.apache.axis2.databinding.Constants;
import org.apache.axis2.databinding.utils.support.BooleanHolder;
import org.apache.axis2.databinding.utils.JavaUtils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.wsdl.BindingOperation;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.UnknownExtensibilityElement;
import javax.wsdl.extensions.soap.SOAPOperation;
import javax.xml.namespace.QName;
//import javax.xml.rpc.holders.BooleanHolder;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * Class Utils
 * 
 * @version %I%, %G%
 */
public class Utils extends org.apache.axis2.databinding.symbolTable.Utils {

    /** Field log */
    protected static Log log = LogFactory.getLog(Utils.class.getName());
    
    /**
     * @see #holder(Parameter, Emitter)
     */
    public static String holder(TypeEntry type, Emitter emitter) {
        Parameter arg = new Parameter();
        // For other fields the default values will do.
        arg.setType(type);
        return holder(arg, emitter);
    }    
    /**
     * Given a type, return the Java mapping of that type's holder.
     * 
     * @param p          parameter whose holder class name we want to obtain.
     * @param emitter    the only {@link Emitter} object embodying the running
     *                   instance of WSDL2Java.
     * @return           the name of the holder class for <tt>p</tt>.
     */
    public static String holder(Parameter p, Emitter emitter) {
        String mimeType = (p.getMIMEInfo() == null)
                ? null
                : p.getMIMEInfo().getType();
        String mimeDimensions = (mimeType == null)
                ? ""
                : p.getMIMEInfo().getDimensions();

        // Add the holders that JAX-RPC forgot about - the MIME type holders.
        if (mimeType != null) {
            if (mimeType.equals("image/gif") || mimeType.equals("image/jpeg")) {
                return "org.apache.axis.holders.ImageHolder" + mimeDimensions;
            } else if (mimeType.equals("text/plain")) {
                return "javax.xml.rpc.holders.StringHolder" + mimeDimensions;
            } else if (mimeType.startsWith("multipart/")) {
                return "org.apache.axis.holders.MimeMultipartHolder"
                        + mimeDimensions;
            } else if (mimeType.startsWith("application/octetstream")
                    || mimeType.startsWith("application/octet-stream")) {
                return "org.apache.axis.holders.OctetStreamHolder"
                        + mimeDimensions;
            } else if (mimeType.equals("text/xml")
                    || mimeType.equals("application/xml")) {
                return "org.apache.axis.holders.SourceHolder" + mimeDimensions;
            } else {
                return "org.apache.axis.holders.DataHandlerHolder"
                        + mimeDimensions;
            }
        }

        TypeEntry type = p.getType();
        String typeValue = type.getName();
        // For base types that are nillable and are mapped to primitives,
        // need to switch to the corresponding wrapper types.
        if (p.isOmittable()
            &&  (type instanceof BaseType
                 ||  type instanceof DefinedElement
                     &&  type.getRefType() instanceof BaseType)) {
            String wrapperTypeValue = (String) TYPES.get(typeValue);
            typeValue = wrapperTypeValue == null  ?  typeValue
                                                  :  wrapperTypeValue;
        }

        // byte[] has a reserved holders
        if (typeValue.equals("byte[]") && type.isBaseType()) {
            return "javax.xml.rpc.holders.ByteArrayHolder";
        }

        // Anything else with [] gets its holder from the qname
        else if (typeValue.endsWith("[]")) {
            String name = emitter.getJavaName(type.getQName());
            String packagePrefix = "";

            // Make sure that holders for arrays of either primitive Java types
            // or their wrappers are generated at a predictable location.
            if ((type instanceof CollectionType)
                    && (type.getRefType() instanceof BaseType)) {
                String uri = type.getRefType().getQName().getNamespaceURI();

                packagePrefix = emitter.getNamespaces().getCreate(uri, false);

                if (packagePrefix == null) {
                    packagePrefix = "";
                } else {
                    packagePrefix += '.';
                }
            }

            name = JavaUtils.replace(name, "java.lang.", "");
            
            // This could be a special QName for a indexed property.
            // If so, change the [] to Array.
            name = JavaUtils.replace(name, "[]", "Array");
            name = addPackageName(name, "holders");

            return packagePrefix + name + "Holder";
        }

        // String also has a reserved holder
        else if (typeValue.equals("String")) {
            return "javax.xml.rpc.holders.StringHolder";
        } else if (typeValue.equals("java.lang.String")) {
            return "javax.xml.rpc.holders.StringHolder";
        }

        // Object also has a reserved holder
        else if (typeValue.equals("Object")) {
            return "javax.xml.rpc.holders.ObjectHolder";
        } else if (typeValue.equals("java.lang.Object")) {
            return "javax.xml.rpc.holders.ObjectHolder";
        }

        // Java primitive types have reserved holders
        else if (typeValue.equals("int") || typeValue.equals("long")
                || typeValue.equals("short") || typeValue.equals("float")
                || typeValue.equals("double") || typeValue.equals("boolean")
                || typeValue.equals("byte")) {
            return "javax.xml.rpc.holders." + capitalizeFirstChar(typeValue)
                    + "Holder";
        }

        // Java language classes have reserved holders (with ClassHolder)
        else if (typeValue.startsWith("java.lang.")) {
            return "javax.xml.rpc.holders"
                    + typeValue.substring(typeValue.lastIndexOf("."))
                    + "WrapperHolder";
        } else if (typeValue.indexOf(".") < 0) {
            return "javax.xml.rpc.holders" + typeValue + "WrapperHolder";
        }

        // The classes have reserved holders because they
        // represent schema/soap encoding primitives
        else if (typeValue.equals("java.math.BigDecimal")) {
            return "javax.xml.rpc.holders.BigDecimalHolder";
        } else if (typeValue.equals("java.math.BigInteger")) {
            return "javax.xml.rpc.holders.BigIntegerHolder";
        } else if (typeValue.equals("java.util.Date")) {
            return "org.apache.axis.holders.DateHolder";
        } else if (typeValue.equals("java.util.Calendar")) {
            return "javax.xml.rpc.holders.CalendarHolder";
        } else if (typeValue.equals("javax.xml.namespace.QName")) {
            return "javax.xml.rpc.holders.QNameHolder";
        } else if (typeValue.equals("javax.activation.DataHandler")) {
            return "org.apache.axis.holders.DataHandlerHolder";
        }

        // Check for Axis specific types and return their holders
        else if (typeValue.startsWith("org.apache.axis.types.")) {
            int i = typeValue.lastIndexOf('.');
            String t = typeValue.substring(i + 1);

            return "org.apache.axis.holders." + t + "Holder";
        }

        // For everything else add "holders" package and append
        // holder to the class name.
        else {
            return addPackageName(typeValue, "holders") + "Holder";
        }
    }    // holder

    /**
     * Add package to name
     * 
     * @param className full name of the class.
     * @param newPkg    name of the package to append
     * @return String name with package name added
     */
    public static String addPackageName(String className, String newPkg) {

        int index = className.lastIndexOf(".");

        if (index >= 0) {
            return className.substring(0, index) + "." + newPkg
                    + className.substring(index);
        } else {
            return newPkg + "." + className;
        }
    }

//    /**
//     * Given a fault message, return the fully qualified Java class name
//     * of the exception to be generated from this fault
//     *
//     * @param faultMessage The WSDL fault message
//     * @param symbolTable  the current symbol table
//     * @return A Java class name for the fault
//     */
//    public static String getFullExceptionName(Message faultMessage,
//                                              SymbolTable symbolTable) {
//
//        MessageEntry me = symbolTable.getMessageEntry(faultMessage.getQName());
//
//        return (String) me.getDynamicVar(
//                JavaGeneratorFactory.EXCEPTION_CLASS_NAME);
//    }    // getFullExceptionName
//
//    /**
//     * Given a fault message, return the XML type of the exception data.
//     *
//     * @param faultMessage The WSDL fault message object
//     * @param symbolTable  the current symbol table
//     * @return A QName for the XML type of the data
//     */
//    public static QName getFaultDataType(Message faultMessage,
//                                         SymbolTable symbolTable) {
//
//        MessageEntry me = symbolTable.getMessageEntry(faultMessage.getQName());
//
//        return (QName) me.getDynamicVar(
//                JavaGeneratorFactory.EXCEPTION_DATA_TYPE);
//    }    // getFaultDataType

    /**
     * Given a fault message, return TRUE if the fault is a complex type fault
     * 
     * @param faultMessage The WSDL fault message object
     * @param symbolTable  the current symbol table
     * @return A Java class name for the fault
     */
//    public static boolean isFaultComplex(Message faultMessage,
//                                         SymbolTable symbolTable) {
//
//        MessageEntry me = symbolTable.getMessageEntry(faultMessage.getQName());
//        Boolean ret =
//                (Boolean) me.getDynamicVar(JavaGeneratorFactory.COMPLEX_TYPE_FAULT);
//
//        if (ret != null) {
//            return ret.booleanValue();
//        } else {
//            return false;
//        }
//    }    // isFaultComplex

    /**
     * If the specified node represents a supported JAX-RPC enumeration,
     * a Vector is returned which contains the base type and the enumeration values.
     * The first element in the vector is the base type (an TypeEntry).
     * Subsequent elements are values (Strings).
     * If this is not an enumeration, null is returned.
     * 
     * @param node        
     * @param symbolTable 
     * @return 
     */
    public static Vector getEnumerationBaseAndValues(Node node,
                                                     SymbolTable symbolTable) {

        if (node == null) {
            return null;
        }

        // If the node kind is an element, dive into it.
        QName nodeKind = Utils.getNodeQName(node);

        if ((nodeKind != null) && nodeKind.getLocalPart().equals("element")
                && Constants.isSchemaXSD(nodeKind.getNamespaceURI())) {
            NodeList children = node.getChildNodes();
            Node simpleNode = null;

            for (int j = 0; (j < children.getLength()) && (simpleNode == null);
                 j++) {
                QName simpleKind = Utils.getNodeQName(children.item(j));

                if ((simpleKind != null)
                        && simpleKind.getLocalPart().equals("simpleType")
                        && Constants.isSchemaXSD(
                                simpleKind.getNamespaceURI())) {
                    simpleNode = children.item(j);
                    node = simpleNode;
                }
            }
        }

        // Get the node kind, expecting a schema simpleType
        nodeKind = Utils.getNodeQName(node);

        if ((nodeKind != null) && nodeKind.getLocalPart().equals("simpleType")
                && Constants.isSchemaXSD(nodeKind.getNamespaceURI())) {

            // Under the simpleType there should be a restriction.
            // (There may be other #text nodes, which we will ignore).
            NodeList children = node.getChildNodes();
            Node restrictionNode = null;

            for (int j = 0;
                 (j < children.getLength()) && (restrictionNode == null);
                 j++) {
                QName restrictionKind = Utils.getNodeQName(children.item(j));

                if ((restrictionKind != null)
                        && restrictionKind.getLocalPart().equals("restriction")
                        && Constants.isSchemaXSD(
                                restrictionKind.getNamespaceURI())) {
                    restrictionNode = children.item(j);
                }
            }

            // The restriction node indicates the type being restricted
            // (the base attribute contains this type).
            // The base type must be a simple type, and not boolean
            TypeEntry baseEType = null;

            if (restrictionNode != null) {
                QName baseType = Utils.getTypeQName(restrictionNode,
                        new BooleanHolder(), false);

                baseEType = symbolTable.getType(baseType);

                if (baseEType != null) {
                    String javaName = baseEType.getName();

                    if (javaName.equals("boolean")
                            || !SchemaUtils.isSimpleSchemaType(
                                    baseEType.getQName())) {
                        baseEType = null;
                    }
                }
            }

            // Process the enumeration elements underneath the restriction node
            if ((baseEType != null) && (restrictionNode != null)) {
                Vector v = new Vector();
                NodeList enums = restrictionNode.getChildNodes();

                for (int i = 0; i < enums.getLength(); i++) {
                    QName enumKind = Utils.getNodeQName(enums.item(i));

                    if ((enumKind != null)
                            && enumKind.getLocalPart().equals("enumeration")
                            && Constants.isSchemaXSD(
                                    enumKind.getNamespaceURI())) {

                        // Put the enum value in the vector.
                        Node enumNode = enums.item(i);
                        String value = Utils.getAttribute(enumNode, "value");

                        if (value != null) {
                            v.add(value);
                        }
                    }
                }

                // is this really an enumeration?
                if (v.isEmpty()) {
                    return null;
                }

                // The first element in the vector is the base type (an TypeEntry).
                v.add(0, baseEType);

                return v;
            }
        }

        return null;
    }

    /**
     * Capitalize the first character of the name.
     * 
     * @param name 
     * @return 
     */
    public static String capitalizeFirstChar(String name) {

        if ((name == null) || name.equals("")) {
            return name;
        }

        char start = name.charAt(0);

        if (Character.isLowerCase(start)) {
            start = Character.toUpperCase(start);

            return start + name.substring(1);
        }

        return name;
    }    // capitalizeFirstChar

    /**
     * Prepend an underscore to the name
     * 
     * @param name 
     * @return 
     */
    public static String addUnderscore(String name) {

        if ((name == null) || name.equals("")) {
            return name;
        }

        return "_" + name;
    }

    /**
     * Map an XML name to a valid Java identifier
     * 
     * @param name 
     * @return 
     */
    public static String xmlNameToJava(String name) {

        // NOTE:  This method should really go away and all callers should call
        // JavaUtils.xmlNameToJava directly.  But there are a lot of them and I wanted
        // to keep the changes to a minimum.  Besides, these are static methods so the should
        // be inlined.
        return JavaUtils.xmlNameToJava(name);
    }

    /**
     * Map an XML name to a valid Java identifier w/ capitolized first letter
     * 
     * @param name 
     * @return 
     */
    public static String xmlNameToJavaClass(String name) {
        return capitalizeFirstChar(xmlNameToJava(name));
    }

    /**
     * Method makePackageName
     * 
     * @param namespace 
     * @return 
     */
    public static String makePackageName(String namespace) {

        String hostname = null;
        String path = "";

        // get the target namespace of the document
        try {
            URL u = new URL(namespace);

            hostname = u.getHost();
            path = u.getPath();
        } catch (MalformedURLException e) {
            if (namespace.indexOf(":") > -1) {
                hostname = namespace.substring(namespace.indexOf(":") + 1);

                if (hostname.indexOf("/") > -1) {
                    hostname = hostname.substring(0, hostname.indexOf("/"));
                }
            } else {
                hostname = namespace;
            }
        }

        // if we didn't file a hostname, bail
        if (hostname == null) {
            return null;
        }

        // convert illegal java identifier
        hostname = hostname.replace('-', '_');
        path = path.replace('-', '_');

        // chomp off last forward slash in path, if necessary
        if ((path.length() > 0) && (path.charAt(path.length() - 1) == '/')) {
            path = path.substring(0, path.length() - 1);
        }

        // tokenize the hostname and reverse it
        StringTokenizer st = new StringTokenizer(hostname, ".:");
        String[] words = new String[st.countTokens()];

        for (int i = 0; i < words.length; ++i) {
            words[i] = st.nextToken();
        }

        StringBuffer sb = new StringBuffer(namespace.length());

        for (int i = words.length - 1; i >= 0; --i) {
            addWordToPackageBuffer(sb, words[i], (i == words.length - 1));
        }

        // tokenize the path
        StringTokenizer st2 = new StringTokenizer(path, "/");

        while (st2.hasMoreTokens()) {
            addWordToPackageBuffer(sb, st2.nextToken(), false);
        }

        return sb.toString();
    }

    /**
     * Massage <tt>word</tt> into a form suitable for use in a Java package name.
     * Append it to the target string buffer with a <tt>.</tt> delimiter iff
     * <tt>word</tt> is not the first word in the package name.
     * 
     * @param sb        the buffer to append to
     * @param word      the word to append
     * @param firstWord a flag indicating whether this is the first word
     */
    private static void addWordToPackageBuffer(StringBuffer sb, String word,
                                               boolean firstWord) {

        if (JavaUtils.isJavaKeyword(word)) {
            word = JavaUtils.makeNonJavaKeyword(word);
        }

        // separate with dot after the first word
        if (!firstWord) {
            sb.append('.');
        }

        // prefix digits with underscores
        if (Character.isDigit(word.charAt(0))) {
            sb.append('_');
        }

        // replace periods with underscores
        if (word.indexOf('.') != -1) {
            char[] buf = word.toCharArray();

            for (int i = 0; i < word.length(); i++) {
                if (buf[i] == '.') {
                    buf[i] = '_';
                }
            }

            word = new String(buf);
        }

        sb.append(word);
    }

    /**
     * Query Java Local Name
     * 
     * @param fullName 
     * @return 
     */
    public static String getJavaLocalName(String fullName) {
        return fullName.substring(fullName.lastIndexOf('.') + 1);
    }    // getJavaLocalName

    /**
     * Query Java Package Name
     * 
     * @param fullName 
     * @return 
     */
    public static String getJavaPackageName(String fullName) {

        if (fullName.lastIndexOf('.') > 0) {
            return fullName.substring(0, fullName.lastIndexOf('.'));
        } else {
            return "";
        }
    }    // getJavaPackageName

    /**
     * Does the given file already exist in the given namespace?
     * 
     * @param name       
     * @param namespace  
     * @param namespaces 
     * @return 
     * @throws IOException 
     */
    public static boolean fileExists(
            String name, String namespace, Namespaces namespaces)
            throws IOException {

        String packageName = namespaces.getAsDir(namespace);
        String fullName = packageName + name;

        return new File(fullName).exists();
    }    // fileExists

    /** A simple map of the primitive types and their holder objects */
    private static HashMap TYPES = new HashMap(7);

    static {
        TYPES.put("int", "java.lang.Integer");
        TYPES.put("float", "java.lang.Float");
        TYPES.put("boolean", "java.lang.Boolean");
        TYPES.put("double", "java.lang.Double");
        TYPES.put("byte", "java.lang.Byte");
        TYPES.put("short", "java.lang.Short");
        TYPES.put("long", "java.lang.Long");
    }

    /**
     * Return a string with "var" wrapped as an Object type if needed
     * 
     * @param type 
     * @param var  
     * @return 
     */
    public static String wrapPrimitiveType(TypeEntry type, String var) {

        String objType = (type == null)
                ? null
                : (String) TYPES.get(type.getName());

        if (objType != null) {
            return "new " + objType + "(" + var + ")";
        } else if ((type != null) && type.getName().equals("byte[]")
                && type.getQName().getLocalPart().equals("hexBinary")) {

            // Need to wrap byte[] in special HexBinary object to get the correct serialization
            return "new org.apache.axis.types.HexBinary(" + var + ")";
        } else {
            return var;
        }
    }    // wrapPrimitiveType

    /**
     * Return the Object variable 'var' cast to the appropriate type
     * doing the right thing for the primitive types.
     * 
     * @param var      
     * @return 
     */
    public static String getResponseString(Parameter param,
                                           String var) {
        if (param.getType() == null) {
            return ";";
        }
        String typeName = param.getType().getName();
        MimeInfo mimeInfo = param.getMIMEInfo();
        
        String mimeType = (mimeInfo == null)
                ? null
                : mimeInfo.getType();
        String mimeDimensions = (mimeInfo == null)
                ? ""
                : mimeInfo.getDimensions();

        if (mimeType != null) {
            if (mimeType.equals("image/gif") || mimeType.equals("image/jpeg")) {
                return "(java.awt.Image" + mimeDimensions + ") " + var + ";";
            } else if (mimeType.equals("text/plain")) {
                return "(java.lang.String" + mimeDimensions + ") " + var + ";";
            } else if (mimeType.equals("text/xml")
                    || mimeType.equals("application/xml")) {
                return "(javax.xml.transform.Source" + mimeDimensions + ") "
                        + var + ";";
            } else if (mimeType.startsWith("multipart/")) {
                return "(javax.mail.internet.MimeMultipart" + mimeDimensions
                        + ") " + var + ";";
            } else if (mimeType.startsWith("application/octetstream")
                    || mimeType.startsWith("application/octet-stream")) {
                //the hyphenated test is new and RFC compliant; the old one was retained
                //for backwards compatibility.
                return "(org.apache.axis.attachments.OctetStream"
                        + mimeDimensions + ") " + var + ";";
            } else {
                return "(javax.activation.DataHandler" + mimeDimensions + ") "
                        + var + ";";
            }
        }

        String objType = (String) TYPES.get(typeName);
        
        if (objType != null) {
            if (param.isOmittable()) {
                typeName = objType;
            } else {
                return "((" + objType + ") " + var + ")." + typeName +
                        "Value();";
            }
        }
        
        return "(" + typeName + ") " + var + ";";
    }    // getResponseString

    /**
     * Method isPrimitiveType
     * 
     * @param type 
     * @return 
     */
    public static boolean isPrimitiveType(TypeEntry type) {
        return TYPES.get(type.getName()) != null;
    }    // isPrimitiveType
    
    /**
     * Return a "wrapper" type for the given type name.  In other words,
     * if it's a primitive type ("int") return the java wrapper class
     * ("java.lang.Integer").  Otherwise return the type name itself.
     * 
     * @param type
     * @return the name of a java wrapper class for the type, or the type's
     *         name if it's not primitive.
     */ 
    public static String getWrapperType(String type) {
        String ret = (String)TYPES.get(type);
        return (ret == null) ? type : ret;
    }

//    /**
//     * Return the operation QName.  The namespace is determined from
//     * the soap:body namespace, if it exists, otherwise it is "".
//     *
//     * @param bindingOper the operation
//     * @param bEntry      the symbol table binding entry
//     * @param symbolTable SymbolTable
//     * @return the operation QName
//     */
//    public static QName getOperationQName(BindingOperation bindingOper,
//                                          BindingEntry bEntry,
//                                          SymbolTable symbolTable) {
//
//        Operation operation = bindingOper.getOperation();
//        String operationName = operation.getName();
//
//        // For the wrapped case, use the part element's name...which is
//        // is the same as the operation name, but may have a different
//        // namespace ?
//        // example:
//        // <part name="paramters" element="ns:myelem">
//        if ((bEntry.getBindingStyle() == Style.DOCUMENT)
//                && symbolTable.isWrapped()) {
//            Input input = operation.getInput();
//
//            if (input != null) {
//                Map parts = input.getMessage().getParts();
//
//                if ((parts != null) && !parts.isEmpty()) {
//                    Iterator i = parts.values().iterator();
//                    Part p = (Part) i.next();
//
//                    return p.getElementName();
//                }
//            }
//        }
//
//        String ns = null;
//
//        // Get a namespace from the soap:body tag, if any
//        // example:
//        // <soap:body namespace="this_is_what_we_want" ..>
//        BindingInput bindInput = bindingOper.getBindingInput();
//
//        if (bindInput != null) {
//            Iterator it = bindInput.getExtensibilityElements().iterator();
//
//            while (it.hasNext()) {
//                ExtensibilityElement elem = (ExtensibilityElement) it.next();
//
//                if (elem instanceof SOAPBody) {
//                    SOAPBody body = (SOAPBody) elem;
//
//                    ns = body.getNamespaceURI();
//                    if (bEntry.getInputBodyType(operation) == Use.ENCODED && (ns == null || ns.length() == 0)) {
//                        log.warn(Messages.getMessage("badNamespaceForOperation00",
//                                bEntry.getName(),
//                                operation.getName()));
//
//                    }
//                    break;
//                } else if (elem instanceof MIMEMultipartRelated) {
//                    Object part = null;
//                    javax.wsdl.extensions.mime.MIMEMultipartRelated mpr =
//                            (javax.wsdl.extensions.mime.MIMEMultipartRelated) elem;
//                    List l =
//                            mpr.getMIMEParts();
//
//                    for (int j = 0;
//                         (l != null) && (j < l.size()) && (part == null);
//                         j++) {
//                        javax.wsdl.extensions.mime.MIMEPart mp =
//                                (javax.wsdl.extensions.mime.MIMEPart) l.get(j);
//                        List ll =
//                                mp.getExtensibilityElements();
//
//                        for (int k = 0; (ll != null) && (k < ll.size())
//                                && (part == null); k++) {
//                            part = ll.get(k);
//
//                            if (part instanceof SOAPBody) {
//                                SOAPBody body = (SOAPBody) part;
//
//                                ns = body.getNamespaceURI();
//                                if (bEntry.getInputBodyType(operation) == Use.ENCODED && (ns == null || ns.length() == 0)) {
//                                    log.warn(Messages.getMessage("badNamespaceForOperation00",
//                                            bEntry.getName(),
//                                            operation.getName()));
//
//                                }
//                                break;
//                            } else {
//                                part = null;
//                            }
//                        }
//                    }
//                } else if (elem instanceof UnknownExtensibilityElement) {
//
//                    // TODO: After WSDL4J supports soap12, change this code
//                    UnknownExtensibilityElement unkElement =
//                            (UnknownExtensibilityElement) elem;
//                    QName name =
//                            unkElement.getElementType();
//
//                    if (name.getNamespaceURI().equals(Constants.URI_WSDL12_SOAP)
//                            && name.getLocalPart().equals("body")) {
//                        ns = unkElement.getElement().getAttribute("namespace");
//                    }
//                }
//            }
//        }
//
//        // If we didn't get a namespace from the soap:body, then
//        // use "".  We should probably use the targetNamespace,
//        // but the target namespace of what?  binding?  portType?
//        // Also, we don't have enough info for to get it.
//        if (ns == null) {
//            ns = "";
//        }
//
//        return new QName(ns, operationName);
//    }

    /**
     * Return the SOAPAction (if any) of this binding operation
     *
     * @param bindingOper the operation to look at
     * @return the SOAPAction or null if not found
     */
    public static String getOperationSOAPAction(BindingOperation bindingOper) {
        // Find the SOAPAction.
        List elems = bindingOper.getExtensibilityElements();
        Iterator it = elems.iterator();
        boolean found = false;
        String action = null;

        while (!found && it.hasNext()) {
            ExtensibilityElement elem =
                    (ExtensibilityElement) it.next();

            if (elem instanceof SOAPOperation) {
                SOAPOperation soapOp = (SOAPOperation) elem;
                action = soapOp.getSoapActionURI();
                found = true;
            } else if (elem instanceof UnknownExtensibilityElement) {

                // TODO: After WSDL4J supports soap12, change this code
                UnknownExtensibilityElement unkElement =
                        (UnknownExtensibilityElement) elem;
                QName name =
                        unkElement.getElementType();

                if (name.getNamespaceURI().equals(
                        Constants.URI_WSDL12_SOAP)
                        && name.getLocalPart().equals("operation")) {
                    action = unkElement.getElement().getAttribute(
                                    "soapAction");
                    found = true;
                }
            }
        }
        return action;
    }

    /**
     * Common code for generating a QName in emitted code.  Note that there's
     * no semicolon at the end, so we can use this in a variety of contexts.
     * 
     * @param qname 
     * @return 
     */
    public static String getNewQName(javax.xml.namespace.QName qname) {
        return "new javax.xml.namespace.QName(\"" + qname.getNamespaceURI()
                + "\", \"" + qname.getLocalPart() + "\")";
    }

    public static String getNewQNameWithLastLocalPart(javax.xml.namespace.QName qname) {
        return "new javax.xml.namespace.QName(\"" + qname.getNamespaceURI()
                + "\", \"" + getLastLocalPart(qname.getLocalPart()) + "\")";
    }

    /**
     * Get the parameter type name.  If this is a MIME type, then
     * figure out the appropriate type from the MIME type, otherwise
     * use the name of the type itself.
     * 
     * @param parm 
     * @return 
     */
    public static String getParameterTypeName(Parameter parm) {

        String ret;

        if (parm.getMIMEInfo() == null) {
            ret = parm.getType().getName();
            if (parm.isOmittable()) {
                String wrapped = (String)TYPES.get(ret);
                if (wrapped != null)
                    ret = wrapped;
            }
        } else {
            String mime = parm.getMIMEInfo().getType();

            ret = JavaUtils.mimeToJava(mime);

            if (ret == null) {
                ret = parm.getType().getName();
            } else {
                ret += parm.getMIMEInfo().getDimensions();
            }
        }

        return ret;
    }    // getParameterTypeName

    /**
     * Get the QName that could be used in the xsi:type
     * when serializing an object for this parameter/return
     * 
     * @param param is a parameter
     * @return the QName of the parameter's xsi type
     */
    public static QName getXSIType(Parameter param) {

        if (param.getMIMEInfo() != null) {
            return getMIMETypeQName(param.getMIMEInfo().getType());
        }

        return getXSIType(param.getType());
    }    // getXSIType

    /**
     * Get the QName that could be used in the xsi:type
     * when serializing an object of the given type.
     * 
     * @param te is the type entry
     * @return the QName of the type's xsi type
     */
    public static QName getXSIType(TypeEntry te) {

        QName xmlType = null;

        // If the TypeEntry describes an Element, get
        // the referenced Type.
        if ((te != null) && (te instanceof Element)
                && (te.getRefType() != null)) {
            te = te.getRefType();
        }

        // If the TypeEntry is a CollectionTE, use
        // the TypeEntry representing the component Type
        // So for example a parameter that takes a
        // collection type for
        // <element name="A" type="xsd:string" maxOccurs="unbounded"/>
        // will be
        // new ParameterDesc(<QName of A>, IN,
        // <QName of xsd:string>,
        // String[])
        if ((te != null) && (te instanceof CollectionTE)
                && (te.getRefType() != null)) {
            te = te.getRefType();
        }

        if (te != null) {
            xmlType = te.getQName();
        }

        return xmlType;
    }

    /**
     * Given a MIME type, return the AXIS-specific type QName.
     * 
     * @param mimeName the MIME type name
     * @return the AXIS-specific QName for the MIME type
     */
    public static QName getMIMETypeQName(String mimeName) {

        if ("text/plain".equals(mimeName)) {
            return Constants.MIME_PLAINTEXT;
        } else if ("image/gif".equals(mimeName)
                || "image/jpeg".equals(mimeName)) {
            return Constants.MIME_IMAGE;
        } else if ("text/xml".equals(mimeName)
                || "applications/xml".equals(mimeName)) {
            return Constants.MIME_SOURCE;
        } else if ("application/octet-stream".equals(mimeName) ||
                   "application/octetstream".equals(mimeName)) {
            return Constants.MIME_OCTETSTREAM;
        } else if ((mimeName != null) && mimeName.startsWith("multipart/")) {
            return Constants.MIME_MULTIPART;
        } else {
            return Constants.MIME_DATA_HANDLER;
        }
    }    // getMIMEType

//    /**
//     * Are there any MIME parameters in the given binding?
//     *
//     * @param bEntry
//     * @return
//     */
//    public static boolean hasMIME(BindingEntry bEntry) {
//
//        List operations = bEntry.getBinding().getBindingOperations();
//
//        for (int i = 0; i < operations.size(); ++i) {
//            BindingOperation operation = (BindingOperation) operations.get(i);
//
//            if (hasMIME(bEntry, operation)) {
//                return true;
//            }
//        }
//
//        return false;
//    }    // hasMIME

//    /**
//     * Are there any MIME parameters in the given binding's operation?
//     *
//     * @param bEntry
//     * @param operation
//     * @return
//     */
//    public static boolean hasMIME(BindingEntry bEntry,
//                                  BindingOperation operation) {
//
//        Parameters parameters = bEntry.getParameters(operation.getOperation());
//
//        if (parameters != null) {
//            for (int idx = 0; idx < parameters.list.size(); ++idx) {
//                Parameter p = (Parameter) parameters.list.get(idx);
//
//                if (p.getMIMEInfo() != null) {
//                    return true;
//                }
//            }
//        }
//
//        return false;
//    }    // hasMIME

    /** Field constructorMap */
    private static HashMap constructorMap = new HashMap(50);

    /** Field constructorThrowMap */
    private static HashMap constructorThrowMap = new HashMap(50);

    static {

        // Type maps to a valid initialization value for that type
        // Type var = new Type(arg)
        // Where "Type" is the key and "new Type(arg)" is the string stored
        // Used in emitting test cases and server skeletons.
        constructorMap.put("int", "0");
        constructorMap.put("float", "0");
        constructorMap.put("boolean", "true");
        constructorMap.put("double", "0");
        constructorMap.put("byte", "(byte)0");
        constructorMap.put("short", "(short)0");
        constructorMap.put("long", "0");
        constructorMap.put("java.lang.Boolean", "new java.lang.Boolean(false)");
        constructorMap.put("java.lang.Byte", "new java.lang.Byte((byte)0)");
        constructorMap.put("java.lang.Double", "new java.lang.Double(0)");
        constructorMap.put("java.lang.Float", "new java.lang.Float(0)");
        constructorMap.put("java.lang.Integer", "new java.lang.Integer(0)");
        constructorMap.put("java.lang.Long", "new java.lang.Long(0)");
        constructorMap.put("java.lang.Short", "new java.lang.Short((short)0)");
        constructorMap.put("java.math.BigDecimal",
                "new java.math.BigDecimal(0)");
        constructorMap.put("java.math.BigInteger",
                "new java.math.BigInteger(\"0\")");
        constructorMap.put("java.lang.Object", "new java.lang.String()");
        constructorMap.put("byte[]", "new byte[0]");
        constructorMap.put("java.util.Calendar",
                "java.util.Calendar.getInstance()");
        constructorMap.put(
                "javax.xml.namespace.QName",
                "new javax.xml.namespace.QName(\"http://double-double\", \"toil-and-trouble\")");
        constructorMap.put(
                "org.apache.axis.types.NonNegativeInteger",
                "new org.apache.axis.types.NonNegativeInteger(\"0\")");
        constructorMap.put("org.apache.axis.types.PositiveInteger",
                "new org.apache.axis.types.PositiveInteger(\"1\")");
        constructorMap.put(
                "org.apache.axis.types.NonPositiveInteger",
                "new org.apache.axis.types.NonPositiveInteger(\"0\")");
        constructorMap.put("org.apache.axis.types.NegativeInteger",
                "new org.apache.axis.types.NegativeInteger(\"-1\")");

        // These constructors throw exception
        constructorThrowMap.put(
                "org.apache.axis.types.Time",
                "new org.apache.axis.types.Time(\"15:45:45.275Z\")");
        constructorThrowMap.put("org.apache.axis.types.UnsignedLong",
                "new org.apache.axis.types.UnsignedLong(0)");
        constructorThrowMap.put("org.apache.axis.types.UnsignedInt",
                "new org.apache.axis.types.UnsignedInt(0)");
        constructorThrowMap.put("org.apache.axis.types.UnsignedShort",
                "new org.apache.axis.types.UnsignedShort(0)");
        constructorThrowMap.put("org.apache.axis.types.UnsignedByte",
                "new org.apache.axis.types.UnsignedByte(0)");
        constructorThrowMap.put(
                "org.apache.axis.types.URI",
                "new org.apache.axis.types.URI(\"urn:testing\")");
        constructorThrowMap.put("org.apache.axis.types.Year",
                "new org.apache.axis.types.Year(2000)");
        constructorThrowMap.put("org.apache.axis.types.Month",
                "new org.apache.axis.types.Month(1)");
        constructorThrowMap.put("org.apache.axis.types.Day",
                "new org.apache.axis.types.Day(1)");
        constructorThrowMap.put("org.apache.axis.types.YearMonth",
                "new org.apache.axis.types.YearMonth(2000,1)");
        constructorThrowMap.put("org.apache.axis.types.MonthDay",
                "new org.apache.axis.types.MonthDay(1, 1)");
    }

    /**
     * Return a constructor for the provided Parameter
     * This string will be suitable for assignment:
     * <p/>
     * Foo var = <i>string returned</i>
     * <p/>
     * Handles basic java types (int, float, etc), wrapper types (Integer, etc)
     * and certain java.math (BigDecimal, BigInteger) types.
     * Will also handle all Axis specific types (org.apache.axis.types.*)
     * <p/>
     * Caller should expect to wrap the construction in a try/catch block
     * if bThrow is set to <i>true</i>.
     * 
     * @param param       info about the parameter we need a constructor for
     * @param symbolTable used to lookup enumerations
     * @param bThrow      set to true if contructor needs try/catch block
     * @return 
     */
    public static String getConstructorForParam(Parameter param,
                                                SymbolTable symbolTable,
                                                BooleanHolder bThrow) {

        String paramType = param.getType().getName();
        if (param.isOmittable()) {
            paramType = Utils.getWrapperType(paramType);
        }
        String mimeType = (param.getMIMEInfo() == null)
                ? null
                : param.getMIMEInfo().getType();
        String mimeDimensions = (param.getMIMEInfo() == null)
                ? ""
                : param.getMIMEInfo().getDimensions();
        String out = null;

        // Handle mime types
        if (mimeType != null) {
            if (mimeType.equals("image/gif") || mimeType.equals("image/jpeg")) {
                return "null";
            } else if (mimeType.equals("text/xml")
                    || mimeType.equals("application/xml")) {
                if (mimeDimensions.length() <= 0) {
                    return "new javax.xml.transform.stream.StreamSource()";
                } else {
                    return "new javax.xml.transform.stream.StreamSource[0]";
                }
            } else if (mimeType.equals("application/octet-stream")||
                       mimeType.equals("application/octetstream")) {
                if (mimeDimensions.length() <= 0) {
                    return "new org.apache.axis.attachments.OctetStream()";
                } else {
                    return "new org.apache.axis.attachments.OctetStream[0]";
                }
            } else {
                return "new " + Utils.getParameterTypeName(param) + "()";
            }
        }

        // Look up paramType in the table
        out = (String) constructorMap.get(paramType);

        if (out != null) {
            return out;
        }

        // Look up paramType in the table of constructors that can throw exceptions
        out = (String) constructorThrowMap.get(paramType);

        if (out != null) {
            bThrow.value = true;

            return out;
        }

        // Handle arrays
        if (paramType.endsWith("[]")) {
            return "new " + JavaUtils.replace(paramType, "[]", "[0]");
        }

        /** * We have some constructed type. */

        // Check for enumeration
        Vector v = Utils.getEnumerationBaseAndValues(param.getType().getNode(),
                symbolTable);

        if (v != null) {

            // This constructed type is an enumeration.  Use the first one.
            String enumeration =
                    (String) JavaEnumTypeWriter.getEnumValueIds(v).get(0);

            return paramType + "." + enumeration;
        }
        
        if(param.getType().getRefType()!= null){
            // Check for enumeration
            Vector v2 = Utils.getEnumerationBaseAndValues(param.getType().getRefType().getNode(),
                    symbolTable);

            if (v2 != null) {

                // This constructed type is an enumeration.  Use the first one.
                String enumeration =
                        (String) JavaEnumTypeWriter.getEnumValueIds(v2).get(0);

                return paramType + "." + enumeration;
            }
        }

        // This constructed type is a normal type, instantiate it.
        return "new " + paramType + "()";
    }

    public static boolean shouldEmit(TypeEntry type) {
        // 1) Don't register types that are base (primitive) types or attributeGroups.
        // If the baseType != null && getRefType() != null this
        // is a simpleType that must be registered.
        // 2) Don't register the special types for collections
        // (indexed properties) or elements
        // 3) Don't register types that are not referenced
        // or only referenced in a literal context.
        return (!(((type.getBaseType() != null) && (type.getRefType() == null))
                || (type instanceof CollectionTE)
                || (type instanceof Element) || !type.isReferenced()
                || type.isOnlyLiteralReferenced()
                || ((type.getNode() != null)
                && type.getNode().getLocalName().equals(
                        "attributeGroup"))));
    }

    public static QName getItemQName(TypeEntry te) {
        if (te instanceof DefinedElement) {
            te = te.getRefType();
        }
        return te.getItemQName();
    }
}    // class Utils
