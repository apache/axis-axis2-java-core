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


import org.apache.axis2.wsdl.WSDLVersionWrapper;
import org.apache.axis2.databinding.Constants;
import org.apache.axis2.databinding.utils.support.BooleanHolder;
import org.apache.axis2.databinding.utils.support.IntHolder;
import org.apache.axis2.databinding.utils.support.QNameHolder;
import org.apache.wsdl.WSDLDescription;
import org.apache.wsdl.WSDLTypes;
import org.apache.wsdl.extensions.Schema;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

/**
 * This class represents a table of all of the top-level symbols from a set of WSDL Definitions and
 * DOM Documents:  XML types; WSDL messages, portTypes, bindings, and services.
 * <p/>
 * This symbolTable contains entries of the form <key, value> where key is of type QName and value is
 * of type Vector.  The Vector's elements are all of the objects that have the given QName.  This is
 * necessary since names aren't unique among the WSDL types.  message, portType, binding, service,
 * could all have the same QName and are differentiated merely by type.  SymbolTable contains
 * type-specific getters to bypass the Vector layer:
 * public PortTypeEntry getPortTypeEntry(QName name), etc.
 */
public class SymbolTable {

    // used to cache dervied types
    protected HashMap derivedTypes = new HashMap();

    // Should the contents of imported files be added to the symbol table?

    /** Field addImports */
    private boolean addImports;


    /** Field symbolTable */
    private HashMap symbolTable = new HashMap();

    // a map of qnames -> Elements in the symbol table

    /** Field elementTypeEntries */
    private final Map elementTypeEntries = new HashMap();

    // an unmodifiable wrapper so that we can share the index with others, safely

    /** Field elementIndex */
    private final Map elementIndex =
            Collections.unmodifiableMap(elementTypeEntries);

    // a map of qnames -> Types in the symbol table

    /** Field typeTypeEntries */
    private final Map typeTypeEntries = new HashMap();

    // an unmodifiable wrapper so that we can share the index with others, safely

    /** Field typeIndex */
    private final Map typeIndex = Collections.unmodifiableMap(typeTypeEntries);

    /**
     * cache of nodes -> base types for complexTypes.  The cache is
     * built on nodes because multiple TypeEntry objects may use the
     * same node.
     */
    protected final Map node2ExtensionBase =
            new HashMap();    // allow friendly access


    /** Field quiet */
    protected boolean quiet;

    /** Field btm */
    private BaseTypeMapping btm = null;

    /** Field ANON_TOKEN */
    public static final String ANON_TOKEN = ">";


    /** Field wsdlURI */
    private String wsdlURI = null;

    /** If this is false, we will "unwrap" literal arrays, generating a plan "String[]" instead
     * of "ArrayOfString" when encountering an element containing a single maxOccurs="unbounded"
     * inner element.
     */
    private boolean wrapArrays;

    Set arrayTypeQNames = new HashSet();

    /**
     * Construct a symbol table with the given Namespaces.
     * 
     * @param btm
     * @param addImports
     * @param verbose
     * @param nowrap
     */
    public SymbolTable(BaseTypeMapping btm, boolean addImports ) {

        this.btm = btm;
        this.addImports = addImports;

    }    // ctor

    /**
     * Method isQuiet
     * 
     * @return
     */
    public boolean isQuiet() {
        return quiet;
    }

    /**
     * Method setQuiet
     * 
     * @param quiet
     */
    public void setQuiet(boolean quiet) {
        this.quiet = quiet;
    }

    /**
     * Get the raw symbol table HashMap.
     * 
     * @return
     */
    public HashMap getHashMap() {
        return symbolTable;
    }    // getHashMap

    /**
     * Get the list of entries with the given QName.  Since symbols can share QNames, this list is
     * necessary.  This list will not contain any more than one element of any given SymTabEntry.
     * 
     * @param qname
     * @return
     */
    public Vector getSymbols(QName qname) {
        return (Vector) symbolTable.get(qname);
    }    // get

