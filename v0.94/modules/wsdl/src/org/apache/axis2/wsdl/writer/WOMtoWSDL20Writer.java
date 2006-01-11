package org.apache.axis2.wsdl.writer;

import org.apache.axis2.wsdl.WSDLVersionWrapper;
import org.apache.wsdl.WSDLDescription;

import java.io.OutputStream;
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

public class WOMtoWSDL20Writer implements WOMWriter {

    public void setEncoding(String encoding) {
        throw new UnsupportedOperationException();
    }

    //to set the defaultWSDLPrefix for the wsdl file
    public void setdefaultWSDLPrefix(String defaultWSDLPrefix) {
        throw new UnsupportedOperationException("Not complete!");
    }

    /**
     * @param wsdlWrapper
     * @param out
     * @throws WriterException
     */
    public void writeWOM(WSDLVersionWrapper wsdlWrapper, OutputStream out) throws WriterException {
        throw new WriterException("Not complete!");
    }

    /**
     * @param wsdlDescription
     * @param out
     * @throws WriterException
     */
    public void writeWOM(WSDLDescription wsdlDescription, OutputStream out) throws WriterException {
        throw new WriterException("Not complete!");
    }
}
