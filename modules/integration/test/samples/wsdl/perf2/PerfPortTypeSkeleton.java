/**
 * PerfPortTypeSkeleton.java This file was auto-generated from WSDL by the
 * Apache Axis2 version: 0.94-dev Jan 07, 2006 (08:13:00 EST)
 */
package samples.wsdl.perf2;

/**
 * PerfPortTypeSkeleton java skeleton for the axisService
 */
public class PerfPortTypeSkeleton {
    /**
     * Auto generated method signature
     *
     * @param param0
     */
    public OutputElement handleStringArray(
        InputElement param0) {

        OutputElement output = new OutputElement();
        output.setOutputElement("The Array length is - " + param0.getItem().length);
        return output;
    } 
}
