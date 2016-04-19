package edu.psu.swe.scim.memory.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.inject.Named;

import edu.psu.swe.scim.memory.extensions.LuckyNumberExtension;
import edu.psu.swe.scim.server.provider.Provider;
import edu.psu.swe.scim.spec.protocol.data.SearchRequest;
import edu.psu.swe.scim.spec.resources.Email;
import edu.psu.swe.scim.spec.resources.ScimExtension;
import edu.psu.swe.scim.spec.resources.ScimUser;

@Named
public class InMemoryUserService implements Provider<ScimUser> {

  private Map<String, ScimUser> users = new HashMap<>();
  
  @PostConstruct
  public void init() {
    ScimUser user = new ScimUser();
    user.setId("1");
    user.setExternalId("e1");
    user.setDisplayName("User 1");
    Email email = new Email();
    email.setDisplay("e1@example.com");
    email.setValue("e1@example.com");
    email.setType("work");
    email.setPrimary(true);
    user.setEmails(Arrays.asList(email));
    
    LuckyNumberExtension luckyNumberExtension = new LuckyNumberExtension();
    luckyNumberExtension.setLuckyNumber(7);
    
    user.addExtension(LuckyNumberExtension.SCHEMA_URN, luckyNumberExtension);
    
    users.put(user.getId(), user);
  }
  
  @Override
  public ScimUser create(ScimUser resource) {
    users.put(resource.getId(), resource);
    return resource;
  }

  @Override
  public ScimUser update(ScimUser resource) {
    users.put(resource.getId(), resource);
    return resource;
  }

  @Override
  public ScimUser get(String id) {
    return users.get(id);
  }

  @Override
  public void delete(String id) {
    users.remove(id);
  }

  @Override
  public List<ScimUser> find(SearchRequest request) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<Class<? extends ScimExtension>> getExtensionList() {
    return Arrays.asList(LuckyNumberExtension.class);
  }

}
