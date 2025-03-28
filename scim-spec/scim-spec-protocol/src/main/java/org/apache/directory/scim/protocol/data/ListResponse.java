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

package org.apache.directory.scim.protocol.data;

import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import org.apache.directory.scim.spec.resources.BaseResource;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class ListResponse<T> extends BaseResource<ListResponse<T>> {

  private static final long serialVersionUID = -2381780997440673136L;

  public static final String SCHEMA_URI = "urn:ietf:params:scim:api:messages:2.0:ListResponse";
  
  @XmlElement
  int totalResults;
  
  @XmlElement
  Integer startIndex;
  
  @XmlElement
  Integer itemsPerPage;

  @XmlElement(name = "Resources")
  List<T> resources;

  public ListResponse() {
    super(SCHEMA_URI);
  }

  public int getTotalResults() {
    return this.totalResults;
  }

  public ListResponse<T> setTotalResults(int totalResults) {
    this.totalResults = totalResults;
    return this;
  }

  public Integer getStartIndex() {
    return this.startIndex;
  }

  public Integer getItemsPerPage() {
    return this.itemsPerPage;
  }

  public ListResponse<T> setItemsPerPage(Integer itemsPerPage) {
    this.itemsPerPage = itemsPerPage;
    return this;
  }

  public List<T> getResources() {
    return this.resources;
  }

  public ListResponse<T> setResources(List<T> resources) {
    this.resources = resources;
    return this;
  }

  public ListResponse<T> setStartIndex(Integer startIndex) {
    this.startIndex = startIndex;
    return this;
  }

  public String toString() {
    return "ListResponse(totalResults=" + this.getTotalResults() + ", startIndex=" + this.getStartIndex() + ", itemsPerPage=" + this.getItemsPerPage() + ", resources=" + this.getResources() + ")";
  }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof ListResponse)) return false;
    final ListResponse<?> other = (ListResponse<?>) o;
    if (!other.canEqual((Object) this)) return false;
    if (!super.equals(o)) return false;
    if (this.getTotalResults() != other.getTotalResults()) return false;
    final Object this$startIndex = this.getStartIndex();
    final Object other$startIndex = other.getStartIndex();
    if (this$startIndex == null ? other$startIndex != null : !this$startIndex.equals(other$startIndex)) return false;
    final Object this$itemsPerPage = this.getItemsPerPage();
    final Object other$itemsPerPage = other.getItemsPerPage();
    if (this$itemsPerPage == null ? other$itemsPerPage != null : !this$itemsPerPage.equals(other$itemsPerPage))
      return false;
    final Object this$resources = this.getResources();
    final Object other$resources = other.getResources();
    if (this$resources == null ? other$resources != null : !this$resources.equals(other$resources)) return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof ListResponse;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = super.hashCode();
    result = result * PRIME + this.getTotalResults();
    final Object $startIndex = this.getStartIndex();
    result = result * PRIME + ($startIndex == null ? 43 : $startIndex.hashCode());
    final Object $itemsPerPage = this.getItemsPerPage();
    result = result * PRIME + ($itemsPerPage == null ? 43 : $itemsPerPage.hashCode());
    final Object $resources = this.getResources();
    result = result * PRIME + ($resources == null ? 43 : $resources.hashCode());
    return result;
  }
}
