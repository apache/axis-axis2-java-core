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

package org.apache.axis2.databinding.utils;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.OMText;
import org.apache.axiom.om.OMXMLBuilderFactory;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.AxisFault;
import org.apache.axis2.databinding.typemapping.SimpleTypeMapper;
import org.apache.axis2.engine.ObjectSupplier;

import javax.xml.namespace.QName;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MultirefHelper {

    public static final String SOAP12_REF_ATTR = "ref";
    public static final String SOAP11_REF_ATTR = "href";

    private boolean filledTable;

    private OMElement parent;

    private HashMap objectmap = new HashMap();
    private HashMap elementMap = new HashMap();
    private HashMap omElementMap = new HashMap();

    /**
     * Maximum nesting of multiref resolutions, and maximum number of resolutions
     * per message. Both -1 for unbounded.
     * <p>
     * A reference is resolved by deep-cloning the referenced element, and both the
     * element and the bean paths memoise a resolution only <em>after</em> it
     * returns, so a reference that leads back to itself recurses until the stack
     * ends. Cycles are refused outright by tracking what is being resolved; these
     * bound the other shape, a reference graph with no cycle in it whose expansion
     * still doubles at every level -- the multiref analogue of an entity-expansion
     * bomb, on well-formed XML that no parser limit sees.
     */
    private static final int MAX_DEPTH =
            getIntProperty("org.apache.axis2.databinding.multiref.maxDepth", 64);
    private static final int MAX_RESOLUTIONS =
            getIntProperty("org.apache.axis2.databinding.multiref.maxResolutions", 5000);

    /**
     * Maximum nodes a message may bring into existence by expanding references.
     * <p>
     * This is the limit that matters for the doubling shape. Resolutions stay linear
     * because each id is memoised after the first one, so counting them catches
     * nothing: what grows is the <em>size</em> of each resolved element, since every
     * level inlines two copies of the level below. Metering nodes as they are moved
     * in stops it while the trees are still small, before a clone of the next level
     * up would exhaust the heap.
     */
    private static final int MAX_EXPANDED_NODES =
            getIntProperty("org.apache.axis2.databinding.multiref.maxExpandedNodes", 50000);

    /**
     * Element ids currently being resolved, so a reference back into one is a cycle.
     * <p>
     * Only the element path uses this. A cyclic reference there means inlining XML
     * into itself, which cannot terminate. The bean path is different: SOAP encoding
     * allows a cyclic <em>object</em> graph -- an employee who is their own employer
     * is a documented multiref shape, exercised by MultirefTest -- so that path is
     * bounded by nesting depth rather than refused outright.
     */
    private final java.util.Set resolvingElements = new java.util.HashSet();

    /** Current nesting of resolutions on either path, against MAX_DEPTH. */
    private int nesting;

    /** Resolutions performed for this message, against MAX_RESOLUTIONS. */
    private int resolutions;

    /** Nodes moved in by expansion, against MAX_EXPANDED_NODES. */
    private int expandedNodes;

    /**
     * Charges the cost of copying an element, before the copy is made.
     * <p>
     * It has to be counted before rather than after: at depth the element being
     * copied is already large, and it is the copy that exhausts the heap. Counting
     * the nodes actually moved does not work either -- only the direct children are
     * moved, each carrying a whole subtree with it, so the count stays flat while the
     * subtrees double.
     */
    private void chargeCopyOf(OMElement element) throws AxisFault {
        if (MAX_EXPANDED_NODES < 0) {
            return;
        }
        int remaining = MAX_EXPANDED_NODES - expandedNodes;
        int size = countNodes(element, remaining + 1);
        expandedNodes += size;
        if (expandedNodes > MAX_EXPANDED_NODES) {
            throw new AxisFault("Expanding multiref references in this message would"
                    + " copy more than " + MAX_EXPANDED_NODES + " nodes");
        }
    }

    /**
     * Counts nodes, stopping once the limit is passed so that measuring a large tree
     * is not itself the expensive part.
     */
    private static int countNodes(OMElement element, int limit) {
        int count = 1;
        Iterator children = element.getChildElements();
        while (children.hasNext() && count <= limit) {
            count += countNodes((OMElement) children.next(), limit - count);
        }
        return count;
    }

    private static int getIntProperty(String name, int defaultValue) {
        try {
            String value = System.getProperty(name);
            if (value != null && !value.trim().isEmpty()) {
                return Integer.parseInt(value.trim());
            }
        } catch (RuntimeException e) {
            // Unreadable or unparseable: keep the default rather than run unbounded.
        }
        return defaultValue;
    }

    /**
     * Claims an id for resolution, refusing a cycle or an over-budget message.
     * Every caller must {@link #release} in a finally block.
     */
    /** Enters a resolution on either path, bounding nesting and total work. */
    private void enter() throws AxisFault {
        if (MAX_DEPTH >= 0 && ++nesting > MAX_DEPTH) {
            nesting--;
            throw new AxisFault("Multiref references nested deeper than " + MAX_DEPTH);
        }
        if (MAX_RESOLUTIONS >= 0 && ++resolutions > MAX_RESOLUTIONS) {
            nesting--;
            throw new AxisFault("Message resolves more than " + MAX_RESOLUTIONS
                    + " multiref references");
        }
    }

    private void exit() {
        nesting--;
    }

    /** Enters an element resolution, where a reference back into one is a cycle. */
    private void claimElement(String id) throws AxisFault {
        if (!resolvingElements.add(id)) {
            throw new AxisFault("Cyclic multiref reference: " + id);
        }
        try {
            enter();
        } catch (AxisFault fault) {
            resolvingElements.remove(id);
            throw fault;
        }
    }

    private void releaseElement(String id) {
        resolvingElements.remove(id);
        exit();
    }

    public MultirefHelper(OMElement parent) {
        this.parent = parent;
    }

    public Object getObject(String id) {
        return objectmap.get(id);
    }

    public OMElement getOMElement(String id) {
        return (OMElement)omElementMap.get(id);
    }

    public OMElement processOMElementRef(String id) throws AxisFault {
        if (!filledTable) {
            readallChildElements();
        }
        OMElement val = (OMElement)elementMap.get(id);
        if (val == null) {
            throw new AxisFault("Invalid reference :" + id);
        } else {
            // The memo below is written only once this returns, so without the
            // claim a reference leading back to id would recurse into itself.
            claimElement(id);
            try {
                OMElement ele = processElementforRefs(val);
                OMElement cloneele = elementClone(ele);
                omElementMap.put(id, cloneele);
                return cloneele;
            } finally {
                releaseElement(id);
            }
        }
    }

    public OMElement processElementforRefs(OMElement elemnts) throws AxisFault {
        Iterator itr = elemnts.getChildElements();
        while (itr.hasNext()) {
            OMElement omElement = (OMElement)itr.next();
            OMAttribute attri = processRefAtt(omElement);
            if (attri != null) {
                String ref = getAttvalue(attri);
                OMElement tempele = getOMElement(ref);
                if (tempele == null) {
                    tempele = processOMElementRef(ref);
                }
                chargeCopyOf(tempele);
                OMElement ele2 = elementClone(tempele);
                Iterator itrChild = ele2.getChildren();
                while (itrChild.hasNext()) {
                    Object obj = itrChild.next();
                    if (obj instanceof OMNode) {
                        itrChild.remove();
                        omElement.addChild((OMNode)obj);
                    }
                }
            }
        }
        return elemnts;
    }

    private OMElement elementClone(OMElement ele) {
        return OMXMLBuilderFactory.createStAXOMBuilder(ele.getXMLStreamReader()).getDocumentElement();
    }

    public Object processRef(Class javatype, String id,
	    ObjectSupplier objectSupplier) throws AxisFault {
	return processRef(javatype, null, id, objectSupplier);
    }
    public Object processRef(Class javatype, Type generictype, String id, ObjectSupplier objectSupplier)
            throws AxisFault {
        if (!filledTable) {
            readallChildElements();
        }
        OMElement val = (OMElement)elementMap.get(id);
        if (val == null) {
            throw new AxisFault("Invalid reference :" + id);
        } else {
            // Not cycle-refused: objectmap is populated only after
            // BeanUtil.deserialize returns, so a self-referencing object graph
            // re-enters here legitimately (MultirefTest.testechoEmployee sends
            // exactly that). Nesting depth is what keeps a crafted one from
            // exhausting the stack.
            enter();
            try {
            if (SimpleTypeMapper.isSimpleType(javatype)) {
                /**
                 * in this case OM element can not contains more child, that is no way to get
                 * the value as an exp ,
                 * <refernce id="12">
                 *   <value>foo</value>
                 * </refernce>
                 * the above one is not valid , that should always be like below
                 * <refernce id="12">foo</refernce>
                 */
                Object valObj = SimpleTypeMapper.getSimpleTypeObject(javatype, val);
                objectmap.put(id, valObj);
                return valObj;
            } else if (generictype != null
        	    && SimpleTypeMapper.isCollection(javatype)) {
        	return BeanUtil.processGenericCollection(val.getFirstElement(),
        		generictype, this, objectSupplier);
            } else if (generictype != null
        	    && SimpleTypeMapper.isMap(javatype)) {
        	Type[] parameterArgTypes = {Object.class, Object.class};
        	if (generictype instanceof ParameterizedType) {
        	    ParameterizedType aType = (ParameterizedType) generictype;
        	    parameterArgTypes = aType.getActualTypeArguments();        	     
        	}                                   
		return BeanUtil.processGenericsMapElement(parameterArgTypes,
			val.getFirstElement(), this, val.getChildren(),
			objectSupplier, generictype);
            } else {
                Object obj = BeanUtil.deserialize(javatype, val, this, objectSupplier);
                objectmap.put(id, obj);
                return obj;
            }
            } finally {
                exit();
            }
        }
    }

    private void readallChildElements() {
        Iterator childs = parent.getChildElements();
        while (childs.hasNext()) {
            OMElement omElement = (OMElement)childs.next();
            OMAttribute id = omElement.getAttribute(new QName("id"));
            if (id != null) {
                childs.remove();
                elementMap.put(id.getAttributeValue(), omElement);
            }
        }
        filledTable = true;
    }

    public static String getAttvalue(OMAttribute omatribute) {
        String ref;
        ref = omatribute.getAttributeValue();
        if (ref != null) {
            if (ref.charAt(0) == '#') {
                ref = ref.substring(1);
            }
        }
        return ref;
    }

    public static OMAttribute processRefAtt(OMElement omElement) {
        OMAttribute omatribute = omElement.getAttribute(new QName(SOAP11_REF_ATTR));
        if (omatribute == null) {
            omatribute = omElement.getAttribute(new QName(SOAP12_REF_ATTR));
        }
        return omatribute;
    }

    public void clean() {
        elementMap.clear();
        objectmap.clear();
    }

    /**
     * this method is used to process the href attributes which may comes with the incomming soap mesaage
     * <soap:body>
     * <operation>
     * <arg1 href="#obj1"/>
     * </operation>
     * <multiref id="obj1">
     * <name>the real argument</name>
     * <color>blue</color>
     * </multiref>
     * </soap:body>
     * here we assume first child of the soap body has the main object structure and others contain the
     * multiref parts.
     * Soap spec says that those multiref parts must be top level elements.
     *
     * @param soapEnvelope
     */

    public static void processHrefAttributes(SOAPEnvelope soapEnvelope)
            throws AxisFault {
        // first populate the multiref parts to a hash table.
        SOAPBody soapBody = soapEnvelope.getBody();
        // first build the whole tree
        soapBody.build();
        OMElement omElement = null;
        OMAttribute idAttribute = null;
        Map idAndOMElementMap = new HashMap();
        for (Iterator iter = soapBody.getChildElements(); iter.hasNext();) {
            omElement = (OMElement) iter.next();
            // the attribute id is an unqualified attribute
            idAttribute = omElement.getAttribute(new QName(null, "id"));
            if (idAttribute != null) {
                // for the first element there may not have an id
                idAndOMElementMap.put(idAttribute.getAttributeValue(), omElement);
            }
        }

        // start processing from the first child
        processHrefAttributes(idAndOMElementMap, soapBody.getFirstElement(), OMAbstractFactory.getOMFactory());

    }

    /**
     * Bounds one message's worth of href expansion.
     * <p>
     * Each resolution copies the referenced element's children into the element being
     * processed and the walk then descends into them, so a reference graph that leads
     * back on itself expands without end. Depth alone is not enough: a graph with no
     * cycle can still double at every level.
     */
    private static final class HrefBudget {
        private int expansions;

        void spend(int depth) throws AxisFault {
            if (MAX_DEPTH >= 0 && depth > MAX_DEPTH) {
                throw new AxisFault("href references nested deeper than " + MAX_DEPTH);
            }
            if (MAX_RESOLUTIONS >= 0 && ++expansions > MAX_RESOLUTIONS) {
                throw new AxisFault("Message expands more than " + MAX_RESOLUTIONS
                        + " href references");
            }
        }
    }

    public static void processHrefAttributes(Map idAndOMElementMap,
                                         OMElement elementToProcess,
                                         OMFactory omFactory)
            throws AxisFault {
        processHrefAttributes(idAndOMElementMap, elementToProcess, omFactory,
                new HrefBudget(), 0);
    }

    private static void processHrefAttributes(Map idAndOMElementMap,
                                         OMElement elementToProcess,
                                         OMFactory omFactory,
                                         HrefBudget budget,
                                         int depth)
            throws AxisFault {

        // first check whether this element has an href value.
        // href is also an unqualifed attribute
        OMAttribute hrefAttribute = elementToProcess.getAttribute(new QName(null, "href"));
        if (hrefAttribute != null) {
            // i.e this has an href attribute
            String hrefAttributeValue = hrefAttribute.getAttributeValue();
            if (!hrefAttributeValue.startsWith("#")) {
                throw new AxisFault("In valid href ==> " + hrefAttributeValue + " does not starts with #");
            } else {
                OMElement referedOMElement =
                        (OMElement) idAndOMElementMap.get(hrefAttributeValue.substring(1));
                if (referedOMElement == null) {
                    throw new AxisFault("In valid href ==> " + hrefAttributeValue + " can not find" +
                            "the matching element");
                } else {
                    // now we have to remove the hrefAttribute and add all the child elements to the
                    // element being proccesed
                    elementToProcess.removeAttribute(hrefAttribute);
                    // Charged before the copy: the children added here are walked
                    // below, and may carry hrefs of their own.
                    budget.spend(depth);
                    OMElement clonedReferenceElement = getClonedOMElement(referedOMElement, omFactory);
                    OMNode omNode = null;
                    for (Iterator iter = clonedReferenceElement.getChildren(); iter.hasNext();) {
                        omNode = (OMNode) iter.next();
                        iter.remove();
                        elementToProcess.addChild(omNode);
                    }

                    // add attributes
                    OMAttribute omAttribute = null;
                    for (Iterator iter = clonedReferenceElement.getAllAttributes(); iter.hasNext();) {
                        omAttribute = (OMAttribute) iter.next();
                        // we do not have to populate the id attribute
                        if (!omAttribute.getLocalName().equals("id")) {
                            elementToProcess.addAttribute(omAttribute);
                        }
                    }
                }
            }
        }

        // call recursively to proces all elements
        OMElement childOMElement = null;
        for (Iterator iter = elementToProcess.getChildElements(); iter.hasNext();) {
            childOMElement = (OMElement) iter.next();
            processHrefAttributes(idAndOMElementMap, childOMElement, omFactory,
                    budget, depth + 1);
        }
    }

    /**
     * returns an cloned om element for this OMElement
     *
     * @param omElement
     * @return cloned omElement
     */
    public static OMElement getClonedOMElement(OMElement omElement, OMFactory omFactory) throws AxisFault {

        OMElement newOMElement = omFactory.createOMElement(omElement.getQName());

        // copying attributes
        OMAttribute omAttribute = null;
        OMAttribute newOMAttribute = null;
        for (Iterator iter = omElement.getAllAttributes(); iter.hasNext();) {
            omAttribute = (OMAttribute) iter.next();
            if (!omAttribute.getAttributeValue().equals("id")) {
                newOMAttribute = omFactory.createOMAttribute(
                        omAttribute.getLocalName(),
                        omAttribute.getNamespace(),
                        omAttribute.getAttributeValue());
                newOMElement.addAttribute(newOMAttribute);
            }
        }
        OMNode omNode = null;
        OMText omText = null;
        for (Iterator iter = omElement.getChildren(); iter.hasNext();) {
            omNode = (OMNode) iter.next();
            if (omNode instanceof OMText) {
                omText = (OMText) omNode;
                newOMElement.addChild(omFactory.createOMText(omText.getText()));
            } else if (omNode instanceof OMElement) {
                newOMElement.addChild(getClonedOMElement((OMElement) omNode, omFactory));
            } else {
                throw new AxisFault("Unknown child element type ==> " + omNode.getClass().getName());
            }
        }
        return newOMElement;
    }

}
