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
package org.apache.axis2.osgi.service;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.apache.axis2.osgi.deployment.tracker.WSTracker;

import java.util.Dictionary;
import java.util.Properties;

/*
* 
*/
public class Activator implements BundleActivator {

    public void start(BundleContext context) throws Exception {
        Dictionary prop = new Properties();
        prop.put(WSTracker.AXIS2_WS, "myCal");
        context.registerService(Calculator.class.getName(), new Calculator(), prop);
    }

    public void stop(BundleContext context) throws Exception {

    }
}
