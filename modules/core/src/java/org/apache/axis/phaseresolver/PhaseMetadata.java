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
package org.apache.axis.phaseresolver;

import org.apache.axis.description.HandlerMetadata;

import java.util.ArrayList;

/**
 * Class PhaseMetadata
 */
public class PhaseMetadata {
    /**
     * Field BOTH_BEFORE_AFTER
     */
    private static final int BOTH_BEFORE_AFTER = 0;

    /**
     * Field BEFORE
     */
    private static final int BEFORE = 1;

    /**
     * Field AFTER
     */
    private static final int AFTER = 2;

    /**
     * Field ANYWHERE
     */
    private static final int ANYWHERE = 3;

    /**
     * Field name
     */
    private String name;

    /**
     * to keet info about phase first handler
     */
    private HandlerMetadata phaseFirst;

    /**
     * Field phasefirstset
     */
    private boolean phasefirstset;

    /**
     * to keet info about phase last handler
     */
    private HandlerMetadata phaseLast;

    /**
     * Field phaselastset
     */
    private boolean phaselastset;

    /**
     * to store and order the handlers
     */
    private ArrayList orderHanders = new ArrayList();

    /**
     * to store HandlerMetaData other than phaseFirst and phseLast
     */
    private ArrayList phaseHandlers = new ArrayList();

    /**
     * this is want if the phaseFirst and phaseLst same hanlder
     * that is for this phase there is only one phase
     */
    private boolean isonehanlder;

    /**
     * Constructor PhaseMetadata
     *
     * @param name
     */
    public PhaseMetadata(String name) {
        this.name = name;
        this.phaseHandlers.clear();
        this.phasefirstset = false;
        this.phaselastset = false;
        this.isonehanlder = false;
    }

    /**
     * Method getPhaseFirst
     *
     * @return
     */
    public HandlerMetadata getPhaseFirst() {
        return phaseFirst;
    }

    /**
     * Method setPhaseFirst
     *
     * @param phaseFirst
     * @throws PhaseException
     */
    public void setPhaseFirst(HandlerMetadata phaseFirst)
            throws PhaseException {
        if (phasefirstset) {
            throw new PhaseException(
                    "PhaseFirst alredy has been set, cannot have two phaseFirst Handler for same phase "
                    + this.name);
        } else {
            if (getBeforeAfter(phaseFirst) != ANYWHERE) {
                throw new PhaseException(
                        "Handler with PhaseFirst can not have any before or after proprty error in "
                        + phaseFirst.getName());
            } else {
                this.phaseFirst = phaseFirst;
            }
            phasefirstset = true;
        }
    }

    /**
     * Method getPhaseLast
     *
     * @return
     */
    public HandlerMetadata getPhaseLast() {
        return phaseLast;
    }

    /**
     * Method setPhaseLast
     *
     * @param phaseLast
     * @throws PhaseException
     */
    public void setPhaseLast(HandlerMetadata phaseLast) throws PhaseException {
        if (phaselastset) {
            throw new PhaseException(
                    "PhaseLast already has been set, cannot have two PhaseLast Handler for same phase "
                    + this.name);
        } else {
            if (getBeforeAfter(phaseLast) != ANYWHERE) {
                throw new PhaseException(
                        "Handler with PhaseLast property can not have any before or after property error in "
                        + phaseLast.getName());
            } else {
                this.phaseLast = phaseLast;
            }
            phaselastset = true;
        }
    }

    /**
     * Method addHandler
     *
     * @param handler
     * @throws PhaseException
     */
    public void addHandler(HandlerMetadata handler) throws PhaseException {
        if (isonehanlder) {
            throw new PhaseException(this.getName() + "can only have one handler, since there is a " +
                    "handler with both phaseFirst and PhaseLast true ");
        } else {
            if (handler.getRules().isPhaseFirst() && handler.getRules().isPhaseLast()) {
                if (phaseHandlers.size() > 0) {
                    throw new PhaseException(this.getName() + " can not have more than one handler "
                            + handler.getName() + " is invalid or incorrect phase rules");
                } else {
                    setPhaseFirst(handler);
                    setPhaseLast(handler);
                    isonehanlder = true;
                }
            } else if (handler.getRules().isPhaseFirst()) {
                setPhaseFirst(handler);
            } else if (handler.getRules().isPhaseLast()) {
                setPhaseLast(handler);
            } else
                phaseHandlers.add(handler);

        }

        //phaseHandlers.add(handler);
    }

