package org.apache.axis2.wsdl.codegen;

import org.apache.axis2.wsdl.util.ConfigPropertyFileLoader;

import javax.xml.transform.URIResolver;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;
import java.io.InputStream;
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

public class XSLTIncludeResolver implements URIResolver,XSLTConstants {


    public Source resolve(String href, String base) throws TransformerException {
        if (XSLT_INCLUDE_DATABIND_SUPPORTER_HREF.equals(href)){
            String supporterTemplate = ConfigPropertyFileLoader.getDbSupporterTemplateName();
            if(supporterTemplate!=null){
                InputStream supporterTemplateStream = getClass().getResourceAsStream(supporterTemplate);
                return new StreamSource(supporterTemplateStream);
            } else{
                throw new TransformerException("Databinding template not found!");
            }

        }

        return null;
    }
}
