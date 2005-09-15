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
package org.apache.axis2.description;

/**
 * Class PhaseRule
 */
public class PhaseRule {

    // can be either name of phase or handler

    /**
     * Field before
     */
    private String before;

    // can be either name of phase or handler

    /**
     * Field after
     */
    private String after;

    /**
     * Field phaseName
     */
    private String phaseName;

    /**
     * Field phaseFirst
     */
    private boolean phaseFirst;

    /**
     * Field phaseLast
     */
    private boolean phaseLast;

    /**
     * Constructor PhaseRule
     */
    public PhaseRule() {
        this.before = "";
        this.after = "";
        this.phaseName = "";
    }

    public PhaseRule(String phaseName) {
        this.before = "";
        this.after = "";
        this.phaseName = phaseName;
    }

    /**
     * Method getBefore
     *
     * @return
     */
    public String getBefore() {
        return before;
    }

    /**
     * Method setBefore
     *
     * @param before
     */
    public void setBefore(String before) {
        this.before = before;
    }

    /**
     * Method getAfter
     *
     * @return
     */
    public String getAfter() {
        return after;
    }

    /**
     * Method setAfter
     *
     * @param after
     */
    public void setAfter(String after) {
        this.after = after;
    }

    /**
     * Method getPhaseName
     *
     * @return
     */
    public String getPhaseName() {
        return phaseName;
    }

    /**
     * Method setPhaseName
     *
     * @param phaseName
     */
    public void setPhaseName(String phaseName) {
        this.phaseName = phaseName;
    }

    /**
     * Method isPhaseFirst
     *
     * @return
     */
    public boolean isPhaseFirst() {
        return phaseFirst;
    }

    /**
     * Method setPhaseFirst
     *
     * @param phaseFirst
     */
    public void setPhaseFirst(boolean phaseFirst) {
        this.phaseFirst = phaseFirst;
    }

    /**
     * Method isPhaseLast
     *
     * @return
     */
    public boolean isPhaseLast() {
        return phaseLast;
    }

    /**
     * Method setPhaseLast
     *
     * @param phaseLast
     */
    public void setPhaseLast(boolean phaseLast) {
        this.phaseLast = phaseLast;
    }
}
