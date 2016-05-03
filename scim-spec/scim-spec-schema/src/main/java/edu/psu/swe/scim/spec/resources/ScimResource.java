package edu.psu.swe.scim.spec.resources;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

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

import edu.psu.swe.scim.spec.annotation.ScimAttribute;
import edu.psu.swe.scim.spec.annotation.ScimExtensionType;
import edu.psu.swe.scim.spec.exception.InvalidExtensionException;
import edu.psu.swe.scim.spec.extension.ScimExtensionRegistry;
import edu.psu.swe.scim.spec.schema.Meta;
import edu.psu.swe.scim.spec.schema.Schema.Attribute.Returned;
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
public abstract class ScimResource extends BaseResource {

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

  public void addExtension(ScimExtension extension) throws InvalidExtensionException {
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
  
  @SuppressWarnings("unchecked")
  public <T> T getExtension(Class<T> extensionClass) throws InvalidExtensionException {
    ScimExtensionType[] se = extensionClass.getAnnotationsByType(ScimExtensionType.class);

    if (se.length == 0 || se.length > 1) {
      throw new InvalidExtensionException("Registered extensions must have an ScimExtensionType annotation");
    }
    
    return (T) extensions.get(se[0].id());
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

}
