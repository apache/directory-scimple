package org.apache.directory.scim.server.utility;

import lombok.extern.slf4j.Slf4j;
import org.apache.directory.scim.spec.protocol.ErrorMessageType;
import org.apache.directory.scim.spec.protocol.data.PatchOperation;
import org.apache.directory.scim.spec.protocol.exception.ScimException;
import org.apache.directory.scim.spec.protocol.filter.*;
import org.apache.directory.scim.spec.schema.Schema;

import javax.ws.rs.core.Response;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

@Slf4j
public
class FilterMatchUtil {
  /**
   * @param parent    the {@link Schema.Attribute} representing the top level attribute
   * @param element   the element of the complex attribute representing the child attributes.
   * @param operation the patch operation
   * @return Returns {@code true} if and only if the filter attribute and it are found within the {@code element}
   * @throws ScimException any expression issue or malformed filters
   */
  public static boolean complexAttributeMatch(final Schema.Attribute parent,
                                              final Map<String, Object> element,
                                              final PatchOperation operation)
    throws ScimException {
    Objects.requireNonNull(parent, "parent attribute must not be null.");

    final ValuePathExpression valuePathExpression =
      requireNonNull(
        requireNonNull(operation, "patchOperation must not be null")
          .getPath(), "path must not be null")
        .getValuePathExpression();

    if (valuePathExpression == null) {
      throw PatchUtil.throwScimException(Response.Status.BAD_REQUEST, ErrorMessageType.INVALID_FILTER);
    }

    if (valuePathExpression.getAttributeExpression() == null) {
      throw PatchUtil.throwScimException(Response.Status.BAD_REQUEST, ErrorMessageType.INVALID_FILTER);
    }

    return expressions(parent, element, valuePathExpression.getAttributeExpression());
  }

  static boolean expressions(final Schema.Attribute parent, final Map<String, Object> element, final FilterExpression filterExpression)
    throws ScimException {
    if (filterExpression instanceof AttributeComparisonExpression) {
      return comparisonExpression(
        (AttributeComparisonExpression) filterExpression,
        parent,
        element);
    } else if (filterExpression instanceof AttributePresentExpression) {
      final AttributePresentExpression attributePresentExpression =
        (AttributePresentExpression) filterExpression;

      return presentExpression(attributePresentExpression.getAttributePath().getSubAttributeName(),
        element);
    } else if (filterExpression instanceof LogicalExpression) {
      LogicalExpression logicalExpression = (LogicalExpression) filterExpression;

      switch (logicalExpression.getOperator()) {
        case AND:
          return (expressions(parent, element, logicalExpression.getLeft())) &&
            expressions(parent, element, logicalExpression.getRight());
        case OR:
          return (expressions(parent, element, logicalExpression.getLeft())) ||
            expressions(parent, element, logicalExpression.getRight());
        default:
          throw PatchUtil.throwScimException(Response.Status.BAD_REQUEST, ErrorMessageType.INVALID_FILTER);
      }
    } else if (filterExpression instanceof GroupExpression) {
      final GroupExpression groupExpression = (GroupExpression) filterExpression;
      return expressions(parent, element, groupExpression.getFilterExpression());
    }

    throw new UnsupportedOperationException("valuePathExpression.getAttributeExpression() instance " +
      filterExpression.getClass());
  }

  static boolean comparisonExpression(final AttributeComparisonExpression attributeComparisonExpression,
                                      final Schema.Attribute parent,
                                      final Map<String, Object> element) throws ScimException {
    final String subAttr = attributeComparisonExpression.getAttributePath().getSubAttributeName();
    final CompareOperator compareOperator = attributeComparisonExpression.getOperation();
    final Object compareValue = attributeComparisonExpression.getCompareValue();

    if (!parent.getAttributes().isEmpty() && subAttr!=null) {
      final Schema.Attribute subAttribute = parent.getAttribute(subAttr);
      if (subAttribute!=null) {
        return comparisonExpression(subAttribute, element.get(subAttr),
          compareOperator, compareValue);
      }
      // TODO else ScimException ( check spec )?
    } else {
      return comparisonExpression(parent, element.get(subAttr),
        compareOperator, compareValue);
    }
    // TODO else ScimException ( check spec )?

    return false;
  }

