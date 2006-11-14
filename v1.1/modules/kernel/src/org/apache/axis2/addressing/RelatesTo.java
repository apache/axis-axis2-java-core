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

import java.io.Serializable;

/**
 * Class RelatesTo
 */
public class RelatesTo implements Serializable {

	private static final long serialVersionUID = 978799688901329012L;

	/**
     * Field relationshipType
     */
    private String relationshipType;

    /**
     * Field value
     */
    private String value;

    /**
     * Constructor RelatesTo
     *
     * @param value
     */
    public RelatesTo(String value) {
        this.value = value;
    }

    /**
     * Constructor RelatesTo
     *
     * @param value
     * @param relationshipType
     */
    public RelatesTo(String value, String relationshipType) {
        this.value = value;
        this.relationshipType = relationshipType;
    }

    /**
     * Method getRelationshipType. If the relationship type has not been set it returns
     * the default value {@link AddressingConstants.Final.WSA_DEFAULT_RELATIONSHIP_TYPE}
     */
    public String getRelationshipType() {
        return (relationshipType != null && !"".equals(relationshipType)?
                    relationshipType :
                    AddressingConstants.Final.WSA_DEFAULT_RELATIONSHIP_TYPE);
    }

    /**
     * Method getValue
     */
    public String getValue() {
        return value;
    }

    /**
     * Method setRelationshipType
     *
     * @param relationshipType
     */
    public void setRelationshipType(String relationshipType) {
        this.relationshipType = relationshipType;
    }

    /**
     * Method setValue
     *
     * @param value
     */
    public void setValue(String value) {
        this.value = value;
    }
    
    /*
     *  (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return "Identifier: " + value
             + ", Relationship type: " + relationshipType;
    }
}
