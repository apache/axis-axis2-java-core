/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *      
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.axis2.jaxws.description.validator;

import java.util.ArrayList;
import java.util.List;


/**
 * 
 */
public class ValidationFailures {

    ArrayList<ValidationFailure> validationFailures = new ArrayList<ValidationFailure>();

    public void add(Validator failingValidator, String message) {
        validationFailures.add(new ValidationFailure(failingValidator, message));
    }

    public List<ValidationFailure> getValidationFailures() {
        return validationFailures;
    }

}

class ValidationFailure {
    Validator validator;
    String message;

    ValidationFailure(Validator validator, String message) {
        this.validator = validator;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public Validator getValidator() {
        return validator;
    }
}
