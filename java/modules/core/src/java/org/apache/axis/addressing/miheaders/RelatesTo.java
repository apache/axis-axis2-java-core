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
package org.apache.axis.addressing.miheaders;

/**
 * Class RelatesTo
 */
public class RelatesTo {
    /**
     * Field address
     */
    private String address;

    /**
     * Field relationshipType
     */
    private String relationshipType = "wsa:Reply";

    /**
     * Constructor RelatesTo
     *
     * @param address
     */
    public RelatesTo(String address) {
        this.address = address;
    }

    /**
     * Constructor RelatesTo
     *
     * @param address
     * @param relationshipType
     */
    public RelatesTo(String address, String relationshipType) {
        this.address = address;
        this.relationshipType = relationshipType;
    }

    /**
     * Method getAddress
     *
     * @return
     */
    public String getAddress() {
        return address;
    }

    /**
     * Method setAddress
     *
     * @param address
     */
    public void setAddress(String address) {
        this.address = address;
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
