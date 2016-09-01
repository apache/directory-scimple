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
public class Address {
  
  @Id
  @Column(name="address_id")
  private long addressId;
  
  @Column(name="Street_address")
  private String streetAddress;
  
  @Column(name="city")
  private String city;
  
  @Column(name="state")
  private String state;
  
  @Column(name="country")
  private String countryCode;
  
  //Intentionally not named postalCode
  @Column(name="zip_code")
  private String zipCode;
  
  @ManyToOne(fetch=FetchType.LAZY)
  @JoinColumn(name="person_id")
  private Person person;
}
