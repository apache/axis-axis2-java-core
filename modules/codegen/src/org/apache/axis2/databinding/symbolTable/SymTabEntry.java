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
package org.apache.axis2.databinding.symbolTable;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * SymTabEntry is the base class for all symbol table entries.  It contains four things:
 * - a QName
 * - space for a Writer-specific name (for example, in Wsdl2java, this will be the Java name)
 * - isReferenced flag indicating whether this entry is referenced by other entries
 * - dynamicVars; a mechanism for Writers to add additional context information onto entries.
 */
public abstract class SymTabEntry {

    // The QName of this entry is immutable.  There is no setter for  it.

    /** Field qname */
    protected QName qname;

    // The name is Writer implementation dependent.  For example, in Wsdl2java, this will become
    // the Java name.

    /** Field name */
    protected String name;

    // Is this entry referenced by any other entry?

    /** Field isReferenced */
    private boolean isReferenced = false;

    /** Field dynamicVars */
    private HashMap dynamicVars = new HashMap();

    /**
     * Construct a symbol table entry with the given QName.
     * 
     * @param qname 
     */
    protected SymTabEntry(QName qname) {
        this.qname = qname;
    }    // ctor

    /**
     * Get the QName of this entry.
     * 
     * @return 
     */
    public final QName getQName() {
        return qname;
    }    // getQName

    /**
     * Get the name of this entry.  The name is Writer-implementation-dependent.  For example, in
     * Wsdl2java, this will become the Java name.
     * 
     * @return 
     */
    public String getName() {
        return name;
    }    // getName

    /**
     * Set the name of this entry.  This method is not called by the framework, it is only called
     * by the Writer implementation.
     * 
     * @param name 
     */
    public void setName(String name) {
        this.name = name;
    }    // setName

    /**
     * Is this entry referenced by any other entry in the symbol table?
     * 
     * @return 
     */
    public final boolean isReferenced() {
        return isReferenced;
    }    // isReferenced

    /**
     * Set the isReferenced variable, default value is true.
     * 
     * @param isReferenced 
     */
    public final void setIsReferenced(boolean isReferenced) {
        this.isReferenced = isReferenced;
    }    // setIsReferenced

    /**
     * There may be information that does not exist in WSDL4J/DOM
     * structures and does not exist in
     * our additional structures, but that Writer implementation
     * will need.  This information is
     * most likely context-relative, so the DynamicVar map is
     * provided for the Writers to store and
     * retrieve their particular information.
     * 
     * @param key 
     * @return 
     */
    public Object getDynamicVar(Object key) {
        return dynamicVars.get(key);
    }    // getDynamicVar

    /**
     * Method setDynamicVar
     * 
     * @param key   
     * @param value 
     */
    public void setDynamicVar(Object key, Object value) {
        dynamicVars.put(key, value);
    }    // setDynamicVar

    /**
     * Collate the info in this object in string form.
     * 
     * @return 
     */
    public String toString() {
        return toString("");
    }    // toString

    /**
     * Collate the info in this object in string form with indentation.
     * 
     * @param indent 
     * @return 
     */
    protected String toString(String indent) {

        String string = indent + "QName:         " + qname + '\n' + indent
                + "name:          " + name + '\n' + indent
                + "isReferenced?  " + isReferenced + '\n';
        String prefix = indent + "dynamicVars:   ";
        Iterator entries = dynamicVars.entrySet().iterator();

        while (entries.hasNext()) {
            Map.Entry entry = (Map.Entry) entries.next();
            Object key = entry.getKey();

            string += prefix + key + " = " + entry.getValue() + '\n';
            prefix = indent + "               ";
        }

        return string;
    }    // toString
}    // abstract class SymTabEntry
