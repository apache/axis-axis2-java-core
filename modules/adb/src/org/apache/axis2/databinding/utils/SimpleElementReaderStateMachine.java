package org.apache.axis2.databinding.utils;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
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
 * A state machine to read elements with simple content. Returns the text
 * of the element and the stream reader will be one event beyond the
 * end element at return
 */
public class SimpleElementReaderStateMachine implements States{



    private QName elementNameToTest = null;
    private int currentState = INIT_STATE;
    private String text="";

    public String getText() {
        return text;
    }

    public void setElementNameToTest(QName elementNameToTest) {
        this.elementNameToTest = elementNameToTest;
    }

    public void reset(){
        elementNameToTest = null;
        currentState = INIT_STATE;
        text="";
    }
    /**
     * public read method - reads a given reader to extract the text value
     * @param reader
     */
    public void read(XMLStreamReader reader) throws XMLStreamException {

        do{
            updateState(reader);
            if (currentState==TEXT_FOUND_STATE){
                //read the text value and store it
                text = reader.getText();
            }
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


    private void updateState(XMLStreamReader reader){
        int event = reader.getEventType();

        //state 1
        if (event==XMLStreamConstants.START_DOCUMENT && currentState==INIT_STATE){
            currentState = STARTED_STATE;
        }else  if (event==XMLStreamConstants.START_ELEMENT  && currentState==INIT_STATE){
            if (elementNameToTest.equals(reader.getName())){
                currentState = START_ELEMENT_FOUND_STATE;
            }else{
                currentState = STARTED_STATE;
            }
        }else if  (event==XMLStreamConstants.START_ELEMENT  && currentState==STARTED_STATE) {
            if (elementNameToTest.equals(reader.getName())){
                currentState = START_ELEMENT_FOUND_STATE;
            }
        }else if (event==XMLStreamConstants.CHARACTERS && currentState==START_ELEMENT_FOUND_STATE){
            currentState  = TEXT_FOUND_STATE;

            //state 3
        } else if (event==XMLStreamConstants.END_ELEMENT && currentState==TEXT_FOUND_STATE){
            if (elementNameToTest.equals(reader.getName())){
                currentState = END_ELEMENT_FOUND_STATE;
            }else{
                currentState = ILLEGAL_STATE;
            }

            //state 4
        }else if (currentState==END_ELEMENT_FOUND_STATE) {
            currentState = FINISHED_STATE;
        }else{
            currentState = ILLEGAL_STATE;
        }
    }





}
