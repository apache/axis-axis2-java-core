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
package org.apache.axis.engine;

import org.apache.axis.context.MessageContext;
import org.apache.axis.description.HandlerDescription;
import org.apache.axis.phaseresolver.PhaseException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;

/**
 * <p>This is Phase, a orderd collection of Handlers.
 * seems this is Handler Chain with order.</p>
 * Should this exttends Hanlders?
 */
public class Phase {

    /**
     * Field phaseName
     */
    private String phaseName;

    /**
     * Field handlers
     */
    private ArrayList handlers;

    /**
     * Field log
     */
    private Log log = LogFactory.getLog(getClass());

    private int indexOfHandlerToExecute = 0;


    /**
     * to keet info about phase first handler
     */
    private Handler phaseFirst = null;

    /**
     * Field phasefirstset
     */
    private boolean phasefirstset;

    /**
     * to keet info about phase last handler
     */
    private Handler phaseLast = null;

    /**
     * Field phaselastset
     */
    private boolean phaselastset;

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
     * this is want if the phaseFirst and phaseLst same hanlder
     * that is for this phase there is only one phase
     */
    private boolean isonehanlder;


    /**
     * Constructor Phase
     *
     * @param phaseName
     */
    public Phase(String phaseName) {
        handlers = new ArrayList();
        this.phaseName = phaseName;
    }

    /**
     * Method addHandler
     *
     * @param handler
     * @param index
     */
    public void addHandler(Handler handler, int index) {
        log.info("Handler " + handler.getName() + "Added to place " + 1
                + " At the Phase " + phaseName);
        handlers.add(index, handler);
    }

    /**
     * add to next empty handler
     *
     * @param handler
     */
    public void addHandler(Handler handler) {
        log.info("Handler " + handler.getName() + " Added to the Phase "
                + phaseName);
        handlers.add(handler);
    }

    /**
     * If need to see how this works look at the stack!
     *
     * @param msgctx
     * @throws AxisFault
     */
    public void invoke(MessageContext msgctx) throws AxisFault {
        //If phase first Hnadler is there then it should run first
        if (phaseFirst != null) {
            if (msgctx.isPaused()) {
                return;
            } else {
                log.info("Invoke the Phase first handler " + phaseFirst.getName()
                        + "with in the Phase " + phaseName);
                phaseFirst.invoke(msgctx);
            }
        }
        //Invoking the rest of handler except phaseFirst and phaseLast
        while (indexOfHandlerToExecute < handlers.size()) {
            if (msgctx.isPaused()) {
                break;
            } else {
                Handler handler = (Handler) handlers.get(indexOfHandlerToExecute);
                if (handler != null) {
                    log.info("Invoke the Handler " + handler.getName()
                            + "with in the Phase " + phaseName);
                    handler.invoke(msgctx);
                    //This line should be after the invoke as if the invocation failed this handlers is takn care of and
                    //no need to revoke agien
                    //         executionStack.push(handler);
                    indexOfHandlerToExecute++;
                }
            }
        }
        //If phase last handler is there will invoke that here
        if (phaseLast != null) {
            if (msgctx.isPaused()) {
                return;
            } else {
                log.info("Invoke the Phase first handler " + phaseLast.getName()
                        + "with in the Phase " + phaseName);
                phaseLast.invoke(msgctx);
            }
        }
    }

    /**
     * @return Returns the name.
     */
    public String getPhaseName() {
        return phaseName;
    }


    public int getHandlerCount() {
        return handlers.size();
    }



    //////////////////////////////////////////////////////////////// FROM PhaseMetaData //////////


    /**
     * Method getBeforeAfter
     *
     * @param handler
     * @return
     * @throws org.apache.axis.phaseresolver.PhaseException
     *
     */
    private int getBeforeAfter(Handler handler) throws PhaseException {
        if ((!handler.getHandlerDesc().getRules().getBefore().equals(""))
                && (!handler.getHandlerDesc().getRules().getAfter().equals(""))) {
            if (handler.getHandlerDesc().getRules().getBefore().equals(handler.getHandlerDesc().getRules().getAfter())) {
                throw new PhaseException("Both before and after cannot be the same for this handler"
                        + handler.getName());
            }
            return BOTH_BEFORE_AFTER;
        } else if (!handler.getHandlerDesc().getRules().getBefore().equals("")) {
            return BEFORE;
        } else if (!handler.getHandlerDesc().getRules().getAfter().equals("")) {
            return AFTER;
        } else {
            return ANYWHERE;
        }
    }

