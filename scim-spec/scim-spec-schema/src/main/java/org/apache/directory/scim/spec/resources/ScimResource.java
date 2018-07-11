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

package org.apache.directory.scim.spec.resources;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.AnnotationIntrospectorPair;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;

import org.apache.directory.scim.spec.annotation.ScimAttribute;
import org.apache.directory.scim.spec.annotation.ScimExtensionType;
import org.apache.directory.scim.spec.exception.InvalidExtensionException;
import org.apache.directory.scim.spec.extension.ScimExtensionRegistry;
import org.apache.directory.scim.spec.schema.Meta;
import org.apache.directory.scim.spec.schema.Schema.Attribute.Returned;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * This class defines the attributes shared by all SCIM resources. It also
 * provides BVF annotations to allow validation of the POJO.
 * 
 * @author smoyer1
 */
@Data
@EqualsAndHashCode(callSuper = true)
@XmlAccessorType(XmlAccessType.NONE)
public abstract class ScimResource extends BaseResource implements Serializable {

  private static final long serialVersionUID = 3673404125396687366L;

  private static final Logger LOG = LoggerFactory.getLogger(ScimResource.class);

  @XmlElement
  @NotNull
  @ScimAttribute(returned = Returned.ALWAYS)
  Meta meta;

  @XmlElement
  @Size(min = 1)
  @ScimAttribute(required = true, returned = Returned.ALWAYS)
  String id;

  @XmlElement
  @ScimAttribute
  String externalId;

  // TODO - Figure out JAXB equivalent of JsonAnyGetter and JsonAnySetter
  // (XmlElementAny?)
  private Map<String, ScimExtension> extensions = new HashMap<String, ScimExtension>();

  private String baseUrn;

  public ScimResource(String urn) {
    super(urn);
    this.baseUrn = urn;
  }

  /**
   * Add an extension to the ScimResource
   * @param extension the scim extension
   * @throws InvalidExtensionException if the ScimExtension passed in is improperly configured.  
   */
  public void addExtension(ScimExtension extension) {
    ScimExtensionType[] se = extension.getClass().getAnnotationsByType(ScimExtensionType.class);

    if (se.length == 0 || se.length > 1) {
      throw new InvalidExtensionException("Registered extensions must have an ScimExtensionType annotation");
    }

    String extensionUrn = se[0].id();
    extensions.put(extensionUrn, extension);
    
    addSchema(extensionUrn);
  }

  public ScimExtension getExtension(String urn) {
    return extensions.get(urn);
  }
  
  /**
   * Returns the scim extension of a particular class
   * @param extensionClass 
   * @return
   * @throws InvalidExtensionException if the ScimExtension passed in is improperly configured.  
   */
  @SuppressWarnings("unchecked")
  public <T> T getExtension(Class<T> extensionClass) {
    ScimExtensionType se = lookupScimExtensionType(extensionClass);
    
    return (T) extensions.get(se.id());
  }

  private <T> ScimExtensionType lookupScimExtensionType(Class<T> extensionClass) {
    ScimExtensionType[] se = extensionClass.getAnnotationsByType(ScimExtensionType.class);

    if (se.length == 0 || se.length > 1) {
      throw new InvalidExtensionException("Registered extensions must have an ScimExtensionType annotation");
    }

    return se[0];
  }

  public abstract String getResourceType();

  public String getBaseUrn() {
    return baseUrn;
  }

  @JsonAnyGetter
  public Map<String, ScimExtension> getExtensions() {
    return extensions;
  }

  @JsonAnySetter
  public void setExtensions(String key, Object value) {
    LOG.debug("Found a ScimExtension");
    LOG.debug("Extension's URN: " + key);
    LOG.debug("Extension's string representation: " + value);

    Class<? extends ScimResource> resourceClass = getClass();
    LOG.debug("Resource class: " + resourceClass.getSimpleName());

    Class<? extends ScimExtension> extensionClass = ScimExtensionRegistry.getInstance().getExtensionClass(resourceClass, key);

    if (extensionClass != null) {
      LOG.debug("Extension class: " + extensionClass.getSimpleName());

      ObjectMapper objectMapper = new ObjectMapper();
      JaxbAnnotationModule jaxbAnnotationModule = new JaxbAnnotationModule();
      objectMapper.registerModule(jaxbAnnotationModule);

      AnnotationIntrospector jaxbIntrospector = new JaxbAnnotationIntrospector(objectMapper.getTypeFactory());
      AnnotationIntrospector jacksonIntrospector = new JacksonAnnotationIntrospector();
      AnnotationIntrospector pair = new AnnotationIntrospectorPair(jacksonIntrospector, jaxbIntrospector);
      objectMapper.setAnnotationIntrospector(pair);

      ScimExtension extension = objectMapper.convertValue(value, extensionClass);
      if (extension != null) {
        LOG.debug("    ***** Added extension to the resource *****");
        extensions.put(key, extension);
      }
    }
  }
  
  public ScimExtension removeExtension(String urn) {
    return extensions.remove(urn);
  }
  
  @SuppressWarnings("unchecked")
  public <T> T removeExtension(Class<T> extensionClass) {
    ScimExtensionType se = lookupScimExtensionType(extensionClass);
    
    return (T) extensions.remove(se.id());
  }

}
