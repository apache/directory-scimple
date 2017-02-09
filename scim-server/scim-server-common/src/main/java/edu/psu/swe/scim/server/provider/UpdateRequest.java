package edu.psu.swe.scim.server.provider;

import java.util.List;

import edu.psu.swe.scim.spec.protocol.data.PatchOperation;
import lombok.Data;

@Data
public class UpdateRequest<T> {

  private String id;
  private T resource;
  private List<PatchOperation> patchOperations;

  public UpdateRequest(String id, T resource) {
    this.id = id;
    this.resource = resource;
  }
  
  public UpdateRequest(String id, List<PatchOperation> patchOperations) {
    this.id = id;
    this.patchOperations = patchOperations;
  }

}