    /**
     * Method setPhaseFirst
     *
     * @param phaseFirst
     * @throws PhaseException
     */
    public void setPhaseFirst(Handler phaseFirst)
            throws PhaseException {
        if (phasefirstset) {
            throw new PhaseException("PhaseFirst alredy has been set, cannot have two phaseFirst Handler for same phase "
                    + this.getPhaseName());
        } else {
            if (getBeforeAfter(phaseFirst) != ANYWHERE) {
                throw new PhaseException("Handler with PhaseFirst can not have any before or after proprty error in "
                        + phaseFirst.getName());
            } else {
                this.phaseFirst = phaseFirst;
            }
            phasefirstset = true;
        }
    }


    /**
     * Method setPhaseLast
     *
     * @param phaseLast
     * @throws PhaseException
     */
    public void setPhaseLast(Handler phaseLast) throws PhaseException {
        if (phaselastset) {
            throw new PhaseException("PhaseLast already has been set, cannot have two PhaseLast Handler for same phase "
                    + this.getPhaseName());
        } else {
            if (getBeforeAfter(phaseLast) != ANYWHERE) {
                throw new PhaseException("Handler with PhaseLast property can not have any before or after property error in "
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
    public void addHandler(HandlerDescription handler) throws PhaseException {
        if (isonehanlder) {
            throw new PhaseException(this.getPhaseName() + "can only have one handler, since there is a " +
                    "handler with both phaseFirst and PhaseLast true ");
        } else {
            if (handler.getRules().isPhaseFirst() && handler.getRules().isPhaseLast()) {
                if (handlers.size() > 0) {
                    throw new PhaseException(this.getPhaseName() + " can not have more than one handler "
                            + handler.getName() + " is invalid or incorrect phase rules");
                } else {
                    handlers.add(handler.getHandler());
                    isonehanlder = true;
                }
            } else if (handler.getRules().isPhaseFirst()) {
                setPhaseFirst(handler.getHandler());
            } else if (handler.getRules().isPhaseLast()) {
                setPhaseLast(handler.getHandler());
            } else {
                insertHandler(handler);
            }

        }
    }

    /**
     * This method is to check whether  user try to add a handler whose before property is
     * phaseFitsr handler , this cannot allowed , so this will throws an exception
     * otherewise it will retun
     *
     * @throws PhaseException
     */
    private void validatebefore(Handler handler) throws PhaseException {
        if (phaseFirst != null) {
            String phasFirstname = phaseFirst.getHandlerDesc().getName().getLocalPart();
            if (handler.getHandlerDesc().getRules().getBefore().equals(phasFirstname)) {
                throw new PhaseException("Trying to insert  a Handler "
                        + handler.getName()
                        + " before phaseFirst "
                        + phasFirstname);
            }
        }
    }

    /**
     * This method is to check user try to add or plase a hander after the phaseLast
     * that operation dose not allowd  so then this throw a exception
     *
     * @throws PhaseException
     */
    private void validateafter(Handler handler) throws PhaseException {
        if (phaseLast != null) {
            String phaseLastName = phaseLast.getHandlerDesc().getName().getLocalPart();
            if (handler.getName().equals(phaseLastName)) {
                throw new PhaseException("Try to insert a Handler "
                        + handler.getName()
                        + " after phaseLast "
                        + phaseLastName);
            }
        }
    }

    /**
     * Method insertBefore
     *
     * @param handler
     */
    private void insertBefore(Handler handler) {
        String beforename = handler.getHandlerDesc().getRules().getBefore();
        if (phaseLast != null) {
            if (phaseLast.getHandlerDesc().getName().getLocalPart().equals(beforename)) {
                handlers.add(handler);
                return;
            }
        }
        for (int i = 0; i < handlers.size(); i++) {
            Handler temphandler = (Handler) handlers.get(i);
            if (temphandler.getHandlerDesc().getName().getLocalPart().equals(beforename)) {
                if (i == 0) {
                    handlers.add(0, handler);
                    return;
                } else {
                    handlers.add(i - 1, handler);
                    return;
                }
            }
        }
        //added as last handler
        handlers.add(handler);
    }

    /**
     * Method insertAfter
     *
     * @param handler
     */
    private void insertAfter(Handler handler) {
        String afterName = handler.getHandlerDesc().getRules().getAfter();
        if (phaseLast != null) {
            if (phaseLast.getHandlerDesc().getName().getLocalPart().equals(afterName)) {
                handlers.add(0, handler);
                return;
            }
        }
        int count = handlers.size();
        for (int i = 0; i < count; i++) {
            Handler temphandler = (Handler) handlers.get(i);
            if (temphandler.getHandlerDesc().getName().getLocalPart().equals(afterName)) {
                if (i == count - 1) {
                    handlers.add(handler);
                    return;
                } else {
                    handlers.add(i + 1, handler);
                    return;
                }
            }
        }
        if (handlers.size() > 0) {
            handlers.add(0, handler);
        } else
            handlers.add(handler);
    }

    /**
     * This method assume that both the before and after cant be a same hander
     * that dose not check inside this , it should check befor calling this method
     *
     * @param handler
     * @throws PhaseException
     */
    private void insertBeforeandAfter(Handler handler)
            throws PhaseException {
        int before = -1;
        int after = -1;

        /**
         * if hander.after = PhaseFirts and hnder.before = phaselast then
         * just add the entery to vector
         */
        if ((phaseFirst != null) && (phaseLast != null)) {
            if ((phaseFirst.getHandlerDesc().getName().getLocalPart().equals(
                    handler.getHandlerDesc().getRules().getAfter()))
                    && (phaseLast.getHandlerDesc().getName().getLocalPart().equals(
                            handler.getHandlerDesc().getRules().getBefore()))) {
                handlers.add(handler);
                return;
            }
        }

        if (phaseFirst != null &&
                (phaseFirst.getHandlerDesc().getName().getLocalPart().equals(
                        handler.getHandlerDesc().getRules().getAfter()))) {
            after = 0;
        }
        if (phaseLast != null &&
                (phaseLast.getHandlerDesc().getName().getLocalPart().equals(
                        handler.getHandlerDesc().getRules().getBefore()))) {
            before = handlers.size();
        }

        for (int i = 0; i < handlers.size(); i++) {
            Handler temphandler = (Handler) handlers.get(i);
            if (handler.getHandlerDesc().getRules().getAfter().equals(
                    temphandler.getName().getLocalPart())) {
                after = i;
            } else if (handler.getHandlerDesc().getRules().getBefore().equals(
                    temphandler.getName().getLocalPart())) {
                before = i;
            }
            if ((after >= 0) && (before >= 0)) {
                // no point of continue since both the before and after index has found
                if (after > before) {
                    //TODO fix me Deepal , (have to check this)
                    throw new PhaseException("incorrect handler order for "
                            + handler.getName());
                } else {
                    if (after + 1 <= handlers.size()) {
                        handlers.add(after + 1, handler);
                        return;
                    } else {
                        handlers.add(after, handler);
                        return;
                    }
                }
            }
        }
        handlers.add(handler);
    }
    private void insertHandler(HandlerDescription handler) throws PhaseException {
        int type = getBeforeAfter(handler.getHandler());
        validateafter(handler.getHandler());
        validatebefore(handler.getHandler());
        switch(type){
            case BOTH_BEFORE_AFTER : {
                insertBeforeandAfter(handler.getHandler());
            }
            case BEFORE : {
                insertBefore(handler.getHandler());
            }
            case AFTER : {
                insertAfter(handler.getHandler());
            }
            case ANYWHERE : {
                handlers.add(handler.getHandler());
            }
        }
    }

}
