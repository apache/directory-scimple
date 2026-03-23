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

package org.apache.directory.scim.spec.filter.attribute;

import java.io.Serial;
import java.io.Serializable;

import org.apache.directory.scim.spec.validator.Urn;

public class AttributeReference implements Serializable {

  @Serial
  private static final long serialVersionUID = -3559538009692681470L;

  @Urn
  String urn;

  String attributeName;

  String subAttributeName;

  public AttributeReference(String name) {
    int endOfUrn = name.lastIndexOf(':');
    String[] attributes = name.substring(endOfUrn + 1).split("\\.");
    this.attributeName = attributes[0];

    if (endOfUrn > -1) {
      this.urn = name.substring(0, endOfUrn);
    }
    if (attributes.length > 1) {
      this.subAttributeName = attributes[1];
    }
  }

  public AttributeReference(String urn, String name) {
    this.urn = urn;

    if (name != null) {
      String[] attributes = name.split("\\.");
      this.attributeName = attributes[0];

      if (attributes.length > 1) {
        this.subAttributeName = attributes[1];
      }
    }
  }

  public AttributeReference(String urn, String attributeName, String subAttributeName) {
    this.urn = urn;
    this.attributeName = attributeName;
    this.subAttributeName = subAttributeName;
  }

  public String getFullAttributeName() {
    return this.attributeName + (this.subAttributeName != null ? "." + this.subAttributeName : "");
  }

  public String getFullyQualifiedAttributeName() {
    String fullyQualifiedAttributeName;
    StringBuilder sb = new StringBuilder();

    if (this.urn != null) {
      sb.append(this.urn);

      if (this.attributeName != null) {
        sb.append(":");
      }
    }
    if (this.attributeName != null) {
      sb.append(this.attributeName);
    }
    if (this.subAttributeName != null) {
      sb.append(".");
      sb.append(subAttributeName);
    }
    fullyQualifiedAttributeName = sb.toString();

    return fullyQualifiedAttributeName;
  }

  public String getAttributeBase() {
    String attributeBase;
    StringBuilder sb = new StringBuilder();

    if (this.urn != null) {
      sb.append(this.urn);

      if (this.subAttributeName != null) {
        sb.append(":");
        sb.append(this.attributeName);
      }
    } else if (this.subAttributeName != null) {
      sb.append(this.attributeName);
    }
    attributeBase = sb.toString();

    return attributeBase;
  }

  public boolean hasSubAttribute() {
    return subAttributeName != null;
  }

  /**
   * Returns {@code true} if this reference includes a schema URN prefix,
   * making it unambiguous across schemas.
   *
   * <p>Extension attributes are always fully qualified since their names
   * are only meaningful with the schema URN. Core attributes may or may
   * not be, depending on how the client sent them.</p>
   *
   * @return {@code true} if the URN is present
   */
  public boolean isFullyQualified() {
    return urn != null;
  }

  /**
   * @deprecated Use {@link #isFullyQualified()} instead.
   */
  @Deprecated
  public boolean hasUrn() {
    return isFullyQualified();
  }

  public String toString() {
    return (this.urn != null ? this.urn + ":" : "") + this.attributeName + (this.subAttributeName != null ? "." + this.subAttributeName : "");
  }

  public @Urn String getUrn() {
    return this.urn;
  }

  public AttributeReference setUrn(@Urn String urn) {
    this.urn = urn;
    return this;
  }

  public String getAttributeName() {
    return this.attributeName;
  }

  public AttributeReference setAttributeName(String attributeName) {
    this.attributeName = attributeName;
    return this;
  }

  public String getSubAttributeName() {
    return this.subAttributeName;
  }

  public AttributeReference setSubAttributeName(String subAttributeName) {
    this.subAttributeName = subAttributeName;
    return this;
  }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof AttributeReference other)) return false;
    if (!other.canEqual((Object) this)) return false;
    final Object this$urn = this.getUrn();
    final Object other$urn = other.getUrn();
    if (this$urn == null ? other$urn != null : !this$urn.equals(other$urn)) return false;
    final Object this$attributeName = this.getAttributeName();
    final Object other$attributeName = other.getAttributeName();
    if (this$attributeName == null ? other$attributeName != null : !this$attributeName.equals(other$attributeName))
      return false;
    final Object this$subAttributeName = this.getSubAttributeName();
    final Object other$subAttributeName = other.getSubAttributeName();
    if (this$subAttributeName == null ? other$subAttributeName != null : !this$subAttributeName.equals(other$subAttributeName))
      return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof AttributeReference;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    final Object $urn = this.getUrn();
    result = result * PRIME + ($urn == null ? 43 : $urn.hashCode());
    final Object $attributeName = this.getAttributeName();
    result = result * PRIME + ($attributeName == null ? 43 : $attributeName.hashCode());
    final Object $subAttributeName = this.getSubAttributeName();
    result = result * PRIME + ($subAttributeName == null ? 43 : $subAttributeName.hashCode());
    return result;
  }
}
