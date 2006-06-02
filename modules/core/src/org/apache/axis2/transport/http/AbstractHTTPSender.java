package org.apache.axis2.transport.http;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.util.Utils;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.i18n.Messages;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HeaderElement;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.NTCredentials;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.zip.GZIPInputStream;

public abstract class AbstractHTTPSender {
    protected static final String ANONYMOUS = "anonymous";
    protected static final String PROXY_HOST_NAME = "proxy_host";
    protected static final String PROXY_PORT = "proxy_port";
    protected boolean chunked = false;
    protected String httpVersion = HTTPConstants.HEADER_PROTOCOL_11;
	private static final Log log = LogFactory.getLog(AbstractHTTPSender.class);
    int soTimeout = HTTPConstants.DEFAULT_SO_TIMEOUT;

    /**
     * proxydiscription
     */
    protected TransportOutDescription proxyOutSetting = null;
    protected OMOutputFormat format = new OMOutputFormat();
    int connectionTimeout = HTTPConstants.DEFAULT_CONNECTION_TIMEOUT;

    public void setChunked(boolean chunked) {
        this.chunked = chunked;
    }

    public void setHttpVersion(String version) throws AxisFault {
        if (version != null) {
            if (HTTPConstants.HEADER_PROTOCOL_11.equals(version)) {
                this.httpVersion = HTTPConstants.HEADER_PROTOCOL_11;
            } else
            if (HTTPConstants.HEADER_PROTOCOL_10.equals(version)) {
                this.httpVersion = HTTPConstants.HEADER_PROTOCOL_10;
                // chunked is not possible with HTTP/1.0
                this.chunked = false;
            } else {
                throw new AxisFault(
                        "Parameter " + HTTPConstants.PROTOCOL_VERSION
                                + " Can have values only HTTP/1.0 or HTTP/1.1");
            }
        }
    }

    /**
     * Helper method to Proxy and NTLM authentication
     *
     * @param client
     * @param proxySetting
     * @param config
     */
    protected void configProxyAuthentication(HttpClient client,
                                             TransportOutDescription proxySetting,
                                             HostConfiguration config,
                                             MessageContext msgCtx)
            throws AxisFault {
        Parameter proxyParam = proxySetting.getParameter(HTTPConstants.PROXY);
        String usrName;
        String domain;
        String passwd;
        Credentials proxyCred = null;
        String proxyHostName = null;
        int proxyPort = -1;

        if (proxyParam != null) {
            String value = (String) proxyParam.getValue();
            String split[] = value.split(":");

            // values being hard coded due best practise
            usrName = split[0];
            domain = split[1];
            passwd = split[2];

            OMElement proxyParamElement = proxyParam.getParameterElement();
            Iterator ite = proxyParamElement.getAllAttributes();

            while (ite.hasNext()) {
                OMAttribute att = (OMAttribute) ite.next();

                if (att.getLocalName().equalsIgnoreCase(PROXY_HOST_NAME)) {
                    proxyHostName = att.getAttributeValue();
                }

                if (att.getLocalName().equalsIgnoreCase(PROXY_PORT)) {
                    proxyPort = Integer.parseInt(att.getAttributeValue());
                }
            }

            if (domain.equals("") || domain.equals(ANONYMOUS)) {
                if (usrName.equals(ANONYMOUS) && passwd.equals(ANONYMOUS)) {
                    proxyCred = new UsernamePasswordCredentials("", "");
                } else {
                    proxyCred = new UsernamePasswordCredentials(usrName,
                            passwd);    // proxy
                }
            } else {
                proxyCred = new NTCredentials(usrName, passwd, proxyHostName,
                        domain);    // NTLM authentication with additionals prams
            }
        }

        HttpTransportProperties.ProxyProperties proxyProperties =
                (HttpTransportProperties.ProxyProperties) msgCtx
                        .getProperty(HTTPConstants.PROXY);

        if (proxyProperties != null) {
            if (proxyProperties.getProxyPort() != -1) {
                proxyPort = proxyProperties.getProxyPort();
            }

            proxyHostName = proxyProperties.getProxyHostName();
            if (proxyHostName == null
                    || proxyHostName.length() == 0) {
                throw new AxisFault("Proxy Name is not valid");
            }

            if (proxyProperties.getUserName().equals(ANONYMOUS)
                    || proxyProperties.getPassWord().equals(ANONYMOUS)) {
                proxyCred = new UsernamePasswordCredentials("", "");
            }
            if (!proxyProperties.getUserName().equals(ANONYMOUS) &&
                    !proxyProperties.getPassWord().equals(ANONYMOUS)) {
                proxyCred = new UsernamePasswordCredentials(
                        proxyProperties.getUserName().trim(),
                        proxyProperties
                                .getPassWord().trim()); // Basic Authentication
            }
            if (!proxyProperties.getDomain().equals(ANONYMOUS)) {
                if (!proxyProperties.getUserName().equals(ANONYMOUS) &&
                        !proxyProperties.getPassWord().equals(ANONYMOUS) &&
                        !proxyProperties.getDomain().equals(ANONYMOUS) &&
                        proxyHostName != null) {
                    proxyCred = new NTCredentials(
                            proxyProperties.getUserName().trim(),
                            proxyProperties.getPassWord().trim(), proxyHostName,
                            proxyProperties
                                    .getDomain().trim()); // NTLM Authentication
                }
            }
        }

        client.getState().setProxyCredentials(AuthScope.ANY, proxyCred);
        config.setProxy(proxyHostName, proxyPort);
    }

