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


import org.apache.axis2.databinding.symbolTable.TypeEntry;
import org.apache.axis2.databinding.utils.JavaUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Vector;

/**
 * This is Wsdl2java's Complex Type Writer.  It writes the <typeName>.java file.
 */
public class JavaEnumTypeWriter extends JavaClassWriter {

    /** Field elements */
    private Vector elements;

    /** Field type */
    private TypeEntry type;

    /**
     * Constructor.
     * 
     * @param emitter  
     * @param type     
     * @param elements 
     */
    protected JavaEnumTypeWriter(Emitter emitter, TypeEntry type,
                                 Vector elements) {

        super(emitter, type.getName(), "enumType");

        this.elements = elements;
        this.type = type;
    }    // ctor

    /**
     * Return "implements java.io.Serializable ".
     * 
     * @return 
     */
    protected String getImplementsText() {
        return "implements java.io.Serializable ";
    }    // getImplementsText

    /**
     * Generate the binding for the given enumeration type.
     * The values vector contains the base type (first index) and
     * the values (subsequent Strings)
     * 
     * @param pw 
     * @throws IOException 
     */
    protected void writeFileBody(PrintWriter pw) throws IOException {

        // Get the java name of the type
        String javaName = getClassName();

        // The first index is the base type.
        // The base type could be a non-object, if so get the corresponding Class.
        String baseType = ((TypeEntry) elements.get(0)).getName();
        String baseClass = baseType;

        if (baseType.indexOf("int") == 0) {
            baseClass = "java.lang.Integer";
        } else if (baseType.indexOf("char") == 0) {
            baseClass = "java.lang.Character";
        } else if (baseType.indexOf("short") == 0) {
            baseClass = "java.lang.Short";
        } else if (baseType.indexOf("long") == 0) {
            baseClass = "java.lang.Long";
        } else if (baseType.indexOf("double") == 0) {
            baseClass = "java.lang.Double";
        } else if (baseType.indexOf("float") == 0) {
            baseClass = "java.lang.Float";
        } else if (baseType.indexOf("byte") == 0) {
            baseClass = "java.lang.Byte";
        }

        // Create a list of the literal values.
        Vector values = new Vector();

        for (int i = 1; i < elements.size(); i++) {
            String value = (String) elements.get(i);

            if (baseClass.equals("java.lang.String")) {
                value = "\"" + value
                        + "\"";    // Surround literal with double quotes
            } else if (baseClass.equals("java.lang.Character")) {
                value = "'" + value + "'";
            } else if (baseClass.equals("java.lang.Float")) {
                if (!value.endsWith("F") && // Indicate float literal so javac
                        !value.endsWith(
                                "f")) {    // doesn't complain about precision.
                    value += "F";
                }
            } else if (baseClass.equals("java.lang.Long")) {
                if (!value.endsWith("L") && // Indicate float literal so javac
                        !value.endsWith(
                                "l")) {    // doesn't complain about precision.
                    value += "L";
                }
            } else if (baseClass.equals("javax.xml.namespace.QName")) {
                value = org.apache.axis2.databinding.symbolTable.Utils.getQNameFromPrefixedName(type.getNode(), value).toString();
                value = "javax.xml.namespace.QName.valueOf(\"" + value + "\")";
            } else if (baseClass.equals(baseType)) {

                // Construct baseClass object with literal string
                value = "new " + baseClass + "(\"" + value + "\")";
            }

            values.add(value);
        }

        // Create a list of ids
        Vector ids = getEnumValueIds(elements);

        // Each object has a private _value_ variable to store the base value
        pw.println("    private " + baseType + " _value_;");

        // The enumeration values are kept in a hashtable
        pw.println(
                "    private static java.util.HashMap _table_ = new java.util.HashMap();");
        pw.println("");

        // A protected constructor is used to create the static enumeration values
       // pw.println("    // " + Messages.getMessage("ctor00"));
        pw.println("    protected " + javaName + "(" + baseType + " value) {");
        pw.println("        _value_ = value;");

        if (baseClass.equals("java.lang.String")
                || baseClass.equals(baseType)) {
            pw.println("        _table_.put(_value_,this);");
        } else {
            pw.println("        _table_.put(new " + baseClass
                    + "(_value_),this);");
        }

        pw.println("    }");
        pw.println("");

        // A public static variable of the base type is generated for each enumeration value.
        // Each variable is preceded by an _.
        for (int i = 0; i < ids.size(); i++) {
            
            // Need to catch the checked MalformedURIException for URI base types
            if(baseType.equals("org.apache.axis.types.URI")) {
                pw.println("    public static final " + baseType + " _" + ids.get(i) + ";");
                pw.println("    static {");
                pw.println("    	try {");
                pw.println("            _" + ids.get(i) + " = " + values.get(i) + ";");
                pw.println("        }");
                pw.println("        catch (org.apache.axis.types.URI.MalformedURIException mue) {");
                pw.println("            throw new java.lang.RuntimeException(mue.toString());");
                pw.println("        }");
                pw.println("    }");
                pw.println("");
            }
            else {
                pw.println("    public static final " + baseType + " _"
                    + ids.get(i) + " = " + values.get(i) + ";");
            }
        }

        // A public static variable is generated for each enumeration value.
        for (int i = 0; i < ids.size(); i++) {
            pw.println("    public static final " + javaName + " " + ids.get(i)
                    + " = new " + javaName + "(_" + ids.get(i) + ");");
        }

        // Getter that returns the base value of the enumeration value
        pw.println("    public " + baseType + " getValue() { return _value_;}");

        // FromValue returns the unique enumeration value object from the table
        pw.println("    public static " + javaName + " fromValue(" + baseType
                + " value)");
        pw.println("          throws java.lang.IllegalArgumentException {");
        pw.println("        " + javaName + " enumeration = (" + javaName + ")");

        if (baseClass.equals("java.lang.String")
                || baseClass.equals(baseType)) {
            pw.println("            _table_.get(value);");
        } else {
            pw.println("            _table_.get(new " + baseClass
                    + "(value));");
        }

        pw.println(
                "        if (enumeration==null) throw new java.lang.IllegalArgumentException();");
        pw.println("        return enumeration;");
        pw.println("    }");

        // FromString returns the unique enumeration value object from a string representation
        pw.println("    public static " + javaName
                + " fromString(java.lang.String value)");
        pw.println("          throws java.lang.IllegalArgumentException {");

        if (baseClass.equals("java.lang.String")) {
            pw.println("        return fromValue(value);");
        } else if (baseClass.equals("javax.xml.namespace.QName")) {
            pw.println("        try {");
            pw.println("            return fromValue(javax.xml.namespace.QName.valueOf"
                    + "(value));");
            pw.println("        } catch (Exception e) {");
            pw.println(
                    "            throw new java.lang.IllegalArgumentException();");
            pw.println("        }");
        } else if (baseClass.equals(baseType)) {
            pw.println("        try {");
            pw.println("            return fromValue(new " + baseClass
                    + "(value));");
            pw.println("        } catch (Exception e) {");
            pw.println(
                    "            throw new java.lang.IllegalArgumentException();");
            pw.println("        }");
        } else if (baseClass.equals("java.lang.Character")) {
            pw.println("        if (value != null && value.length() == 1);");
            pw.println("            return fromValue(value.charAt(0));");
            pw.println(
                    "        throw new java.lang.IllegalArgumentException();");
        } else if (baseClass.equals("java.lang.Integer")) {
            pw.println("        try {");
            pw.println(
                    "            return fromValue(java.lang.Integer.parseInt(value));");
            pw.println("        } catch (Exception e) {");
            pw.println(
                    "            throw new java.lang.IllegalArgumentException();");
            pw.println("        }");
        } else {
            String parse = "parse"
                    + baseClass.substring(baseClass.lastIndexOf(".")
                    + 1);

            pw.println("        try {");
            pw.println("            return fromValue(" + baseClass + "."
                    + parse + "(value));");
            pw.println("        } catch (Exception e) {");
            pw.println(
                    "            throw new java.lang.IllegalArgumentException();");
            pw.println("        }");
        }

        pw.println("    }");

        // Equals == to determine equality value.
        // Since enumeration values are singletons, == is appropriate for equals()
        pw.println(
                "    public boolean equals(java.lang.Object obj) {return (obj == this);}");

        // Provide a reasonable hashCode method (hashCode of the string value of the enumeration)
        pw.println(
                "    public int hashCode() { return toString().hashCode();}");

        // toString returns a string representation of the enumerated value
        if (baseClass.equals("java.lang.String")) {
            pw.println(
                    "    public java.lang.String toString() { return _value_;}");
        } else if (baseClass.equals(baseType)) {
            pw.println(
                    "    public java.lang.String toString() { return _value_.toString();}");
        } else {
            pw.println(
                    "    public java.lang.String toString() { return java.lang.String.valueOf(_value_);}");
        }

        pw.println(
                "    public java.lang.Object readResolve() throws java.io.ObjectStreamException { return fromValue(_value_);}");
        pw.println(
                "    public static org.apache.axis.encoding.Serializer getSerializer(");
        pw.println("           java.lang.String mechType, ");
        pw.println("           java.lang.Class _javaType,  ");
        pw.println("           javax.xml.namespace.QName _xmlType) {");
        pw.println("        return ");
        pw.println(
                "          new org.apache.axis.encoding.ser.EnumSerializer(");
        pw.println("            _javaType, _xmlType);");
        pw.println("    }");
        pw.println(
                "    public static org.apache.axis.encoding.Deserializer getDeserializer(");
        pw.println("           java.lang.String mechType, ");
        pw.println("           java.lang.Class _javaType,  ");
        pw.println("           javax.xml.namespace.QName _xmlType) {");
        pw.println("        return ");
        pw.println(
                "          new org.apache.axis.encoding.ser.EnumDeserializer(");
        pw.println("            _javaType, _xmlType);");
        pw.println("    }");
        //pw.println("    // " + Messages.getMessage("typeMeta"));
        pw.println(
                "    private static org.apache.axis.description.TypeDesc typeDesc =");
        pw.println("        new org.apache.axis.description.TypeDesc("
                + Utils.getJavaLocalName(type.getName()) + ".class);");
        pw.println();
        pw.println("    static {");
        pw.println("        typeDesc.setXmlType("
                + Utils.getNewQName(type.getQName()) + ");");
        pw.println("    }");
        pw.println("    /**");
        //pw.println("     * " + Messages.getMessage("returnTypeMeta"));
        pw.println("     */");
        pw.println(
                "    public static org.apache.axis.description.TypeDesc getTypeDesc() {");
        pw.println("        return typeDesc;");
        pw.println("    }");
        pw.println();
    }    // writeFileBody

