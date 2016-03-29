package edu.psu.swe.scim.spec.schema;

import java.util.List;

import javax.ws.rs.core.Response.Status;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import edu.psu.swe.scim.spec.exception.ScimException;
import lombok.Data;

@XmlAccessorType(XmlAccessType.NONE)
@Data
public abstract class MultiValuedAttribute {

  @XmlElement(name = "display")
  private String display;

  @XmlElement(name = "operation")
  private String operation;

  @XmlElement(name = "primary")
  private boolean primary = false;

  public static final String MULTIPLE_PRIMARIES_ERROR = "Multiple entries were flagged as primary.  Only on primary is allowed per attribute";

  /**
   * Returns the primary value
   * 
   * @param values
   * @return
   */
  static <T extends MultiValuedAttribute> T getPrimary(List<T> values) {
    T retVal = null;

    if (values != null) {
      for (T t : values) {
        if (t.isPrimary()) {
          retVal = t;
          break;
        }
      }
    }

    return retVal;
  }

  static <T extends MultiValuedAttribute> void addValue(T newValue, List<T> values) {
    if (values != null) {
      if (newValue != null && newValue.isPrimary()) {
        for (T t : values) {
          t.setPrimary(false);
        }
      }

      if (newValue != null) {
        values.add(newValue);
      }
    }
  }

  static <T extends MultiValuedAttribute> void validatePrimaryUniqueness(List<T> values) throws ScimException {
    if (values == null) {
      return;
    }

    boolean primaryFound = false;

    for (T t : values) {
      if (t != null) {
        if (t.isPrimary() && primaryFound == false) {
          primaryFound = true;
        } else if (t.isPrimary() && primaryFound == true) {
          throw new ScimException(new ErrorResponse("400", MULTIPLE_PRIMARIES_ERROR), Status.BAD_REQUEST);
        }
      }
    }
  }
}
