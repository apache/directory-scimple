package edu.psu.swe.scim.rdbms.filter;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;
import javax.ws.rs.core.Response.Status;

import edu.psu.swe.scim.server.exception.AttributeDoesNotExistException;
import edu.psu.swe.scim.server.provider.extensions.AttributeFilterExtension;
import edu.psu.swe.scim.server.provider.extensions.ScimRequestContext;
import edu.psu.swe.scim.server.provider.extensions.exceptions.ClientFilterException;
import edu.psu.swe.scim.server.utility.AttributeUtil;
import edu.psu.swe.scim.spec.protocol.attribute.AttributeReference;
import edu.psu.swe.scim.spec.resources.ScimResource;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExampleFilter implements AttributeFilterExtension {

  @Inject
  Instance<AttributeUtil> attributeUtil;

  private static final Set<String> allowedAttributes = new HashSet<>();
  private static final Set<AttributeReference> allowedReferences = new HashSet<>();

  static {
    allowedAttributes.add("name.givenName");
    allowedAttributes.add("name.familyName");
    allowedAttributes.add("userName");
    allowedAttributes.add("emails");
    allowedAttributes.add("addresses.streetAddress");

    allowedReferences.add(new AttributeReference("name.givenName"));
    allowedReferences.add(new AttributeReference("name.familyName"));
    allowedReferences.add(new AttributeReference("userName"));
    allowedReferences.add(new AttributeReference("emails"));
    allowedReferences.add(new AttributeReference("addresses.streetAddress"));
  }

  @Override
  public ScimResource filterAttributes(ScimResource scimResource, ScimRequestContext scimRequestContext) throws ClientFilterException {

    log.info("###### ----> In FilterAttributes");
    Set<AttributeReference> references = scimRequestContext.getAttributeReferences();

    if (!references.isEmpty()) {
      references = scimRequestContext.getAttributeReferences();

      log.info("###### -----> We have attributes through the web interface");
      Set<String> incomingAttributes = references.stream()
                                                 .map(ar -> ar.getFullAttributeName())
                                                 .collect(Collectors.toCollection(HashSet::new));

      for (AttributeReference ar : references) {
        incomingAttributes.add(ar.getFullAttributeName());
      }

      incomingAttributes.removeAll(allowedAttributes);
      if (!incomingAttributes.isEmpty()) {
        throw new ClientFilterException(Status.FORBIDDEN, "Request included parameters not allowed for this requestor");
      }
    }

    log.info("####### -----> Request is legal, strip down to the allowed references");
    try {
      return CDI.current().select(AttributeUtil.class).get().setAttributesForDisplay(scimResource, allowedReferences);
    } catch (IllegalArgumentException | AttributeDoesNotExistException e) {
      throw new ClientFilterException(Status.BAD_REQUEST, e.getMessage());
    } catch (IllegalAccessException | IOException e) {
      throw new ClientFilterException(Status.INTERNAL_SERVER_ERROR, e.getMessage());
    }     
  }
}
