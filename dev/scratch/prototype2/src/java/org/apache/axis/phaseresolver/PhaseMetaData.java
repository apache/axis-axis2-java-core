package org.apache.axis.phaseresolver;

import java.util.Vector;

import org.apache.axis.description.HandlerMetaData;


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
 */
public class PhaseMetaData {


    private final int BOTH_BEFORE_AFTER = 0;
    private final int BEORE = 1;
    private final int AFTER = 2;
    private final int ANYWHERE = 3;


    private String name;

    /**
     * to keet info about phase first handler
     */
    private HandlerMetaData phaseFirst;
    private boolean phasefirstset;

    /**
     * to keet info about phase last handler
     */
    private HandlerMetaData phaseLast;
    private boolean phaselastset;

    /**
     * to store and order the handlers
     */
    private Vector orderHanders = new Vector();

    /**
     * to store HandlerMetaData other than phaseFirst and phseLast
     */
    private Vector phaseHandlers = new Vector();

    /**
     * this is want if the phaseFirst and phaseLst same hanlder
     * that is for this phase there is only one phase
     */
    private boolean isonehanlder;

    public PhaseMetaData(String name) {
        this.name = name;
        this.phaseHandlers.removeAllElements();
        this.phasefirstset = false;
        this.phaselastset = false;
        this.isonehanlder = false;
    }

    public HandlerMetaData getPhaseFirst() {
        return phaseFirst;
    }

    public void setPhaseFirst(HandlerMetaData phaseFirst) throws PhaseException {
        if (phasefirstset) {
            throw new PhaseException("PhaseFirst alredy has been set, cannot have two phaseFirst Hander for same phase " + this.name);
        } else {
            if (getBefoerAfter(phaseFirst) != ANYWHERE) {
                throw new PhaseException("Hander with PhaseFirst can not have any before or after proprty error in " + phaseFirst.getName());
            } else
                this.phaseFirst = phaseFirst;
            phasefirstset = true;
        }
    }

    public HandlerMetaData getPhaseLast() {
        return phaseLast;
    }

    public void setPhaseLast(HandlerMetaData phaseLast) throws PhaseException {
        if (phaselastset) {
            throw new PhaseException("PhaseLast alredy has been set, cannot have two PhaseLast Hander for same phase " + this.name);
        } else {
            if (getBefoerAfter(phaseLast) != ANYWHERE) {
                throw new PhaseException("Hander with PhaseLast property can not have any before or after proprty error in " + phaseLast.getName());
            } else
                this.phaseLast = phaseLast;
            phaselastset = true;
        }
    }

    public void addHandler(HandlerMetaData handler) throws PhaseException {
        /**
         * for the M1 we are not going to care about phaseFirst , PhaseLast only thinh u can do it
         * insert it to a pahse.
         * todo if you uncomment this will work fine for phase Conditions :)
         */
        /*
        if (isonehanlder) {
            throw new PhaseException(this.getName() + "can only have one handler, since there is a handler with both phaseFirst and PhaseLast true ");
        } else {
            if (handler.getRules().isPhaseFirst() && handler.getRules().isPhaseLast()) {
                if (phaseHandlers.size() > 0) {
                    throw new PhaseException(this.getName() + " PhaseMetaData already added a hander so this operation not allowed  cannot add the handler " + handler.getName());
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
        */
         phaseHandlers.add(handler);

    }

    public String getName() {
        return name;
    }

    public int getBeforeIndex(String beforeName) {
        return 0;
    }

    public int getAfterIndex(String afterName) {
        return 0;
    }

    public HandlerMetaData[] getOrderedHandlers() throws PhaseException {
        int size = 0;
        /**
         * order the handlers
         */
        orderHandlers();


        HandlerMetaData handler [];
        if (isonehanlder) {
            size = 1;
            handler = new HandlerMetaData[size];
            handler[0] = getPhaseFirst();
            return handler;
        }
        if (phasefirstset) {
            if (phaseHandlers.size() > 0) {
                phaseHandlers.add(0, getPhaseFirst());
            } else
                phaseHandlers.add(getPhaseFirst());
        }
        if (phaselastset) {
            phaseHandlers.add(getPhaseLast());
        }
        size = phaseHandlers.size();
        handler = new HandlerMetaData[size];

        for (int i = 0; i < phaseHandlers.size(); i++) {
            handler[i] = (HandlerMetaData) phaseHandlers.elementAt(i);
        }
        return handler;
    }


