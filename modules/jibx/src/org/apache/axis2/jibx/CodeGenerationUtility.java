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
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.axis2.AxisFault;
import org.apache.axis2.description.AxisMessage;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.axis2.wsdl.WSDLUtil;
import org.apache.axis2.wsdl.codegen.CodeGenConfiguration;
import org.apache.axis2.wsdl.databinding.JavaTypeMapper;
import org.apache.axis2.wsdl.util.Constants;
import org.apache.axis2.wsdl.util.MessagePartInformationHolder;
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
import org.jibx.binding.model.ModelVisitor;
import org.jibx.binding.model.NamespaceElement;
import org.jibx.binding.model.ValidationContext;
import org.jibx.runtime.JiBXException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Framework-linked code used by JiBX data binding support. This is accessed via
 * reflection from the JiBX code generation extension when JiBX data binding is
 * selected. JiBX uses a different approach to unwrapping method parameters from
 * that implemented by ADB, and since the ADB technique is assumed by all code
 * generation templates this has to create the same data structures. These data
 * structures are undocumented, and can only be determined by going through the
 * {@link org.apache.axis2.wsdl.codegen.extension.SchemaUnwrapperExtension} and
 * {@link org.apache.axis2.wsdl.codegen.emitter.AxisServiceBasedMultiLanguageEmitter}
 * code.
 */
public class CodeGenerationUtility {
    private static final String SCHEMA_NAMESPACE = "http://www.w3.org/2001/XMLSchema";
    
    private final CodeGenConfiguration codeGenConfig;
    
    /**
     * Constructor.
     * 
     * @param config code generation configuration
     */
    public CodeGenerationUtility(CodeGenConfiguration config) {
        codeGenConfig = config;
    }
    
