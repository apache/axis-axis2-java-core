import org.apache.axis.om.SOAPEnvelope;

public class Echo2 {
    public SOAPEnvelope echo(SOAPEnvelope in){
        return in;
    }    
}
