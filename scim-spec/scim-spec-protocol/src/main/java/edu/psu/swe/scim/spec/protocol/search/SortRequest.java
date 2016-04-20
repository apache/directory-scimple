package edu.psu.swe.scim.spec.protocol.search;

import lombok.Data;

@Data
public class SortRequest {
  private SortOrder sortOrder;
  private String sortBy;
}
