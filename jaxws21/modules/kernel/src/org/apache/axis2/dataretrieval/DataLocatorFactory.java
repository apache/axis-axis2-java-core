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

package org.apache.axis2.dataretrieval;

/**
 * Factory to constructor Axis2 Data Locators based on the specified
 * Dialect.
 */

public class DataLocatorFactory {
    private static org.apache.axis2.dataretrieval.WSDLDataLocator wsdlDataLocator = null;
    private static org.apache.axis2.dataretrieval.PolicyDataLocator policyDataLocator = null;
    private static org.apache.axis2.dataretrieval.SchemaDataLocator schemaDataLocator = null;

    /*
    * Return instance of default Data Locator for the dialect.
    */
    public static AxisDataLocator createDataLocator(String dialect) {
        return (createDataLocator(dialect, null));
    }

    public static AxisDataLocator createDataLocator(String dialect,
                                                    ServiceData[] serviceDataArray) {
        AxisDataLocator dataLocator;

        if (dialect.equals(DRConstants.SPEC.DIALECT_TYPE_WSDL)) {
            dataLocator = getWsdlDataLocator(serviceDataArray);
        } else if (dialect.trim().equals(DRConstants.SPEC.DIALECT_TYPE_POLICY)) {
            if (policyDataLocator == null) {
                dataLocator = new PolicyDataLocator(serviceDataArray);
            } else {
                dataLocator = policyDataLocator;
            }
        } else if (dialect.equals(DRConstants.SPEC.DIALECT_TYPE_SCHEMA)) {
            if (schemaDataLocator == null) {
                dataLocator = new SchemaDataLocator(serviceDataArray);
            } else {
                dataLocator = schemaDataLocator;
            }
        } else {
            dataLocator = null;
        }
        return dataLocator;
    }

    protected static AxisDataLocator getWsdlDataLocator(ServiceData[] serviceDataArray) {

        if (wsdlDataLocator == null) {
            wsdlDataLocator = new org.apache.axis2.dataretrieval.WSDLDataLocator(serviceDataArray);
        } else {
            wsdlDataLocator.setServiceData(serviceDataArray);
        }
        return wsdlDataLocator;
    }


}
