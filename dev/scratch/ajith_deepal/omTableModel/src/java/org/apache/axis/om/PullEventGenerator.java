package org.apache.axis.om;

import org.apache.axis.om.util.OMConstants;
import org.xmlpull.v1.XmlPullParser;

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
 * @author Axis team
 * Date: Sep 30, 2004
 * Time: 9:41:22 PM
 *
 * Generates the pull events from the model
 * Still preliminary
 */
//todo make this implement the Stax APi and make it a compelte pull parser like thing
public class PullEventGenerator  {

    private OMModel model;
    private int currentCount = 0;
    private XmlPullParser parser;

    /**
     *
     * @param model
     */
    public PullEventGenerator(OMModel model) {
        this.model = model;
    }

    /**
     *
     * @return an integer that represents the pull event
     */
    public int generatePullEvent(){
        try {
            int size = model.getEventCount();
            if (size==currentCount){
                //set the caching to off
                model.setCacheOff();
                parser = model.getParser();
                currentCount ++;
                return parser.next();
            }else if (currentCount>size){
                 currentCount ++;
                return parser.next();
            }else if (currentCount<size){
                return model.getPullEvent(currentCount++);
            }
        } catch (Exception e) {
            throw new OMException(e);
        }
        return OMConstants.DEFAULT_INT_VALUE;
    }

}
