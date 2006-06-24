/*
 * Copyright 2006 The Apache Software Foundation.
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

package org.apache.axis2.jibx;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.axis2.wsdl.databinding.JavaTypeMapper;
import org.apache.axis2.wsdl.databinding.TypeMapper;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaObjectCollection;
import org.apache.ws.commons.schema.XmlSchemaParticle;
import org.apache.ws.commons.schema.XmlSchemaSequence;
import org.apache.ws.commons.schema.XmlSchemaSimpleType;
import org.apache.ws.commons.schema.XmlSchemaType;
import org.jibx.binding.model.BindingElement;
import org.jibx.binding.model.ElementBase;
import org.jibx.binding.model.FormatElement;
import org.jibx.binding.model.IncludeElement;
import org.jibx.binding.model.MappingElement;
import org.jibx.binding.model.NamespaceElement;
import org.jibx.binding.model.ValidationContext;
import org.jibx.runtime.JiBXException;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Framework-linked code used by JiBX data binding support. This is accessed via
 * reflection from the JiBX code generation extension when JiBX data binding is
 * selected.
 */
public class CodeGenerationUtility {
    private static final String SCHEMA_NAMESPACE = "http://www.w3.org/2001/XMLSchema";

    /**
     * Get type mappings from binding definition. If unwrapping is enabled, this
     * sets the type mapping for unwrappable elements (those which only contain
     * a sequence of other elements, which all have maxOccurs="1") to a document
     * giving a format or mapped class corresponding to each of the child
     * child elements. Otherwise, the type mapping goes direct to a mapped class
     * name for each element.
     * 
     * @param path binding definition file path
     * @param defsmap map from element qname to schema definition
     * @param unwrap flag for elements to be unwrapped where possible
     * @return map from qname to class name
     */
    public static TypeMapper getBindingMap(String path, HashMap defsmap,
        boolean unwrap) {
        
        // make sure the binding definition file is present
        File file = new File(path);
        if (!file.exists()) {
            throw new RuntimeException("jibx binding definition file " + path + " not found");
//                CodegenMessages.getMessage("extension.encodedNotSupported"));
        }
        
        // Read the JiBX binding definition into memory. The binding definition
        // is not currently validated so as not to require the user to have all
        // the referenced classes in the classpath, though this does make for
        // added work in finding the namespaces.
        try {
            ValidationContext vctx = BindingElement.newValidationContext();
            BindingElement binding =
                BindingElement.readBinding(new FileInputStream(file), path, vctx);
            if (vctx.getErrorCount() != 0 || vctx.getFatalCount() != 0) {
                throw new RuntimeException("invalid jibx binding definition file " + path);
            }
            
            // create table with all built-in format definitions
            HashMap simpleTypeMap = new HashMap();
            buildFormat("byte", "byte",
                "org.jibx.runtime.Utility.serializeByte",
                "org.jibx.runtime.Utility.parseByte", "0", simpleTypeMap);
            buildFormat("unsignedShort", "char",
                "org.jibx.runtime.Utility.serializeChar",
                "org.jibx.runtime.Utility.parseChar", "0", simpleTypeMap);
            buildFormat("double", "double",
                "org.jibx.runtime.Utility.serializeDouble",
                "org.jibx.runtime.Utility.parseDouble", "0.0", simpleTypeMap);
            buildFormat("float", "float",
                "org.jibx.runtime.Utility.serializeFloat",
                "org.jibx.runtime.Utility.parseFloat", "0.0", simpleTypeMap);
            buildFormat("int", "int",
                "org.jibx.runtime.Utility.serializeInt",
                "org.jibx.runtime.Utility.parseInt", "0", simpleTypeMap);
            buildFormat("long", "long",
                "org.jibx.runtime.Utility.serializeLong",
                "org.jibx.runtime.Utility.parseLong", "0", simpleTypeMap);
            buildFormat("short", "short",
                "org.jibx.runtime.Utility.serializeShort",
                "org.jibx.runtime.Utility.parseShort", "0", simpleTypeMap);
            buildFormat("boolean", "boolean",
                "org.jibx.runtime.Utility.serializeBoolean",
                "org.jibx.runtime.Utility.parseBoolean", "false",
                simpleTypeMap);
            buildFormat("dateTime", "java.util.Date",
                "org.jibx.runtime.Utility.serializeDateTime",
                "org.jibx.runtime.Utility.deserializeDateTime", null,
                simpleTypeMap);
            buildFormat("date", "java.sql.Date",
                "org.jibx.runtime.Utility.serializeSqlDate",
                "org.jibx.runtime.Utility.deserializeSqlDate", null,
                simpleTypeMap);
            buildFormat("time", "java.sql.Time",
                "org.jibx.runtime.Utility.serializeSqlTime",
                "org.jibx.runtime.Utility.deserializeSqlTime", null,
                simpleTypeMap);
            buildFormat("base64Binary", "byte[]",
                "org.jibx.runtime.Utility.serializeBase64",
                "org.jibx.runtime.Utility.deserializeBase64", null,
                simpleTypeMap);
            buildFormat("string","java.lang.String", null, null, null,
                simpleTypeMap);
            
            // collect all the top-level mapping and format definitions
            HashMap elementMap = new HashMap();
            HashMap complexTypeMap = new HashMap();
            collectTopLevelComponents(binding, null, elementMap,
                complexTypeMap, simpleTypeMap);
            
            // populate type mapper for all elements used by service
            TypeMapper mapper = new JavaTypeMapper();
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            for (Iterator iter = defsmap.keySet().iterator(); iter.hasNext();) {
                QName qname = (QName)iter.next();
                Object obj = elementMap.get(qname);
                if (obj == null) {
                    if (unwrap) {
                        
                        // element must be sequence with non-repeated child elements
                        XmlSchemaElement element =
                            (XmlSchemaElement)defsmap.get(qname);
                        XmlSchemaType type = element.getSchemaType();
                        Document detail = unwrapDefinition(qname, type,
                            simpleTypeMap, complexTypeMap, factory);
                        if (detail != null) {
                            mapper.addTypeMappingObject(qname, detail);
                        }
                        
                    } else {
                        throw new RuntimeException
                            ("No mapping definition found for element " + qname);
                    }
                } else {
                    
                    // concrete mapping, just save the mapped class name
                    MappingElement mapping = (MappingElement)obj;
                    mapper.addTypeMappingName(qname, mapping.getClassName());
                    
                }
            }
            return mapper;
            
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (JiBXException e) {
            throw new RuntimeException(e);
        }
    }
    
    private static void buildFormat(String stype, String jtype, String sname,
        String dname, String dflt, HashMap map) {
        FormatElement format = new FormatElement();
        format.setTypeName(jtype);
        format.setSerializerName(sname);
        format.setDeserializerName(dname);
        format.setDefaultText(dflt);
        map.put(new QName(SCHEMA_NAMESPACE, stype), format);
    }

    /**
     * Handle the unwrapping of an element definition. The element to be
     * unwrapped must be defined as a complexType with no attributes wrapping a
     * sequence, where each particle in the sequence is an element definition
     * with maxOccurs='1'.
     * 
     * @param qname element qualified name
     * @param type schema type definition
     * @param simpleTypeMap 
     * @param complexTypeMap 
     * @param factory 
     * @return document with data binding details
     * @throws RuntimeException on error unwrapping document
     */
    private static Document unwrapDefinition(QName qname, XmlSchemaType type,
        HashMap simpleTypeMap, HashMap complexTypeMap,
        DocumentBuilderFactory factory) {
        try {
            
            // dig down to the sequence
            if (type instanceof XmlSchemaComplexType) {
                XmlSchemaComplexType ctype = (XmlSchemaComplexType)type;
                if (ctype.getAttributes().getCount() != 0) {
                    throw new RuntimeException("Cannot unwrap element " +
                        qname + ": attributes not allowed on type to be unwrapped");
                }
                XmlSchemaParticle particle = ctype.getParticle();
                if (!(particle instanceof XmlSchemaSequence)) {
                    throw new RuntimeException("Cannot unwrap element " +
                        qname + ": type to be unwrapped must be a sequence");
                }
                if (particle.getMinOccurs() != 1 || particle.getMaxOccurs() != 1) {
                    throw new RuntimeException("Cannot unwrap element " +
                        qname + ": contained sequence must have minOccurs='1' and maxOccurs='1'");
                }
                XmlSchemaSequence sequence = (XmlSchemaSequence)particle;
                
                // create document to hold data binding details for element
                Document doc = factory.newDocumentBuilder().newDocument();
                Element root = doc.createElement("wrapper");
                root.setAttribute("ns", qname.getNamespaceURI());
                root.setAttribute("name", qname.getLocalPart());
                doc.appendChild(root);
                
                // add child param element matching each child of wrapper element
                XmlSchemaObjectCollection items = sequence.getItems();
                for (Iterator iter = items.getIterator(); iter.hasNext();) {
                    
                    // check that child item obeys the unwrapping rules
                    XmlSchemaParticle item = (XmlSchemaParticle)iter.next();
                    if (!(item instanceof XmlSchemaElement)) {
                        throw new RuntimeException("Cannot unwrap element " +
                            qname + ": only element items allowed in seqence");
                    }
                    XmlSchemaElement element = (XmlSchemaElement)item;
                    QName typename = element.getSchemaTypeName();
                    if (typename == null) {
                        throw new RuntimeException("Cannot unwrap element " +
                            qname + ": all elements in contained sequence must reference a named type");
                    }
                    
                    // add element to output with details of this element handling
                    Element param = doc.createElement("param");
                    QName itemname = element.getQName();
                    param.setAttribute("ns", itemname.getNamespaceURI());
                    param.setAttribute("name", itemname.getLocalPart());
                    if (element.getSchemaType() instanceof XmlSchemaSimpleType) {
                        
                        // simple type translates to format element in binding
                        FormatElement format = (FormatElement)simpleTypeMap.get(typename);
                        if (format == null) {
                            throw new RuntimeException("Cannot unwrap element " +
                                qname + ": no format definition found for child element " + itemname);
                        }
                        param.setAttribute("type", "simple");
                        param.setAttribute("java-type", format.getTypeName());
                        param.setAttribute("serializer", format.getSerializerName());
                        param.setAttribute("deserializer", format.getDeserializerName());
                        String dflt = element.getDefaultValue();
                        if (dflt == null) {
                            dflt = format.getDefaultText();
                        }
                        param.setAttribute("default", dflt);
                        
                    } else {
                        
                        // complex type translates to abstract mapping in binding
                        MappingElement mapping = (MappingElement)complexTypeMap.get(typename);
                        if (mapping == null) {
                            throw new RuntimeException("Cannot unwrap element " +
                                qname + ": no mapping definition found for child element " + itemname);
                        }
                        param.setAttribute("type", "complex");
                        param.setAttribute("java-type", mapping.getClassName());
                        param.setAttribute("type-ns", typename.getNamespaceURI());
                        param.setAttribute("type-name", typename.getLocalPart());
                        
                    }
                    
                    // add shared information to binding details element
                    if (element.getMinOccurs() == 0) {
                        param.setAttribute("optional", "true");
                    }
                    if (element.getMaxOccurs() > 1) {
                        param.setAttribute("repeated", "true");
                    }
                    Attr[] attrs = element.getUnhandledAttributes();
                    if (attrs != null) {
                        for (int i = 0; i < attrs.length; i++) {
                            Attr attr = attrs[i];
                            if ("nillable".equals(attr.getName()) &&
                                SCHEMA_NAMESPACE.equals(attr.getNamespaceURI())) {
                                param.setAttribute("nillable", "true");
                                break;
                            }
                        }
                    }
                    root.appendChild(param);
                }
                return doc;
                
            }
            throw new RuntimeException("Cannot unwrap element " +
                qname);
            
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Collect mapping from qnames to classes for top level mappings in JiBX
     * binding.
     * 
     * @param binding
     * @param dns default namespace to be used unless overridden
     * (<code>null</code> if none)
     * @param elementMap map from element names to concrete mapping components
     * of binding
     * @param complexTypeMap map from type names to abstract mapping
     * components of binding
     * @param simpleTypeMap map from type names to format definition components
     * of binding
     */
    private static void collectTopLevelComponents(BindingElement binding,
        String dns, HashMap elementMap, HashMap complexTypeMap,
        HashMap simpleTypeMap) {
        
        // check default namespace set at top level of binding
        String defaultns = findDefaultNS(binding.topChildIterator(), dns);
        
        // add all top level mapping and format definitions to maps
        for (Iterator iter = binding.topChildIterator(); iter.hasNext();) {
            ElementBase child = (ElementBase)iter.next();
            if (child.type() == ElementBase.INCLUDE_ELEMENT) {
                
                // recurse to process included binding definitions
                IncludeElement include = (IncludeElement)child;
                collectTopLevelComponents(include.getBinding(), defaultns,
                    elementMap, complexTypeMap, simpleTypeMap);
                
            } else if (child.type() == ElementBase.FORMAT_ELEMENT) {
                
                // register named formats as simple type conversions
                FormatElement format = (FormatElement)child;
                registerElement(format.getQName(), format, simpleTypeMap);
                
            } else if (child.type() == ElementBase.MAPPING_ELEMENT) {
                MappingElement mapping = (MappingElement)child;
                if (mapping.isAbstract()) {
                    
                    // register named abstract mappings as complex type conversions
                    registerElement(mapping.getTypeQName(), mapping,
                        complexTypeMap);
                    
                } else {
                    
                    // register concrete mappings as element conversions
                    String uri = mapping.getUri();
                    if (uri == null) {
                        uri = findDefaultNS(mapping.topChildIterator(),
                            defaultns);
                    }
                    elementMap.put(new QName(uri, mapping.getName()), mapping);
                }
            }
        }
    }
    
    /**
     * Register binding element by qualified name. This converts the qualified
     * name format used by the JiBX binding model to that used by Axis2.
     * 
     * @param qname qualified name in JiBX format (<code>null</code> if none)
     * @param element corresponding element of binding definition
     * @param map qualified name to element map
     */
    private static void registerElement(org.jibx.runtime.QName qname,
        ElementBase element, HashMap map) {
        if (qname != null) {
            map.put(new QName(qname.getUri(), qname.getName()), element);
        }
    }

    /**
     * Find the default namespace within a list of JiBX binding model elements
     * possibly including namespace definitions. Once a non-namespace definition
     * element is seen in the list, this just returns (since the namespace
     * definitions always come first in JiBX's binding format).
     * 
     * @param iter iterator for elements in list
     * @param dns default namespace if not overridden
     */
    private static String findDefaultNS(Iterator iter, String dns) {
        while (iter.hasNext()) {
            ElementBase child = (ElementBase)iter.next();
            if (child.type() == ElementBase.NAMESPACE_ELEMENT) {
                NamespaceElement namespace = (NamespaceElement)child;
                String defaultName = namespace.getDefaultName();
                if ("elements".equals(defaultName) || "all".equals(defaultName)) {
                    return namespace.getUri();
                }
            } else {
               break;
            }
        }
        return dns;
    }

    /**
     * Get map from qname to corresponding class name from binding definition.
     * Only the global &lt;mapping> elements in the binding definition are
     * included, since these are the only ones accessible from the Axis2
     * interface.
     * 
     * @param path binding definition file path
     * @return map from qname to class name
     */
    public static HashMap getBindingMap(String path) {
        
        // make sure the binding definition file is present
        File file = new File(path);
        if (!file.exists()) {
            throw new RuntimeException("jibx binding definition file " + path + " not found");
//                CodegenMessages.getMessage("extension.encodedNotSupported"));
        }
        
        // Read the JiBX binding definition into memory. The binding definition
        // is not currently validated so as not to require the user to have all
        // the referenced classes in the classpath, though this does make for
        // added work in finding the namespaces.
        try {
            ValidationContext vctx = BindingElement.newValidationContext();
            BindingElement binding =
                BindingElement.readBinding(new FileInputStream(file), path, vctx);
            if (vctx.getErrorCount() != 0 || vctx.getFatalCount() != 0) {
                throw new RuntimeException("invalid jibx binding definition file " + path);
            }
            
            // create map from qname to class for all top-level mappings
            return defineBoundClasses(binding);
            
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (JiBXException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Create mapping from qnames to classes for top level mappings in JiBX binding.
     * 
     * @param binding
     * @return map from qname to class
     */
    private static HashMap defineBoundClasses(BindingElement binding) {
        
        // check default namespace set at top level of binding
        String defaultns = findDefaultNS(binding.topChildIterator());
        
        // add all top level mapping definitions to map from qname to class
        HashMap mappings = new HashMap();
        for (Iterator iter = binding.topChildIterator(); iter.hasNext();) {
            ElementBase child = (ElementBase)iter.next();
            if (child.type() == ElementBase.MAPPING_ELEMENT) {
                MappingElement mapping = (MappingElement)child;
                String name = mapping.getName();
                if (name != null) {
                    String uri = mapping.getUri();
                    if (uri == null) {
                        uri = findDefaultNS(mapping.topChildIterator());
                        if (uri == null) {
                            uri = defaultns;
                        }
                    }
                    mappings.put(new QName(uri, name), mapping.getClassName());
                }
            }
        }
        return mappings;
    }

    /**
     * Find the default namespace within a list of JiBX binding model elements
     * possibly including namespace definitions. Once a non-namespace definition
     * element is seen in the list, this just returns (since the namespace
     * definitions always come first in JiBX's binding format).
     * 
     * @param iter iterator for elements in list
     */
    private static String findDefaultNS(Iterator iter) {
        while (iter.hasNext()) {
            ElementBase child = (ElementBase)iter.next();
            if (child.type() == ElementBase.NAMESPACE_ELEMENT) {
                NamespaceElement namespace = (NamespaceElement)child;
                String defaultName = namespace.getDefaultName();
                if ("elements".equals(defaultName) || "all".equals(defaultName)) {
                    return namespace.getUri();
                }
            } else {
               break;
            }
        }
        return null;
    }
}