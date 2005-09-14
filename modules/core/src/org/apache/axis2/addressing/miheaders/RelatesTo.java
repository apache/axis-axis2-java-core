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
package org.apache.axis2.addressing.miheaders;

import java.io.Serializable;

/**
 * Class RelatesTo
 */
public class RelatesTo implements Serializable{
    /**
     * Field value
     */
    private String value;

    /**
     * Field relationshipType
     */
    private String relationshipType;

    /**
     * Constructor RelatesTo
     *
     * @param address
     */
    public RelatesTo(String address) {
        this.value = address;
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
     * Method getValue
     *
     * @return
     */
    public String getValue() {
        return value;
    }

    /**
     * Method setValue
     *
     * @param value
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Method getRelationshipType
     *
     * @return
     */
    public String getRelationshipType() {
        return relationshipType;
    }

    /**
     * Method setRelationshipType
     *
     * @param relationshipType
     */
    public void setRelationshipType(String relationshipType) {
        this.relationshipType = relationshipType;
    }
}
