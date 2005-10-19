package org.apache.axis2.databinding.utils;

import org.apache.axis2.databinding.ADBBean;
import org.apache.axis2.om.OMAttribute;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.util.BeanSerializerUtil;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

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
 */

public class ADBPullParser implements XMLStreamReader {

    private Object[] properties;
    private OMAttribute[] attributes;
    private QName elementQName;

    // Every parser can contain a reference to a pull parser of one of its children
    private XMLStreamReader childPullParser;

    // a flag for this class to know, we are currently accessing one of the children's parsers
    private boolean accessingChildPullParser = false;

    // ===== To be used this with ADBBean =============
    private boolean isEndElementFinished = false;

    // ===== To be used with Simple Name Value pair ====
    private boolean processingADBNameValuePair = false;
    private boolean nameValuePairStartElementProcessed = false;
    private boolean nameValuePairTextProcessed = false;
    private boolean nameValuePairEndElementProcessed = false;
    private ParserInformation tempParserInfo;
    // ==============================================

    private ParserInformation parserInformation;

    // a pointer to the children list of current location
    private int currentIndex = 0;

    // namespace handling
    private HashMap declaredNamespaces;

    // the following two are to improve the efficiency in code and should not be used by anyone as
    // the values these holds may be out of date.
    private ArrayList tempDeclaredNamespacesArray;
    private ArrayList tempDeclaredNamespacePrefixesArray;

    /**
     * This namespace map will contain uri as the key and the prefix as the value, so that I can check
     * whether a particular namespace is there or not easily.
     * Element has the responsibility of removing the new namespaces he has added after the end element.
     * Stack is the best option, but searching for a particular namespace in the stack is not that efficient
     * we should be able to do the search more often than adding and removing namespace. So here I have to
     * forgo the easyness of Stacks for pushing and poping over the easier searchability of HashMap.
     * For removing newly added namespaces, element has to keep track of new namespaces he has added
     * remove them from the namespace map, after the end element.
     */
    private HashMap namespaceMap;


    private ADBPullParser(QName adbBeansQName, Object[] properties, OMAttribute[] attributes) {
        this.properties = properties;
        this.elementQName = adbBeansQName;
        this.attributes = attributes;
        namespaceMap = new HashMap();
    }

    /**
     * @param properties    - this should contain all the stuff that stax events should be generated.
     *                      Lets take an example of a bean.
     *                      <pre> <Person>
     *                                                                                                                                                                                                                                            <Name>FooOne</Name>
     *                                                                                                                                                                                                                                            <DependentOne>
     *                                                                                                                                                                                                                                                <Name>FooTwo</Name>
     *                                                                                                                                                                                                                                                <Age>25</Age>
     *                                                                                                                                                                                                                                                <Sex>Male</Sex>
     *                                                                                                                                                                                                                                            </DependentOne>
     *                                                                                                                                                                                                                                        </Person>
     *                      <p/>
     *                                                                                                                                                                                                                                        so the mapping bean for this is
     *                                                                                                                                                                                                                                        class Person {
     *                                                                                                                                                                                                                                            String Name;
     *                                                                                                                                                                                                                                            Dependent dependentOne;
     *                                                                                                                                                                                                                                        }
     *                      <p/>
     *                                                                                                                                                                                                                                        class Dependent {
     *                                                                                                                                                                                                                                            String name;
     *                                                                                                                                                                                                                                            int age;
     *                                                                                                                                                                                                                                            String sex;
     *                                                                                                                                                                                                                                        }
     *                      <p/>
     *                                                                                                                                                                                                                                        So if one needs to generate pull events out of a Person bean, the array he needs
     *                                                                                                                                                                                                                                        to pass is like this.
     *                                                                                                                                                                                                                                        ---------------------------------------------------------------------------------------------------
     *                                                                                                                                                                                                                                        | "Name" | "FooOne" | QName("DependentOne") | Dependent object| null | Array of Dependent objects |
     *                                                                                                                                                                                                                                        ---------------------------------------------------------------------------------------------------
     *                                                                                                                                                                                                                                        This DependentObject can either be an ADBBean, OMElement or a POJO. If its an ADBBean
     *                                                                                                                                                                                                                                        We directly get the pull parser from that. If not we create a reflection based
     *                                                                                                                                                                                                                                        pull parser for that java bean.
     *                                                                                                                                                                                                                    </pre>
     * @param adbBeansQName
     * @return XMLStreamReader
     */
    public static XMLStreamReader createPullParser(QName adbBeansQName, Object[] properties, OMAttribute[] attributes) {
        return new ADBPullParser(adbBeansQName, properties, attributes);
    }

