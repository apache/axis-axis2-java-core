/*
 * Copyright 2001-2004 The Apache Software Foundation.
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
package org.apache.axis2.wsdl.builder;

import org.apache.axis2.wsdl.WSDLVersionWrapper;

import javax.wsdl.WSDLException;
import java.io.InputStream;

/**
 * @author chathura@opensource.lk
 */
public interface WOMBuilder {

    public WSDLVersionWrapper build(InputStream in) throws WSDLException;
    public WSDLVersionWrapper build(InputStream in,String baseURI) throws WSDLException;
    public WSDLVersionWrapper build(InputStream in,
                                    WSDLComponentFactory wsdlComponentFactory) throws WSDLException;
}
