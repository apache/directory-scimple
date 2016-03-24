package edu.psu.swe.scim.api.protocol;

public class ErrorMessageConstants {
  private ErrorMessageConstants() {
  }
  
  //HTTP 400 Error messages (SCIM Protocol Specification, section 3.12, page 69)
  
  //GET (Section specified attribute and filter 3.4.2), POST (Search - Section 3.4.3), PATCH (Path Filter - Section 3.5.2)
  public static final String INVALID_FILTER = "The specified filter syntax was invalid, or the specified attribute and filter comparison combination is not supported.";

  //GET (Section 3.4.2), POST (Search - Section 3.4.3)
  public static final String TOO_MANY = "The specified filter yields many more results than the server is willing to calculate or process.  For example, a filter such as \"(userName pr)\" by itself would return all entries with a \"userName\" and MAY not be acceptable to the service provider.";
     
  //POST (Create - Section 3.3), PUT (Section 3.5.1), PATCH (Section 3.5.2)
  public static final String UNIQUENESS = "One or more of the attribute values are already in use or are reserved.";

  //PUT (Section 3.5.1), PATCH (Section 3.5.2)
  public static final String MUTABILITY = "The attempted modification is not compatible with the target attribute's mutability or current state (e.g., modification of an \"immutable\" attribute with an existing value).";
  
  //POST (Search - Section 3.4.3, Create - Section 3.3, Bulk - Section 3.7), PUT (Section 3.5.1) 
  public static final String INVALID_SYNTAX = "The request body message structure was invalid or did not conform to the request schema.";

  //PATCH (Section 3.5.2) 
  public static final String INVALID_PATH = "The \"path\" attribute was invalid or malformed (see invalid or malformed (see Figure 7).";

  //PATCH (Section 3.5.2)
  public static final String NO_TARGET = "The specified \"path\" did not yield an attribute or attribute value that could be operated on.  This occurs when the specified \"path\" value contains a filter that yields no match.";

  //GET (Section 3.4.2), POST (Create - Section 3.3, Query - Section 3.4.3), PUT (Section 3.5.1), PATCH (Section 3.5.2)
  public static final String INVALID_VALUE = "A required value was missing, or the value specified was not compatible with the operation or attribute type (see Section 2.2 of [RFC7643]), or resource schema (see Section 4 of [RFC7643]).";

  //GET (Section 3.4.2), POST (ALL), PUT (Section 3.5.1), PATCH (Section 3.5.2), DELETE (Section 3.6)
  public static final String INVALID_VERS = "The specified SCIM protocol version is not supported (see Section 3.13).";

  //GET (Section 3.4.2)
  public static final String SENSITIVE = "The specified request cannot be completed, due to the passing of sensitive (e.g., personal) information in a request URI.  For example, personal information SHALL NOT be transmitted over request URIs.  See Section 7.5.2.";
}
