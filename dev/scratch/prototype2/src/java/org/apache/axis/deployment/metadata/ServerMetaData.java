package org.apache.axis.deployment.metadata;

import org.apache.axis.deployment.metadata.phaserule.PhaseMetaData;
import org.apache.axis.description.*;
import org.apache.axis.description.HandlerMetaData;

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
 *         4:18:09 PM
 */
public class ServerMetaData {

    public static String SERVERNAME = "name";

    private String name;
    private Vector parameters = new Vector();
    private Vector handlers = new Vector();
    private Vector modules = new Vector();
    private Vector typemappings = new Vector();
    private Vector phases = new Vector();

    private int parameterCount = 0;
    private int handlerCount = 0;
    private int moduleCount = 0;
    private int typemappingCount = 0;
    private int phaseCount = 0;

    public ServerMetaData() {
        handlers.removeAllElements();
        parameters.removeAllElements();
        modules.removeAllElements();
        phases.removeAllElements();
    }

    /**
     * this is a dumy method  to fill some pahses
     *
     * @return
     */

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void appParameter(Parameter parameter) {
        parameters.add(parameter);
        parameterCount++;
    }

    public Parameter getParameter(int index) {
        if (index <= parameterCount) {
            return (Parameter) parameters.get(index);
        } else
            return null;
    }

    public void addHandlers(HandlerMetaData handler) {
        handlers.add(handler);
        handlerCount++;
    }

    public HandlerMetaData getHandler(int index) {
        if (index <= handlerCount) {
            return (HandlerMetaData) handlers.get(index);
        } else
            return null;
    }

    public void addModule(AxisModule module) {
        modules.add(module);
        moduleCount++;
    }

    public AxisModule getModule(int index) {
        if (index <= moduleCount) {
            return (AxisModule) modules.get(index);
        } else
            return null;
    }

    public void addTypeMapping(TypeMappingMetaData typeMapping) {
        typemappings.add(typeMapping);
        typemappingCount++;
    }

    public TypeMappingMetaData getTypeMapping(int index) {
        if (index <= typemappingCount) {
            return (TypeMappingMetaData) typemappings.get(index);
        } else
            return null;
    }

    public void addPhases(PhaseMetaData phase) {
        phases.add(phase);
        phaseCount++;
    }

    public boolean isPhaseExist(String phaseName) {
        for (int i = 0; i < phases.size(); i++) {
            PhaseMetaData phase = (PhaseMetaData) phases.elementAt(i);
            if (phase.getName().equals(phaseName)) {
                return true;
            }
        }
        return false;
    }

    public PhaseMetaData[] getOrderPhases(PhaseMetaData[] phasesmetadats) {
        PhaseMetaData[] temppahse = new PhaseMetaData[phasesmetadats.length];
        int count = 0;
        for (int i = 0; i < phases.size(); i++) {
            PhaseMetaData phasemetadata = (PhaseMetaData) phases.elementAt(i);
            for (int j = 0; j < phasesmetadats.length; j++) {
                PhaseMetaData tempmetadata = phasesmetadats[j];
                if (tempmetadata.getName().equals(phasemetadata.getName())) {
                    temppahse[count] = tempmetadata;
                    count++;
                }
            }


        }
        return temppahse;
    }

}
