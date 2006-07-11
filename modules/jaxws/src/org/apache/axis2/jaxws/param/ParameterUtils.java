/*
 * Copyright 2004,2005 The Apache Software Foundation.
 * Copyright 2006 International Business Machines Corp.
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

package org.apache.axis2.jaxws.param;

import javax.xml.ws.WebServiceException;
import javax.xml.ws.Service.Mode;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder;
import org.apache.axis2.jaxws.util.SoapUtils;


public class ParameterUtils {

    /**
     * This utility method can be used to convert a single Parameter object into a 
     * SOAP envelope.  
     * 
     * If the MODE is MESSAGE, it assumes that the parameter content
     * has a valid SOAP envelope inside of it, with &lt;Envelope&gt; as root node. If not 
     * then it will throw a WebServiceException.
     * 
     * If the MODE is PAYLOAD, then it builds a SOAPEnvelope representation and then sets
     * the param content as the first child of the &lt;Body&gt; element.
     * 
     * @param mode - A flag that determines the scope of the content
     * @param soapVersionURI - A String with the correct version of the SOAP URI
     * @param param - The actual Parameter instance to be acted upon
     * @return
     */
    public static SOAPEnvelope toEnvelope(Mode mode, String soapVersionURI, Parameter param) {
        SOAPFactory soapfactory =  SoapUtils.getSoapFactory(soapVersionURI);
        SOAPEnvelope env = null;
        try{
            if(mode != null && mode.equals(Mode.MESSAGE)){   
                StAXSOAPModelBuilder builder = new StAXSOAPModelBuilder(param.getValueAsStreamReader(),
                        soapfactory, soapVersionURI);
                return builder.getSOAPEnvelope();
            }
            else{
                env = soapfactory.getDefaultEnvelope();
                env.getHeader().detach();
                SOAPBody body = env.getBody();
                body.addChild(param.toOMElement());
            }
        }catch(Exception e){
            throw new WebServiceException(e.getMessage());
        }
       return env;
    }

    /**
     * This utility method creates a Parameter instance based on the data
     * provided in the SOAPEnvelope.
     * 
     * If the MODE is MESSAGE, the Parameter that gets created will contain
     * the entire SOAP envelope in whatever form was specified (String, Source, etc.)
     * 
     * If the MODE is PAYLOAD, then the Parameter created will be based on the 
     * contents of the SOAP body.
     * 
     * @param mode
     * @param env
     */
    public static void fromEnvelope(Mode mode, SOAPEnvelope env, Parameter param) {
        if(env == null){
            //add validation code here
        }
        
        if(mode == null || mode.equals(Mode.PAYLOAD)){
            OMElement om = env.getBody();
            param.fromOM(om.getFirstElement());
        }
        else if(mode.equals(Mode.MESSAGE)){
            param.fromOM(env);
        }
    }
}
