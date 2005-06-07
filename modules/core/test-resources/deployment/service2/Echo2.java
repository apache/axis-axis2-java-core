import org.apache.axis.soap.SOAPEnvelope;

public class Echo2 {
    public SOAPEnvelope echo(SOAPEnvelope in){
        return in;
    }    
}
