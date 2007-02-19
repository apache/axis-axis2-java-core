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
package org.apache.axis2.schema.references;

import com.americanexpress.www.wsdl.ctn.utilities.atb.AtbRequestCheckEligibility_type0;
import com.americanexpress.www.wsdl.ctn.utilities.atb.CheckEligibility1;
import com.americanexpress.www.wsdl.ctn.utilities.atb.CheckEligibility2;
import junit.framework.TestCase;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.StAXUtils;

import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;


public class ElementReferenceTest extends TestCase {

    public void testSingleElementReference() {
        CheckEligibility1 echCheckEligibility1 = new CheckEligibility1();
        AtbRequestCheckEligibility_type0 atbRequestCheckEligibility = new AtbRequestCheckEligibility_type0();
        echCheckEligibility1.setAtbRequestCheckEligibility(atbRequestCheckEligibility);
        atbRequestCheckEligibility.setCardNumber("carnumber");
        atbRequestCheckEligibility.setClientId("clientid");
        atbRequestCheckEligibility.setExpirationDate("date");
        atbRequestCheckEligibility.setNameAsOnCard("cardname");
        atbRequestCheckEligibility.setYearOfRedemption(2);

        OMElement omElement = echCheckEligibility1.getOMElement(CheckEligibility1.MY_QNAME, OMAbstractFactory.getSOAP12Factory());

        try {
            String omElementString = omElement.toStringWithConsume();
            XMLStreamReader xmlReader = StAXUtils.createXMLStreamReader(new ByteArrayInputStream(omElementString.getBytes()));
            CheckEligibility1 result = CheckEligibility1.Factory.parse(xmlReader);
            assertEquals(result.getAtbRequestCheckEligibility().getCardNumber(), "carnumber");
            assertEquals(result.getAtbRequestCheckEligibility().getClientId(), "clientid");
            assertEquals(result.getAtbRequestCheckEligibility().getExpirationDate(), "date");
            assertEquals(result.getAtbRequestCheckEligibility().getNameAsOnCard(), "cardname");
            assertEquals(result.getAtbRequestCheckEligibility().getYearOfRedemption(), 2);
        } catch (Exception e) {
            fail();
        }
    }

    public void testMultipleElementReference() {
        CheckEligibility2 echCheckEligibility2 = new CheckEligibility2();
        AtbRequestCheckEligibility_type0[] atbRequestCheckEligibility = new AtbRequestCheckEligibility_type0[2];
        echCheckEligibility2.setAtbRequestCheckEligibility(atbRequestCheckEligibility);

        atbRequestCheckEligibility[0] = new AtbRequestCheckEligibility_type0();
        atbRequestCheckEligibility[0].setCardNumber("carnumber");
        atbRequestCheckEligibility[0].setClientId("clientid");
        atbRequestCheckEligibility[0].setExpirationDate("date");
        atbRequestCheckEligibility[0].setNameAsOnCard("cardname");
        atbRequestCheckEligibility[0].setYearOfRedemption(2);

        atbRequestCheckEligibility[1] = new AtbRequestCheckEligibility_type0();
        atbRequestCheckEligibility[1].setCardNumber("carnumber");
        atbRequestCheckEligibility[1].setClientId("clientid");
        atbRequestCheckEligibility[1].setExpirationDate("date");
        atbRequestCheckEligibility[1].setNameAsOnCard("cardname");
        atbRequestCheckEligibility[1].setYearOfRedemption(2);

        OMElement omElement = echCheckEligibility2.getOMElement(CheckEligibility2.MY_QNAME, OMAbstractFactory.getSOAP12Factory());

        try {
            String omElementString = omElement.toStringWithConsume();
            XMLStreamReader xmlReader = StAXUtils.createXMLStreamReader(new ByteArrayInputStream(omElementString.getBytes()));
            CheckEligibility2 result = CheckEligibility2.Factory.parse(xmlReader);
            assertEquals(result.getAtbRequestCheckEligibility()[0].getCardNumber(), "carnumber");
            assertEquals(result.getAtbRequestCheckEligibility()[0].getClientId(), "clientid");
            assertEquals(result.getAtbRequestCheckEligibility()[0].getExpirationDate(), "date");
            assertEquals(result.getAtbRequestCheckEligibility()[0].getNameAsOnCard(), "cardname");
            assertEquals(result.getAtbRequestCheckEligibility()[0].getYearOfRedemption(), 2);

            assertEquals(result.getAtbRequestCheckEligibility()[1].getCardNumber(), "carnumber");
            assertEquals(result.getAtbRequestCheckEligibility()[1].getClientId(), "clientid");
            assertEquals(result.getAtbRequestCheckEligibility()[1].getExpirationDate(), "date");
            assertEquals(result.getAtbRequestCheckEligibility()[1].getNameAsOnCard(), "cardname");
            assertEquals(result.getAtbRequestCheckEligibility()[1].getYearOfRedemption(), 2);
        } catch (Exception e) {
            fail();
        }
    }
}
