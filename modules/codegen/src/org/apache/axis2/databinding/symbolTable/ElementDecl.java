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
package org.apache.axis2.databinding.symbolTable;

import javax.xml.namespace.QName;

/**
 * Simple utility struct for holding element declarations.
 * <p/>
 * This simply correlates a QName to a TypeEntry.
 * 
 * @author Glen Daniels (gdaniels@apache.org)
 * @author Tom Jordahl (tomj@apache.org)
 */
public class ElementDecl extends ContainedEntry {

    
    /** Field documentation */
    private String documentation;

    // The following property is set if minOccurs=0.
    // An item that is not set and has minOccurs=0
    // should not be passed over the wire.  This
    // is slightly different than nillable=true which
    // causes nil=true to be passed over the wire.

    /** Field minOccursIs0 */
    private boolean minOccursIs0 = false;

    /** Field nillable */
    private boolean nillable = false;

    /** Field optional */
    private boolean optional = false;

    // Indicate if the ElementDecl represents
    // an xsd:any element

    /** Field anyElement */
    private boolean anyElement = false;

    /** Field maxOccursIsUnbounded */
    private boolean maxOccursIsUnbounded = false;

    /**
     * Constructor ElementDecl
     *
     * @param type
     * @param name
     */
    public ElementDecl(TypeEntry type, QName name) {
        super(type, name);
    }

    /**
     * Method getMinOccursIs0
     *
     * @return
     */
    public boolean getMinOccursIs0() {
        return minOccursIs0;
    }

    /**
     * Method setMinOccursIs0
     *
     * @param minOccursIs0
     */
    public void setMinOccursIs0(boolean minOccursIs0) {
        this.minOccursIs0 = minOccursIs0;
    }

    /**
     * Method getMaxOccursIsUnbounded
     *
     * @return
     */
    public boolean getMaxOccursIsUnbounded() {
        return maxOccursIsUnbounded;
    }

    /**
     * Method setMinOccursIsUnbounded
     *
     * @param maxOccursIsUnbounded
     */
    public void setMaxOccursIsUnbounded(boolean maxOccursIsUnbounded) {
        this.maxOccursIsUnbounded = maxOccursIsUnbounded;
    }

    /**
     * Method setNillable
     *
     * @param nillable
     */
    public void setNillable(boolean nillable) {
        this.nillable = nillable;
    }

    /**
     * Method getNillable
     * 
     * @return 
     */
    public boolean getNillable() {
        return nillable;
    }

    /**
     * Method setOptional
     * 
     * @param optional 
     */
    public void setOptional(boolean optional) {
        this.optional = optional;
    }

    /**
     * Method getOptional
     * 
     * @return 
     */
    public boolean getOptional() {
        return optional;
    }

    /**
     * Method getAnyElement
     * 
     * @return 
     */
    public boolean getAnyElement() {
        return anyElement;
    }

    /**
     * Method setAnyElement
     * 
     * @param anyElement 
     */
    public void setAnyElement(boolean anyElement) {
        this.anyElement = anyElement;
    }

    /**
     * Method getDocumentation
     * 
     * @return string
     */
    public String getDocumentation() {
        return documentation;
    }

    /**
     * Method setDocumentation
     * @param documentation
     */
    public void setDocumentation(String documentation) {
        this.documentation = documentation;
    }
}