  static boolean presentExpression(final String subAttributeName, final Map<String, Object> element) {
    return element.get(subAttributeName)!=null;
  }

  @SuppressWarnings("unchecked")
  static boolean comparisonExpression(final Schema.Attribute attribute,
                                      final Object value,
                                      final CompareOperator compareOperator,
                                      final Object compareValue) throws ScimException {
    if ((value==null) && (compareValue==null)) {
      return true;
    }

    if ((value!=null) && (compareValue==null)) {
      return false;
    }

    if (value!=null) {
      switch (attribute.getType()) {
        case BINARY:
          return binaryCompare((String) value, (String) compareValue, compareOperator);
        case BOOLEAN:
          return booleanCompare((Boolean) value, (Boolean) compareValue, compareOperator);
        case COMPLEX:
          return complexCompare((Map<String, Object>) value, (Map<String, Object>) compareValue, compareOperator);
        case DATE_TIME:
          return dateTimeCompare((LocalDateTime) value, (LocalDateTime) compareValue, compareOperator);
        case DECIMAL:
          return numberCompare((Double) value, (Double) compareValue, compareOperator);
        case INTEGER:
          return integerCompare((Integer) value, (Integer) compareValue, compareOperator);
        case REFERENCE:
          return referenceCompare((URI) value, (URI) compareValue, compareOperator);
        case STRING:
          return stringCompare((String) value, (String) compareValue, compareOperator,
            attribute.isCaseExact());
      }
    }

    return false;
  }

  /**
   * https://tools.ietf.org/html/rfc7643#page-10
   * <p>
   * 2.3.6.  Binary
   * <p>
   * Arbitrary binary data.  The attribute value MUST be base64 encoded as
   * specified in Section 4 of [RFC4648].  In cases where a URL-safe
   * encoding is required, the attribute definition MAY specify that
   * base64 URL encoding be used as per Section 5 of [RFC4648].  Unless
   * otherwise specified in the attribute definition, trailing padding
   * characters MAY be omitted ("=").
   */
  static boolean binaryCompare(final String value,
                               final String compareValue,
                               final CompareOperator compareOperator) throws ScimException {
    /*
     * EQ/NE are only supported for binary attributes
     */
    switch (compareOperator) {
      case EQ:
        return value.equals(compareValue);
      case NE:
        return !value.equals(compareValue);
      case CO:
      case SW:
      case EW:
      case GT:
      case GE:
      case LT:
      case LE:
        // see https://tools.ietf.org/html/rfc7643#page-16, Table 3
        throw PatchUtil.throwScimException(Response.Status.BAD_REQUEST, ErrorMessageType.INVALID_FILTER);
    }

    return false;
  }

  /**
   * https://tools.ietf.org/html/rfc7643#page-10
   * <p>
   * 2.3.2.  Boolean
   * <p>
   * The literal "true" or "false".  The JSON format is defined in
   * Section 3 of [RFC7159].  A boolean has no case sensitivity or
   * uniqueness.
   */
  static boolean booleanCompare(final Boolean bool,
                                final Boolean compareValue,
                                final CompareOperator compareOperator) throws ScimException {
    /*
     * EQ/NE are only supported for boolean attributes
     */
    switch (compareOperator) {
      case EQ:
        return bool.equals(compareValue);
      case NE:
        return !bool.equals(compareValue);
      case GT:
      case GE:
      case LT:
      case LE:
        // see https://tools.ietf.org/html/rfc7643#page-16, Table 3
        throw PatchUtil.throwScimException(Response.Status.BAD_REQUEST, ErrorMessageType.INVALID_FILTER);
    }

    return false;
  }

