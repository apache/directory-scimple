package edu.psu.swe.scim.spec.id;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import lombok.Getter;

@Getter
@XmlAccessorType(XmlAccessType.NONE)
public class ScimResourceId implements CharSequence {
  final String value;
  final boolean bulkId;

  public ScimResourceId(String value) {
    this.value = value;
    this.bulkId = value.startsWith("bulkId:");
  }

  @Override
  public String toString() {
    return this.value;
  }

  @Override
  public int length() {
    return this.value.length();
  }

  @Override
  public char charAt(int index) {
    return this.value.charAt(index);
  }

  @Override
  public CharSequence subSequence(int start, int end) {
    return this.value.subSequence(start, end);
  }
}
