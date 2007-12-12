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
!!V 03/03/98 rel. _.___- SWING and reorganization.
    ***
    30/06/98 rel. _.___- Swing, JBuilder2 e VSS.
 */


package net.infordata.em.crt5250;

import java.io.IOException;


/**
 * Common base interface for XI5250FieldsList and XI5250Field.
 * Only for possible future implementations.
 *
 * @version
 * @author   Valentino Proietti - Infordata S.p.A.
 */
public interface XI5250BaseField {
  /**
   */
  public void init();


  /**
   */
  public void saveTo(XI5250FieldSaver aSaver) throws IOException;
}