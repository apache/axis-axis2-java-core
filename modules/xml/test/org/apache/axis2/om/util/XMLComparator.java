package org.apache.axis2.om.util;

import org.apache.axis2.om.OMAttribute;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMNamespace;
import org.apache.axis2.om.OMNode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Iterator;

/**
 * Copyright 2001-2004 The Apache Software Foundation.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * <p/>
 */
public class XMLComparator {
    /**
     * Eran Chinthaka (chinthaka@apache.org)
     */
    private Log log = LogFactory.getLog(getClass());


    public boolean compare(OMElement elementOne, OMElement elementTwo) throws XMLComparisonException {
        if(elementOne == null && elementTwo == null){
            log.info("Both Elements are null.");
            return true;
        }
        if(elementOne == null && elementTwo != null){
            throw new XMLComparisonException("Element One is null and Element Two is not null");
        }
        if(elementOne != null && elementTwo == null){
            throw new XMLComparisonException("Element Two is null and Element One is not null");
        }

        log.info("Now Checking "+ elementOne.getLocalName() + " and " + elementTwo.getLocalName() + "=============================");

        log.info("Comparing Element Names .......");
        compare("Elements names are not equal. ", elementOne.getLocalName(), elementTwo.getLocalName());

        log.info("Comparing Namespaces .........");
        compare("Element namespaces are not equal", elementOne.getNamespace(), elementTwo.getNamespace());

        log.info("Comparing attributes .....");
        compareAllAttributes(elementOne, elementTwo);

        log.info("Comparing texts .....");
        compare("Elements texts are not equal ", elementOne.getText(), elementTwo.getText());

        log.info("Comparing Children ......");
        compareAllChildren(elementOne, elementTwo);


        return true;
    }

    private void compareAllAttributes(OMElement elementOne, OMElement elementTwo) throws XMLComparisonException {
        compareAttibutes(elementOne, elementTwo);
        compareAttibutes(elementTwo, elementOne);
    }

    private void compareAllChildren(OMElement elementOne, OMElement elementTwo) throws XMLComparisonException {
        compareChildren(elementOne, elementTwo);
        compareChildren(elementTwo, elementOne);
    }

    private void compareChildren(OMElement elementOne, OMElement elementTwo) throws XMLComparisonException {
        Iterator elementOneChildren = elementOne.getChildren();
        while (elementOneChildren.hasNext()) {
            OMNode omNode = (OMNode) elementOneChildren.next();
            if(omNode instanceof OMElement){
                OMElement elementOneChild = (OMElement) omNode;
                OMElement elementTwoChild = elementTwo.getFirstChildWithName(elementOneChild.getQName());
                if(elementTwoChild == null){
                    throw new XMLComparisonException(" There is no " + elementOneChild.getLocalName() + " element under " + elementTwo.getLocalName());
                }
                compare(elementOneChild, elementTwoChild);
            }
        }
    }


    private void compareAttibutes(OMElement elementOne, OMElement elementTwo) throws XMLComparisonException {
        Iterator attributes = elementOne.getAttributes();
        while (attributes.hasNext()) {
            OMAttribute omAttribute = (OMAttribute) attributes.next();
            OMAttribute attr = elementTwo.getFirstAttribute(omAttribute.getQName());
            if(attr == null){
                throw new XMLComparisonException("Attributes are not the same in two elements. Attribute "+ omAttribute.getLocalName() + " != ");
            }
        }
    }

    private void compare(String failureNotice, String one, String two) throws XMLComparisonException {
        if(!one.equals(two)){
            throw new XMLComparisonException(failureNotice+ one + " != " + two);
        }
    }

    private void compare(String failureNotice, OMNamespace one, OMNamespace two) throws XMLComparisonException {
        if(one == null && two == null){
            return;
        }else if(one != null && two == null){
            throw new XMLComparisonException("First Namespace is NOT null. But the second is null");
        }else if(one == null && two != null){
            throw new XMLComparisonException("First Namespace is null. But the second is NOT null");
        }

        if(!one.getName().equals(two.getName())){
            throw new XMLComparisonException(failureNotice + one + " != " + two);
        }

        // Do we need to compare prefixes as well
    }
}
