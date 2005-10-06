package org.apache.axis2.databinding;

import junit.framework.TestCase;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.databinding.extensions.XMLBeans.XMLBeansSchemaUtility;
import org.apache.axis2.description.ServiceDescription;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/*
 * Copyright 2001-2004 The Apache Software Foundation.
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
 *
 * @author : Eran Chinthaka (chinthaka@apache.org)
 */

public class XMLBeansSchemaUtilityTest extends TestCase {

    AxisConfiguration ar;
    String repo = "./test-resources/XMLBeanSchemaUtilityRepo";
    protected Log log = LogFactory.getLog(getClass());


    private ServiceDescription getDummyServiceDescription() throws AxisFault {
        ConfigurationContextFactory builder = new ConfigurationContextFactory();
        ar = builder.buildConfigurationContext(repo).getAxisConfiguration();
        return ar.getService("databindingService");
    }

    public void testIsRelevant(){
        try {
            XMLBeansSchemaUtility xmlBeansSchemaUtility = new XMLBeansSchemaUtility();
            assertTrue(xmlBeansSchemaUtility.isRelevant(getDummyServiceDescription()));
        } catch (AxisFault axisFault) {
            log.error("Error in testIsRelevant ", axisFault);
            fail("Error");
        }
    }

    public void testGetSchema(){
        try {
            XMLBeansSchemaUtility xmlBeansSchemaUtility = new XMLBeansSchemaUtility();
            ServiceDescription dummyServiceDescription = getDummyServiceDescription();
            OMElement schema = xmlBeansSchemaUtility.getSchema(dummyServiceDescription);
        } catch (AxisFault axisFault) {
            log.error("Error in testIsRelevant ", axisFault);
            fail("Error");
        }
    }


}
