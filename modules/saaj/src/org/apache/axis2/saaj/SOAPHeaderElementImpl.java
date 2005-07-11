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
package org.apache.axis2.saaj;

import org.apache.axis2.soap.SOAPHeaderBlock;

import javax.xml.soap.SOAPHeaderElement;

/**
 * Class SOAPHeaderImpl
 *
 * @author Ashutosh Shahi
 *         ashutosh.shahi@gmail.com
 */
public class SOAPHeaderElementImpl extends SOAPElementImpl implements
        SOAPHeaderElement {

    /**
     * Field omHeaderElement
     */
    SOAPHeaderBlock omHeaderElement;

    /**
     * Constructor SOAPHeaderElementImpl
     *
     * @param headerElement
     */
    public SOAPHeaderElementImpl(
            org.apache.axis2.soap.SOAPHeaderBlock headerElement) {
        super(headerElement);
        this.omHeaderElement = headerElement;
    }

    /**
     * method setActor
     *
     * @param actorURI
     * @see javax.xml.soap.SOAPHeaderElement#setActor(java.lang.String)
     */
    public void setActor(String actorURI) {

        omHeaderElement.setRole(actorURI);
    }

    /**
     * method getActor
     *
     * @return
     * @see javax.xml.soap.SOAPHeaderElement#getActor()
     */
    public String getActor() {

        return omHeaderElement.getRole();
    }

    /**
     * method setMustUnderstand
     *
     * @param mustUnderstand
     * @see javax.xml.soap.SOAPHeaderElement#setMustUnderstand(boolean)
     */
    public void setMustUnderstand(boolean mustUnderstand) {

        omHeaderElement.setMustUnderstand(mustUnderstand);
    }

    /**
     * method getMustUnderstand
     *
     * @return
     * @see javax.xml.soap.SOAPHeaderElement#getMustUnderstand()
     */
    public boolean getMustUnderstand() {

        return omHeaderElement.getMustUnderstand();
    }

}
