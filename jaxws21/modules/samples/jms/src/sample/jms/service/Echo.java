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

package sample.jms.service;

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @version $Rev: $ $Date: $
 */

public class Echo {

	private static final Log log = LogFactory.getLog(Echo.class);
    public Echo() {
    }

//    public void echoVoid() {
//        log.info("echo Service Called");
//    }
//
//    public void echoOMElementNoResponse(OMElement omEle) {
//        log.info("echoOMElementNoResponse service called.");
//    }

    public OMElement echoOMElement(OMElement omEle) {
        log.info("echoOMElement service called.");
    	omEle.buildWithAttachments();
        omEle.setLocalName(omEle.getLocalName() + "Response");
        if(omEle.getFirstElement().getText().trim().startsWith("fault")){
            throw new RuntimeException("fault string found in echoOMElement");
        }
        return omEle;
    }
//     public OMElement echoOM(OMElement omEle) {
//        return omEle;
//    }
//
//    public String echoString(String in) {
//        return in;
//    }
//
//    public int echoInt(int in) {
//        return in;
//    }
//
//    public OMElement echoMTOMtoBase64(OMElement omEle) {
//        OMText omText =  (OMText) (omEle.getFirstElement()).getFirstOMChild();
//        omText.setOptimize(false);
//        return omEle;
//    }
}