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

//import org.apache.axis.utils.Messages;
//import org.apache.axis.wsdl.symbolTable.ContainedAttribute;
//import org.apache.axis.utils.JavaUtils;
//import org.apache.axis.wsdl.symbolTable.DefinedType;
//import org.apache.axis.wsdl.symbolTable.ElementDecl;
//import org.apache.axis.wsdl.symbolTable.SchemaUtils;
//import org.apache.axis.wsdl.symbolTable.TypeEntry;
//import org.apache.axis.wsdl.symbolTable.CollectionTE;
import org.apache.axis2.databinding.symbolTable.*;
import org.apache.axis2.databinding.utils.JavaUtils;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Vector;
import java.util.Set;

/**
 * This is Wsdl2java's Helper Type Writer.  It writes the <typeName>.java file.
 */
public class JavaBeanHelperWriter extends JavaClassWriter {

    /** Field type */
    protected TypeEntry type;

    /** Field elements */
    protected Vector elements;

    /** Field attributes */
    protected Vector attributes;

    /** Field extendType */
    protected TypeEntry extendType;

    /** Field wrapperPW */
    protected PrintWriter wrapperPW = null;

    /** Field elementMetaData */
    protected Vector elementMetaData = null;

    /** Field canSearchParents */
    protected boolean canSearchParents;

    /** Field reservedPropNames */
    protected Set reservedPropNames;

    /**
     * Constructor.
     * 
     * @param emitter    
     * @param type       The type representing this class
     * @param elements   Vector containing the Type and name of each property
     * @param extendType The type representing the extended class (or null)
     * @param attributes Vector containing the attribute types and names
     */
    protected JavaBeanHelperWriter(Emitter emitter, TypeEntry type,
                                   Vector elements, TypeEntry extendType,
                                   Vector attributes, Set reservedPropNames) {

        super(emitter, type.getName() + "_Helper", "helper");

        this.type = type;
        this.elements = elements;
        this.attributes = attributes;
        this.extendType = extendType;
        this.reservedPropNames = reservedPropNames;

        // is this a complex type that is derived from other types
        // by restriction?  if so, set the policy of the generated
        // TypeDescription to ignore metadata associated with
        // superclasses, as restricted types are required to
        // define their entire content model.  Hence the type
        // description associated with the current type provides
        // all of the types (and only those types) allowed in
        // the restricted derivation.
        if ((null != extendType)
                && (null
                != SchemaUtils.getComplexElementRestrictionBase(
                        type.getNode(), emitter.getSymbolTable()))) {
            this.canSearchParents = false;
        } else {
            this.canSearchParents = true;
        }
    }    // ctor

    /**
     * The bean helper class may be its own class, or it may be
     * embedded within the bean class.  If it's embedded within the
     * bean class, the JavaBeanWriter will set JavaBeanHelperWriter's
     * PrintWriter to its own.
     * 
     * @param pw 
     */
    protected void setPrintWriter(PrintWriter pw) {
        this.wrapperPW = pw;
    }    // setPrintWriter

    /**
     * The default behaviour (of super.getPrintWriter) is, given the
     * file name, create a PrintWriter for it.  If the bean helper
     * that this class is generating is embedded within a bean, then
     * the PrintWriter returned by this method is the JavaBeanWriter's
     * PrintWriter.  Otherwise super.getPrintWriter is called.
     * 
     * @param filename 
     * @return 
     * @throws IOException 
     */
    protected PrintWriter getPrintWriter(String filename) throws IOException {

        return (wrapperPW == null)
                ? super.getPrintWriter(filename)
                : wrapperPW;
    }    // getPrintWriter

    /**
     * Only register the filename if the bean helper is not wrapped
     * within a bean.
     * 
     * @param file 
     */
    protected void registerFile(String file) {

        if (wrapperPW == null) {
            super.registerFile(file);
        }
    }    // registerFile

    /**
     * Return the string:  "Generating <file>".
     * only if we are going to generate a new file.
     * 
     * @param file 
     * @return 
     */
    protected String verboseMessage(String file) {

        if (wrapperPW == null) {
            return super.verboseMessage(file);
        } else {
            return null;
        }
    }    // verboseMessage

    /**
     * Only write the file header if the bean helper is not wrapped
     * within a bean.
     * 
     * @param pw 
     * @throws IOException 
     */
    protected void writeFileHeader(PrintWriter pw) throws IOException {

        if (wrapperPW == null) {
            super.writeFileHeader(pw);
        }
    }    // writeFileHeader

    /**
     * Generate the file body for the bean helper.
     * 
     * @param pw 
     * @throws IOException 
     */
    protected void writeFileBody(PrintWriter pw) throws IOException {

        writeMetaData(pw);
        writeSerializer(pw);
        writeDeserializer(pw);
    }    // writeFileBody

    /**
     * Only write the file footer if the bean helper is not
     * wrapped within a bean.
     * 
     * @param pw 
     * @throws IOException 
     */
    protected void writeFileFooter(PrintWriter pw) throws IOException {

        if (wrapperPW == null) {
            super.writeFileFooter(pw);
        }
    }    // writeFileFooter

