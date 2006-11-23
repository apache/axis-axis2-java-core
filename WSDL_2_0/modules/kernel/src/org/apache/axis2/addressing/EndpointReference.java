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


package org.apache.axis2.addressing;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMNode;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Class EndpointReference
 * This class models the WS-A EndpointReferenceType. But this can be used without any WS-A handlers as well
 * Since the models for this in Submission and Final versions are different, lets make this to comply with
 * WS-A Final version. So any information found with WS-A submission will be "pumped" in to this model.
 */
public class EndpointReference implements Serializable {

    private static final long serialVersionUID = 5278892171162372439L;

    private static final Log log = LogFactory.getLog(EndpointReference.class);
    
    /**
     * <EndpointReference>
     *    <Address>xs:anyURI</Address>
     *    <ReferenceParameters>xs:any*</ReferenceParameters>
     *    <MetaData>xs:any*</MetaData>
     * <!-- In addition to this, EPR can contain any number of OMElements -->
     * </EndpointReference>
     */

    private String name;
    private String address;
    private ArrayList metaData;
    private Map referenceParameters;
    private ArrayList extensibleElements;
    private ArrayList attributes;


    /**
     * @param address
     */
    public EndpointReference(String address) {
        this.address = address;
    }

    /**
     * @param omElement
     */
    public void addReferenceParameter(OMElement omElement) {
        if (omElement == null) {
            return;
        }
        if (referenceParameters == null) {
            referenceParameters = new HashMap();
        }
        referenceParameters.put(omElement.getQName(), omElement);
    }

    /**
     * @param qname
     * @param value - the text of the OMElement. Remember that this is a convenient method for the user,
     *              which has limited capability. If you want more power use @See EndpointReference#addReferenceParameter(OMElement)
     */
    public void addReferenceParameter(QName qname, String value) {
        if (qname == null) {
            return;
        }
        OMElement omElement = OMAbstractFactory.getOMFactory().createOMElement(qname, null);
        omElement.setText(value);
        addReferenceParameter(omElement);
    }

    /**
     * This will return a Map of reference parameters with QName as the key and an OMElement
     * as the value
     *
     * @return - map of the reference parameters, where the key is the QName of the reference parameter
     *         and the value is an OMElement
     */
    public Map getAllReferenceParameters() {
        return referenceParameters;
    }

    public String getAddress() {
        return address;
    }

    /**
     * @param address - xs:anyURI
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * hasAnonymousAddress
     * 
     * @return true if address is 'Anonymous URI' from either supported addressing version
     */
    public boolean hasAnonymousAddress(){
        boolean result  = (AddressingConstants.Final.WSA_ANONYMOUS_URL.equals(address) ||
                           AddressingConstants.Submission.WSA_ANONYMOUS_URL.equals(address));
        if(log.isTraceEnabled()){
            log.trace("hasAnonymousAddress: "+address+" is Anonymous: "+result);
        }
        return result;
    }
    
    /**
     * hasNoneAddress
     * 
     * @return true if the address is the 'None URI' from the final addressing spec.
     */
    public boolean hasNoneAddress() {
        boolean result = AddressingConstants.Final.WSA_NONE_URI.equals(address);
        if(log.isTraceEnabled()){
            log.trace("hasNoneAddress: "+address+" is None: "+result);
        }
        return result;
    }
    
    /**
     * @param localName
     * @param ns
     * @param value
     */
    public void addAttribute(String localName, OMNamespace ns, String value) {
        if (attributes == null) {
            attributes = new ArrayList();
        }
        attributes.add(OMAbstractFactory.getOMFactory().createOMAttribute(localName, ns, value));
    }

    public ArrayList getAttributes() {
        return attributes;
    }


    /**
     * @param omAttribute
     */
    public void addAttribute(OMAttribute omAttribute) {
        if (attributes == null) {
            attributes = new ArrayList();
        }
        attributes.add(omAttribute);
    }

    public ArrayList getExtensibleElements() {
        return extensibleElements;
    }

    /**
     * {any}
     *
     * @param extensibleElements
     */
    public void setExtensibleElements(ArrayList extensibleElements) {
        this.extensibleElements = extensibleElements;
    }

    public void addExtensibleElement(OMElement extensibleElement) {
        if (extensibleElement != null) {
            if (this.extensibleElements == null) {
                this.extensibleElements = new ArrayList();
            }
            this.extensibleElements.add(extensibleElement);
        }
    }

    public ArrayList getMetaData() {
        return metaData;
    }

    public void addMetaData(OMNode metaData) {
        if (metaData != null) {
            if (this.metaData == null) {
                this.metaData = new ArrayList();
            }
            this.metaData.add(metaData);
        }

    }

    /**
     * @deprecated
     */
    public String getName() {
        return name;
    }

