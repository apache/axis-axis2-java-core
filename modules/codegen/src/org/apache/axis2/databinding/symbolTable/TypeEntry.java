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

//import org.apache.axis.utils.Messages;
import org.w3c.dom.Node;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Vector;

/**
 * This class represents a wsdl types entry that is supported by the WSDL2Java emitter.
 * A TypeEntry has a QName representing its XML name and a name, which in the
 * WSDL2Java back end is its full java name.  The TypeEntry may also have a Node,
 * which locates the definition of the emit type in the xml.
 * A TypeEntry object extends SymTabEntry and is built by the SymbolTable class for
 * each supported root complexType, simpleType, and elements that are
 * defined or encountered.
 * <p/>
 * SymTabEntry
 * |
 * TypeEntry
 * /           \
 * Type                Element
 * |                     |
 * (BaseType,                    (DefinedElement,
 * CollectionType                CollectionElement,
 * DefinedType,                  UndefinedElement)
 * UndefinedType)
 * <p/>
 * UndefinedType and UndefinedElement are placeholders when the real type or element
 * is not encountered yet.  Both of these implement the Undefined interface.
 * <p/>
 * A TypeEntry whose java (or other language) name depends on an Undefined type, will
 * have its name initialization deferred until the Undefined type is replaced with
 * a defined type.  The updateUndefined() method is invoked by the UndefinedDelegate to
 * update the information.
 * <p/>
 * Each TypeEntry whose language name depends on another TypeEntry will have the refType
 * field set.  For example:
 * <element name="foo" type="bar" />
 * The TypeEntry for "foo" will have a refType set to the TypeEntry of "bar".
 * <p/>
 * Another Example:
 * <xsd:complexType name="hobbyArray">
 * <xsd:complexContent>
 * <xsd:restriction base="soapenc:Array">
 * <xsd:attribute ref="soapenc:arrayType" wsdl:arrayType="xsd:string[]"/>
 * </xsd:restriction>
 * </xsd:complexContent>
 * </xsd:complexType>
 * The TypeEntry for "hobbyArray" will have a refType that locates the TypeEntry for xsd:string
 * and the dims field will be "[]"
 * 
 * @author Rich Scheuerle  (scheu@us.ibm.com)
 */
public abstract class TypeEntry extends SymTabEntry implements Serializable {

    /** Field node */
    protected Node node;                               // Node

    /** Field refType */
    protected TypeEntry refType;                       // Some TypeEntries refer to other types.

    /** Field dims */
    protected String dims = "";                        // If refType is an element, dims indicates

                                                       // the array dims (for example "[]").

    protected QName componentType = null;              // If this is an array, the component type

    /** If this TypeEntry represents an array with elements inside a "wrapper"
     * this field can optionally change the inner QName (default is <item>).
     */
    protected QName itemQName = null;

    /** Field undefined */
    protected boolean undefined;                       // If refType is an Undefined type

    // (or has a refType that is Undefined)
    // then the undefined flag is set.
    // The name cannot be determined
    // until the Undefined type is found.

    /** Field isBaseType */
    protected boolean isBaseType;                      // Indicates if represented by a

    // primitive or util class

    /** Field isSimpleType */
    protected boolean isSimpleType =
            false;                                         // Indicates if this type is a simple type

    /** Field onlyLiteralReference */
    protected boolean onlyLiteralReference = false;    // Indicates
    
    /** Field types */
    protected HashSet types = null;
    
    /** contained elements in the schema's type definition */
    protected Vector containedElements;

    /** contained attributes in the schema's type definition */
    protected Vector containedAttributes;

    // whether this type is only referenced
    // via a binding's literal use.