  /**
   * https://tools.ietf.org/html/rfc7643#page-10
   * <p>
   * 2.3.8.  Complex
   * <p>
   * A singular or multi-valued attribute whose value is a composition of
   * one or more simple attributes.  The JSON format is defined in
   * Section 4 of [RFC7159].  The order of the component attributes is not
   * significant.  Servers and clients MUST NOT require or expect
   * attributes to be in any specific order when an object is either
   * generated or analyzed.  A complex attribute has no uniqueness or case
   * sensitivity.  A complex attribute MUST NOT contain sub-attributes
   * that have sub-attributes (i.e., that are complex).
   */
  @SuppressWarnings("unused")
  static boolean complexCompare(final Map<String, Object> value,
                                final Map<String, Object> compareValue,
                                final CompareOperator compareOperator) {
    throw new UnsupportedOperationException("Comparison of complex attribute values isn't supported.");
  }

  /**
   * https://tools.ietf.org/html/rfc7643#page-10
   * <p>
   * 2.3.5.  DateTime
   * <p>
   * A DateTime value (e.g., 2008-01-23T04:56:22Z).  The attribute value
   * MUST be encoded as a valid xsd:dateTime as specified in Section 3.3.7
   * of [XML-Schema] and MUST include both a date and a time.  A date time
   * format has no case sensitivity or uniqueness.
   * <p>
   * Values represented in JSON format MUST conform to the XML constraints
   * above and are represented as a JSON string per Section 7 of
   * [RFC7159].
   */
  static boolean dateTimeCompare(final LocalDateTime value,
                                 final LocalDateTime compareValue,
                                 final CompareOperator compareOperator) {
    /*
     * EQ/NE are only supported for string attributes
     */

    switch (compareOperator) {

      case EQ:
        return value.equals(compareValue);
      case NE:
        return !value.equals(compareValue);
      case GT:
      case GE:
      case LT:
      case LE:
        final int compareResult = value.compareTo(compareValue);
        return compareResult(compareOperator, compareResult);
    }

    return false;
  }

  /**
   * https://tools.ietf.org/html/rfc7643#page-10
   * <p>
   * 2.3.4.  Integer
   * <p>
   * A whole number with no fractional digits or decimal.  The JSON format
   * is defined in Section 6 of [RFC7159], with the additional constraint
   * that the value MUST NOT contain fractional or exponent parts.  An
   * integer has no case sensitivity.
   */
  static boolean integerCompare(final Integer integer,
                                final Integer compareValue,
                                final CompareOperator compareOperator) {
    /*
     * CO/SW/EW are only supported for string attributes
     */

    switch (compareOperator) {
      case EQ:
        return integer.equals(compareValue);
      case NE:

        return !integer.equals(compareValue);
      case GT:
      case GE:
      case LT:
      case LE:
        final int compareResult = integer.compareTo(compareValue);

        return compareResult(compareOperator, compareResult);
    }

    return false;
  }

  /**
   * https://tools.ietf.org/html/rfc7643#page-10
   * <p>
   * 2.3.3.  Decimal
   * <p>
   * A real number with at least one digit to the left and right of the
   * period.  The JSON format is defined in Section 6 of [RFC7159].  A
   * decimal has no case sensitivity.
   */
  static boolean numberCompare(final Double value,
                               final Double compareValue,
                               final CompareOperator compareOperator) {
    /*
     * CO/SW/EW are only supported for string attributes
     */

    switch (compareOperator) {
      case EQ:
        return value.equals(compareValue);
      case NE:
        return !value.equals(compareValue);
      case GT:
      case GE:
      case LT:
      case LE:
        final int compareResult = value.compareTo(compareValue);
        return compareResult(compareOperator, compareResult);
    }

    return false;
  }

