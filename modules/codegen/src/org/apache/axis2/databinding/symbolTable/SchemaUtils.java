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
package org.apache.axis2.databinding.symbolTable;

//import org.apache.axis.Constants;
//import org.apache.axis.AxisProperties;
//import org.apache.axis.utils.JavaUtils;
import org.apache.axis2.databinding.utils.support.IntHolder;
import org.apache.axis2.databinding.utils.support.QNameHolder;
import org.apache.axis2.databinding.utils.support.BooleanHolder;
import org.apache.axis2.databinding.utils.JavaUtils;
import org.apache.axis2.databinding.Constants;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.namespace.QName;
//import javax.xml.rpc.holders.BooleanHolder;
//import javax.xml.rpc.holders.IntHolder;
//import javax.xml.rpc.holders.QNameHolder;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * This class contains static utility methods specifically for schema type queries.
 * 
 * @author Rich Scheuerle  (scheu@us.ibm.com)
 */
public class SchemaUtils {

    /** Field VALUE_QNAME */
    static final QName VALUE_QNAME = Utils.findQName("", "_value");

    /**
     * This method checks mixed=true attribute is set either on
     * complexType or complexContent element.
     */
    public static boolean isMixed(Node node) {
        // Expecting a schema complexType
        if (isXSDNode(node, "complexType")) {
            String mixed = ((Element)node).getAttribute("mixed");
            if (mixed != null && mixed.length() > 0) {
                return ("true".equalsIgnoreCase(mixed) ||
                        "1".equals(mixed));
            }
            // Under the complexType there could be complexContent with
            // mixed="true"
            NodeList children = node.getChildNodes();
            
            for (int j = 0; j < children.getLength(); j++) {
                Node kid = children.item(j);
                if (isXSDNode(kid, "complexContent")) {
                    mixed = ((Element)kid).getAttribute("mixed");
                    return ("true".equalsIgnoreCase(mixed) ||
                            "1".equals(mixed));
                }
            }
        }
        return false;
    }

    public static Node getUnionNode(Node node) {
        // Expecting a schema complexType
        if (isXSDNode(node, "simpleType")) {
            // Under the simpleType there could be union
            NodeList children = node.getChildNodes();
            for (int j = 0; j < children.getLength(); j++) {
                Node kid = children.item(j);
                if (isXSDNode(kid, "union")) {
                    return kid;
                }
            }
        }
        return null;
    }
    
    public static Node getListNode(Node node) {
        // Expecting a schema simpleType 
        if (isXSDNode(node, "simpleType")) {
            // Under the simpleType there could be list
            NodeList children = node.getChildNodes();
            for (int j = 0; j < children.getLength(); j++) {
                Node kid = children.item(j);
                if (isXSDNode(kid, "list")) {
                    return kid;
                }
            }
        }
        return null;
    }

    public static boolean isSimpleTypeWithUnion(Node node) {
        return (getUnionNode(node) != null);
    }
    
  /**
   * This method checks out if the given node satisfies the 3rd condition
   * of the "wrapper" style:
   * such an element (a wrapper) must be of a complex type defined using the
   * xsd:sequence compositor and containing only elements declarations.
   * (excerpt from JAX-RPC spec 1.1 Maintenanace Review 2 Chapter 6 Section 4.1.)
   * 
   * @param node        
   * @return 
   */
  public static boolean isWrappedType(Node node) {
    
    if (node == null) {
      return false;
    }

    // If the node kind is an element, dive into it.
    if (isXSDNode(node, "element")) {
      NodeList children = node.getChildNodes();
      boolean hasComplexType = false;
      for (int j = 0; j < children.getLength(); j++) {
        Node kid = children.item(j);
        if (isXSDNode(kid, "complexType")) {
          node = kid;
          hasComplexType = true;
          break;
        }
      }
      if (!hasComplexType) {
        return false;
      }
    }

    // Expecting a schema complexType
    if (isXSDNode(node, "complexType")) {
      // Under the complexType there could be complexContent/simpleContent
      // and extension elements if this is a derived type.
      // A wrapper element must be complex-typed.
      
      NodeList children = node.getChildNodes();

      for (int j = 0; j < children.getLength(); j++) {
        Node kid = children.item(j);

        if (isXSDNode(kid, "complexContent")) {
          return false;
        } else if (isXSDNode(kid, "simpleContent")) {
          return false;
        }
      }

      // Under the complexType there may be choice, sequence, group and/or all nodes.
      // (There may be other #text nodes, which we will ignore).
      // The complex type of a wrapper element must have only sequence 
      // and again element declarations in the sequence. 
      children = node.getChildNodes();
      int len =  children.getLength();
      for (int j = 0; j < len; j++) {
          Node kid = children.item(j);
          String localName = kid.getLocalName();
          if (localName != null &&
              Constants.isSchemaXSD(kid.getNamespaceURI())) {
              if (localName.equals("sequence")) {
                  Node sequenceNode = kid;
                  NodeList sequenceChildren = sequenceNode.getChildNodes();
                  int sequenceLen = sequenceChildren.getLength();
                  for (int k = 0; k < sequenceLen; k++) {
                      Node sequenceKid = sequenceChildren.item(k);
                      String sequenceLocalName = sequenceKid.getLocalName();
                      if (sequenceLocalName != null &&
                          Constants.isSchemaXSD(sequenceKid.getNamespaceURI())) {
                          // allow choice with element children
                          if (sequenceLocalName.equals("choice")) {
                              Node choiceNode = sequenceKid;
                              NodeList choiceChildren = choiceNode.getChildNodes();
                              int choiceLen = choiceChildren.getLength();
                              for (int l = 0; l < choiceLen; l++) {
                                  Node choiceKid = choiceChildren.item(l);
                                  String choiceLocalName = choiceKid.getLocalName();
                                  if (choiceLocalName != null &&
                                      Constants.isSchemaXSD(choiceKid.getNamespaceURI())) {
                                      if (!choiceLocalName.equals("element")) {
                                          return false;
                                      }
                                  }
                              }
                          }
                          else
                          if (!sequenceLocalName.equals("element")) {
                              return false;
                          }
                      }
                  }
                  return true;
              } else {
                  return false;
              }
          }
      }
    } 
    // allows void type
    return true;
  }
  