    /**
     * Get the entry with the given QName of the given class.  If it does not exist, return null.
     * 
     * @param qname
     * @param cls
     * @return
     */
    public SymTabEntry get(QName qname, Class cls) {

        Vector v = (Vector) symbolTable.get(qname);

        if (v == null) {
            return null;
        } else {
            for (int i = 0; i < v.size(); ++i) {
                SymTabEntry entry = (SymTabEntry) v.elementAt(i);

                if (cls.isInstance(entry)) {
                    return entry;
                }
            }

            return null;
        }
    }    // get

    /**
     * Get the type entry for the given qname.
     * 
     * @param qname
     * @param wantElementType boolean that indicates type or element (for type= or ref=)
     * @return
     */
    public TypeEntry getTypeEntry(QName qname, boolean wantElementType) {

        if (wantElementType) {
            return getElement(qname);
        } else {
            return getType(qname);
        }
    }    // getTypeEntry

    /**
     * Get the Type TypeEntry with the given QName.  If it doesn't
     * exist, return null.
     * 
     * @param qname
     * @return
     */
    public Type getType(QName qname) {
        return (Type) typeTypeEntries.get(qname);
    }    // getType

    /**
     * Get the Element TypeEntry with the given QName.  If it doesn't
     * exist, return null.
     * 
     * @param qname
     * @return
     */
    public Element getElement(QName qname) {
        return (Element) elementTypeEntries.get(qname);
    }    // getElement



    /**
     * Get the list of all the XML schema types in the symbol table.  In other words, all entries
     * that are instances of TypeEntry.
     * 
     * @return
     * @deprecated use specialized get{Element,Type}Index() methods instead
     */
    public Vector getTypes() {

        Vector v = new Vector();

        v.addAll(elementTypeEntries.values());
        v.addAll(typeTypeEntries.values());

        return v;
    }    // getTypes

    /**
     * Return an unmodifiable map of qnames -> Elements in the symbol
     * table.
     * 
     * @return an unmodifiable <code>Map</code> value
     */
    public Map getElementIndex() {
        return elementIndex;
    }

    /**
     * Return an unmodifiable map of qnames -> Elements in the symbol
     * table.
     * 
     * @return an unmodifiable <code>Map</code> value
     */
    public Map getTypeIndex() {
        return typeIndex;
    }

    /**
     * Return the count of TypeEntries in the symbol table.
     * 
     * @return an <code>int</code> value
     */
    public int getTypeEntryCount() {
        return elementTypeEntries.size() + typeTypeEntries.size();
    }



    /**
     * Get the WSDL URI.  The WSDL URI is null until populate
     * is called, and ONLY if a WSDL URI is provided.
     * 
     * @return
     */
    public String getWSDLURI() {
        return wsdlURI;
    }    // getWSDLURI



    /**
     * Dump the contents of the symbol table.  For debugging purposes only.
     * 
     * @param out
     */
    public void dump(java.io.PrintStream out) {
        out.println("-----------------------");
        Iterator it = symbolTable.values().iterator();

        while (it.hasNext()) {
            Vector v = (Vector) it.next();

            for (int i = 0; i < v.size(); ++i) {
                out.println(v.elementAt(i).getClass().getName());
                out.println(v.elementAt(i));
            }
        }

        out.println("-----------------------");
    }    // dump


