package axis2.om;

/**
 * OMWriter interface
 */
public interface OMWriter {
    void startElement(String name) throws Exception;

    void endElement() throws Exception;

    void writeText(String text) throws Exception;

    void serialize(Object obj) throws Exception;
}
