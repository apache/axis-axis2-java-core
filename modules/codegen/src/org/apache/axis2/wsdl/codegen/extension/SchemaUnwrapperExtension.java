package org.apache.axis2.wsdl.codegen.extension;

import org.apache.axis2.wsdl.codegen.CodeGenConfiguration;
import org.apache.axis2.wsdl.codegen.CodeGenerationException;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisMessage;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaType;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaParticle;
import org.apache.ws.commons.schema.XmlSchemaSequence;
import org.apache.ws.commons.schema.XmlSchemaObjectCollection;

import javax.xml.namespace.QName;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
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
 * This extension invokes the schema unwrapper depending on the users setting.
 * it is desirable to put this extension before other extensions since extnsions
 * such as the databinding extension may well depend on the schema being unwrapped
 * previously
 */
public class SchemaUnwrapperExtension extends AbstractCodeGenerationExtension {

    /**
     *
     * @param configuration
     * @throws CodeGenerationException
     */
    public void engage(CodeGenConfiguration configuration) throws CodeGenerationException {
        if (!configuration.isParametersWrapped()){
            //walk the schema and find the top level elements
            AxisService axisService = configuration.getAxisService();

            for(Iterator operations = axisService.getOperations();
                operations.hasNext();){
                AxisOperation op = (AxisOperation)operations.next();

                //todo check whether we need to unwrap this message depending on
                //the binding state.

               // walkSchema(op.getMessage(
               //         WSDLConstants.MESSAGE_LABEL_IN_VALUE),configuration);
            }
        }
    }

    /**
     * walk the given schema element
     */

    public void walkSchema(AxisMessage message,CodeGenConfiguration config){

        XmlSchemaType schemaType = message.getSchemaElement().getSchemaType();
        //create a type mapper
        if (schemaType instanceof XmlSchemaComplexType){
            XmlSchemaComplexType cmplxType = (XmlSchemaComplexType)schemaType;
            XmlSchemaParticle particle = cmplxType.getParticle();
            if (particle instanceof XmlSchemaSequence){
                // get the name of the operation name and namespace,
                // part name and hang them somewhere ? The ideal place
                // would be the property bag in the codegen config!
                QName opName = ((AxisOperation)message.getParent()).getName();
                List partNameList = new LinkedList();

                XmlSchemaSequence sequence = (XmlSchemaSequence)particle;
                XmlSchemaObjectCollection items = sequence.getItems();
                for (Iterator i = items.getIterator();i.hasNext();){
                    Object item = i.next();
                    // get each and every element in the sequence and
                    // traverse through them
                    if (item instanceof XmlSchemaElement){
                        //add the element name to the part name list
                        partNameList.add(((XmlSchemaElement)item).getName());
                    }else{
                        // if the particle contains anything other than
                        // a XMLSchemaElement then we are not in a position
                        // to unwrap it
                        //todo throw an Exception ??
                    }
                }

                //attach the opName and the parts name list into the
                //codegen configuration
                // config.getConfigurationProperties()


            }else{
                //we do not know how to deal with other particles
                //such as xs:all or xs:choice
                //todo throw an Exception ??
            }
        }else{
            //we've no idea how to unwrap a non complex type!!!!!!
            //todo throw an Exception ??
        }

    }
}
