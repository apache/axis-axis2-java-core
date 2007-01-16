/*
 * Copyright  1999-2004 The Apache Software Foundation.
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

package org.apache.savan.util;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Calendar;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.impl.llom.factory.OMXMLBuilderFactory;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder;
import org.apache.axis2.databinding.types.Duration;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.savan.SavanConstants;
import org.apache.savan.storage.SubscriberStore;

/**
 * A common set of methods that may be used in various places of Savan.
 */
public class CommonUtil {

	public static Calendar addDurationToCalendar (Calendar calendar,Duration duration) {
		calendar.add(Calendar.YEAR,duration.getYears());
		calendar.add(Calendar.MONTH,duration.getMonths());
		calendar.add(Calendar.DATE,duration.getDays());
		calendar.add(Calendar.HOUR,duration.getHours());
		calendar.add(Calendar.MINUTE,duration.getMinutes());
		calendar.add(Calendar.SECOND,(int) duration.getSeconds());
		
		return calendar;
	}
	
	/**
	 * Will be used by test cases to load XML files from test-resources as Envelopes
	 * SOAP 1.1 is assumed
	 * 
	 * @param path
	 * @param name
	 * @return
	 */
	public static SOAPEnvelope getTestEnvelopeFromFile (String path, String name) throws IOException {
        try {
        	String fullName = path + File.separator + name;
            FileReader reader = new FileReader(fullName);
            XMLStreamReader streamReader = XMLInputFactory.newInstance().createXMLStreamReader(
                    reader);
            StAXSOAPModelBuilder builder = OMXMLBuilderFactory.createStAXSOAPModelBuilder(
                    OMAbstractFactory.getSOAP11Factory(), streamReader);
            return builder.getSOAPEnvelope();
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
	}
	
	public static boolean isDuration (String timeStr) {
        return timeStr.startsWith("p") || timeStr.startsWith("P") || timeStr.startsWith("-p") || timeStr.startsWith("-P");

    }
	
	public static SubscriberStore getSubscriberStore (AxisService axisService) {
		Parameter parameter = axisService.getParameter(SavanConstants.SUBSCRIBER_STORE);
		if (parameter==null)
			return null;
		
		return (SubscriberStore) parameter.getValue();
	}
}
