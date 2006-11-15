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

import java.util.HashMap;

import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ParameterFactory {
    
    private static HashMap<Class, Class> supportedSourceTypes;
    //private static final Log log = LogFactory.getLog(ParameterFactory.class);
    
    static {
        supportedSourceTypes = new HashMap<Class, Class>();
        supportedSourceTypes.put(StreamSource.class, StreamSourceParameter.class);
        supportedSourceTypes.put(DOMSource.class, DOMSourceParameter.class);
        supportedSourceTypes.put(SAXSource.class, SAXSourceParameter.class);
        supportedSourceTypes.put(Source.class, StreamSourceParameter.class);
    }
    
    /**
     * Creates a Parameter instance based on the class type of the 
     * object passed in and then uses that object instance as the content
     * 
     * @param o
     * @return
     */
    public static Parameter createParameter(Object o) {
        Class c = o.getClass();
        
        //if (log.isDebugEnabled()) {
        //    log.debug("Creating Parameter from existing object of type [" + c.getName() + "]");
        //}
        System.out.println(">> Creating Parameter from existing object of type [" + c.getName() + "]");
        if (o instanceof String) {
            return new StringParameter((String) o);
        }
        else if(o instanceof SOAPMessage){
        	return new SOAPMessageParameter((SOAPMessage) o);
        }
        else if(Source.class.isAssignableFrom(c)) {
            Parameter p = getSourceParameter(o.getClass());
            p.setValue(o);
            return p;
        }
        else {
            JAXBParameter p = new JAXBParameter();
            p.setValue(o);
            return p;
        }
    }
    
    /**
     * Creates a Parameter instance based on the class type
     * 
     * @param c
     * @return
     */
    public static Parameter createParameter(Class c) {
        //if (log.isDebugEnabled()) {
        //    log.debug("Creating Parameter Class type [" + c.getName() + "]");
        //}
        System.out.println("Creating Parameter Class type [" + c.getName() + "]");
        if (c == String.class) {
            return new StringParameter();
        }
        else if(c == SOAPMessage.class) {
            return new SOAPMessageParameter();
        }
        else if(Source.class.isAssignableFrom(c)) {
            Parameter p = getSourceParameter(c);
            return p;
        }
        else {
            return new JAXBParameter();
        }
    }
    
    private static Parameter getSourceParameter(Class c) {
        Parameter p = null;
        Class<Parameter> paramClass = supportedSourceTypes.get(c);
        
        if (paramClass != null) {
            try {
                p = paramClass.newInstance();
                System.out.println(">> New " + p.getClass().getName() + " created");
                //if (log.isDebugEnabled()) {
                //    log.debug("New " + p.getClass().getName() + " created");
                //}
                
                return p;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        return p;
    }
}
