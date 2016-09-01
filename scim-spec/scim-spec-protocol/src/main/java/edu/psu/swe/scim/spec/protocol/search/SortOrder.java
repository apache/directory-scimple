package edu.psu.swe.scim.spec.protocol.search;

import javax.xml.bind.annotation.XmlEnumValue;

public enum SortOrder {
  @XmlEnumValue("ascending")
  ASCENDING,
  @XmlEnumValue("descending")
  DESCENDING
}

