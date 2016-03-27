package edu.psu.swe.scim.server.provider;

import java.util.List;

import edu.psu.swe.scim.spec.protocol.data.SearchRequest;
import edu.psu.swe.scim.spec.resources.ScimUser;

public interface UserProvider {
  public ScimUser createUser(ScimUser scimGroup);
  public ScimUser updateUser(ScimUser scimGroup);
  public ScimUser getUser(String id);
  public void deleteUser(String id);
  public List<ScimUser> findGroups(SearchRequest request);
}