    /**
     * Only close the PrintWriter if the PrintWriter belongs to
     * this class.  If the bean helper is embedded within a bean
     * then the PrintWriter belongs to JavaBeanWriter and THAT
     * class is responsible for closing the PrintWriter.
     * 
     * @param pw 
     */
    protected void closePrintWriter(PrintWriter pw) {

        // If the output of this writer is wrapped within
        // another writer (JavaBeanWriter), then THAT
        // writer will close the PrintWriter, not this one.
        if (wrapperPW == null) {
            pw.close();
        }
    }    // closePrintWriter

    /**
     * write MetaData code
     * 
     * @param pw 
     * @throws IOException 
     */
    protected void writeMetaData(PrintWriter pw) throws IOException {

        // Collect elementMetaData
        if (elements != null) {
            for (int i = 0; i < elements.size(); i++) {
                ElementDecl elem = (ElementDecl) elements.get(i);

                // String elemName = elem.getName().getLocalPart();
                // String javaName = Utils.xmlNameToJava(elemName);
                // Changed the code to write meta data
                // for all of the elements in order to
                // support sequences. Defect 9060
                // Meta data is needed if the default serializer
                // action cannot map the javaName back to the
                // element's qname.  This occurs if:
                // - the javaName and element name local part are different.
                // - the javaName starts with uppercase char (this is a wierd
                // case and we have several problems with the mapping rules.
                // Seems best to gen meta data in this case.)
                // - the element name is qualified (has a namespace uri)
                // its also needed if:
                // - the element has the minoccurs flag set
                // if (!javaName.equals(elemName) ||
                // Character.isUpperCase(javaName.charAt(0)) ||
                // !elem.getName().getNamespaceURI().equals("") ||
                // elem.getMinOccursIs0()) {
                // If we did some mangling, make sure we'll write out the XML
                // the correct way.
                if (elementMetaData == null) {
                    elementMetaData = new Vector();
                }

                elementMetaData.add(elem);

                // }
            }
        }

//        pw.println("    // " + Messages.getMessage("typeMeta"));
        pw.println(
                "    private static org.apache.axis.description.TypeDesc typeDesc =");
        pw.println("        new org.apache.axis.description.TypeDesc("
                + Utils.getJavaLocalName(type.getName()) + ".class, "
                + (this.canSearchParents
                ? "true"
                : "false") + ");");
        pw.println();
        pw.println("    static {");
        pw.println("        typeDesc.setXmlType("
                + Utils.getNewQName(type.getQName()) + ");");

        // Add attribute and element field descriptors
        if ((attributes != null) || (elementMetaData != null)) {
            if (attributes != null) {
                boolean wroteAttrDecl = false;

                for (int i = 0; i < attributes.size(); i++) {
                    ContainedAttribute attr = (ContainedAttribute) attributes.get(i);
                    TypeEntry te = attr.getType();
                    QName attrName = attr.getQName();
                    String fieldName = getAsFieldName(attr.getName());

                    QName attrXmlType = te.getQName();

                    pw.print("        ");

                    if (!wroteAttrDecl) {
                        pw.print("org.apache.axis.description.AttributeDesc ");

                        wroteAttrDecl = true;
                    }

                    pw.println(
                            "attrField = new org.apache.axis.description.AttributeDesc();");
                    pw.println("        attrField.setFieldName(\"" + fieldName
                            + "\");");
                    pw.println("        attrField.setXmlName("
                            + Utils.getNewQNameWithLastLocalPart(attrName) + ");");

                    if (attrXmlType != null) {
                        pw.println("        attrField.setXmlType("
                                + Utils.getNewQName(attrXmlType) + ");");
                    }

                    pw.println("        typeDesc.addFieldDesc(attrField);");
                }
            }

            if (elementMetaData != null) {
                boolean wroteElemDecl = false;

                for (int i = 0; i < elementMetaData.size(); i++) {
                    ElementDecl elem =
                            (ElementDecl) elementMetaData.elementAt(i);

                    if (elem.getAnyElement()) {
                        continue;
                    }

                    String fieldName = getAsFieldName(elem.getName());
                    QName xmlName = elem.getQName();

                    // Some special handling for arrays.
                    TypeEntry elemType = elem.getType();
                    QName xmlType = null;

                    if ((elemType.getDimensions().length() > 1)
                            && (elemType.getClass() == DefinedType.class)) {

                        // If we have a DefinedType with dimensions, it must
                        // be a SOAP array derived type.  In this case, use
                        // the refType's QName for the metadata.
                        elemType = elemType.getRefType();
                    } else {
                        // Otherwise, use the first non-Collection type we
                        // encounter up the ref chain.
                        while (elemType instanceof CollectionTE) {
                            elemType = elemType.getRefType();
                        }
                    }
                    xmlType = elemType.getQName();

                    pw.print("        ");

                    if (!wroteElemDecl) {
                        pw.print("org.apache.axis.description.ElementDesc ");

                        wroteElemDecl = true;
                    }

                    pw.println(
                            "elemField = new org.apache.axis.description.ElementDesc();");
                    pw.println("        elemField.setFieldName(\"" + fieldName
                            + "\");");
                    pw.println("        elemField.setXmlName("
                            + Utils.getNewQNameWithLastLocalPart(xmlName) + ");");

                    if (xmlType != null) {
                        pw.println("        elemField.setXmlType("
                                + Utils.getNewQName(xmlType) + ");");
                    }

                    if (elem.getMinOccursIs0()) {
                        pw.println("        elemField.setMinOccurs(0);");
                    }
                    if (elem.getNillable()) {
                        pw.println("        elemField.setNillable(true);");
                    } else {
                        pw.println("        elemField.setNillable(false);");
                    }

                    if(elem.getMaxOccursIsUnbounded()) {
                        pw.println("        elemField.setMaxOccursUnbounded(true);");
                    }
                    QName itemQName = elem.getType().getItemQName();
                    if (itemQName != null) {
                        pw.println("        elemField.setItemQName(" +
                                   Utils.getNewQName(itemQName) + ");");
                    }

                    pw.println("        typeDesc.addFieldDesc(elemField);");
                }
            }
        }

        pw.println("    }");
        pw.println();
        pw.println("    /**");
//        pw.println("     * " + Messages.getMessage("returnTypeMeta"));
        pw.println("     */");
        pw.println(
                "    public static org.apache.axis.description.TypeDesc getTypeDesc() {");
        pw.println("        return typeDesc;");
        pw.println("    }");
        pw.println();
    }