    /**
     * Method getName
     *
     * @return
     */
    public String getName() {
        return name;
    }
    /**
     * Method getOrderedHandlers
     *
     * @return
     * @throws PhaseException
     */
    public HandlerMetadata[] getOrderedHandlers() throws PhaseException {
        int size = 0;

        /**
         * order the handlers
         */
        orderHandlers();
        HandlerMetadata handler[];
        if (isonehanlder) {
            size = 1;
            handler = new HandlerMetadata[size];
            handler[0] = getPhaseFirst();
            return handler;
        }
        if (phasefirstset) {
            if (phaseHandlers.size() > 0) {
                phaseHandlers.add(0, getPhaseFirst());
            } else {
                phaseHandlers.add(getPhaseFirst());
            }
        }
        if (phaselastset) {
            phaseHandlers.add(getPhaseLast());
        }
        size = phaseHandlers.size();
        handler = new HandlerMetadata[size];
        for (int i = 0; i < phaseHandlers.size(); i++) {
            handler[i] = (HandlerMetadata) phaseHandlers.get(i);
        }
        return handler;
    }

    /**
     * Method orderHandlers
     *
     * @throws PhaseException
     */
    private void orderHandlers() throws PhaseException {
        validatebefore();
        validateafter();
        arrangeHanders();
        phaseHandlers.clear();
        phaseHandlers = orderHanders;
    }

    /**
     * This method is to check whether  user try to add a handler whose before property is
     * phaseFitsr handler , this cannot allowed , so this will throws an exception
     * otherewise it will retun
     *
     * @throws PhaseException
     */
    private void validatebefore() throws PhaseException {
        if (getPhaseFirst() != null) {
            String phasFirstname = getPhaseFirst().getName().getLocalPart();
            for (int i = 0; i < phaseHandlers.size(); i++) {
                HandlerMetadata handler =
                        (HandlerMetadata) phaseHandlers.get(i);
                if (handler.getRules().getBefore().equals(phasFirstname)) {
                    throw new PhaseException("Try to insert  a Handler "
                            + handler.getName()
                            + " before phaseFirst "
                            + phasFirstname);
                }
            }
        } else {
            return;
        }
    }

    /**
     * This method is to check user try to add or plase a hander after the phaseLast
     * that operation dose not allowd  so then this throw a exception
     *
     * @throws PhaseException
     */
    private void validateafter() throws PhaseException {
        if (getPhaseLast() != null) {
            String phaseLastName = getPhaseLast().getName().getLocalPart();
            for (int i = 0; i < phaseHandlers.size(); i++) {
                HandlerMetadata handler =
                        (HandlerMetadata) phaseHandlers.get(i);
                if (handler.getName().equals(phaseLastName)) {
                    throw new PhaseException("Try to insert a Handler "
                            + handler.getName()
                            + " after phaseLast "
                            + phaseLastName);
                }
            }
        }
    }

    /**
     * Method arrangeHanders
     *
     * @throws PhaseException
     */
    private void arrangeHanders() throws PhaseException {
        int count = 0;
        int before_after = 0;
        boolean status = false;
        HandlerMetadata handler = null;
        while (phaseHandlers.size() > 0) {
            if (status) {
                handler = (HandlerMetadata) phaseHandlers.get(0);
            } else {
                handler = (HandlerMetadata) phaseHandlers.get(count);
            }
            status = false;
            if (count > phaseHandlers.size()) {
                throw new PhaseException("Invalid pahse rule for "  + handler.getName());
            }
            before_after = getBeforeAfter(handler);
            switch (before_after) {
                case ANYWHERE:
                    {
                        orderHanders.add(handler);
                        phaseHandlers.remove(handler);
                        count = 0;
                        status = true;
                        break;
                    }
                case BEFORE:
                    {
                        status = insertBefore(handler);
                        if (status) {
                            phaseHandlers.remove(handler);
                            count = 0;
                        }
                        break;
                    }
                case AFTER:
                    {
                        status = insertAfter(handler);
                        if (status) {
                            phaseHandlers.remove(handler);
                            count = 0;
                        }
                        break;
                    }
                case BOTH_BEFORE_AFTER:
                    {
                        status = insertBeforeandAfter(handler);
                        if (status) {
                            phaseHandlers.remove(handler);
                            count = 0;
                        }
                        break;
                    }
            }
            if(! status){
                count++;
            }
        }
    }

