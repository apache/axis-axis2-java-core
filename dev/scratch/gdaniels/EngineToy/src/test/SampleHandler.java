package test;

import axis2.Handler;
import axis2.MessageContext;
import axis2.handlers.BasicHandler;

/**
 * SampleHandler
 */
public class SampleHandler extends BasicHandler {
    public static final String PAUSE = "SampleHandler.pause";

    /**
     * @param context
     * @return
     * @throws Exception
     */
    public boolean invoke(MessageContext context) throws Exception {
        System.out.println(name + ": invoke()");
        // If we're in the midst of running (i.e. this is the second
        // time we've been called), remove the marker from the MC
        // and continue.
        if (context.getProperty(PAUSE) != null) {
            context.setProperty(PAUSE, null);
            return true;
        }

        // Check our pause option.  If it's not there
        // or false, just return success.
        Boolean ret = (Boolean)getOption(PAUSE);
        if (ret == null || ret.booleanValue() == false)
            return true;

        context.setProperty(PAUSE, Boolean.TRUE);
        return false;
    }
}
