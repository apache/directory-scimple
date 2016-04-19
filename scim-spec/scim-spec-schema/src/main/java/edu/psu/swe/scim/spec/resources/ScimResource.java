package edu.psu.swe.scim.spec.resources;

import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;

import edu.psu.swe.scim.spec.annotation.ScimAttribute;
import edu.psu.swe.scim.spec.schema.Meta;
import edu.psu.swe.scim.spec.schema.Schema.Attribute.Returned;

/**
 * This class defines the attributes shared by all SCIM resources.  It also
 * provides BVF annotations to allow validation of the POJO.
 * 
 * @author smoyer1
 */
@Data
@EqualsAndHashCode(callSuper = true)
@XmlAccessorType(XmlAccessType.NONE)
public abstract class ScimResource extends BaseResource {
  
  private static final Logger LOG = LoggerFactory.getLogger(ScimResource.class);
  
  @XmlElement
  @NotNull
  @ScimAttribute(returned=Returned.ALWAYS)
  Meta meta;
  
  @XmlElement
  @Size(min = 1)
  @ScimAttribute(required=true, returned=Returned.ALWAYS)
  String id;
  
  @XmlElement
  @ScimAttribute
  String externalId;
  
  // TODO - Figure out JAXB equivalent of JsonAnyGetter and JsonAnySetter (XmlElementAny?)
  private Map<String, ScimExtension> extensions = new HashMap<String, ScimExtension>();

  private String baseUrn;
  
  public ScimResource(String urn) {
    super(urn);
    this.baseUrn = urn;
  }
  
  public void addExtension(String urn, ScimExtension extension) {
    extensions.put(urn, extension);
  }
  
  public ScimExtension getExtension(String urn) {
    return extensions.get(urn);  
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
  public void setExtensions(Map<String, Object> extensions) {
//    LOG.debug("Found a ScimExtension");
//    LOG.debug("Extension's URN: " + key);
//    LOG.debug("Extension's string representation: " + value);
//    
//    Class<? extends ScimResource> resourceClass = getClass();
//    LOG.debug("Resource class: " + resourceClass.getSimpleName());
//    
//    Class<? extends ScimExtension> extensionClass = ScimExtensionRegistry.getInstance().getExtensionClass(resourceClass, key);
//    LOG.debug("Extension class: " + extensionClass.getSimpleName());
//    
//    ObjectMapper mapper = new ObjectMapper();
//    AnnotationIntrospector jaxbIntrospector = new JaxbAnnotationIntrospector(mapper.getTypeFactory());
//    AnnotationIntrospector jacksonIntrospector = new JacksonAnnotationIntrospector();
//    AnnotationIntrospector pair = new AnnotationIntrospectorPair(jacksonIntrospector, jaxbIntrospector);
//    mapper.setAnnotationIntrospector(pair);
//    
//    ScimExtension extension = mapper.convertValue(value, extensionClass);
//    if(extension != null) {
//      LOGGER.debug("    ***** Added extension to the resource *****");
//      extensions.put(key, extension);
//    }
//    this.extensions = extensions;
  }
  
  

}
