package org.apache.axis.deployment.phaserule;

import org.apache.axis.deployment.util.Handler;

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
 *         Nov 8, 2004
 *         1:57:15 PM
 *
 */
public class Phase {

    private final int BOTH_BEFORE_AFTER = 0;
    private final int BEORE = 1;
    private final int AFTER = 2;
    private final int ANYWHERE = 3;


    private String name ;

    /**
     * to keet info about phase first handler
     */
    private Handler phaseFirst;
    private boolean phasefirstset;

    /**
     * to keet info about phase last handler
     */
    private Handler phaseLast;
    private boolean phaselastset;

    /**
     * to store and order the handlers
     */
    private Vector orderHanders = new Vector();

    /**
     * to store Handler other than phaseFirst and phseLast
     */
    private Vector phaseHandlers = new Vector();

    /**
     * this is want if the phaseFirst and phaseLst same hanlder
     * that is for this phase there is only one phase
     */
    private boolean isonehanlder  ;

    public Phase(String name) {
        this.name = name;
        this.phaseHandlers.removeAllElements();
        this.phasefirstset = false;
        this.phaselastset = false;
        this.isonehanlder = false;
    }

    public Handler getPhaseFirst() {
        return phaseFirst;
    }

    public void setPhaseFirst(Handler phaseFirst) throws PhaseException {
        if(phasefirstset){
            throw new PhaseException("PhaseFirst alredy has been set, cannot have two phaseFirst Hander for same phase " + this.name);
        } else {
            this.phaseFirst = phaseFirst;
            phasefirstset = true;
        }
    }

    public Handler getPhaseLast() {
        return phaseLast;
    }

    public void setPhaseLast(Handler phaseLast) throws PhaseException{
        if(phaselastset){
            throw new PhaseException("PhaseLast alredy has been set, cannot have two PhaseLast Hander for same phase " + this.name );
        } else {
            this.phaseLast = phaseLast;
            phaselastset = true;
        }
    }

    public void addHandler(Handler handler) throws PhaseException{
        if(isonehanlder){
            throw new PhaseException( this.getName() + "can only have one handler, since there is a handler with both phaseFirst and PhaseLast true ");
        } else {
            if (handler.isPhaseFirst() && handler.isPhaseLast()){
                if(phaseHandlers.size() > 0){
                    throw new PhaseException(this.getName() + " Phase already added a hander so this operation not allowed  cannot add the handler " + handler.getName());
                } else {
                    setPhaseFirst(handler);
                    setPhaseLast(handler);
                    isonehanlder = true;
                }
            } else if (handler.isPhaseFirst()){
                setPhaseFirst(handler);
            } else if (handler.isPhaseLast()){
                setPhaseLast(handler);
            } else
                phaseHandlers.add(handler);

        }

    }

    public String getName() {
        return name;
    }

    public int getBeforeIndex(String beforeName){
        return 0;
    }

    public int getAfterIndex(String afterName){
        return 0;
    }

    public void listOrderdHandlers(){
        if(isonehanlder){
            getPhaseFirst().printMe();
            return ;
        }

        Handler handler = getPhaseFirst();
        if(handler != null){
            handler.printMe();
        }
        for (int i = 0; i < phaseHandlers.size(); i++) {
            handler = (Handler) phaseHandlers.elementAt(i);
            handler.printMe();
        }
        handler = getPhaseLast();
        if(handler != null){
            handler.printMe();
        }
    }

    public Handler[] getOrderedHandlers() throws PhaseException{
        int size = 0;
        /**
         * order the handlers
         */
        orderHandlers();


        Handler handler [];
        if(isonehanlder){
            size = 1;
            handler = new Handler[size];
            handler[0] = getPhaseFirst();
            return handler;
        }
        if (phasefirstset){
            if(phaseHandlers.size() > 0){
                phaseHandlers.add(0,getPhaseFirst());
            } else
                phaseHandlers.add(getPhaseFirst());
        }
        if(phaselastset){
            phaseHandlers.add(getPhaseLast());
        }
        size = phaseHandlers.size();
        handler = new Handler[size];

        for (int i = 0; i < phaseHandlers.size(); i++) {
            handler[i] = (Handler) phaseHandlers.elementAt(i);
        }
        return handler;
    }


