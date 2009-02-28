/*
Copyright 2007 Infordata S.p.A.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

/*
    ***
    30/06/98 rel. _.___- Swing, JBuilder2 e VSS.
 */
 
 
package net.infordata.em.tn5250;


/**
 * Used internally.<br>
 * It is raised if an error is detected while reading and parsing the 5250 stream.
 *
 * @version  
 * @author   Valentino Proietti - Infordata S.p.A.
 */
public class XI5250Exception extends Exception {

  private static final long serialVersionUID = 1L;
  
  private final int ivErrorCode;

  public XI5250Exception(String s, int errorCode) {
	super(s);
    ivErrorCode = errorCode;
  }
  
  public final int getErrorCode() {
    return ivErrorCode;
  }
}