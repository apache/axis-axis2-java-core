/*
 * Copyright 2001-2004 The Apache Software Foundation.
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
package org.apache.axis.encoding;
import java.io.IOException;

import org.apache.xml.utils.QName;
/**
 * I consider this to be a interface as the Axis may use differant serializers and deserializers 
 * that works on DOM/SAX/StAx ..so it can have different timplementatoins behind this.
 * StAx(pull)->SAX->DOM :)  
 */
public interface Serializer {
    public void serialize(Object object,QName xmlType,QName elementName)throws IOException;
    //Should we have overridden tyes for the simple types ?? 
    //I am feel like so 
    public void serialize(int in,QName elementName)throws IOException;
    public void serialize(double in,QName elementName)throws IOException;
    public void serialize(float in,QName elementName)throws IOException;
    public void serialize(byte in,QName elementName)throws IOException;
    public void serialize(long in,QName elementName)throws IOException;
    public void serialize(boolean in,QName elementName)throws IOException;
    public void serialize(short in,QName elementName)throws IOException;
    public void serialize(char in,QName elementName)throws IOException;
}