    public static XMLStreamReader createPullParser(QName adbBeansQName, Object[] properties, OMAttribute[] attributes, boolean isDocumentElement) {
        return new ADBPullParser(adbBeansQName, properties, attributes);
    }

    public boolean isCompleted() {
        return isEndElementFinished;
    }

    // ----------- XMLStreamReader Methods -------------------------------------------//
    public Object getProperty(String string) throws IllegalArgumentException {
        return null;
    }

    public int next() throws XMLStreamException {

        /** First check whether the parser has already has completed. currentIndex starts with 0. But
         but the first emulated event is the start element of the given element. The same index pointer
         is used to point to the elements in the Array. Since we allocate index 0 to the current element,
         currentIndex is always ahead one step from the location of the array which is currently being processed.
         so when you check for completeness you have to check for currentIndex >= array length + 2
         */

        // terminate condition.
        // if properties are set check we have traversed all of them. If there are no properties, then
        // check whether we have already thrown the END Element.
        if ((properties != null && currentIndex >= properties.length + 2) || (properties == null && isEndElementFinished)) {
            throw new XMLStreamException("End of elements has already been reached. Can not go beyond that");
        }

        if (accessingChildPullParser) {
            if (childPullParser instanceof ADBPullParser && !((ADBPullParser) childPullParser).isCompleted()) {
                return childPullParser.next();
            } else {
                accessingChildPullParser = false;
                currentIndex += 2;
            }
        }

        if (processingADBNameValuePair && nameValuePairEndElementProcessed) {
            processingADBNameValuePair = false;
            currentIndex = currentIndex + 2;
            parserInformation = tempParserInfo;
        }

        if (currentIndex == 0) {
            // then this is just the start element
            currentIndex++;
            parserInformation = new ParserInformation(this.elementQName);
            handleNamespaces();
            return XMLStreamConstants.START_ELEMENT;
        } else if (properties == null || properties.length + 1 == currentIndex) {
            // this is the end of this element
            currentIndex++;
            isEndElementFinished = true;
            return XMLStreamConstants.END_ELEMENT;
        } else {
            if (processingADBNameValuePair) {
                return processADBNameValuePair(null, null);
            }
            Object o = properties[currentIndex - 1];
            if (o instanceof QName) {

                Object object = properties[currentIndex];
                if (object instanceof ADBBean) {
                    ADBBean adbBean = (ADBBean) object;
                    ADBPullParser adbPullParser = (ADBPullParser) adbBean.getPullParser((QName) o);
                    adbPullParser.setNamespaceMap(this.namespaceMap);
                    childPullParser = adbPullParser;
                } else if (object instanceof OMElement) {
//                   childPullParser = (OMElement) ;
                } else {
                    childPullParser = BeanSerializerUtil.getPullParser(object, (QName) o);
                }
                accessingChildPullParser = true;
                return this.next();
            } else if (o instanceof String) {
                String simplePropertyName = (String) o;
                String simplePropertyValue = (String) properties[currentIndex];
                processingADBNameValuePair = true;
                return processADBNameValuePair(simplePropertyName, simplePropertyValue);
            } else {
                throw new XMLStreamException("Sorry !! We only support QNames and Strings as the keys of the properties list");
            }
        }

    }

