package edu.psu.swe.scim.server.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE})
public @interface ScimElement {
  boolean required() default false;
  String [] canonicalValues();
  boolean caseExact() default false;
  String mutability() default "readWrite";
  String returned() default "default";
  String uniqueness() default "none";
  
  /**
  * +-----------+-------------+-----------------------------------------+
  * | SCIM Data | SCIM Schema | JSON Type                               |
  * | Type      | "type"      |                                         |
  * +-----------+-------------+-----------------------------------------+
  * | String    | "string"    | String per Section 7 of [RFC7159]       |
  * |           |             |                                         |
  * | Boolean   | "boolean"   | Value per Section 3 of [RFC7159]        |
  * |           |             |                                         |
  * | Decimal   | "decimal"   | Number per Section 6 of [RFC7159]       |
  * |           |             |                                         |
  * | Integer   | "integer"   | Number per Section 6 of [RFC7159]       |
  * |           |             |                                         |
  * | DateTime  | "dateTime"  | String per Section 7 of [RFC7159]       |
  * |           |             |                                         |
  * | Binary    | "binary"    | Binary value base64 encoded per Section |
  * |           |             | 4 of [RFC4648], or with URL and         |
  * |           |             | filename safe alphabet URL per Section  |
  * |           |             | 5 of [RFC4648] that is passed as a JSON |
  * |           |             | string per Section 7 of [RFC7159]       |
  * |           |             |                                         |
  * | Reference | "reference" | String per Section 7 of [RFC7159]       |
  * |           |             |                                         |
  * | Complex   | "complex"   | Object per Section 4 of [RFC7159]       |
  * +-----------+-------------+-----------------------------------------+
  */

  String [] referenceTypes() default "string";
}
