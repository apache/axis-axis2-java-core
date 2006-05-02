/**
 * PerfPortTypeSkeleton.java This file was auto-generated from WSDL by the
 * Apache Axis2 version: 0.94-dev Jan 07, 2006 (08:13:00 EST)
 */
package samples.wsdl.perf;

/**
 * PerfPortTypeSkeleton java skeleton for the axisService
 */
public class PerfPortTypeSkeleton implements PerfPortTypeSkeletonInterface {
    /**
     * Auto generated method signature
     *
     * @param param0
     */
    public samples.wsdl.perf.OutputElementDocument handleStringArray(
        samples.wsdl.perf.InputElementDocument param0) {

        samples.wsdl.perf.OutputElementDocument output = samples.wsdl.perf.OutputElementDocument.Factory.newInstance();
        output.setOutputElement("The Array length is - " + param0.getInputElement().getItemArray().length);
        return output;
    } 
}
