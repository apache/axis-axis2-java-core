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
*
*/

package org.apache.axis2.databinding.utils;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.AxisFault;
import org.apache.axis2.databinding.typemapping.SimpleTypeMapper;
import org.apache.axis2.engine.ObjectSupplier;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.Iterator;

public class MultirefHelper {

    public static final String SOAP12_REF_ATTR = "ref";
    public static final String SOAP11_REF_ATTR = "href";

    private boolean filledTable;

    private OMElement parent;

    private HashMap objectmap = new HashMap();
    private HashMap elementMap = new HashMap();
    private HashMap omElementMap = new HashMap();

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
            OMElement ele = processElementforRefs(val);
            OMElement cloneele = elementClone(ele);
            omElementMap.put(id, cloneele);
            return cloneele;
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
                OMElement ele2 = elementClone(tempele);
                Iterator itrChild = ele2.getChildren();
                while (itrChild.hasNext()) {
                    Object obj = itrChild.next();
                    if (obj instanceof OMNode) {
                        omElement.addChild((OMNode)obj);
                    }
                }
            }
        }
        return elemnts;
    }

    private OMElement elementClone(OMElement ele) {
        return new StAXOMBuilder(ele.getXMLStreamReader()).getDocumentElement();
    }

    public Object processRef(Class javatype, String id, ObjectSupplier objectSupplier)
            throws AxisFault {
        if (!filledTable) {
            readallChildElements();
        }
        OMElement val = (OMElement)elementMap.get(id);
        if (val == null) {
            throw new AxisFault("Invalid reference :" + id);
        } else {
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
            } else if (SimpleTypeMapper.isCollection(javatype)) {
                Object valobj = SimpleTypeMapper.getArrayList(val);
                objectmap.put(id, valobj);
                return valobj;
            } else {
                Object obj = BeanUtil.deserialize(javatype, val, this, objectSupplier);
                objectmap.put(id, obj);
                return obj;
            }
        }
    }

    private void readallChildElements() {
        Iterator childs = parent.getChildElements();
        while (childs.hasNext()) {
            OMElement omElement = (OMElement)childs.next();
            OMAttribute id = omElement.getAttribute(new QName("id"));
            if (id != null) {
                omElement.build();
                elementMap.put(id.getAttributeValue(), omElement.detach());
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

}
