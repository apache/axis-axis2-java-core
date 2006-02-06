package org.apache.axis2.databinding.utils;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamConstants;
import java.util.List;
import java.util.ArrayList;
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

/**
 * A state machine that reads arrays with simple content. returns a string array
 */
public class SimpleArrayReaderStateMachine implements States,Constants {

    private QName elementNameToTest = null;
    private int currentState = INIT_STATE;
    private boolean nillable = false;
    private List list = new ArrayList();

    /**
     * @return an array of strings
     */
    public String[] getTextArray() {
        return (String[])list.toArray(new String[list.size()]);
    }


    public void setNillable(){
       nillable = true;
    }

    /**
     * Resets the state machine. Once the reset is called
     * the state machine is good enough for a fresh run
     */
    public void reset(){
        elementNameToTest = null;
        currentState = INIT_STATE;
        nillable = false;
        list=new ArrayList();
    }

    public void setElementNameToTest(QName elementNameToTest) {
        this.elementNameToTest = elementNameToTest;
    }

    /**
     * public read method - reads a given reader to extract the text value
     * @param reader
     */
    public void read(XMLStreamReader reader) throws XMLStreamException {

        do{
            updateState(reader);

            //test for the nillable attribute
            if (currentState==START_ELEMENT_FOUND_STATE &&
                     nillable){
               if (TRUE.equals(reader.getAttributeValue("",NIL))){
                   list.add(null);
                   //force the state to be null found
                   currentState= NULLED_STATE;
               }
            }

            if (currentState==TEXT_FOUND_STATE){
                //read the text value and store it in the list
                list.add(reader.getText());
            }
            //increment the parser only if the  state is
            //not finished
             if (currentState!=FINISHED_STATE
                && currentState!= ILLEGAL_STATE){
               reader.next();
            }
          
        }while(currentState!=FINISHED_STATE
                && currentState!= ILLEGAL_STATE);

        if (currentState==ILLEGAL_STATE){
            throw new RuntimeException("Illegal state!");
        }

    }





    private void updateState(XMLStreamReader reader) throws XMLStreamException{
        int event = reader.getEventType();

        //Starting state
        if (event== XMLStreamConstants.START_DOCUMENT && currentState==INIT_STATE){
            currentState = STARTED_STATE;

        //start element found at init
        }else  if (event==XMLStreamConstants.START_ELEMENT  && currentState==INIT_STATE){
            if (elementNameToTest.equals(reader.getName())){
                currentState = START_ELEMENT_FOUND_STATE;
            }else{
                currentState = STARTED_STATE;
            }
        //start element found after starting
        }else if  (event==XMLStreamConstants.START_ELEMENT  && currentState==STARTED_STATE) {
            if (elementNameToTest.equals(reader.getName())){
                currentState = START_ELEMENT_FOUND_STATE;
            }
        //characters found after start
        }else if (event==XMLStreamConstants.CHARACTERS && currentState==START_ELEMENT_FOUND_STATE){
            currentState  = TEXT_FOUND_STATE;

         //end element found after characters
        } else if (event==XMLStreamConstants.END_ELEMENT && currentState==TEXT_FOUND_STATE){
            if (elementNameToTest.equals(reader.getName())){
                currentState = END_ELEMENT_FOUND_STATE;
            }else{
                currentState = ILLEGAL_STATE;
            }

        //another start element found after end-element
        }else if (event==XMLStreamConstants.START_ELEMENT && currentState==END_ELEMENT_FOUND_STATE ) {
            if (elementNameToTest.equals(reader.getName())){
                currentState = START_ELEMENT_FOUND_STATE;
            }else{
                currentState = FINISHED_STATE;
            }
        //another end element found after end-element
        }else if (event==XMLStreamConstants.END_ELEMENT && currentState==END_ELEMENT_FOUND_STATE ) {
            currentState = FINISHED_STATE;
         //end  document found
        }else if (event==XMLStreamConstants.END_DOCUMENT){
            currentState = FINISHED_STATE;

        //the element was found to be null and this state was forced.
        //we are sure here that the parser was at the START_ELEMENT_FOUND_STATE before
        //being forced. Hence we need to advance the parser upto the end element and
        //set the state to be end element found
        }else if (currentState==NULLED_STATE){
           while (event!= XMLStreamConstants.END_ELEMENT){
               event=reader.next();
           }
           currentState = END_ELEMENT_FOUND_STATE;
         //all other combinations are invalid
        }else{
            currentState = ILLEGAL_STATE;
        }
    }


}
