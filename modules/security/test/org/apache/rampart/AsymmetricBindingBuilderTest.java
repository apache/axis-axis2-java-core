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

package org.apache.rampart;

import org.apache.axis2.context.MessageContext;
import org.apache.neethi.Policy;

public class AsymmetricBindingBuilderTest extends MessageBuilderTestBase {
    
    public void testAsymmBinding() {
        try {
            MessageContext ctx = getMsgCtx();
            
            String policyXml = "test-resources/policy/rampart-asymm-binding-1.xml";
            Policy policy = this.loadPolicy(policyXml);
            
            ctx.setProperty(RampartMessageData.KEY_RAMPART_POLICY, policy);
            
            MessageBuilder builder = new MessageBuilder();
            builder.build(ctx);
            
            System.out.println(ctx.getEnvelope());
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
    
    public void testAsymmBindingServerSide() {
        try {
            MessageContext ctx = getMsgCtx();
            
            ctx.setServerSide(true);
            String policyXml = "test-resources/policy/rampart-asymm-binding-1.xml";
            Policy policy = this.loadPolicy(policyXml);
            
            ctx.setProperty(RampartMessageData.KEY_RAMPART_POLICY, policy);
            
            MessageBuilder builder = new MessageBuilder();
            builder.build(ctx);
            
            System.out.println(ctx.getEnvelope());
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
    
    public void testAsymmBindingWithSigDK() {
        try {
            MessageContext ctx = getMsgCtx();
            
            String policyXml = "test-resources/policy/rampart-asymm-binding-2-sig-dk.xml";
            Policy policy = this.loadPolicy(policyXml);
            
            ctx.setProperty(RampartMessageData.KEY_RAMPART_POLICY, policy);
            
            MessageBuilder builder = new MessageBuilder();
            builder.build(ctx);
            
            System.out.println(ctx.getEnvelope());
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
    
    public void testAsymmBindingWithDK() {
        try {
            MessageContext ctx = getMsgCtx();
            
            String policyXml = "test-resources/policy/rampart-asymm-binding-3-dk.xml";
            Policy policy = this.loadPolicy(policyXml);
            
            ctx.setProperty(RampartMessageData.KEY_RAMPART_POLICY, policy);
            
            MessageBuilder builder = new MessageBuilder();
            builder.build(ctx);
            
            System.out.println(ctx.getEnvelope());
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
    
    public void testAsymmBindingWithDKEncrBeforeSig() {
        try {
            MessageContext ctx = getMsgCtx();
            
            String policyXml = "test-resources/policy/rampart-asymm-binding-4-dk-ebs.xml";
            Policy policy = this.loadPolicy(policyXml);
            
            ctx.setProperty(RampartMessageData.KEY_RAMPART_POLICY, policy);
            
            MessageBuilder builder = new MessageBuilder();
            builder.build(ctx);
            
            System.out.println(ctx.getEnvelope());
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
    
    
    public void testAsymmBindingEncrBeforeSig() {
        try {
            MessageContext ctx = getMsgCtx();
            
            String policyXml = "test-resources/policy/rampart-asymm-binding-5-ebs.xml";
            Policy policy = this.loadPolicy(policyXml);
            
            ctx.setProperty(RampartMessageData.KEY_RAMPART_POLICY, policy);
            
            MessageBuilder builder = new MessageBuilder();
            builder.build(ctx);
            
            System.out.println(ctx.getEnvelope());
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
}
