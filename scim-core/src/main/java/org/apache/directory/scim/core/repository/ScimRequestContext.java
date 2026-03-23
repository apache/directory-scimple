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

package org.apache.directory.scim.core.repository;

import org.apache.directory.scim.spec.filter.PageRequest;
import org.apache.directory.scim.spec.filter.SortRequest;
import org.apache.directory.scim.spec.filter.attribute.AttributeReference;

import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class ScimRequestContext {

  private Set<AttributeReference> includedAttributes;
  private Set<AttributeReference> excludedAttributes;
  private PageRequest pageRequest;
  private SortRequest sortRequest;

  /**
   * Optional ETag(s) in 'If-Match' header. If not null, to avoid dirty writing, {@code ScimResource.meta.version} must match one of this set (the set should contain only one element).
   */
  private Set<ETag> eTag;

  public ScimRequestContext() {
    this(Set.of(), Set.of());
  }

  /**
   * Create a ScimRequestContext object.
   *
   * @param includedAttributes optional set of attributes to include from returned ScimResources, may be used to optimize queries.
   * @param excludedAttributes optional set of attributes to exclude from returned ScimResources, may be used to optimize queries.
   */
  public ScimRequestContext(Set<AttributeReference> includedAttributes,
                            Set<AttributeReference> excludedAttributes) {
    this(includedAttributes, excludedAttributes, null, null, Set.of());
  }
  /**
   * Create a ScimRequestContext object.
   *
   * @param includedAttributes optional set of attributes to include from returned ScimResources, may be used to optimize queries.
   * @param excludedAttributes optional set of attributes to exclude from returned ScimResources, may be used to optimize queries.
   * @param pageRequest For paged requests, this object specifies the start
   *        index and number of ScimResources that should be returned.
   * @param sortRequest Specifies which fields the returned ScimResources
   *        should be sorted by and whether the sort order is ascending or
   *        descending.
   */
  public ScimRequestContext(Set<AttributeReference> includedAttributes,
                            Set<AttributeReference> excludedAttributes,
                            PageRequest pageRequest,
                            SortRequest sortRequest,
                            Set<ETag> eTag) {
    this.includedAttributes = includedAttributes;
    this.excludedAttributes = excludedAttributes;
    this.pageRequest = pageRequest;
    this.sortRequest = sortRequest;
    this.eTag = eTag != null ? Collections.unmodifiableSet(eTag) :  Set.of();
  }

  public Set<AttributeReference> getIncludedAttributes() {
    return this.includedAttributes;
  }

  public ScimRequestContext setIncludedAttributes(Set<AttributeReference> includedAttributes) {
    this.includedAttributes = includedAttributes;
    return this;
  }

  public Set<AttributeReference> getExcludedAttributes() {
    return this.excludedAttributes;
  }

  public ScimRequestContext setExcludedAttributes(Set<AttributeReference> excludedAttributes) {
    this.excludedAttributes = excludedAttributes;
    return this;
  }

  public Optional<PageRequest> getPageRequest() {
    return Optional.ofNullable(pageRequest);
  }

  /**
   * Returns the page request, defaulting to an unbounded request if none was specified.
   *
   * @return the page request; never null
   */
  public PageRequest getPageRequestOrDefault() {
    return pageRequest != null ? pageRequest : new PageRequest();
  }

  public ScimRequestContext setPageRequest(PageRequest pageRequest) {
    this.pageRequest = pageRequest;
    return this;
  }

  public SortRequest getSortRequest() {
    return sortRequest;
  }

  public ScimRequestContext setSortRequest(SortRequest sortRequest) {
    this.sortRequest = sortRequest;
    return this;
  }

  public Set<ETag> getETag() {
    return eTag;
  }

  public ScimRequestContext setETag(Set<ETag> eTag) {
    this.eTag = eTag == null
      ? Collections.emptySet()
      : Collections.unmodifiableSet(eTag);
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    ScimRequestContext that = (ScimRequestContext) o;
    return Objects.equals(getIncludedAttributes(), that.getIncludedAttributes())
      && Objects.equals(getExcludedAttributes(), that.getExcludedAttributes())
      && Objects.equals(getPageRequest(), that.getPageRequest())
      && Objects.equals(getSortRequest(), that.getSortRequest())
      && Objects.equals(eTag, that.eTag);
  }

  @Override
  public int hashCode() {
    return Objects.hash(getIncludedAttributes(), getExcludedAttributes(), getPageRequest(), getSortRequest(), eTag);
  }

  @Override
  public String toString() {
    return "ScimRequestContext{" +
      "includedAttributes=" + includedAttributes +
      ", excludedAttributes=" + excludedAttributes +
      ", pageRequest=" + pageRequest +
      ", sortRequest=" + sortRequest +
      ", eTag=" + eTag +
      '}';
  }

  public static ScimRequestContext empty() {
    return new ScimRequestContext();
  }

  /**
   * Returns the fully qualified SCIM attribute names the client explicitly requested
   * via the {@code attributes} query parameter, or an empty set if no specific
   * attributes were requested (meaning all attributes should be returned).
   *
   * <p>All attribute names are returned in their fully qualified form
   * (e.g., {@code urn:ietf:params:scim:schemas:core:2.0:User:userName}).
   * The server layer normalizes unqualified attribute references before they
   * reach the repository, so all references in this context carry their schema URN.</p>
   *
   * <p>Repository implementations MAY use this to optimize backend queries
   * (e.g., selecting specific LDAP attributes or database columns). The SCIM server
   * layer always applies attribute filtering post-retrieval as a safety net, so
   * repositories that ignore this hint will still produce correct results.</p>
   *
   * @return set of fully qualified SCIM attribute names, or empty for "all"
   * @see #getIncludedAttributes()
   */
  public Set<String> getIncludedAttributeNames() {
    if (includedAttributes == null) {
      return Set.of();
    }
    return includedAttributes.stream()
      .map(AttributeReference::getFullyQualifiedAttributeName)
      .collect(Collectors.toUnmodifiableSet());
  }

  /**
   * Returns the fully qualified SCIM attribute names the client explicitly excluded
   * via the {@code excludedAttributes} query parameter, or an empty set if none
   * were excluded.
   *
   * <p>Attribute names follow the same fully qualified rules as
   * {@link #getIncludedAttributeNames()}.</p>
   *
   * <p>Repository implementations MAY use this to optimize backend queries by
   * omitting excluded attributes from the fetch. The SCIM server layer always
   * applies attribute filtering post-retrieval as a safety net.</p>
   *
   * @return set of fully qualified excluded SCIM attribute names, or empty for "none excluded"
   * @see #getExcludedAttributes()
   */
  public Set<String> getExcludedAttributeNames() {
    if (excludedAttributes == null) {
      return Set.of();
    }
    return excludedAttributes.stream()
      .map(AttributeReference::getFullyQualifiedAttributeName)
      .collect(Collectors.toUnmodifiableSet());
  }
}
