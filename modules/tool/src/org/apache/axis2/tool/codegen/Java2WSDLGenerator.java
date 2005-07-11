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
package org.apache.axis.tool.codegen;


import org.apache.axis.utils.ClassUtils;
import org.apache.axis.wsdl.fromJava.Emitter;

import java.util.Vector;

/**
 * @author Ajith
 *         <p/>
 *         TODO To change the template for this generated type comment go to Window -
 *         Preferences - Java - Code Style - Code Templates
 */
public class Java2WSDLGenerator {

    public void emit(String classPath,
                     String implementationClassName,
                     String serviceLocationUrl,
                     String inputWsdlName,
                     String bindingName,
                     String portypeName,
                     String style,
                     String outputFileName,
                     int mode,
                     Vector listOfIncludedMethods) throws Throwable {
        try {

            ClassUtils.setDefaultClassLoader(
                    ClassUtils.createClassLoader(classPath,
                            this.getClass().getClassLoader()));

            // Instantiate the emitter
            Emitter emitter = new Emitter();

            //implementation class
            emitter.setCls(implementationClassName);

            //service location
            if (serviceLocationUrl != null &&
                    !serviceLocationUrl.trim().equals(""))
                emitter.setLocationUrl(serviceLocationUrl);
            
            //input wsdl
            if (inputWsdlName != null && !inputWsdlName.trim().equals(""))
                emitter.setInputWSDL(inputWsdlName);
            
            //portype name
            if (portypeName != null && !portypeName.trim().equals(""))
                emitter.setPortTypeName(portypeName);
            
            //Style
            if (style != null && !style.trim().equals(""))
                emitter.setStyle(style);
            if (listOfIncludedMethods != null)
                emitter.setAllowedMethods(listOfIncludedMethods);

            if (mode != Emitter.MODE_ALL &&
                    mode != Emitter.MODE_IMPLEMENTATION &&
                    mode != Emitter.MODE_INTERFACE)
                mode = Emitter.MODE_ALL; // Default to all in unknown case

            emitter.emit(outputFileName, mode);

        } catch (Throwable t) {
            throw t;

        }
    }
}