    /**
     *
     * @param versionWrapper
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    public void populate(WSDLVersionWrapper versionWrapper)
            throws IOException, SAXException, ParserConfigurationException {
        if (versionWrapper.getDescription()!=null) {
            populate(versionWrapper.getDescription());
        }
    }    // populate


    /**
     *
     * @param description
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    public void populate(WSDLDescription description)
            throws IOException, SAXException, ParserConfigurationException {

        //go through the types section and populate the types
        WSDLTypes types = description.getTypes();
        List elementsList = types.getExtensibilityElements();
        for (int i = 0; i < elementsList.size(); i++) {
            Object o =  elementsList.get(i);
            if (o instanceof Schema){
                populateTypes(null, ((Schema)o).getElement());
            }else{
                //ignore the non-schema elements
            }

        }

    }    // populate








    /**
     * This is essentially a call to "new URL(contextURL, spec)" with extra handling in case spec is
     * a file.
     * 
     * @param contextURL
     * @param spec
     * @return
     * @throws IOException
     */
    private static URL getURL(URL contextURL, String spec) throws IOException {

        // First, fix the slashes as windows filenames may have backslashes
        // in them, but the URL class wont do the right thing when we later
        // process this URL as the contextURL.
        String path = spec.replace('\\', '/');

        // See if we have a good URL.
        URL url = null;

        try {

            // first, try to treat spec as a full URL
            url = new URL(contextURL, path);

            // if we are deail with files in both cases, create a url
            // by using the directory of the context URL.
            if ((contextURL != null) && url.getProtocol().equals("file")
                    && contextURL.getProtocol().equals("file")) {
                url = getFileURL(contextURL, path);
            }
        } catch (MalformedURLException me) {

            // try treating is as a file pathname
            url = getFileURL(contextURL, path);
        }

        // Everything is OK with this URL, although a file url constructed
        // above may not exist.  This will be caught later when the URL is
        // accessed.
        return url;
    }    // getURL

    /**
     * Method getFileURL
     * 
     * @param contextURL
     * @param path
     * @return
     * @throws IOException
     */
    private static URL getFileURL(URL contextURL, String path)
            throws IOException {

        if (contextURL != null) {

            // get the parent directory of the contextURL, and append
            // the spec string to the end.
            String contextFileName = contextURL.getFile();
            URL parent = null;
            File parentFile = new File(contextFileName).getParentFile();
            if ( parentFile != null ) {
                parent = parentFile.toURL();
            }
            if (parent != null) {
                return new URL(parent, path);
            }
        }

        return new URL("file", "", path);
    }    // getFileURL

    /**
     * Check if this is a known namespace (soap-enc or schema xsd or schema xsi or xml)
     * 
     * @param namespace
     * @return true if this is a know namespace.
     */
    public boolean isKnownNamespace(String namespace) {

        if (Constants.isSOAP_ENC(namespace)) {
            return true;
        }

        if (Constants.isSchemaXSD(namespace)) {
            return true;
        }

        if (Constants.isSchemaXSI(namespace)) {
            return true;
        }

        if (namespace.equals(Constants.NS_URI_XML)) {
            return true;
        }

        return false;
    }

    /**
     * Populate the symbol table with all of the Types from the Document.
     * 
     * @param context
     * @param doc
     * @throws IOException
     * @throws SAXException
     * @throws WSDLException
     * @throws ParserConfigurationException
     */
    public void populateTypes(URL context, org.w3c.dom.Element doc)
            throws IOException, SAXException,
            ParserConfigurationException {
        addTypes(context, doc, ABOVE_SCHEMA_LEVEL);
    }    // populateTypes

    /**
     * Utility method which walks the Document and creates Type objects for
     * each complexType, simpleType, attributeGroup or element referenced or defined.
     * <p/>
     * What goes into the symbol table?  In general, only the top-level types
     * (ie., those just below
     * the schema tag).  But base types and references can
     * appear below the top level.  So anything
     * at the top level is added to the symbol table,
     * plus non-Element types (ie, base and refd)
     * that appear deep within other types.
     */
    private static final int ABOVE_SCHEMA_LEVEL = -1;

    /** Field SCHEMA_LEVEL */
    private static final int SCHEMA_LEVEL = 0;

