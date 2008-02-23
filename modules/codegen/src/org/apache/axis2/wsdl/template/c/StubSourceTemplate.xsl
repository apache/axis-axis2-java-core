<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="text"/>

    
    <xsl:template match="/class">
      <xsl:variable name="interfaceName"><xsl:value-of select="@interfaceName"/></xsl:variable>
      <xsl:variable name="package"><xsl:value-of select="@package"/></xsl:variable>
      <xsl:variable name="isSync"><xsl:value-of select="@isSync"/></xsl:variable>
      <xsl:variable name="isAsync"><xsl:value-of select="@isAsync"/></xsl:variable>
      <xsl:variable name="soapVersion"><xsl:value-of select="@soap-version"/></xsl:variable>
      <xsl:variable name="callbackname"><xsl:value-of select="@callbackname"/></xsl:variable>
      <xsl:variable name="method-prefix"><xsl:value-of select="@prefix"/></xsl:variable> <!-- This is no longer using -->
      <xsl:variable name="qname"><xsl:value-of select="@qname"/></xsl:variable>
      <xsl:variable name="servicename"><xsl:value-of select="@servicename"/></xsl:variable>

      /**
       * <xsl:value-of select="@name"/>.c
       *
       * This file was auto-generated from WSDL for "<xsl:value-of select="$qname"/>" service
       * by the Apache Axis2/Java version: #axisVersion# #today#
       */

      #include "<xsl:value-of select="@name"/>.h"
      #include &lt;axis2_msg.h&gt;

      /**
       * <xsl:value-of select="@name"/> C implementation
       */

      axis2_stub_t*
      axis2_stub_create_<xsl:value-of select="$servicename"/>(const axutil_env_t *env,
                                      axis2_char_t *client_home,
                                      axis2_char_t *endpoint_uri)
      {
         axis2_stub_t *stub = NULL;
         axis2_endpoint_ref_t *endpoint_ref = NULL;
         AXIS2_FUNC_PARAM_CHECK (client_home, env, NULL)

         if (NULL == endpoint_uri)
         {
            endpoint_uri = axis2_stub_get_endpoint_uri_of_<xsl:value-of select="$servicename"/>(env);
         }

         endpoint_ref = axis2_endpoint_ref_create(env, endpoint_uri);

         stub = axis2_stub_create_with_endpoint_ref_and_client_home (env, endpoint_ref, client_home);

         if (NULL == stub)
         {
            if(NULL != endpoint_ref)
            {
                axis2_endpoint_ref_free(endpoint_ref, env);
            }
            return NULL;
         }


         axis2_stub_populate_services_for_<xsl:value-of select="$servicename"/>(stub, env);
         return stub;
      }


      void
      axis2_stub_populate_services_for_<xsl:value-of select="$servicename"/>(axis2_stub_t *stub, const axutil_env_t *env)
      {
         axis2_svc_client_t *svc_client = NULL;
         axutil_qname_t *svc_qname =  NULL;
         axutil_qname_t *op_qname =  NULL;
         axis2_svc_t *svc = NULL;
         axis2_op_t *op = NULL;
         axis2_op_t *annon_op = NULL;
         axis2_msg_t *msg_out = NULL;
         axis2_msg_t *msg_in = NULL;
         axis2_msg_t *msg_out_fault = NULL;
         axis2_msg_t *msg_in_fault = NULL;


         /* Modifying the Service */
         svc_client = axis2_stub_get_svc_client (stub, env );
         svc = (axis2_svc_t*)axis2_svc_client_get_svc( svc_client, env );

         annon_op = axis2_svc_get_op_with_name(svc, env, AXIS2_ANON_OUT_IN_OP);
         msg_out = axis2_op_get_msg(annon_op, env, AXIS2_MSG_OUT);
         msg_in = axis2_op_get_msg(annon_op, env, AXIS2_MSG_IN);
         msg_out_fault = axis2_op_get_msg(annon_op, env, AXIS2_MSG_OUT_FAULT);
         msg_in_fault = axis2_op_get_msg(annon_op, env, AXIS2_MSG_IN_FAULT);

         svc_qname = axutil_qname_create(env,"<xsl:value-of select="@servicename"/>" ,NULL, NULL);
         axis2_svc_set_qname (svc, env, svc_qname);

         /* creating the operations*/

         <xsl:for-each select="method">
           op_qname = axutil_qname_create(env,
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
           axis2_msg_increment_ref(msg_out, env);
           axis2_msg_increment_ref(msg_in, env);
           axis2_msg_increment_ref(msg_out_fault, env);
           axis2_msg_increment_ref(msg_in_fault, env);
           axis2_op_add_msg(op, env, AXIS2_MSG_OUT, msg_out);
           axis2_op_add_msg(op, env, AXIS2_MSG_IN, msg_in);
           axis2_op_add_msg(op, env, AXIS2_MSG_OUT_FAULT, msg_out_fault);
           axis2_op_add_msg(op, env, AXIS2_MSG_IN_FAULT, msg_in_fault);
           
           axis2_svc_add_op(svc, env, op);

         </xsl:for-each>
      }

      /**
       *return end point picked from wsdl
       */
      axis2_char_t*
      axis2_stub_get_endpoint_uri_of_<xsl:value-of select="$servicename"/>( const axutil_env_t *env )
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
        <xsl:when test="output/param/@ours">
            <xsl:choose>
                    <xsl:when test="not(@type='char' or @type='bool' or @type='date_time' or @type='duration')">adb_<xsl:value-of select="output/param/@type"/>_t*</xsl:when>
                    <xsl:when test="@type='duration' or @type='date_time' or @type='uri' or @type='qname' or @type='base64_binary'">axutil_<xsl:value-of select="@type"/>_t*</xsl:when>
                    <xsl:otherwise>axis2_<xsl:value-of select="output/param/@type"/>_t*</xsl:otherwise>
            </xsl:choose>
        </xsl:when>
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
          *<xsl:for-each select="input/param[@type!='']"><xsl:text>
          </xsl:text>* @param _<xsl:value-of select="@name"/></xsl:for-each>
          * @return
          */
         <xsl:choose>
         <xsl:when test="$outputtype=''">void</xsl:when> <!--this case is unexpected-->
         <xsl:otherwise><xsl:value-of select="$outputtype"/></xsl:otherwise>
         </xsl:choose>
         <xsl:text> </xsl:text>
         axis2_stub_op_<xsl:value-of select="$servicename"/>_<xsl:value-of select="@name"/>( axis2_stub_t *stub, const axutil_env_t *env<xsl:for-each select="input/param[@type!='']">,
                                              <xsl:variable name="inputtype"><xsl:if test="@ours">adb_</xsl:if><xsl:value-of select="@type"/><xsl:if test="@ours">_t*</xsl:if></xsl:variable>
                                              <xsl:value-of select="$inputtype"/><xsl:text> _</xsl:text><xsl:value-of select="@name"/>
                                              </xsl:for-each>)
         {
            axis2_svc_client_t *svc_client = NULL;
            axis2_options_t *options = NULL;
            axiom_node_t *ret_node = NULL;

            const axis2_char_t *soap_action = NULL;
            axutil_qname_t *op_qname =  NULL;
            axiom_node_t *payload = NULL;
            axis2_bool_t is_soap_act_set = AXIS2_TRUE;
            <xsl:if test="$style='doc'">
            axutil_string_t *soap_act = NULL;
            </xsl:if>    
            <xsl:if test="output/param/@ours">
           	    <!-- this means data binding is enable -->
                <xsl:value-of select="$outputtype"/> ret_val = NULL;
            </xsl:if>


            <!-- for service client currently suppported only 1 input param -->
            <xsl:variable name="firstParam" select="input/param[1]"/>
            <xsl:if test="$firstParam/@type!=''">
               <xsl:choose>
                   <xsl:when test="$firstParam/@ours">
                       payload = adb_<xsl:value-of select="$firstParam/@type"/>_serialize(_<xsl:value-of select="$firstParam/@name"/>, env, NULL, NULL, AXIS2_TRUE, NULL, NULL);
                   </xsl:when>
                   <xsl:otherwise>
                       payload = _<xsl:value-of select="$firstParam/@name"/>;
                   </xsl:otherwise>
               </xsl:choose>
            </xsl:if>

            options = axis2_stub_get_options( stub, env);
            if ( NULL == options )
            {
                AXIS2_ERROR_SET(env->error, AXIS2_ERROR_INVALID_NULL_PARAM, AXIS2_FAILURE);
                AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "options is null in stub");
                return NULL;
            }
            svc_client = axis2_stub_get_svc_client(stub, env );
            soap_action = axis2_options_get_action( options, env );
            if (NULL == soap_action)
            {
              is_soap_act_set = AXIS2_FALSE;
              soap_action = "<xsl:value-of select="$soapAction"/>";
              <xsl:if test="$style='doc'">
              soap_act = axutil_string_create(env, "<xsl:value-of select="$soapAction"/>");
              axis2_options_set_soap_action(options, env, soap_act);    
              </xsl:if>
              axis2_options_set_action( options, env, soap_action );
            }
            <xsl:if test="$soapVersion='1.2'">
            axis2_options_set_soap_version(options, env, AXIOM_SOAP12 );
            </xsl:if>
            <xsl:if test="$soapVersion!='1.1'">
            axis2_options_set_soap_version(options, env, AXIOM_SOAP11 );
            </xsl:if>
            ret_node =  axis2_svc_client_send_receive_with_op_qname( svc_client, env, op_qname, payload);
 
            if (!is_soap_act_set)
            {
              <xsl:if test="$style='doc'">
              axis2_options_set_soap_action(options, env, NULL);    
              </xsl:if>
              axis2_options_set_action( options, env, NULL);
            }

            <xsl:choose>
                <xsl:when test="$outputtype=''">
                    return;
                </xsl:when>
                <xsl:when test="output/param/@ours">
                    if ( NULL == ret_node )
                    {
                        return NULL;
                    }
                    ret_val = adb_<xsl:value-of select="output/param/@type"/>_create(env);

                    if(adb_<xsl:value-of select="output/param/@type"/>_deserialize(ret_val, env, &amp;ret_node, NULL, AXIS2_FALSE ) == AXIS2_FAILURE)
                    {
                        if(ret_val != NULL)
                        {
                            adb_<xsl:value-of select="output/param/@type"/>_free(ret_val, env);
                        }

                        AXIS2_LOG_ERROR( env->log, AXIS2_LOG_SI, "NULL returnted from the <xsl:value-of select="output/param/@type"/>_deserialize: "
                                                                "This should be due to an invalid XML");
                        return NULL;
                    }
                    return ret_val;
                </xsl:when>
                <xsl:otherwise>
                    return ret_node;
                </xsl:otherwise>
            </xsl:choose>
        }
        </xsl:if>  <!--close for  test="$isSync='1'-->
       </xsl:if> <!-- close for  test="$mep='http://www.w3.org/2004/08/wsdl/in-out' -->

  </xsl:for-each>



  <xsl:for-each select="method">
    <xsl:variable name="outputours"><xsl:value-of select="output/param/@ours"></xsl:value-of></xsl:variable>
    <xsl:variable name="outputtype">
      <xsl:choose>
        <xsl:when test="output/param/@ours">
            <xsl:choose>
                    <xsl:when test="not(@type='char' or @type='bool' or @type='date_time' or @type='duration')">adb_<xsl:value-of select="output/param/@type"/>_t*</xsl:when>
                    <xsl:when test="@type='duration' or @type='date_time' or @type='uri' or @type='qname' or @type='base64_binary'">axutil_<xsl:value-of select="@type"/>_t*</xsl:when>
                    <xsl:otherwise>axis2_<xsl:value-of select="output/param/@type"/>_t*</xsl:otherwise>
                </xsl:choose>
            
                </xsl:when>
        <xsl:otherwise><xsl:value-of select="output/param/@type"></xsl:value-of></xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="caps-outputtype"><xsl:value-of select="output/param/@caps-type"></xsl:value-of></xsl:variable>
    <xsl:variable name="style"><xsl:value-of select="@style"></xsl:value-of></xsl:variable>
    <xsl:variable name="soapAction"><xsl:value-of select="@soapaction"></xsl:value-of></xsl:variable>
    <xsl:variable name="mep"><xsl:value-of select="@mep"/></xsl:variable>

    <xsl:variable name="method-name"><xsl:value-of select="@name"/></xsl:variable>
    <xsl:variable name="method-ns"><xsl:value-of select="@namespace"/> </xsl:variable>


     <xsl:if test="$mep='12'">
        <!-- Async method generation -->
        <xsl:if test="$isAsync='1'">

        struct axis2_stub_<xsl:value-of select="$servicename"/>_<xsl:value-of select="@name"/>_callback_data
        {   
            void *data;
            axis2_status_t ( AXIS2_CALL *on_complete ) (const axutil_env_t *, <xsl:value-of select="$outputtype"/><xsl:text> _</xsl:text><xsl:value-of select="output/param/@name"/>, void *data);
            axis2_status_t ( AXIS2_CALL *on_error ) (const axutil_env_t *, int exception, void *data);
        };

        static axis2_status_t AXIS2_CALL axis2_stub_on_error_<xsl:value-of select="$servicename"/>_<xsl:value-of select="@name"/>(axis2_callback_t *callback, const axutil_env_t *env, int exception)
        {
            axis2_status_t ( AXIS2_CALL *on_error ) (const axutil_env_t *, int, void *data);
            struct axis2_stub_<xsl:value-of select="$servicename"/>_<xsl:value-of select="@name"/>_callback_data* callback_data = NULL;
            void *user_data = NULL;

            axis2_status_t status;
        
            callback_data = (struct axis2_stub_<xsl:value-of select="$servicename"/>_<xsl:value-of select="@name"/>_callback_data*)axis2_callback_get_data(callback);
        
            user_data = callback_data->data;
            on_error = callback_data->on_error;
        
            status = on_error(env, exception, user_data);

            if(callback_data)
            {
                AXIS2_FREE(env->allocator, callback_data);
            }
            return status;
        } 

        axis2_status_t AXIS2_CALL axis2_stub_on_complete_<xsl:value-of select="$servicename"/>_<xsl:value-of select="@name"/>(axis2_callback_t *callback, const axutil_env_t *env)
        {
            axis2_status_t ( AXIS2_CALL *on_complete ) (const axutil_env_t *, <xsl:value-of select="$outputtype"/><xsl:text> _</xsl:text><xsl:value-of select="output/param/@name"/>, void *data);
            struct axis2_stub_<xsl:value-of select="$servicename"/>_<xsl:value-of select="@name"/>_callback_data* callback_data = NULL;
            void *user_data = NULL;
            axis2_status_t status;
 
            <xsl:if test="output/param/@ours">
           	    <!-- this means data binding is enable -->
                <xsl:value-of select="$outputtype"/> ret_val = NULL;
            </xsl:if>

            axiom_node_t *ret_node = NULL;
            axiom_soap_envelope_t *soap_envelope = NULL;

            callback_data = (struct axis2_stub_<xsl:value-of select="$servicename"/>_<xsl:value-of select="@name"/>_callback_data*)axis2_callback_get_data(callback);

            soap_envelope = axis2_callback_get_envelope(callback, env);
            if(soap_envelope)
            {
                axiom_soap_body_t *soap_body;
                soap_body = axiom_soap_envelope_get_body(soap_envelope, env);
                if(soap_body)
                {
                    axiom_node_t *body_node = axiom_soap_body_get_base_node(soap_body, env);
                    if(body_node)
                    {
                        ret_node = axiom_node_get_first_child(body_node, env);
                    }
                }
            }

            user_data = callback_data->data;
            on_complete = callback_data->on_complete;

            <xsl:choose>
                <xsl:when test="output/param/@ours">
                    if(ret_node != NULL)
                    {
                        ret_val = adb_<xsl:value-of select="output/param/@type"/>_create(env);
     
                        if(adb_<xsl:value-of select="output/param/@type"/>_deserialize(ret_val, env, &amp;ret_node, NULL, AXIS2_FALSE ) == AXIS2_FAILURE)
                        {
                            AXIS2_LOG_ERROR( env->log, AXIS2_LOG_SI, "NULL returnted from the LendResponse_deserialize: "
                                                                    "This should be due to an invalid XML");
                            adb_<xsl:value-of select="output/param/@type"/>_free(ret_val, env);
                            ret_val = NULL;
                        }
                     }
                     else
                     {
                         ret_val = NULL; 
                     }
                     status = on_complete(env, ret_val, user_data);
                </xsl:when>
                <xsl:otherwise>
                     status = on_complete(env, ret_node, user_data);
                </xsl:otherwise>
            </xsl:choose>
 
            if(callback_data)
            {
                AXIS2_FREE(env->allocator, callback_data);
            }
            return status;
        }

        /**
          * auto generated method signature for asynchronous invocations
          * for "<xsl:value-of select="@qname"/>" operation.
          <!--  select only the body parameters  -->
          *<xsl:for-each select="input/param[@type!='']"><xsl:text>
          </xsl:text>* @param _<xsl:value-of select="@name"/></xsl:for-each>
          * @param on_complete callback to handle on complete
          * @param on_error callback to handle on error
          */
         void axis2_stub_start_op_<xsl:value-of select="$servicename"/>_<xsl:value-of select="@name"/>( axis2_stub_t *stub, const axutil_env_t *env<xsl:for-each select="input/param[@type!='']">,
                                                    <xsl:variable name="inputtype"><xsl:if test="@ours">adb_</xsl:if><xsl:value-of select="@type"/><xsl:if test="@ours">_t*</xsl:if></xsl:variable>
                                                    <xsl:value-of select="$inputtype"/><xsl:text> _</xsl:text><xsl:value-of select="@name"/>
                                                  </xsl:for-each>,
                                                  void *user_data,
                                                  axis2_status_t ( AXIS2_CALL *on_complete ) (const axutil_env_t *, <xsl:value-of select="$outputtype"/><xsl:text> _</xsl:text><xsl:value-of select="output/param/@name"/>, void *data) ,
                                                  axis2_status_t ( AXIS2_CALL *on_error ) (const axutil_env_t *, int exception, void *data) )
         {

            axis2_callback_t *callback = NULL;

            axis2_svc_client_t *svc_client = NULL;
            axis2_options_t *options = NULL;

            const axis2_char_t *soap_action = NULL;
            axiom_node_t *payload = NULL;

            axis2_bool_t is_soap_act_set = AXIS2_TRUE;
            <xsl:if test="$style='doc'">
            axutil_string_t *soap_act = NULL;
            </xsl:if>
            
            struct axis2_stub_<xsl:value-of select="$servicename"/>_<xsl:value-of select="@name"/>_callback_data *callback_data;

            callback_data = (struct axis2_stub_<xsl:value-of select="$servicename"/>_<xsl:value-of select="@name"/>_callback_data*) AXIS2_MALLOC(env->allocator, 
                                    sizeof(struct axis2_stub_<xsl:value-of select="$servicename"/>_<xsl:value-of select="@name"/>_callback_data));
            if(NULL == callback_data)
            {
                AXIS2_LOG_ERROR( env->log, AXIS2_LOG_SI, "Can not allocate memeory for the callback data structures");
                return;
            }
            <!-- for service client currently suppported only 1 input param -->
            <xsl:variable name="firstParam" select="input/param[1]"/>
            <xsl:if test="$firstParam/@type!=''">
               <xsl:choose>
                   <xsl:when test="$firstParam/@ours">
                       payload = adb_<xsl:value-of select="$firstParam/@type"/>_serialize(_<xsl:value-of select="$firstParam/@name"/>, env, NULL, NULL, AXIS2_TRUE, NULL, NULL);
                   </xsl:when>
                   <xsl:otherwise>
                       payload = _<xsl:value-of select="$firstParam/@name"/>;
                   </xsl:otherwise>
               </xsl:choose>
            </xsl:if>



            options = axis2_stub_get_options( stub, env);
            if (NULL == options)
            {
              AXIS2_ERROR_SET(env->error, AXIS2_ERROR_INVALID_NULL_PARAM, AXIS2_FAILURE);
              AXIS2_LOG_ERROR( env->log, AXIS2_LOG_SI, "options is null in stub");
              return;
            }
            svc_client = axis2_stub_get_svc_client (stub, env);
            soap_action =axis2_options_get_action (options, env);
            if (NULL == soap_action)
            {
              is_soap_act_set = AXIS2_FALSE;
              soap_action = "<xsl:value-of select="$soapAction"/>";
              <xsl:if test="$style='doc'">
              soap_act = axutil_string_create(env, "<xsl:value-of select="$soapAction"/>");
              axis2_options_set_soap_action(options, env, soap_act);
              </xsl:if>
              axis2_options_set_action( options, env, soap_action);
            }
            <xsl:choose>
             <xsl:when test="$soapVersion='1.2'">
            axis2_options_set_soap_version(options, env, AXIOM_SOAP12);
             </xsl:when>
             <xsl:otherwise>
            axis2_options_set_soap_version(options, env, AXIOM_SOAP11);
             </xsl:otherwise>
            </xsl:choose>

            callback = axis2_callback_create(env);
            /* Set our on_complete fucntion pointer to the callback object */
            axis2_callback_set_on_complete(callback, axis2_stub_on_complete_<xsl:value-of select="$servicename"/>_<xsl:value-of select="@name"/>);
            /* Set our on_error function pointer to the callback object */
            axis2_callback_set_on_error(callback, axis2_stub_on_error_<xsl:value-of select="$servicename"/>_<xsl:value-of select="@name"/>);

            callback_data-> data = user_data;
            callback_data-> on_complete = on_complete;
            callback_data-> on_error = on_error;

            axis2_callback_set_data(callback, (void*)callback_data);

            /* Send request */
            axis2_svc_client_send_receive_non_blocking(svc_client, env, payload, callback);
            
            if (!is_soap_act_set)
            {
              <xsl:if test="$style='doc'">
              axis2_options_set_soap_action(options, env, NULL);
              </xsl:if>
              axis2_options_set_action(options, env, NULL);
            }
         }

         </xsl:if>  <!--close for  test="$isASync='1'-->
       <!-- End of in-out mep -->
       </xsl:if> <!-- close for  test="$mep='http://www.w3.org/2004/08/wsdl/in-out' -->

       <xsl:if test="$mep='10'">
         /**
          * auto generated method signature for in only mep invocations
          * for "<xsl:value-of select="@qname"/>" operation.
          <!--  select only the body parameters  -->
          <xsl:for-each select="input/param[@type!='']">* @param _<xsl:value-of select="@name"></xsl:value-of></xsl:for-each>
          * @param on_complete callback to handle on complete
          * @param on_error callback to handle on error
          */
         axis2_status_t
         axis2_stub_op_<xsl:value-of select="$servicename"/>_<xsl:value-of select="@name"/>( axis2_stub_t *stub, const axutil_env_t *env <xsl:for-each select="input/param[@type!='']"> ,
                                                 <xsl:variable name="inputtype"><xsl:if test="@ours">adb_</xsl:if><xsl:value-of select="@type"/><xsl:if test="@ours">_t*</xsl:if></xsl:variable>
                                                 <xsl:value-of select="$inputtype"/><xsl:text> _</xsl:text><xsl:value-of select="@name"/>
                                              </xsl:for-each>)
         {
            axis2_status_t status;

            axis2_svc_client_t *svc_client = NULL;
            axis2_options_t *options = NULL;

            const axis2_char_t *soap_action = NULL;
            axutil_qname_t *op_qname =  NULL;
            axiom_node_t *payload = NULL;

            <!-- for service client currently suppported only 1 input param -->
            <xsl:variable name="firstParam" select="input/param[1]"/>
            <xsl:if test="$firstParam/@type!=''">
               <xsl:choose>
                   <xsl:when test="$firstParam/@ours">
                       payload = adb_<xsl:value-of select="$firstParam/@type"/>_serialize(_<xsl:value-of select="$firstParam/@name"/>, env, NULL, NULL, AXIS2_TRUE, NULL, NULL);
                   </xsl:when>
                   <xsl:otherwise>
                       payload = _<xsl:value-of select="$firstParam/@name"/>;
                   </xsl:otherwise>
               </xsl:choose>
            </xsl:if>

            options = axis2_stub_get_options( stub, env);
            if ( NULL == options )
            { 
              AXIS2_ERROR_SET(env->error, AXIS2_ERROR_INVALID_NULL_PARAM, AXIS2_FAILURE);
              AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "options is null in stub");
              return AXIS2_FAILURE;
            }
            svc_client = axis2_stub_get_svc_client (stub, env );
            soap_action = axis2_options_get_action ( options, env );
            if ( NULL == soap_action )
            {
              soap_action = "<xsl:value-of select="$soapAction"/>";
              axis2_options_set_action( options, env, soap_action );
            }
            <xsl:choose>
             <xsl:when test="$soapVersion='1.2'">
            axis2_options_set_soap_version(options, env, AXIOM_SOAP12 );
             </xsl:when>
             <xsl:otherwise>
            axis2_options_set_soap_version(options, env, AXIOM_SOAP11 );
             </xsl:otherwise>
            </xsl:choose>
            op_qname = axutil_qname_create(env,
                                        "<xsl:value-of select="@localpart"/>" ,
                                        "<xsl:value-of select="@namespace"/>",
                                        NULL);
            status =  axis2_svc_client_send_robust_with_op_qname( svc_client, env, op_qname, payload);
            return status;

        }
       </xsl:if> <!-- close for  test="$mep='http://www.w3.org/2004/08/wsdl/in-only' -->
     </xsl:for-each>   <!-- close of for-each select = "method" -->
   </xsl:template>
</xsl:stylesheet>
