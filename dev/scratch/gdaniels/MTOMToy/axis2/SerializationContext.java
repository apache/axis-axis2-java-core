package axis2;

import axis2.om.OMWriter;

/**
 * Created by IntelliJ IDEA.
 * User: glen
 * Date: Nov 19, 2004
 * Time: 12:41:29 AM
 * To change this template use File | Settings | File Templates.
 */
public interface SerializationContext {
    void serialize(Object obj, OMWriter writer) throws Exception;
}
