package sample.jms.client;

import javax.xml.stream.XMLStreamException;

import org.apache.axiom.om.util.StAXUtils;
import org.apache.axis2.client.async.AsyncResult;
import org.apache.axis2.client.async.Callback;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

class JMSClientCallback extends Callback {
        private static final Log log = LogFactory.getLog(JMSClientCallback.class);
        private boolean finish = false;
        
        public boolean isFinish() {
            return finish;
        }
        
        public void onComplete(AsyncResult result) {
            try {
                result.getResponseEnvelope().serialize(StAXUtils
                        .createXMLStreamWriter(System.out));
            } catch (XMLStreamException e) {
                onError(e);
            } finally {
                finish = true;
            }
        }
        
        public void onError(Exception e) {
            log.info(e.getMessage());
            finish = true;
        }
        
    }