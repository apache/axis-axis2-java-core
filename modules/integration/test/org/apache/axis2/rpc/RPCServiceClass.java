package org.apache.axis2.rpc;

import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMNamespace;
import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.soap.SOAPFactory;

import javax.xml.namespace.QName;
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
*
*/

/**
 * Author: Deepal Jayasinghe
 * Date: Oct 11, 2005
 * Time: 10:29:05 PM
 */
public class RPCServiceClass {

    public MyBean editBean(MyBean bean , int a){
        bean.setAge(a);
        return bean;
    }

    public MyBean echoBean(MyBean bean){
        return bean;
    }

    public String echoString(String in){
        return in;
    }

    public int echoInt(int i){
        return i;
    }

    public int add(int a , int b){
        return a+b;
    }

    public boolean echoBool(boolean b){
        return b;
    }

    public byte echoByte(byte b){
        return b;
    }
    public OMElement echoOM(OMElement b){
        SOAPFactory fac =   OMAbstractFactory.getSOAP12Factory();
        OMNamespace ns = fac.createOMNamespace(
                "http://soapenc/", "res");
        OMElement bodyContent = fac.createOMElement(new QName("echoOM") + "Response", ns);
        bodyContent.addChild(b);
        return bodyContent;
    }

    public double divide(double a , double b){
       return (a/b);
    }
}
