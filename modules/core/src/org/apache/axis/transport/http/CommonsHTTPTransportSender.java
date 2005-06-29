package org.apache.axis.transport.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axis.Constants;
import org.apache.axis.addressing.AddressingConstants;
import org.apache.axis.addressing.EndpointReference;
import org.apache.axis.context.ConfigurationContext;
import org.apache.axis.context.MessageContext;
import org.apache.axis.description.Parameter;
import org.apache.axis.description.TransportOutDescription;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.handlers.AbstractHandler;
import org.apache.axis.om.OMElement;
import org.apache.axis.om.OMOutput;
import org.apache.axis.transport.TransportSender;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;

public class CommonsHTTPTransportSender extends AbstractHandler implements TransportSender {
    private boolean chuncked = false;
    private boolean doMTOM = false;
    private String httpVersion = HTTPConstants.HEADER_PROTOCOL_10;
    public static final String TRANSPORT_SENDER_INFO = "TRANSPORT_SENDER_INFO";

    protected PostMethod postMethod;
    protected HttpClient httpClient;
    protected OMElement outputMessage;
    protected boolean doREST;

    public CommonsHTTPTransportSender() {
    } //default

    public void invoke(MessageContext msgContext) throws AxisFault {
        try {
            //Check for the REST behaviour, if you desire rest beahaviour
            //put a <parameter name="doREST" value="true"/> at the server.xml/client.xml file
            Object doREST = msgContext.getProperty(Constants.Configuration.DO_REST);
            if (doREST != null && Constants.VALUE_TRUE.equals(doREST)) {
                this.doREST = true;
            }

            EndpointReference epr = null;
            if (msgContext.getTo() != null
                && !AddressingConstants.Submission.WSA_ANONYMOUS_URL.equals(
                    msgContext.getTo().getAddress())
                && !AddressingConstants.Final.WSA_ANONYMOUS_URL.equals(
                    msgContext.getTo().getAddress())) {
                epr = msgContext.getTo();
            }

            OMElement dataOut = null;
            if (this.doREST) {
                dataOut = msgContext.getEnvelope().getFirstElement();
            } else {
                dataOut = msgContext.getEnvelope();
            }

            //TODO timeout, configuration
            if (epr != null) {
                writeMessageWithCommons(msgContext, epr, dataOut);
            } else {
                OutputStream out =
                    (OutputStream) msgContext.getProperty(MessageContext.TRANSPORT_OUT);
                OMOutput output = new OMOutput(out, false);
                dataOut.serialize(output);
            }
            msgContext.getOperationContext().setProperty(
                Constants.RESPONSE_WRITTEN,
                Constants.VALUE_TRUE);
        } catch (XMLStreamException e) {
            throw new AxisFault(e);
        } catch (FactoryConfigurationError e) {
            throw new AxisFault(e);
        }
    }

    public void writeMessageWithToOutPutStream(MessageContext msgContext, OutputStream out) {

    }

    public void writeMessageWithCommons(
        MessageContext msgContext,
        EndpointReference toURL,
        OMElement dataout)
        throws AxisFault {
        try {
            URL url = new URL(toURL.getAddress());
            //Configure the transport
            String soapAction = msgContext.getWSAAction();
            //settign soapAction
            String soapActionString = soapAction == null ? "" : soapAction.toString();

            PostMethod postMethod = new PostMethod();
            postMethod.setRequestEntity(new AxisRequestEntity(dataout, chuncked));
            if (!httpVersion.equals(HTTPConstants.HEADER_PROTOCOL_10) && chuncked) {
                ((PostMethod) postMethod).setContentChunked(true);
            }

            postMethod.setRequestHeader(
                HTTPConstants.HEADER_CONTENT_TYPE,
                "text/xml; charset=utf-8");
            postMethod.setRequestHeader(
                HTTPConstants.HEADER_ACCEPT,
                HTTPConstants.HEADER_ACCEPT_APPL_SOAP
                    + HTTPConstants.HEADER_ACCEPT_APPLICATION_DIME
                    + HTTPConstants.HEADER_ACCEPT_MULTIPART_RELATED
                    + HTTPConstants.HEADER_ACCEPT_TEXT_ALL);
            postMethod.setRequestHeader(HTTPConstants.HEADER_HOST, url.getHost());
            postMethod.setRequestHeader(HTTPConstants.HEADER_CACHE_CONTROL, "no-cache");
            postMethod.setRequestHeader(HTTPConstants.HEADER_PRAGMA, "no-cache");
            //content length is not set yet
            //setting HTTP vesion

            if (httpVersion != null) {
                if (httpVersion.equals(HTTPConstants.HEADER_PROTOCOL_10)) {
                    //postMethod.setHttp11(false); todo method to findout the transport version...
                    //allowing keep-alive for 1.0
                    postMethod.setRequestHeader(
                        HTTPConstants.HEADER_CONNECTION,
                        HTTPConstants.HEADER_CONNECTION_KEEPALIVE);
                } else {
                    // allowing keep-alive for 1.1
                    postMethod.setRequestHeader(
                        HTTPConstants.HEADER_CONNECTION,
                        HTTPConstants.HEADER_CONNECTION_KEEPALIVE);
                }
            }
            // othervise assumes HTTP 1.1 and keep-alive is default.
            if (!this.doREST) {
                postMethod.setRequestHeader(HTTPConstants.HEADER_SOAP_ACTION, soapActionString);
            }

            //execuite the HtttpMethodBase - a connection manager can be given for handle multiple
            httpClient = new HttpClient();
            //hostConfig handles the socket functions..
            HostConfiguration hostConfig = getHostConfiguration(msgContext, url);

            //code that wirte the stream to the wire

            this.httpClient.executeMethod(hostConfig, this.postMethod);
            if(postMethod.getStatusCode() == HttpStatus.SC_OK){
                InputStream in = postMethod.getResponseBodyAsStream();
                if(in == null){
                    throw new AxisFault("Input Stream can not be Null");
                }
                msgContext.getOperationContext().setProperty(MessageContext.TRANSPORT_IN,in);
            }else if(postMethod.getStatusCode() == HttpStatus.SC_ACCEPTED){
                return;                              
            }else{
                throw new AxisFault("Error "+ postMethod.getStatusCode() +  "  Error Message is "+postMethod.getResponseBodyAsString());
            }
        } catch (MalformedURLException e) {
            throw new AxisFault(e);
        } catch (HttpException e) {
            throw new AxisFault(e);
        } catch (IOException e) {
            throw new AxisFault(e);
        }

    }

