package org.apache.axis2.wsdl.util;

import org.apache.axis2.wsdl.i18n.CodegenMessages;
import org.apache.axis2.wsdl.util.ConfigPropertyFileLoader;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;
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

    private Map externalPropertyMap;

    public XSLTIncludeResolver() {
    }

    public XSLTIncludeResolver(Map externalPropertyMap) {
        this.externalPropertyMap = externalPropertyMap;
    }

    public void setExternalPropertyMap(Map externalPropertyMap) {
        this.externalPropertyMap = externalPropertyMap;
    }

    public Source resolve(String href, String base) throws TransformerException {
        String templateName;
        InputStream supporterTemplateStream;
        if (XSLT_INCLUDE_DATABIND_SUPPORTER_HREF_KEY.equals(href)){
            return getSourceFromTemplateName(ConfigPropertyFileLoader.getDbSupporterTemplateName());
        }

        if (XSLT_INCLUDE_TEST_OBJECT_HREF_KEY.equals((href))){
              return getSourceFromTemplateName(ConfigPropertyFileLoader.getTestObjectTemplateName());
        }

        if (externalPropertyMap.get(href)!=null){
            templateName = externalPropertyMap.get(href).toString();
            if(templateName!=null){
                supporterTemplateStream = getClass().getResourceAsStream(templateName);
                return new StreamSource(supporterTemplateStream);
            }
        }
        //if nothing could be found return an empty source
        return getEmptySource();
    }

    private Source getSourceFromTemplateName(String templateName) throws TransformerException {
        InputStream supporterTemplateStream;
        if(templateName!=null){
            supporterTemplateStream = getClass().getResourceAsStream(templateName);
            return new StreamSource(supporterTemplateStream);
        } else{
            throw new TransformerException(CodegenMessages.getMessage("reslover.templateNotFound",templateName));
        }
    }

    private Source getEmptySource(){
        return new StreamSource(new ByteArrayInputStream("<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\"/>".getBytes()));
    }
}
