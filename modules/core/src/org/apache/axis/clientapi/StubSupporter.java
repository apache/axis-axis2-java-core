package org.apache.axis.clientapi;

import org.apache.axis.om.OMElement;
import org.apache.axis.om.OMFactory;
import org.apache.axis.om.OMNamespace;

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
 *
 *  A utility class for the use of the stub
 *  Not visible outside
 */
class StubSupporter {

    public static OMElement createRPCMappedElement(String elementName,OMNamespace ns,Object value, OMFactory fac){
       OMElement returnElement = fac.createOMElement(elementName,ns);
       Class inputParamClass = value.getClass();

       if (inputParamClass.equals(String.class)){
           returnElement.addChild(fac.createText(returnElement,value.toString()));
       }else if (inputParamClass.equals(Integer.class)){
            returnElement.addChild(fac.createText(returnElement,String.valueOf(((Integer)value).intValue())));
       }else if (inputParamClass.equals(Float.class)){
            returnElement.addChild(fac.createText(returnElement,String.valueOf(((Float)value).floatValue())));
       }else if (inputParamClass.equals(Double.class)){
            returnElement.addChild(fac.createText(returnElement,String.valueOf(((Double)value).doubleValue())));
       //todo this seems to be a long list... need to complete this
       }else if (inputParamClass.equals(OMElement.class)){
           returnElement.addChild((OMElement)value);
       }else{
           returnElement.addChild(fac.createText(returnElement,value.toString()));
       }
        return returnElement;
    }

    public static Object getRPCMappedElementValue(OMElement elt, Class outputTypeClass){
       Object outputObj = null;
       if (outputTypeClass.equals(String.class)){
           outputObj = elt.getText();
       }else if (outputTypeClass.equals(Integer.class)){
            outputObj = new Integer(elt.getText());
       }else if (outputTypeClass.equals(Float.class)){
            outputObj = new Float(elt.getText());
       }else if (outputTypeClass.equals(Double.class)){
            outputObj = new Double(elt.getText());

       //todo this seems to be a long list... need to complete this

       }else if (outputTypeClass.equals(OMElement.class)){
           outputObj = elt;
       }else{
           outputObj = elt.toString();
       }

        return outputObj;
    }

}
