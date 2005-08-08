/*
 * Copyright  2004 The Apache Software Foundation.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.apache.axis2.attachments;

import javax.activation.DataHandler;
import javax.mail.MessagingException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;

/**
 * @author <a href="mailto:thilina@opensource.lk"> Thilina Gunarathne </a>
 */
public interface Part{

    
    public int getSize() throws MessagingException ;

    public String getContentType() throws MessagingException;
    
    public String getContentID() throws MessagingException;
    
    public String getFileName() throws MessagingException;

    public InputStream getInputStream() throws IOException, MessagingException ;
    
    public DataHandler getDataHandler() throws MessagingException ;
    
    public String getHeader(String arg0) throws MessagingException ;
    
    public Enumeration getAllHeaders() throws MessagingException ;
    
}
