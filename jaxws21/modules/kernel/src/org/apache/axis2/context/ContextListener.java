package org.apache.axis2.context;

/*
* Copyright 2007 The Apache Software Foundation.
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

/**
 * A ContextListener gets informed about new context creations.  Register one with
 * a ConfigurationContext and you'll get contextCreated() notifications for every
 * sub-context creation event.
 */
public interface ContextListener {
    void contextCreated(AbstractContext context);
}
