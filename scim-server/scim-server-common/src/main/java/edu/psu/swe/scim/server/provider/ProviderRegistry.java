package edu.psu.swe.scim.server.provider;

import javax.ejb.Singleton;
import javax.ejb.Startup;

import edu.psu.swe.scim.spec.resources.ScimGroup;
import edu.psu.swe.scim.spec.resources.ScimUser;
import lombok.Data;

@Singleton
@Startup
@Data
public class ProviderRegistry {
  private Provider<ScimGroup> groupProvider = null;
  private Provider<ScimUser> userProfider = null;
}
