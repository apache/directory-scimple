package edu.psu.swe.scim.spec.adapter;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import edu.psu.swe.scim.spec.protocol.search.Filter;

public class FilterAdapter extends XmlAdapter<String, Filter> {

  @Override
  public Filter unmarshal(String string) throws Exception {
    if (string == null) {
      return null;
    }
    return new Filter(string);
  }

  @Override
  public String marshal(Filter filter) throws Exception {
    if (filter == null) {
      return null;
    }
    return filter.getExpression().toFilter();
  }


}
