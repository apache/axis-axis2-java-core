package org.apache.module;

import org.apache.axis.modules.Module;
import org.apache.axis.context.EngineContext;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.engine.ExecutionChain;

/**
 * Created by IntelliJ IDEA.
 * Author : Deepal Jayasinghe
 * Date: Apr 19, 2005
 * Time: 3:26:07 PM
 */
public class Module2Impl implements Module{
    public void init(EngineContext moduleContext) throws AxisFault {
        //do nothing
    }

    public void engage(ExecutionChain exeChain) throws AxisFault {
        //do nothing
    }

    public void shutDown() throws AxisFault {
        //do nothing
    }
}
