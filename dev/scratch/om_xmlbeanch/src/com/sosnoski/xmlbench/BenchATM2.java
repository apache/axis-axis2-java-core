package com.sosnoski.xmlbench;

import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.mxp1.MXParserFactory;
import org.apache.axis.om.*;

import java.io.*;
import java.util.Iterator;

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
 * Date: Oct 8, 2004
 * Time: 1:46:37 PM
 * 
 */
public class BenchATM2 extends BenchDocBase{

    private XmlPullParserFactory xmlPullParserFactory;

    public BenchATM2() {
        super("AXTM");
    }


    /**
     *
     * @param in
     * @return
     */
    protected Object build(InputStream in) {
        OMModel model=null;
        try {

            if(xmlPullParserFactory==null){
                xmlPullParserFactory = MXParserFactory.newInstance();
                xmlPullParserFactory.setNamespaceAware(true);
            }

            XmlPullParser parser = xmlPullParserFactory.newPullParser();
            parser.setInput(new InputStreamReader(in));
            StreamingOMBuilder builder = new StreamingOMBuilder(parser);
            model = builder.getTableModel();

        } catch (XmlPullParserException e) {
            e.printStackTrace(System.err);
            System.exit(0);
        }

        //build the whole document before doing anything
        while(!model.isComplete())
            model.proceed();

        return model.getDocument();
    }

    /**
     *
     * @param doc
     * @param summary
     */
    protected void walk(Object doc, DocumentSummary summary) {

        summary.addElements(1);
        OMElement documentElement = null;
        documentElement = ((OMDocument)doc).getDocumentElement();
        walkElement(documentElement, summary);

    }

    protected void walkElement(OMElement element, DocumentSummary summary) {

        // include attribute values in summary
        Iterator iter = element.getAttributes();

        while (iter.hasNext()) {
            OMAttribute attr = (OMAttribute)iter.next();
            summary.addAttribute(attr.getValue().length());
        }

        // loop through children
        iter = element.getChildren();
        while (iter.hasNext()) {
            // handle child by type
            Object child = iter.next();
            if (child==null){
                return;
            }else if (child instanceof OMText) {
                summary.addContent(((OMText)child).getValue().length());
            } else if (child instanceof OMElement) {
                summary.addElements(1);
                walkElement((OMElement)child, summary);
            }

        }


    }

    protected void output(Object document, OutputStream out) {
        //cannot test this
        //No serialisers defined yet
        return;

    }

    protected void modify(Object document) {
        // Not possible yet
        //the model is still read only
        return;
    }

    protected boolean serialize(Object doc, OutputStream out) {
        return false;
    }



