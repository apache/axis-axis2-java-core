import java.io.IOException;

/*
 * Created on Feb 14, 2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */

/**
 * @author hemapani
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class Waiter {

    private static int waitingCount = 0;
    
    public static synchronized void waitForMe() {
        waitingCount++;

    }
    public static synchronized void release(){
            waitingCount--;
    }
    
    public static synchronized int getValue() {
        return waitingCount;
    }
}
