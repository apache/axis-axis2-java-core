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
package org.apache.axis2.schema.restriction;

import junit.framework.TestCase;
import org.tempuri.SimpleRestriction;
import org.tempuri.BusinessObjectDocumentType;
import org.tempuri.NormalizedStringType;
import org.apache.axis2.databinding.types.NormalizedString;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.util.StAXUtils;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;

public class SimpleRestrictionTest extends TestCase {

    public void testSimpleRestriction() {
        SimpleRestriction simpleRestriction = new SimpleRestriction();
        BusinessObjectDocumentType businessObjectDocumentType = new BusinessObjectDocumentType();
        simpleRestriction.setSimpleRestriction(businessObjectDocumentType);
        NormalizedStringType releaseID = new NormalizedStringType();
        NormalizedStringType versionID = new NormalizedStringType();
        businessObjectDocumentType.setReleaseID(releaseID);
        businessObjectDocumentType.setVersionID(versionID);
        releaseID.setNewNormalizedStringType(new NormalizedString("releaseID"));
        versionID.setNewNormalizedStringType(new NormalizedString("versionID"));

        OMElement omElement = simpleRestriction.getOMElement(SimpleRestriction.MY_QNAME,
                OMAbstractFactory.getOMFactory());

        try {
            String omElementString = omElement.toStringWithConsume();
            System.out.println("OM Element ==>" + omElement);
            XMLStreamReader xmlReader =  StAXUtils.createXMLStreamReader(new ByteArrayInputStream(omElementString.getBytes()));
            SimpleRestriction newSimpleRestriction = SimpleRestriction.Factory.parse(xmlReader);
            assertEquals(newSimpleRestriction.getSimpleRestriction().getVersionID().toString(),"versionID");
            assertEquals(newSimpleRestriction.getSimpleRestriction().getReleaseID().toString(),"releaseID");
        } catch (Exception e) {
            assertFalse(true);
        }


    }

}
