/*
 * Copyright 2006 The Apache Software Foundation.
 * Copyright 2006 International Business Machines Corp.
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
package org.apache.axis2.jaxws.message.attachments;

import java.util.ArrayList;
import java.util.Iterator;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.OMText;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.jaxws.message.Attachment;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A suite of utilities used for handling MTOM attachment data. 
 */
public class AttachmentUtils {
    
    private static final Log log = LogFactory.getLog(AttachmentUtils.class);
    private static final QName XOP_INCLUDE = 
        new QName("http://www.w3.org/2004/08/xop/include", "Include");
    
    /**
     * Can be used to find all instances of the <pre><xop:include></pre> element 
     * within a given OM SOAPEnvelope.
     * @param env
     * @return
     */
    public static ArrayList<OMElement> findXopElements(SOAPEnvelope env) {
        ArrayList<OMElement> xops = new ArrayList<OMElement>();
        findXopElements(env, xops);
        return xops;
    }
    
    /*
     * A recursive search for all of the <xop:include> elements in the tree.
     */
    private static void findXopElements(OMElement root, ArrayList<OMElement> xops) {
        Iterator itr = root.getChildElements();
        if (log.isDebugEnabled())
            log.debug("[subtree] " + root.getLocalName());
        
        // If it has no children, then it's a leaf and we need
        // to check if it's an <xop:include> element.  If not, then
        // we need to grab each of the children and continue traversing
        // down the tree.
        if (itr == null || !itr.hasNext()) {
            if (log.isDebugEnabled())
                log.debug("[leaf] " + root.getLocalName());
            
            if (root.getQName().equals(XOP_INCLUDE)) {
                xops.add(root);
            }
        }
        else if (itr != null && itr.hasNext()) {
            while (itr.hasNext()) {
                OMElement next = (OMElement) itr.next();
                findXopElements(next, xops);
            }
        }
    }
    
    /**
     * Can be used to find all of the nodes in a tree that contain binary
     * content that is targetted for optimization via MTOM.
     * @param env
     * @return
     */
    public static ArrayList<OMText> findBinaryNodes(SOAPEnvelope env) {
        ArrayList<OMText> nodes = new ArrayList<OMText>();
        findBinaryElements(env, nodes);
        return nodes;
    }
    
    /*
     * A recursive search for all of the binary, optimized nodes in a tree.
     */
    private static void findBinaryElements(OMNode node, ArrayList<OMText> attachments) {
        // If it's an OMText, then it's essentially a leaf and won't
        // have any children.  If not, then check to see if it's an OMElement
        // and continue traversing down.
        if (node instanceof OMText) {
            if (log.isDebugEnabled())
                log.debug("text node found");
            
            OMText textNode = (OMText) node;
            if (textNode.isOptimized()) {
                if (log.isDebugEnabled())
                    log.debug("optimized text node found");
                
                attachments.add(textNode);
            }
        }
        else if (node instanceof OMElement){
            OMElement element = (OMElement) node;
            Iterator itr = element.getChildren();
            while (itr.hasNext()) {
                OMNode next = (OMNode) itr.next();
                findBinaryElements(next, attachments);
            }
        }
    }
    
    /**
     * Given an <pre><xop:include></pre> element, create an OMText element
     * with the appropriate attachment data.
     * @param xop
     * @param data
     * @return
     */
    public static OMText makeBinaryOMNode(OMElement xop, Attachment data) {
        OMFactory factory = xop.getOMFactory();
        OMText binaryNode = factory.createOMText(data.getDataHandler(), true);
        return binaryNode;
    }
    
    /**
     * Given an OMText node, create it's corresponding <pre><xop:include></pre>
     * element.
     */
    public static OMElement makeXopElement(OMText data) {
        OMFactory factory = data.getOMFactory();
        OMElement xop = factory.createOMElement(XOP_INCLUDE, null);
        xop.addAttribute("href", data.getContentID(), null);
        return xop;
    }
   
}
