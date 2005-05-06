package org.apache.axis.wsdl.util;


import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;


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
 *  The XSLT template processor
 * this is based on the JDK built in transformers
 */
public class XSLTTemplateProcessor {

    /**
     * Parses an XML stream with an XSL stream
     * @param out Stream to write the output
     * @param xmlStream Source XML stream
     * @param xsltStream Source XSL stream
     * @throws TransformerFactoryConfigurationError
     * @throws TransformerException
     */
    public static  void parse(OutputStream out,InputStream xmlStream,InputStream xsltStream)
            throws TransformerFactoryConfigurationError,TransformerException {
            Source xmlSource = new StreamSource(xmlStream);
            Source xsltSource =  new StreamSource(xsltStream);
            Result result = new StreamResult(out);
            Transformer transformer = TransformerFactory.newInstance().newTransformer(xsltSource);
            transformer.transform(xmlSource, result);

    }



}
