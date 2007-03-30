package org.apache.axis2.jaxws.description.builder.converter;

import org.apache.axis2.jaxws.description.builder.FieldDescriptionComposite;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/** This class will be used to convert Java Fields into FieldDescriptionComposite objects. */
public class JavaFieldsToFDCConverter {

    private Field[] fields;

    public JavaFieldsToFDCConverter(Field[] fields) {
        this.fields = fields;
    }

    /**
     * This method will be called to create <code>FieldDescriptionComposite</code> objects for public
     * fields in a given Class.
     *
     * @return - <code>List</code>
     */
    public List<FieldDescriptionComposite> convertFields() {
        List<FieldDescriptionComposite> fdcList = new
                ArrayList<FieldDescriptionComposite>();
        for (Field field : fields) {
            FieldDescriptionComposite fdc = new FieldDescriptionComposite();
            fdc.setFieldName(field.getName());
            fdc.setModifierType(Modifier.toString(field.getModifiers()));
            attachHandlerChainAnnotation(fdc, field);
            attachWebServiceRefAnnotation(fdc, field);
        }
        return fdcList;
    }

    /**
     * This method will drive the call to attach @HandlerChain annotation data if it is found on the
     * Field.
     *
     * @param fdc   - <code>FieldDescriptionComposite</code>
     * @param field - <code>Field</code>
     */
    private void attachHandlerChainAnnotation(FieldDescriptionComposite fdc,
                                              Field field) {
        ConverterUtils.attachHandlerChainAnnotation(fdc, field);
    }

    /**
     * This method will drive the call to attach @WebServiceRef annotation data
     * if it is found on the Field.
     * @param fdc - <code>FieldDescriptionComposite</code>
     * @param field - <code>Field</code>
     */
    private void attachWebServiceRefAnnotation(FieldDescriptionComposite fdc,
                                               Field field) {
        ConverterUtils.attachWebServiceRefAnnotation(fdc, field);
    }
}