    /**
     * 
     * @param name
     * @deprecated
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Set a Map with QName as the key and an OMElement
     * as the value
     *
     * @param referenceParameters
     */
    public void setReferenceParameters(Map referenceParameters) {
        this.referenceParameters = referenceParameters;
    }
    
    /*
     *  (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer("Address: " + address);
        
        if (metaData != null)
            buffer.append(", Metadata: ").append(metaData);

        if (referenceParameters != null)
            buffer.append(", Reference Parameters: ").append(referenceParameters);
        
        if (extensibleElements != null)
            buffer.append(", Extensibility elements: ").append(extensibleElements);
        
        if (attributes != null)
            buffer.append(", Attributes: ").append(attributes);
        
        return buffer.toString();
    }

    /**
     * 
     * @param eprOMElement
     * @deprecated use {@link org.apache.axis2.addressing.EndpointReferenceHelper#fromOM(OMElement)} instead.
     */
    public void fromOM(OMElement eprOMElement) {
        setAddress(eprOMElement.getFirstChildWithName(new QName("Address")).getText());
        OMElement refParamElement = eprOMElement.getFirstChildWithName(new QName(AddressingConstants.EPR_REFERENCE_PARAMETERS));

        if (refParamElement != null) {
            Iterator refParams = refParamElement.getChildElements();
            while (refParams.hasNext()) {
                OMElement omElement = (OMElement) refParams.next();
                addReferenceParameter(omElement);
            }
        }


        OMElement metaDataElement = eprOMElement.getFirstChildWithName(new QName(AddressingConstants.Final.WSA_METADATA));
        if (metaDataElement != null) {
            Iterator children = metaDataElement.getChildren();
            while (children.hasNext()) {
                OMNode omNode = (OMNode) children.next();
                addMetaData(omNode);
            }
        }

        setName(eprOMElement.getLocalName());

        Iterator allAttributes = eprOMElement.getAllAttributes();
        if (attributes == null) {
            attributes = new ArrayList();
        }

        while (allAttributes.hasNext()) {
            OMAttribute attribute = (OMAttribute) allAttributes.next();
            attributes.add(attribute);
        }
        
        Iterator childElements = eprOMElement.getChildElements();
        while (childElements.hasNext()) {
            OMElement eprChildElement = (OMElement) childElements.next();
            String localName = eprChildElement.getLocalName();
            if(!localName.equals("Address") &&
               !localName.equals(AddressingConstants.EPR_REFERENCE_PARAMETERS) &&
               !localName.equals(AddressingConstants.Final.WSA_METADATA)){
                addExtensibleElement(eprChildElement);
            }
        }
    }

    /**
     * 
     * @param nsurl
     * @param localName
     * @param prefix
     * @throws AxisFault
     * @deprecated  use {@link org.apache.axis2.addressing.EndpointReferenceHelper#toOM(EndpointReference, QName, String)} instead.
     */
    public OMElement toOM(String nsurl, String localName, String prefix) throws AxisFault {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        if (prefix != null) {
            OMNamespace wrapNs = fac.createOMNamespace(nsurl, prefix);
            OMElement epr = fac.createOMElement(localName, wrapNs);
            OMNamespace wsaNS = fac.createOMNamespace(AddressingConstants.Final.WSA_NAMESPACE, AddressingConstants.WSA_DEFAULT_PREFIX);
            OMElement addressE = fac.createOMElement(AddressingConstants.EPR_ADDRESS, wsaNS, epr);
            addressE.setText(address);
            
            if (this.metaData != null) {
                OMElement metadataE = fac.createOMElement(AddressingConstants.Final.WSA_METADATA, wsaNS, epr);
                Iterator metadata = this.metaData.iterator();
                while (metadata.hasNext()) {
                    metadataE.addChild((OMNode) metadata.next());
                }
            }

            if (this.referenceParameters != null) {
                OMElement refParameterElement = fac.createOMElement(AddressingConstants.EPR_REFERENCE_PARAMETERS, wsaNS, epr);
                Iterator refParms = referenceParameters.values().iterator();
                while (refParms.hasNext()) {
                    refParameterElement.addChild((OMNode) refParms.next());
                }
            }
            
            if (attributes != null) {
                Iterator attrIter = attributes.iterator();
                while (attrIter.hasNext()) {
                    OMAttribute omAttributes = (OMAttribute) attrIter.next();
                    epr.addAttribute(omAttributes);
                }
            }
            
            // add xs:any
            ArrayList omElements = extensibleElements;
            if (omElements != null) {
                for (int i = 0; i < omElements.size(); i++) {
                    epr.addChild((OMElement) omElements.get(i));
                }
            }
            
            return epr;
        } else {
            throw new AxisFault("prefix must be specified");
        }
    }


}
