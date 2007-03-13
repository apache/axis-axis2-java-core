<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="text"/>

    <xsl:template match="/class">
      <xsl:variable name="interfaceName"><xsl:value-of select="@interfaceName"/></xsl:variable>
      <xsl:variable name="package"><xsl:value-of select="@package"/></xsl:variable>
      <xsl:variable name="isSync"><xsl:value-of select="@isSync"/></xsl:variable>
      <xsl:variable name="isAsync"><xsl:value-of select="@isAsync"/></xsl:variable>
      <xsl:variable name="soapVersion"><xsl:value-of select="@soap-version"/></xsl:variable>
      <xsl:variable name="callbackname"><xsl:value-of select="@callbackname"/></xsl:variable>
      <xsl:variable name="method-prefix"><xsl:value-of select="@prefix"/></xsl:variable>
      <xsl:variable name="qname"><xsl:value-of select="@qname"/></xsl:variable>

      /**
       * <xsl:value-of select="@name"/>.c
       *
       * This file was auto-generated from WSDL for "<xsl:value-of select="$qname"/>" service
       * by the Apache Axis2/C version: #axisVersion# #today#
       */

      #include "<xsl:value-of select="@name"/>.h"

      /**
       * <xsl:value-of select="@name"/> C implementation
       */

      axis2_stub_t*
      <xsl:value-of select="$method-prefix"/>_create (const axis2_env_t *env,
                                      axis2_char_t *client_home,
                                      axis2_char_t *endpoint_uri)
      {
         axis2_stub_t *stub = NULL;
         axis2_endpoint_ref_t *endpoint_ref = NULL;
         AXIS2_FUNC_PARAM_CHECK ( client_home, env, NULL)

         if (NULL == endpoint_uri )
         {
            endpoint_uri = <xsl:value-of select="$method-prefix"/>_get_endpoint_uri_from_wsdl( env );
         }

         endpoint_ref = axis2_endpoint_ref_create(env, endpoint_uri);

         stub = axis2_stub_create_with_endpoint_ref_and_client_home ( env, endpoint_ref, client_home );
         <xsl:value-of select="$method-prefix"/>_populate_services( stub, env );
         return stub;
      }


      void <xsl:value-of select="$method-prefix"/>_populate_services( axis2_stub_t *stub, const axis2_env_t *env)
      {
         axis2_svc_client_t *svc_client = NULL;
         axis2_qname_t *svc_qname =  NULL;
         axis2_qname_t *op_qname =  NULL;
         axis2_svc_t *svc = NULL;
         axis2_op_t *op = NULL;

         /* Modifying the Service */
         svc_client = AXIS2_STUB_GET_SVC_CLIENT (stub, env );
         svc = (axis2_svc_t*)AXIS2_SVC_CLIENT_GET_AXIS_SERVICE ( svc_client, env );
         axis2_qname_create(env,"<xsl:value-of select="@servicename"/>" ,NULL, NULL);
         AXIS2_SVC_SET_QNAME (svc, env, svc_qname);

         /* creating the operations*/

         <xsl:for-each select="method">

           op_qname = axis2_qname_create(env,
                                         "<xsl:value-of select="@localpart"/>" ,
                                         "<xsl:value-of select="@namespace"/>",
                                         NULL);
           op = axis2_op_create_with_qname(env, op_qname);
           <xsl:choose>
             <xsl:when test="@mep='10'">
               axis2_op_set_msg_exchange_pattern(op, env, AXIS2_MEP_URI_IN_ONLY);
             </xsl:when>
             <xsl:otherwise>
               axis2_op_set_msg_exchange_pattern(op, env, AXIS2_MEP_URI_OUT_IN);
             </xsl:otherwise>
           </xsl:choose>
           AXIS2_SVC_ADD_OP(svc, env, op);

         </xsl:for-each>
      }

      /**
       *return end point picked from wsdl
       */
      axis2_char_t*
      <xsl:value-of select="$method-prefix"/>_get_endpoint_uri_from_wsdl ( const axis2_env_t *env )
      {
        axis2_char_t *endpoint_uri = NULL;
        /* set the address from here */
        <xsl:for-each select="endpoint">
          <xsl:choose>
            <xsl:when test="position()=1">
              endpoint_uri = "<xsl:value-of select="."/>";
            </xsl:when>
           </xsl:choose>
        </xsl:for-each>
        return endpoint_uri;
      }


  <xsl:for-each select="method">
    <xsl:variable name="outputours"><xsl:value-of select="output/param/@ours"></xsl:value-of></xsl:variable>
    <xsl:variable name="outputtype">
      <xsl:choose>
        <xsl:when test="output/param/@ours">axis2_<xsl:value-of select="output/param/@type"></xsl:value-of>_t*</xsl:when>
        <xsl:otherwise><xsl:value-of select="output/param/@type"></xsl:value-of></xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="caps-outputtype"><xsl:value-of select="output/param/@caps-type"></xsl:value-of></xsl:variable>
    <xsl:variable name="style"><xsl:value-of select="@style"></xsl:value-of></xsl:variable>
    <xsl:variable name="soapAction"><xsl:value-of select="@soapaction"></xsl:value-of></xsl:variable>
    <xsl:variable name="mep"><xsl:value-of select="@mep"/></xsl:variable>

    <xsl:variable name="method-name"><xsl:value-of select="@name"/></xsl:variable>
    <xsl:variable name="method-ns"><xsl:value-of select="@namespace"/> </xsl:variable>

    <!-- Code generation for the in-out mep -->
    <xsl:if test="$mep='12'">
      <xsl:if test="$isSync='1'">
         /**
          * auto generated method signature
          * for "<xsl:value-of select="@qname"/>" operation.
          <xsl:for-each select="input/param[@type!='']">* @param <xsl:value-of select="@name"></xsl:value-of></xsl:for-each>
          * @return
          */
         <xsl:choose>
         <xsl:when test="$outputtype=''">void</xsl:when> <!--this case is unexpected-->
         <xsl:otherwise><xsl:value-of select="$outputtype"/></xsl:otherwise>
         </xsl:choose>
         <xsl:text> </xsl:text>
         <xsl:value-of select="$method-prefix"/>_<xsl:value-of select="@name"/>( axis2_stub_t *stub, const axis2_env_t *env<xsl:for-each select="input/param[@type!='']">,
                                              <xsl:variable name="inputtype">
                                                  <xsl:if test="@ours">axis2_</xsl:if><xsl:value-of select="@type"/><xsl:if test="@ours">_t*</xsl:if>
                                              </xsl:variable>
                                              <xsl:if test="position()>1">,</xsl:if><xsl:value-of select="$inputtype"/><xsl:text> </xsl:text><xsl:value-of select="@name"/>
                                              </xsl:for-each>)
         {
            axis2_svc_client_t *svc_client = NULL;
            axis2_options_t *options = NULL;
            axiom_node_t *ret_node = NULL;

            const axis2_char_t *soap_action = NULL;
            axis2_qname_t *op_qname =  NULL;
            axiom_node_t *payload = NULL;
            <xsl:if test="output/param/@ours">
           	    <!-- this means data binding is enable -->
                <xsl:value-of select="$outputtype"/> ret_val = NULL;
            </xsl:if>


            <!-- for service client currently suppported only 1 input param -->
            <xsl:for-each select="input/param[@type!='']">
                <xsl:if test="position()=1">
                    <xsl:choose>
                        <xsl:when test="@ours">
                            payload = AXIS2_<xsl:value-of select="@caps-type"/>_SERIALIZE(<xsl:value-of select="@name"/>, env, NULL, AXIS2_FALSE);
                        </xsl:when>
                        <xsl:otherwise>
                            payload = <xsl:value-of select="@name"/>;
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:if>
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
            <xsl:if test="$soapVersion='1.2'">
            AXIS2_OPTIONS_SET_SOAP_VERSION(options, env, AXIOM_SOAP12 );
            </xsl:if>
            <xsl:if test="$soapVersion!='1.1'">
            AXIS2_OPTIONS_SET_SOAP_VERSION(options, env, AXIOM_SOAP11 );
            </xsl:if>
            op_qname = axis2_qname_create(env,
                                        "<xsl:value-of select="@localpart"/>" ,
                                        "<xsl:value-of select="@namespace"/>",
                                        NULL);
            ret_node =  AXIS2_SVC_CLIENT_SEND_RECEIVE_WITH_OP_QNAME( svc_client, env, op_qname, payload);


            <xsl:choose>
                <xsl:when test="$outputtype=''">
                    return;
                </xsl:when>
                <xsl:when test="output/param/@ours">
                    if ( NULL == ret_node )
                    {
                        return NULL;
                    }
                    ret_val = axis2_<xsl:value-of select="output/param/@type"/>_create(env);

                    AXIS2_<xsl:value-of select="$caps-outputtype"/>_DESERIALIZE(ret_val, env, ret_node );
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
          * auto generated method signature for asynchronous invocations
          * for "<xsl:value-of select="@qname"/>" operation.
          <!--  select only the body parameters  -->
          <xsl:for-each select="input/param[@type!='']">* @param <xsl:value-of select="@name"></xsl:value-of></xsl:for-each>
          * @param on_complete callback to handle on complete
          * @param on_error callback to handle on error
          */
         <xsl:variable name="callbackoncomplete"><xsl:value-of select="$callbackname"></xsl:value-of><xsl:text>_on_complete</xsl:text></xsl:variable>
         <xsl:variable name="callbackonerror"><xsl:value-of select="$callbackname"></xsl:value-of><xsl:text>_on_error</xsl:text></xsl:variable>
         void <xsl:value-of select="$method-prefix"/>_<xsl:value-of select="@name"/>_start( axis2_stub_t *stub, const axis2_env_t *env<xsl:for-each select="input/param[@type!='']">,
                                                    <xsl:variable name="inputtype">
                                                        <xsl:if test="@ours">axis2_</xsl:if><xsl:value-of select="@type"/><xsl:if test="@ours">_t*</xsl:if>
                                                    </xsl:variable>
                                                    <xsl:if test="position()>1">,</xsl:if><xsl:value-of select="$inputtype"/><xsl:text> </xsl:text><xsl:value-of select="@name"/>
                                                  </xsl:for-each>,
                                                  axis2_status_t ( AXIS2_CALL *on_complete ) (struct axis2_callback *, const axis2_env_t *) ,
                                                  axis2_status_t ( AXIS2_CALL *on_error ) (struct axis2_callback *, const axis2_env_t *, int ) )
         {

            axis2_callback_t *callback = NULL;

            axis2_svc_client_t *svc_client = NULL;
            axis2_options_t *options = NULL;

            const axis2_char_t *soap_action = NULL;
            axiom_node_t *payload = NULL;

            <!-- for service client currently suppported only 1 input param -->
            <xsl:for-each select="input/param[@type!='']">
                <xsl:if test="position()=1">
                    <xsl:choose>
                        <xsl:when test="@ours">
                            payload = AXIS2_<xsl:value-of select="@caps-type"/>_SERIALIZE(<xsl:value-of select="@name"/>, env, NULL, AXIS2_FALSE);
                        </xsl:when>
                        <xsl:otherwise>
                            payload = <xsl:value-of select="@name"/>;
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:if>
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
          * auto generated method signature for in only mep invocations
          * for "<xsl:value-of select="@qname"/>" operation.
          <!--  select only the body parameters  -->
          <xsl:for-each select="input/param[@type!='']">* @param <xsl:value-of select="@name"></xsl:value-of></xsl:for-each>
          * @param on_complete callback to handle on complete
          * @param on_error callback to handle on error
          */
         axis2_status_t
         <xsl:value-of select="$method-prefix"/>_<xsl:value-of select="@name"/>( axis2_stub_t *stub, const axis2_env_t *env <xsl:for-each select="input/param[@type!='']"> ,
                                                 <xsl:variable name="inputtype">
                                                    <xsl:if test="@ours">axis2_</xsl:if><xsl:value-of select="@type"/><xsl:if test="@ours">_t*</xsl:if>
                                                 </xsl:variable>
                                                 <xsl:if test="position()>1">,</xsl:if><xsl:value-of select="$inputtype"/><xsl:text> </xsl:text><xsl:value-of select="@name"/>
                                              </xsl:for-each>)
         {
            axis2_status_t status;

            axis2_svc_client_t *svc_client = NULL;
            axis2_options_t *options = NULL;

            const axis2_char_t *soap_action = NULL;
            axis2_qname_t *op_qname =  NULL;
            axiom_node_t *payload = NULL;

            <!-- for service client currently suppported only 1 input param -->
            <xsl:for-each select="input/param[@type!='']">
                <xsl:if test="position()=1">
                    <xsl:choose>
                        <xsl:when test="@ours">
                            payload = AXIS2_<xsl:value-of select="@caps-type"/>_SERIALIZE(<xsl:value-of select="@name"/>, env, NULL, AXIS2_FALSE);
                        </xsl:when>
                        <xsl:otherwise>
                            payload = <xsl:value-of select="@name"/>;
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:if>
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
                                        "<xsl:value-of select="@localpart"/>" ,
                                        "<xsl:value-of select="@namespace"/>",
                                        NULL);
            status =  AXIS2_SVC_CLIENT_SEND_ROBUST_WITH_OP_QNAME( svc_client, env, op_qname, payload);
            return status;

        }
       </xsl:if> <!-- close for  test="$mep='http://www.w3.org/2004/08/wsdl/in-only' -->
     </xsl:for-each>   <!-- close of for-each select = "method" -->
   </xsl:template>
</xsl:stylesheet>
