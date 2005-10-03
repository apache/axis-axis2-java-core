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


public interface WOMWriter {

    /**
     * Writes a WOM to the given output stream.
     * @param wsdlWrapper, A wrapper for both the wsdl object models. Contains references to WOM and
     * the standard WSDL definition class
     * @param out the output stream
     */
    public void writeWOM(WSDLVersionWrapper wsdlWrapper, OutputStream out) throws WriterException;


    /**
     * Write a WSDLDescription directly. The version wrapper may not be available at some instances
     * and the user might need to serailize the decription directly 
     * @param wsdlDescription
     * @param out
     * @throws WriterException
     */
    public void writeWOM(WSDLDescription wsdlDescription,OutputStream out) throws WriterException;

    public void setEncoding(String encoding);





}
