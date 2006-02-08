package org.apache.axis2.wsdl.builder;

import org.apache.wsdl.WSDLDescription;
import org.apache.wsdl.WSDLBinding;
import org.apache.wsdl.WSDLInterface;
import org.apache.wsdl.WSDLOperation;
import org.apache.ws.commons.schema.XmlSchemaElement;

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
                unwrapSchemaForInterface(bindingsArray[i].getBoundInterface(),description);

            }

        }else if (interfaces!=null && !interfaces.isEmpty()){
            //process the interfaces (porttypes)
            WSDLInterface[] interfacesArray = (WSDLInterface[])
                            interfaces.values().toArray(new WSDLInterface[interfaces.size()]);
            for (int i = 0; i < interfacesArray.length; i++) {
                 unwrapSchemaForInterface(interfacesArray[i],description);
            }


        }

    }

    /**
     * Process a single wsdlInterface
     * @param wsdlInterface
     * @param decription
     */
    private static void unwrapSchemaForInterface(WSDLInterface wsdlInterface,WSDLDescription decription){

        Map operationsMap = wsdlInterface.getOperations();
        if (!operationsMap.isEmpty()){
             WSDLOperation[] operations = (WSDLOperation[])
                    operationsMap.values().toArray(new WSDLOperation[operationsMap.size()]);
            WSDLOperation operation;
            for (int i = 0; i < operations.length; i++) {
                operation = operations[i];
                //process Schema
                XmlSchemaElement elt = operation.getInputMessage().getElementSchema();
            }
        }


    }

}
