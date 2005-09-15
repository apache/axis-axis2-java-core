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
package org.apache.axis2.saaj;

import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMFactory;
import org.apache.axis2.om.OMNamespace;
import org.apache.axis2.soap.SOAPFactory;
import org.apache.axis2.soap.SOAPFaultDetail;

import javax.xml.soap.*;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Class DetailImpl
 *
 * @author Ashutosh Shahi
 *         ashutosh.shahi@gmail.com
 */
public class DetailImpl extends SOAPFaultElementImpl implements Detail {

    /**
     * Field detail
     */
    protected SOAPFaultDetail detail;

    /**
     * Constructor DetailImpl
     *
     * @param detailName
     * @param parent
     */
    public DetailImpl(SOAPFault parent) {
        SOAPFactory soapFactory = OMAbstractFactory.getDefaultSOAPFactory();
        org.apache.axis2.soap.SOAPFault omFault = ((SOAPFaultImpl) parent).getOMFault();
        detail = soapFactory.createSOAPFaultDetail(omFault);
    }

    public DetailImpl(SOAPFaultDetail detail) {
        this.detail = detail;
    }

    /**
     * Method addDetailEntry
     *
     * @param name
     * @return
     * @throws SOAPException
     * @see javax.xml.soap.Detail#addDetailEntry(javax.xml.soap.Name)
     */
    public DetailEntry addDetailEntry(Name name) throws SOAPException {

        String localName = name.getLocalName();
        OMFactory omFactory = OMAbstractFactory.getOMFactory();
        OMNamespace ns = omFactory.createOMNamespace(name.getURI(),
                name.getPrefix());
        OMElement detailEntry = omFactory.createOMElement(localName, ns);
        detail.addDetailEntry(detailEntry);
        return (new DetailEntryImpl(detailEntry));
    }

    /**
     * Method addDetailEntry
     *
     * @param detailEntry
     * @return
     */
    protected DetailEntry addDetailEntry(
            org.apache.axis2.om.OMElement detailEntry) {
        detail.addDetailEntry(detailEntry);
        return (new DetailEntryImpl(detailEntry));
    }

    /**
     * Method getDetailEntries
     *
     * @return
     * @see javax.xml.soap.Detail#getDetailEntries()
     */
    public Iterator getDetailEntries() {
        // Get the detailEntried which will be omElements
        // convert them to soap DetailEntry and return the iterator
        Iterator detailEntryIter = detail.getAllDetailEntries();
        ArrayList aList = new ArrayList();
        while (detailEntryIter.hasNext()) {
            Object o = detailEntryIter.next();
            if (o instanceof org.apache.axis2.om.OMElement) {
                OMElement omDetailEntry = (OMElement) o;
                DetailEntry detailEntry = new DetailEntryImpl(omDetailEntry);
                aList.add(detailEntry);
            }
        }
        return aList.iterator();
    }

}
