/*
 * Copyright 2001-2004 The Apache Software Foundation.
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
package org.apache.ws.security.secpolicy.builders;

import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.apache.neethi.Assertion;
import org.apache.neethi.AssertionBuilderFactory;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyEngine;
import org.apache.neethi.XmlPrimtiveAssertion;
import org.apache.neethi.builders.AssertionBuilder;
import org.apache.ws.security.secpolicy.Constants;
import org.apache.ws.security.secpolicy.model.AlgorithmSuite;

public class AlgorithmSuiteBuilder implements AssertionBuilder {
    
    public static final String INCLUSIVE_C14N = "InclusiveC14N";
    public static final String SOAP_NORMALIZATION_10 = "SoapNormalization10";
    public static final String STR_TRANSFORM_10 = "STRTransform10";
    public static final String XPATH10 = "XPath10";
    public static final String XPATH_FILTER20 = "XPathFilter20"; 

    
    public Assertion build(OMElement element, AssertionBuilderFactory factory) throws IllegalArgumentException {
        AlgorithmSuite algorithmSuite = new AlgorithmSuite();
        
        Policy policy = PolicyEngine.getPolicy(element.getFirstElement());
        policy = (Policy) policy.normalize(false);
        
        for (Iterator iterator = policy.getAlternatives(); iterator.hasNext();) {
            processAlternative((List) iterator.next(), algorithmSuite);
        }
        return algorithmSuite;
    }
    
    private void processAlternative(List assertionList, AlgorithmSuite target) {
        AlgorithmSuite algorithmSuite = new AlgorithmSuite();
        
        XmlPrimtiveAssertion primtiveAssertion;
        
        for (Iterator iterator = assertionList.iterator(); iterator.hasNext();) {
            primtiveAssertion = (XmlPrimtiveAssertion) iterator.next();
            QName qname = primtiveAssertion.getName();
            String localName = qname.getLocalPart();
            
            if (localName.equals(INCLUSIVE_C14N)) {
                algorithmSuite.setC14n(Constants.C14N);
                
            } else if (localName.equals(SOAP_NORMALIZATION_10)) {
                algorithmSuite.setSoapNormalization(Constants.SNT);
                
            } else if (localName.equals(STR_TRANSFORM_10)) {
                algorithmSuite.setStrTransform(Constants.STRT10);
                
            } else if (localName.equals(XPATH10)) {
                algorithmSuite.setXPath(Constants.XPATH);
                
            } else if (localName.equals(XPATH_FILTER20)) {
                algorithmSuite.setXPath(Constants.XPATH20);
                           
            } else if (localName.equals(Constants.ALGO_SUITE_BASIC128)) {
                algorithmSuite.setAlgorithmSuite(Constants.ALGO_SUITE_BASIC128);
                
            } else if (localName.equals(Constants.ALGO_SUITE_BASIC128_RSA15)) {
                algorithmSuite.setAlgorithmSuite(Constants.ALGO_SUITE_BASIC128_RSA15);
                
            } else if (localName.equals(Constants.ALGO_SUITE_BASIC128_SHA256)) {
                algorithmSuite.setAlgorithmSuite(Constants.ALGO_SUITE_BASIC128_SHA256);
                
            } else if (localName.equals(Constants.ALGO_SUITE_BASIC128_SHA256_RSA15)) {
                algorithmSuite.setAlgorithmSuite(Constants.ALGO_SUITE_BASIC128_SHA256_RSA15);
                
            } else if (localName.equals(Constants.ALGO_SUITE_BASIC192)) {
                algorithmSuite.setAlgorithmSuite(Constants.ALGO_SUITE_BASIC192);
                
            } else if (localName.equals(Constants.ALGO_SUITE_BASIC192_RSA15)) {
                algorithmSuite.setAlgorithmSuite(Constants.ALGO_SUITE_BASIC192_RSA15);
                
            } else if (localName.equals(Constants.ALGO_SUITE_BASIC192_SHA256)) {
                algorithmSuite.setAlgorithmSuite(Constants.ALGO_SUITE_BASIC192_SHA256);
                
            } else if (localName.equals(Constants.ALGO_SUITE_BASIC192_SHA256_RSA15)) {
                algorithmSuite.setAlgorithmSuite(Constants.ALGO_SUITE_BASIC192_SHA256_RSA15);
                
            } else if (localName.equals(Constants.ALGO_SUITE_BASIC256)) {
                algorithmSuite.setAlgorithmSuite(Constants.ALGO_SUITE_BASIC256);
                
            } else if (localName.equals(Constants.ALGO_SUITE_BASIC256_RSA15)) {
                algorithmSuite.setAlgorithmSuite(Constants.ALGO_SUITE_BASIC256_RSA15);
                
            } else if (localName.equals(Constants.ALGO_SUITE_BASIC256_SHA256)) {
                algorithmSuite.setAlgorithmSuite(Constants.ALGO_SUITE_BASIC256_SHA256);
                
            } else if (localName.equals(Constants.ALGO_SUITE_BASIC256_SHA256_RSA15)) {
                algorithmSuite.setAlgorithmSuite(Constants.ALGO_SUITE_BASIC256_SHA256_RSA15);
                
            } else if (localName.equals(Constants.ALGO_SUITE_TRIPLE_DES)) {
                algorithmSuite.setAlgorithmSuite(Constants.ALGO_SUITE_TRIPLE_DES);
                
            } else if (localName.equals(Constants.ALGO_SUITE_TRIPLE_DES_RSA15)) {
                algorithmSuite.setAlgorithmSuite(Constants.ALGO_SUITE_TRIPLE_DES_RSA15);
                
            } else if (localName.equals(Constants.ALGO_SUITE_TRIPLE_DES_SHA256)) {
                algorithmSuite.setAlgorithmSuite(Constants.ALGO_SUITE_TRIPLE_DES_SHA256);
                
            } else if (localName.equals(Constants.ALGO_SUITE_TRIPLE_DES_SHA256_RSA15)) {
                algorithmSuite.setAlgorithmSuite(Constants.ALGO_SUITE_TRIPLE_DES_SHA256_RSA15);
            }    
        }
        
        target.addOption(algorithmSuite);
    }
}
