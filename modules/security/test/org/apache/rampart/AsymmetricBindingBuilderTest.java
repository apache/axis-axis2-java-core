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
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.conversation.ConversationConstants;

import javax.xml.namespace.QName;

import java.util.ArrayList;

public class AsymmetricBindingBuilderTest extends MessageBuilderTestBase {
//    
//    public void testAsymmBinding() {
//        try {
//            MessageContext ctx = getMsgCtx();
//            
//            String policyXml = "test-resources/policy/rampart-asymm-binding-1.xml";
//            Policy policy = this.loadPolicy(policyXml);
//            
//            ctx.setProperty(RampartMessageData.KEY_RAMPART_POLICY, policy);
//            
//            MessageBuilder builder = new MessageBuilder();
//            builder.build(ctx);
//
//            ArrayList list = new ArrayList();
//            
//            list.add(new QName(WSConstants.WSU_NS, WSConstants.TIMESTAMP_TOKEN_LN));
//            list.add(new QName(WSConstants.ENC_NS, WSConstants.ENC_KEY_LN));
//            list.add(new QName(WSConstants.SIG_NS, WSConstants.SIG_LN));
//            
//            this.verifySecHeader(list.iterator(), ctx.getEnvelope());
//        } catch (Exception e) {
//            e.printStackTrace();
//            fail(e.getMessage());
//        }
//    }
//    
//    public void testAsymmBindingServerSide() {
//        try {
//            MessageContext ctx = getMsgCtx();
//            
//            ctx.setServerSide(true);
//            String policyXml = "test-resources/policy/rampart-asymm-binding-1.xml";
//            Policy policy = this.loadPolicy(policyXml);
//            
//            ctx.setProperty(RampartMessageData.KEY_RAMPART_POLICY, policy);
//            
//            MessageBuilder builder = new MessageBuilder();
//            builder.build(ctx);
//            
//            ArrayList list = new ArrayList();
//            
//            list.add(new QName(WSConstants.WSU_NS, WSConstants.TIMESTAMP_TOKEN_LN));
//            list.add(new QName(WSConstants.ENC_NS, WSConstants.ENC_KEY_LN));
//            list.add(new QName(WSConstants.SIG_NS, WSConstants.SIG_LN));
//            
//            this.verifySecHeader(list.iterator(), ctx.getEnvelope());
//        } catch (Exception e) {
//            e.printStackTrace();
//            fail(e.getMessage());
//        }
//    }
//    
//    public void testAsymmBindingWithSigDK() {
//        try {
//            MessageContext ctx = getMsgCtx();
//            
//            String policyXml = "test-resources/policy/rampart-asymm-binding-2-sig-dk.xml";
//            Policy policy = this.loadPolicy(policyXml);
//            
//            ctx.setProperty(RampartMessageData.KEY_RAMPART_POLICY, policy);
//            
//            MessageBuilder builder = new MessageBuilder();
//            builder.build(ctx);
//            
//            ArrayList list = new ArrayList();
//            
//            list.add(new QName(WSConstants.WSU_NS, WSConstants.TIMESTAMP_TOKEN_LN));
//            list.add(new QName(WSConstants.ENC_NS, WSConstants.ENC_KEY_LN));
//            list.add(new QName(WSConstants.WSSE_NS, WSConstants.BINARY_TOKEN_LN));
//            list.add(new QName(WSConstants.ENC_NS, WSConstants.ENC_KEY_LN));
//            list.add(new QName(ConversationConstants.WSC_NS_05_02, ConversationConstants.DERIVED_KEY_TOKEN_LN));
//            list.add(new QName(WSConstants.SIG_NS, WSConstants.SIG_LN));
//            
//            this.verifySecHeader(list.iterator(), ctx.getEnvelope());
//            
//        } catch (Exception e) {
//            e.printStackTrace();
//            fail(e.getMessage());
//        }
//    }
//    
//    public void testAsymmBindingWithDK() {
//        try {
//            MessageContext ctx = getMsgCtx();
//            
//            String policyXml = "test-resources/policy/rampart-asymm-binding-3-dk.xml";
//            Policy policy = this.loadPolicy(policyXml);
//            
//            ctx.setProperty(RampartMessageData.KEY_RAMPART_POLICY, policy);
//            
//            MessageBuilder builder = new MessageBuilder();
//            builder.build(ctx);
//            
//            ArrayList list = new ArrayList();
//            
//            list.add(new QName(WSConstants.WSU_NS, WSConstants.TIMESTAMP_TOKEN_LN));
//            list.add(new QName(WSConstants.WSSE_NS, WSConstants.BINARY_TOKEN_LN));
//            list.add(new QName(WSConstants.ENC_NS, WSConstants.ENC_KEY_LN));
//            list.add(new QName(ConversationConstants.WSC_NS_05_02, ConversationConstants.DERIVED_KEY_TOKEN_LN));
//            list.add(new QName(WSConstants.ENC_NS, WSConstants.REF_LIST_LN));
//            list.add(new QName(ConversationConstants.WSC_NS_05_02, ConversationConstants.DERIVED_KEY_TOKEN_LN));
//            list.add(new QName(WSConstants.SIG_NS, WSConstants.SIG_LN));
//            
//            this.verifySecHeader(list.iterator(), ctx.getEnvelope());
//            
//        } catch (Exception e) {
//            e.printStackTrace();
//            fail(e.getMessage());
//        }
//    }
//    
//    public void testAsymmBindingWithDKEncrBeforeSig() {
//        try {
//            MessageContext ctx = getMsgCtx();
//            
//            String policyXml = "test-resources/policy/rampart-asymm-binding-4-dk-ebs.xml";
//            Policy policy = this.loadPolicy(policyXml);
//            
//            ctx.setProperty(RampartMessageData.KEY_RAMPART_POLICY, policy);
//            
//            MessageBuilder builder = new MessageBuilder();
//            builder.build(ctx);
//            
//            ArrayList list = new ArrayList();
//            
//            list.add(new QName(WSConstants.WSU_NS, WSConstants.TIMESTAMP_TOKEN_LN));
//            list.add(new QName(WSConstants.ENC_NS, WSConstants.ENC_KEY_LN));
//            list.add(new QName(ConversationConstants.WSC_NS_05_02, ConversationConstants.DERIVED_KEY_TOKEN_LN));
//            list.add(new QName(WSConstants.SIG_NS, WSConstants.SIG_LN));
//            list.add(new QName(ConversationConstants.WSC_NS_05_02, ConversationConstants.DERIVED_KEY_TOKEN_LN));
//            list.add(new QName(WSConstants.ENC_NS, WSConstants.REF_LIST_LN));
//             
//            this.verifySecHeader(list.iterator(), ctx.getEnvelope());
//            
//        } catch (Exception e) {
//            e.printStackTrace();
//            fail(e.getMessage());
//        }
//    }
//    
//    
//    public void testAsymmBindingEncrBeforeSig() {
//        try {
//            MessageContext ctx = getMsgCtx();
//            
//            String policyXml = "test-resources/policy/rampart-asymm-binding-5-ebs.xml";
//            Policy policy = this.loadPolicy(policyXml);
//            
//            ctx.setProperty(RampartMessageData.KEY_RAMPART_POLICY, policy);
//            
//            MessageBuilder builder = new MessageBuilder();
//            builder.build(ctx);
//            
//            ArrayList list = new ArrayList();
//            
//            list.add(new QName(WSConstants.WSU_NS, WSConstants.TIMESTAMP_TOKEN_LN));
//            list.add(new QName(WSConstants.ENC_NS, WSConstants.ENC_KEY_LN));
//            list.add(new QName(WSConstants.SIG_NS, WSConstants.SIG_LN));
//            list.add(new QName(WSConstants.ENC_NS, WSConstants.REF_LIST_LN));
//             
//            this.verifySecHeader(list.iterator(), ctx.getEnvelope());
//        } catch (Exception e) {
//            e.printStackTrace();
//            fail(e.getMessage());
//        }
//    }
//    
    public void testAsymmBindingTripleDesRSA15() {
        try {
            MessageContext ctx = getMsgCtx();
            
            String policyXml = "test-resources/policy/rampart-asymm-binding-6-3des-r15.xml";
            Policy policy = this.loadPolicy(policyXml);
            
            ctx.setProperty(RampartMessageData.KEY_RAMPART_POLICY, policy);
            
            MessageBuilder builder = new MessageBuilder();
            builder.build(ctx);
            System.out.println(ctx.getEnvelope());
            ArrayList list = new ArrayList();
            
            list.add(new QName(WSConstants.WSU_NS, WSConstants.TIMESTAMP_TOKEN_LN));
            list.add(new QName(WSConstants.ENC_NS, WSConstants.ENC_KEY_LN));
            list.add(new QName(WSConstants.WSSE_NS, WSConstants.BINARY_TOKEN_LN));
            list.add(new QName(WSConstants.SIG_NS, WSConstants.SIG_LN));
            
            this.verifySecHeader(list.iterator(), ctx.getEnvelope());
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    public void testAsymmBindingTripleDesRSA15DK() {
        try {
            MessageContext ctx = getMsgCtx();
            
            String policyXml = "test-resources/policy/rampart-asymm-binding-7-3des-r15-DK.xml";
            Policy policy = this.loadPolicy(policyXml);
            
            ctx.setProperty(RampartMessageData.KEY_RAMPART_POLICY, policy);
            
            MessageBuilder builder = new MessageBuilder();
            builder.build(ctx);

            System.out.println(ctx.getEnvelope());
            
//            ArrayList list = new ArrayList();
//            
//            list.add(new QName(WSConstants.WSU_NS, WSConstants.TIMESTAMP_TOKEN_LN));
//            list.add(new QName(WSConstants.ENC_NS, WSConstants.ENC_KEY_LN));
//            list.add(new QName(WSConstants.SIG_NS, WSConstants.SIG_LN));
//            
//            this.verifySecHeader(list.iterator(), ctx.getEnvelope());
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
    
}
