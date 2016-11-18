package edu.psu.swe.scim.client.rest;

import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.GenericType;

import edu.psu.swe.commons.jaxrs.exceptions.RestClientException;
import edu.psu.swe.scim.spec.resources.ScimUser;

public class ScimUserClient extends BaseScimClient<ScimUser> {

  private static final GenericType<List<ScimUser>> LIST_SCIM_USER = new GenericType<List<ScimUser>>(){};

  public ScimUserClient(Client client, String baseUrl) throws IllegalArgumentException {
    super(client, baseUrl, ScimUser.class, LIST_SCIM_USER);
  }

  public static void main(String[] args) throws RestClientException {
    try (ScimUserClient c = new ScimUserClient(javax.ws.rs.client.ClientBuilder.newClient(), "http://localhost:8000")) {
      ScimUser user = new ScimUser();
      edu.psu.swe.scim.spec.resources.Name name = new edu.psu.swe.scim.spec.resources.Name();

      name.setFamilyName("Familyname");
      name.setGivenName("Bob");
      name.setMiddleName("Bobby");
      user.setActive(true);
      user.setAddresses(new java.util.ArrayList<>());
      user.setDisplayName("display_name_bob");
      user.setEmails(new java.util.ArrayList<>());
      user.setEntitlements(new java.util.ArrayList<>());
      user.setExternalId("external_id_bob");
      user.setIms(new java.util.ArrayList<>());
      user.setLocale("locale");
      user.setName(name);
      user.setNickName("Bob");
      user.setUserName("bob");

      c.getById("userA");
      c.delete("userA");
      c.create(user);
      c.update("userB", user);
    }
  }
}
