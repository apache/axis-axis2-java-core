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

/*Speacial Notice : -
  This SOAPComparator was implementd for only SOAP 1.2 Tests. Chekings are concerend on specific
  tests in SOAP 1.2 Testing. Therefore this can not be used for comman soap comparisons.
*/

package test.soap12testing.client;

import org.apache.axiom.om.impl.exception.XMLComparisonException;
import org.apache.axiom.om.impl.llom.util.XMLComparator;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFault;

public class SOAPComparator extends XMLComparator {

    public boolean compare(SOAPEnvelope expectedEnvelope, SOAPEnvelope ReplyEnvelope)
            throws XMLComparisonException {

        if (expectedEnvelope.getNamespace().getNamespaceURI()
                .equals(ReplyEnvelope.getNamespace().getNamespaceURI())) {
            //a!=null && b==null
            if ((expectedEnvelope.getHeader() != null && ReplyEnvelope.getHeader() != null) ||
                    (expectedEnvelope.getHeader() == null && ReplyEnvelope.getHeader() == null)) {

                if (expectedEnvelope.getHeader() != null)
                    super.compare(ReplyEnvelope.getHeader(), expectedEnvelope.getHeader());

                if (expectedEnvelope.getBody().hasFault()) {
                    SOAPFault replyFault = ReplyEnvelope.getBody().getFault();
                    SOAPFault expectedFault = expectedEnvelope.getBody().getFault();

                    super.compare(replyFault.getCode(), expectedFault.getCode());
                    super.compare(replyFault.getReason(), expectedFault.getReason());

                    if (expectedFault.getRole() != null) {
                        super.compare(replyFault.getRole(), expectedFault.getRole());
                    }
                    if (expectedFault.getNode() != null) {
                        super.compare(replyFault.getNode(), expectedFault.getNode());
                    }
                    if (expectedFault.getDetail() != null) {
                        super.compare(replyFault.getDetail(), expectedFault.getDetail());
                    }
                } else {
                    super.compare(ReplyEnvelope.getBody(), expectedEnvelope.getBody());
                }
            } else {
                throw new XMLComparisonException("Envelope headers mismatched...!");
            }
        } else {
            throw new XMLComparisonException("Envelope namespaces mismatched...!");
        }
        return true;
    }
}