    /**
     * Collect the HTTP header information and set them in the message context
     *
     * @param method
     * @param msgContext
     */
    protected void obtainHTTPHeaderInformation(HttpMethodBase method,
                                               MessageContext msgContext) {
        Header header =
                method.getResponseHeader(HTTPConstants.HEADER_CONTENT_TYPE);

        if (header != null) {
            HeaderElement[] headers = header.getElements();

            for (int i = 0; i < headers.length; i++) {
                NameValuePair charsetEnc =
                        headers[i].getParameterByName(
                                HTTPConstants.CHAR_SET_ENCODING);
                OperationContext opContext = msgContext.getOperationContext();
                String name = headers[i].getName();
                if (name.equalsIgnoreCase(
                        HTTPConstants.HEADER_ACCEPT_MULTIPART_RELATED)) {
                    if (opContext != null) {
                        opContext.setProperty(
                                HTTPConstants.MTOM_RECEIVED_CONTENT_TYPE,
                                header.getValue());
                    }
                } else if (charsetEnc != null) {
                    if (opContext != null) {
                        opContext.setProperty(MessageContext.CHARACTER_SET_ENCODING,
                                charsetEnc.getValue());    // change to the value, which is text/xml or application/xml+soap
                    }
                }
            }
        }
        Header cookieHeader =
                method.getResponseHeader(HTTPConstants.HEADER_SET_COOKIE);
        if (cookieHeader == null) {
            cookieHeader =
                    method.getResponseHeader(HTTPConstants.HEADER_SET_COOKIE2);
        }
        if (cookieHeader != null) {
            msgContext.getServiceContext().setProperty(Constants.COOKIE_STRING,
                    cookieHeader.getValue());
        }
    }

    protected void processResponse(HttpMethodBase httpMethod,
                                   MessageContext msgContext)
            throws IOException {
        obtainHTTPHeaderInformation(httpMethod, msgContext);

        InputStream in = httpMethod.getResponseBodyAsStream();

        Header contentEncoding =
            httpMethod.getResponseHeader(HTTPConstants.HEADER_CONTENT_ENCODING);
        if (contentEncoding != null) {
            if (contentEncoding.getValue().
                    equalsIgnoreCase(HTTPConstants.COMPRESSION_GZIP)) {
                in =
                    new GZIPInputStream(in);
            } else {
                throw new AxisFault("HTTP :"
                        + "unsupported content-encoding of '"
                        + contentEncoding.getValue()
                        + "' found");
            }
        }

        if (in == null) {
            throw new AxisFault(
                    Messages.getMessage("canNotBeNull", "InputStream"));
        }

        if(msgContext.getOperationContext() != null) {
            msgContext.getOperationContext()
                    .setProperty(MessageContext.TRANSPORT_IN, in);
        }
    }

    public abstract void send(MessageContext msgContext, OMElement dataout,
                              URL url,
                              String soapActionString)
            throws MalformedURLException, AxisFault, IOException;

    /**
     * getting host configuration to support standard http/s, proxy and NTLM support
     */
    protected HostConfiguration getHostConfiguration(HttpClient client,
                                                     MessageContext msgCtx,
                                                     URL targetURL)
            throws AxisFault {
        boolean isHostProxy = isProxyListed(msgCtx);    // list the proxy

        boolean basicAuthenticationEnabled = serverBasicPreemtiveAuthentication(msgCtx); // server authentication
        int port = targetURL.getPort();

        if (port == -1) {
            port = 80;
        }

        // to see the host is a proxy and in the proxy list - available in axis2.xml
        HostConfiguration config = new HostConfiguration();

        if (!isHostProxy && !basicAuthenticationEnabled) {
            config.setHost(targetURL.getHost(), port, targetURL.getProtocol());
        }else if(basicAuthenticationEnabled){
             // premtive authentication
            this.configServerPreemtiveAuthenticaiton(client,msgCtx,config,targetURL);
        } else {

            // proxy and NTLM configuration
            this.configProxyAuthentication(client, proxyOutSetting, config,
                    msgCtx);
        }

        return config;
    }

