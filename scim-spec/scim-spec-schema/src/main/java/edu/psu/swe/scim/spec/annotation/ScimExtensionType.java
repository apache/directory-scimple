package edu.psu.swe.scim.spec.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface ScimExtensionType {
  
  String id();
  String name() default "";
  boolean required() default false;
  String description() default "";
}