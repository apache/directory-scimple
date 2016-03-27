package edu.psu.swe.scim.server.provider;

import javax.ejb.Singleton;
import javax.ejb.Startup;

import lombok.Data;

@Singleton
@Startup
@Data
public class ProviderRegistry {
  private GroupProvider groupProvider = null;
  private UserProvider userProfider = null;
}
