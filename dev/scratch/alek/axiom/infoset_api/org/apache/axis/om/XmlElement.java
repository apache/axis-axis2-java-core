package org.apache.axis.om;

import java.util.Iterator;

/**
 * Represents
 * <a href="http://www.w3.org/TR/xml-infoset/#infoitem.element">Element Information Item</a>
 * except for in-scope namespaces that can be reconstructed by visiting this element parent,
 * checking its namespaces, then grandparent and so on. For convenience there are
 * methods to resolve namespace prefix for given namespace name.
 *
 * <br />NOTE: this representaiton is optimized for streaming - iterator approach that
 * allows gradual visiting of nodes is preferred over indexed access.
 *
 * @version $Revision: 1.21 $
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */
public interface XmlElement extends XmlContainer, XmlContained, Cloneable
{

    //JDK15 covariant public XmlElement clone() throws CloneNotSupportedException
    /**
     * Method clone
     *
     * @return   an Object
     *
     * @exception   CloneNotSupportedException
     *
     */
    public Object clone() throws CloneNotSupportedException;


    //----------------------------------------------------------------------------------------------
    // Element properties

    /**
     * XML Infoset [base URI] property
     *
     * @return   a String
     *
     */
    public String getBaseUri();

    /**
     * XML Infoset [base URI] property
     *
     * @param    baseUri             a  String
     *
     */
    public void setBaseUri(String baseUri);

    /**
     * Get top most container that is either XmlDocument or XmlElement (may be event this element!!!)
     */
    public XmlContainer getRoot();

    /**
     * XML Infoset [parent] property.
     * If current element is not child of containing parent XmlElement or XmlDocument
     * then builder exception will be thrown
     */
    public XmlContainer getParent();

    /**
     * Method setParent
     *
     * @param    parent              a  XmlContainer
     *
     */
    public void setParent(XmlContainer parent);

    /**
     * Return namespace of current element
     * (XML Infoset [namespace name] and [prefix] properties combined)
     * null is only returned if
     * element was created without namespace
     * */
    public XmlNamespace getNamespace();

    /**
     * Return namespace name (XML Infoset [namespace name]property
     * or null if element has no namespace
     */
    public String getNamespaceName();

    /**
     * Set namespace ot use for theis element.
     * Note: namespace prefix is <b>always</b> ignored.
     */
    public void setNamespace(XmlNamespace namespace);

    //    public String getPrefix();
    //    public void setPrefix(String prefix);

    /**
     * XML Infoset [local name] property.
     *
     * @return   a String
     *
     */
    public String getName();

    /**
     * XML Infoset [local name] property.
     *
     * @param    name                a  String
     *
     */
    public void setName(String name);



    //----------------------------------------------------------------------------------------------
    // Attributes management


    //JDK15 Iterable
    /** Return Iterator<XmlAttribute> - null is never returned if there is no children
     then iteraotr over empty collection is returned */
    public Iterator attributes();

    /**
     * Add attribute (adds it to the XML Infoset [namespace attributes] set)
     * Attribute mist
     *
     * @param    attributeValueToAdd a  XmlAttribute
     *
     * @return   a XmlAttribute
     *
     */
    public XmlAttribute addAttribute(XmlAttribute attributeValueToAdd);
    /**
     * addAttribute
     *
     * @param    name                a  String
     * @param    value               a  String
     *
     * @return   a XmlAttribute
     *
     */
    public XmlAttribute addAttribute(String name, String value);
    /**
     * Method addAttribute
     *
     * @param    namespace           a  XmlNamespace
     * @param    name                a  String
     * @param    value               a  String
     *
     * @return   a XmlAttribute
     *
     */
    public XmlAttribute addAttribute(XmlNamespace  namespace, String name, String value);
    /**
     * Method addAttribute
     *
     * @param    type                a  String
     * @param    namespace           a  XmlNamespace
     * @param    name                a  String
     * @param    value               a  String
     *
     * @return   a XmlAttribute
     *
     */
    public XmlAttribute addAttribute(String type, XmlNamespace namespace,
                                     String name, String value);
    /**
     * Method addAttribute
     *
     * @param    type                a  String
     * @param    namespace           a  XmlNamespace
     * @param    name                a  String
     * @param    value               a  String
     * @param    specified           a  boolean
     *
     * @return   a XmlAttribute
     *
     */
    public XmlAttribute addAttribute(String type, XmlNamespace namespace,
                                     String name, String value, boolean specified);
    /**
     * Method addAttribute
     *
     * @param    attributeType       a  String
     * @param    attributePrefix     a  String
     * @param    attributeNamespace  a  String
     * @param    attributeName       a  String
     * @param    attributeValue      a  String
     * @param    specified           a  boolean
     *
     * @return   a XmlAttribute
     *
     */
    public XmlAttribute addAttribute(String attributeType,
                                     String attributePrefix,
                                     String attributeNamespace,
                                     String attributeName,
                                     String attributeValue,
                                     boolean specified);

