package edu.psu.swe.scim.rdbms.model;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import lombok.Data;

// The names in this class intentionally don't match scim names.  
// This will be used to illustrate name mapping

@Entity
@Data
public class Person {
  
  @Id
  @Column(name="person_id")
  private long personId;
  
  @Column(name="first_name")
  private String firstName;

  @Column(name="last_name")
  private String lastName;
  
  @Column(name="middle_name")
  private String middleName;
  
  @Column(name="active")
  private boolean active;
  
  @OneToMany(fetch=FetchType.EAGER, mappedBy="person")
  List<Address> addressList;
  
  @OneToMany(fetch=FetchType.EAGER, mappedBy="person")
  List<Phone> phoneList;
}
