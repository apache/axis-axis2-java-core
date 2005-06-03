package userguide.example1;

import org.apache.axis.om.OMElement;

/**
 * Created by IntelliJ IDEA.
 * User: Jaliya
 * Date: Jun 2, 2005
 * Time: 2:17:58 PM
 */
public class MyService {
     public OMElement echo(OMElement element){
        element.getNextSibling();
        element.detach();
        return element;
    }

    public void ping(OMElement element){
       //Do the ping
    }
}