    /**
     * Method addTypes
     * 
     * @param context
     * @param node
     * @param level
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws WSDLException
     * @throws SAXException
     */
    private void addTypes(URL context, Node node, int level)
            throws IOException, ParserConfigurationException,
            SAXException {

        if (node == null) {
            return;
        }

        // Get the kind of node (complexType, wsdl:part, etc.)
        String localPart = node.getLocalName();

        if (localPart != null) {
            boolean isXSD =
                    Constants.isSchemaXSD(node.getNamespaceURI());

            if (((isXSD && localPart.equals("complexType"))
                    || localPart.equals("simpleType"))) {

                // If an extension or restriction is present,
                // create a type for the reference
                Node re = SchemaUtils.getRestrictionOrExtensionNode(node);

                if ((re != null) && (Utils.getAttribute(re, "base") != null)) {
                    createTypeFromRef(re);
                }

                Node list = SchemaUtils.getListNode(node);
                if (list != null && Utils.getAttribute(list,"itemType") != null) {
                    createTypeFromRef(list);
                }

                Node union = SchemaUtils.getUnionNode(node);
                if (union != null) {
                    QName [] memberTypes = Utils.getMemberTypeQNames(union);
                    if (memberTypes != null) {
                        for (int i=0;i<memberTypes.length;i++) {
                            if (SchemaUtils.isSimpleSchemaType(memberTypes[i]) &&
                                    getType(memberTypes[i]) == null) {
                                symbolTablePut(new BaseType(memberTypes[i]));
                            }
                        }
                    }
                }

                // This is a definition of a complex type.
                // Create a Type.
                createTypeFromDef(node, false, false);
            } else if (isXSD && localPart.equals("element")) {

                // Create a type entry for the referenced type
                createTypeFromRef(node);

                // If an extension or restriction is present,
                // create a type for the reference
                Node re = SchemaUtils.getRestrictionOrExtensionNode(node);

                if ((re != null) && (Utils.getAttribute(re, "base") != null)) {
                    createTypeFromRef(re);
                }

                // Create a type representing an element.  (This may
                // seem like overkill, but is necessary to support ref=
                // and element=.
                createTypeFromDef(node, true, level > SCHEMA_LEVEL);
            } else if (isXSD && localPart.equals("attributeGroup")) {

                // bug 23145: support attributeGroup (Brook Richan)
                // Create a type entry for the referenced type
                createTypeFromRef(node);

                // Create a type representing an attributeGroup.
                createTypeFromDef(node, false, level > SCHEMA_LEVEL);
            }  else if (isXSD && localPart.equals("group")) {
                // Create a type entry for the referenced type
                createTypeFromRef(node);
                // Create a type representing an group
                createTypeFromDef(node, false, level > SCHEMA_LEVEL);
            } else if (isXSD && localPart.equals("attribute")) {
                //todo this is disabled for now
//                // Create a type entry for the referenced type
//                BooleanHolder forElement = new BooleanHolder();
//                QName refQName = Utils.getTypeQName(node, forElement,
//                        false);
//
//                if ((refQName != null) && !forElement.value) {
//                    createTypeFromRef(node);
//
//                    // Get the symbol table entry and make sure it is a simple
//                    // type
//                    if (refQName != null) {
//                        TypeEntry refType = getTypeEntry(refQName, false);
//
//                        if ((refType != null)
//                                && (refType instanceof Undefined)) {
//
//                            // Don't know what the type is.
//                            // It better be simple so set it as simple
//                            refType.setSimpleType(true);
//                        } else if ((refType == null)
//                                || (!(refType instanceof BaseType)
//                                && !refType.isSimpleType())) {
//
//                            // Problem if not simple
//                            throw new IOException();
////                                    Messages.getMessage(
////                                            "AttrNotSimpleType01",
////                                            refQName.toString()));
//                        }
//                    }
//                }
//                createTypeFromDef(node, true, level > SCHEMA_LEVEL);
            } else if (isXSD && localPart.equals("any")) {

                // Map xsd:any element to special xsd:any "type"
                if (getType(Constants.XSD_ANY) == null) {
                    Type type = new BaseType(Constants.XSD_ANY);

                    symbolTablePut(type);
                }
            } else if (localPart.equals("part")
                    && Constants.isWSDL(node.getNamespaceURI())) {

                // This is a wsdl part.  Create an TypeEntry representing the reference
                createTypeFromRef(node);
            } else if (isXSD && localPart.equals("include")) {
               //todo This is also disabled for now
//                String includeName = Utils.getAttribute(node, "schemaLocation");
//
//                if (includeName != null) {
//                    URL url = getURL(context, includeName);
//                    Document includeDoc = XMLUtils.newDocument(url.toString());
//
//                    // Vidyanand : Fix for Bug #15124
//                    org.w3c.dom.Element schemaEl =
//                            includeDoc.getDocumentElement();
//
//                    if (!schemaEl.hasAttribute("targetNamespace")) {
//                        org.w3c.dom.Element parentSchemaEl =
//                                (org.w3c.dom.Element) node.getParentNode();
//
//                        if (parentSchemaEl.hasAttribute("targetNamespace")) {
//
//                            // we need to set two things in here
//                            // 1. targetNamespace
//                            // 2. setup the xmlns=<targetNamespace> attribute
//                            String tns =
//                                    parentSchemaEl.getAttribute("targetNamespace");
//
//                            schemaEl.setAttribute("targetNamespace", tns);
//                            schemaEl.setAttribute("xmlns", tns);
//                        }
//                    }
//
//                    populate(url, null, includeDoc, url.toString());
//                }
            }
        }

        if (level == ABOVE_SCHEMA_LEVEL) {
            if ((localPart != null)
                    && localPart.equals("schema")) {
                level = SCHEMA_LEVEL;
            }
        } else {
            ++level;
        }

        // Recurse through children nodes
        NodeList children = node.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            addTypes(context, children.item(i), level);
        }
    }    // addTypes

    /**
     * Create a TypeEntry from the indicated node, which defines a type
     * that represents a complexType, simpleType or element (for ref=).
     * 
     * @param node
     * @param isElement
     * @param belowSchemaLevel
     * @throws IOException
     */
    private void createTypeFromDef(
            Node node, boolean isElement, boolean belowSchemaLevel)
            throws IOException {

        // Get the QName of the node's name attribute value
        QName qName = Utils.getNodeNameQName(node);

        if (qName != null) {

            // If the qname is already registered as a base type,
            // don't create a defining type/element.
            if (!isElement && (btm.getBaseName(qName) != null)) {
                return;
            }

            // If the node has a type or ref attribute, get the
            // qname representing the type
            BooleanHolder forElement = new BooleanHolder();
            QName refQName = Utils.getTypeQName(node, forElement,
                    false);

            if (refQName != null) {

                // Error check - bug 12362
                if (qName.getLocalPart().length() == 0) {
                    String name = Utils.getAttribute(node, "name");

                    if (name == null) {
                        name = "unknown";
                    }

                    throw new IOException();//Messages.getMessage("emptyref00",
//                            name));
                }

                // Now get the TypeEntry
                TypeEntry refType = getTypeEntry(refQName, forElement.value);

                if (!belowSchemaLevel) {
                    if (refType == null) {
                        throw new IOException( );
//                                Messages.getMessage(
//                                        "absentRef00", refQName.toString(),
//                                        qName.toString()));
                    }

                    symbolTablePut(new DefinedElement(qName, refType, node,
                            ""));
                }
            } else {

                // Flow to here indicates no type= or ref= attribute.
                // See if this is an array or simple type definition.
                IntHolder numDims = new IntHolder();

                // If we're supposed to unwrap arrays, supply someplace to put the "inner" QName
                // so we can propagate it into the appropriate metadata container.
                QNameHolder itemQName = wrapArrays ? null : new QNameHolder();

                numDims.value = 0;

                QName arrayEQName =
                        SchemaUtils.getArrayComponentQName(node,
                                numDims,
                                itemQName,
                                this);

                if (arrayEQName != null) {

                    // Get the TypeEntry for the array element type
                    refQName = arrayEQName;

                    TypeEntry refType = getTypeEntry(refQName, false);

                    if (refType == null) {
//                        arrayTypeQNames.add(refQName);

                        // Not defined yet, add one
                        String baseName = btm.getBaseName(refQName);

                        if (baseName != null) {
                            refType = new BaseType(refQName);
                        } else {
                            refType = new UndefinedType(refQName);
                        }

                        symbolTablePut(refType);
                    }

                    // Create a defined type or element that references refType
                    String dims = "";

                    while (numDims.value > 0) {
                        dims += "[]";

                        numDims.value--;
                    }

                    TypeEntry defType = null;

                    if (isElement) {
                        if (!belowSchemaLevel) {
                            defType =
                                    new DefinedElement(qName, refType, node, dims);
                            // Save component type for ArraySerializer
                            defType.setComponentType(arrayEQName);
                            if (itemQName != null)
                                defType.setItemQName(itemQName.value);
                        }
                    } else {
                        defType = new DefinedType(qName, refType, node, dims);
                        // Save component type for ArraySerializer
                        defType.setComponentType(arrayEQName);
                        if (itemQName != null)
                            defType.setItemQName(itemQName.value);
                    }

                    if (defType != null) {
                        symbolTablePut(defType);
                    }
                } else {

                    // Create a TypeEntry representing this  type/element
                    String baseName = btm.getBaseName(qName);

                    if (baseName != null) {
                        symbolTablePut(new BaseType(qName));
                    } else {

                        // Create a type entry, set whether it should
                        // be mapped as a simple type, and put it in the
                        // symbol table.
                        TypeEntry te = null;
                        TypeEntry parentType = null;

                        if (!isElement) {
                            te = new DefinedType(qName, node);

                            // check if we are an anonymous type underneath
                            // an element.  If so, we point the refType of the
                            // element to us (the real type).
                            if (qName.getLocalPart().indexOf(ANON_TOKEN) >= 0) {
                                Node parent = node.getParentNode();
                                QName parentQName =
                                        Utils.getNodeNameQName(parent);
                                parentType = getElement(parentQName);
                            }
                        } else {
                            if (!belowSchemaLevel) {
                                te = new DefinedElement(qName, node);
                            }
                        }

                        if (te != null) {
                            if (SchemaUtils.isSimpleTypeOrSimpleContent(node)) {
                                te.setSimpleType(true);
                            }
                            te = (TypeEntry)symbolTablePut(te);

                            if (parentType != null) {
                                parentType.setRefType(te);
                            }
                        }
                    }
                }
            }
        }
    }    // createTypeFromDef

    /**
     * Node may contain a reference (via type=, ref=, or element= attributes) to
     * another type.  Create a Type object representing this referenced type.
     * 
     * @param node
     * @throws IOException
     */
    private void createTypeFromRef(Node node) throws IOException {

        // Get the QName of the node's type attribute value
        BooleanHolder forElement = new BooleanHolder();
        QName qName = Utils.getTypeQName(node, forElement, false);

        if (qName == null || (Constants.isSchemaXSD(qName.getNamespaceURI()) &&
                qName.getLocalPart().equals("simpleRestrictionModel"))) {
            return;
        }

        // Error check - bug 12362
        if (qName.getLocalPart().length() == 0) {
            String name = Utils.getAttribute(node, "name");

            if (name == null) {
                name = "unknown";
            }

            throw new IOException();//Messages.getMessage("emptyref00", name));
        }

        // Get Type or Element depending on whether type attr was used.
        TypeEntry type = getTypeEntry(qName, forElement.value);

        // A symbol table entry is created if the TypeEntry is not found
        if (type == null) {

            // See if this is a special QName for collections
            if (qName.getLocalPart().indexOf("[") > 0) {
                QName containedQName = Utils.getTypeQName(node,
                        forElement, true);
                TypeEntry containedTE = getTypeEntry(containedQName,
                        forElement.value);

                if (!forElement.value) {

                    // Case of type and maxOccurs
                    if (containedTE == null) {

                        // Collection Element Type not defined yet, add one.
                        String baseName = btm.getBaseName(containedQName);

                        if (baseName != null) {
                            containedTE = new BaseType(containedQName);
                        } else {
                            containedTE = new UndefinedType(containedQName);
                        }

                        symbolTablePut(containedTE);
                    }

                    symbolTablePut(new CollectionType(qName, containedTE,
                            node, "[]"));
                } else {

                    // Case of ref and maxOccurs
                    if (containedTE == null) {
                        containedTE = new UndefinedElement(containedQName);

                        symbolTablePut(containedTE);
                    }

                    symbolTablePut(new CollectionElement(qName,
                            containedTE, node,
                            "[]"));
                }
            } else {

                // Add a BaseType or Undefined Type/Element
                String baseName = btm.getBaseName(qName);

                if (baseName != null) {
                    symbolTablePut(new BaseType(qName));

                    // bugzilla 23145: handle attribute groups
                    // soap/encoding is treated as a "known" schema
                    // so now let's act like we know it
                } else if (qName.equals(Constants.SOAP_COMMON_ATTRS11)) {
                    symbolTablePut(new BaseType(qName));

                    // the 1.1 commonAttributes type contains two attributes
                    // make sure those attributes' types are in the symbol table
                    // attribute name = "id" type = "xsd:ID"
                    if (getTypeEntry(Constants.XSD_ID, false) == null) {
                        symbolTablePut(new BaseType(Constants.XSD_ID));
                    }

                    // attribute name = "href" type = "xsd:anyURI"
                    if (getTypeEntry(Constants.XSD_ANYURI, false) == null) {
                        symbolTablePut(new BaseType(Constants.XSD_ANYURI));
                    }
                } else if (qName.equals(Constants.SOAP_COMMON_ATTRS12)) {
                    symbolTablePut(new BaseType(qName));

                    // the 1.2 commonAttributes type contains one attribute
                    // make sure the attribute's type is in the symbol table
                    // attribute name = "id" type = "xsd:ID"
                    if (getTypeEntry(Constants.XSD_ID, false) == null) {
                        symbolTablePut(new BaseType(Constants.XSD_ID));
                    }
                } else if (qName.equals(Constants.SOAP_ARRAY_ATTRS11)) {
                    symbolTablePut(new BaseType(qName));

                    // the 1.1 arrayAttributes type contains two attributes
                    // make sure the attributes' types are in the symbol table
                    // attribute name = "arrayType" type = "xsd:string"
                    if (getTypeEntry(Constants.XSD_STRING, false) == null) {
                        symbolTablePut(new BaseType(Constants.XSD_STRING));
                    }

                    // attribute name = "offset" type = "soapenc:arrayCoordinate"
                    // which is really an xsd:string
                } else if (qName.equals(Constants.SOAP_ARRAY_ATTRS12)) {
                    symbolTablePut(new BaseType(qName));

                    // the 1.2 arrayAttributes type contains two attributes
                    // make sure the attributes' types are in the symbol table
                    // attribute name = "arraySize" type = "2003soapenc:arraySize"
                    // which is really a hairy beast that is not
                    // supported, yet; so let's just use string
                    if (getTypeEntry(Constants.XSD_STRING, false) == null) {
                        symbolTablePut(new BaseType(Constants.XSD_STRING));
                    }

                    // attribute name = "itemType" type = "xsd:QName"
                    if (getTypeEntry(Constants.XSD_QNAME, false) == null) {
                        symbolTablePut(new BaseType(Constants.XSD_QNAME));
                    }
                } else if (forElement.value == false) {
                    symbolTablePut(new UndefinedType(qName));
                } else {
                    symbolTablePut(new UndefinedElement(qName));
                }
            }
        }
    }    // createTypeFromRef

       /**
     * Put the given SymTabEntry into the symbol table, if appropriate.
     * 
     * @param entry
     * @throws IOException
     */
    private SymTabEntry symbolTablePut(SymTabEntry entry) throws IOException {

        QName name = entry.getQName();

        SymTabEntry e = get(name, entry.getClass());

        if (e == null) {
            e = entry;

            // An entry of the given qname of the given type doesn't exist yet.
            if ((entry instanceof Type)
                    && (get(name, UndefinedType.class) != null)) {

                // A undefined type exists in the symbol table, which means
                // that the type is used, but we don't yet have a definition for
                // the type.  Now we DO have a definition for the type, so
                // replace the existing undefined type with the real type.
                if (((TypeEntry) get(name, UndefinedType.class)).isSimpleType()
                        && !((TypeEntry) entry).isSimpleType()) {

                    // Problem if the undefined type was used in a
                    // simple type context.
                    throw new IOException();
//                            Messages.getMessage(
//                                    "AttrNotSimpleType01", name.toString()));
                }

                Vector v = (Vector) symbolTable.get(name);

                for (int i = 0; i < v.size(); ++i) {
                    Object oldEntry = v.elementAt(i);

                    if (oldEntry instanceof UndefinedType) {

                        // Replace it in the symbol table
                        v.setElementAt(entry, i);

                        // Replace it in the types index
                        typeTypeEntries.put(name, entry);

                        // Update all of the entries that refer to the unknown type
                        ((UndefinedType) oldEntry).update((Type) entry);
                    }
                }
            } else if ((entry instanceof Element)
                    && (get(name, UndefinedElement.class) != null)) {

                // A undefined element exists in the symbol table, which means
                // that the element is used, but we don't yet have a definition for
                // the element.  Now we DO have a definition for the element, so
                // replace the existing undefined element with the real element.
                Vector v = (Vector) symbolTable.get(name);

                for (int i = 0; i < v.size(); ++i) {
                    Object oldEntry = v.elementAt(i);

                    if (oldEntry instanceof UndefinedElement) {

                        // Replace it in the symbol table
                        v.setElementAt(entry, i);

                        // Replace it in the elements index
                        elementTypeEntries.put(name, entry);

                        // Update all of the entries that refer to the unknown type
                        ((Undefined) oldEntry).update((Element) entry);
                    }
                }
            } else {

                // Add this entry to the symbol table
                Vector v = (Vector) symbolTable.get(name);

                if (v == null) {
                    v = new Vector();

                    symbolTable.put(name, v);
                }

                v.add(entry);

                // add TypeEntries to specialized indices for
                // fast lookups during reference resolution.
                if (entry instanceof Element) {
                    elementTypeEntries.put(name, entry);
                } else if (entry instanceof Type) {
                    typeTypeEntries.put(name, entry);
                }
            }
        } else {
            if (!quiet) {
//                System.out.println(Messages.getMessage("alreadyExists00",
//                        "" + name));
            }
        }

        return e;
    }    // symbolTablePut





    protected void processTypes() {
        for (Iterator i = typeTypeEntries.values().iterator(); i.hasNext(); ) {
            Type type = (Type) i.next();
            Node node = type.getNode();

            // Process the attributes
            Vector attributes =
                    SchemaUtils.getContainedAttributeTypes(node, this);

            if (attributes != null) {
                type.setContainedAttributes(attributes);
            }

            // Process the elements
            Vector elements =
                    SchemaUtils.getContainedElementDeclarations(node, this);

            if (elements != null) {
                type.setContainedElements(elements);
            }
        }
    }



    public void setWrapArrays(boolean wrapArrays) {
        this.wrapArrays = wrapArrays;
    }
}    // class SymbolTable
