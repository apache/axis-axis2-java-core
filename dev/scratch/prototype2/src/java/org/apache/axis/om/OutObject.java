package org.apache.axis.om;

import org.xml.sax.ContentHandler;

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
 * <p/>
 */

/**
 * From the point of view of AXIOM, we expect something from the OUT object provided by the provider.
 * Especially AXIOM expects a SAX interface from the Object AXIOM recieves.
 * <p/>
 * So this interface has to be implemented by the Object which AXIOM recieves for OUT path.
 */
public interface OutObject {
    /**
     * This method will help to register a ContentHandler with the Object
     *
     * @param contentHandler
     */
    public void setContentHandler(ContentHandler contentHandler);

    /**
     * @return
     */

    public ContentHandler getContentHandler();

    /**
     * When this method is being called the Object should start throwing SAX events through the
     * ContentHandler registered earlier.
     */
    public void startBuilding()throws OMException ;
}
