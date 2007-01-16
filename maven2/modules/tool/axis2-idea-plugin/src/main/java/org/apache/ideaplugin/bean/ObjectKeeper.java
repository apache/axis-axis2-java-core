package org.apache.ideaplugin.bean;

import javax.swing.*;
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
*
*
*/

/**
 * Author: Deepal Jayasinghe
 * Date: Sep 17, 2005
 * Time: 11:40:12 PM
 */

//to fill the bean
public interface ObjectKeeper {

    void fillBean(ArchiveBean bean);

    //to keep a refernce to next panel
    void setNext(JPanel next);

    JPanel getNext();

    //to keep a refernce to previous panel
    void setPrivious(JPanel privious);

    JPanel getPrivious();

    String getTopLable();

    String getLable();

}
