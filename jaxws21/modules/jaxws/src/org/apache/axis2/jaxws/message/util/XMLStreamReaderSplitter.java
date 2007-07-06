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
package org.apache.axis2.jaxws.message.util;


import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * XMLStreamReaderSplitter An XMLStreamReaderSplitter wraps an existing XMLStreamReader and makes it
 * appear that the XMLStreamReader is for a single element tree. Once the element tree is consumed,
 * the getNextReader method may be called to get the next element tree.
 * <p/>
 * For example, suppose the input xml is the following:
 * <p/>
 * spurious text 1 <data> <a>a data</a> <b>b data</b> </data> spurious text 2 <data2> <a2>a
 * data</a2> <b2>b data</b2> </data2>
 * <p/>
 * The XMLStreamReaderSplitter will only return the events for: <data> <a>a data</a> <b>b data</b>
 * </data>
 * <p/>
 * Once these events are consumed, the nextStreamReader method can be called, which will return:
 * <data2> <a2>a data</a2> <b2>b data</b2> </data2>
 * <p/>
 * Note that the spurious text and comments between the elements are ignored.
 */
public class XMLStreamReaderSplitter extends XMLStreamReaderFilter {

    int depth = 0;
    boolean start = true;

    /**
     * Split the input reader into multiple element trees
     *
     * @param reader XMLStreamReader
     */
    public XMLStreamReaderSplitter(XMLStreamReader reader) {
        super(reader);
        init();
    }

    /** Reset for a new element tree */
    private void init() {
        depth = 0;
        start = true;
    }

    public int next() throws XMLStreamException {
        // If starting a new tree, ignore all of the
        // events until the first start element is found.
        if (start) {
            start = false;
            depth = 1;
            int event = super.next();

            while (event != START_ELEMENT) {
                if (event == START_ELEMENT) {
                    return event;
                } else if (this.hasNext()) {
                    event = super.next();
                } else {
                    return event;
                }
            }
            return event;
        } else {
            // Processing a tree.  Adjust the
            // depth as the tags are encountered.
            if (depth == 0) {
                // Some consumers expect an END_DOCUMENT to end their processing (some consumers will trigger the end of processing using hasNext())
                return END_DOCUMENT;
            }
            int event = super.next();
            if (event == START_ELEMENT) {
                depth++;
            } else if (event == END_ELEMENT) {
                depth--;
            }
            return event;
        }
    }

    @Override
    public boolean hasNext() throws XMLStreamException {
        if (depth == 0) {
            // If this element tree is consumed return false
            if (start) {
                return super.hasNext();
            } else {
                return false;
            }
        } else {
            return super.hasNext();
        }
    }


    /**
     * getStreamReader
     *
     * @return XMLStreamReader for the next element tree. If the prior stream reader is not consumed,
     *         this method will return null. (Use hasNext() to see if the prior stream reader is
     *         consumed)
     * @throws XMLStreamReader
     */
    public XMLStreamReader getNextReader() throws XMLStreamException {
        if (!hasNext()) {
            init();
            if (hasNext()) {
                // Return newly initialized reader
                return this;
            } else {
                // Return null indicating no reader is available
                return null;
            }
        } else {
            // Return null if the current reader is still be read
            return null;
        }
    }

    @Override
    public void close() throws XMLStreamException {
        // Swallow close for intermediate readers
        if (!super.hasNext()) {
            super.close();
        }
    }

}
