
    package org.apache.axis2.oasis.ping;


    import org.apache.axis2.oasis.ping.databinding.org.xmlsoap.PingDocument;
    import org.apache.axis2.oasis.ping.databinding.org.xmlsoap.PingResponse;
    import org.apache.axis2.oasis.ping.databinding.org.xmlsoap.PingResponseDocument;

    /**
     *  Auto generated java skeleton for the service by the Axis code generator
     */
    public class PingPortSkeleton {



        /**
         * Auto generated method signature

          * @param param0

         */
        public  PingResponseDocument Ping
                  (PingDocument param0 ){
                PingResponseDocument response = PingResponseDocument.Factory.newInstance();
            PingResponse pingRes = response.addNewPingResponse();
            pingRes.setText("Response: " + param0.getPing().getText());
            return response;
        }

    }
    