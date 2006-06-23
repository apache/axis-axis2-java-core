<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="text"/>

    <!-- import the databinding template-->
    <xsl:include href="databindsupporter"/>
    <!-- import the other templates for databinding
         Note  -  these names would be handled by a special
         URI resolver during the xslt transformations
     -->
    <xsl:include href="externalTemplate"/>


    <xsl:include href="policyExtensionTemplate"/>

    <xsl:template match="/class">
        <xsl:variable name="interfaceName"><xsl:value-of select="@interfaceName"/></xsl:variable>
        <xsl:variable name="package"><xsl:value-of select="@package"/></xsl:variable>
        <xsl:variable name="callbackname"><xsl:value-of select="@callbackname"/></xsl:variable>
        <xsl:variable name="isSync"><xsl:value-of select="@isSync"/></xsl:variable>
        <xsl:variable name="isAsync"><xsl:value-of select="@isAsync"/></xsl:variable>
        <xsl:variable name="soapVersion"><xsl:value-of select="@soap-version"/></xsl:variable>
        /**
        * <xsl:value-of select="@name"/>.c
        *
        * This file was auto-generated from WSDL for Axis2/C - stub code
        * by the Apache Axis2/Java version: #axisVersion# #today#
        */
        #include "<xsl:value-of select="@name"/>.h"

       /**
        *  <xsl:value-of select="@name"/> C implementation
        */

        axis2_stub_t*
        axis2_<xsl:value-of select="$interfaceName"/>_stub_create (const axis2_env_t *env,
                                        axis2_char_t *client_home,
                                        axis2_char_t *endpoint_uri)
        {
           axis2_stub_t* stub = NULL;
           axis2_endpoint_ref_t* endpoint_ref = NULL;
           AXIS2_FUNC_PARAM_CHECK ( client_home, env, NULL)

           if (NULL == endpoint_uri )
           {
              endpoint_uri = axis2_get_endpoint_uri_from_wsdl( env );
           }

           endpoint_ref = axis2_endpoint_ref_create(env, endpoint_uri);

           stub = axis2_stub_create_with_endpoint_ref_and_client_home ( env, endpoint_ref, client_home );
           axis2_populate_axis_service( stub, env );
           return stub;
        }


        void axis2_populate_axis_service( axis2_stub_t* stub, const axis2_env_t* env)
        {
          axis2_svc_client_t* svc_client = NULL;
          axis2_qname_t *svc_qname =  NULL;
          axis2_qname_t *op_qname =  NULL;
          axis2_svc_t* svc = NULL;
          axis2_op_t* op = NULL;

          /*Modifying the Service*/
          svc_client = AXIS2_STUB_GET_SVC_CLIENT (stub, env );
          svc = (axis2_svc_t*)AXIS2_SVC_CLIENT_GET_AXIS_SERVICE ( svc_client, env );
          axis2_qname_create(env,"<xsl:value-of select="@servicename"/>" ,NULL, NULL);
          AXIS2_SVC_SET_QNAME (svc, env, svc_qname);

          /*creating the operations*/

          <xsl:for-each select="method">
            op_qname = axis2_qname_create(env,
                                          "<xsl:value-of select="@name"/>" ,
                                          "<xsl:value-of select="@namespace"/>",
                                          NULL);
            op = axis2_op_create_with_qname(env, op_qname);
            <xsl:choose>
              <xsl:when test="@mep='10'">
                AXIS2_OP_SET_MSG_EXCHANGE_PATTERN(op, env, AXIS2_MEP_URI_IN_ONLY);
              </xsl:when>
              <xsl:otherwise>
                AXIS2_OP_SET_MSG_EXCHANGE_PATTERN(op, env, AXIS2_MEP_URI_OUT_IN);
              </xsl:otherwise>
            </xsl:choose>
            AXIS2_SVC_ADD_OP(svc, env, op);

          </xsl:for-each>
        }

        /**
         * return end point picked from wsdl
         */
        axis2_char_t* axis2_get_endpoint_uri_from_wsdl ( const axis2_env_t* env )
        {
          axis2_char_t* endpoint_uri = NULL;
          /*set the address from here*/
        <xsl:for-each select="endpoint">
          <xsl:choose>
            <xsl:when test="position()=1">
              endpoint_uri = "<xsl:value-of select="."/>";
            </xsl:when>
            <xsl:otherwise>
              /* mulitiple address defined*/
            </xsl:otherwise>
           </xsl:choose>
        </xsl:for-each>
          return endpoint_uri;
        }


    <xsl:for-each select="method">
      <xsl:variable name="outputours"><xsl:value-of select="output/param/@ours"></xsl:value-of></xsl:variable>
      <xsl:variable name="outputtype"><xsl:choose><xsl:when test="output/param/@type='org.apache.axiom.om.OMElement'">axiom_node_t*</xsl:when><xsl:otherwise><xsl:if test="output/param/@ours">axis2_</xsl:if><xsl:value-of select="output/param/@type"></xsl:value-of><xsl:if test="output/param/@ours">_t*</xsl:if></xsl:otherwise></xsl:choose></xsl:variable>
      <xsl:variable name="capsoutputtype"><xsl:if test="$outputours"></xsl:if><xsl:choose><xsl:when test="output/param/@type='org.apache.axiom.om.OMElement'">AXIOM_NODE</xsl:when><xsl:otherwise>AXIS2_<xsl:value-of select="output/param/@caps-type"></xsl:value-of></xsl:otherwise></xsl:choose></xsl:variable>
      <xsl:variable name="style"><xsl:value-of select="@style"></xsl:value-of></xsl:variable>
      <xsl:variable name="soapAction"><xsl:value-of select="@soapaction"></xsl:value-of></xsl:variable>
      <xsl:variable name="mep"><xsl:value-of select="@mep"/></xsl:variable>

      <xsl:variable name="method-name"><xsl:value-of select="@name"/></xsl:variable>
      <xsl:variable name="method-ns"><xsl:value-of select="@namespace"/> </xsl:variable>

      <!-- Code generation for the in-out mep -->
      <xsl:if test="not($mep='10')">
        <xsl:if test="$isSync='1'">
          /**
           * Auto generated method signature
           */

           <xsl:choose>
           <xsl:when test="$outputtype=''">axis2_status_t</xsl:when>
           <xsl:when test="$outputtype='axis2__t*'">void</xsl:when>
           <xsl:otherwise>
           <xsl:value-of select="$outputtype"/>
           </xsl:otherwise>
           </xsl:choose>
           axis2_<xsl:value-of select="@name"/>( axis2_stub_t* stub, const axis2_env_t* env <xsl:for-each select="input/param[@type!='']"> ,<xsl:variable name="paramtype"><xsl:choose><xsl:when test="@type='org.apache.axiom.om.OMElement'">axiom_node_t*</xsl:when><xsl:otherwise><xsl:if test="@ours">axis2_</xsl:if><xsl:value-of select="@type"></xsl:value-of><xsl:if test="@ours">_t*</xsl:if></xsl:otherwise></xsl:choose></xsl:variable>
                                                <xsl:if test="position()>1">,</xsl:if><xsl:value-of select="$paramtype"/><xsl:text> </xsl:text><xsl:value-of select="@name"/>
                                                </xsl:for-each>)
           {
              axis2_svc_client_t* svc_client = NULL;
              axis2_options_t *options = NULL;
              axiom_node_t* ret_node = NULL;

              axis2_char_t* soap_action = NULL;
              axis2_qname_t *op_qname =  NULL;
              axiom_node_t* payload = NULL;
              <xsl:if test="output/param/@ours">
              	<!-- this means data binding is enable -->
                <xsl:value-of select="$outputtype"/> ret_val = NULL;
              </xsl:if>


             <xsl:for-each select="input/param[@type!='' and @ours]">
                <xsl:if test="position()=1"> <!--declare only once-->
                  /* declarations for temp nodes to build input message */

                  <xsl:if test="$style='rpc'">
                   axiom_attribute_t* attri1 = NULL;
                   axiom_namespace_t* ns1 = NULL;
                   axiom_namespace_t* xsi = NULL;
                   axiom_namespace_t* xsd = NULL;
                   axiom_element_t* payload_ele = NULL;
                  </xsl:if>
                </xsl:if>
              </xsl:for-each>

              <xsl:for-each select="input/param[@type!='']">
              <!-- for service client currently suppported only 1 input param -->
              <xsl:variable name="paramtype"><xsl:choose><xsl:when test="@type='org.apache.axiom.om.OMElement'">axiom_node_t*</xsl:when><xsl:otherwise><xsl:if test="@ours">axis2_</xsl:if><xsl:value-of select="@type"></xsl:value-of><xsl:if test="@ours">_t*</xsl:if></xsl:otherwise></xsl:choose></xsl:variable>
              <xsl:variable name="capsparamtype"><xsl:if test="@ours"></xsl:if><xsl:choose><xsl:when test="@type='org.apache.axiom.om.OMElement'">AXIOM_NODE</xsl:when><xsl:otherwise>AXIS2_<xsl:value-of select="@caps-type"></xsl:value-of></xsl:otherwise></xsl:choose></xsl:variable>
                 <xsl:choose>
                     <xsl:when test="not(@ours)">
                        <xsl:if test="position()=1">
                          payload = <xsl:value-of select="@name"/>;
                        </xsl:if>
                     </xsl:when>
                     <xsl:otherwise>
                       <xsl:if test="position()=1">
                       </xsl:if>
                       <xsl:choose>
                       <xsl:when test="$style='rpc'">
                       <xsl:if test="position()=1">
                           payload = <xsl:value-of select="$capsparamtype"/>_BUILD_OM(<xsl:value-of select="@name"/>, env, NULL, xsi, xsd );
                           payload_ele =    AXIOM_NODE_GET_DATA_ELEMENT ( payload, env );
                           xsi =
                            axiom_namespace_create (env,
                                                       <xsl:choose><xsl:when test="$soapVersion='1.2'">"http://www.w3.org/2001/XMLSchema-instance"</xsl:when><xsl:when test="$soapVersion='1.1'">"http://www.w3.org/1999/XMLSchema-instance"</xsl:when></xsl:choose>,
                                                       "xsi");
                           AXIOM_ELEMENT_DECLARE_NAMESPACE (payload_ele, env,
                                                      payload, xsi);
                           xsd =
                            axiom_namespace_create (env,
                                                       <xsl:choose><xsl:when test="$soapVersion='1.2'">"http://www.w3.org/2001/XMLSchema"</xsl:when><xsl:when test="$soapVersion='1.1'">"http://www.w3.org/1999/XMLSchema"</xsl:when></xsl:choose>,
                                                       "xsd");
                           AXIOM_ELEMENT_DECLARE_NAMESPACE (payload_ele, env,
                                                      payload, xsd);
                           ns1 =
                            axiom_namespace_create (env,
                                                       <xsl:choose><xsl:when test="$soapVersion='1.2'">AXIOM_SOAP12_SOAP_ENVELOPE_NAMESPACE_URI</xsl:when><xsl:when test="$soapVersion='1.1'">AXIOM_SOAP11_SOAP_ENVELOPE_NAMESPACE_URI</xsl:when></xsl:choose>,
                                                       "soapenv");
                           AXIOM_ELEMENT_DECLARE_NAMESPACE (payload_ele, env,
                                                      payload, ns1);
                           attri1 =
                            axiom_attribute_create (env, "encodingStyle",
                                                       "http://schemas.xmlsoap.org/soap/encoding/",
                                                       ns1);
                           AXIOM_ELEMENT_ADD_ATTRIBUTE (payload_ele, env,
                                                      attri1, payload );
                           </xsl:if>
                       </xsl:when>
                       <xsl:otherwise>
                           payload = <xsl:value-of select="$capsparamtype"/>_BUILD_OM(<xsl:value-of select="@name"/>, env, NULL, NULL, NULL );

                       </xsl:otherwise>
                       </xsl:choose>
                        <!--should this omit in doclit type?-->
                        <!--AXIOM_ELEMENT_SET_NAMESPACE ( payload_ele, env, payload_ns, payload );-->
                     </xsl:otherwise>
                 </xsl:choose>
              </xsl:for-each>

              options = AXIS2_STUB_GET_OPTIONS( stub, env);
              if ( NULL == options )
              {
                AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "options is null in stub: Error code:"
                     " %d :: %s", env->error->error_number,
                AXIS2_ERROR_GET_MESSAGE(env->error));
                return NULL;
              }
              svc_client = AXIS2_STUB_GET_SVC_CLIENT (stub, env );
              soap_action = AXIS2_OPTIONS_GET_ACTION ( options, env );
              if ( NULL == soap_action )
              {
                soap_action = "<xsl:value-of select="$soapAction"/>";
                AXIS2_OPTIONS_SET_ACTION( options, env, soap_action );
              }
              <xsl:choose>
               <xsl:when test="$soapVersion='1.2'">
              AXIS2_OPTIONS_SET_SOAP_VERSION(options, env, AXIOM_SOAP12 );
               </xsl:when>
               <xsl:otherwise>
              AXIS2_OPTIONS_SET_SOAP_VERSION(options, env, AXIOM_SOAP11 );
               </xsl:otherwise>
              </xsl:choose>
              op_qname = axis2_qname_create(env,
                                          "<xsl:value-of select="@name"/>" ,
                                          "<xsl:value-of select="@namespace"/>",
                                          NULL);
              ret_node =  AXIS2_SVC_CLIENT_SEND_RECEIVE_WITH_OP_QNAME( svc_client, env, op_qname, payload);


              <xsl:choose>
                  <xsl:when test="$outputtype='axis2__t*'">
                   return;
                  </xsl:when>
                  <xsl:when test="$outputtype!='axiom_node_t*'">
                    if ( NULL == ret_node )
                      return NULL;
                    ret_val = axis2_<xsl:value-of select="output/param/@type"/>_create(env);

                    <xsl:value-of select="$capsoutputtype"/>_PARSE_OM(ret_val, env, ret_node );
                   return ret_val;
                  </xsl:when>
                  <xsl:otherwise>
                   return ret_node;
                  </xsl:otherwise>
              </xsl:choose>
          }
          </xsl:if>  <!--close for  test="$isSync='1'-->
          <!-- Async method generation -->
          <xsl:if test="$isAsync='1'">
          /**
           * Auto generated method signature for Asynchronous Invocations
           */
           <xsl:variable name="callbackoncomplete"><xsl:value-of select="$callbackname"></xsl:value-of><xsl:text>_on_complete</xsl:text></xsl:variable>
           <xsl:variable name="callbackonerror"><xsl:value-of select="$callbackname"></xsl:value-of><xsl:text>_on_error</xsl:text></xsl:variable>
           void axis2_start_<xsl:value-of select="@name"/>( axis2_stub_t* stub, const axis2_env_t *env, <xsl:for-each select="input/param[@type!='']"><xsl:variable name="paramtype"><xsl:choose><xsl:when test="@type='org.apache.axiom.om.OMElement'">axiom_node_t*</xsl:when><xsl:otherwise><xsl:if test="@ours">axis2_</xsl:if><xsl:value-of select="@type"></xsl:value-of><xsl:if test="@ours">_t*</xsl:if></xsl:otherwise></xsl:choose></xsl:variable>
                                                    <xsl:if test="position()>1">,</xsl:if><xsl:value-of select="$paramtype"/><xsl:text> </xsl:text><xsl:value-of select="@name"/>
                                                    </xsl:for-each>,
                                                    axis2_status_t ( AXIS2_CALL *on_complete ) (struct axis2_callback *, const axis2_env_t* ) ,
                                                    axis2_status_t ( AXIS2_CALL *on_error ) (struct axis2_callback *, const axis2_env_t*, int ) )
           {

              axis2_callback_t *callback = NULL;

              axis2_svc_client_t* svc_client = NULL;
              axis2_options_t *options = NULL;

              axis2_char_t* soap_action = NULL;
              axiom_node_t* payload = NULL;

             <xsl:for-each select="input/param[@type!='' and @ours]">
                <xsl:if test="position()=1"> <!--declare only once-->
                  /* declarations for temp nodes to build input message */
                  <xsl:if test="$style='rpc'">
                  axiom_element_t* payload_ele = NULL;
                   axiom_attribute_t* attri1 = NULL;
                   axiom_namespace_t* ns1 = NULL;
                   axiom_namespace_t* xsi = NULL;
                   axiom_namespace_t* xsd = NULL;
                  </xsl:if>
                </xsl:if>
              </xsl:for-each>

              <xsl:for-each select="input/param[@type!='']">
              <!-- for service client currently suppported only 1 input param -->
              <xsl:variable name="paramtype"><xsl:choose><xsl:when test="@type='org.apache.axiom.om.OMElement'">axiom_node_t*</xsl:when><xsl:otherwise><xsl:if test="@ours">axis2_</xsl:if><xsl:value-of select="@type"></xsl:value-of><xsl:if test="@ours">_t*</xsl:if></xsl:otherwise></xsl:choose></xsl:variable>
              <xsl:variable name="capsparamtype"><xsl:if test="@ours"></xsl:if><xsl:choose><xsl:when test="@type='org.apache.axiom.om.OMElement'">AXIOM_NODE</xsl:when><xsl:otherwise>AXIS2_<xsl:value-of select="@caps-type"></xsl:value-of></xsl:otherwise></xsl:choose></xsl:variable>
                 <xsl:choose>
                     <xsl:when test="not(@ours)">
                        <xsl:if test="position()=1">
                          payload = <xsl:value-of select="@name"/>;
                        </xsl:if>
                     </xsl:when>
                     <xsl:otherwise>
                       <xsl:if test="position()=1">
                       </xsl:if>
                       <xsl:choose>
                       <xsl:when test="$style='rpc'">
                       <xsl:if test="position()=1">
                           payload = <xsl:value-of select="$capsparamtype"/>_BUILD_OM(<xsl:value-of select="@name"/>, env, NULL, xsi, xsd );
                           payload_ele =    AXIOM_NODE_GET_DATA_ELEMENT ( payload, env );
                           xsi =
                            axiom_namespace_create (env,
                                                       <xsl:choose><xsl:when test="$soapVersion='1.2'">"http://www.w3.org/2001/XMLSchema-instance"</xsl:when><xsl:when test="$soapVersion='1.1'">"http://www.w3.org/1999/XMLSchema-instance"</xsl:when></xsl:choose>,
                                                       "xsi");
                           AXIOM_ELEMENT_DECLARE_NAMESPACE (payload_ele, env,
                                                      payload, xsi);
                           xsd =
                            axiom_namespace_create (env,
                                                       <xsl:choose><xsl:when test="$soapVersion='1.2'">"http://www.w3.org/2001/XMLSchema"</xsl:when><xsl:when test="$soapVersion='1.1'">"http://www.w3.org/1999/XMLSchema"</xsl:when></xsl:choose>,
                                                       "xsd");
                           AXIOM_ELEMENT_DECLARE_NAMESPACE (payload_ele, env,
                                                      payload, xsd);
                           ns1 =
                            axiom_namespace_create (env,
                                                       <xsl:choose><xsl:when test="$soapVersion='1.2'">AXIOM_SOAP12_SOAP_ENVELOPE_NAMESPACE_URI</xsl:when><xsl:when test="$soapVersion='1.1'">AXIOM_SOAP11_SOAP_ENVELOPE_NAMESPACE_URI</xsl:when></xsl:choose>,
                                                       "soapenv");
                           AXIOM_ELEMENT_DECLARE_NAMESPACE (payload_ele, env,
                                                      payload, ns1);
                           attri1 =
                            axiom_attribute_create (env, "encodingStyle",
                                                       "http://schemas.xmlsoap.org/soap/encoding/",
                                                       ns1);
                           AXIOM_ELEMENT_ADD_ATTRIBUTE (payload_ele, env,
                                                      attri1, payload );
                           </xsl:if>
                       </xsl:when>
                       <xsl:otherwise>
                           payload = <xsl:value-of select="$capsparamtype"/>_BUILD_OM(<xsl:value-of select="@name"/>, env, NULL, NULL, NULL );

                       </xsl:otherwise>
                       </xsl:choose>
                        <!--should this omit in doclit type?-->
                        <!--AXIOM_ELEMENT_SET_NAMESPACE ( payload_ele, env, payload_ns, payload );-->
                     </xsl:otherwise>
                 </xsl:choose>
              </xsl:for-each>

              options = AXIS2_STUB_GET_OPTIONS( stub, env);
              if ( NULL == options )
              {
                AXIS2_LOG_ERROR( env->log, AXIS2_LOG_SI, "options is null in stub: Error code:"
                        " %d :: %s", env->error->error_number,
                        AXIS2_ERROR_GET_MESSAGE(env->error));
                return;
              }
              svc_client = AXIS2_STUB_GET_SVC_CLIENT (stub, env );
              soap_action = AXIS2_OPTIONS_GET_ACTION ( options, env );
              if ( NULL == soap_action )
              {
                soap_action = "<xsl:value-of select="$soapAction"/>";
                AXIS2_OPTIONS_SET_ACTION( options, env, soap_action );
              }
              <xsl:choose>
               <xsl:when test="$soapVersion='1.2'">
              AXIS2_OPTIONS_SET_SOAP_VERSION(options, env, AXIOM_SOAP12 );
               </xsl:when>
               <xsl:otherwise>
              AXIS2_OPTIONS_SET_SOAP_VERSION(options, env, AXIOM_SOAP11 );
               </xsl:otherwise>
              </xsl:choose>

              callback = axis2_callback_create(env);
              /* Set our on_complete fucntion pointer to the callback object */
              AXIS2_CALLBACK_SET_ON_COMPLETE(callback, on_complete);
              /* Set our on_error function pointer to the callback object */
              AXIS2_CALLBACK_SET_ON_ERROR(callback, on_error);

              /* Send request */
              AXIS2_SVC_CLIENT_SEND_RECEIVE_NON_BLOCKING(svc_client, env, payload, callback);
           }

           </xsl:if>  <!--close for  test="$isASync='1'-->
                <!-- End of in-out mep -->
         </xsl:if> <!-- close for  test="$mep='http://www.w3.org/2004/08/wsdl/in-out' -->

         <xsl:if test="$mep='10'">
          /**
           * Auto generated method signature
           */

           axis2_status_t
           axis2_<xsl:value-of select="@name"/>( axis2_stub_t* stub, const axis2_env_t* env <xsl:for-each select="input/param[@type!='']"> ,<xsl:variable name="paramtype"><xsl:choose><xsl:when test="@type='org.apache.axiom.om.OMElement'">axiom_node_t*</xsl:when><xsl:otherwise><xsl:if test="@ours">axis2_</xsl:if><xsl:value-of select="@type"></xsl:value-of><xsl:if test="@ours">_t*</xsl:if></xsl:otherwise></xsl:choose></xsl:variable>
                                                <xsl:if test="position()>1">,</xsl:if><xsl:value-of select="$paramtype"/><xsl:text> </xsl:text><xsl:value-of select="@name"/>
                                                </xsl:for-each>)
           {
              axis2_status_t status;

              axis2_svc_client_t* svc_client = NULL;
              axis2_options_t *options = NULL;

              axis2_char_t* soap_action = NULL;
              axis2_qname_t *op_qname =  NULL;
              axiom_node_t* payload = NULL;

             <xsl:for-each select="input/param[@type!='' and @ours]">
                <xsl:if test="position()=1"> <!--declare only once-->
                  /* declarations for temp nodes to build input message */
                    
                  <xsl:if test="$style='rpc'">
                  axiom_element_t* payload_ele = NULL;
                   axiom_attribute_t* attri1 = NULL;
                   axiom_namespace_t* ns1 = NULL;
                   axiom_namespace_t* xsi = NULL;
                   axiom_namespace_t* xsd = NULL;
                  </xsl:if>
                </xsl:if>
              </xsl:for-each>

             <xsl:for-each select="input/param[@type!='']">
             <!-- for service client currently suppported only 1 input param -->
             <xsl:variable name="paramtype"><xsl:choose><xsl:when test="@type='org.apache.axiom.om.OMElement'">axiom_node_t*</xsl:when><xsl:otherwise><xsl:if test="@ours">axis2_</xsl:if><xsl:value-of select="@type"></xsl:value-of><xsl:if test="@ours">_t*</xsl:if></xsl:otherwise></xsl:choose></xsl:variable>
             <xsl:variable name="capsparamtype"><xsl:if test="@ours"></xsl:if><xsl:choose><xsl:when test="@type='org.apache.axiom.om.OMElement'">AXIOM_NODE</xsl:when><xsl:otherwise>AXIS2_<xsl:value-of select="@caps-type"></xsl:value-of></xsl:otherwise></xsl:choose></xsl:variable>
                <xsl:choose>
                    <xsl:when test="not(@ours)">
                       <xsl:if test="position()=1">
                         payload = <xsl:value-of select="@name"/>;
                       </xsl:if>
                    </xsl:when>
                    <xsl:otherwise>
                      <xsl:if test="position()=1">
                      </xsl:if>
                      <xsl:choose>
                      <xsl:when test="$style='rpc'">
                      <xsl:if test="position()=1">
                          payload = <xsl:value-of select="$capsparamtype"/>_BUILD_OM(<xsl:value-of select="@name"/>, env, NULL, xsi, xsd );
                          payload_ele =    AXIOM_NODE_GET_DATA_ELEMENT ( payload, env );
                          xsi =
                           axiom_namespace_create (env,
                                                      <xsl:choose><xsl:when test="$soapVersion='1.2'">"http://www.w3.org/2001/XMLSchema-instance"</xsl:when><xsl:when test="$soapVersion='1.1'">"http://www.w3.org/1999/XMLSchema-instance"</xsl:when></xsl:choose>,
                                                      "xsi");
                          AXIOM_ELEMENT_DECLARE_NAMESPACE (payload_ele, env,
                                                     payload, xsi);
                          xsd =
                           axiom_namespace_create (env,
                                                      <xsl:choose><xsl:when test="$soapVersion='1.2'">"http://www.w3.org/2001/XMLSchema"</xsl:when><xsl:when test="$soapVersion='1.1'">"http://www.w3.org/1999/XMLSchema"</xsl:when></xsl:choose>,
                                                      "xsd");
                          AXIOM_ELEMENT_DECLARE_NAMESPACE (payload_ele, env,
                                                     payload, xsd);
                          ns1 =
                           axiom_namespace_create (env,
                                                      <xsl:choose><xsl:when test="$soapVersion='1.2'">AXIOM_SOAP12_SOAP_ENVELOPE_NAMESPACE_URI</xsl:when><xsl:when test="$soapVersion='1.1'">AXIOM_SOAP11_SOAP_ENVELOPE_NAMESPACE_URI</xsl:when></xsl:choose>,
                                                      "soapenv");
                          AXIOM_ELEMENT_DECLARE_NAMESPACE (payload_ele, env,
                                                     payload, ns1);
                          attri1 =
                           axiom_attribute_create (env, "encodingStyle",
                                                      "http://schemas.xmlsoap.org/soap/encoding/",
                                                      ns1);
                          AXIOM_ELEMENT_ADD_ATTRIBUTE (payload_ele, env,
                                                     attri1, payload );
                          </xsl:if>
                      </xsl:when>
                      <xsl:otherwise>
                          payload = <xsl:value-of select="$capsparamtype"/>_BUILD_OM(<xsl:value-of select="@name"/>, env, NULL, NULL, NULL );

                      </xsl:otherwise>
                      </xsl:choose>
                       <!--should this omit in doclit type?-->
                       <!--AXIOM_ELEMENT_SET_NAMESPACE ( payload_ele, env, payload_ns, payload );-->
                    </xsl:otherwise>
                </xsl:choose>
             </xsl:for-each>


              options = AXIS2_STUB_GET_OPTIONS( stub, env);
              if ( NULL == options )
              {
                AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "options is null in stub: Error code:"
                     " %d :: %s", env->error->error_number,
                        AXIS2_ERROR_GET_MESSAGE(env->error));
                return AXIS2_FAILURE;
              }
              svc_client = AXIS2_STUB_GET_SVC_CLIENT (stub, env );
              soap_action = AXIS2_OPTIONS_GET_ACTION ( options, env );
              if ( NULL == soap_action )
              {
                soap_action = "<xsl:value-of select="$soapAction"/>";
                AXIS2_OPTIONS_SET_ACTION( options, env, soap_action );
              }
              <xsl:choose>
               <xsl:when test="$soapVersion='1.2'">
              AXIS2_OPTIONS_SET_SOAP_VERSION(options, env, AXIOM_SOAP12 );
               </xsl:when>
               <xsl:otherwise>
              AXIS2_OPTIONS_SET_SOAP_VERSION(options, env, AXIOM_SOAP11 );
               </xsl:otherwise>
              </xsl:choose>
              op_qname = axis2_qname_create(env,
                                          "<xsl:value-of select="@name"/>" ,
                                          "<xsl:value-of select="@namespace"/>",
                                          NULL);
              status =  AXIS2_SVC_CLIENT_SEND_ROBUST_WITH_OP_QNAME( svc_client, env, op_qname, payload);
              return status;

          }
         </xsl:if> <!-- close for  test="$mep='http://www.w3.org/2004/08/wsdl/in-only' -->
    </xsl:for-each>   <!-- close of for-each select = "method" -->
   </xsl:template>
</xsl:stylesheet>
