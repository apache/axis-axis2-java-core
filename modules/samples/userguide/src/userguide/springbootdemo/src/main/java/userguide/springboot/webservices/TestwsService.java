
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
package userguide.springboot.webservices;

import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Validator;

import org.springframework.stereotype.Component;

@Component
public class TestwsService {

    private static final Logger logger = LogManager.getLogger(TestwsService.class);

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public TestwsResponse doTestws(TestwsRequest request) {

        String uuid = UUID.randomUUID().toString();

        String logPrefix = "TestwsService.doTestws() , uuid: " + uuid + " , ";

        logger.warn(logPrefix + "starting on request: " + request.toString());
        TestwsResponse response = new TestwsResponse();

        try {
            // All data is evil!
            Validator validator = ESAPI.validator();
            boolean messageinstatus = validator.isValidInput("userInput", request.getMessagein(), "SafeString", 100 , false);
            if (!messageinstatus) {
                logger.error(logPrefix + "returning with failure status on invalid messagein: " + request.getMessagein());
                response.setStatus("FAILED");
                return response;
            }
            response.setStatus("OK");
            String evil = "<script xmlns=\"http://www.w3.org/1999/xhtml\">alert('Hello');</script> \">";
            response.setMessageout(evil);
            
            logger.warn(logPrefix + "returning response: " + response.toString());
            return response;

        } catch (Exception ex) {
            logger.error(logPrefix + "failed: " + ex.getMessage(), ex);
            response.setStatus("FAILED");
            return response;
        }

    }

}