    /**
     * If the specified node represents a supported JAX-RPC complexType or
     * simpleType, a Vector is returned which contains ElementDecls for the
     * child element names.
     * If the element is a simpleType, an ElementDecls is built representing
     * the restricted type with the special name "value".
     * If the element is a complexType which has simpleContent, an ElementDecl
     * is built representing the extended type with the special name "value".
     * This method does not return attribute names and types
     * (use the getContainedAttributeTypes)
     * If the specified node is not a supported
     * JAX-RPC complexType/simpleType/element null is returned.
     * 
     * @param node        
     * @param symbolTable 
     * @return 
     */
    public static Vector getContainedElementDeclarations(Node node,
                                                         SymbolTable symbolTable) {

        if (node == null) {
            return null;
        }

        // If the node kind is an element, dive into it.
        if (isXSDNode(node, "element")) {
            NodeList children = node.getChildNodes();

            for (int j = 0; j < children.getLength(); j++) {
                Node kid = children.item(j);

                if (isXSDNode(kid, "complexType")) {
                    node = kid;

                    break;
                }
            }
        }

        // Expecting a schema complexType or simpleType
        if (isXSDNode(node, "complexType")) {

            // Under the complexType there could be complexContent/simpleContent
            // and extension elements if this is a derived type.  Skip over these.
            NodeList children = node.getChildNodes();
            Node complexContent = null;
            Node simpleContent = null;
            Node extension = null;

            for (int j = 0; j < children.getLength(); j++) {
                Node kid = children.item(j);

                if (isXSDNode(kid, "complexContent")) {
                    complexContent = kid;

                    break;    // REMIND: should this be here or on either branch?
                } else if (isXSDNode(kid, "simpleContent")) {
                    simpleContent = kid;
                }
            }

            if (complexContent != null) {
                children = complexContent.getChildNodes();

                for (int j = 0;
                     (j < children.getLength()) && (extension == null);
                     j++) {
                    Node kid = children.item(j);

                    if (isXSDNode(kid, "extension")
                            || isXSDNode(kid, "restriction")) {
                        extension = kid;
                    }
                }
            }

            if (simpleContent != null) {
                children = simpleContent.getChildNodes();

                int len =  children.getLength();
                for (int j = 0;
                     (j < len) && (extension == null);
                     j++) {
                    Node kid = children.item(j);
                    String localName = kid.getLocalName();

                    if ((localName != null)
                        && (localName.equals("extension") || localName.equals("restriction"))
                        && Constants.isSchemaXSD(kid.getNamespaceURI())) {
                        
                        // get the type of the extension/restriction from the "base" attribute
                        QName extendsOrRestrictsType =
                                Utils.getTypeQName(children.item(j),
                                        new BooleanHolder(), false);

                        // Return an element declaration with a fixed name
                        // ("value") and the correct type.
                        Vector v = new Vector();
                        ElementDecl elem = new ElementDecl(symbolTable.getTypeEntry(extendsOrRestrictsType, false), VALUE_QNAME);
                        v.add(elem);

                        return v;
                    }
                }
            }

            if (extension != null) {
                node = extension;    // Skip over complexContent and extension
            }

            // Under the complexType there may be choice, sequence, group and/or all nodes.
            // (There may be other #text nodes, which we will ignore).
            children = node.getChildNodes();

            Vector v = new Vector();
            int len = children.getLength();
            for (int j = 0; j < len; j++) {
                Node kid = children.item(j);
                String localName = kid.getLocalName();
                if (localName != null &&
                    Constants.isSchemaXSD(kid.getNamespaceURI())) {
                    if (localName.equals("sequence")) {
                        v.addAll(processSequenceNode(kid, symbolTable));
                    } else if (localName.equals("all")) {
                        v.addAll(processAllNode(kid, symbolTable));
                    } else if (localName.equals("choice")) {
                        v.addAll(processChoiceNode(kid, symbolTable));
                    } else if (localName.equals("group")) {
                        v.addAll(processGroupNode(kid, symbolTable));
                    }
                }
            }

            return v;
        } else if (isXSDNode(node, "group")) {
            NodeList children = node.getChildNodes();
            Vector v = new Vector();
            int len = children.getLength();
            for (int j = 0; j < len; j++) {
                Node kid = children.item(j);
                String localName = kid.getLocalName();
                if (localName != null &&
                    Constants.isSchemaXSD(kid.getNamespaceURI())) {
                    if (localName.equals("sequence")) {
                        v.addAll(processSequenceNode(kid, symbolTable));
                    } else if (localName.equals("all")) {
                        v.addAll(processAllNode(kid, symbolTable));
                    } else if (localName.equals("choice")) {
                        v.addAll(processChoiceNode(kid, symbolTable));
                    }
                }
            }
            return v;
        } else {

            // This may be a simpleType, return the type with the name "value"
            QName[] simpleQName = getContainedSimpleTypes(node);

            if (simpleQName != null) {
                Vector v = null;

                for (int i = 0; i < simpleQName.length; i++) {

                    Type simpleType = symbolTable.getType(simpleQName[i]);

                    if (simpleType != null) {
                        if (v == null) {
                            v = new Vector();
                        }

                        QName qname = null;
                        if (simpleQName.length > 1) {
                            qname = new QName("", simpleQName[i].getLocalPart() + "Value");
                        } else {
                            qname = new QName("", "value");
                        }

                        v.add(new ElementDecl(simpleType, qname));
                    }
                }

                return v;
            }
        }

        return null;
    }

    /**
     * Invoked by getContainedElementDeclarations to get the child element types
     * and child element names underneath a Choice Node
     * 
     * @param choiceNode  
     * @param symbolTable 
     * @return 
     */
    private static Vector processChoiceNode(Node choiceNode,
                                            SymbolTable symbolTable) {

        Vector v = new Vector();
        NodeList children = choiceNode.getChildNodes();
        int len = children.getLength();
        for (int j = 0; j < len; j++) {
            Node kid = children.item(j);
            String localName = kid.getLocalName();
            if (localName != null &&
                Constants.isSchemaXSD(kid.getNamespaceURI())) {
                if (localName.equals("choice")) {
                    v.addAll(processChoiceNode(kid, symbolTable));
                } else if (localName.equals("sequence")) {
                    v.addAll(processSequenceNode(kid, symbolTable));
                } else if (localName.equals("group")) {
                    v.addAll(processGroupNode(kid, symbolTable));
                } else if (localName.equals("element")) {
                    ElementDecl elem = processChildElementNode(kid, 
                                                               symbolTable);
                    
                    if (elem != null) {
                        // XXX: forces minOccurs="0" so that a null choice
                        // element can be serialized ok.
                        elem.setMinOccursIs0(true);

                        v.add(elem);
                    }
                } else if (localName.equals("any")) {
                    // Represent this as an element named any of type any type.
                    // This will cause it to be serialized with the element
                    // serializer.
                    Type type = symbolTable.getType(Constants.XSD_ANY);
                    ElementDecl elem = new ElementDecl(type,
                            Utils.findQName("",
                                    "any"));

                    elem.setAnyElement(true);
                    v.add(elem);
                }
            }
        }

        return v;
    }