    /**
     * Note -  the original implementation has to be overridden to test the
     * @param passes
     * @param reps
     * @param excludes
     * @param text
     * @return
     */
    public int[] runTimeTest(int passes, int reps, int excludes, byte[] text) {

        // allocate array for result values
        int[] results = new int[TIME_RESULT_COUNT];
        for (int i = 0; i < results.length; i++) {
            results[i] = Integer.MIN_VALUE;
        }

        // create the reusable objects
        ByteArrayInputStream[][] ins = new ByteArrayInputStream[passes][reps];
        for (int i = 0; i < passes; i++) {
            for (int j = 0; j < reps; j++) {
                ins[i][j] = new ByteArrayInputStream(text);
            }
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream(text.length*2);

        // set start time for tests
        initTime();

        // first build the specified number of copies of the document
        Object[][] docs = new Object[passes][reps];
        int best = Integer.MAX_VALUE;
        int sum = 0;
        for (int i = 0; i < passes; i++) {
            for (int j = 0; j < reps; j++) {
                docs[i][j] = build(ins[i][j]);
            }
            int time = testPassTime();
            if (m_printPass) {
                reportValue("Build document pass " + i, time);
            }
            if (best > time) {
                best = time;
            }
            if (i >= excludes) {
                sum += time;
            }
        }
        results[BUILD_MIN_INDEX] = best;
        results[BUILD_AVERAGE_INDEX] = sum / (passes - excludes);

        // walk the constructed document copies
        DocumentSummary info = new DocumentSummary();
        best = Integer.MAX_VALUE;
        sum = 0;
        for (int i = 0; i < passes; i++) {
            for (int j = 0; j < reps; j++) {
                info.reset();
                walk(docs[i][j], info);
            }
            int time = testPassTime();
            if (m_printPass) {
                reportValue("Walk document pass " + i, time);
            }
            if (best > time) {
                best = time;
            }
            if (i >= excludes) {
                sum += time;
            }
        }
        results[WALK_MIN_INDEX] = best;
        results[WALK_AVERAGE_INDEX] = sum / (passes - excludes);

        // generate text representation of document copies
        //No need to do this

        initTime();

        // attempt serialization of document
        byte[] serial = null;
        Object restored = null;
        out.reset();
        if (!serialize(docs[0][0], out)) {
            if (m_printPass) {
                m_printStream.println("  **Serialization not supported by model**");
            }
        } else {

            // serialize with printing of times (first copy already done)
            best = Integer.MAX_VALUE;
            sum = 0;
            serial = out.toByteArray();
            for (int i = 0; i < passes; i++) {
                for (int j = (i == 0) ? 1 : 0; j < reps; j++) {
                    out.reset();
                    serialize(docs[i][j], out);
                }
                int time = testPassTime();
                if (m_printPass) {
                    reportValue("Serialize pass " + i, time);
                }
                if (best > time) {
                    best = time;
                }
                if (i >= excludes) {
                    sum += time;
                }
            }
            results[SERIALIZE_MIN_INDEX] = best;
            results[SERIALIZE_AVERAGE_INDEX] = sum / (passes - excludes);
            results[SERIALIZE_SIZE_INDEX] = serial.length;

            // restore from serialized form
            ByteArrayInputStream sin = new ByteArrayInputStream(serial);
            best = Integer.MAX_VALUE;
            sum = 0;
            for (int i = 0; i < passes; i++) {
                for (int j = 0; j < reps; j++) {
                    sin.reset();
                    restored = unserialize(sin);
                }
                int time = testPassTime();
                if (m_printPass) {
                    reportValue("Unserialize pass " + i, time);
                }
                if (best > time) {
                    best = time;
                }
                if (i >= excludes) {
                    sum += time;
                }
            }
            results[UNSERIALIZE_MIN_INDEX] = best;
            results[UNSERIALIZE_AVERAGE_INDEX] = sum / (passes - excludes);
        }

        // modify the document representation
        initTime();
        best = Integer.MAX_VALUE;
        sum = 0;
        for (int i = 0; i < passes; i++) {
            for (int j = 0; j < reps; j++) {
                modify(docs[i][j]);
            }
            int time = testPassTime();
            if (m_printPass) {
                reportValue("Modify pass " + i, time);
            }
            if (best > time) {
                best = time;
            }
            if (i >= excludes) {
                sum += time;
            }
        }
        results[MODIFY_MIN_INDEX] = best;
        results[MODIFY_AVERAGE_INDEX] = sum / (passes - excludes);

        // make sure generated text matches original document (outside timing)
//		Object check = build(new ByteArrayInputStream(output));
//		DocumentSummary verify = new DocumentSummary();
//		walk(check, verify);
//		if (!info.structureEquals(verify)) {
//			PrintStream err = m_printStream != null ?
//				m_printStream : System.err;
//			err.println("  **" + getName() + " Error: " +
//				"Document built from output text does " +
//				"not match original document**");
//			printSummary("  Original", info, err);
//			printSummary("  Rebuild", verify, err);
//		}

        // check if restored from serialized form
//		if (restored != null) {
//
//			// validate the serialization for exact match (outside timing)
//			verify.reset();
//			walk(restored, verify);
//			if (!info.equals(verify)) {
//				PrintStream err = m_printStream != null ?
//					m_printStream : System.err;
//				err.println("  **" + getName() + " Error: " +
//					"Document built from output text does " +
//					"not match original document**");
//				printSummary("  Original", info, err);
//				printSummary("  Rebuild", verify, err);
//			}
//		}

        // copy document summary values for return
        results[ELEMENT_COUNT_INDEX] = info.getElementCount();
        results[ATTRIBUTE_COUNT_INDEX] = info.getAttributeCount();
        results[CONTENT_COUNT_INDEX] = info.getContentCount();
        results[TEXTCHAR_COUNT_INDEX] = info.getTextCharCount();
        results[ATTRCHAR_COUNT_INDEX] = info.getAttrCharCount();

        // print summary for document
        if (m_printSummary) {
            printSummary("  Document", info, m_printStream);
            m_printStream.println("  Original text size was " + text.length );
//				", output text size was " + output.length);
            if (serial != null) {
                m_printStream.println("  Serialized length was " +
                        serial.length);
            }
            info.reset();
            walk(docs[0][0], info);
            printSummary("  Modified document", info, m_printStream);
/*			if (s_firstTime) {
out.reset();
output(docs[0][0], out);
m_printStream.println(" Text of modified document:");
m_printStream.println(out.toString());
s_firstTime = false;
}	*/
        }
        reset();
        return results;
    }
}
