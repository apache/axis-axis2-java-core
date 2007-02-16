package userguide.mex.datalocators;

import org.apache.axis2.context.MessageContext;
import org.apache.axis2.dataRetrieval.AxisDataLocator;
import org.apache.axis2.dataRetrieval.DRConstants;
import org.apache.axis2.dataRetrieval.Data;
import org.apache.axis2.dataRetrieval.DataRetrievalException;
import org.apache.axis2.dataRetrieval.DataRetrievalRequest;
import org.apache.axis2.dataRetrieval.OutputForm;

/*
 * Sample user-defined Global Level Locator, GlobalDataLocator
 * 
 * The sample Data Locator implemented supports data retrieval for the Policy and Schema
 * dialects. For dialects that it does not understand, it delegates the request
 * to the available Data Locators in the hierachy by returning Result with
 * useDataLocatorHierachy indicator set.
 * 
 * See  {@link DemoServiceLevelDataLocator} for steps to invoke getData API
 * of this Data Locator.   
 * 
 */
public class GlobalDataLocator implements AxisDataLocator {

	public Data[] getData(DataRetrievalRequest request,
			MessageContext msgContext) throws DataRetrievalException {
		Data[] output = null;
        String dialect = request.getDialect();
        OutputForm form = request.getOutputForm();
        if (form == OutputForm.REFERENCE_FORM){
        	
        }
        if (dialect.equals(DRConstants.SPEC.DIALECT_TYPE_POLICY) || dialect.equals(DRConstants.SPEC.DIALECT_TYPE_SCHEMA)){
       	 System.out.print("ServiceLevelDataLocator has not implemented data retrieval for dialect " + dialect);
        	 System.out.println("");
       	 System.out.println("!!!! get Axis2 default Data Locator to retrieve data for " + dialect);

       	// result = new Result();
        	// result.setUseDataLocatorHierachy(true);
  
        }
        else {
       	 System.out.println("!!!! ServiceLevelDataLocator does not support dialect " + dialect);
       	 System.out.println("");
       	 System.out.println("!!!! get Axis2 default Data Locator to retrieve data for " + dialect);
        	// result = new Result();
        	// result.setUseDataLocatorHierachy(true);
          }
        return output;
		
	}

}
