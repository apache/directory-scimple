package edu.psu.swe.scim.spec.protocol.search;

import lombok.Data;

@Data
public class PageRequest {
  private Integer startIndex;
  private Integer count;
}
