package edu.psu.swe.scim.spec.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import edu.psu.swe.scim.spec.schema.Schema.Attribute.Mutability;
import edu.psu.swe.scim.spec.schema.Schema.Attribute.Returned;
import edu.psu.swe.scim.spec.schema.Schema.Attribute.Uniqueness;


@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface ScimType {
}