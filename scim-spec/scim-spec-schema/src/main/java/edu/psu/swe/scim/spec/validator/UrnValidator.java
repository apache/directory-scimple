package edu.psu.swe.scim.spec.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class UrnValidator implements ConstraintValidator<Urn, String> {

  private static final String URN_RFC2141_REGEX = "^urn:[a-z0-9][a-z0-9-]{0,31}:([a-z0-9()+,\\-.:=@;$_!*']|%[0-9a-f]{2})+$";

  @Override
  public void initialize(Urn validator) {
  }

  @Override
  public boolean isValid(String urn, ConstraintValidatorContext context) {
    if (urn == null || urn.isEmpty()) {
      return true;
    }

    return urn.matches(URN_RFC2141_REGEX);
  }
}
