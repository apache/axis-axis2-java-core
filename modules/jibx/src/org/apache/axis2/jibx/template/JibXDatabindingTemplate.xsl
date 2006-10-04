
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="text"/>
<!-- #################################################################################  -->
    <!-- ############################   JiBX template   ##############################  -->
  <xsl:template match="databinders[@dbtype='jibx']">
    <xsl:param name="context">unknown</xsl:param>
    
    <xsl:apply-templates select="initialize-binding"/>

    <!-- wrapped='true' uses original code, wrapped='false' unwraps method calls -->
    <xsl:variable name="wrapped"><xsl:value-of select="@wrapped"/></xsl:variable>
    <xsl:if test="$wrapped='true'">
      
      <!-- MTOM not yet supported by JiBX, but array may be needed -->
      <xsl:variable name="base64"><xsl:value-of select="base64Elements/name"/></xsl:variable>
      <xsl:if test="$base64">
        private static javax.xml.namespace.QName[] qNameArray = {
        <xsl:for-each select="base64Elements/name">
          <xsl:if test="position()">1">,</xsl:if>new javax.xml.namespace.QName("<xsl:value-of select="@ns-url"/>","<xsl:value-of select="@localName"/>")
        </xsl:for-each>
        };
      </xsl:if>
  
      <xsl:variable name="firstType"><xsl:value-of select="param[1]/@type"/></xsl:variable>
  
      <xsl:for-each select="param[not(@type = preceding-sibling::param/@type)]">
        <xsl:if test="@type!=''">
  
            private org.apache.axiom.om.OMElement toOM(<xsl:value-of select="@type"/> param, org.apache.axiom.soap.SOAPFactory factory, boolean optimizeContent) {
                if (param instanceof org.jibx.runtime.IMarshallable){
                    if (bindingFactory == null) {
                        throw new RuntimeException("Could not find JiBX binding information for <xsl:value-of select="$firstType"/>, JiBX binding unusable");
                    }
                    org.jibx.runtime.IMarshallable marshallable =
                        (org.jibx.runtime.IMarshallable)param;
                    int index = marshallable.JiBX_getIndex();
                    org.apache.axis2.jibx.JiBXDataSource source =
                        new org.apache.axis2.jibx.JiBXDataSource(marshallable, bindingFactory);
                    org.apache.axiom.om.OMNamespace namespace = factory.createOMNamespace(bindingFactory.getElementNamespaces()[index], null);
                    return factory.createOMElement(source, bindingFactory.getElementNames()[index], namespace);
                } else {
                    throw new RuntimeException("No JiBX &lt;mapping> defined for class <xsl:value-of select="@type"/>");
                }
            }

            private org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, <xsl:value-of select="@type"/> param, boolean optimizeContent) {
                org.apache.axiom.soap.SOAPEnvelope envelope = factory.getDefaultEnvelope();
                if (param != null){
                    envelope.getBody().addChild(toOM(param, factory, optimizeContent));
                }
                return envelope;
            }

        </xsl:if>
      </xsl:for-each>

        /**
        *  get the default envelope
        */
        private org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory) {
            return factory.getDefaultEnvelope();
        }

        private java.lang.Object fromOM(
            org.apache.axiom.om.OMElement param,
            java.lang.Class type,
            java.util.Map extraNamespaces) {
            try {
                if (bindingFactory == null) {
                    throw new RuntimeException("Could not find JiBX binding information for com.sosnoski.seismic.jibxsoap.Query, JiBX binding unusable");
                }
                org.jibx.runtime.impl.UnmarshallingContext ctx =
                    (org.jibx.runtime.impl.UnmarshallingContext)bindingFactory.createUnmarshallingContext();
                org.jibx.runtime.IXMLReader reader = new org.jibx.runtime.impl.StAXReaderWrapper(param.getXMLStreamReaderWithoutCaching(), "SOAP-message", true);
                ctx.setDocument(reader);
                return ctx.unmarshalElement(type);
            } catch (Exception e) {
                 throw new RuntimeException(e);
            }
        }
      
    </xsl:if>
    
    <xsl:if test="$wrapped='false'">
      <xsl:choose>
        <xsl:when test="$context='message-receiver'">
          <xsl:apply-templates mode="message-receiver" select="dbmethod"/>
        </xsl:when>
        <xsl:when test="$context='interface-implementation'">
          <xsl:apply-templates mode="interface-implementation" select="dbmethod"/>
        </xsl:when>
      </xsl:choose>
    </xsl:if>

  </xsl:template>
  
  <xsl:template match="dbmethod" mode="message-receiver">
      public org.apache.axiom.soap.SOAPEnvelope <xsl:value-of select="@receiver-name"/>(org.apache.axiom.om.OMElement element, <xsl:value-of select="/*/@skeletonname"/> skel, org.apache.axiom.soap.SOAPFactory factory) throws org.apache.axis2.AxisFault {
          org.apache.axiom.soap.SOAPEnvelope envelope = null;
          try {
              org.jibx.runtime.impl.UnmarshallingContext uctx = getNewUnmarshalContext(element);
              uctx.next();
              int index;
    <xsl:apply-templates select="in-wrapper/parameter-element"/>
    <xsl:choose>
      <xsl:when test="out-wrapper/@empty='false' and out-wrapper/return-element/@array='true'">
              envelope = factory.getDefaultEnvelope();
              org.apache.axiom.om.OMElement wrapper = factory.createOMElement("<xsl:value-of select='out-wrapper/@name'/>", "<xsl:value-of select='out-wrapper/@ns'/>", "");
              envelope.getBody().addChild(wrapper);
              <xsl:value-of select="out-wrapper/return-element/@java-type"/>[] results = skel.<xsl:call-template name="call-arg-list"/>;
              if (results == null || results.length == 0) {
        <xsl:choose>
          <xsl:when test="out-wrapper/return-element/@optional='true'"/>
          <xsl:otherwise>
                  throw new org.apache.axis2.AxisFault("Missing required result");
          </xsl:otherwise>
        </xsl:choose>
              } else {
                  org.apache.axiom.om.OMNamespace appns = factory.createOMNamespace("<xsl:value-of select='out-wrapper/return-element/@ns'/>", "app");
                  wrapper.declareNamespace(appns);
        <xsl:choose>
          <xsl:when test="out-wrapper/return-element/@form='complex'">
                  for (int i = 0; i &lt; results.length; i++) {
                      <xsl:value-of select="out-wrapper/return-element/@java-type"/> result = results[i];
                      if (result == null) {
            <xsl:choose>
              <xsl:when test="out-wrapper/return-element/@nillable='true'">
                          org.apache.axiom.om.OMElement child = factory.createOMElement("<xsl:value-of select='out-wrapper/return-element/@name'/>", appns);
                          org.apache.axiom.om.OMNamespace xsins = factory.createOMNamespace("http://www.w3.org/2001/XMLSchema-instance", "xsi");
                          child.declareNamespace(xsins);
                          child.addAttribute("nil", "true", xsins);
                          wrapper.addChild(child);
              </xsl:when>
              <xsl:otherwise>
                          throw new org.apache.axis2.AxisFault("Null value in result array not allowed unless element has nillable='true'");
              </xsl:otherwise>
            </xsl:choose>
                      } else {
                          org.apache.axiom.om.OMDataSource src = new org.apache.axis2.jibx.JiBXDataSource(result, _type_index<xsl:value-of select="out-wrapper/return-element/@type-index"/>, bindingFactory);
                          org.apache.axiom.om.OMElement child = factory.createOMElement(src, "<xsl:value-of select='out-wrapper/return-element/@name'/>", appns);
                          wrapper.addChild(child);
                      }
                  }
          </xsl:when>
          <xsl:otherwise>
                  for (int i = 0; i &lt; results.length; i++) {
                      <xsl:value-of select="out-wrapper/return-element/@java-type"/> result = results[i];
                      org.apache.axiom.om.OMElement child = factory.createOMElement("<xsl:value-of select='out-wrapper/return-element/@name'/>", appns);
            <xsl:choose>
              <xsl:when test="out-wrapper/return-element/@serializer=''">
                      child.setText(result.toString());
              </xsl:when>
              <xsl:otherwise>
                      child.setText(<xsl:value-of select="out-wrapper/return-element/@serializer"/>(result);
              </xsl:otherwise>
            </xsl:choose>
                      wrapper.addChild(child);
                  }
          </xsl:otherwise>
        </xsl:choose>
              }
      </xsl:when>
      <xsl:when test="out-wrapper/@empty='false'">
              envelope = factory.getDefaultEnvelope();
              org.apache.axiom.om.OMElement wrapper = factory.createOMElement("<xsl:value-of select='out-wrapper/@name'/>", "<xsl:value-of select='out-wrapper/@ns'/>", "");
              envelope.getBody().addChild(wrapper);
              <xsl:value-of select="out-wrapper/return-element/@java-type"/> result = skel.<xsl:call-template name="call-arg-list"/>;
              org.apache.axiom.om.OMNamespace appns = factory.createOMNamespace("<xsl:value-of select='out-wrapper/return-element/@ns'/>", "app");
              wrapper.declareNamespace(appns);
        <xsl:choose>
          <xsl:when test="out-wrapper/return-element/@form='complex'">
              if (result == null) {
            <xsl:choose>
              <xsl:when test="out-wrapper/return-element/@optional='true'"/>
              <xsl:when test="out-wrapper/return-element/@nillable='true'">
                  org.apache.axiom.om.OMElement child = factory.createOMElement("<xsl:value-of select='out-wrapper/return-element/@name'/>", appns);
                  org.apache.axiom.om.OMNamespace xsins = factory.createOMNamespace("http://www.w3.org/2001/XMLSchema-instance", "xsi");
                  child.declareNamespace(xsins);
                  child.addAttribute("nil", "true", xsins);
                  wrapper.addChild(child);
              </xsl:when>
              <xsl:otherwise>
                  throw new org.apache.axis2.AxisFault("Missing required result");
              </xsl:otherwise>
            </xsl:choose>
              } else {
                  org.apache.axiom.om.OMDataSource src = new org.apache.axis2.jibx.JiBXDataSource(result, _type_index<xsl:value-of select="out-wrapper/return-element/@type-index"/>, bindingFactory);
                  org.apache.axiom.om.OMElement child = factory.createOMElement(src, "<xsl:value-of select='out-wrapper/return-element/@name'/>", appns);
                  wrapper.addChild(child);
              }
          </xsl:when>
          <xsl:otherwise>
              org.apache.axiom.om.OMElement child = factory.createOMElement("<xsl:value-of select='out-wrapper/return-element/@name'/>", appns);
            <xsl:choose>
              <xsl:when test="out-wrapper/return-element/@serializer=''">
              child.setText(result.toString());
              </xsl:when>
              <xsl:otherwise>
              child.setText(<xsl:value-of select="out-wrapper/return-element/@serializer"/>(result));
              </xsl:otherwise>
            </xsl:choose>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:otherwise>
              skel.<xsl:call-template name="call-arg-list"/>;
        <xsl:if test="count(out-wrapper)&gt;0">
              envelope = factory.getDefaultEnvelope();
              envelope.getBody().addChild(factory.createOMElement("<xsl:value-of select='out-wrapper/@name'/>", "<xsl:value-of select='out-wrapper/@ns'/>", ""););
        </xsl:if>
      </xsl:otherwise>
    </xsl:choose>
          } catch (org.jibx.runtime.JiBXException e) {
              throw new org.apache.axis2.AxisFault(e);
          }
          return envelope;
      }
  </xsl:template>
  
  <!-- Generate argument list for message receiver call to actual implementation method. -->
  <xsl:template name="call-arg-list">
    <xsl:value-of select="@method-name"/>(
    <xsl:for-each select="in-wrapper/parameter-element">
      <xsl:if test="position()&gt;1">, </xsl:if><xsl:value-of select="@java-name"/>
    </xsl:for-each>
    )
  </xsl:template>
  
  <!-- Generate code for a particular parameter element in a message receiver method -->
  <xsl:template match="parameter-element">
    <xsl:choose>
      <xsl:when test="@array='true'">
        <xsl:call-template name="unmarshal-array"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="unmarshal-value"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <!-- Unmarshal a repeated element into an array -->
  <xsl:template name="unmarshal-array">
    <xsl:value-of select="@java-type"/>[] <xsl:value-of select="@java-name"/> = new <xsl:value-of select="@java-type"/>[4];
      index = 0;
      while (uctx.isAt("<xsl:value-of select="@ns"/>", "<xsl:value-of select="@name"/>")) {
          if (index >= <xsl:value-of select="@java-name"/>.length) {
              <xsl:value-of select="@java-name"/> = (<xsl:value-of select="@java-type"/>[])org.jibx.runtime.Utility.growArray(<xsl:value-of select="@java-name"/>);
          }
    <xsl:if test="@nillable='true'">
          if (uctx.attributeBoolean("http://www.w3.org/2001/XMLSchema-instance", "nil", false)) {
              uctx.skipElement();
          } else {
    </xsl:if>
    <xsl:value-of select="@java-name"/>[index++] = (<xsl:value-of select="@java-type"/>)<xsl:call-template name="deserialize-element-value"/>;
    <xsl:if test="@nillable='true'">
          }
    </xsl:if>
      }
      <xsl:value-of select="@java-name"/> = (<xsl:value-of select="@java-type"/>[])org.jibx.runtime.Utility.resizeArray(index, <xsl:value-of select="@java-name"/>);
    <xsl:if test="@optional!='true'">
      if (index == 0) {
          throw new org.apache.axis2.AxisFault("Receive message is missing required element {<xsl:value-of select='@ns'/>}<xsl:value-of select='@name'/>");
      }
    </xsl:if>
  </xsl:template>
  
  <!-- Unmarshal a non-repeated element into an simple value -->
  <xsl:template name="unmarshal-value">
    <xsl:value-of select="@java-type"/><xsl:text> </xsl:text><xsl:value-of select="@java-name"/> = <xsl:choose><xsl:when test="boolean(@default)"><xsl:value-of select="@default"/></xsl:when><xsl:otherwise>null</xsl:otherwise></xsl:choose>;
          if (uctx.isAt("<xsl:value-of select="@ns"/>", "<xsl:value-of select="@name"/>")) {
    <xsl:if test="@nillable='true'">
              if (uctx.attributeBoolean("http://www.w3.org/2001/XMLSchema-instance", "nil", false)) {
                  uctx.skipElement();
              } else {
    </xsl:if>
    <xsl:value-of select="@java-name"/> = (<xsl:value-of select="@java-type"/>)<xsl:call-template name="deserialize-element-value"/>;
    <xsl:if test="@nillable='true'">
              }
    </xsl:if>
    <xsl:choose>
      <xsl:when test="optional">
          }
      </xsl:when>
      <xsl:otherwise>
          } else {
              throw new org.apache.axis2.AxisFault("Receive message is missing required element {<xsl:value-of select='@ns'/>}<xsl:value-of select='@name'/>");
          }
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <!-- Convert the current element into a value. -->
  <xsl:template name="deserialize-element-value">
    <xsl:choose>
      <xsl:when test="@java-type='java.lang.String' and @deserializer=''">
        uctx.parseElementText("<xsl:value-of select="@ns"/>", "<xsl:value-of select="@name"/>")
      </xsl:when>
      <xsl:when test="@form='simple' and @deserializer=''">
        new <xsl:value-of select="@java-type"/>(uctx.parseElementText("<xsl:value-of select="@ns"/>", "<xsl:value-of select="@name"/>"))
      </xsl:when>
      <xsl:when test="@form='simple'">
        <xsl:value-of select="@deserializer"/>(uctx.parseElementText("<xsl:value-of select="@ns"/>", "<xsl:value-of select="@name"/>"))
      </xsl:when>
      <xsl:when test="@form='complex'">
        uctx.getUnmarshaller(_type_index<xsl:value-of select="@type-index"/>).unmarshal(null, ctx)
      </xsl:when>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template match="dbmethod" mode="interface-implementation">
    <xsl:variable name="return-base-type"><xsl:value-of select="out-wrapper/return-element/@java-type"/></xsl:variable>
    <xsl:variable name="return-full-type"><xsl:value-of select="$return-base-type"/><xsl:if test="out-wrapper/return-element/@array='true'">[]</xsl:if></xsl:variable>
      public <xsl:value-of select="$return-full-type"/><xsl:text> </xsl:text><xsl:value-of select="@method-name"/>() {
          
      }
  </xsl:template>
  
  <!-- Generate the method parameter list declaration -->
  <xsl:template match="in-wrapper" mode="parameter-list">
    (<xsl:for-each select="parameter-element"><xsl:if test="position()&gt;1">, </xsl:if><xsl:value-of select="@fulltype"/> <xsl:value-of select="@name"/></xsl:for-each>
  </xsl:template>
  
  <!-- Called by main template to handle static data structures. -->
  <xsl:template match="initialize-binding">
      private static final org.jibx.runtime.IBindingFactory bindingFactory;
      private static final String bindingErrorMessage;
    <xsl:apply-templates mode="generate-index-fields" select="abstract-type"/>
      static {
          org.jibx.runtime.IBindingFactory factory = null;
          String message = null;
          try {
              factory = org.jibx.runtime.BindingDirectory.getFactory(<xsl:value-of select="@bound-class"/>.class);
              message = null;
          } catch (Exception e) { message = e.getMessage(); }
          bindingFactory = factory;
          bindingErrorMessage = message;
    <xsl:apply-templates mode="set-index-fields" select="abstract-type"/>
      };
      
      private static org.jibx.runtime.impl.UnmarshallingContext getNewUnmarshalContext(org.apache.axiom.om.OMElement param)
          throws org.jibx.runtime.JiBXException {
          if (bindingFactory == null) {
              throw new RuntimeException(bindingErrorMessage);
          }
          org.jibx.runtime.impl.UnmarshallingContext ctx =
              (org.jibx.runtime.impl.UnmarshallingContext)bindingFactory.createUnmarshallingContext();
          org.jibx.runtime.IXMLReader reader = new org.jibx.runtime.impl.StAXReaderWrapper(param.getXMLStreamReaderWithoutCaching(), "SOAP-message", true);
          ctx.setDocument(reader);
          return ctx;
      }
  </xsl:template>
  
  <!-- Called by "initialize-binding" template to generate mapped class index fields. -->
  <xsl:template match="abstract-type" mode="generate-index-fields">
          private static final int _type_index<xsl:value-of select="@type-index"/>;
  </xsl:template>
    
  <!-- Called by "initialize-binding" template to initialize mapped class index fields. -->
  <xsl:template match="abstract-type" mode="set-index-fields">
         _type_index<xsl:value-of select="@type-index"/> = (bindingFactory == null) ?
            -1 : bindingFactory.getTypeIndex("{<xsl:value-of select="@ns"/>}<xsl:value-of select="@name"/>");
  </xsl:template>
  
</xsl:stylesheet>