  /**
   * https://tools.ietf.org/html/rfc7643#page-10
   * <p>
   * 2.3.7.  Reference
   * <p>
   * A URI for a resource.  A resource MAY be a SCIM resource, an external
   * link to a resource (e.g., a photo), or an identifier such as a URN.
   * The value MUST be the absolute or relative URI of the target
   * resource.  Relative URIs should be resolved as specified in
   * Section 5.2 of [RFC3986].  However, the base URI for relative URI
   * resolution MUST include all URI components and path segments up to,
   * but not including, the Endpoint URI (the SCIM service provider root
   * endpoint); e.g., the base URI for a request to
   * "https://example.com/v2/Users/2819c223-7f76-453a-919d-413861904646"
   * would be "https://example.com/v2/", and the relative URI for this
   * resource would be "Users/2819c223-7f76-453a-919d-413861904646".
   * <p>
   * In JSON representation, the URI value is represented as a JSON string
   * per Section 7 of [RFC7159].  A reference is case exact.  A reference
   * has a "referenceTypes" attribute that indicates what types of
   * resources may be linked, as per Section 7 of this document.
   * <p>
   * A reference URI MUST be to an HTTP-addressable resource.  An HTTP
   * client performing a GET operation on a reference URI MUST receive the
   * target resource or an appropriate HTTP response code.  A SCIM service
   * provider MAY choose to enforce referential integrity for reference
   * types referring to SCIM resources.
   * <p>
   * By convention, a reference is commonly represented as a "$ref"
   * sub-attribute in complex or multi-valued attributes; however, this is
   * OPTIONAL.
   */
  static boolean referenceCompare(final URI value,
                                  final URI compareValue,
                                  final CompareOperator compareOperator) {
    /*
     * CO/SW/EW are only supported for string attributes
     */

    switch (compareOperator) {

      case EQ:
        return value.equals(compareValue);
      case NE:
        return !value.equals(compareValue);
      case GT:
      case GE:
      case LT:
      case LE:
        final int compareResult = value.compareTo(compareValue);
        return compareResult(compareOperator, compareResult);
    }
    return false;
  }

  /**
   * https://tools.ietf.org/html/rfc7643#page-10
   * <p>
   * 2.3.1.  String
   * <p>
   * A sequence of zero or more Unicode characters encoded using UTF-8 as
   * per [RFC2277] and [RFC3629].  The JSON format is defined in Section 7
   * of [RFC7159].  An attribute with SCIM schema type "string" MAY
   * specify a required data format.  Additionally, when "canonicalValues"
   * is specified, service providers MAY restrict accepted values to the
   * specified values.
   */
  static boolean stringCompare(final String value,
                               final String compareValue,
                               final CompareOperator compareOperator,
                               final boolean isCaseExact) {
    switch (compareOperator) {
      case EQ:
        return isCaseExact
          ? value.equals(compareValue)
          :value.equalsIgnoreCase(compareValue);
      case NE:
        return isCaseExact
          ? !value.equals(compareValue)
          :!value.equalsIgnoreCase(compareValue);
      case CO:
        return isCaseExact
          ? value.contains(compareValue)
          :value.toLowerCase().contains(compareValue.toLowerCase());
      case SW:
        return isCaseExact
          ? value.startsWith(compareValue)
          :value.toLowerCase().startsWith(compareValue.toLowerCase());
      case EW:
        return isCaseExact
          ? value.endsWith(compareValue)
          :value.toLowerCase().endsWith(compareValue.toLowerCase());
      case GT:
      case GE:
      case LT:
      case LE:
        final int compareResult = isCaseExact
          ? value.compareTo(compareValue)
          :value.compareToIgnoreCase(compareValue);
        return compareResult(compareOperator, compareResult);
    }

    return false;
  }

  static boolean compareResult(final CompareOperator compareOperator, int compareResult) {
    switch (compareOperator) {
      case LT:
        return compareResult < 0;
      case GT:
        return compareResult > 0;
      case LE:
        return compareResult <= 0;
      case GE:
        return compareResult >= 0;
    }

    return false;
  }
}
