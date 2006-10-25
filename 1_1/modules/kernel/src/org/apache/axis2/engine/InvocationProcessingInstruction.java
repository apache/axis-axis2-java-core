/*
* Copyright 2006 The Apache Software Foundation.
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

package org.apache.axis2.engine;

/**
 * This type encapsulates an enumeration of possible message processing
 * instruction values that may be returned by a handler/phase within the
 * runtime.  The returned instruction will determine the next step in
 * the processing.
 */
public class InvocationProcessingInstruction
{
  public static InvocationProcessingInstruction CONTINUE_PROCESSING = new InvocationProcessingInstruction(0);
  public static InvocationProcessingInstruction SUSPEND_PROCESSING = new InvocationProcessingInstruction(1);
  public static InvocationProcessingInstruction ABORT_PROCESSING = new InvocationProcessingInstruction(2);
  
  private int instructionID;
    
  private InvocationProcessingInstruction(int instructionID)
  {
    this.instructionID = instructionID;
  }
    
  public boolean equals(InvocationProcessingInstruction instruction)
  {
    return this.instructionID == instruction.instructionID;
  }
    
  public int hashCode()
  {
    return instructionID;
  }
}
