/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package javax.xml.ws;

/**
 * Interface AsyncHandler<T>
 * The javax.xml.rpc.AsyncHandler interface is implemented by clients that 
 * wish to receive callback notification of the completion of service 
 * endpoint operations invoked asynchronously.
 * 
 * @version 1.0
 * @author sunja07
 */
public interface AsyncHandler<T> {

	/**
	 * Method handleResponse
	 * Called when the response to an asynchronous operation is available.
	 * 
	 * @param res - The response to the operation invocation. 
	 */
	void handleResponse(Response<T> res) ;

}