    protected HostConfiguration getHostConfiguration(MessageContext context, URL targetURL) {
        //TODO cheaking wheather the host is a proxy
        HostConfiguration config = new HostConfiguration();
        config.setHost(targetURL.getHost(), targetURL.getPort() == -1 ? 80 : targetURL.getPort());
        return config;
    }
    //get the contentLength...
    public class AxisRequestEntity implements RequestEntity {
        private OMElement element;
        private boolean chuncked;
        private byte[] bytes;

        public AxisRequestEntity(OMElement element, boolean chuncked) {
            this.element = element;
            this.chuncked = chuncked;
        }
        public boolean isRepeatable() {
            return false;
        }

        public byte[] writeBytes() throws AxisFault {
            try {
                ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
                XMLStreamWriter outputWriter =
                    XMLOutputFactory.newInstance().createXMLStreamWriter(bytesOut);
                OMOutput output = new OMOutput(outputWriter);
                element.serialize(output);
                outputWriter.flush();
                return bytesOut.toByteArray();
            } catch (XMLStreamException e) {
                throw new AxisFault(e);
            } catch (FactoryConfigurationError e) {
                throw new AxisFault(e);
            }
        }

        public void writeRequest(OutputStream out) throws IOException {
            try {
                if (chuncked) {
                    XMLStreamWriter outputWriter = null;
                    outputWriter = XMLOutputFactory.newInstance().createXMLStreamWriter(out);
                    OMOutput output = new OMOutput(outputWriter);
                    element.serialize(output);
                    outputWriter.flush();
                    out.flush();
                } else {
                    if (bytes == null) {
                        bytes = writeBytes();
                    }
                    out.write(bytes);
                }
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
                if (chuncked) {
                    return -1;
                } else {
                    if (bytes == null) {
                        bytes = writeBytes();
                    }
                    return bytes.length;
                }
            } catch (AxisFault e) {
                return -1;
            }
        }

        public String getContentType() {
            return "text/xml; charset=utf-8";
        }
    }

    /* (non-Javadoc)
     * @see org.apache.axis.transport.TransportSender#cleanUp(org.apache.axis.context.MessageContext)
     */
    public void cleanUp(MessageContext msgContext) throws AxisFault {
        ((PostMethod) this.postMethod).releaseConnection();

    }

    public void init(ConfigurationContext confContext, TransportOutDescription transportOut)
        throws AxisFault {
        //<parameter name="PROTOCOL" locked="xsd:false">HTTP/1.0</parameter> or 
        //<parameter name="PROTOCOL" locked="xsd:false">HTTP/1.1</parameter> is checked
        Parameter version = transportOut.getParameter(HTTPConstants.PROTOCOL_VERSION);
        if (version != null) {
            if (HTTPConstants.HEADER_PROTOCOL_11.equals(version.getValue())) {
                this.httpVersion = HTTPConstants.HEADER_PROTOCOL_11;
                Parameter transferEncoding =
                    transportOut.getParameter(HTTPConstants.HEADER_TRANSFER_ENCODING);
                if (transferEncoding != null
                    && HTTPConstants.HEADER_TRANSFER_ENCODING_CHUNKED.equals(
                        transferEncoding.getValue())) {
                    this.chuncked = true;
                }
            } else if (HTTPConstants.HEADER_PROTOCOL_10.equals(version.getValue())) {
                //TODO HTTP1.0 specific parameters
            } else {
                throw new AxisFault(
                    "Parameter "
                        + HTTPConstants.PROTOCOL_VERSION
                        + " Can have values only HTTP/1.0 or HTTP/1.1");
            }
        }

    }

}