    /**
     * Method ensureAttributeCapacity
     *
     * @param    minCapacity         an int
     *
     */
    public void ensureAttributeCapacity(int minCapacity) ;

    //TODO add attributeValue(name)
    //TODO add attributeValue(XmlNamespace, name)

    /**
     * Method getAttributeValue
     *
     * @param    attributeNamespaceNamea  String
     * @param    attributeName       a  String
     *
     * @return   a String
     *
     */
    public String getAttributeValue(String attributeNamespaceName,
                                    String attributeName);

    /**
     * Find attribute that matches given name or namespace
     * Returns null if not found.
     * Will match only attribute that have no namesapce.
     */
    public XmlAttribute attribute(String attributeName);

    /**
     * Find attribute that matches given name or namespace
     * Returns null if not found.
     * NOTE: if namespace is null in this case it will match only
     * attributes that have no namespace.
     *
     */
    public XmlAttribute attribute(XmlNamespace attributeNamespaceName,
                                  String attributeName);

    /**
     * Find attribute that matches given name or namespace
     * Returns null if not found.
     * NOTE: if namespace is null in this case it will match only
     * attributes that has no namespace.
     * @deprecated Use attribute()
     */
    public XmlAttribute findAttribute(String attributeNamespaceName,
                                      String attributeName);


    /**
     * Method hasAttributes
     *
     * @return   a boolean
     *
     */
    public boolean hasAttributes();

    /**
     * Method removeAttribute
     *
     * @param    attr                a  XmlAttribute
     *
     */
    public void removeAttribute(XmlAttribute attr);
    /**
     * Method removeAllAttributes
     *
     */
    public void removeAllAttributes();



    //----------------------------------------------------------------------------------------------
    // Namespaces management

    //JDK15 Iterable
    /** Return Iterator<XmlNamespace> - null is never returned if there is no children
     then iteraotr over empty collection is returned */
    public Iterator namespaces();

    /**
     * Create new namespace with prefix and namespace name (both must be not null)
     * and add it to current element.
     */
    public XmlNamespace declareNamespace(String prefix, String namespaceName);

    /**
     * Add namespace to current element (both prefix and namespace name must be not null)
     */
    public XmlNamespace declareNamespace(XmlNamespace namespace);

    /**
     * Method ensureNamespaceDeclarationsCapacity
     *
     * @param    minCapacity         an int
     *
     */
    public void ensureNamespaceDeclarationsCapacity(int minCapacity);

    /**
     * Method hasNamespaceDeclarations
     *
     * @return   a boolean
     *
     */
    public boolean hasNamespaceDeclarations();

    /**
     * Find namespace (will have non empty prefix) corresponding to namespace prefix
     * checking first current elemen and if not found continue in parent (if element has parent)
     * and so on.
     */
    public XmlNamespace lookupNamespaceByPrefix(String namespacePrefix);

    /**
     * Find namespace (will have non empty prefix) corresponding to namespace name
     * checking first current elemen and if not found continue in parent (if element has parent).
     * and so on.
     */
    public XmlNamespace lookupNamespaceByName(String namespaceName);

    /**
     * Create new namespace with null prefix (namespace name must be not null).
     */
    public XmlNamespace newNamespace(String namespaceName);

    /**
     * Create new namespace with prefix and namespace name (both must be not null).
     */
    public XmlNamespace newNamespace(String prefix, String namespaceName);

    /**
     * Method removeAllNamespaceDeclarations
     *
     */
    public void removeAllNamespaceDeclarations();




    //----------------------------------------------------------------------------------------------
    // Children management (element content)

    //JDK15 Iterable
    /** Return Iterator<Object>  - null is never returned if there is no children
     then iteraotr over empty collection is returned */
    public Iterator children();

    /**
     * NOTE: =child added is _not_ checked if it XmlContainer, caller must manually fix
     * parent in child by calling setParent() !!!!
     */
    public void addChild(Object child);
    /**
     * Method addChild
     *
     * @param    pos                 an int
     * @param    child               an Object
     *
     */
    public void addChild(int pos, Object child);

    /**
     * NOTE: the child element must unattached to be added
     * (it is atttached if it is XmlContainer of recognized type and getParent() != null)
     */
    public XmlElement addElement(XmlElement el);
    /**
     * Method addElement
     *
     * @param    pos                 an int
     * @param    child               a  XmlElement
     *
     * @return   a XmlElement
     *
     */
    public XmlElement addElement(int pos, XmlElement child);

