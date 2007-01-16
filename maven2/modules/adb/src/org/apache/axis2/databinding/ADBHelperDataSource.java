package org.apache.axis2.databinding;

import org.apache.axiom.om.OMDataSource;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axiom.om.util.StAXUtils;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLStreamReader;
import java.io.OutputStream;
import java.io.Writer;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
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

public abstract class ADBHelperDataSource implements OMDataSource {

    protected QName parentQName;
    protected Object bean;
    protected String helperClassName;

    /**
     * Constructor taking in an ADBBean
     * @param bean
     */
    protected ADBHelperDataSource(Object bean,QName parentQName,String helperClassName) {
        this.bean = bean;
        this.parentQName = parentQName;
        this.helperClassName = helperClassName;
    }


    /**
     * @see OMDataSource#serialize(java.io.OutputStream, org.apache.axiom.om.OMOutputFormat)
     * @param output
     * @param format
     * @throws javax.xml.stream.XMLStreamException
     */
    public void serialize(OutputStream output, OMOutputFormat format) throws XMLStreamException {
       serialize(StAXUtils.createXMLStreamWriter(output));
    }

    /**
     * @see OMDataSource#serialize(java.io.Writer, org.apache.axiom.om.OMOutputFormat)
     * @param writer
     * @param format
     * @throws XMLStreamException
     */
    public void serialize(Writer writer, OMOutputFormat format) throws XMLStreamException {
        serialize(StAXUtils.createXMLStreamWriter(writer));
    }

    /**
     * This needs to be generated inside the ADB bean
     * @see OMDataSource#serialize(javax.xml.stream.XMLStreamWriter)
     * @param xmlWriter
     * @throws XMLStreamException
     */
    public abstract void serialize(XMLStreamWriter xmlWriter)
            throws XMLStreamException;


    /**
     * @see org.apache.axiom.om.OMDataSource#getReader()
     * @throws XMLStreamException
     */
    public XMLStreamReader getReader() throws XMLStreamException {
        // since only ADBBeans related to elements can be serialized
        try {
            Class helperClass = Class.forName(helperClassName);
            Method method = helperClass.getMethod("getPullParser",new Class[]{Object.class,
                                               QName.class});
            return (XMLStreamReader)method.invoke(null,new Object[]{bean,parentQName});
        } catch (ClassNotFoundException e) {
            throw new XMLStreamException(e);
        } catch (NoSuchMethodException e) {
           throw new XMLStreamException(e);
        } catch (IllegalAccessException e) {
             throw new XMLStreamException(e);
        } catch (InvocationTargetException e) {
            throw new XMLStreamException(e);
        }

    }

}
