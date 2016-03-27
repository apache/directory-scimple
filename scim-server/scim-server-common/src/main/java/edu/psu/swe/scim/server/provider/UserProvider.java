package edu.psu.swe.scim.server.provider;

import java.util.List;

import edu.psu.swe.scim.spec.protocol.data.SearchRequest;
import edu.psu.swe.scim.spec.resources.ScimUser;

public interface UserProvider {
  public void createUser(ScimUser scimGroup);
  public void updateUser(ScimUser scimGroup);
  public void getUser(String id);
  public void deleteUser(String id);
  public List<ScimUser> findGroups(SearchRequest request);
}
