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
package org.apache.axis2.jaxbri.identityservice;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import org.apache.axis2.context.MessageContext;

public class IdentityLinkingServiceImpl implements IdentityLinkingServiceSkeletonInterface {
    private Map<String,LinkIdentitiesType> map = new HashMap<String,LinkIdentitiesType>();

    public LinkIdentitiesResponseType createLinkedIdentities(LinkIdentitiesType linkIdentities) {
        assertEquals("LinkIdentities", MessageContext.getCurrentMessageContext().getEnvelope().getSOAPBodyFirstElementLocalName());
        if (map.containsKey(linkIdentities.getOwningPlatform())) {
            fail("Already exists");
        }
        map.put(linkIdentities.getOwningPlatform(), linkIdentities);
        LinkIdentitiesResponseType result = new LinkIdentitiesResponseType();
        result.setOwningPlatform(linkIdentities.getOwningPlatform());
        return result;
    }

    public ModifyLinkResponseType modifyLink(LinkIdentitiesType modifyLink) {
        assertEquals("ModifyLink", MessageContext.getCurrentMessageContext().getEnvelope().getSOAPBodyFirstElementLocalName());
        if (!map.containsKey(modifyLink.getOwningPlatform())) {
            fail("Doesn't exist");
        }
        map.put(modifyLink.getOwningPlatform(), modifyLink);
        ModifyLinkResponseType result = new ModifyLinkResponseType();
        result.setOwningPlatform(modifyLink.getOwningPlatform());
        return result;
    }

    public RemoveLinkResponseType removeLink(LinkIdentitiesType removeLink) {
        assertEquals("RemoveLink", MessageContext.getCurrentMessageContext().getEnvelope().getSOAPBodyFirstElementLocalName());
        if (!map.containsKey(removeLink.getOwningPlatform())) {
            fail("Doesn't exist");
        }
        map.remove(removeLink.getOwningPlatform());
        RemoveLinkResponseType result = new RemoveLinkResponseType();
        result.setOwningPlatform(removeLink.getOwningPlatform());
        return result;
    }
}
