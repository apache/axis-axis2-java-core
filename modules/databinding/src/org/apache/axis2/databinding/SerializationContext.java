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
package org.apache.axis2.databinding;

import org.apache.axis.xsd.Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * SerializationContext
 */
public class SerializationContext {

    protected Log log = LogFactory.getLog(getClass());


    // Multiref modes
    public static final int NO_MULTIREFS = 0;
    public static final int SOAP11_MULTIREFS = 1;
    public static final int SOAP12_MULTIREFS = 2;

    // Null handling modes
    public static final int NULL_OMIT = 0;
    public static final int NULL_NILLABLE = 1;

    static final QName MULTIREF_QNAME = new QName("MultiRef");

    Map multirefObjects = null;
    ArrayList multirefsToWrite = null;

    int multirefs = NO_MULTIREFS;
    int lastID = 0;
    boolean writingMultirefs = true;

    class Multiref {
        Object value;
        String id;
        Serializer serializer;
    }

    XMLStreamWriter writer = null;

    public SerializationContext(XMLStreamWriter writer) {
        this.writer = writer;
    }

    public void setMultirefBehavior(int multiref) {
        this.multirefs = multiref;
    }

    public void finish() throws Exception {
        while (multirefsToWrite != null) {
            // Writing actual data...
            writingMultirefs = false;

            ArrayList currentMultirefs = multirefsToWrite;
            multirefsToWrite = null;

            for (Iterator i = currentMultirefs.iterator(); i.hasNext();) {
                Multiref ref = (Multiref)i.next();
                writer.writeStartElement(MULTIREF_QNAME.getNamespaceURI(),
                                         MULTIREF_QNAME.getLocalPart());
                writer.writeAttribute("id", ref.id);
                ref.serializer.serializeData(ref.value, this);
            }
        }
    }

    public void serializeElement(QName qname,
                                 Object obj,
                                 Serializer serializer) throws Exception {
        serializeElement(qname, obj, NULL_OMIT, serializer);
    }

    public void serializeElement(QName qname,
                                 Object obj,
                                 int nullHandlingMode,
                                 Serializer serializer) throws Exception {
        if (obj == null) {
            switch (nullHandlingMode) {
                case NULL_NILLABLE:
                    // write xsi:nil
                    writer.writeStartElement(qname.getNamespaceURI(), qname.getLocalPart());
                    writer.writeAttribute(Constants.URI_2001_SCHEMA_XSI,
                                          "nil",
                                          "true");
                    writer.writeEndElement();
                default:
                    return;
            }

        }

        writer.writeStartElement(qname.getNamespaceURI(), qname.getLocalPart());
        serializer.serialize(obj, this);
    }

    public void serializeData(Object obj, Serializer ser) throws Exception {
        ser.serialize(obj, this);
    }

    public boolean checkMultiref(Object obj, Serializer serializer)
            throws Exception {
        switch (multirefs) {
            case SOAP11_MULTIREFS: {
                String id = getSOAP11IDForObject(obj, serializer);
                writer.writeAttribute("href", "#" + id);
                writer.writeEndElement();
                return true;
            }
            case SOAP12_MULTIREFS: {
                String id = getSOAP12IDForObject(obj);
                if (id != null) {
                    writer.writeAttribute("ref", "#" + id);
                    writer.writeEndElement();
                    return true;
                }
                id = getNewIDForObject(obj, serializer);
                writer.writeAttribute("id", id);
            }
        }
        return false;
    }

    /**
     * Obtain an ID for this object, which will be written (according to the
     * SOAP 1.1 multiref rules) as an independent element at the end of the
     * SOAP body - see finish()).
     *
     * @param obj
     * @return an ID, always.  Either a new one or a previously registered one.
     */
    public String getSOAP11IDForObject(Object obj, Serializer serializer) {
        if (multirefObjects != null) {
            String id = (String)multirefObjects.get(obj);
            if (id != null) return id;
        }

        return getNewIDForObject(obj, serializer);
    }

    public String getSOAP12IDForObject(Object obj) {
        if (multirefObjects == null) return null;
        return (String)multirefObjects.get(obj);
    }

    public String getNewIDForObject(Object obj, Serializer serializer) {
        if (multirefObjects == null)
            multirefObjects = new HashMap();
        lastID++;
        String id = "" + lastID;
        multirefObjects.put(obj, id);

        if (multirefs == SOAP11_MULTIREFS) {
            if (multirefsToWrite == null)
                multirefsToWrite = new ArrayList();
            Multiref ref = new Multiref();
            ref.id = id;
            ref.value = obj;
            ref.serializer = serializer;
            multirefsToWrite.add(ref);
        }

        return "" + lastID;
    }

    public XMLStreamWriter getWriter() {
        return writer;
    }

    public String qName2String(QName qname) {
        return qName2String(qname, true);
    }

    public String qName2String(QName qname, boolean doDefault) {
        String ns = qname.getNamespaceURI();
        NamespaceContext ctx = writer.getNamespaceContext();
        try {
            String prefix = writer.getPrefix(ns);
            if (prefix != null) {
                // Got a prefix
                return prefix + ":" + qname.getLocalPart();
            } else if (prefix.equals("")) {
                if (doDefault) {
                    // Default namespace, no prefix
                    return qname.getLocalPart();
                }

            } else {
                // need to map this NS
                writer.writeNamespace(prefix, ns);
            }
        } catch (XMLStreamException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }
}
