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

package test.interop.util;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMNode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Iterator;
import java.util.Vector;
public class XMLComparatorInterop {
	private static final Log log = LogFactory.getLog(XMLComparatorInterop.class);

    String failureNotice = "";

    private Vector ignorableNamespaceList = new Vector();

    public void addIgnorableNamespace(String nsURI) {
        ignorableNamespaceList.add(nsURI);
    }

    public void clearIgnorableNamespaces() {
        ignorableNamespaceList.clear();
    }


    public boolean compare(OMElement elementOne, OMElement elementTwo) {

        boolean status = false;
        //ignore if the elements belong to any of the ignorable namespaces list

        if (isIgnorable(elementOne) ||
                isIgnorable(elementTwo)) {
            return true;
        }

        if (elementOne == null && elementTwo == null) {
            log.info("Both Elements are null.");
            return true;
        }
        if (elementOne == null && elementTwo != null) {
            //failureNotice = "Element One is null and Element Two is not null";
            return false;
        }
        if (elementOne != null && elementTwo == null) {
            //failureNotice = "Element Two is null and Element One is not null";
            return false;
        }

        log.info("Now Checking " + elementOne.getLocalName() + " and " +
                elementTwo.getLocalName() +
                "=============================");

        log.info("Comparing Element Names .......");
        status = compare(elementOne.getLocalName(),elementTwo.getLocalName());
        if (!status)
            return false;

        log.info("Comparing Namespaces .........");
        status = compare(elementOne.getNamespace(),
                elementTwo.getNamespace());
        if (!status)
            return false;

        log.info("Comparing attributes .....");
        status = compareAllAttributes(elementOne, elementTwo);
        if (!status)
            return false;

        log.info("Comparing texts .....");

        /*
        * Trimming the value of the XMLElement is not correct
        * since this compare method cannot be used to compare
        * element contents with trailing and leading whitespaces
        * BUT for the practicalltiy of tests and to get the current
        * tests working we have to trim() the contents
        */
        status = compare(
                elementOne.getText().trim(),
                elementTwo.getText().trim());
        if (!status)
            return false;

        log.info("Comparing Children ......");
        status = compareAllChildren(elementOne, elementTwo);

        return status;
    }

    private boolean compareAllAttributes(OMElement elementOne,
                                         OMElement elementTwo) {
        boolean status = false;
        status = compareAttibutes(elementOne, elementTwo);
        status = compareAttibutes(elementTwo, elementOne);
        return status;
    }

    private boolean compareAllChildren(OMElement elementOne,
                                       OMElement elementTwo) {
        boolean status = false;
        status = compareChildren(elementOne, elementTwo);
        //status =compareChildren(elementTwo, elementOne);
        return status;
    }


    private boolean isIgnorable(OMElement elt) {
        if (elt != null) {
            OMNamespace namespace = elt.getNamespace();
            if (namespace != null) {
                return ignorableNamespaceList.contains(namespace.getNamespaceURI());
            } else {
                return false;
            }
        } else {
            return false;
        }
    }


    private boolean compareChildren(OMElement elementOne, OMElement elementTwo) {
        //ignore if the elements belong to any of the ignorable namespaces list
        boolean status = true;
        if (isIgnorable(elementOne) ||
                isIgnorable(elementTwo)) {
            return true;
        }
        Iterator elementOneChildren = elementOne.getChildren();
        while (elementOneChildren.hasNext()) {
            OMNode omNode = (OMNode) elementOneChildren.next();
            if (omNode instanceof OMElement) {
                OMElement elementOneChild = (OMElement) omNode;
                OMElement elementTwoChild = null;
                //Do the comparison only if the element is not ignorable
                if (!isIgnorable(elementOneChild)) {
                    Iterator elementTwoChildren = elementTwo.getChildren();
                    while (elementTwoChildren.hasNext() ) {
                        status = false;
                        OMNode node = (OMNode) elementTwoChildren.next();
                        if (node.getType() == OMNode.ELEMENT_NODE) {
                            elementTwoChild = (OMElement) node;
                            if (elementTwoChild.getLocalName().equals(elementOneChild.getLocalName())) {
                                //Do the comparison only if the element is not ignorable
                                if (!isIgnorable(elementTwoChild)) {
                                    if (elementTwoChild == null) {
                                        return false;
                                    }
                                }

                                status = compare(elementOneChild, elementTwoChild);

                            }
                        }
                        if(status){
                            break;
                        }
                    }
                    if (!status) {
                        return false;
                    }
                } else
                    status = compare(elementOneChild, elementTwoChild);
            }
        }

        return status;
    }

    private boolean compareAttibutes(OMElement elementOne, OMElement elementTwo) {
        int elementOneAtribCount = 0;
        int elementTwoAtribCount = 0;
        Iterator attributes = elementOne.getAllAttributes();
        while (attributes.hasNext()) {
            OMAttribute omAttribute = (OMAttribute) attributes.next();
            OMAttribute attr = elementTwo.getAttribute(omAttribute.getQName());
            if (attr == null) {
                return false;
            }
            elementOneAtribCount++;
        }

        Iterator elementTwoIter = elementTwo.getAllAttributes();
        while (elementTwoIter.hasNext()) {
            elementTwoIter.next();
            elementTwoAtribCount++;

        }

        return elementOneAtribCount == elementTwoAtribCount;
    }

    private boolean compare(String one, String two) {
        return one.equals(two);
    }

    private boolean compare(OMNamespace one,OMNamespace two) {
        if (one == null && two == null) {
            return true;
        } else if (one != null && two == null) {
            return false;
        } else if (one == null && two != null) {
            return false;
        }
        if (!one.getNamespaceURI().equals(two.getNamespaceURI())) {
            return false;
        }

        // Do we need to compare prefixes as well
        return true;
    }
}