    /**
     * Method addElement
     *
     * @param    name                a  String
     *
     * @return   a XmlElement
     *
     */
    public XmlElement addElement(String name);
    /**
     * Method addElement
     *
     * @param    namespace           a  XmlNamespace
     * @param    name                a  String
     *
     * @return   a XmlElement
     *
     */
    public XmlElement addElement(XmlNamespace namespace, String name);

    /**
     * Method hasChildren
     *
     * @return   a boolean
     *
     */
    public boolean hasChildren();
    /**
     * Method hasChild
     *
     * @param    child               an Object
     *
     * @return   a boolean
     *
     */
    public boolean hasChild(Object child);

    /**
     * Method ensureChildrenCapacity
     *
     * @param    minCapacity         an int
     *
     */
    public void ensureChildrenCapacity(int minCapacity);

    /**
     * @deprecated see element()
     */
    public XmlElement findElementByName(String name);
    /**
     * @deprecated see element()
     */
    public XmlElement findElementByName(String namespaceName, String name);
    /**
     * @deprecated see elements()
     */
    public XmlElement findElementByName(String name,
                                        XmlElement elementToStartLooking);
    /**
     * @deprecated see elements()
     */
    public XmlElement findElementByName(String namespaceName, String name,
                                        XmlElement elementToStartLooking);

    /**
     * return element at poition (0..count-1) or IndexOutOfBoundsException if positon incorrect
     */
    public XmlElement element(int position);

    //int count()
    //int countElement()
    //XmlElement element(String name) //return first element matching, null if not found!
    /**
     * call element(n, name) and if null was returnedthrow XmlBuilderException
     */
    public XmlElement requiredElement(XmlNamespace n, String name)  throws XmlBuilderException;

    /**
     * find first element with name and namespace (if namespace is null it is ignored in search)
     * */
    public XmlElement element(XmlNamespace n, String name);
    /**
     * find first element with name and namespace (if namespace is null it is ignored in search)
     * if no matching element is found then new element is created, appended to children, and returned
     * */
    public XmlElement element(XmlNamespace n, String name, boolean create);
    //Iterable elements(String name);
    //Iterable elements(XmlNamespace n, String name);

    /** Return all elements that has namespace and name (null is never returned but empty iteraotr) */
    public Iterable elements(XmlNamespace n, String name);



    public void insertChild(int pos, Object childToInsert);

    /**
     * Create unattached element
     */
    public XmlElement newElement(String name);
    /**
     * Method newElement
     *
     * @param    namespace           a  XmlNamespace
     * @param    name                a  String
     *
     * @return   a XmlElement
     *
     */
    public XmlElement newElement(XmlNamespace namespace, String name);
    /**
     * Method newElement
     *
     * @param    namespaceName       a  String
     * @param    name                a  String
     *
     * @return   a XmlElement
     *
     */
    public XmlElement newElement(String namespaceName, String name);

    /**
     * Removes all children - every child that was
     * implementing XmlNode will have set parent to null.
     */
    public void removeAllChildren();

    /**
     * Method removeChild
     *
     * @param    child               an Object
     *
     */
    public void removeChild(Object child);

    /**
     * Method replaceChild
     *
     * @param    newChild            an Object
     * @param    oldChild            an Object
     *
     */
    public void replaceChild(Object newChild, Object oldChild);

    //public void remove(int pos);
    //public void set(int index, Object child);

    //----------------------------------------------------------------------------------------------
    // Utility methods to make manipulating Infoset easier for typical use cases
    //JDK15 Iterable
    /** Return Iterator<XmlElement>  -  that represents all XmlElement content.
     * When used exception will be thrown if non white space children are found
     * (as expected no mixed content!).
     */
    public Iterable requiredElementContent();

    /**return children content as text - if there are any no text children throw exception  */
    public String requiredTextContent();

    //public Iterable elementsContent();
    //public Iterable elementsContent(String name);
    //public Iterable elementsContent(XmlNamespace n String name);

    //String text() //children must map to text only nodes!!!

    //selectNodes(String xpath)

    //public XmlNamespace getNamespacePrefix(String namespaceName, boolean generate)
    //public XmlNamespace findNamespace(String prefix, String namespace)

    /** it may need to reconsruct whole subtree to get count ... */
    //public int getChildrenCount();

    //public Object getFirstChild() throws XmlPullParserException;
    //public Object getNextSibling() throws XmlPullParserException;

    //public Object getChildByName(String namespace, String name);

}

