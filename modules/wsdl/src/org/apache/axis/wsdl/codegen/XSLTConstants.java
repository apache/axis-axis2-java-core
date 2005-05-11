package org.apache.axis.wsdl.codegen;

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
*   Constants for the XSLT related items
*/
public interface XSLTConstants {
    /**
     * Language constants
     */
    public interface LanguageTypes{

        public static final int JAVA=1;
        public static final int C_SHARP=2;
        public static final int C_PLUS_PLUS=3;
        public static final int VB_DOT_NET=4;
    }

    /**
     * Interface templates
     */
    public interface XSLTInterfaceTemplates{
        public static final String JAVA_TEMPLATE = "/org/apache/axis/wsdl/template/java/InterfaceTemplate.xsl";
        public static final String CSHARP_TEMPLATE = "/org/apache/axis/wsdl/template/csharp/InterfaceTemplate.xsl";
    }

    /**
     * Interface implementation templates
     */
    public interface XSLTInterfaceImplementationTemplates{
        public static final String JAVA_TEMPLATE = "/org/apache/axis/wsdl/template/java/InterfaceImplementationTemplate.xsl";
        public static final String CSHARP_TEMPLATE = "/org/apache/axis/wsdl/template/csharp/InterfaceImplementationTemplate.xsl";
    }

    /**
     * Interface bean templates
     */
    public interface XSLTBeanTemplates{
        public static final String JAVA_TEMPLATE = "/org/apache/axis/wsdl/template/java/BeanTemplate.xsl";
        public static final String CSHARP_TEMPLATE = "/org/apache/axis/wsdl/template/csharp/BeanTemplate.xsl";
    }
}
