package edu.psu.swe.scim.spec.protocol.adapters;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import edu.psu.swe.scim.spec.schema.Schema;

public class SchemaToStringListAdapter extends XmlAdapter<List<String>, List<Schema>> {

  @Override
  public List<Schema> unmarshal(List<String> v) throws Exception {
   
    List<Schema> schemaList = new ArrayList<>();
    
    for (String uri : v) {
      Schema s = new Schema();
      s.setName(uri);
      schemaList.add(s);
    }
    
    return schemaList;
  }

  @Override
  public List<String> marshal(List<Schema> v) throws Exception {
    List<String> uriList = new ArrayList<>();
    
    for (Schema s : v) {
      uriList.add(s.getName());
    }
    
    return uriList;
  }
}
