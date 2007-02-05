
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:template match="module-codegen-policy-extensions">
        
        <xsl:if test="//createSequence">
        /**
         * Starts a reliabel message sequence
         */
        public void startSequence() {
            _getServiceClient().getOptions().setProperty("START_RM_SEQUENCE", "true");            
        }
        </xsl:if>
        
        <xsl:if test="//setLastMessage">
        /**
         * Marks the last message for the sequence
         */
         public void setLastMessage() {
             _getServiceClient().getOptions().setProperty("Sandesha2ClientAPIPropertyWSRMLastMessage", "true");
         }
        </xsl:if>
        
        <xsl:if test="//endSequence">
        /**
         * Terminates the reliabel message sequence
         */
        public void endSequence() {
            _getServiceClient().getOptions().setProperty("END_RM_SEQUENCE", "true");
        }
        </xsl:if>
        
        <xsl:variable name="optimized">
            <xsl:value-of select="//optimizeContent"/>
        </xsl:variable>
        
        
        <xsl:if test="//security-codegen-policy-extensions/usernametoken-action">
    // auto genenrated stub methods 
    
    public void setUsername(java.lang.String username) {
        _serviceClient.getOptions().setProperty(org.apache.ws.security.handler.WSHandlerConstants.USER, username);
    }

    public void setPassword(java.lang.String password) {
        _UTCallback pwCallback = new _UTCallback();
        pwCallback.setPassword(password);
    _serviceClient
                .getOptions()
                .setProperty(
                        org.apache.ws.security.handler.WSHandlerConstants.PASSWORD_TYPE,
                        "PasswordText");
        _serviceClient
                .getOptions()
                .setProperty(
                        org.apache.ws.security.handler.WSHandlerConstants.PW_CALLBACK_REF,
                        pwCallback);    }

    private class _UTCallback implements
            javax.security.auth.callback.CallbackHandler {

        private java.lang.String password;

        public _UTCallback() {
        }

        public void setPassword(java.lang.String password) {
            this.password = password;
        }

        public void handle(javax.security.auth.callback.Callback[] callbacks)

        throws java.io.IOException,
                javax.security.auth.callback.UnsupportedCallbackException {

            for (int i = 0; i &lt; callbacks.length; i++) {

                if (callbacks[i] instanceof org.apache.ws.security.WSPasswordCallback) {

                    org.apache.ws.security.WSPasswordCallback pc = (org.apache.ws.security.WSPasswordCallback) callbacks[i];

                    if (pc.getIdentifer().equals(getUsername())) {
                        pc.setPassword(password);

                    } else {
                        pc.setPassword(password);
                    }

                } else {

                    throw new javax.security.auth.callback.UnsupportedCallbackException(
                            callbacks[i], "Unrecognized Callback");
                }
            }
        }

        private java.lang.String getUsername() {
            return (String) _serviceClient.getOptions().getProperty(
                    org.apache.ws.security.handler.WSHandlerConstants.USER);
        }
    }
        </xsl:if>
        

        <xsl:choose>
            <xsl:when test="$optimized">
            private void setOpNameArray(){
            opNameArray = new javax.xml.namespace.QName[] {
            <xsl:for-each select="optimizeContent/opName">
                <xsl:if test="position()>1">,
                </xsl:if>new javax.xml.namespace.QName("<xsl:value-of select="@ns-url"/>","<xsl:value-of select="@localName"/>")
            </xsl:for-each>
            };
           }
           </xsl:when>
            <xsl:otherwise>
            private void setOpNameArray(){
            opNameArray = null;
            }
           </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>