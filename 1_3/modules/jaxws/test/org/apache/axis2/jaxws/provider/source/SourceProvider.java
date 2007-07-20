/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.axis2.jaxws.provider.source;

import org.apache.axis2.jaxws.TestLogger;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.Provider;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceProvider;

@WebServiceProvider()
public class SourceProvider implements Provider<Source> {
    
    // Same logic as StringProvider
    public Source invoke(Source source) {

        TestLogger.logger.debug(">> SourceProvider: Request received.\n");
    	if (source == null) {
    	    return source;
        }
        
        // Non-null source
    	
    	StringWriter writer = new StringWriter();
    	try {
            Transformer t = TransformerFactory.newInstance().newTransformer();
            Result result = new StreamResult(writer);
            t.transform(source, result);
        } catch (TransformerConfigurationException e) {
            throw new WebServiceException(e);
        } catch (TransformerFactoryConfigurationError e) {
            throw new WebServiceException(e);
        } catch (TransformerException e) {
            throw new WebServiceException(e);
        }
    	String text = writer.getBuffer().toString();
        TestLogger.logger.debug(">> Source Request on Server: \n" + text);
    	
    	if (text != null && text.contains("throwWebServiceException")) {
    	    throw new WebServiceException("provider");
    	}
    	
    	ByteArrayInputStream stream = new ByteArrayInputStream(text.getBytes());
    	Source srcStream = new StreamSource((InputStream) stream);
    	return srcStream;
        
    }
    	

}