    /**
     * Utility function to get the bean property name (as will be returned
     * by the Introspector) for a given field name.  This just means
     * we capitalize the first character if the second character is
     * capitalized.  Example: a field named "fOO" will turn into
     * getter/setter methods "getFOO()/setFOO()".  So when the Introspector
     * looks at that bean, the property name will be "FOO", not "fOO" due
     * to the rules in the JavaBeans spec.  So this makes sure the
     * metadata will match. <p>
     *
     * The method also makes sure that the returned property name is not in
     * the set of reserved properties as defined by {@link #reservedPropNames}.     
     *  
     * @param fieldName 
     * @return 
     */
    private String getAsFieldName(String fieldName) {

        // If there's a second character, and it is uppercase, then the
        // bean property name will have a capitalized first character
        // (because setURL() maps to a property named "URL", not "uRL")
        if ((fieldName.length() > 1)
                && Character.isUpperCase(fieldName.charAt(1))) {
            fieldName = Utils.capitalizeFirstChar(fieldName);
        }

        // Make sure the property name is not reserved.
        return JavaUtils.getUniqueValue(reservedPropNames, fieldName);
    }

    /**
     * write Serializer getter code and pass in meta data to avoid
     * undo introspection.
     * 
     * @param pw 
     * @throws IOException 
     */
    protected void writeSerializer(PrintWriter pw) throws IOException {

        String typeDesc = "typeDesc";
        String ser = " org.apache.axis.encoding.ser.BeanSerializer";

        if (type.isSimpleType()) {
            ser = " org.apache.axis.encoding.ser.SimpleSerializer";
        }

        pw.println("    /**");
        pw.println("     * Get Custom Serializer");
        pw.println("     */");
        pw.println(
                "    public static org.apache.axis.encoding.Serializer getSerializer(");
        pw.println("           java.lang.String mechType, ");
        pw.println("           java.lang.Class _javaType,  ");
        pw.println("           javax.xml.namespace.QName _xmlType) {");
        pw.println("        return ");
        pw.println("          new " + ser + "(");
        pw.println("            _javaType, _xmlType, " + typeDesc + ");");
        pw.println("    }");
        pw.println();
    }

    /**
     * write Deserializer getter code and pass in meta data to avoid
     * undo introspection.
     * 
     * @param pw 
     * @throws IOException 
     */
    protected void writeDeserializer(PrintWriter pw) throws IOException {

        String typeDesc = "typeDesc";
        String dser = " org.apache.axis.encoding.ser.BeanDeserializer";

        if (type.isSimpleType()) {
            dser = " org.apache.axis.encoding.ser.SimpleDeserializer";
        }

        pw.println("    /**");
        pw.println("     * Get Custom Deserializer");
        pw.println("     */");
        pw.println(
                "    public static org.apache.axis.encoding.Deserializer getDeserializer(");
        pw.println("           java.lang.String mechType, ");
        pw.println("           java.lang.Class _javaType,  ");
        pw.println("           javax.xml.namespace.QName _xmlType) {");
        pw.println("        return ");
        pw.println("          new " + dser + "(");
        pw.println("            _javaType, _xmlType, " + typeDesc + ");");
        pw.println("    }");
        pw.println();
    }
}    // class JavaBeanHelperWriter
