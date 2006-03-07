package org.apache.axis2.databinding.utils;

import org.apache.axis2.databinding.ADBBean;
import org.apache.ws.commons.om.OMAbstractFactory;
import org.apache.ws.commons.om.OMAttribute;
import org.apache.ws.commons.om.OMElement;
import org.apache.ws.commons.om.impl.llom.EmptyOMLocation;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.lang.reflect.Array;
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

    // this will help to handle Text within the current element.
    // user should pass the element text to the property list as this ELEMENT_TEXT as the key
    public static final String ELEMENT_TEXT = "Element Text";
    private boolean processingElementText = false;

    private Object[] properties;
    private Object[] attributes;
    private QName elementQName;

    // attributes array can contain attributes in different ways. This will serve as constant way
    // of handling atributes within the code. I know this is another object, BUT .....
    private ArrayList attributesList;


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
    private boolean finishedProcessingNameValuePair = false;
    private ParserInformation tempParserInfo;
    // ==============================================

    // ===== To be used with Simple Name Value pair ====
    // this is used when we have an array of Strings
    private boolean processingComplexADBNameValuePair = false;
    private Object complexObjectArray;
    private QName complexStringArrayName;
    // ==============================================
    //
    // ===== To be used with Arrays coming within the propery list (except String arrays) ====
    // this is used when we have an array of ADBBeans, OMElements or Beans
    private boolean processingComplexArray = false;
    private Object[] complexArray;
    private QName complexArrayQName;
    // ==============================================

    // some time arrays can come within the property list array. Following will bes used as the
    // index of that array
    private int secondArrayIndex = 0;


    private ParserInformation parserInformation;

    // a pointer to the children list of current location
    private int currentIndex = 0;

    // namespace handling
    private HashMap declaredNamespaces;

    // the following two are to improve the efficiency in code and should not be used by anyone as
    // the values these holds may be out of date.
    private ArrayList tempDeclaredNamespacesArray;
    private ArrayList tempDeclaredNamespacePrefixesArray;

    private int eventType;

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
    private static final QName NIL_QNAME = new QName("http://www.w3.org/2001/XMLSchema-instance","nil");


    private ADBPullParser(QName adbBeansQName, Object[] properties, Object[] attributes) {
        this.properties = properties;
        this.elementQName = adbBeansQName;
        this.attributes = attributes;
        namespaceMap = new HashMap();
    }

    /**
     * @param properties - this should contain all the stuff that stax events should be generated.
     *                   Lets take an example of a bean.
     *                   <pre>
     *                                                                                                                                                                                                           <Person>
     *                                                                                                                                                                                                               <DependentOne>
     *                                                                                                                                                                                                                  <Name>FooTwo</Name>
     *                                                                                                                                                                                                                  <Age>25</Age>
     *                                                                                                                                                                                                                  <Sex>Male</Sex>
     *                                                                                                                                                                                                                  </DependentOne>
     *                                                                                                                                                                                                          </Person>
     *                   <p/>
     *                   <p/>
     *                                                                                                                                                                                                      so the mapping bean for this is
     *                                                                                                                                                                                                      class Person {
     *                                                                                                                                                                                                         String Name;
     *                                                                                                                                                                                                         Dependent dependentOne;
     *                                                                                                                                                                                                      }
     *                   <p/>
     *                   <p/>
     *                                                                                                                                                                                                                                                                                                                                                         }
     *                   <p/>
     *                   <p/>
     *                                                                                                                                                                                                      So if one needs to generate pull events out of a Person bean, the array he needs
     *                                                                                                                                                                                                      to pass is like this.
     *                                                                                                                                                                                                      ---------------------------------------------------------------------------------------------------
     *                                                                                                                                                                                                      | "Name" | "FooOne" | QName("DependentOne") | Dependent object| null | Array of Dependent objects |
     *                                                                                                                                                                                                      ---------------------------------------------------------------------------------------------------
     *                                                                                                                                                                                                      This DependentObject can either be an ADBBean, OMElement or a POJO. If its an ADBBean
     *                                                                                                                                                                                                      We directly get the pull parser from that. If not we create a reflection based
     *                                                                                                                                                                                     pull parser for that java bean.
     *                   <p/>
     *                   <p/>
     *                   <p/>
     *                                                                                                                                                                                     This is the how the passed array should look like
     *                                                                                                                                                                                                               Key             Value
     *                                                                                                                                                                                                             String          String
     *                                                                                                                                                                                                             QName           ADBBean, OMElement, Bean, String, null
     *                                                                                                                                                                                     String          String[]
     *                                                                                                                                                                                     QName          String[]
     *                                                                                                                                                                                     QName           Object[] - this contains only one type of objects
     *                   <p/>
     *                   <p/>
     *                                                                                                                                                                                     This is how the passed attribute array should look like
     *                                                                                                                                                                                     Key             Value
     *                                                                                                                                                                                     null            OMAttribute[]
     *                                                                                                                                                                                     QName           String
     *                                                                                                                                                                                     String          String
     *                                                                                                                                                                                     </pre>
     * @return XMLStreamReader
     */
    public static XMLStreamReader createPullParser(QName adbBeansQName, Object[] properties, Object[] attributes) {
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
        if ((properties != null && currentIndex >= properties.length + 2) || (properties == null && isEndElementFinished))
        {
            throw new XMLStreamException("End of elements has already been reached. Can not go beyond that");
        }

        // check we can still get events from the child pull parser
        if (accessingChildPullParser) {

            // if there are some stuff available proceed with that
            if (childPullParser.hasNext()) {
                eventType = childPullParser.next();
                return eventType;
            } else if (processingComplexArray) {
                // check we are processing an array which was sent inside the property list
                if (complexArray.length > ++secondArrayIndex) {
                    // seems we have some more to process
                    getPullParser(complexArray[secondArrayIndex], complexArrayQName);
                    eventType = this.next();
                    return eventType;
                } else {
                    processingComplexArray = false;
                    accessingChildPullParser = false;
                    currentIndex += 2;
                }

            } else {
                accessingChildPullParser = false;
                currentIndex += 2;
            }
        }

        // now check whether we are processing a complex string array or not
        if (processingComplexADBNameValuePair && finishedProcessingNameValuePair) {
            // this means we are done with processing one complex string array entry
            // check we have more
            if (Array.getLength(complexObjectArray) > ++secondArrayIndex) {
                // we have some more to process
                processingADBNameValuePair = true;
                eventType = processADBNameValuePair(complexStringArrayName, Array.get(complexObjectArray, secondArrayIndex));
                return eventType;
            } else {
                // completed looking at all the entries. Now go forward with normal entries, if any.
                processingComplexADBNameValuePair = false;
            }
        }

        // check whether we are done with processing a name value pair from an earlier cycle
        if (processingADBNameValuePair && finishedProcessingNameValuePair) {
            processingADBNameValuePair = false;
            currentIndex = currentIndex + 2;
            parserInformation = tempParserInfo;
        }

        if (currentIndex == 0) {
            // then this is just the start element
            currentIndex++;
            parserInformation = new ParserInformation(this.elementQName);
            handleNamespacesAndAttributes();
            eventType = XMLStreamConstants.START_ELEMENT;
            return eventType;
        } else if (properties == null || properties.length + 1 == currentIndex) {
            // this is the end of this element
            currentIndex++;
            isEndElementFinished = true;
            removeDeclaredNamespaces();
            eventType = XMLStreamConstants.END_ELEMENT;
            return eventType;
        } else {

            // first remove all the attributes from the current element
            attributesList = null;

            if (processingADBNameValuePair) {
                eventType = processADBNameValuePair(null, null);
                return eventType;
            }
            Object o = properties[currentIndex - 1];
            if (o instanceof QName) {
                Object object = properties[currentIndex];
                if (object instanceof String[]) {

                    complexStringArrayName = (QName) o;
                    complexObjectArray = object;
                    secondArrayIndex = 0;

                    complexObjectArray = object;
                    secondArrayIndex = 0;
                    processingComplexADBNameValuePair = true;

                    // use the simple name value pair processing recursively
                    processingADBNameValuePair = true;

                    eventType = processADBNameValuePair((QName) o, Array.get(complexObjectArray, secondArrayIndex));
                    return eventType;


                } else if (object instanceof Object[]) {
                    secondArrayIndex = 0;
                    complexArray = (Object[]) object;
                    complexArrayQName = (QName) o;
                    getPullParser(complexArray[secondArrayIndex], complexArrayQName);
                    processingComplexArray = true;
                } else if (object instanceof String) {
                    processingADBNameValuePair = true;
                    eventType = processADBNameValuePair((QName) o, (String) object);
                    return eventType;
                } else if (object == null) {
                    OMElement omElement = OMAbstractFactory.getOMFactory().createOMElement((QName) o, null);
                    omElement.addAttribute("nil", "true", null);
                    childPullParser = omElement.getXMLStreamReader();

                    // here we are injecting one additional element. So need to accomodate that.
//                    currentIndex --;
                } else {
                    getPullParser(object, (QName) o);
                }
                accessingChildPullParser = true;
                eventType = this.next();
                return eventType;
            } else if (o instanceof String) {

                // TODO : Chinthaka this is not finished.
                Object property = properties[currentIndex];
                String simplePropertyName = (String) o;

                if (property.getClass().isArray()) {

                    complexStringArrayName = new QName(simplePropertyName);
                    complexObjectArray = property;
                    secondArrayIndex = 0;
                    processingComplexADBNameValuePair = true;

                    // use the simple name value pair processing recursively
                    processingADBNameValuePair = true;
                    eventType = processADBNameValuePair(new QName(simplePropertyName), Array.get(complexObjectArray, secondArrayIndex));
                    return eventType;

                } else if (property instanceof String) {
                    String simplePropertyValue = (String) properties[currentIndex];
                    if (ELEMENT_TEXT.equals(simplePropertyName)) {
                        // this is element text.
                        processingElementText = true;
                    }
                    processingADBNameValuePair = true;

                    eventType = processADBNameValuePair(new QName(simplePropertyName), simplePropertyValue);
                    return eventType;
                } else if (property == null) {
                    // a null value has a special resolution, it should produce an element with nil="true" attribute and
                    // no content
                    //add to the attributes nil="true" to the list
                    if (attributesList == null || attributesList.size() == 0) {
                        attributesList = new ArrayList();
                        attributesList.add(NIL_QNAME);
                        attributesList.add("true");
                    } else {
                        //since we append the nil attribute at the end, check the nil attrib at the end
                        //if it's already there, move on
                        if (!attributesList.contains(NIL_QNAME)) {
                            attributesList.add(NIL_QNAME);
                            attributesList.add("true");
                        }
                    }
                    processingADBNameValuePair = true;
                    eventType = processADBNameValuePair(new QName(simplePropertyName), null);
                    return eventType;
                }
                throw new XMLStreamException("Only String and String[] are accepted as the values when the key is a String");
            } else {
                throw new XMLStreamException("Sorry !! We only support QNames and Strings as the keys of the properties list");
            }
        }

    }


    private void removeDeclaredNamespaces() {
        if (declaredNamespaces != null) {
            Iterator declaredNamespacesURIIter = declaredNamespaces.keySet().iterator();
            while (declaredNamespacesURIIter.hasNext()) {
                String s = (String) declaredNamespacesURIIter.next();
                namespaceMap.remove(s);
            }
        }
    }

    private XMLStreamReader getPullParser(Object object, QName qname) {
        if (object instanceof ADBBean) {
            ADBBean adbBean = (ADBBean) object;
            ADBPullParser adbPullParser = (ADBPullParser) adbBean.getPullParser(qname);
            adbPullParser.setNamespaceMap(this.namespaceMap);
            childPullParser = adbPullParser;
        } else if (object instanceof OMElement) {
            childPullParser = ((OMElement) object).getXMLStreamReader();
        } else {
            childPullParser = BeanUtil.getPullParser(object, qname);
        }
        return childPullParser;
    }

    private void handleNamespacesAndAttributes() {
        // by this time all the attributes related methods can be called.
        // now need to extract namespace from them and attach them to the element itself.
        String elementNSUri = this.elementQName.getNamespaceURI();
        if (!"".equals(elementNSUri) && elementNSUri != null && namespaceMap.get(elementNSUri) == null)
        {
            String prefix = this.elementQName.getPrefix();
            namespaceMap.put(elementNSUri, prefix);
            if (declaredNamespaces == null) declaredNamespaces = new HashMap();
            declaredNamespaces.put(elementNSUri, prefix);
        }

        attributesList = new ArrayList();
        if (attributes != null) {
            for (int i = 0; i < attributes.length; i += 2) {
                Object key = attributes[i];

                if ((key == null) && (attributes[i + 1] instanceof OMAttribute[])) {
                    OMAttribute[] omAttributesArray = ((OMAttribute[]) attributes[i + 1]);
                    for (int j = 0; j < omAttributesArray.length; j++) {
                        OMAttribute omAttribute = omAttributesArray[j];
                        checkNamespaceList(omAttribute.getQName().getNamespaceURI(),
                                omAttribute.getQName().getPrefix());
                        attributesList.add(omAttribute.getQName());
                        attributesList.add(omAttribute.getAttributeValue());
                    }

                } else if (key instanceof QName) {
                    QName qName = (QName) key;
                    checkNamespaceList(qName.getNamespaceURI(), qName.getPrefix());
                    attributesList.add(qName);
                    attributesList.add(attributes[i + 1]);
                } else if (key instanceof String) {
                    String keyString = (String) key;
                    attributesList.add(new QName(keyString));
                    attributesList.add(attributes[i + 1]);
                }
            }
        }
    }

    private void checkNamespaceList(String namespaceURI, String namespacePrefix) {
        if (namespaceMap.get(namespaceURI) == null) {
            namespaceMap.put(namespaceURI, namespacePrefix);
            if (declaredNamespaces == null) declaredNamespaces = new HashMap();
            declaredNamespaces.put(namespaceURI, namespacePrefix);
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
        if (attributesList != null) return attributesList.size() / 2;
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
        if (accessingChildPullParser)
            return childPullParser.getAttributeValue(namespaceURI, localName);

        // Do I need to handle the no attributes case. It will automatically gets passed from here, returning null.
        if (namespaceURI == null || "".equals(namespaceURI)) testNSUri = true;
        if (localName == null || "".equals(localName)) testLocalName = true;

        for (int i = 0; i < attributes.length; i += 2) {
            QName attrQName = (QName) attributesList.get(i);
            boolean isNSUriMatch = !testNSUri;
            boolean isLocalNameMatch = !testLocalName;
            if (testNSUri && namespaceURI.equals(attrQName.getNamespaceURI())) {
                isNSUriMatch = true;
            }
            if (testLocalName && localName.equals(attrQName.getLocalPart())) {
                isLocalNameMatch = true;
            }
            if (isLocalNameMatch && isNSUriMatch) return (String) attributesList.get(i + 1);
        }
        return null;
    }

    public QName getAttributeName(int i) {
        if (accessingChildPullParser) {
            return childPullParser.getAttributeName(i);
        } else if (attributesList != null && attributesList.size() / 2 >= i) {
            return (QName) attributesList.get(i * 2);
        }
        return null;
    }

    public String getAttributeNamespace(int i) {
        if (accessingChildPullParser) {
            return childPullParser.getAttributeNamespace(i);
        } else if (attributesList != null && (attributesList.size() / 2) >= i) {
            return ((QName) attributesList.get(i * 2)).getNamespaceURI();
        }
        return null;
    }

    public String getAttributeLocalName(int i) {
        if (accessingChildPullParser) {
            return childPullParser.getAttributeLocalName(i);
        } else if (attributesList != null && attributesList.size() / 2 >= i) {
            return ((QName) attributesList.get(i * 2)).getLocalPart();
        }
        return null;
    }

    public String getAttributePrefix(int i) {
        if (accessingChildPullParser) {
            return childPullParser.getAttributePrefix(i);
        } else if (attributes != null && attributesList.size() / 2 >= i) {
            return ((QName) attributesList.get(i * 2)).getPrefix();
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
        } else if (attributes != null && attributesList.size() / 2 >= i) {
            return (String) attributesList.get(i * 2 + 1);
        }
        return null;
    }

    public boolean isAttributeSpecified(int i) {
        if (accessingChildPullParser) {
            return childPullParser.isAttributeSpecified(i);
        }
        return (attributes != null && attributesList.size() >= i * 2);
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
            if (tempDeclaredNamespacePrefixesArray == null || (tempDeclaredNamespacePrefixesArray.size() != declaredNamespaces.size()))
            {
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
            if (tempDeclaredNamespacesArray == null || (tempDeclaredNamespacesArray.size() != declaredNamespaces.size()))
            {
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

    public NamespaceContext getNamespaceContext() {
        throw new UnsupportedOperationException("Yet to be implemented !!");
    }

    public int getEventType() {
        return eventType;
    }

    public char[] getTextCharacters() {
        if (accessingChildPullParser) return childPullParser.getTextCharacters();
        String text = parserInformation != null ? parserInformation.getText() : "";
        return text.toCharArray();
    }

    public int getTextCharacters(int i, char[] chars, int i1, int i2) throws XMLStreamException {
        throw new UnsupportedOperationException("Yet to be implemented !!");
    }

    public int getTextStart() {
        return 0;

    }

    public int getTextLength() {
        if (accessingChildPullParser) return childPullParser.getTextLength();
        String text = parserInformation != null ? parserInformation.getText() : "";
        return text.length();
    }

    public String getEncoding() {
        // https://stax-utils.dev.java.net/nonav/javadoc/api/javax/xml/stream/XMLStreamReader.html#getEncoding()
        return null;
    }

    public Location getLocation() {
        return new EmptyOMLocation();
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

    private int processADBNameValuePair(QName simplePropertyName, Object simplePropertyValue) {
        int event = 0;
        if (processingElementText) {
            this.parserInformation.setText(ConverterUtil.convertToString(simplePropertyValue));
            finishedProcessingNameValuePair = true;
            event = XMLStreamConstants.CHARACTERS;
        } else if (!nameValuePairStartElementProcessed) {
            event = XMLStreamConstants.START_ELEMENT;
            tempParserInfo = parserInformation;
            parserInformation = new ParserInformation(simplePropertyName, ConverterUtil.convertToString(simplePropertyValue));
            nameValuePairStartElementProcessed = true;
            finishedProcessingNameValuePair = false;
            //Forcibly set nameValuePairTextProcessed to avoid a character event
            if (simplePropertyValue == null) {
                nameValuePairTextProcessed = true;
            }
        } else if (nameValuePairStartElementProcessed && !nameValuePairTextProcessed) {
            event = XMLStreamConstants.CHARACTERS;
            nameValuePairTextProcessed = true;
        } else if (nameValuePairTextProcessed) {
            event = XMLStreamConstants.END_ELEMENT;
            finishedProcessingNameValuePair = true;
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
