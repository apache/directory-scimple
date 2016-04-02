package edu.psu.swe.scim.spec.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import edu.psu.swe.scim.spec.schema.Schema.Attribute.Mutability;
import edu.psu.swe.scim.spec.schema.Schema.Attribute.Returned;
import edu.psu.swe.scim.spec.schema.Schema.Attribute.Type;
import edu.psu.swe.scim.spec.schema.Schema.Attribute.Uniqueness;


@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface ScimAttribute {
  
  String name() default "";
  boolean required() default false;
  String [] canonicalValues() default "";
  boolean caseExact() default false;
  Mutability mutability() default Mutability.READ_WRITE;
  Returned returned() default Returned.DEFAULT;
  Uniqueness uniqueness() default Uniqueness.NONE;
  Type [] referenceTypes() default Type.STRING;
  String description() default "";
}