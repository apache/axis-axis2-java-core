package axis2.om;

import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

/**
 * "Toy" OMElement class - uses simple String names, no QNames, no attributes.
 */
public class OMElement {
    String name;
    Object content;
    List children;

    public OMElement() {
    }

    public OMElement(String name) {
        this.name = name;
    }

    public OMElement(String name, Object content) {
        this.name = name;
        this.content = content;
    }

    public void addChild(OMElement child) {
        if (children == null) {
            children = new ArrayList();
        }
        children.add(child);
    }

    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
        this.content = content;
    }

    public void writeTo(OMWriter writer) throws Exception {
        writer.startElement(name);

        if (content != null) {
            writer.serialize(content);
        } else if (children != null) {
            for (Iterator i = children.iterator(); i.hasNext();) {
                OMElement child = (OMElement) i.next();
                child.writeTo(writer);
            }
        }

        writer.endElement();
    }
}
