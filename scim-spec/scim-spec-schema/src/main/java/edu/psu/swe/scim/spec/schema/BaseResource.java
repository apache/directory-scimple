package edu.psu.swe.scim.spec.schema;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import edu.psu.swe.scim.spec.protocol.adapters.SchemaToStringListAdapter;
import lombok.Data;

/**
 * This class defines the attributes shared by all SCIM resources.  It also
 * provides BVF annotations to allow validation of the POJO.
 * 
 * @author chrisharm
 */
@Data
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public abstract class BaseResource {
  
  @XmlElement
  @Size(min = 1)
  @XmlJavaTypeAdapter(SchemaToStringListAdapter.class)
  List<Schema> schemas;
  
  public void addSchema(Schema schema) {
    if (schemas == null){
      schemas = new ArrayList<>();
    }
    
    schemas.add(schema);
  }
  
}
