package org.apache.axis.om.storage.table;

import org.apache.axis.om.util.OMConstants;
import junit.framework.TestCase;

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
 * Date: Sep 28, 2004
 * Time: 7:40:32 PM
 */
public class AttributeTableTest extends TestCase{
    private final String LOCAL_NAME= "name";
    private final String VALUE= "value";
    private final int PARENT_KEY =5;
    private int myKey;
    private AttributeTable table;

    protected void setUp() throws Exception {
        super.setUp();
        table = new AttributeTable();
        myKey=table.addAttribute(LOCAL_NAME,VALUE,PARENT_KEY);
    }


    public void testAddAttribute(){
        assertTrue(myKey>=0);
    }

    public void testGetAttribute(){

        String[][] values = table.getAttribute(myKey);

        String[] keyArray = values[0];
        String[] valueArray = values[1];
        for (int j = 0; j < valueArray.length; j++) {
            String key = keyArray[j];
            String value = valueArray[j];

            if (key.equals(OMConstants.VALUE_KEY)){
                assertEquals(value, VALUE);
            }else if (key.equals(OMConstants.LOCAL_NAME_KEY)){
                assertEquals(value, LOCAL_NAME);
            }else if (key.equals(OMConstants.PARENT_ID_KEY)){
                assertEquals(value, PARENT_KEY+"");
            }
        }
    }
}