    private void handleNamespaces() {
        // by this time all the attributes related methods can be called.
        // now need to extract namespace from them and attach them to the element itself.
        String elementNSUri = this.elementQName.getNamespaceURI();
        if (namespaceMap.get(elementNSUri) == null) {
            String prefix = this.elementQName.getPrefix();
            namespaceMap.put(elementNSUri, prefix);
            if (declaredNamespaces == null) declaredNamespaces = new HashMap();
            declaredNamespaces.put(elementNSUri, prefix);
        }
        if (attributes != null) {
            for (int i = 0; i < attributes.length; i++) {
                OMAttribute attribute = attributes[i];
                if (namespaceMap.get(attribute.getQName().getNamespaceURI()) == null) {
                    String namespaceURI = attribute.getQName().getNamespaceURI();
                    String prefix = attribute.getQName().getPrefix();
                    namespaceMap.put(namespaceURI, prefix);
                    if (declaredNamespaces == null) declaredNamespaces = new HashMap();
                    declaredNamespaces.put(namespaceURI, prefix);
                }
            }
        }
    }


    public boolean hasNext() throws XMLStreamException {
        return !isEndElementFinished;
    }

    public String getElementText() throws XMLStreamException {
        if (accessingChildPullParser) return childPullParser.getElementText();
        return parserInformation != null ? parserInformation.getText() : "";
    }


    public int getAttributeCount() {
        if (accessingChildPullParser) return childPullParser.getAttributeCount();
        if (attributes != null) return attributes.length;
        return 0;
    }

    public int getNamespaceCount() {
        if (accessingChildPullParser) return childPullParser.getNamespaceCount();
        return declaredNamespaces == null ? 0 : declaredNamespaces.size();
    }

    public String getText() {
        if (accessingChildPullParser) return childPullParser.getText();
        return parserInformation != null ? parserInformation.getText() : "";
    }

    public boolean hasText() {
        if (accessingChildPullParser) return childPullParser.hasText();
        return parserInformation != null && parserInformation.getText() != null && !"".equals(parserInformation.getText());
    }

    public QName getName() {
        if (accessingChildPullParser) return childPullParser.getName();
        return parserInformation != null ? parserInformation.getName() : null;
    }

    public String getLocalName() {
        if (accessingChildPullParser) return childPullParser.getLocalName();
        return parserInformation != null ? parserInformation.getName().getLocalPart() : null;
    }

    public boolean hasName() {
        if (accessingChildPullParser) return childPullParser.hasName();
        return parserInformation != null && parserInformation.getName() != null;
    }

    public String getNamespaceURI() {
        if (accessingChildPullParser) return childPullParser.getNamespaceURI();
        return parserInformation != null && parserInformation.getName() != null ? parserInformation.getName().getNamespaceURI() : "";
    }

    public String getPrefix() {
        if (accessingChildPullParser) return childPullParser.getPrefix();
        return parserInformation != null ? parserInformation.getName().getPrefix() : null;
    }

    public String getAttributeValue(String namespaceURI, String localName) {
        boolean testNSUri = false;
        boolean testLocalName = false;
        if (accessingChildPullParser) return childPullParser.getAttributeValue(namespaceURI, localName);

        // Do I need to handle the no attributes case. It will automatically gets passed from here, returning null.
        if (namespaceURI == null || "".equals(namespaceURI)) testNSUri = true;
        if (localName == null || "".equals(localName)) testLocalName = true;

        for (int i = 0; i < attributes.length; i++) {
            QName attrQName = attributes[i].getQName();
            boolean isNSUriMatch = !testNSUri;
            boolean isLocalNameMatch = !testLocalName;
            if (testNSUri && namespaceURI.equals(attrQName.getNamespaceURI())) {
                isNSUriMatch = true;
            }
            if (testLocalName && localName.equals(attrQName.getLocalPart())) {
                isLocalNameMatch = true;
            }
            if (isLocalNameMatch && isNSUriMatch) return attributes[i].getAttributeValue();
        }
        return null;
    }

    public QName getAttributeName(int i) {
        if (accessingChildPullParser) {
            return childPullParser.getAttributeName(i);
        } else if (attributes != null && attributes.length >= i) {
            return attributes[i].getQName();
        }
        return null;
    }

    public String getAttributeNamespace(int i) {
        if (accessingChildPullParser) {
            return childPullParser.getAttributeNamespace(i);
        } else if (attributes != null && attributes.length >= i && attributes[i].getNamespace() != null) {
            return attributes[i].getNamespace().getName();
        }
        return null;
    }

    public String getAttributeLocalName(int i) {
        if (accessingChildPullParser) {
            return childPullParser.getAttributeLocalName(i);
        } else if (attributes != null && attributes.length >= i) {
            return attributes[i].getLocalName();
        }
        return null;
    }

