package org.apache.axis.deployment.util;

import org.apache.axis.deployment.DeploymentException;

import java.util.Vector;

/**
 * Copyright 2001-2004 The Apache Software Foundation.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @author Deepal Jayasinghe
 *         Oct 18, 2004
 *         3:16:17 PM
 *
 */
public class Handler {
    /**
     * Following constant values need to parse the <handler>..<handler> tag
     */
    public static final String REF = "ref";
    public static final String CLASSNAME = "class";
    public static final String NAME = "name";
    public static final String BEFORE = "before";
    public static final String AFTER = "after";
    public static final String PHASE = "phase";
    public static final String PHASEFIRST = "phaseFirst";
    public static final String PHASELAST = "phaseLast";

    public static final String ORDER = "order";  // to resolve the order tag

    /**
     * ************************
     * Handler properties
     */
    private Vector parameters =new Vector();

    private String ref;

    private String className ;// represent the class atribute in the handler element
    private String name ;

    private String before;
    private String after;

    private String phase;
    private boolean phaseFirst;
    private boolean phaseLast;

    private int count = 0;  // to keep the number of parameters

    public Handler() {
        this.name = "";
        this.ref = "";
        this.className = "";
        this.before ="";
        this.after = "";
        this.phase = "";
        this.phaseFirst = false;
        this.phaseLast = false;

        parameters.removeAllElements();
    }

    public Vector getParameters() {
        return parameters;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
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

    public String getPhase() {
        return phase;
    }

    public void setPhase(String phase) {
        this.phase = phase;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addParameter(Parameter parameter){
        parameters.add(parameter);
        count ++;
    }

    public Parameter getParameter(int index){
        if(index < count){
            return  (Parameter)parameters.get(index) ;
        }
        else {
            return null;
        }
    }

    public void printMe(){
        System.out.println("===========================================");
        System.out.println("Name : " + getName());
        System.out.println("Ref : "  + getRef());
        System.out.println("Class : " + getClassName());
         System.out.println("Phase : " + getPhase());
        System.out.println("===========================================");
    }
}
