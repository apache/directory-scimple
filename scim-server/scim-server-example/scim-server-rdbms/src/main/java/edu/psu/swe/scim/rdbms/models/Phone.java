package edu.psu.swe.scim.rdbms.models;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Table
@Data
public class Phone {

  @Id
  @Column(name="phone_id")
  private long phoneId;
  
  @Column(name="international_prefix")
  private Integer internationalPrefix;
  
  @Column(name="number")
  private String number;
  
  @Column(name="extension")
  private String extension;
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    
    sb.append("tel:");
    if (internationalPrefix != null) {
      sb.append("+");
      sb.append(internationalPrefix);
      sb.append(" ");
    }
    
    sb.append(number);
    if (extension != null) {
      sb.append(";ext=");
      sb.append(extension);
    }
    
    return sb.toString();
  }
}
