package edu.psu.swe.scim.memory.service;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;

import edu.psu.swe.scim.server.exception.InvalidProviderException;
import edu.psu.swe.scim.server.exception.UnableToRetrieveExtensionsException;
import edu.psu.swe.scim.server.provider.ProviderRegistry;
import edu.psu.swe.scim.spec.resources.ScimGroup;
import edu.psu.swe.scim.spec.resources.ScimUser;

@WebListener
public class ScimConfigurator implements ServletContextListener {

  public static final Logger LOG = LoggerFactory.getLogger(ScimConfigurator.class);

  @Inject
  private ProviderRegistry providerRegistry;

  @Inject
  private Instance<InMemoryUserService> userProviderInstance;

  @Inject
  private Instance<InMemoryGroupService> groupProviderInstance;

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    try {
      providerRegistry.registerProvider(ScimUser.class, userProviderInstance);
      providerRegistry.registerProvider(ScimGroup.class, groupProviderInstance);
    } catch (InvalidProviderException | JsonProcessingException | UnableToRetrieveExtensionsException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void contextDestroyed(ServletContextEvent arg0) {
    // NOOP
  }

}