    /**
     * Returns named child node.
     * 
     * @param parentNode Parent node.
     * @param name Element name of child node to return.
     */
    private static Node getChildByName(Node parentNode, String name) throws DOMException {
        if (parentNode == null) return null;
        NodeList children = parentNode.getChildNodes();
        if (children != null) {
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                if (child != null) {
                    if (child.getNodeName() != null && name.equals(child.getNodeName())) {
                        return child;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Returns all textual nodes of a subnode defined by a parent node
     * and a path of element names to that subnode.
     * 
     * @param root Parent node.
     * @param path Path of element names to text of interest, delimited by "/". 
     */
    public static String getTextByPath(Node root, String path) throws DOMException {
        StringTokenizer st = new StringTokenizer(path, "/");
        Node node = root;
        while (st.hasMoreTokens()) {
            String elementName = st.nextToken();
            Node child = getChildByName(node, elementName);
            if (child == null)
                throw new DOMException(DOMException.NOT_FOUND_ERR, "could not find " + elementName);
            node = child;
        }
    
        // should have found the node
        String text = "";
        NodeList children = node.getChildNodes();
        if (children != null) {
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                if (child != null) {
                    if (child.getNodeName() != null
                            && (child.getNodeName().equals("#text")
                            || child.getNodeName().equals("#cdata-section"))) {
                        text += child.getNodeValue();
                    }
                }
            }
        }
        return text;
    }

    /**
     * Returns the complete text of the child xsd:annotation/xsd:documentation 
     * element from the provided node.  Only the first annotation element and 
     * the first documentation element in the annotation element will be used.
     * 
     * @param typeNode Parent node.
     */
    public static String getAnnotationDocumentation(Node typeNode) {
        Node annotationNode = typeNode.getFirstChild();
        while (annotationNode != null) {
            if (isXSDNode(annotationNode, "annotation")) {
                break;
            }
            annotationNode = annotationNode.getNextSibling();
        }
        Node documentationNode;
        if (annotationNode != null) {
            documentationNode = annotationNode.getFirstChild();
            while (documentationNode != null) {
                if (isXSDNode(documentationNode, "documentation")) {
                    break;
                }
                documentationNode = documentationNode.getNextSibling();
            }
        } else {
            documentationNode = null;
        }
        
        // should have found the node if it exists
        String text = "";
        if (documentationNode != null) {
            NodeList children = documentationNode.getChildNodes();
            if (children != null) {
                for (int i = 0; i < children.getLength(); i++) {
                    Node child = children.item(i);
                    if (child != null) {
                        if (child.getNodeName() != null
                                && (child.getNodeName().equals("#text")
                                || child.getNodeName().equals("#cdata-section"))) {
                            text += child.getNodeValue();
                        }
                    }
                }
            }
        }
        return text;
    }

    /**
     * Invoked by getContainedElementDeclarations to get the child element types
     * and child element names underneath a Sequence Node
     * 
     * @param sequenceNode 
     * @param symbolTable  
     * @return 
     */
    private static Vector processSequenceNode(Node sequenceNode,
                                              SymbolTable symbolTable) {

        Vector v = new Vector();
        NodeList children = sequenceNode.getChildNodes();
        int len = children.getLength();
        for (int j = 0; j < len; j++) {
            Node kid = children.item(j);
            String localName = kid.getLocalName();

            if (localName != null &&
                Constants.isSchemaXSD(kid.getNamespaceURI())) {
                if (localName.equals("choice")) {
                    v.addAll(processChoiceNode(kid, symbolTable));
                } else if (localName.equals("sequence")) {
                    v.addAll(processSequenceNode(kid, symbolTable));
                } else if (localName.equals("group")) {
                    v.addAll(processGroupNode(kid, symbolTable));
                } else if (localName.equals("any")) {
                    // Represent this as an element named any of type any type.
                    // This will cause it to be serialized with the element
                    // serializer.
                    Type type = symbolTable.getType(Constants.XSD_ANY);
                    ElementDecl elem = new ElementDecl(type,
                            Utils.findQName("",
                                    "any"));

                    elem.setAnyElement(true);
                    v.add(elem);
                } else if (localName.equals("element")) {
                    ElementDecl elem = processChildElementNode(kid,
                                                               symbolTable);

                    if (elem != null) {
                        v.add(elem);
                    }
                }
            }
        }

        return v;
    }

    /**
     * Invoked by getContainedElementDeclarations to get the child element types
     * and child element names underneath a group node. If a ref attribute is 
     * specified, only the referenced group element is returned.
     * 
     * @param groupNode   
     * @param symbolTable 
     * @return 
     */
    private static Vector processGroupNode(Node groupNode,
                                           SymbolTable symbolTable) {

        Vector v = new Vector();
        if (groupNode.getAttributes().getNamedItem("ref") == null) {
            NodeList children = groupNode.getChildNodes();
            int len = children.getLength();
            for (int j = 0; j < len; j++) {
                Node kid = children.item(j);
                String localName = kid.getLocalName();
                if (localName != null &&
                    Constants.isSchemaXSD(kid.getNamespaceURI())) {
                    if (localName.equals("choice")) {
                        v.addAll(processChoiceNode(kid, symbolTable));
                    } else if (localName.equals("sequence")) {
                        v.addAll(processSequenceNode(kid, symbolTable));
                    } else if (localName.equals("all")) {
                        v.addAll(processAllNode(kid, symbolTable));
                    }
                }
            }
        } else {
            QName nodeName = Utils.getNodeNameQName(groupNode);
            QName nodeType = Utils.getTypeQName(groupNode, new BooleanHolder(), false);
            // The value of the second argument is 'false' since global model group
            // definitions are always represented by objects whose type is
            // assignment compatible with 'org.apache.axis.wsdl.symbolTable.Type'.
            Type type = (Type) symbolTable.getTypeEntry(nodeType, false);

            if (type != null) {
                v.add(new ElementDecl(type, nodeName));
            }
        }
        return v;
    }


    /**
     * Invoked by getContainedElementDeclarations to get the child element types
     * and child element names underneath an all node.
     * 
     * @param allNode     
     * @param symbolTable 
     * @return 
     */
    private static Vector processAllNode(Node allNode,
                                         SymbolTable symbolTable) {

        Vector v = new Vector();
        NodeList children = allNode.getChildNodes();

        for (int j = 0; j < children.getLength(); j++) {
            Node kid = children.item(j);

            if (isXSDNode(kid, "element")) {
                ElementDecl elem = processChildElementNode(kid, symbolTable);

                if (elem != null) {
                    v.add(elem);
                }
            }
        }

        return v;
    }

    /**
     * Invoked by getContainedElementDeclarations to get the child element type
     * and child element name for a child element node.
     * <p/>
     * If the specified node represents a supported JAX-RPC child element,
     * we return an ElementDecl containing the child element name and type.
     * 
     * @param elementNode 
     * @param symbolTable 
     * @return 
     */
    private static ElementDecl processChildElementNode(Node elementNode,
                                                       SymbolTable symbolTable) {

        // Get the name qnames.
        QName nodeName = Utils.getNodeNameQName(elementNode);
        BooleanHolder forElement = new BooleanHolder();
        String comments = null;
        comments = getAnnotationDocumentation(elementNode);
        
        // The type qname is used to locate the TypeEntry, which is then
        // used to retrieve the proper java name of the type.
        QName nodeType = Utils.getTypeQName(elementNode, forElement, false);
        TypeEntry type = symbolTable.getTypeEntry(nodeType,
                forElement.value);

        // An element inside a complex type is either qualified or unqualified.
        // If the ref= attribute is used, the name of the ref'd element is used
        // (which must be a root element).  If the ref= attribute is not
        // used, the name of the element is unqualified.
        if (!forElement.value) {

            // check the Form (or elementFormDefault) attribute of this node to
            // determine if it should be namespace quailfied or not.
            String form = Utils.getAttribute(elementNode, "form");

            if ((form != null) && form.equals("unqualified")) {

                // Unqualified nodeName
                nodeName = Utils.findQName("", nodeName.getLocalPart());
            } else if (form == null) {

                // check elementFormDefault on schema element
                String def = Utils.getScopedAttribute(elementNode,
                        "elementFormDefault");

                if ((def == null) || def.equals("unqualified")) {

                    // Unqualified nodeName
                    nodeName = Utils.findQName("", nodeName.getLocalPart());
                }
            }
        }

        if (type != null) {
            ElementDecl elem = new ElementDecl(type, nodeName);
            elem.setDocumentation(comments);
            String minOccurs = Utils.getAttribute(elementNode,
                    "minOccurs");

            if ((minOccurs != null) && minOccurs.equals("0")) {
                elem.setMinOccursIs0(true);
            }

            String maxOccurs = Utils.getAttribute(elementNode, "maxOccurs");
            if (maxOccurs != null && maxOccurs.equals("unbounded")) {
                    elem.setMaxOccursIsUnbounded(true);
            }
            elem.setNillable(
                    JavaUtils.isTrueExplicitly(
                            Utils.getAttribute(elementNode, "nillable")));

            String useValue = Utils.getAttribute(elementNode, "use");

            if (useValue != null) {
                elem.setOptional(useValue.equalsIgnoreCase("optional"));
            }

            return elem;
        }

        return null;
    }

    /**
     * Returns the WSDL2Java QName for the anonymous type of the element
     * or null.
     * 
     * @param node 
     * @return 
     */
    public static QName getElementAnonQName(Node node) {

        if (isXSDNode(node, "element")) {
            NodeList children = node.getChildNodes();

            for (int j = 0; j < children.getLength(); j++) {
                Node kid = children.item(j);

                if (isXSDNode(kid, "complexType")
                        || isXSDNode(kid, "simpleType")) {
                    return Utils.getNodeNameQName(kid);
                }
            }
        }

        return null;
    }

    /**
     * Returns the WSDL2Java QName for the anonymous type of the attribute
     * or null.
     * 
     * @param node 
     * @return 
     */
    public static QName getAttributeAnonQName(Node node) {

        if (isXSDNode(node, "attribute")) {
            NodeList children = node.getChildNodes();

            for (int j = 0; j < children.getLength(); j++) {
                Node kid = children.item(j);

                if (isXSDNode(kid, "complexType")
                        || isXSDNode(kid, "simpleType")) {
                    return Utils.getNodeNameQName(kid);
                }
            }
        }

        return null;
    }

    /**
     * If the specified node is a simple type or contains simpleContent, return true
     * 
     * @param node 
     * @return 
     */
    public static boolean isSimpleTypeOrSimpleContent(Node node) {

        if (node == null) {
            return false;
        }

        // If the node kind is an element, dive into it.
        if (isXSDNode(node, "element")) {
            NodeList children = node.getChildNodes();

            for (int j = 0; j < children.getLength(); j++) {
                Node kid = children.item(j);

                if (isXSDNode(kid, "complexType")) {
                    node = kid;

                    break;
                } else if (isXSDNode(kid, "simpleType")) {
                    return true;
                }
            }
        }

        // Expecting a schema complexType or simpleType
        if (isXSDNode(node, "simpleType")) {
            return true;
        }

        if (isXSDNode(node, "complexType")) {

            // Under the complexType there could be complexContent/simpleContent
            // and extension elements if this is a derived type.  Skip over these.
            NodeList children = node.getChildNodes();
            Node complexContent = null;
            Node simpleContent = null;

            for (int j = 0; j < children.getLength(); j++) {
                Node kid = children.item(j);

                if (isXSDNode(kid, "complexContent")) {
                    complexContent = kid;

                    break;
                } else if (isXSDNode(kid, "simpleContent")) {
                    simpleContent = kid;
                }
            }

            if (complexContent != null) {
                return false;
            }

            if (simpleContent != null) {
                return true;
            }
        }

        return false;
    }

    /**
     * Test whether <tt>node</tt> is not null, belongs to the XML
     * Schema namespace, and has a localName that matches
     * <tt>schemaLocalName</tt>
     * <p/>
     * This can be used to determine that a given Node defines a
     * schema "complexType" "element" and so forth.
     * 
     * @param node            a <code>Node</code> value
     * @param schemaLocalName a <code>String</code> value
     * @return true if the node is matches the name in the schema namespace.
     */
    private static boolean isXSDNode(Node node, String schemaLocalName) {
        if (node == null) {
            return false;
        }
        String localName = node.getLocalName();
        if (localName == null) {
            return false;
        }
        return (localName.equals(schemaLocalName) &&
                Constants.isSchemaXSD(node.getNamespaceURI()));
    }

    /**
     * Look for the base type of node iff node is a complex type that has been
     * derived by restriction; otherwise return null.
     * 
     * @param node        
     * @param symbolTable 
     * @return 
     */
    public static TypeEntry getComplexElementRestrictionBase(Node node,
                                                             SymbolTable symbolTable) {

        if (node == null) {
            return null;
        }

        // If the node kind is an element, dive into it.
        if (isXSDNode(node, "element")) {
            NodeList children = node.getChildNodes();
            Node complexNode = null;

            for (int j = 0;
                 (j < children.getLength()) && (complexNode == null); j++) {
                if (isXSDNode(children.item(j), "complexType")) {
                    complexNode = children.item(j);
                    node = complexNode;
                }
            }
        }

        // Expecting a schema complexType
        if (isXSDNode(node, "complexType")) {

            // Under the complexType there could be should be a complexContent &
            // restriction elements if this is a derived type.
            NodeList children = node.getChildNodes();
            Node content = null;
            Node restriction = null;

            for (int j = 0; (j < children.getLength()) && (content == null);
                 j++) {
                Node kid = children.item(j);

                if (isXSDNode(kid, "complexContent")) {
                    content = kid;
                }
            }

            if (content != null) {
                children = content.getChildNodes();

                for (int j = 0;
                     (j < children.getLength()) && (restriction == null);
                     j++) {
                    Node kid = children.item(j);

                    if (isXSDNode(kid, "restriction")) {
                        restriction = kid;
                    }
                }
            }

            if (restriction == null) {
                return null;
            } else {

                // Get the QName of the extension base
                QName restrictionType = Utils.getTypeQName(restriction,
                        new BooleanHolder(),
                        false);

                if (restrictionType == null) {
                    return null;
                } else {

                    // Return associated Type
                    return symbolTable.getType(restrictionType);
                }
            }
        } else {
            return null;
        }
    }

    /**
     * If the specified node represents a supported JAX-RPC complexType/element
     * which extends another complexType.  The Type of the base is returned.
     * 
     * @param node        
     * @param symbolTable 
     * @return 
     */
    public static TypeEntry getComplexElementExtensionBase(Node node,
                                                           SymbolTable symbolTable) {

        if (node == null) {
            return null;
        }

        TypeEntry cached = (TypeEntry) symbolTable.node2ExtensionBase.get(node);

        if (cached != null) {
            return cached;    // cache hit
        }

        // If the node kind is an element, dive into it.
        if (isXSDNode(node, "element")) {
            NodeList children = node.getChildNodes();
            Node complexNode = null;

            for (int j = 0;
                 (j < children.getLength()) && (complexNode == null); j++) {
                if (isXSDNode(children.item(j), "complexType")) {
                    complexNode = children.item(j);
                    node = complexNode;
                }
            }
        }

        // Expecting a schema complexType
        if (isXSDNode(node, "complexType")) {

            // Under the complexType there could be should be a complexContent &
            // extension elements if this is a derived type.
            NodeList children = node.getChildNodes();
            Node content = null;
            Node extension = null;

            for (int j = 0; (j < children.getLength()) && (content == null);
                 j++) {
                Node kid = children.item(j);

                if (isXSDNode(kid, "complexContent")
                        || isXSDNode(kid, "simpleContent")) {
                    content = kid;
                }
            }

            if (content != null) {
                children = content.getChildNodes();

                for (int j = 0;
                     (j < children.getLength()) && (extension == null);
                     j++) {
                    Node kid = children.item(j);

                    if (isXSDNode(kid, "extension")) {
                        extension = kid;
                    }
                }
            }

            if (extension == null) {
                cached = null;
            } else {

                // Get the QName of the extension base
                QName extendsType = Utils.getTypeQName(extension,
                        new BooleanHolder(),
                        false);

                if (extendsType == null) {
                    cached = null;
                } else {

                    // Return associated Type
                    cached = symbolTable.getType(extendsType);
                }
            }
        }

        symbolTable.node2ExtensionBase.put(node, cached);

        return cached;
    }

    /**
     * If the specified node represents a 'normal' non-enumeration simpleType,
     * the QName of the simpleType base is returned.
     * 
     * @param node 
     * @return 
     */
    public static QName getSimpleTypeBase(Node node) {

        QName[] qname = getContainedSimpleTypes(node);

        if ((qname != null) && (qname.length > 0)) {
            return qname[0];
        }

        return null;
    }

    /**
     * Method getContainedSimpleTypes
     * 
     * @param node 
     * @return 
     */
    public static QName[] getContainedSimpleTypes(Node node) {

        QName[] baseQNames = null;

        if (node == null) {
            return null;
        }

        // If the node kind is an element, dive into it.
        if (isXSDNode(node, "element")) {
            NodeList children = node.getChildNodes();

            for (int j = 0; j < children.getLength(); j++) {
                if (isXSDNode(children.item(j), "simpleType")) {
                    node = children.item(j);

                    break;
                }
            }
        }

        // Get the node kind, expecting a schema simpleType
        if (isXSDNode(node, "simpleType")) {

            // Under the simpleType there should be a restriction.
            // (There may be other #text nodes, which we will ignore).
            NodeList children = node.getChildNodes();
            Node restrictionNode = null;
            Node unionNode = null;

            for (int j = 0;
                 (j < children.getLength()) && (restrictionNode == null);
                 j++) {
                if (isXSDNode(children.item(j), "restriction")) {
                    restrictionNode = children.item(j);
                } else if (isXSDNode(children.item(j), "union")) {
                    unionNode = children.item(j);
                }
            }

            // The restriction node indicates the type being restricted
            // (the base attribute contains this type).
            if (restrictionNode != null) {
                baseQNames = new QName[1];
                baseQNames[0] = Utils.getTypeQName(restrictionNode,
                        new BooleanHolder(), false);
            }

            if (unionNode != null) {
                baseQNames = Utils.getMemberTypeQNames(unionNode);
            }

            // Look for enumeration elements underneath the restriction node
            if ((baseQNames != null) && (restrictionNode != null)
                    && (unionNode != null)) {
                NodeList enums = restrictionNode.getChildNodes();

                for (int i = 0; i < enums.getLength(); i++) {
                    if (isXSDNode(enums.item(i), "enumeration")) {

                        // Found an enumeration, this isn't a
                        // 'normal' simple type.
                        return null;
                    }
                }
            }
        }

        return baseQNames;
    }

    /**
     * Returns the contained restriction or extension node underneath
     * the specified node.  Returns null if not found
     * 
     * @param node 
     * @return 
     */
    public static Node getRestrictionOrExtensionNode(Node node) {

        Node re = null;

        if (node == null) {
            return re;
        }

        // If the node kind is an element, dive into it.
        if (isXSDNode(node, "element")) {
            NodeList children = node.getChildNodes();

            for (int j = 0; j < children.getLength(); j++) {
                Node n = children.item(j);

                if (isXSDNode(n, "simpleType") || isXSDNode(n, "complexType")
                        || isXSDNode(n, "simpleContent")) {
                    node = n;

                    break;
                }
            }
        }

        // Get the node kind, expecting a schema simpleType
        if (isXSDNode(node, "simpleType") || isXSDNode(node, "complexType")) {

            // Under the complexType there could be a complexContent.
            NodeList children = node.getChildNodes();
            Node complexContent = null;

            if (node.getLocalName().equals("complexType")) {
                for (int j = 0;
                     (j < children.getLength()) && (complexContent == null);
                     j++) {
                    Node kid = children.item(j);

                    if (isXSDNode(kid, "complexContent")
                            || isXSDNode(kid, "simpleContent")) {
                        complexContent = kid;
                    }
                }

                node = complexContent;
            }

            // Now get the extension or restriction node
            if (node != null) {
                children = node.getChildNodes();

                for (int j = 0; (j < children.getLength()) && (re == null);
                     j++) {
                    Node kid = children.item(j);

                    if (isXSDNode(kid, "extension")
                            || isXSDNode(kid, "restriction")) {
                        re = kid;
                    }
                }
            }
        }

        return re;
    }

    /**
     * If the specified node represents an array encoding of one of the following
     * forms, then return the qname repesenting the element type of the array.
     * 
     * @param node is the node
     * @param dims is the output value that contains the number of dimensions if return is not null
     * @param itemQName will end up containing the "inner" QName for a
     *                       wrapped literal array
     * @return QName or null
     */
    public static QName getArrayComponentQName(Node node,
                                               IntHolder dims,
                                               QNameHolder itemQName,
                                               SymbolTable symbolTable) {

        dims.value = 1;    // assume 1 dimension

        QName qName = getCollectionComponentQName(node, itemQName);

        if (qName == null) {
            qName = getArrayComponentQName_JAXRPC(node, dims, symbolTable);
        }

        return qName;
    }

    /**
     * If the specified node represents an element that references a collection
     * then return the qname repesenting the component of the collection.
     * <p/>
     * <xsd:element name="alias" type="xsd:string" maxOccurs="unbounded"/>
     * returns qname for"xsd:string"
     * <p/>
     * <xsd:complexType>
     *  <xsd:sequence>
     *   <xsd:element name="alias" type="xsd:string" maxOccurs="unbounded"/>
     *  </xsd:sequence>
     * </xsd:complexType>
     * returns qname for"xsd:string"
     * <p/>
     * <xsd:element ref="alias"  maxOccurs="unbounded"/>
     * returns qname for "alias"
     * 
     * @param node is the Node
     * @return QName of the compoent of the collection
     */
    public static QName getCollectionComponentQName(Node node,
                                                    QNameHolder itemQName) {
        // If we're going to turn "wrapped" arrays into types such that
        // <complexType><sequence>
        //   <element name="foo" type="xs:string" maxOccurs="unbounded"/>
        // </sequence></complexType>
        // becomes just "String []", we need to keep track of the inner
        // element name "foo" in metadata... This flag indicates whether to
        // do so.
        boolean storeComponentQName = false;

        if (node == null) {
            return null;
        }

        if (itemQName != null && isXSDNode(node, "complexType")) {
            // If this complexType is a sequence of exactly one element
            // we will continue processing below using that element, and
            // let the type checking logic determine if this is an array
            // or not.
            Node sequence = SchemaUtils.getChildByName(node, "sequence");
            if (sequence == null) {
                return null;
            }
            NodeList children = sequence.getChildNodes();
            Node element = null;
            for (int i = 0; i < children.getLength(); i++) {
                if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    if (element == null) {
                        element = children.item(i);
                    } else {
                        return null;
                    }
                }
            }
            if (element == null) {
                return null;
            }

            // OK, exactly one element child of <sequence>,
            // continue the processing using that element ...
            node = element;
            storeComponentQName = true;
        }

        // If the node kind is an element, dive to get its type.
        if (isXSDNode(node, "element")) {

            // Compare the componentQName with the name of the
            // full name.  If different, return componentQName
            BooleanHolder forElement = new BooleanHolder();
            QName componentTypeQName = Utils.getTypeQName(node,
                                                          forElement,
                                                          true);

            if (componentTypeQName != null) {
                QName fullQName = Utils.getTypeQName(node, forElement, false);

                if (!componentTypeQName.equals(fullQName)) {
                    if (storeComponentQName) {
                        String name = Utils.getAttribute(node, "name");
                        if (name != null) {
                            // check elementFormDefault on schema element
                            String def = Utils.getScopedAttribute(node,
                                    "elementFormDefault");
                            String namespace = "";
                            if ((def != null) && def.equals("qualified")) {
                                 namespace = Utils.getScopedAttribute(node, "targetNamespace");
                            }
                            itemQName.value = new QName(namespace, name);
                        }
                    }
                    return componentTypeQName;
                }
            }
        }

        return null;
    }

    /**
     * If the specified node represents an array encoding of one of the following
     * forms, then return the qname repesenting the element type of the array.
     * 
     * @param node is the node
     * @param dims is the output value that contains the number of dimensions if return is not null
     * @return QName or null
     *         <p/>
     *         JAX-RPC Style 2:
     *         <xsd:complexType name="hobbyArray">
     *         <xsd:complexContent>
     *         <xsd:restriction base="soapenc:Array">
     *         <xsd:attribute ref="soapenc:arrayType" wsdl:arrayType="xsd:string[]"/>
     *         </xsd:restriction>
     *         </xsd:complexContent>
     *         </xsd:complexType>
     *         <p/>
     *         JAX-RPC Style 3:
     *         <xsd:complexType name="petArray">
     *         <xsd:complexContent>
     *         <xsd:restriction base="soapenc:Array">
     *         <xsd:sequence>
     *         <xsd:element name="alias" type="xsd:string" maxOccurs="unbounded"/>
     *         </xsd:sequence>
     *         </xsd:restriction>
     *         </xsd:complexContent>
     *         </xsd:complexType>
     */
    private static QName getArrayComponentQName_JAXRPC(Node node,
                                                       IntHolder dims,
                                                       SymbolTable symbolTable)
    {

        dims.value = 0;    // Assume 0

        if (node == null) {
            return null;
        }

        // If the node kind is an element, dive into it.
        if (isXSDNode(node, "element")) {
            NodeList children = node.getChildNodes();

            for (int j = 0; j < children.getLength(); j++) {
                Node kid = children.item(j);

                if (isXSDNode(kid, "complexType")) {
                    node = kid;

                    break;
                }
            }
        }

        // Get the node kind, expecting a schema complexType
        if (isXSDNode(node, "complexType")) {

            // Under the complexType there should be a complexContent.
            // (There may be other #text nodes, which we will ignore).
            NodeList children = node.getChildNodes();
            Node complexContentNode = null;

            for (int j = 0; j < children.getLength(); j++) {
                Node kid = children.item(j);

                if (isXSDNode(kid, "complexContent")
                        || isXSDNode(kid, "simpleContent")) {
                    complexContentNode = kid;

                    break;
                }
            }

            // Under the complexContent there should be a restriction.
            // (There may be other #text nodes, which we will ignore).
            Node restrictionNode = null;

            if (complexContentNode != null) {
                children = complexContentNode.getChildNodes();

                for (int j = 0; j < children.getLength(); j++) {
                    Node kid = children.item(j);

                    if (isXSDNode(kid, "restriction")) {
                        restrictionNode = kid;

                        break;
                    }
                }
            }

            // The restriction node must have a base of soapenc:Array.
            QName baseType = null;

            if (restrictionNode != null) {
                baseType = Utils.getTypeQName(restrictionNode,
                        new BooleanHolder(), false);

                if (baseType != null) {
                    if (!baseType.getLocalPart().equals("Array") ||
                            !Constants.isSOAP_ENC(baseType.getNamespaceURI())) {
                        if (!symbolTable.arrayTypeQNames.contains(baseType)) {
                            baseType = null; // Did not find base=soapenc:Array
                        }
                    }
                }
            }
            
            // Under the restriction there should be an attribute OR a sequence/all group node.
            // (There may be other #text nodes, which we will ignore).
            Node groupNode = null;
            Node attributeNode = null;

            if (baseType != null) {
                children = restrictionNode.getChildNodes();

                for (int j = 0; (j < children.getLength())
                        && (groupNode == null)
                        && (attributeNode == null); j++) {
                    Node kid = children.item(j);

                    if (isXSDNode(kid, "sequence") || isXSDNode(kid, "all")) {
                        groupNode = kid;

                        if (groupNode.getChildNodes().getLength() == 0) {

                            // This covers the rather odd but legal empty sequence.
                            // <complexType name="ArrayOfString">
                            // <complexContent>
                            // <restriction base="soapenc:Array">
                            // <sequence/>
                            // <attribute ref="soapenc:arrayType" wsdl:arrayType="string[]"/>
                            // </restriction>
                            // </complexContent>
                            // </complexType>
                            groupNode = null;
                        }
                    }

                    if (isXSDNode(kid, "attribute")) {

                        // If the attribute node does not have ref="soapenc:arrayType"
                        // then keep looking.
                        BooleanHolder isRef = new BooleanHolder();
                        QName refQName = Utils.getTypeQName(kid, isRef,
                                false);

                        if ((refQName != null) && isRef.value
                                && refQName.getLocalPart().equals("arrayType")
                                && Constants.isSOAP_ENC(
                                        refQName.getNamespaceURI())) {
                            attributeNode = kid;
                        }
                    }
                }
            }

            // If there is an attribute node, look at wsdl:arrayType to get the element type
            if (attributeNode != null) {
                String wsdlArrayTypeValue = null;
                Vector attrs =
                        Utils.getAttributesWithLocalName(attributeNode,
                                "arrayType");

                for (int i = 0;
                     (i < attrs.size()) && (wsdlArrayTypeValue == null);
                     i++) {
                    Node attrNode = (Node) attrs.elementAt(i);
                    String attrName = attrNode.getNodeName();
                    QName attrQName =
                            Utils.getQNameFromPrefixedName(attributeNode, attrName);

                    if (Constants.isWSDL(attrQName.getNamespaceURI())) {
                        wsdlArrayTypeValue = attrNode.getNodeValue();
                    }
                }

                // The value could have any number of [] or [,] on the end
                // Strip these off to get the prefixed name.
                // The convert the prefixed name into a qname.
                // Count the number of [ and , to get the dim information.
                if (wsdlArrayTypeValue != null) {
                    int i = wsdlArrayTypeValue.indexOf('[');

                    if (i > 0) {
                        String prefixedName = wsdlArrayTypeValue.substring(0,
                                i);
                        String mangledString = wsdlArrayTypeValue.replace(',',
                                '[');

                        dims.value = 0;

                        int index = mangledString.indexOf('[');

                        while (index > 0) {
                            dims.value++;

                            index = mangledString.indexOf('[', index + 1);
                        }

                        return Utils.getQNameFromPrefixedName(restrictionNode,
                                prefixedName);
                    }
                }
            } else if (groupNode != null) {

                // Get the first element node under the group node.
                NodeList elements = groupNode.getChildNodes();
                Node elementNode = null;

                for (int i = 0;
                     (i < elements.getLength()) && (elementNode == null);
                     i++) {
                    Node kid = elements.item(i);

                    if (isXSDNode(kid, "element")) {
                        elementNode = elements.item(i);

                        break;
                    }
                }

                // The element node should have maxOccurs="unbounded" and
                // a type
                if (elementNode != null) {
                    String maxOccursValue = Utils.getAttribute(elementNode,
                            "maxOccurs");

                    if ((maxOccursValue != null)
                            && maxOccursValue.equalsIgnoreCase("unbounded")) {

                        // Get the QName of the type without considering maxOccurs
                        dims.value = 1;

                        return Utils.getTypeQName(elementNode,
                                new BooleanHolder(), true);
                    }
                }
            }
        }

        return null;
    }

    /**
     * adds an attribute node's type and name to the vector
     * helper used by getContainedAttributeTypes
     * 
     * @param v           
     * @param child       
     * @param symbolTable 
     */
    private static void addAttributeToVector(Vector v, Node child,
                                             SymbolTable symbolTable) {

        // Get the name and type qnames.
        // The type qname is used to locate the TypeEntry, which is then
        // used to retrieve the proper java name of the type.
        QName attributeName = Utils.getNodeNameQName(child);
        BooleanHolder forElement = new BooleanHolder();
        QName attributeType = Utils.getTypeQName(child, forElement,
                false);

        // An attribute is either qualified or unqualified.
        // If the ref= attribute is used, the name of the ref'd element is used
        // (which must be a root element).  If the ref= attribute is not
        // used, the name of the attribute is unqualified.
        if (!forElement.value) {

            // check the Form (or attributeFormDefault) attribute of
            // this node to determine if it should be namespace
            // quailfied or not.
            String form = Utils.getAttribute(child, "form");

            if ((form != null) && form.equals("unqualified")) {

                // Unqualified nodeName
                attributeName = Utils.findQName("",
                        attributeName.getLocalPart());
            } else if (form == null) {

                // check attributeFormDefault on schema element
                String def = Utils.getScopedAttribute(child,
                        "attributeFormDefault");

                if ((def == null) || def.equals("unqualified")) {

                    // Unqualified nodeName
                    attributeName =
                            Utils.findQName("", attributeName.getLocalPart());
                }
            }
        } else {
            attributeName = attributeType;
        }

        // Get the corresponding TypeEntry from the symbol table
        TypeEntry type = symbolTable.getTypeEntry(attributeType,
                forElement.value);

        // Try to get the corresponding global attribute ElementEntry
        // from the symbol table.
        if (type instanceof org.apache.axis2.databinding.symbolTable.Element) {
                type = ((org.apache.axis2.databinding.symbolTable.Element) type).getRefType();
        }
        
        // add type and name to vector, skip it if we couldn't parse it
        // XXX - this may need to be revisited.
        if ((type != null) && (attributeName != null)) {
            ContainedAttribute attr =
	    	new ContainedAttribute(type, attributeName);

	    String useValue = Utils.getAttribute(child, "use");

	    if (useValue != null) {
		attr.setOptional(useValue.equalsIgnoreCase("optional"));
	    }

            v.add(attr);
        }
    }

    /**
     * adds an attribute to the vector
     * helper used by addAttributeGroupToVector
     * 
     * @param v           
     * @param symbolTable 
     * @param type        
     * @param name        
     */
    private static void addAttributeToVector(Vector v, SymbolTable symbolTable,
                                             QName type, QName name) {

        TypeEntry typeEnt = symbolTable.getTypeEntry(type, false);

        if (typeEnt != null)    // better not be null
        {
            v.add(new ContainedAttribute(typeEnt, name));
        }
    }

    /**
     * adds each attribute group's attribute node to the vector
     * helper used by getContainedAttributeTypes
     * 
     * @param v           
     * @param attrGrpnode 
     * @param symbolTable 
     */
    private static void addAttributeGroupToVector(Vector v, Node attrGrpnode,
                                                  SymbolTable symbolTable) {

        // get the type of the attributeGroup
        QName attributeGroupType = Utils.getTypeQName(attrGrpnode,
                new BooleanHolder(), false);
        TypeEntry type =
                symbolTable.getTypeEntry(attributeGroupType, false);

        if (type != null) {
            if (type.getNode() != null) {

                // for each attribute or attributeGroup defined in the attributeGroup...
                NodeList children = type.getNode().getChildNodes();

                for (int j = 0; j < children.getLength(); j++) {
                    Node kid = children.item(j);

                    if (isXSDNode(kid, "attribute")) {
                        addAttributeToVector(v, kid, symbolTable);
                    } else if (isXSDNode(kid, "attributeGroup")) {
                        addAttributeGroupToVector(v, kid, symbolTable);
                    }
                }
            } else if (type.isBaseType()) {

                // soap/encoding is treated as a "known" schema
                // so let's act like we know it
                if (type.getQName().equals(Constants.SOAP_COMMON_ATTRS11)) {

                    // 1.1 commonAttributes contains two attributes
                    addAttributeToVector(v, symbolTable, Constants.XSD_ID,
                            new QName(Constants.URI_SOAP11_ENC,
                                    "id"));
                    addAttributeToVector(v, symbolTable, Constants.XSD_ANYURI,
                            new QName(Constants.URI_SOAP11_ENC,
                                    "href"));
                } else if (type.getQName().equals(
                        Constants.SOAP_COMMON_ATTRS12)) {

                    // 1.2 commonAttributes contains one attribute
                    addAttributeToVector(v, symbolTable, Constants.XSD_ID,
                            new QName(Constants.URI_SOAP12_ENC,
                                    "id"));
                } else if (type.getQName().equals(
                        Constants.SOAP_ARRAY_ATTRS11)) {

                    // 1.1 arrayAttributes contains two attributes
                    addAttributeToVector(v, symbolTable, Constants.XSD_STRING,
                            new QName(Constants.URI_SOAP12_ENC,
                                    "arrayType"));
                    addAttributeToVector(v, symbolTable, Constants.XSD_STRING,
                            new QName(Constants.URI_SOAP12_ENC,
                                    "offset"));
                } else if (type.getQName().equals(
                        Constants.SOAP_ARRAY_ATTRS12)) {

                    // 1.2 arrayAttributes contains two attributes
                    // the type of "arraySize" is really "2003soapenc:arraySize"
                    // which is rather of a hairy beast that is not yet supported
                    // in Axis, so let's just use string; nobody should care for
                    // now because arraySize wasn't used at all up until this
                    // bug 23145 was fixed, which had nothing to do, per se, with
                    // adding support for arraySize
                    addAttributeToVector(v, symbolTable, Constants.XSD_STRING,
                            new QName(Constants.URI_SOAP12_ENC,
                                    "arraySize"));
                    addAttributeToVector(v, symbolTable, Constants.XSD_QNAME,
                            new QName(Constants.URI_SOAP12_ENC,
                                    "itemType"));
                }
            }
        }
    }

    /**
     * Return the attribute names and types if any in the node
     * The even indices are the attribute types (TypeEntry) and
     * the odd indices are the corresponding names (Strings).
     * <p/>
     * Example:
     * <complexType name="Person">
     * <sequence>
     * <element minOccurs="1" maxOccurs="1" name="Age" type="double" />
     * <element minOccurs="1" maxOccurs="1" name="ID" type="xsd:float" />
     * </sequence>
     * <attribute name="Name" type="string" />
     * <attribute name="Male" type="boolean" />
     * <attributeGroup ref="s0:MyAttrSet" />
     * </complexType>
     * 
     * @param node        
     * @param symbolTable 
     * @return 
     */
    public static Vector getContainedAttributeTypes(Node node,
                                                    SymbolTable symbolTable) {

        Vector v = null;    // return value

        if (node == null) {
            return null;
        }

        // Check for SimpleContent
        // If the node kind is an element, dive into it.
        if (isXSDNode(node, "element")) {
            NodeList children = node.getChildNodes();
            int len = children.getLength();
            for (int j = 0; j < len; j++) {
                Node kid = children.item(j);

                if (isXSDNode(kid, "complexType")) {
                    node = kid;

                    break;
                }
            }
        }

        // Expecting a schema complexType
        if (isXSDNode(node, "complexType")) {

            // Under the complexType there could be complexContent/simpleContent
            // and extension elements if this is a derived type.  Skip over these.
            NodeList children = node.getChildNodes();
            Node content = null;
            int len = children.getLength();
            for (int j = 0; j < len; j++) {
                Node kid = children.item(j);

                if (isXSDNode(kid, "complexContent")
                        || isXSDNode(kid, "simpleContent")) {
                    content = kid;

                    break;
                }
            }

            // Check for extensions or restrictions
            if (content != null) {
                children = content.getChildNodes();
                len = children.getLength();
                for (int j = 0; j < len; j++) {
                    Node kid = children.item(j);

                    if (isXSDNode(kid, "extension")
                            || isXSDNode(kid, "restriction")) {
                        node = kid;

                        break;
                    }
                }
            }

            // examine children of the node for <attribute> elements
            children = node.getChildNodes();
            len = children.getLength();
            for (int i = 0; i < len; i++) {
                Node child = children.item(i);

                if (isXSDNode(child, "attributeGroup")) {
                    if (v == null) {
                        v = new Vector();
                    }
                    addAttributeGroupToVector(v, child, symbolTable);
                } else if (isXSDNode(child, "anyAttribute")) {
                    // do nothing right now
                    if (v == null) {
                        v = new Vector();
                    }
                } else if (isXSDNode(child, "attribute")) {
                    // we have an attribute
                    if (v == null) {
                        v = new Vector();
                    }
                    addAttributeToVector(v, child, symbolTable);
                }
            }
        }

        return v;
    }

    // list of all of the XSD types in Schema 2001

    /** Field schemaTypes[] */
    private static String schemaTypes[] = {
        "string", "normalizedString", "token", "byte", "unsignedByte",
        "base64Binary", "hexBinary", "integer", "positiveInteger",
        "negativeInteger", "nonNegativeInteger", "nonPositiveInteger", "int",
        "unsignedInt", "long", "unsignedLong", "short", "unsignedShort",
        "decimal", "float", "double", "boolean", "time", "dateTime", "duration",
        "date", "gMonth", "gYear", "gYearMonth", "gDay", "gMonthDay", "Name",
        "QName", "NCName", "anyURI", "language", "ID", "IDREF", "IDREFS",
        "ENTITY", "ENTITIES", "NOTATION", "NMTOKEN", "NMTOKENS",
        "anySimpleType"
    };

    /** Field schemaTypeSet */
    private static final Set schemaTypeSet =
            new HashSet(Arrays.asList(schemaTypes));

    /**
     * Determine if a string is a simple XML Schema type
     * 
     * @param s 
     * @return 
     */
    private static boolean isSimpleSchemaType(String s) {

        if (s == null) {
            return false;
        }

        return schemaTypeSet.contains(s);
    }

    /**
     * Determine if a QName is a simple XML Schema type
     * 
     * @param qname 
     * @return 
     */
    public static boolean isSimpleSchemaType(QName qname) {

        if ((qname == null) || !Constants.isSchemaXSD(qname.getNamespaceURI())) {
            return false;
        }

        return isSimpleSchemaType(qname.getLocalPart());
    }

    /**
     * Returns the base type of a given type with its symbol table.
     * This logic is extracted from JavaTypeWriter's constructor() method
     * for reusing.
     * 
     * @param type 
     * @param symbolTable 
     * @return 
     */
    public static TypeEntry getBaseType(TypeEntry type, SymbolTable symbolTable) {
        Node node = type.getNode();     
        TypeEntry base = getComplexElementExtensionBase(
                node, symbolTable);
        if (base == null) {
            base = getComplexElementRestrictionBase(node, symbolTable);
        }                    
        
        if (base == null) {
            QName baseQName = getSimpleTypeBase(node);                        
            if (baseQName != null) {
                base = symbolTable.getType(baseQName);
            }
        }
        return base;
    }

    /**
     * Returns whether the specified node represents a <xsd:simpleType> 
     * with a nested <xsd:list itemType="...">.
     * @param node 
     * @return 
     */
    public static boolean isListWithItemType(Node node) {
        
        return getListItemType(node) != null;
    }

    /**
     * Returns the value of itemType attribute of <xsd:list> in <xsd:simpleType> 
     * @param node 
     * @return 
     */
    public static QName getListItemType(Node node) {
        
        if (node == null) {
            return null;
        }
        
        // If the node kind is an element, dive into it.
        if (isXSDNode(node, "element")) {
            NodeList children = node.getChildNodes();
            for (int j = 0; j < children.getLength(); j++) {
                if (isXSDNode(children.item(j), "simpleType")) {
                    node = children.item(j);
                    break;
                }
            }
        }
        // Get the node kind, expecting a schema simpleType
        if (isXSDNode(node, "simpleType")) {
            NodeList children = node.getChildNodes();
            for (int j = 0; j < children.getLength(); j++) {
                if (isXSDNode(children.item(j), "list")) {
                    Node listNode = children.item(j);
                    org.w3c.dom.Element listElement = 
                    (org.w3c.dom.Element) listNode;
                    String type = listElement.getAttribute("itemType");
                    if (type.equals("")) {
                        Node localType = null;
                        children = listNode.getChildNodes();
                        for (j = 0; j < children.getLength() && localType == null; j++) {
                            if (isXSDNode(children.item(j), "simpleType")) {
                                localType = children.item(j);
                            }
                        }
                        if (localType != null) {
                            return getSimpleTypeBase(localType);
                        }
                        return null;
                    }
                    //int colonIndex = type.lastIndexOf(":");
                    //if (colonIndex > 0) {
                        //type = type.substring(colonIndex + 1);
                    //}
                    //return new QName(Constants.URI_2001_SCHEMA_XSD, type + "[]");
                    return Utils.getQNameFromPrefixedName(node, type);
                }
            }
        }
        return null;
    }
}