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
package org.apache.axis.description;

/**
 * Class FlowIncludeImpl
 */
public class FlowIncludeImpl implements FlowInclude {
    /**
     * Field in
     */
    private Flow in;

    /**
     * Field out
     */
    private Flow out;

    /**
     * Field fault
     */
    private Flow fault;

    /**
     * Method getFaultFlow
     *
     * @return
     */
    public Flow getFaultFlow() {
        return fault;
    }

    /**
     * Method getInFlow
     *
     * @return
     */
    public Flow getInFlow() {
        return in;
    }

    /**
     * Method getOutFlow
     *
     * @return
     */
    public Flow getOutFlow() {
        return out;
    }

    /**
     * Method setFaultFlow
     *
     * @param flow
     */
    public void setFaultFlow(Flow flow) {
        this.fault = flow;
    }

    /**
     * Method setInFlow
     *
     * @param flow
     */
    public void setInFlow(Flow flow) {
        this.in = flow;
    }

    /**
     * Method setOutFlow
     *
     * @param flow
     */
    public void setOutFlow(Flow flow) {
        this.out = flow;
    }
}