    private void configServerPreemtiveAuthenticaiton(HttpClient agent,
                                                     MessageContext msgCtx,
                                                     HostConfiguration config,
                                                     URL targetURL) {
        config.setHost(targetURL.getHost(), targetURL.getPort(),
                       targetURL.getProtocol());

        agent.getParams().setAuthenticationPreemptive(true);

        HttpTransportProperties.BasicAuthentication basicAuthentication =
                (HttpTransportProperties.BasicAuthentication) msgCtx
                        .getProperty(HTTPConstants.BASIC_AUTHENTICATION);
        Credentials defaultCredentials = null;
        if (basicAuthentication.getRealm() == null) {
            defaultCredentials = new UsernamePasswordCredentials(
                    basicAuthentication.getUsername(),
                    basicAuthentication.getPassword());
        } else {
            defaultCredentials = new NTCredentials(
                    basicAuthentication.getUsername(),
                    basicAuthentication.getPassword(),
                    basicAuthentication.getHost(),
                    basicAuthentication.getRealm());
            agent.getState().setCredentials(new AuthScope(
                    basicAuthentication.getHost(),
                    basicAuthentication.getPort(),
                    null), defaultCredentials);

            return;
        }
        if (basicAuthentication.getPort() == -1 ||
            basicAuthentication.getHost() == null) {
            agent.getState().setCredentials(AuthScope.ANY, defaultCredentials);
        } else {
            if (basicAuthentication.getRealm() == null) {
                agent.getState().setCredentials(new AuthScope(
                        basicAuthentication.getHost(),
                        basicAuthentication.getPort(),
                        AuthScope.ANY_REALM), defaultCredentials);

            } else {
                agent.getState().setCredentials(new AuthScope(
                        basicAuthentication.getHost(),
                        basicAuthentication.getPort(),
                        basicAuthentication.getRealm()), defaultCredentials);
            }
        }


    }

    /**
     * This is used to get the dynamically set time out values from the
     * message context. If the values are not available or invalid then
     * teh default values or the values set by teh configuration will be used
     *
     * @param msgContext
     */
    protected void getTimeoutValues(MessageContext msgContext) {
        try {

            // If the SO_TIMEOUT of CONNECTION_TIMEOUT is set by dynamically the
            // override the static config
            Integer tempSoTimeoutProperty =
                    (Integer) msgContext.getProperty(HTTPConstants.SO_TIMEOUT);
            Integer tempConnTimeoutProperty =
                    (Integer) msgContext
                            .getProperty(HTTPConstants.CONNECTION_TIMEOUT);

            if (tempSoTimeoutProperty != null) {
                soTimeout = tempSoTimeoutProperty.intValue();
            }

            if (tempConnTimeoutProperty != null) {
                connectionTimeout = tempConnTimeoutProperty.intValue();
            }
        } catch (NumberFormatException nfe) {

            // If there's a problem log it and use the default values
            log.error("Invalid timeout value format: not a number", nfe);
        }
    }

    //Server Preemptive Authentication RUNTIME

    private boolean serverBasicPreemtiveAuthentication(MessageContext msgContext) {

        return msgContext.getProperty(HTTPConstants.BASIC_AUTHENTICATION) !=
               null;
    }

    private boolean isProxyListed(MessageContext msgCtx) throws AxisFault {
        boolean returnValue = false;
        Parameter par = null;

        proxyOutSetting = msgCtx.getConfigurationContext()
                .getAxisConfiguration().getTransportOut(
                new QName(Constants.TRANSPORT_HTTP));

        if (proxyOutSetting != null) {
            par = proxyOutSetting.getParameter(HTTPConstants.PROXY);
        }

        OMElement hostElement = null;

        if (par != null) {
            hostElement = par.getParameterElement();
        }

        if (hostElement != null) {
            Iterator ite = hostElement.getAllAttributes();

            while (ite.hasNext()) {
                OMAttribute attribute = (OMAttribute) ite.next();

                if (attribute.getLocalName().equalsIgnoreCase(PROXY_HOST_NAME))
                {
                    returnValue = true;
                }
            }
        }

        HttpTransportProperties.ProxyProperties proxyProperties;

        if ((proxyProperties =
                (HttpTransportProperties.ProxyProperties) msgCtx.getProperty(
                        HTTPConstants.PROXY)) != null) {
            if (proxyProperties.getProxyHostName() != null) {
                returnValue = true;
            }
        }

        return returnValue;
    }

    public void setFormat(OMOutputFormat format) {
        this.format = format;
    }

    public class AxisRequestEntity implements RequestEntity {
        private boolean doingMTOM = false;
        private byte[] bytes;
        private String charSetEnc;
        private boolean chunked;
        private OMElement element;
        private MessageContext msgCtxt;
        private String soapActionString;

