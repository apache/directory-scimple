package edu.psu.swe.scim.memory.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import edu.psu.swe.scim.memory.extensions.LuckyNumberExtension;
import edu.psu.swe.scim.server.provider.Provider;
import edu.psu.swe.scim.spec.exception.InvalidExtensionException;
import edu.psu.swe.scim.spec.protocol.search.Filter;
import edu.psu.swe.scim.spec.protocol.search.PageRequest;
import edu.psu.swe.scim.spec.protocol.search.SortRequest;
import edu.psu.swe.scim.spec.resources.Email;
import edu.psu.swe.scim.spec.resources.ScimExtension;
import edu.psu.swe.scim.spec.resources.ScimUser;

/**
 * Creates a singleton (effectively) Provider<User> with a memory-based
 * persistence layer.
 * 
 * @author Chris Harm &lt;crh5255@psu.edu&gt;
 */
@Named
@ApplicationScoped
public class InMemoryUserService implements Provider<ScimUser> {
  
  static final String DEFAULT_USER_ID = "1";
  static final String DEFAULT_USER_EXTERNAL_ID = "e" + DEFAULT_USER_ID;
  static final String DEFAULT_USER_DISPLAY_NAME = "User " + DEFAULT_USER_ID;
  static final String DEFAULT_USER_EMAIL_VALUE = "e1@example.com";
  static final String DEFAULT_USER_EMAIL_TYPE = "work";
  static final int DEFAULT_USER_LUCKY_NUMBER = 7;

  private Map<String, ScimUser> users = new HashMap<>();
  
  @PostConstruct
  public void init() {
    ScimUser user = new ScimUser();
    user.setId(DEFAULT_USER_ID);
    user.setExternalId(DEFAULT_USER_EXTERNAL_ID);
    user.setUserName(DEFAULT_USER_EXTERNAL_ID);
    user.setDisplayName(DEFAULT_USER_DISPLAY_NAME);
    Email email = new Email();
    email.setDisplay(DEFAULT_USER_EMAIL_VALUE);
    email.setValue(DEFAULT_USER_EMAIL_VALUE);
    email.setType(DEFAULT_USER_EMAIL_TYPE);
    email.setPrimary(true);
    user.setEmails(Arrays.asList(email));
    
    LuckyNumberExtension luckyNumberExtension = new LuckyNumberExtension();
    luckyNumberExtension.setLuckyNumber(DEFAULT_USER_LUCKY_NUMBER);
    
    try {
      user.addExtension(luckyNumberExtension);
    } catch (InvalidExtensionException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    
    users.put(user.getId(), user);
  }
  
  /**
   * @see edu.psu.swe.scim.server.provider.Provider#create(java.lang.Object)
   */
  @Override
  public ScimUser create(ScimUser resource) {
    users.put(resource.getId(), resource);
    return resource;
  }

  /**
   * @see edu.psu.swe.scim.server.provider.Provider#update(java.lang.Object)
   */
  @Override
  public ScimUser update(String id, ScimUser resource) {
    users.put(id, resource);
    return resource;
  }

  /**
   * @see edu.psu.swe.scim.server.provider.Provider#get(java.lang.String)
   */
  @Override
  public ScimUser get(String id) {
    return users.get(id);
  }

  /**
   * @see edu.psu.swe.scim.server.provider.Provider#delete(java.lang.String)
   */
  @Override
  public void delete(String id) {
    users.remove(id);
  }

  /**
   * @see edu.psu.swe.scim.server.provider.Provider#find(edu.psu.swe.scim.spec.protocol.search.Filter, edu.psu.swe.scim.spec.protocol.search.PageRequest, edu.psu.swe.scim.spec.protocol.search.SortRequest)
   */
  @Override
  public List<ScimUser> find(Filter filter, PageRequest pageRequest, SortRequest sortRequest) {
    return new ArrayList<ScimUser>(users.values());
  }

  /**
   * @see edu.psu.swe.scim.server.provider.Provider#getExtensionList()
   */
  @Override
  public List<Class<? extends ScimExtension>> getExtensionList() {
    return Arrays.asList(LuckyNumberExtension.class);
  }

}