    /**
     * Get the enumeration names for the values.
     * The name is affected by whether all of the values of the enumeration
     * can be expressed as valid java identifiers.
     * 
     * @param bv Vector base and values vector from getEnumerationBaseAndValues
     * @return Vector names of enum value identifiers.
     */
    public static Vector getEnumValueIds(Vector bv) {

        boolean validJava = true;    // Assume all enum values are valid ids

        // Walk the values looking for invalid ids
        for (int i = 1; (i < bv.size()) && validJava; i++) {
            String value = (String) bv.get(i);

            if (!JavaUtils.isJavaId(value)) {
                validJava = false;
            }
        }

        // Build the vector of ids
        Vector ids = new Vector();

        for (int i = 1; i < bv.size(); i++) {

            // If any enum values are not valid java, then
            // all of the ids are of the form value<1..N>.
            if (!validJava) {
                ids.add("value" + i);
            } else {
                ids.add((String) bv.get(i));
            }
        }

        return ids;
    }

    /** Generate a java source file for enum class.
     * If the emitter works in deploy mode and the class already exists, the source wull not be generated.
     */
    public void generate() throws IOException {
        String fqcn = getPackage() + "." + getClassName();	
        if (emitter.isDeploy()) {
            if (!emitter.doesExist(fqcn)) {
                super.generate();
            }
        } else {
            super.generate();
        }
    }
}    // class JavaEnumTypeWriter
