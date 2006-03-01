package org.apache.axis2.wsdl.builder;

import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.wsdl.WSDLBinding;
import org.apache.wsdl.WSDLDescription;
import org.apache.wsdl.WSDLInterface;
import org.apache.wsdl.WSDLOperation;
import org.apache.wsdl.MessageReference;
import org.apache.ws.commons.schema.XmlSchemaType;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaParticle;
import org.apache.ws.commons.schema.XmlSchemaSequence;
import org.apache.ws.commons.schema.XmlSchemaObjectCollection;

import java.util.Map;
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
 * The purpose of the schema unwrapper is to walk the schema and figure out
 * the expandable items. The schema unwrapper works off the WOM and attaches
 * the unwrapping information to the metadata bags of the relevant messageReference
 * objects
 */
public class SchemaUnwrapper {

    /**
     * Unwraps a given WSDLDescription
     * @param description
     */
    public static void unwrap(WSDLDescription description){
        //first start with the bindings (if present), else we
        //switch back to the interfaces list
        //BTW we use the WSDL 2.0 terminology here

        Map bindings = description.getBindings();
        Map interfaces = description.getWsdlInterfaces();

        if (bindings != null && !bindings.isEmpty()){
            //process bindings
            WSDLBinding[] bindingsArray = (WSDLBinding[])
                    bindings.values().toArray(new WSDLBinding[bindings.size()]);
            for (int i = 0; i < bindingsArray.length; i++) {
                unwrapSchemaForInterface(bindingsArray[i].getBoundInterface());

            }

        }else if (interfaces!=null && !interfaces.isEmpty()){
            //process the interfaces (porttypes)
            WSDLInterface[] interfacesArray = (WSDLInterface[])
                    interfaces.values().toArray(new WSDLInterface[interfaces.size()]);
            for (int i = 0; i < interfacesArray.length; i++) {
                unwrapSchemaForInterface(interfacesArray[i]);
            }


        }

    }

    /**
     * Process a single wsdlInterface
     * @param wsdlInterface
     * @param decription
     */
    private static void unwrapSchemaForInterface(WSDLInterface wsdlInterface){
        // we should be getting all the operation since we also need to consider the inherited ones
        Map operationsMap = wsdlInterface.getAllOperations();
        if (!operationsMap.isEmpty()){
            WSDLOperation[] operations = (WSDLOperation[])
                    operationsMap.values().toArray(new WSDLOperation[operationsMap.size()]);
            WSDLOperation operation;
            for (int i = 0; i < operations.length; i++) {
                operation = operations[i];
                // There's is no such concept as having multiple output parameters
                // (atleast in java) so our focus is only in the input message reference
                MessageReference inputMessage = operation.getInputMessage();
                processMessageReference(inputMessage);
            }
        }


    }

    /**
     * Processes a message reference,
     * What we do is to store the relevant message elements inside
     * the metadata bag for further use. The unwrapping algorithm
     * only looks at the sequnece and not anything else
     *
     * @param messageReference
     */

    private static void processMessageReference(MessageReference messageReference) {
        XmlSchemaElement elt = messageReference.getElementSchema();
        if (elt!=null){
            XmlSchemaType schemaType = elt.getSchemaType();
            // if the schema is a complex type then we are interested!
            if (schemaType instanceof XmlSchemaComplexType) {
                XmlSchemaComplexType complexType = (XmlSchemaComplexType)schemaType;
                if (complexType.getParticle()!=null){
                    XmlSchemaParticle particle = complexType.getParticle();
                    if (particle instanceof XmlSchemaSequence){
                        //fine! We have a sequence. so we need to traverse through this
                        //and find the nested elements
                        XmlSchemaSequence sequence = (XmlSchemaSequence)particle;
                        XmlSchemaElement internalElement;
                        XmlSchemaObjectCollection items = sequence.getItems();
                        for (int i = 0; i < items.getCount(); i++) {
                             if (items.getItem(i) instanceof XmlSchemaElement){
                                 internalElement = (XmlSchemaElement)items.getItem(i);
                                 //attach this element to the metadatabag of the message reference
                                  messageReference.getMetadataBag().put(
                                          internalElement.getQName(),
                                          internalElement);
                             }
                        }
                    }
                }
            }
        }
    }

}
