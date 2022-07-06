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

package org.apache.directory.scim.server.utility;

import java.net.URI;

import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;

import org.apache.directory.scim.spec.annotation.ScimResourceType;
import org.apache.directory.scim.spec.exception.ScimResourceInvalidException;
import org.apache.directory.scim.spec.resources.ScimResource;

@RequestScoped
public class EndpointUtil {
  private URI baseUri;
  
  public UriBuilder getBaseUriBuilder() {
    return UriBuilder.fromUri(baseUri);
  }
  
  public UriBuilder getEndpointUriBuilder(Class<? extends ScimResource> resource) {
    ScimResourceType[] sr = resource.getAnnotationsByType(ScimResourceType.class);
    
    if (baseUri == null) {
      throw new IllegalStateException("BaseUri for Resource "+resource+" was null");
    }

    if (sr.length == 0 || sr.length > 1) {
      throw new ScimResourceInvalidException("ScimResource class must have a ScimResourceType annotation");
    }

    // yuck! TODO where to get REST endpoint from?
    String resourceName = sr[0].name() + "s";  
    
    return UriBuilder.fromUri(baseUri).path(resourceName);
  }
  
  public void process(UriInfo uriInfo) {
    baseUri = uriInfo.getBaseUri();
  }
}
