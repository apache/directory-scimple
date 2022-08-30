/*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements.  See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership.  The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License.  You may obtain a copy of the License at

* http://www.apache.org/licenses/LICENSE-2.0

* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.apache.directory.scim.spec.protocol;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Data;
import org.apache.directory.scim.spec.annotation.ScimAttribute;
import org.apache.directory.scim.spec.annotation.ScimExtensionType;
import org.apache.directory.scim.spec.resources.ScimExtension;
import org.apache.directory.scim.spec.schema.Schema;

/**
 * Allows a User's lucky number to be passed as part of the User's entry via
 * the SCIM protocol.
 * 
 * @author Chris Harm &lt;crh5255@psu.edu&gt;
 */
@XmlRootElement( name = "LuckyNumberExtension", namespace = "http://www.psu.edu/schemas/psu-scim" )
@XmlAccessorType(XmlAccessType.NONE)
@Data
@ScimExtensionType(id = LuckyNumberExtension.SCHEMA_URN, description="Lucky Numbers", name="LuckyNumbers", required=true)
public class LuckyNumberExtension implements ScimExtension {
  
  public static final String  SCHEMA_URN = "urn:mem:params:scim:schemas:extension:LuckyNumberExtension";

  @ScimAttribute(returned=Schema.Attribute.Returned.DEFAULT, required=true)
  @XmlElement
  private long luckyNumber;
  
  /**
   * Provides the URN associated with this extension which, as defined by the
   * SCIM specification is the extension's unique identifier.
   * 
   * @return The extension's URN.
   */
  @Override
  public String getUrn() {
    return SCHEMA_URN;
  }

}