    public String getAttributePrefix(int i) {
        if (accessingChildPullParser) {
            return childPullParser.getAttributePrefix(i);
        } else if (attributes != null && attributes.length >= i) {
            return attributes[i].getQName().getPrefix();
        }
        return null;
    }

    public String getAttributeType(int i) {
        // see http://www.w3.org/TR/REC-xml/#NT-StringType.
        // since all our attrinutes are Strings, I'm returning CDATA here. But not 100% sure what to do
        return "CDATA";
    }

    public String getAttributeValue(int i) {
        if (accessingChildPullParser) {
            return childPullParser.getAttributeValue(i);
        } else if (attributes != null && attributes.length >= i) {
            return attributes[i].getAttributeValue();
        }
        return null;
    }

    public boolean isAttributeSpecified(int i) {
        if (accessingChildPullParser) {
            return childPullParser.isAttributeSpecified(i);
        }
        return (attributes != null && attributes.length >= i);
    }

// -------- un-implemented methods ----------

    public void require(int i, String string, String string1) throws XMLStreamException {
        throw new UnsupportedOperationException("Yet to be implemented !!");
    }

    public int nextTag() throws XMLStreamException {
        throw new UnsupportedOperationException("Yet to be implemented !!");
    }

    public void close() throws XMLStreamException {
        throw new UnsupportedOperationException("Yet to be implemented !!");
    }

    public String getNamespaceURI(String prefixParam) {
        if (accessingChildPullParser) return childPullParser.getNamespaceURI(prefixParam);
        if ("".equals(prefixParam) || prefixParam == null) return null;

        if (declaredNamespaces != null) {
            Iterator nsIter = declaredNamespaces.keySet().iterator();
            while (nsIter.hasNext()) {
                String nsURI = (String) nsIter.next();
                if (prefixParam.equals(declaredNamespaces.get(nsURI))) {
                    return nsURI;
                }
            }
        }
        return null;
    }

    public boolean isStartElement() {
        throw new UnsupportedOperationException("Yet to be implemented !!");
    }

    public boolean isEndElement() {
        throw new UnsupportedOperationException("Yet to be implemented !!");
    }

    public boolean isCharacters() {
        throw new UnsupportedOperationException("Yet to be implemented !!");
    }

    public boolean isWhiteSpace() {
        throw new UnsupportedOperationException("Yet to be implemented !!");
    }


    public String getNamespacePrefix(int index) {
        /* We are holding namespaces in a HashMap and there is no direct way to retrieve a prefix
         by its index. So I need to call toArray and then get the index. Since this method will be
         called recursively and if we call toArray again and again, this is a performance killer.
         So I create a temp array, at the first time this method being called and use that to serve
         future requests. But at the same time I check whether the temp array contains up-to-date
         information or not.
        */
        if (accessingChildPullParser) return childPullParser.getNamespacePrefix(index);
        if (declaredNamespaces != null && declaredNamespaces.size() >= index) {
            if (tempDeclaredNamespacePrefixesArray == null || tempDeclaredNamespacePrefixesArray.size() != declaredNamespaces.size()) {
                tempDeclaredNamespacePrefixesArray = new ArrayList();
                Iterator iterator = declaredNamespaces.values().iterator();
                while (iterator.hasNext()) {
                    String s = (String) iterator.next();
                    tempDeclaredNamespacePrefixesArray.add(s);
                }
            }
            return (String) tempDeclaredNamespacePrefixesArray.get(index);
        }
        return null;
    }

    public String getNamespaceURI(int index) {
        /* We are holding namespaces in a HashMap and there is no direct way to retrieve a namespace
         by its index. So I need to call toArray and then get the index. Since this method will be
         called recursively and if we call toArray again and again, this is a performance killer.
         So I create a temp array, at the first time this method being called and use that to serve
         future requests. But at the same time I check whether the temp array contains up-to-date
         information or not.
        */
        if (accessingChildPullParser) return childPullParser.getNamespaceURI(index);
        if (declaredNamespaces != null && declaredNamespaces.size() >= index) {
            if (tempDeclaredNamespacesArray == null || tempDeclaredNamespacesArray.size() != declaredNamespaces.size()) {
                tempDeclaredNamespacesArray = new ArrayList();
                Iterator iterator = declaredNamespaces.keySet().iterator();
                while (iterator.hasNext()) {
                    String s = (String) iterator.next();
                    tempDeclaredNamespacesArray.add(s);
                }
            }
            return (String) tempDeclaredNamespacesArray.get(index);
        }
        return null;
    }