    private void orderHandlers() throws PhaseException{
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
     * @throws PhaseException
     */
    private void validatebefore() throws PhaseException {
        if(getPhaseFirst() != null){
            String phasFirstname = getPhaseFirst().getName();
            for (int i = 0; i < phaseHandlers.size(); i++) {
                Handler handler = (Handler) phaseHandlers.elementAt(i);
                if(handler.getBefore().equals(phasFirstname)){
                    throw new PhaseException("Try to plase a Hander " + handler.getName() + " before phaseFirst " + phasFirstname);
                }
            }
        }   else
            return ;
    }


    /**
     * This method is to check user try to add or plase a hander after the phaseLast
     * that operation dose not allowd  so then this throw a exception
     * @throws PhaseException
     */
    private void validateafter() throws PhaseException {
        if (getPhaseLast() != null){
            String phaseLastName = getPhaseLast().getName();
            for (int i = 0; i < phaseHandlers.size(); i++) {
                Handler handler = (Handler) phaseHandlers.elementAt(i);
                if(handler.getName().equals(phaseLastName)){
                    throw new PhaseException("Try to plase a Hander " + handler.getName() + " after phaseLast " + phaseLastName);
                }
            }
        }   else
            return ;
    }

    private void arrangeHanders()throws PhaseException{
        int count =0;
        int before_after = 0;
        boolean status = false;
        Handler handler = null;
        while(phaseHandlers.size() > 0){
            if(status ){
                handler = (Handler)phaseHandlers.firstElement();
            } else
                handler = (Handler)phaseHandlers.get(count);

            status = false;
            if(count > phaseHandlers.size()){
                throw new PhaseException("Incorrect hander order for " + handler.getName());
            }
            before_after = getBefoerAfter(handler);
            switch(before_after){
                case ANYWHERE :{
                    orderHanders.add(handler);
                    phaseHandlers.removeElement(handler);
                    count = 0;
                    status = true;
                    break;
                }
                case BEORE : {
                    status = insertBefore(handler);
                    if(status){
                        phaseHandlers.removeElement(handler);
                       count = 0;
                    }
                    break;
                }
                case AFTER :{
                    status = insertAfter(handler);
                    if(status){
                        phaseHandlers.removeElement(handler);
                        count = 0;
                    }
                    break;
                }
                case  BOTH_BEFORE_AFTER :{
                    status = insertBeforeandAfter(handler);
                    if(status){
                        phaseHandlers.removeElement(handler);
                        count = 0;
                    }
                    break;
                }
            }
            count ++;
        }
    }

    private int getBefoerAfter(Handler handler)throws PhaseException{
        if((! handler.getBefore().equals("")) && (! handler.getAfter().equals(""))){
            if(handler.getBefore().equals(handler.getAfter())){
                throw new PhaseException("Both before and after cannot be the same for this handler" + handler.getName());
            }
            return BOTH_BEFORE_AFTER;
        } else if (! handler.getBefore().equals("")){
            return BEORE;
        } else if (! handler.getAfter().equals("")){
            return AFTER;
        } else
            return ANYWHERE;
    }

    private boolean insertBefore(Handler handler){
        String beforename = handler.getBefore();
        if(getPhaseLast() != null){
            if (getPhaseLast().getName().equals(beforename)){
                orderHanders.add(handler);
                return true;
            }
        }
        for (int i = 0; i < orderHanders.size(); i++) {
            Handler temphandler = (Handler) orderHanders.elementAt(i);
            if(temphandler.getName().equals(beforename)){
                orderHanders.add(i,handler);
                return true;
            }
        }
        return false;
    }

    private boolean insertAfter(Handler handler){
        String afterName = handler.getAfter();
        if(getPhaseFirst() != null){
            if(getPhaseFirst().getName().equals(afterName)){
                orderHanders.add(0,handler);
                return true;
            }
        }
        for (int i = 0; i < orderHanders.size(); i++) {
            Handler temphandler = (Handler) orderHanders.elementAt(i);
            if(temphandler.getName().equals(afterName)){
                if(i == orderHanders.size() -1){
                    orderHanders.add(handler);
                    return true;
                }else {
                    orderHanders.add(i+1,handler);
                    return true;
                }
            }

        }
        return false;
    }

    /**
     * This method assume that both the before and after cant be a same hander
     * that dose not check inside this , it should check befor calling this method
     * @param handler
     * @return
     */
    private boolean insertBeforeandAfter(Handler handler)throws PhaseException{
        int before = -1;
        int after = -1;
        /**
         * if hander.after = PhaseFirts and hnder.before = phaselast then
         * just add the entery to vector
         */
        if((getPhaseFirst() != null) && (getPhaseLast() != null)){
            if((getPhaseFirst().getName().equals(handler.getAfter()))&&(getPhaseLast().getName().equals(handler.getBefore()))){
                orderHanders.add(handler);
                return true;
            }
        }

        for (int i = 0; i < orderHanders.size(); i++) {
            Handler temphandler = (Handler) orderHanders.elementAt(i);
            if(handler.getAfter().equals(temphandler.getName())){
                after = i;
            } else if(handler.getBefore().equals(temphandler.getName())){
                before = i;
            }
            if((after >= 0)&& (before >= 0)){
                // no point of continue since both the before and after index has found
                if(after > before){
                    throw new PhaseException("incorrect handler order for " + handler.getName());
                } else {
                    orderHanders.add(after + 1 ,handler);
                    return true;
                }
            }
        }
        return false;
    }


}
