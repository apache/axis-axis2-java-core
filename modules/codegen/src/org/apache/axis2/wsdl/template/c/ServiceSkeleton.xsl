<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="text"/>

      <!--Template for in out message receiver -->
      <xsl:template match="/interface">
        <xsl:variable name="skeletonname"><xsl:value-of select="@name"/></xsl:variable>
        <xsl:variable name="svcname"><xsl:value-of select="@svcname"/></xsl:variable>
        <xsl:variable name="soapVersion"><xsl:value-of select="@soap-version"/></xsl:variable>

        /**
         * svc_skel_<xsl:value-of select="$skeletonname"/>.c
         *
         * This file was auto-generated from WSDL
         * by the Apache Axis2 version: #axisVersion# #today#
         *  <xsl:value-of select="$skeletonname"/>
         */

        #include "<xsl:value-of select="@svcname"/>.h"
        #include &lt;axis2_svc_skeleton.h&gt;
        #include &lt;axis2_array_list.h&gt;
        #include &lt;stdio.h&gt;

        /**
         * functions prototypes
         */

        /* On fault, handle the fault */
        axiom_node_t* AXIS2_CALL
        <xsl:value-of select="$skeletonname"/>_on_fault(axis2_svc_skeleton_t *svc_skeleton,
                  const axis2_env_t *env, axiom_node_t *node);

        /* Free the service */
        int AXIS2_CALL
        <xsl:value-of select="$skeletonname"/>_free(axis2_svc_skeleton_t *svc_skeleton,
                  const axis2_env_t *env);

        /* This method invokes the right service method */
        axiom_node_t* AXIS2_CALL
        <xsl:value-of select="$skeletonname"/>_invoke(axis2_svc_skeleton_t *svc_skeleton,
                    const axis2_env_t *env,
                    axiom_node_t *node,
                    axis2_msg_ctx_t *msg_ctx);

        /* Initializing the environment  */
        int AXIS2_CALL
        <xsl:value-of select="$skeletonname"/>_init(axis2_svc_skeleton_t *svc_skeleton,
                        const axis2_env_t *env);

        /* Create the service  */
        axis2_svc_skeleton_t* AXIS2_CALL
        <xsl:value-of select="$skeletonname"/>_create(const axis2_env_t *env);

        /**
         * Implementations for the functions
         */

	axis2_svc_skeleton_t* AXIS2_CALL
	<xsl:value-of select="$skeletonname"/>_create(const axis2_env_t *env)
	{
	    axis2_svc_skeleton_t *svc_skeleton = NULL;
	    svc_skeleton = AXIS2_MALLOC(env->allocator,
	        sizeof(axis2_svc_skeleton_t));


	    svc_skeleton->ops = AXIS2_MALLOC(
	        env->allocator, sizeof(axis2_svc_skeleton_ops_t));

	    svc_skeleton->func_array = NULL;

	    svc_skeleton->ops->free = <xsl:value-of select="$skeletonname"/>_free;
	    svc_skeleton->ops->init = <xsl:value-of select="$skeletonname"/>_init;
	    svc_skeleton->ops->invoke = <xsl:value-of select="$skeletonname"/>_invoke;
	    svc_skeleton->ops->on_fault = <xsl:value-of select="$skeletonname"/>_on_fault;

	    return svc_skeleton;
	}


	int AXIS2_CALL
	<xsl:value-of select="$skeletonname"/>_init(axis2_svc_skeleton_t *svc_skeleton,
	                        const axis2_env_t *env)
	{
	    svc_skeleton->func_array = axis2_array_list_create(env, 0);
            <xsl:for-each select="method">
	      AXIS2_ARRAY_LIST_ADD(svc_skeleton->func_array, env, "<xsl:value-of select="@name"/>");
            </xsl:for-each>

	    /* Any initialization stuff of math goes here */
	    return AXIS2_SUCCESS;
	}

	int AXIS2_CALL
	<xsl:value-of select="$skeletonname"/>_free(axis2_svc_skeleton_t *svc_skeleton,
				 const axis2_env_t *env)
	{
          if(svc_skeleton->func_array)
          {
            AXIS2_ARRAY_LIST_FREE(svc_skeleton->func_array, env);
            svc_skeleton->func_array = NULL;
          }

          if(svc_skeleton->ops)
          {
            AXIS2_FREE(env->allocator, svc_skeleton->ops);
            svc_skeleton->ops = NULL;
          }

          if(svc_skeleton)
          {
            AXIS2_FREE(env->allocator, svc_skeleton);
            svc_skeleton = NULL;
          }
          return AXIS2_SUCCESS;
	}


	/*
	 * This method invokes the right service method
	 */
	axiom_node_t* AXIS2_CALL
	<xsl:value-of select="$skeletonname"/>_invoke(axis2_svc_skeleton_t *svc_skeleton,
				const axis2_env_t *env,
				axiom_node_t *node,
				axis2_msg_ctx_t *msg_ctx)
	{
         /* Depending on the function name invoke the
          *  corresponding  method
          */
          axiom_node_t *content_node = NULL;

          <xsl:for-each select="method">
            <xsl:for-each select="input/param[@location='body']">
              <xsl:variable name="inputours"><xsl:if test="@type!='org.apache.axiom.om.OMElement'"><xsl:value-of select="@ours"></xsl:value-of></xsl:if></xsl:variable>
              <xsl:if test="$inputours and $inputours!=''">
               <xsl:variable name="paramname"><xsl:value-of select="@name"></xsl:value-of></xsl:variable>
               <xsl:variable name="paramtype"><xsl:if test="$inputours">axis2_</xsl:if><xsl:choose><xsl:when test="@type='org.apache.axiom.om.OMElement'">om_node</xsl:when><xsl:otherwise><xsl:value-of select="@type"></xsl:value-of></xsl:otherwise></xsl:choose><xsl:if test="$inputours">_t*</xsl:if></xsl:variable>
               <xsl:value-of select="$paramtype"/><xsl:text> </xsl:text><xsl:value-of select="$paramname"/>;
              </xsl:if>
            </xsl:for-each>
            <xsl:variable name="outputours"><xsl:value-of select="output/param/@ours"></xsl:value-of></xsl:variable>
            <xsl:variable name="outputtype"><xsl:if test="$outputours">axis2_</xsl:if><xsl:choose><xsl:when test="output/param/@type='org.apache.axiom.om.OMElement'">om_node</xsl:when><xsl:otherwise><xsl:value-of select="output/param/@type"></xsl:value-of></xsl:otherwise></xsl:choose><xsl:if test="$outputours">_t*</xsl:if></xsl:variable>
            <xsl:value-of select="$outputtype"/> ret_value<xsl:value-of select="position()"></xsl:value-of> = NULL;
          </xsl:for-each>

          <xsl:for-each select="method">
           <xsl:if test="position()=1">
            <xsl:variable name="outputours"><xsl:value-of select="output/param/@ours"></xsl:value-of></xsl:variable>
            <xsl:for-each select="input/param[@location='body']">
              <xsl:variable name="inputours"><xsl:if test="@type!='org.apache.axiom.om.OMElement'"><xsl:value-of select="@ours"></xsl:value-of></xsl:if></xsl:variable>
              <xsl:if test="$inputours and $inputours!=''">
               <xsl:if test="position()=1 and @style='rpc'">
                axiom_namespace_t* ns1 = NULL;
                axiom_namespace_t* xsi = NULL;
                axiom_namespace_t* xsd = NULL;
                axiom_attribute_t* attri1 = NULL;
               </xsl:if>
              </xsl:if>
            </xsl:for-each>

            <xsl:if test="output/param/@type!='org.apache.axiom.om.OMElement'">
             axiom_namespace_t* payload_ns = NULL;
             axiom_node_t* payload = NULL;
             axiom_element_t* payload_ele = NULL;
            </xsl:if>
           </xsl:if>
          </xsl:for-each> 

          if (node)
	      {
            if (AXIOM_NODE_GET_NODE_TYPE(node, env) == AXIOM_ELEMENT)
            {
               axiom_element_t *element = NULL;
               element = (axiom_element_t *)AXIOM_NODE_GET_DATA_ELEMENT(node, env);
               if (element)
               {
                  axis2_char_t *op_name = AXIOM_ELEMENT_GET_LOCALNAME(element, env);
                  if (op_name)
                  {
                    <xsl:for-each select="method">
                    <xsl:variable name="outputours"><xsl:if test="output/param/@type!='org.apache.axiom.om.OMElement'">yes</xsl:if></xsl:variable>
                    <xsl:variable name="outputtype"><xsl:if test="$outputours">axis2_</xsl:if><xsl:choose><xsl:when test="output/param/@type='org.apache.axiom.om.OMElement'">om_node</xsl:when><xsl:otherwise><xsl:value-of select="output/param/@type"></xsl:value-of></xsl:otherwise></xsl:choose><xsl:if test="$outputours">_t*</xsl:if></xsl:variable>
                    <xsl:variable name="capsoutputtype"><xsl:if test="$outputours">AXIS2_</xsl:if><xsl:choose><xsl:when test="output/param/@type='org.apache.axiom.om.OMElement'">om_node</xsl:when><xsl:otherwise><xsl:value-of select="output/param/@caps-type"></xsl:value-of>RESPONSE</xsl:otherwise></xsl:choose></xsl:variable>

                    <xsl:variable name="returnvariable"><xsl:value-of select="output/param/@name"/></xsl:variable>
                    <xsl:variable name="namespace"><xsl:value-of select="@namespace"/></xsl:variable>

                    <xsl:variable name="soapAction"><xsl:value-of select="@soapaction"></xsl:value-of></xsl:variable>
                    <xsl:variable name="name"><xsl:value-of select="@name"/></xsl:variable>
                    <xsl:variable name="style"><xsl:value-of select="@style"/></xsl:variable>
                    <xsl:variable name="paramcount"><xsl:value-of select="count(input/param[@location='body'])"/></xsl:variable>
                    <xsl:variable name="method-name"><xsl:value-of select="@name"/></xsl:variable>
                    <xsl:variable name="method-ns"><xsl:value-of select="@namespace"/> </xsl:variable>

                    if ( AXIS2_STRCMP(op_name, "<xsl:value-of select="@name"/>") == 0 )
                    {
                    <xsl:choose>
                       <xsl:when test="$style='rpc'">

                         content_node = AXIOM_NODE_GET_FIRST_CHILD(node, env);
                       </xsl:when>
                       <xsl:when test="$style='document'">
                         <!--content_node = node; /* spcial for doc-lit */-->
                         content_node = AXIOM_NODE_GET_FIRST_CHILD(node, env);
                       </xsl:when>
                    </xsl:choose>
                    
                    <xsl:for-each select="input/param[@location='body']">
                      <xsl:variable name="inputours"><xsl:if test="@type!='org.apache.axiom.om.OMElement'">yes</xsl:if></xsl:variable>
                      <xsl:if test="$inputours and $inputours!=''">
                        <xsl:variable name="paramname"><xsl:value-of select="@name"></xsl:value-of></xsl:variable>
                        <xsl:variable name="paramtype">axis2_<xsl:value-of select="@type"></xsl:value-of></xsl:variable>
                        <xsl:variable name="capsparamtype">AXIS2_<xsl:value-of select="@caps-type"></xsl:value-of></xsl:variable>
                        <xsl:value-of select="$paramname"/> = <xsl:value-of select="$paramtype"/>_create ( env );
                        <xsl:value-of select="$capsparamtype"/>_PARSE_OM ( <xsl:value-of select="$paramname"/>, env, content_node );
                      </xsl:if>

                    </xsl:for-each>
                    <xsl:choose>
                      <xsl:when test="not($outputtype) or $outputtype=''"></xsl:when>
                      <xsl:otherwise>ret_value<xsl:value-of select="position()"></xsl:value-of> = </xsl:otherwise>
                    </xsl:choose>
                    <xsl:value-of select="$svcname"/>_<xsl:value-of select="$name"/>(env <xsl:for-each select="input/param[@location='body']"> ,<xsl:variable name="inputours"><xsl:if test="@type!='org.apache.axiom.om.OMElement'">yes</xsl:if></xsl:variable>
                                                                          <xsl:choose><xsl:when test="$inputours and $inputours!=''"><xsl:value-of select="@name"/></xsl:when><xsl:otherwise>content_node</xsl:otherwise></xsl:choose>
                                                                          </xsl:for-each>);
                    <xsl:choose>
                      <xsl:when test="not($outputtype) or $outputtype=''">return NULL;</xsl:when>
                      <xsl:when test="not($outputours) or $outputours=''">return ret_value<xsl:value-of select="position()"></xsl:value-of>;</xsl:when>
                        <xsl:otherwise>
                       <xsl:choose>
                         <xsl:when test="$style='rpc'">
                           payload_ns = axiom_namespace_create (env, "<xsl:value-of select="$soapAction"/>", "ns0");
                           payload_ele = axiom_element_create(env, NULL,"<xsl:value-of select="$method-name"/>" , payload_ns, &amp;payload);
                           content_node = <xsl:value-of select="$capsoutputtype"/>_BUILD_OM ( ret_value<xsl:value-of select="position()"></xsl:value-of>, env, payload, xsi, xsd  );
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
                         </xsl:when>
                         <xsl:when test="$style='document'">
                           payload = <xsl:value-of select="$capsoutputtype"/>_BUILD_OM ( ret_value<xsl:value-of select="position()"></xsl:value-of>, env,NULL, NULL, NULL  );
                         </xsl:when>
                       </xsl:choose>
                       return payload;
                      </xsl:otherwise>
                     </xsl:choose>
                    }
                    </xsl:for-each>
                  }
                }
             }
          }
          printf("<xsl:value-of select="$skeletonname"/> service ERROR: invalid OM parameters in request\n");
          return node;
        }

        axiom_node_t* AXIS2_CALL
        <xsl:value-of select="$skeletonname"/>_on_fault(axis2_svc_skeleton_t *svc_skeleton,
                  const axis2_env_t *env, axiom_node_t *node)
	{
    		axiom_node_t *error_node = NULL;
		    axiom_node_t* text_node = NULL;
    		axiom_element_t *error_ele = NULL;
    		error_ele = axiom_element_create(env, node, "<xsl:value-of select="$skeletonname"/>Error", NULL,
        					&amp;error_node);
    		AXIOM_ELEMENT_SET_TEXT(error_ele, env, "<xsl:value-of select="$skeletonname"/>failed",
        					text_node);
    		return error_node;
	}


	/**
	 * Following block distinguish the exposed part of the dll.
 	 */

    AXIS2_EXTERN int AXIS2_CALL
    axis2_get_instance(struct axis2_svc_skeleton **inst,
	                        const axis2_env_t *env)
	{
		*inst = <xsl:value-of select="$skeletonname"/>_create(env);
		/*if(NULL != *inst)
	    	{
			status = *inst->init();
    	    	}*/
    		if(!(*inst))
    		{
        		return AXIS2_FAILURE;
    		}

    		return AXIS2_SUCCESS;
	}

	AXIS2_EXTERN int AXIS2_CALL
    axis2_remove_instance(axis2_svc_skeleton_t *inst,
                            const axis2_env_t *env)
	{
    		axis2_status_t status = AXIS2_FAILURE;
        	if (inst)
        	{
        		status = AXIS2_SVC_SKELETON_FREE(inst, env);
    		}
    		return status;
	}


    </xsl:template>

</xsl:stylesheet>
