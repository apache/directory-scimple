/*
 * The Pennsylvania State University © 2016
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.directory.scim.ws.common;

import jakarta.ws.rs.core.Response.Status;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "error-message")
@XmlAccessorType(XmlAccessType.NONE)
public class JaxRsStatusAdapterType
{
  @XmlElement(name="code")
  private int code;
  
  @XmlElement(name="message")
  private String message;
  
  public JaxRsStatusAdapterType() {
    // Required no-argument constructor for JAXB marshalling/unmarshalling
  }
  
  public JaxRsStatusAdapterType(Status status)
  {
    code = status.getStatusCode();
    message  = status.name();
  }
  
  public void setStatus(Status status)
  {
    code = status.getStatusCode();
    message = status.name();
  }
  
  public Status getStatus()
  {
    return Status.fromStatusCode(code);
  }
  
  public int getCode()
  {
    return code;
  }
  
  public String getMessage()
  {
    return message;
  }
}
