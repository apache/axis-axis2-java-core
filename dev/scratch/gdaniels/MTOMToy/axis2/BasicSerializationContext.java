package axis2;

import axis2.om.OMWriter;

/**
 * SerializationContext placeholder.
 */
public class BasicSerializationContext implements SerializationContext {
    public void serialize(Object obj, OMWriter writer) throws Exception {
        writer.writeText(obj.toString());
    }
}
