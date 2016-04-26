package edu.psu.swe.scim.spec.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import edu.psu.swe.scim.spec.schema.Schema.Attribute.Mutability;
import edu.psu.swe.scim.spec.schema.Schema.Attribute.Returned;
import edu.psu.swe.scim.spec.schema.Schema.Attribute.Uniqueness;


@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface ScimAttribute {
  
  //This is an ugly but necessary work around
  //We need something to determine which canonical value
  //parameter is desired and we can't use null so we had
  //to create this little empty enum as a place holder
  //for assignment checks.
  enum NoOp {
  }
  
  String name() default "";
  boolean required() default false;
  
  //These two canonical attributes should be mutually exclusive, if both are 
  //present we will reject the registered provider
  Class<? extends Enum<?>>  canonicalValueEnum() default NoOp.class;
  String [] canonicalValueList() default "";
  
  boolean caseExact() default false;
  Mutability mutability() default Mutability.READ_WRITE;
  Returned returned() default Returned.DEFAULT;
  Uniqueness uniqueness() default Uniqueness.NONE;
  String [] referenceTypes() default "";
  String description() default "";
}