package edu.psu.swe.scim.memory.service;

import javax.inject.Inject;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.psu.swe.scim.server.provider.Provider;
import edu.psu.swe.scim.server.provider.ProviderRegistry;
import edu.psu.swe.scim.spec.resources.ScimUser;

@WebListener
public class ScimConfigurator implements ServletContextListener {

  public static final Logger LOG = LoggerFactory.getLogger(ScimConfigurator.class);

  @Inject
  private ProviderRegistry providerRegistry;

  @Inject 
  private Provider<ScimUser> userProvider;
  
  @Override
  public void contextInitialized(ServletContextEvent sce) {
    providerRegistry.registerProvider(ScimUser.class, userProvider);
  }

  @Override
  public void contextDestroyed(ServletContextEvent arg0) {
    // NOOP
  }
}