    /**
     * Method getBeforeAfter
     *
     * @param handler
     * @return
     * @throws PhaseException
     */
    private int getBeforeAfter(HandlerMetadata handler) throws PhaseException {
        if ((!handler.getRules().getBefore().equals(""))
                && (!handler.getRules().getAfter().equals(""))) {
            if (handler.getRules().getBefore().equals(
                    handler.getRules().getAfter())) {
                throw new PhaseException(
                        "Both before and after cannot be the same for this handler"
                        + handler.getName());
            }
            return BOTH_BEFORE_AFTER;
        } else if (!handler.getRules().getBefore().equals("")) {
            return BEFORE;
        } else if (!handler.getRules().getAfter().equals("")) {
            return AFTER;
        } else {
            return ANYWHERE;
        }
    }

    /**
     * Method insertBefore
     *
     * @param handler
     * @return
     */
    private boolean insertBefore(HandlerMetadata handler) {
        String beforename = handler.getRules().getBefore();
        if (getPhaseLast() != null) {
            if (getPhaseLast().getName().getLocalPart().equals(beforename)) {
                orderHanders.add(handler);
                return true;
            }
        }
        for (int i = 0; i < orderHanders.size(); i++) {
            HandlerMetadata temphandler = (HandlerMetadata) orderHanders.get(i);
            if (temphandler.getName().getLocalPart().equals(beforename)) {
                orderHanders.add(i, handler);
                return true;
            }
        }
        return false;
    }

    /**
     * Method insertAfter
     *
     * @param handler
     * @return
     */
    private boolean insertAfter(HandlerMetadata handler) {
        String afterName = handler.getRules().getAfter();
        if (getPhaseFirst() != null) {
            if (getPhaseFirst().getName().getLocalPart().equals(afterName)) {
                orderHanders.add(0, handler);
                return true;
            }
        }
        for (int i = 0; i < orderHanders.size(); i++) {
            HandlerMetadata temphandler = (HandlerMetadata) orderHanders.get(i);
            if (temphandler.getName().getLocalPart().equals(afterName)) {
                if (i == orderHanders.size() - 1) {
                    orderHanders.add(handler);
                    return true;
                } else {
                    orderHanders.add(i + 1, handler);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * This method assume that both the before and after cant be a same hander
     * that dose not check inside this , it should check befor calling this method
     *
     * @param handler
     * @return
     * @throws PhaseException
     */
    private boolean insertBeforeandAfter(HandlerMetadata handler)
            throws PhaseException {
        int before = -1;
        int after = -1;

        /**
         * if hander.after = PhaseFirts and hnder.before = phaselast then
         * just add the entery to vector
         */
        if ((getPhaseFirst() != null) && (getPhaseLast() != null)) {
            if ((getPhaseFirst().getName().getLocalPart().equals(
                    handler.getRules().getAfter()))
                    && (getPhaseLast().getName().getLocalPart().equals(
                            handler.getRules().getBefore()))) {
                orderHanders.add(handler);
                return true;
            }
        }

        if(getPhaseFirst() != null &&
                (getPhaseFirst().getName().getLocalPart().equals(handler.getRules().getAfter()))){
            after = 0;
        }
        if(getPhaseLast() != null &&
                (getPhaseLast().getName().getLocalPart().equals(handler.getRules().getBefore()))){
            before = orderHanders.size();
        }

        for (int i = 0; i < orderHanders.size(); i++) {
            HandlerMetadata temphandler = (HandlerMetadata) orderHanders.get(i);
            if (handler.getRules().getAfter().equals(temphandler.getName().getLocalPart())) {
                after = i;
            } else if (handler.getRules().getBefore().equals(
                    temphandler.getName())) {
                before = i;
            }
            if ((after >= 0) && (before >= 0)) {
                // no point of continue since both the before and after index has found
                if (after > before) {
                    throw new PhaseException("incorrect handler order for "
                            + handler.getName());
                } else {
                    if(after + 1 <= orderHanders.size()){
                        orderHanders.add(after + 1, handler);
                    } else
                        orderHanders.add(after, handler);
                    return true;
                }
            }
        }
        return false;
    }
}
