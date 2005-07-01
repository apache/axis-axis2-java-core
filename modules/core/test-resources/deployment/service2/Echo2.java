import org.apache.axis2.soap.SOAPEnvelope;

public class Echo2 {
    public SOAPEnvelope echo(SOAPEnvelope in){
        return in;
    }    
}
