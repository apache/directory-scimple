package edu.psu.swe.scim.server.provider;

import java.util.List;

import edu.psu.swe.scim.spec.protocol.data.SearchRequest;
import edu.psu.swe.scim.spec.resources.ScimGroup;

public interface GroupProvider {
  public void createGroup(ScimGroup scimGroup);
  public void updateGroup(ScimGroup scimGroup);
  public void getGroup(String id);
  public void deleteGroup(String id);
  public List<ScimGroup> findGroups(SearchRequest request);
}
