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
 */

package org.apache.axis.engine.registry;

/**
 * @author hemapani@opensource.lk
 */
public class ConcreateFlowInclude implements FlowInclude {
    private Flow in;
    private Flow out;
    private Flow fault;
    public Flow getFaultFlow() {
        return fault;
    }

    public Flow getInFlow() {
        return in;
    }

    public Flow getOutFlow() {
        return out;
    }

    public void setFaultFlow(Flow flow) {
        this.fault = flow;
    }

    public void setInFlow(Flow flow) {
        this.in = flow;
    }

    public void setOutFlow(Flow flow) {
        this.out = flow;
    }
}
