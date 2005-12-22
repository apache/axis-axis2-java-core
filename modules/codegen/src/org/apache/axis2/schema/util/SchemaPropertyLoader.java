package org.apache.axis2.schema.util;

import org.apache.axis2.schema.SchemaCompiler;
import org.apache.axis2.schema.typemap.TypeMap;
import org.apache.axis2.schema.writer.BeanWriter;

import java.util.Properties;
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

/**
 * Loads the properties  for the schema compiler
 */
public class SchemaPropertyLoader {
    private static String unwrappedBeanTemplate = null;
    private static BeanWriter beanWriterInstance = null;
    private static TypeMap typeMapperInstance = null;

    private static final String SCHEMA_COMPILER_PROPERTIES = "/org/apache/axis2/schema/schema-compile.properties";
    private static final String BEAN_WRITER_KEY = "schema.bean.writer.class";
    private static final String BEAN_WRITER_UNWRAPPED_TEMPLATE_KEY = "schema.bean.writer.template.wrapped";
    private static final String BEAN_WRITER_TYPEMAP_KEY = "schema.bean.typemap";

    static {
        try {
            //load the properties
            Properties props = new Properties();
            props.load(SchemaCompiler.class.getResourceAsStream(SCHEMA_COMPILER_PROPERTIES));

            String beanWriterClassName = props.getProperty(BEAN_WRITER_KEY);
            if (beanWriterClassName != null) {
                beanWriterInstance = (BeanWriter) Class.forName(beanWriterClassName).newInstance();
            }

            String typeMapperClassName = props.getProperty(BEAN_WRITER_TYPEMAP_KEY);
            if (typeMapperClassName != null) {
                typeMapperInstance = (TypeMap) Class.forName(typeMapperClassName).newInstance();
            }

            unwrappedBeanTemplate = props.getProperty(BEAN_WRITER_UNWRAPPED_TEMPLATE_KEY);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }

    public static String getUnwrappedBeanTemplate() {
        return unwrappedBeanTemplate;
    }

    public static BeanWriter getBeanWriterInstance() {
        return beanWriterInstance;
    }

    public static TypeMap getTypeMapperInstance() {
        return typeMapperInstance;
    }
}
