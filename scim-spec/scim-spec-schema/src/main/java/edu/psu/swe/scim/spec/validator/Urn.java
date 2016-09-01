package edu.psu.swe.scim.spec.validator;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;

import javax.validation.Constraint;
import javax.validation.Payload;

@Constraint(validatedBy = UrnValidator.class)
@Target( { METHOD, FIELD, PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface  Urn 
{
  String message() default "The urn is malformed";
	 
  Class<?>[] groups() default {};
  
  Class<? extends Payload>[] payload() default {};
}
