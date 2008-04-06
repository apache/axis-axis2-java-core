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

/**
 * PerfPortTypeSkeleton.java This file was auto-generated from WSDL by the
 * Apache Axis2 version: 0.94-dev Jan 07, 2006 (08:13:00 EST)
 */
package samples.wsdl.perf2;

/** PerfPortTypeSkeleton java skeleton for the axisService */
public class PerfPortTypeSkeleton {
    /**
     * Auto generated method signature
     *
     * @param param0
     */
    public OutputElement handleStringArray(
            InputElement param0) {

        OutputElement output = new OutputElement();
        output.setOutputElement("The Array length is - " + param0.getItem().length);
        return output;
    }
}
