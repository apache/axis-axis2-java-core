/*
* Copyright 2003,2004 The Apache Software Foundation.
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
 * @author Deepal Jayasinghe
 */
public class PhaseRule {
    //can be either name of phase or handler
    private String before ;
    //can be either name of phase or handler
    private String after ;

    private String phaseName;
    private boolean phaseFirst;
    private boolean phaseLast;

    public PhaseRule() {
        this.before = "";
        this.after = "";
        this.phaseName = "";
    }

    public String getBefore() {
        return before;
    }

    public void setBefore(String before) {
        this.before = before;
    }

    public String getAfter() {
        return after;
    }

    public void setAfter(String after) {
        this.after = after;
    }

    public String getPhaseName() {
        return phaseName;
    }

    public void setPhaseName(String phaseName) {
        this.phaseName = phaseName;
    }

    public boolean isPhaseFirst() {
        return phaseFirst;
    }

    public void setPhaseFirst(boolean phaseFirst) {
        this.phaseFirst = phaseFirst;
    }

    public boolean isPhaseLast() {
        return phaseLast;
    }

    public void setPhaseLast(boolean phaseLast) {
        this.phaseLast = phaseLast;
    }


}
