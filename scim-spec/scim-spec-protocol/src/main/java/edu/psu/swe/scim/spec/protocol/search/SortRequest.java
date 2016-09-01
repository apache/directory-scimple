package edu.psu.swe.scim.spec.protocol.search;

import edu.psu.swe.scim.spec.protocol.attribute.AttributeReference;
import lombok.Data;

@Data
public class SortRequest {
  private AttributeReference sortBy;
  private SortOrder sortOrder;
}
