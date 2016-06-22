package edu.psu.swe.scim.rdbms.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import lombok.Data;

@Entity
@Data
public class Phone {

  @Id
  @Column(name="phone_id")
  private long phoneId;
  
  @Column(name="type")
  private String type;
  
  @Column(name="international_prefix")
  private Integer internationalPrefix;
  
  @Column(name="number")
  private String number;
  
  @Column(name="extension")
  private String extension;
  
  @ManyToOne(fetch=FetchType.LAZY)
  @JoinColumn(name="person_id")
  private Person person;
  
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
