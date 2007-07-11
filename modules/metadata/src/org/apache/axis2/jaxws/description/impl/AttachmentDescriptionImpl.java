/*
 * Copyright 2004,2005 The Apache Software Foundation.
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
package org.apache.axis2.jaxws.description.impl;

import org.apache.axis2.jaxws.description.AttachmentType;

public class AttachmentDescriptionImpl implements
        org.apache.axis2.jaxws.description.AttachmentDescription {

    private AttachmentType attachmentType;
    private String[] mimeTypes;
    
    /**
     * @param attachmentType
     * @param mimeTypes
     */
    public AttachmentDescriptionImpl(AttachmentType attachmentType, String[] mimeTypes) {
        this.attachmentType = attachmentType;
        this.mimeTypes = mimeTypes;
    }

    public AttachmentType getAttachmentType() {
        return attachmentType;
    }

    public String[] getMimeTypes() {
        return mimeTypes;
    }
    
    public String toString() {
        final String newline = "\n";
        final String sameline = "; ";
        StringBuffer string = new StringBuffer();
        
        string.append(super.toString());
        string.append(newline);
        string.append("  Attachment Type: " + getAttachmentType());
        //
        string.append(newline);
        string.append("  Mime Types: " + getMimeTypes());
        return string.toString();
    }

}
