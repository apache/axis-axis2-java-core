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

package org.apache.axis2.databinding.deserializers;

import org.apache.axis2.databinding.Deserializer;
import org.apache.axis2.databinding.DeserializationContext;
import org.apache.axis2.databinding.DeserializationTarget;

import javax.xml.stream.XMLStreamReader;

/**
 * CollectionDeserializer
 */
public class CollectionDeserializer implements Deserializer {
    Deserializer itemDeserializer;

    public void deserialize(XMLStreamReader reader, DeserializationContext context) throws Exception {
        // strip the wrapper element.
        reader.next();

        // Now deserialize each inner element
        itemDeserializer.deserialize(reader, context);
    }

    public void setTarget(DeserializationTarget target) {
    }
}
