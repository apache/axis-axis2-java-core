package org.apache.axis2.jaxws.description.builder.converter;

import org.apache.axis2.jaxws.description.builder.ParameterDescriptionComposite;
import org.apache.axis2.jaxws.description.builder.WebParamAnnot;

import javax.jws.WebParam;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class JavaParamToPDCConverter {

    private Type[] paramTypes;

    private Annotation[][] paramAnnotations;

    public JavaParamToPDCConverter(Type[] paramTypes, Annotation[][] paramAnnotations) {
        this.paramTypes = paramTypes;
        this.paramAnnotations = paramAnnotations;
    }

    public List<ParameterDescriptionComposite> convertParams() {
        List<ParameterDescriptionComposite> pdcList = new
                ArrayList<ParameterDescriptionComposite>();
        for (int i = 0; i < paramTypes.length; i++) {
            ParameterDescriptionComposite pdc = new ParameterDescriptionComposite();
            Type paramType = paramTypes[i];
            if (paramType instanceof Class) {
                Class paramClass = (Class)paramType;
                String fullType = "";
                pdc.setParameterType(paramClass.getName());
            } else if (paramType instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType)paramType;
                String fullType = "";
                fullType = ConverterUtils.getFullType(pt, fullType);
                pdc.setParameterType(fullType);
            }
            pdc.setListOrder(i);
            attachWebParamAnnotation(pdc, i);
            pdcList.add(pdc);
        }
        return pdcList;
    }


    /**
     * This method will attach @WebParam annotation data to the <code> ParameterDescriptionComposite</code>
     * if the annotation was found on the parameter represented by this index in the parameter list.
     *
     * @param pdc   - <code>ParameterDescriptionComposite</code>
     * @param order - the current index in the parameter list
     */
    private void attachWebParamAnnotation(ParameterDescriptionComposite pdc, int order) {
        Annotation[] orderAnnots = paramAnnotations[order];
        for (Annotation annot : orderAnnots) {
            if (annot instanceof WebParam) {
                WebParam webParam = (WebParam)annot;
                WebParamAnnot wpAnnot = WebParamAnnot.createWebParamAnnotImpl();
                wpAnnot.setHeader(webParam.header());
                wpAnnot.setMode(webParam.mode());
                wpAnnot.setName(webParam.name());
                wpAnnot.setPartName(webParam.partName());
                wpAnnot.setTargetNamespace(webParam.targetNamespace());
                pdc.setWebParamAnnot(wpAnnot);
            }
        }
    }
}
