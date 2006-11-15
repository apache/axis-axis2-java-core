/*
 * Copyright 2006 The Apache Software Foundation.
 * Copyright 2006 International Business Machines Corp.
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
package org.apache.axis2.jaxws.message;

import javax.activation.DataHandler;

/**
 * Attachment
 * 
 * Used for attaching documents to a Message.  Each Attachment must have a 
 * uniquie identifier or "contentID".  The actual content of the attachment
 * is stored in a <link>javax.activation.DataHandler</link>.
 */
public interface Attachment {
    
    /**
     * Gets the MIME type for the content of the attachment.
     * @return contentType
     */
    public String getContentType();
    
    /**
     * Gets the contendID that identifies this attachment.
     * @return contentID
     */
    public String getContentID();
    
    /**
     * Gets the actual content of the attachment in a DataHandler form.
     * @return content
     */
    public DataHandler getDataHandler();
    
}