    public NamespaceContext getNamespaceContext() {
        throw new UnsupportedOperationException("Yet to be implemented !!");
    }

    public int getEventType() {
        throw new UnsupportedOperationException("Yet to be implemented !!");
    }

    public char[] getTextCharacters() {
        throw new UnsupportedOperationException("Yet to be implemented !!");
    }

    public int getTextCharacters(int i, char[] chars, int i1, int i2) throws XMLStreamException {
        throw new UnsupportedOperationException("Yet to be implemented !!");
    }

    public int getTextStart() {
        throw new UnsupportedOperationException("Yet to be implemented !!");

    }

    public int getTextLength() {
        throw new UnsupportedOperationException("Yet to be implemented !!");
    }

    public String getEncoding() {
        // https://stax-utils.dev.java.net/nonav/javadoc/api/javax/xml/stream/XMLStreamReader.html#getEncoding()
        return null;
    }

    public Location getLocation() {
        throw new UnsupportedOperationException("Yet to be implemented !!");
    }

    public String getVersion() {
        // https://stax-utils.dev.java.net/nonav/javadoc/api/javax/xml/stream/XMLStreamReader.html#getVersion()
        return null;
    }

    public boolean isStandalone() {
        // https://stax-utils.dev.java.net/nonav/javadoc/api/javax/xml/stream/XMLStreamReader.html#isStandalone()
        return false;
    }

    public boolean standaloneSet() {
        throw new UnsupportedOperationException("Yet to be implemented !!");
    }

    public String getCharacterEncodingScheme() {
        // https://stax-utils.dev.java.net/nonav/javadoc/api/javax/xml/stream/XMLStreamReader.html#getCharacterEncodingScheme()
        return null;
    }

    public String getPITarget() {
        throw new UnsupportedOperationException("Yet to be implemented !!");
    }

    public String getPIData() {
        throw new UnsupportedOperationException("Yet to be implemented !!");
    }

// =============================================================================
// Utill methods inside this class
// =============================================================================

    private int processADBNameValuePair(String simplePropertyName, String simplePropertyValue) {
        int event = 0;
        if (!nameValuePairStartElementProcessed) {
            event = XMLStreamConstants.START_ELEMENT;
            tempParserInfo = parserInformation;
            parserInformation = new ParserInformation(new QName(simplePropertyName), simplePropertyValue);
            nameValuePairStartElementProcessed = true;
            nameValuePairEndElementProcessed = false;
        } else if (nameValuePairStartElementProcessed && !nameValuePairTextProcessed) {
            event = XMLStreamConstants.CHARACTERS;
            nameValuePairTextProcessed = true;
        } else if (nameValuePairTextProcessed) {
            event = XMLStreamConstants.END_ELEMENT;
            nameValuePairEndElementProcessed = true;
            nameValuePairStartElementProcessed = false;
            nameValuePairTextProcessed = false;
        }
        return event;
    }

    public HashMap getNamespaceMap() {
        return namespaceMap;
    }

    public void setNamespaceMap(HashMap namespaceMap) {
        this.namespaceMap = namespaceMap;
    }

// --------------------------------------------------------------------------------------------------//

    /**
     * Inner class which holds stuff for the parser to pick data.
     * This hold the information the parser will hold when user request for data. Every ADBPullParser
     * holds this kind of object inside it and within the methods of ADBPullParser, they refer to the
     * fields inside this class. So if user needs to change what parser returns, he just need to
     * change parser information object.
     */
    public class ParserInformation {
        String text;
        QName name;


        public ParserInformation(QName name, String text) {
            this.text = text;
            this.name = name;
        }

        public ParserInformation(QName name) {
            this.name = name;
        }


        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public QName getName() {
            return name;
        }

        public void setName(QName name) {
            this.name = name;
        }
    }


}