    private void orderHandlers() throws PhaseException {
        validatebefore();
        validateafter();
        arrangeHanders();
        phaseHandlers.removeAllElements();
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
                HandlerMetaData handler = (HandlerMetaData) phaseHandlers.elementAt(i);
                if (handler.getRules().getBefore().equals(phasFirstname)) {
                    throw new PhaseException("Try to plase a Hander " + handler.getName() + " before phaseFirst " + phasFirstname);
                }
            }
        } else
            return;
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
                HandlerMetaData handler = (HandlerMetaData) phaseHandlers.elementAt(i);
                if (handler.getName().equals(phaseLastName)) {
                    throw new PhaseException("Try to plase a Hander " + handler.getName() + " after phaseLast " + phaseLastName);
                }
            }
        } else
            return;
    }

    private void arrangeHanders() throws PhaseException {
        int count = 0;
        int before_after = 0;
        boolean status = false;
        HandlerMetaData handler = null;
        while (phaseHandlers.size() > 0) {
            if (status) {
                handler = (HandlerMetaData) phaseHandlers.firstElement();
            } else
                handler = (HandlerMetaData) phaseHandlers.get(count);

            status = false;
            if (count > phaseHandlers.size()) {
                throw new PhaseException("Incorrect hander order for " + handler.getName());
            }
            before_after = getBefoerAfter(handler);
            switch (before_after) {
                case ANYWHERE:
                    {
                        orderHanders.add(handler);
                        phaseHandlers.removeElement(handler);
                        count = 0;
                        status = true;
                        break;
                    }
                case BEORE:
                    {
                        status = insertBefore(handler);
                        if (status) {
                            phaseHandlers.removeElement(handler);
                            count = 0;
                        }
                        break;
                    }
                case AFTER:
                    {
                        status = insertAfter(handler);
                        if (status) {
                            phaseHandlers.removeElement(handler);
                            count = 0;
                        }
                        break;
                    }
                case BOTH_BEFORE_AFTER:
                    {
                        status = insertBeforeandAfter(handler);
                        if (status) {
                            phaseHandlers.removeElement(handler);
                            count = 0;
                        }
                        break;
                    }
            }
            count++;
        }
    }

    private int getBefoerAfter(HandlerMetaData handler) throws PhaseException {
        if ((!handler.getRules().getBefore().equals("")) && (!handler.getRules().getAfter().equals(""))) {
            if (handler.getRules().getBefore().equals(handler.getRules().getAfter())) {
                throw new PhaseException("Both before and after cannot be the same for this handler" + handler.getName());
            }
            return BOTH_BEFORE_AFTER;
        } else if (!handler.getRules().getBefore().equals("")) {
            return BEORE;
        } else if (!handler.getRules().getAfter().equals("")) {
            return AFTER;
        } else
            return ANYWHERE;
    }

    private boolean insertBefore(HandlerMetaData handler) {
        String beforename = handler.getRules().getBefore();
        if (getPhaseLast() != null) {
            if (getPhaseLast().getName().equals(beforename)) {
                orderHanders.add(handler);
                return true;
            }
        }
        for (int i = 0; i < orderHanders.size(); i++) {
            HandlerMetaData temphandler = (HandlerMetaData) orderHanders.elementAt(i);
            if (temphandler.getName().equals(beforename)) {
                orderHanders.add(i, handler);
                return true;
            }
        }
        return false;
    }

    private boolean insertAfter(HandlerMetaData handler) {
        String afterName = handler.getRules().getAfter();
        if (getPhaseFirst() != null) {
            if (getPhaseFirst().getName().equals(afterName)) {
                orderHanders.add(0, handler);
                return true;
            }
        }
        for (int i = 0; i < orderHanders.size(); i++) {
            HandlerMetaData temphandler = (HandlerMetaData) orderHanders.elementAt(i);
            if (temphandler.getName().equals(afterName)) {
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
     */
    private boolean insertBeforeandAfter(HandlerMetaData handler) throws PhaseException {
        int before = -1;
        int after = -1;
        /**
         * if hander.after = PhaseFirts and hnder.before = phaselast then
         * just add the entery to vector
         */
        if ((getPhaseFirst() != null) && (getPhaseLast() != null)) {
            if ((getPhaseFirst().getName().equals(handler.getRules().getAfter())) &&
                    (getPhaseLast().getName().equals(handler.getRules().getBefore()))) {
                orderHanders.add(handler);
                return true;
            }
        }

        for (int i = 0; i < orderHanders.size(); i++) {
            HandlerMetaData temphandler = (HandlerMetaData) orderHanders.elementAt(i);
            if (handler.getRules().getAfter().equals(temphandler.getName())) {
                after = i;
            } else if (handler.getRules().getBefore().equals(temphandler.getName())) {
                before = i;
            }
            if ((after >= 0) && (before >= 0)) {
                // no point of continue since both the before and after index has found
                if (after > before) {
                    throw new PhaseException("incorrect handler order for " + handler.getName());
                } else {
                    orderHanders.add(after + 1, handler);
                    return true;
                }
            }
        }
        return false;
    }


}
