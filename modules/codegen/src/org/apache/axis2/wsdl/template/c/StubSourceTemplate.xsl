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
        axis2_create_<xsl:value-of select="$interfaceName"/>_stub (axis2_env_t **env,
                                        axis2_char_t *client_home,
                                        axis2_endpoint_ref_t *endpoint_ref)
        {
           axis2_stub_t* stub = NULL;
           AXIS2_FUNC_PARAM_CHECK ( client_home, env, NULL)
           
           if (NULL == endpoint_ref )
           {
              endpoint_ref = axis2_get_endpoint_ref_from_wsdl( env );
           }

           stub = axis2_stub_create_with_endpoint_ref_and_client_home ( env, endpoint_ref, client_home );
           axis2_populate_axis_service( stub, env );
           return stub;
        }


        void axis2_populate_axis_service( axis2_stub_t* stub, axis2_env_t** env)
        {
          axis2_svc_client_t* svc_client = NULL;
          axis2_qname_t *svc_qname =  NULL;
          axis2_qname_t *op_qname =  NULL;
          axis2_svc_t* svc = NULL;
          axis2_op_t* op = NULL;

          /*Modifying the Service*/
          svc_client = AXIS2_STUB_GET_SVC_CLIENT (stub, env );
          svc = AXIS2_SVC_CLIENT_GET_AXIS_SERVICE ( svc_client, env );
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
              <xsl:when test="@mep='http://www.w3.org/2004/08/wsdl/in-only'">
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
        axis2_endpoint_ref_t* axis2_get_endpoint_ref_from_wsdl ( axis2_env_t** env )
        {
          axis2_endpoint_ref_t* endpoint_ref = NULL;
          axis2_char_t* address = NULL;
          /*set the address from here*/
        <xsl:for-each select="endpoint">
          <xsl:choose>
            <xsl:when test="position()=1">
              address = "<xsl:value-of select="."/>";
            </xsl:when>
            <xsl:otherwise>
              /* mulitiple address defined*/
            </xsl:otherwise>
           </xsl:choose>
        </xsl:for-each>
          endpoint_ref = axis2_endpoint_ref_create(env, address);
          return endpoint_ref;
        }
     

    <xsl:for-each select="method">
      <xsl:variable name="outputtype"><xsl:value-of select="output/param/@type"></xsl:value-of></xsl:variable>
      <xsl:variable name="style"><xsl:value-of select="@style"></xsl:value-of></xsl:variable>
      <xsl:variable name="soapAction"><xsl:value-of select="@soapaction"></xsl:value-of></xsl:variable>
      <xsl:variable name="mep"><xsl:value-of select="@mep"/></xsl:variable>
	    
      <!-- MTOM -->
      <xsl:variable name="method-name"><xsl:value-of select="@name"/></xsl:variable>
      <xsl:variable name="method-ns"><xsl:value-of select="@namespace"/> </xsl:variable>
      <!-- MTOM -->

      <!-- Code generation for the in-out mep -->
      <xsl:if test="$mep='http://www.w3.org/2004/08/wsdl/in-out'">
        <xsl:if test="$isSync='1'">
          /**
           * Auto generated method signature
           */
           
           <xsl:choose>
           <xsl:when test="$outputtype=''">axis2_status_t</xsl:when>
           <xsl:otherwise>
           axis2_om_node_t*
           </xsl:otherwise>
           </xsl:choose>
           axis2_<xsl:value-of select="@name"/>( axis2_stub_t* stub, axis2_env_t** env <xsl:for-each select="input/param[@type!='']"> ,
                                          axis2_om_node_t*<xsl:text> </xsl:text>payload<!--<xsl:value-of select="@name"/>-->
                                          </xsl:for-each> )
           {
              axis2_svc_client_t* svc_client = NULL;
              axis2_options_t *options = NULL;
              axis2_om_node_t* ret_node = NULL;

              axis2_char_t* soap_action = NULL;
              axis2_qname_t *op_qname =  NULL;

              options = AXIS2_STUB_GET_OPTIONS( stub, env);
              if ( NULL == options )
              {
                AXIS2_LOG_ERROR((*env)->log, AXIS2_LOG_SI, "options is null in stub: Error code:"
                     " %d :: %s", (*env)->error->error_number,
                AXIS2_ERROR_GET_MESSAGE((*env)->error));
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
              AXIS2_OPTIONS_SET_SOAP_VERSION(options, env, AXIS2_SOAP12 );
               </xsl:when>
               <xsl:otherwise>
              AXIS2_OPTIONS_SET_SOAP_VERSION(options, env, AXIS2_SOAP11 );
               </xsl:otherwise>
              </xsl:choose>
              op_qname = axis2_qname_create(env,
                                          "<xsl:value-of select="@name"/>" ,
                                          "<xsl:value-of select="@namespace"/>",
                                          NULL);
              ret_node =  AXIS2_SVC_CLIENT_SEND_RECEIVE_WITH_OP_QNAME( svc_client, env, op_qname, payload);
              return ret_node;
        
          }
          </xsl:if>  <!--close for  test="$isSync='1'-->
          <!-- Async method generation -->
          <xsl:if test="$isAsync='1'">
          /**
           * Auto generated method signature for Asynchronous Invocations
           */
           <xsl:variable name="callbackoncomplete"><xsl:value-of select="$callbackname"></xsl:value-of><xsl:text>_on_complete</xsl:text></xsl:variable>
           <xsl:variable name="callbackonerror"><xsl:value-of select="$callbackname"></xsl:value-of><xsl:text>_on_error</xsl:text></xsl:variable>
           void axis2_start_<xsl:value-of select="@name"/>( axis2_stub_t* stub, axis2_env_t** env, <xsl:for-each select="input/param[@type!='']">
                                                    axis2_om_node_t*<xsl:text> </xsl:text>payload<!--<xsl:value-of select="@name"></xsl:value-of>--> ,
                                                    </xsl:for-each>
                                                    axis2_status_t ( AXIS2_CALL *on_complete ) (struct axis2_callback *, axis2_env_t** ) ,
                                                    axis2_status_t ( AXIS2_CALL *on_error ) (struct axis2_callback *, axis2_env_t**, int ) )
           {

              axis2_callback_t *callback = NULL;
              axis2_svc_client_t* svc_client = NULL;
              axis2_options_t *options = NULL;

              axis2_char_t* soap_action = NULL;

              options = AXIS2_STUB_GET_OPTIONS( stub, env);
              if ( NULL == options )
              {
                AXIS2_LOG_ERROR((*env)->log, AXIS2_LOG_SI, "options is null in stub: Error code:"
                     " %d :: %s", (*env)->error->error_number,
                AXIS2_ERROR_GET_MESSAGE((*env)->error));
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
              AXIS2_OPTIONS_SET_SOAP_VERSION(options, env, AXIS2_SOAP12 );
               </xsl:when>
               <xsl:otherwise>
              AXIS2_OPTIONS_SET_SOAP_VERSION(options, env, AXIS2_SOAP11 );
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

         <xsl:if test="$mep='http://www.w3.org/2004/08/wsdl/in-only'">
          /**
           * Auto generated method signature
           */
           
           <xsl:choose>
           <xsl:when test="$outputtype=''">axis2_status_t </xsl:when>
           <xsl:otherwise>
           axis2_om_node_t*
           </xsl:otherwise>
           </xsl:choose>
           axis2_<xsl:value-of select="@name"/>( axis2_stub_t* stub, axis2_env_t** env <xsl:for-each select="input/param[@type!='']"> ,
                                          axis2_om_node_t*<xsl:text> </xsl:text>payload<!--<xsl:value-of select="@name"/>-->
                                          </xsl:for-each> )
           {
              axis2_svc_client_t* svc_client = NULL;
              axis2_options_t *options = NULL;
              int status;

              axis2_char_t* soap_action = NULL;
              axis2_qname_t *op_qname =  NULL;

              options = AXIS2_STUB_GET_OPTIONS( stub, env);
              if ( NULL == options )
              {
                AXIS2_LOG_ERROR((*env)->log, AXIS2_LOG_SI, "options is null in stub: Error code:"
                     " %d :: %s", (*env)->error->error_number,
                AXIS2_ERROR_GET_MESSAGE((*env)->error));
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
              AXIS2_OPTIONS_SET_SOAP_VERSION(options, env, AXIS2_SOAP12 );
               </xsl:when>
               <xsl:otherwise>
              AXIS2_OPTIONS_SET_SOAP_VERSION(options, env, AXIS2_SOAP11 );
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