    /**
     * Configure the code generation based on the supplied parameters and WSDL.
     * This first gets type mappings from binding definition, then goes through
     * the operations checking the input and output messages. If unwrapping is
     * disabled the message element must be handled directly by a mapping. If
     * unwrapping is enabled, this checks that the message element is of the
     * proper form (a sequence of other elements, which all have maxOccurs="1").
     * It then generates an unwrapping description and adds it to the code
     * generation configuration, where it'll be picked up and included in the
     * XML passed to code generation. This also constructs a type mapping, since
     * that's required by the base Axis2 code generation. In the case of
     * unwrapped elements, the type mapping includes a synthesized qname for
     * each unwrapped parameter, and the detailed information is set on the
     * message information. Sound confusing? Welcome to Axis2 code generation.
     * 
     * @param path binding path
     */
    public void engage(String path) {
        
        // make sure the binding definition file is present
        File file = new File(path);
        if (!file.exists()) {
            throw new RuntimeException("jibx binding definition file " + path + " not found");
//                CodegenMessages.getMessage("extension.encodedNotSupported"));
        }
        
        // Read the JiBX binding definition into memory. The binding definition
        // is only prevalidated so as not to require the user to have all
        // the referenced classes in the classpath, though this does make for
        // added work in finding the namespaces.
        try {
            ValidationContext vctx = BindingElement.newValidationContext();
            BindingElement binding =
                BindingElement.readBinding(new FileInputStream(file), path, vctx);
            binding.setBaseUrl(file.toURL());
            vctx.setBindingRoot(binding);
            IncludePrevalidationVisitor ipv = new IncludePrevalidationVisitor(vctx);
            vctx.tourTree(binding, ipv);
            if (vctx.getErrorCount() != 0 || vctx.getFatalCount() != 0) {
                throw new RuntimeException("invalid jibx binding definition file " + path);
            }
            
            // create table with all built-in format definitions
            Map simpleTypeMap = new HashMap();
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
            Map elementMap = new HashMap();
            Map complexTypeMap = new HashMap();
            collectTopLevelComponents(binding, null, elementMap,
                complexTypeMap, simpleTypeMap);
            
            // force off inappropriate option (set by error in options handling)
            codeGenConfig.setPackClasses(false);

            // configure handling for all operations of service
            codeGenConfig.setTypeMapper(new NamedParameterTypeMapper());
            Iterator operations = codeGenConfig.getAxisService().getOperations();
            boolean unwrap = !codeGenConfig.isParametersWrapped();
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            int opindex = 0;
            Map typeMappedClassMap = new HashMap();
            String mappedclass = null;
            while (operations.hasNext()) {
                
                // get the basic operation information
                AxisOperation op = (AxisOperation)operations.next();
                String mep = op.getMessageExchangePattern();
                AxisMessage inmsg = null;
                AxisMessage outmsg = null;
                if (WSDLUtil.isInputPresentForMEP(mep)) {
                    inmsg = op.getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
                    if (inmsg == null) {
                        throw new RuntimeException("Expected input message not found for operation " + op.getName());
                    }
                }
                if (WSDLUtil.isOutputPresentForMEP(mep)) {
                    outmsg = op.getMessage(WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
                    if (outmsg == null) {
                        throw new RuntimeException("Expected output message not found for operation " + op.getName());
                    }
                }
                if (unwrap) {
                    
                    // use unwrapping for both input and output
                    String receivername = "jibxReceiver" + opindex++;
                    Element dbmethod = doc.createElement("dbmethod");
                    dbmethod.setAttribute("receiver-name", receivername);
                    dbmethod.setAttribute("method-name", op.getName().getLocalPart());
                    Set nameset = new HashSet();
                    if (inmsg != null) {
                        dbmethod.appendChild(unwrapMessage(inmsg, false, simpleTypeMap, complexTypeMap, typeMappedClassMap, nameset, doc));
                    }
                    if (outmsg != null) {
                        dbmethod.appendChild(unwrapMessage(outmsg, true, simpleTypeMap, complexTypeMap, typeMappedClassMap, nameset, doc));
                    }
                    
                    // save unwrapping information for use in code generation
                    op.addParameter(new Parameter(Constants.DATABINDING_GENERATED_RECEIVER, receivername));
                    op.addParameter(new Parameter(Constants.DATABINDING_GENERATED_IMPLEMENTATION, Boolean.TRUE));
                    op.addParameter(new Parameter(Constants.DATABINDING_OPERATION_DETAILS, dbmethod));
                    
                } else {
                    
                    // concrete mappings, just save the mapped class name(s)
                    if (inmsg != null) {
                        mappedclass = mapMessage(inmsg, elementMap);
                    }
                    if (outmsg != null) {
                        mappedclass = mapMessage(outmsg, elementMap);
                    }
                    
                }
            }
            
            // add type usage information as service parameter
            Element bindinit = doc.createElement("initialize-binding");
            if (!typeMappedClassMap.isEmpty()) {
                for (Iterator iter = typeMappedClassMap.keySet().iterator(); iter.hasNext();) {
                    QName tname = (QName)iter.next();
                    String clsindex = ((Integer)typeMappedClassMap.get(tname)).toString();
                    Element detail = doc.createElement("abstract-type");
                    detail.setAttribute("ns", tname.getNamespaceURI());
                    detail.setAttribute("name", tname.getLocalPart());
                    detail.setAttribute("type-index", clsindex);
                    bindinit.appendChild(detail);
                    if (mappedclass == null) {
                        MappingElement mapping = (MappingElement)complexTypeMap.get(tname);
                        mappedclass = mapping.getClassName();
                    }
                }
            }
            bindinit.setAttribute("bound-class", mappedclass);
            codeGenConfig.getAxisService().addParameter(new Parameter(Constants.DATABINDING_SERVICE_DETAILS, bindinit));
            
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (JiBXException e) {
            throw new RuntimeException(e);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (AxisFault e) {
            throw new RuntimeException(e);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Add format definition for type with built-in JiBX handling to map.
     * 
     * @param stype schema type name
     * @param jtype java type name
     * @param sname serializer method name
     * @param dname deserializer method name
     * @param dflt default value
     * @param map schema type qname to format definition map
     */
    private static void buildFormat(String stype, String jtype, String sname,
        String dname, String dflt, Map map) {
        FormatElement format = new FormatElement();
        format.setTypeName(jtype);
        format.setSerializerName(sname);
        format.setDeserializerName(dname);
        format.setDefaultText(dflt);
        map.put(new QName(SCHEMA_NAMESPACE, stype), format);
    }
    
    /**
     * Handles unwrapping a message. This generates and returns the detailed
     * description of how the message is to be unwrapped. It also creates the
     * data structures expected by the code generation in order to be somewhat
     * compatible with ADB unwrapping.
     * 
     * @param msg message to be unwrapped
     * @param isout output message flag (wrapper inherits inner type, for XSLTs)
     * @param simpleTypeMap binding formats
     * @param complexTypeMap binding mappings
     * @param typeMappedClassMap map from type qname to index
     * @param nameset parameter variable names used in method
     * @param doc document used for DOM components
     * @return detailed description element for code generation
     */
    private Element unwrapMessage(AxisMessage msg, boolean isout,
        Map simpleTypeMap, Map complexTypeMap, Map typeMappedClassMap,
        Set nameset, Document doc) {
        
        // find the schema definition for this message element
        QName qname = msg.getElementQName();
        if (qname == null) {
            throw new RuntimeException("No element reference in message " + msg.getName());
        }
        XmlSchemaElement wrapdef = codeGenConfig.getAxisService().getSchemaElement(qname);
        if (wrapdef == null) {
            throw new RuntimeException("Cannot unwrap - no definition found for element " + qname);
        }
        XmlSchemaType type = wrapdef.getSchemaType();
        
        // create document to hold data binding details for element
        Element wrapdetail = doc.createElement(isout ? "out-wrapper" : "in-wrapper");
        wrapdetail.setAttribute("ns", qname.getNamespaceURI());
        wrapdetail.setAttribute("name", qname.getLocalPart());
        
        // dig down to the sequence
        List partNameList = new ArrayList();
        String wrappertype = "";
        if (type instanceof XmlSchemaComplexType) {
            wrapdetail.setAttribute("empty", "false");
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
            
            // add child param element matching each child of wrapper element
            QName opName = ((AxisOperation)msg.getParent()).getName();
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
                Element param = doc.createElement(isout ? "return-element" : "parameter-element");
                QName itemname = element.getQName();
                param.setAttribute("ns", itemname.getNamespaceURI());
                param.setAttribute("name", itemname.getLocalPart());
                param.setAttribute("java-name", toJavaName(itemname.getLocalPart(), nameset));
                param.setAttribute("nillable", Boolean.toString(element.isNillable()));
                param.setAttribute("optional", Boolean.toString(element.getMinOccurs() == 0));
                boolean isarray = element.getMaxOccurs() > 1;
                param.setAttribute("array", Boolean.toString(isarray));
                String javatype;
                if (element.getSchemaType() instanceof XmlSchemaSimpleType) {
                    
                    // simple type translates to format element in binding
                    FormatElement format = (FormatElement)simpleTypeMap.get(typename);
                    if (format == null) {
                        throw new RuntimeException("Cannot unwrap element " +
                            qname + ": no format definition found for child element " + itemname);
                    }
                    javatype = format.getTypeName();
                    param.setAttribute("form", "simple");
                    param.setAttribute("serializer", format.getSerializerName());
                    param.setAttribute("deserializer", format.getDeserializerName());
                    String dflt = element.getDefaultValue();
                    if (dflt == null) {
                        dflt = format.getDefaultText();
                    }
                    if (dflt != null) {
                        param.setAttribute("default", dflt);
                    }
                    
                } else {
                    
                    // complex type translates to abstract mapping in binding
                    MappingElement mapping = (MappingElement)complexTypeMap.get(typename);
                    if (mapping == null) {
                        throw new RuntimeException("Cannot unwrap element " +
                            qname + ": no abstract mapping definition found for child element " + itemname);
                    }
                    Integer tindex = (Integer)typeMappedClassMap.get(typename);
                    if (tindex == null) {
                        tindex = new Integer(typeMappedClassMap.size());
                        typeMappedClassMap.put(typename, tindex);
                    }
                    javatype = mapping.getClassName();
                    param.setAttribute("form", "complex");
                    param.setAttribute("type-index", tindex.toString());
                    
                }
                param.setAttribute("java-type", javatype);
                String fulltype = javatype;
                if (isarray) {
                    fulltype += "[]";
                }
                if (isout) {
                    wrappertype = fulltype;
                } else {
                    wrappertype = "java.lang.Object";
                }
                wrapdetail.appendChild(param);
                
                // this magic code comes from org.apache.axis2.wsdl.codegen.extension.SchemaUnwrapperExtension
                //  it's used here to fit into the ADB-based code generation model
                QName partqname = WSDLUtil.getPartQName(opName.getLocalPart(),
                    WSDLConstants.INPUT_PART_QNAME_SUFFIX, itemname.getLocalPart());
                partNameList.add(partqname);
                
                // add type mapping so we look like ADB
                codeGenConfig.getTypeMapper().addTypeMappingName(partqname, fulltype);
            }
            
        } else if (type == null) {
            wrapdetail.setAttribute("empty", "true");
            wrappertype = "";
        } else {
            throw new RuntimeException("Cannot unwrap element " + qname +
                ": not a complexType definition");
        }

        // this magic code comes from org.apache.axis2.wsdl.codegen.extension.SchemaUnwrapperExtension
        //  it's used here to fit into the ADB-based code generation model
        MessagePartInformationHolder infoHolder = new MessagePartInformationHolder();
        infoHolder.setOperationName(((AxisOperation)msg.getParent()).getName());
        infoHolder.setPartsList(partNameList);
        try {
            msg.addParameter(new Parameter(Constants.UNWRAPPED_DETAILS, infoHolder));
        } catch (AxisFault e) {
            throw new RuntimeException(e);
        }
        
        // set indication for unwrapped message
        try {
            msg.addParameter(new Parameter(Constants.UNWRAPPED_KEY, Boolean.TRUE));
        } catch (AxisFault e) {
            throw new RuntimeException(e);
        }
        
        // add fake mapping for wrapper name (necessary for current XSLTs)
        codeGenConfig.getTypeMapper().addTypeMappingName(qname, wrappertype);
        
        // return the unwrapping details
        return wrapdetail;
    }
    
    private static String toJavaName(String name, Set nameset) {
        StringBuffer buff = new StringBuffer(name.length());
        for (int i = 0; i < name.length(); i++) {
            char chr = name.charAt(i);
            if ((i == 0 && Character.isJavaIdentifierStart(chr)) ||
                (i > 0 && Character.isJavaIdentifierPart(chr))) {
                buff.append(chr);
            } else if (chr == ':' || chr == '.') {
                buff.append('$');
            } else {
                buff.append('_');
            }
        }
        int count = 0;
        String jname = buff.toString();
        while (!nameset.add(jname)) {
            jname = buff.toString() + count++;
        }
        return jname;
    }
    
    private String mapMessage(AxisMessage msg, Map complexTypeMap) {
        QName qname = msg.getElementQName();
        if (qname == null) {
            throw new RuntimeException("No element reference in message " + msg.getName());
        }
        Object obj = complexTypeMap.get(qname);
        if (obj == null) {
            throw new RuntimeException("No mapping defined for element " + qname);
        }
        MappingElement mapping = (MappingElement)obj;
        String cname = mapping.getClassName();
        codeGenConfig.getTypeMapper().addTypeMappingName(qname, cname);
        return cname;
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
        String dns, Map elementMap, Map complexTypeMap,
        Map simpleTypeMap) {
        
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
        ElementBase element, Map map) {
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
     * @return default namespace
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
     * Inner class for handling prevalidation of include elements only. Unlike
     * the normal JiBX binding definition prevalidation step, this visitor
     * ignores everything except include elements.
     */
    private class IncludePrevalidationVisitor extends ModelVisitor
    {
        private final ValidationContext m_context;
        
        private IncludePrevalidationVisitor(ValidationContext vctx) {
            m_context = vctx;
        }
        
        /* (non-Javadoc)
         * @see org.jibx.binding.model.ModelVisitor#visit(org.jibx.binding.model.ElementBase)
         */
        public boolean visit(IncludeElement node) {
            try {
                node.prevalidate(m_context);
            } catch (Throwable t) {
                m_context.addFatal("Error during validation: " +
                    t.getMessage());
                t.printStackTrace();
                return false;
            }
            return true;
        }
    }
    
    private static class NamedParameterTypeMapper extends JavaTypeMapper
    {
        /**
         * Return the real parameter name, not a dummy.
         * 
         * @param qname
         * @return local part of name
         */
        public String getParameterName(QName qname) {
            return qname.getLocalPart();
        }
    }
}