    /**
     * Create a TypeEntry object for an xml construct that references another type.
     * Defer processing until refType is known.
     * 
     * @param pqName  
     * @param refType 
     * @param pNode   
     * @param dims    
     */
    protected TypeEntry(QName pqName, TypeEntry refType, Node pNode,
                        String dims) {

        super(pqName);

        node = pNode;
        this.undefined = refType.undefined;
        this.refType = refType;

        if (dims == null) {
            dims = "";
        }

        this.dims = dims;

        if (refType.undefined) {

            // Need to defer processing until known.
            TypeEntry uType = refType;

            while (!(uType instanceof Undefined)) {
                uType = uType.refType;
            }

            ((Undefined) uType).register(this);
        } else {
            isBaseType = (refType.isBaseType && refType.dims.equals("")
                    && dims.equals(""));
        }
    }

    /**
     * Create a TypeEntry object for an xml construct that is not a base type
     * 
     * @param pqName 
     * @param pNode  
     */
    protected TypeEntry(QName pqName, Node pNode) {

        super(pqName);

        node = pNode;
        refType = null;
        undefined = false;
        dims = "";
        isBaseType = false;
    }

    /**
     * Create a TypeEntry object for an xml construct name that represents a base type
     * 
     * @param pqName 
     */
    protected TypeEntry(QName pqName) {

        super(pqName);

        node = null;
        undefined = false;
        dims = "";
        isBaseType = true;
    }

    /**
     * Query the node for this type.
     * 
     * @return 
     */
    public Node getNode() {
        return node;
    }

    /**
     * Returns the Base Type Name.
     * For example if the Type represents a schema integer, "int" is returned.
     * If this is a user defined type, null is returned.
     * 
     * @return 
     */
    public String getBaseType() {

        if (isBaseType) {
            return name;
        } else {
            return null;
        }
    }

    /**
     * Method isBaseType
     * 
     * @return 
     */
    public boolean isBaseType() {
        return isBaseType;
    }

    /**
     * Method setBaseType
     * 
     * @param baseType 
     */
    public void setBaseType(boolean baseType) {
        isBaseType = baseType;
    }
    
    /**
     * Method isSimpleType
     * 
     * @return 
     */
    public boolean isSimpleType() {
        return isSimpleType;
    }

    /**
     * Method setSimpleType
     * 
     * @param simpleType 
     */
    public void setSimpleType(boolean simpleType) {
        isSimpleType = simpleType;
    }

    /**
     * Is this type references ONLY as a literal type?  If a binding's
     * message's soapBody says:  use="literal", then a type is referenced
     * literally.  Note that that type's contained types (ie., an address
     * contains a phone#) are not referenced literally.  Since a type
     * that is ONLY referenced as a literal may cause a generator to act
     * differently (like WSDL2Java), this extra reference distinction is
     * needed.
     * 
     * @return 
     */
    public boolean isOnlyLiteralReferenced() {
        return onlyLiteralReference;
    }    // isOnlyLiteralReferenced

    /**
     * Set the isOnlyLiteralReference flag.
     * 
     * @param set 
     */
    public void setOnlyLiteralReference(boolean set) {
        onlyLiteralReference = set;
    }    // setOnlyLiteralRefeerence

    /**
     * getUndefinedTypeRef returns the Undefined TypeEntry that this entry depends on or NULL.
     * 
     * @return 
     */
    protected TypeEntry getUndefinedTypeRef() {

        if (this instanceof Undefined) {
            return this;
        }

        if (undefined && (refType != null)) {
            if (refType.undefined) {
                TypeEntry uType = refType;

                while (!(uType instanceof Undefined)) {
                    uType = uType.refType;
                }

                return uType;
            }
        }

        return null;
    }

