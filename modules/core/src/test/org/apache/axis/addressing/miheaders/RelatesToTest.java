/**
 * Copyright 2001-2004 The Apache Software Foundation.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p/>
 * Author: Eran Chinthaka - Lanka Software Foundation
 * Date: Dec 22, 2004
 * Time: 12:11:29 PM
 */
package org.apache.axis.addressing.miheaders;

import junit.framework.TestCase;


public class RelatesToTest extends TestCase {
    private RelatesTo relatesTo;
    String address = "www.someaddress.com";
    String relationshipType = "Reply";


    public static void main(String[] args) {
        junit.textui.TestRunner.run(RelatesToTest.class);

    }

    protected void setUp() throws Exception {

    }

    public void testGetAddress() {
        relatesTo = new RelatesTo(address, relationshipType);

        assertEquals("RelatesTo address has not been set properly in the constructor", relatesTo.getAddress(), address);

        String newAddress = "www.newRelation.org";
        relatesTo.setAddress(newAddress);
        assertEquals("RelatesTo address has not been get/set properly", relatesTo.getAddress(), newAddress);

    }

    public void testGetRelationshipType() {
        relatesTo = new RelatesTo(address, relationshipType);

        assertEquals("RelatesTo RelationshipType has not been set properly in the constructor", relatesTo.getRelationshipType(), relationshipType);

        String newRelationshipType = "AnyOtherType";
        relatesTo.setRelationshipType(newRelationshipType);
        assertEquals("RelatesTo address has not been get/set properly", relatesTo.getRelationshipType(), newRelationshipType);
    }

    public void testSingleArgumentConstructor() {
        relatesTo = new RelatesTo(address);
        assertEquals("RelatesTo address has not been set properly in the constructor", relatesTo.getAddress(), address);

    }

}
