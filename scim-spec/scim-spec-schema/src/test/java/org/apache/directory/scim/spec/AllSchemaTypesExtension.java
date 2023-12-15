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

package org.apache.directory.scim.spec;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Data;
import org.apache.directory.scim.spec.annotation.ScimAttribute;
import org.apache.directory.scim.spec.annotation.ScimExtensionType;
import org.apache.directory.scim.spec.resources.ScimExtension;
import org.apache.directory.scim.spec.schema.ResourceReference;
import org.apache.directory.scim.spec.schema.Schema;

import java.time.Instant;
import java.util.Date;
import java.util.List;

/**
 * An extension that can be used to validate schema generation for each type of attribute.
 */
@XmlRootElement( name = "AllSchemaTypesExtension", namespace = "https://directory.apache.org/scimple/test/extensions" )
@XmlAccessorType(XmlAccessType.NONE)
@Data
@ScimExtensionType(id = AllSchemaTypesExtension.SCHEMA_URN, description="All Schema Attribute Types", name="AllSchemaTypesExtension", required=true)
public class AllSchemaTypesExtension implements ScimExtension {
  
  public static final String  SCHEMA_URN = "urn:mem:params:scim:schemas:extension:AllSchemaTypesExtension";

  @ScimAttribute(description = "One String")
  @XmlElement
  private String string1;

  @ScimAttribute(caseExact = true)
  @XmlElement
  private String string2;

  @ScimAttribute
  @XmlElement
  private List<String> stringList1;

  @ScimAttribute(required = true)
  @XmlElement
  private Boolean boolean1;

  @ScimAttribute(uniqueness = Schema.Attribute.Uniqueness.SERVER)
  @XmlElement
  private List<Boolean> booleanList1;

  @ScimAttribute(required = true)
  @XmlElement
  private boolean boolean2;

  @ScimAttribute(mutability = Schema.Attribute.Mutability.IMMUTABLE)
  @XmlElement
  private boolean[] booleanArray1;

  @ScimAttribute
  @XmlElement
  private Double decimal1;

  @ScimAttribute(required = true)
  @XmlElement
  private double decimal2;

  @ScimAttribute
  @XmlElement
  private List<Double> decimalList1;

  @ScimAttribute
  @XmlElement
  private double[] decimalArray1;

  @ScimAttribute
  @XmlElement
  private Integer integer1;

  @ScimAttribute(required = true)
  @XmlElement
  private int integer2;

  @ScimAttribute
  @XmlElement
  private List<Integer> integerList1;

  @ScimAttribute
  @XmlElement
  private int[] integerArray1;

  @ScimAttribute
  @XmlElement
  private Date date1;

  @ScimAttribute
  @XmlElement
  private List<Date> dateList1;

  @ScimAttribute
  @XmlElement
  private Date[] dateArray1;

  @ScimAttribute
  @XmlElement
  private Instant instant1;

  @ScimAttribute
  @XmlElement
  private List<Instant> instantList1;

  @ScimAttribute
  @XmlElement
  private Instant[] instantArray1;

  @ScimAttribute
  @XmlElement
  private byte[] binary1;

  @ScimAttribute
  @XmlElement
  private List<byte[]> binaryList1;

  @ScimAttribute
  @XmlElement
  private ResourceReference.ReferenceType ref1;

  @ScimAttribute
  @XmlElement
  private List<ResourceReference.ReferenceType> refList1;

  @Override
  public String getUrn() {
    return SCHEMA_URN;
  }

}
