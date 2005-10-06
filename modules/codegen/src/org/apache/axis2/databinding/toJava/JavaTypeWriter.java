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

//import org.apache.axis.wsdl.gen.Generator;
//import org.apache.axis.wsdl.symbolTable.SchemaUtils;
//import org.apache.axis.wsdl.symbolTable.SymTabEntry;
//import org.apache.axis.wsdl.symbolTable.SymbolTable;
//import org.apache.axis.wsdl.symbolTable.Type;
//import org.apache.axis.wsdl.symbolTable.TypeEntry;
import org.apache.axis2.databinding.gen.Generator;
import org.apache.axis2.databinding.symbolTable.*;
import org.w3c.dom.Node;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.util.Vector;
import java.util.Collections;

/**
 * This is Wsdl2java's Type Writer.  It writes the following files, as appropriate:
 * <typeName>.java, <typeName>Holder.java.
 */
public class JavaTypeWriter implements Generator {

    /** Field HOLDER_IS_NEEDED */
    public static final String HOLDER_IS_NEEDED = "Holder is needed";

    /** Field typeWriter */
    private Generator typeWriter = null;

    /** Field holderWriter */
    private Generator holderWriter = null;

    /**
     * Constructor.
     * 
     * @param emitter     
     * @param type        
     * @param symbolTable 
     */
    public JavaTypeWriter(Emitter emitter, TypeEntry type,
                          SymbolTable symbolTable) {

        //if (type.isReferenced() && !type.isOnlyLiteralReferenced()) {

            // Determine what sort of type this is and instantiate
            // the appropriate Writer.
            Node node = type.getNode();

            boolean isSimpleList = SchemaUtils.isListWithItemType(node);
            // If it's an array, don't emit a class
            if (!type.getName().endsWith("[]") && !isSimpleList) {
                
                // Generate the proper class for either "complex" or "enumeration" types
                Vector v = Utils.getEnumerationBaseAndValues(node, symbolTable);

                if (v != null) {
                    typeWriter = getEnumTypeWriter(emitter, type, v);
                } else {
                    TypeEntry base =
                            SchemaUtils.getComplexElementExtensionBase(node,
                                    symbolTable);

                    if (base == null) {
                        base = SchemaUtils.getComplexElementRestrictionBase(
                                node, symbolTable);
                    }

                    if (base == null) {
                        QName baseQName = SchemaUtils.getSimpleTypeBase(node);

                        if (baseQName != null) {
                            base = symbolTable.getType(baseQName);
                        }
                    }

                    typeWriter = getBeanWriter(emitter, type, base);
                }
            }

            // If the holder is needed (ie., something uses this type as an out or inout
            // parameter), instantiate the holder writer.
            if (holderIsNeeded(type)) {
                holderWriter = getHolderWriter(emitter, type);
            }
            
            if (typeWriter != null && type instanceof Type) {
                ((Type)type).setGenerated(true);
            }
        //}
    }    // ctor

    /**
     * Write all the service bindnigs:  service and testcase.
     * 
     * @throws IOException 
     */
    public void generate() throws IOException {

        if (typeWriter != null) {
            typeWriter.generate();
        }

        if (holderWriter != null) {
            holderWriter.generate();
        }
    }    // generate

    /**
     * Does anything use this type as an inout/out parameter?  Query the Type dynamicVar
     * 
     * @param entry 
     * @return 
     */
    private boolean holderIsNeeded(SymTabEntry entry) {

        Boolean holderIsNeeded =
                (Boolean) entry.getDynamicVar(HOLDER_IS_NEEDED);

        return ((holderIsNeeded != null) && holderIsNeeded.booleanValue());
    }    // holderIsNeeded

    /**
     * getEnumWriter
     * 
     * @param emitter 
     * @param type    
     * @param v       
     * @return 
     */
    protected JavaWriter getEnumTypeWriter(Emitter emitter, TypeEntry type,
                                           Vector v) {
        return new JavaEnumTypeWriter(emitter, type, v);
    }

    /**
     * getBeanWriter
     * 
     * @param emitter    
     * @param type       
     * @param base       
     * @return 
     */
    protected JavaWriter getBeanWriter(Emitter emitter, TypeEntry type, TypeEntry base) {   // CONTAINED_ELEM_AND_ATTR
        Vector elements = type.getContainedElements();
        Vector attributes = type.getContainedAttributes();
        
        // If this complexType is referenced in a
        // fault context, emit a bean-like exception
        // class
        Boolean isComplexFault = (Boolean) type.getDynamicVar(COMPLEX_TYPE_FAULT);

        if ((isComplexFault != null) && isComplexFault.booleanValue()) {

            return new JavaBeanFaultWriter(emitter, type, elements, base,
                attributes,
                getBeanHelperWriter(emitter, type, elements, base,
                                    attributes, true));
        }

        return new JavaBeanWriter(emitter, type, elements, base, attributes,
                getBeanHelperWriter(emitter, type, elements, base,
                                    attributes, false));
    }

    /**
     * getHelperWriter
     * 
     * @param emitter    
     * @param type       
     * @param elements   
     * @param base       
     * @param attributes 
     * @return 
     */
    protected JavaWriter getBeanHelperWriter(
            Emitter emitter, TypeEntry type, Vector elements, TypeEntry base,
            Vector attributes, boolean forException) {
        return new JavaBeanHelperWriter(
                emitter, type, elements, base, attributes,
                forException  ?  JavaBeanFaultWriter.RESERVED_PROPERTY_NAMES
                              :  Collections.EMPTY_SET);
    }

    /**
     * getHolderWriter
     * 
     * @param emitter 
     * @param type    
     * @return 
     */
    protected Generator getHolderWriter(Emitter emitter, TypeEntry type) {
        return new JavaHolderWriter(emitter, type);
    }
}    // class JavaTypeWriter
