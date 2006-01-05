package org.apache.axis2.wsdl.writer;

import java.io.IOException;
import java.io.Writer;
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
// Enforcing package access
class WriterUtil {

    /**
     * Write a start element
     * @param elementName
     * @param writer
     * @throws IOException
     */
    public static void writeStartElement(String elementName, Writer writer) throws IOException {
        writer.write("<" + elementName);
    }

    /**
     *
     * @param elementName
     * @param nsPrefix
     * @param writer
     * @throws IOException
     */
    public static void writeStartElement(String elementName,String nsPrefix, Writer writer) throws IOException {
        if (nsPrefix==null){
            writeStartElement(elementName,writer);
        }else{
            writer.write("<" + nsPrefix+":"+elementName);
        }

    }


    /**
     * Close start Element
     * @param elementName
     * @param writer
     * @throws IOException
     */
    public static void writeCloseStartElement(Writer writer) throws IOException {
        writer.write(">\n");
    }
    /**
     * write an attrib
     * @param attName
     * @param value
     * @param writer
     * @throws IOException
     */
    public static void writeAttribute(String attName,String value, Writer writer) throws IOException {
        writer.write(" " + attName + "=\""+value + "\"");
    }

    /**
     * Write end element
     * @param attName
     * @param value
     * @param writer
     * @throws IOException
     */
    public static void writeEndElement(String eltName, Writer writer) throws IOException {
        writer.write("</" + eltName + ">\n");
    }

    /**
     * Write end element
     * @param writer
     * @throws IOException
     */
    public static void writeCompactEndElement(Writer writer) throws IOException {
        writer.write("/>\n");
    }
    /**
     * Write end element
     * @param attName
     * @param value
     * @param writer
     * @throws IOException
     */
    public static void writeEndElement(String eltName,String nsPrefix, Writer writer) throws IOException {
        if (nsPrefix==null){
            writeEndElement(eltName,writer);
        }else{
            writer.write("</" + nsPrefix+":"+eltName + ">\n");
        }
    }

    public static void writeNamespace(String prefix,String namespaceURI,Writer writer) throws IOException{
        if (prefix==null || prefix.trim().length()==0){
            writeAttribute("xmlns",namespaceURI,writer);
        }else{
            writeAttribute("xmlns:"+prefix,namespaceURI,writer);
        }
    }

    public static void writeComment(String comment,Writer writer) throws IOException{
          writer.write("<!--" + comment + "-->");
       }


}
