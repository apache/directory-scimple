package edu.psu.swe.scim.spec.protocol.filter;

import java.util.Collection;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import edu.psu.swe.scim.spec.protocol.search.PageRequest;

@Data
@EqualsAndHashCode
@ToString
public class FilterResponse<T> {
  
  private Collection<T> resources;
  private PageRequest pageRequest;
  private int totalResults;
  
  public FilterResponse() {}
  
  public FilterResponse(Collection<T> resources, PageRequest pageRequest, int totalResults) {
    this.resources = resources;
    this.pageRequest = pageRequest;
    this.totalResults = totalResults;
  }

}
