package org.apache.module;

import org.apache.axis.modules.Module;
import org.apache.axis.context.SystemContext;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.engine.ExecutionChain;
import org.apache.axis.engine.AxisSystem;

/**
 * Created by IntelliJ IDEA.
 * Author : Deepal Jayasinghe
 * Date: Apr 19, 2005
 * Time: 3:16:14 PM
 */
public class Module1Impl implements Module{

    // initialize the module
    public void init(AxisSystem axisSystem) throws AxisFault {
        /**
         * in this case in the init the module does not need to do any thing
         */
    }

    // shutdown the module
    public void shutdown(AxisSystem axisSystem) throws AxisFault {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