    /**
     * UpdateUndefined is called when the ref TypeEntry is finally known.
     * 
     * @param oldRef The TypeEntry representing the Undefined TypeEntry
     * @param newRef The replacement TypeEntry
     * @return true if TypeEntry is changed in any way.
     * @throws IOException 
     */
    protected boolean updateUndefined(TypeEntry oldRef, TypeEntry newRef)
            throws IOException {

        boolean changedState = false;

        // Replace refType with the new one if applicable
        if (refType == oldRef) {
            refType = newRef;
            changedState = true;

            // Detect a loop
            TypeEntry te = refType;

            while ((te != null) && (te != this)) {
                te = te.refType;
            }

            if (te == this) {

                // Detected a loop.
                undefined = false;
                isBaseType = false;
                node = null;

                throw new IOException( );
//                        Messages.getMessage(
//                                "undefinedloop00", getQName().toString()));
            }
        }

        // Update information if refType is now defined
        if ((refType != null) && undefined && (refType.undefined == false)) {
            undefined = false;
            changedState = true;
            isBaseType = (refType.isBaseType && refType.dims.equals("")
                    && dims.equals(""));
        }

        return changedState;
    }

    /**
     * If this type references another type, return that type, otherwise return null.
     * 
     * @return 
     */
    public TypeEntry getRefType() {
        return refType;
    }    // getRefType

    /**
     * Method setRefType
     * 
     * @param refType 
     */
    public void setRefType(TypeEntry refType) {
        this.refType = refType;
    }

    /**
     * Return the dimensions of this type, which can be 0 or more "[]".
     * 
     * @return 
     */
    public String getDimensions() {
        return dims;
    }    // getDimensions

    /**
     * Return the QName of the component if this is an array type
     * @return QName of array elements or null
     */
    public QName getComponentType()
    {
        return componentType;
    }

    /**
     * Set the QName of the component if this is an array type
     */ 
    public void setComponentType(QName componentType)
    {
        this.componentType = componentType;
    }

    public QName getItemQName() {
        return itemQName;
    }

    public void setItemQName(QName itemQName) {
        this.itemQName = itemQName;
    }

    /**
     * Get string representation.
     * 
     * @return 
     */
    public String toString() {
        return toString("");
    }

    /**
     * Get string representation with indentation
     * 
     * @param indent 
     * @return 
     */
    protected String toString(String indent) {

        String refString = indent + "RefType:       null \n";

        if (refType != null) {
            refString = indent + "RefType:\n" + refType.toString(indent + "  ")
                    + "\n";
        }

        return super.toString(indent) 
                + indent + "Class:         " + this.getClass().getName() + "\n" 
                + indent + "Base?:         " + isBaseType + "\n" 
                + indent + "Undefined?:    " + undefined + "\n" 
                + indent + "isSimpleType?  " + isSimpleType + "\n"
                + indent + "Node:          " + getNode() + "\n" 
                + indent + "Dims:          " + dims + "\n"              
                + indent + "isOnlyLiteralReferenced: " + onlyLiteralReference + "\n"
                + refString;
    }

    /**
     * This method returns a set of all the nested types.
     * Nested types are types declared within this TypeEntry (or descendents)
     * plus any extended types and the extended type nested types
     * The elements of the returned HashSet are Types.
     * 
     * @param symbolTable is the symbolTable
     * @param derivedFlag should be set if all dependendent derived types should also be
     *                    returned.
     * @return 
     */
    public HashSet getNestedTypes(SymbolTable symbolTable,
                                         boolean derivedFlag) {
        if( types == null) {
            types = Utils.getNestedTypes(this, symbolTable, derivedFlag);
        }
        return types;
    }    // getNestedTypes

    /**
     * @return Returns the containedAttributes.
     */
    public Vector getContainedAttributes() {
        return containedAttributes;
    }
    /**
     * @param containedAttributes The containedAttributes to set.
     */
    public void setContainedAttributes(Vector containedAttributes) {
        this.containedAttributes = containedAttributes;
    }
    /**
     * @return Returns the containedElements.
     */
    public Vector getContainedElements() {
        return containedElements;
    }
    /**
     * @param containedElements The containedElements to set.
     */
    public void setContainedElements(Vector containedElements) {
        this.containedElements = containedElements;
    }
}