        public AxisRequestEntity(OMElement element, boolean chunked,
                                 MessageContext msgCtxt,
                                 String charSetEncoding,
                                 String soapActionString) {
            this.element = element;
            this.chunked = chunked;
            this.msgCtxt = msgCtxt;
            this.doingMTOM = msgCtxt.isDoingMTOM();
            this.charSetEnc = charSetEncoding;
            this.soapActionString = soapActionString;
        }

        private void handleOMOutput(OutputStream out, boolean doingMTOM)
                throws XMLStreamException {
            format.setDoOptimize(doingMTOM);
            element.serializeAndConsume(out, format);
        }

        public byte[] writeBytes() throws AxisFault {
            try {
                ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();

                if (!doingMTOM) {
                    OMOutputFormat format2 = new OMOutputFormat();

                    format2.setCharSetEncoding(charSetEnc);
                    element.serializeAndConsume(bytesOut, format2);

                    return bytesOut.toByteArray();
                } else {
                    format.setCharSetEncoding(charSetEnc);
                    format.setDoOptimize(true);
                    element.serializeAndConsume(bytesOut, format);

                    return bytesOut.toByteArray();
                }
            } catch (XMLStreamException e) {
                throw new AxisFault(e);
            } catch (FactoryConfigurationError e) {
                throw new AxisFault(e);
            }
        }

        public void writeRequest(OutputStream out) throws IOException {
            try {
                {
                    if (chunked) {
                        this.handleOMOutput(out, doingMTOM);
                    } else {
                        if (bytes == null) {
                            bytes = writeBytes();
                        }

                        out.write(bytes);
                    }
                }

                out.flush();
            } catch (XMLStreamException e) {
                throw new AxisFault(e);
            } catch (FactoryConfigurationError e) {
                throw new AxisFault(e);
            } catch (IOException e) {
                throw new AxisFault(e);
            }
        }

        public long getContentLength() {
            try {
                {
                    if (chunked) {
                        return -1;
                    } else {
                        if (bytes == null) {
                            bytes = writeBytes();
                        }

                        return bytes.length;
                    }
                }
            } catch (AxisFault e) {
                return -1;
            }
        }

        public String getContentType() {
            String encoding = format.getCharSetEncoding();
            String contentType = format.getContentType();

            if (encoding != null) {
                contentType += "; charset=" + encoding;
            }

            // action header is not mandated in SOAP 1.2. So putting it, if available
            if (!msgCtxt.isSOAP11() && (soapActionString != null)
                    && !"".equals(soapActionString.trim())) {
                contentType =
                        contentType + ";action=\"" + soapActionString + "\";";
            }

            return contentType;
        }

        public boolean isRepeatable() {
            return true;
        }
    }

    protected HttpClient getHttpClient(MessageContext msgContext) {
        HttpClient httpClient = null;
        Boolean reuse = (Boolean) msgContext.getOptions().getProperty(HTTPConstants.REUSE_HTTP_CLIENT);
        if(reuse != null && reuse.booleanValue()) {
            httpClient = (HttpClient) msgContext.getConfigurationContext().getProperty(HTTPConstants.CACHED_HTTP_CLIENT);
            if(httpClient == null){
                MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
                httpClient = new HttpClient(connectionManager);
                msgContext.getConfigurationContext().setProperty(HTTPConstants.CACHED_HTTP_CLIENT, httpClient);
            }
        } else {
            httpClient = new HttpClient();
        }

        // Get the timeout values set in the runtime
        getTimeoutValues(msgContext);

        // SO_TIMEOUT -- timeout for blocking reads
        httpClient.getHttpConnectionManager().getParams().setSoTimeout(soTimeout);

        // timeout for initial connection
        httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(connectionTimeout);
        return httpClient;
    }

    protected void executeMethod(HttpClient httpClient, MessageContext msgContext, URL url, HttpMethod method) throws IOException {
        HostConfiguration config = this.getHostConfiguration(httpClient, msgContext, url);
        msgContext.setProperty(HTTPConstants.HTTP_METHOD, method);

        // add compression headers if needed
        if (Utils.isExplicitlyTrue(msgContext, HTTPConstants.MC_ACCEPT_GZIP)) {
            method.addRequestHeader(HTTPConstants.HEADER_ACCEPT_ENCODING,
                    HTTPConstants.COMPRESSION_GZIP);
        }
        if (Utils.isExplicitlyTrue(msgContext, HTTPConstants.MC_GZIP_REQUEST)) {
            method.addRequestHeader(HTTPConstants.HEADER_CONTENT_ENCODING,
                    HTTPConstants.COMPRESSION_GZIP);
        }

        httpClient.executeMethod(config, method);
    }
}
