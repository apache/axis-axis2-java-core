package org.apache.axis2.wsdl.codegen.extension;

import org.apache.axis2.wsdl.codegen.CodeGenConfiguration;
import org.apache.axis2.wsdl.codegen.CodeGenerationException;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.axis2.wsdl.WSDLUtil;
import org.apache.axis2.wsdl.util.MessagePartInformationHolder;
import org.apache.axis2.wsdl.util.Constants;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisMessage;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.AxisFault;
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

                if (WSDLUtil.isInputPresentForMEP(op.getMessageExchangePattern())){
                    walkSchema(op.getMessage(
                            WSDLConstants.MESSAGE_LABEL_IN_VALUE),
                            configuration);
                }

            }
        }
    }

    /**
     * walk the given schema element
     */

    public void walkSchema(AxisMessage message,CodeGenConfiguration config)
            throws CodeGenerationException{
        //nothing to unwrap
        if (message.getSchemaElement()==null){
            return;
        }

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
                        String partName = ((XmlSchemaElement) item).getName();

                        //  part names are not unique across messages. Hence
                        //  we need some way of making the part name a unique
                        //  one (due to the fact that the type mapper
                        //  is a global list of types).
                        //  The seemingly best way to do that is to
                        //  specify a namespace for the part QName reference which
                        //  is stored in the  list. This part qname is
                        //  temporary and should not be used with it's
                        //  namespace URI (which happened to be the operation name)
                        //  with _input attached to it


                        partNameList.add(
                                WSDLUtil.getPartQName(opName.getLocalPart(),
                                        WSDLConstants.INPUT_PART_QNAME_SUFFIX,
                                        partName));


                    }else{
                        // if the particle contains anything other than
                        // a XMLSchemaElement then we are not in a position
                        // to unwrap it
                        //in this case just do nothing and return breaking
                        //the whole thing
                        return;
                    }
                }

                try {
                    //set in the axis message that the unwrapping was success
                    message.addParameter(getParameter(
                            Constants.UNWRAPPED_KEY,
                            Boolean.TRUE));

                    // attach the opName and the parts name list into the
                    // axis message by using the holder
                    MessagePartInformationHolder infoHolder = new MessagePartInformationHolder();
                    infoHolder.setOperationName(opName);
                    infoHolder.setPartsList(partNameList);

                    //attach it to the parameters
                    message.addParameter(
                            getParameter(Constants.UNWRAPPED_DETAILS,
                                    infoHolder));

                } catch (AxisFault axisFault) {
                    throw new CodeGenerationException(axisFault);
                }


            }else{
                //we do not know how to deal with other particles
                //such as xs:all or xs:choice. Usually occurs when
                //passed with the user built WSDL where the style
                //is document. We'll just return here doing nothing

            }
        }else{
            //we've no idea how to unwrap a non complexYype!!!!!!
        }

    }

    /**
     * Generate a parametes object
     * @param key
     * @param value
     * @return
     */
    private Parameter getParameter(String key,Object value){
        Parameter myParameter = new Parameter();
        myParameter.setName(key);
        myParameter.setValue(value);

        return myParameter;
    }



}
