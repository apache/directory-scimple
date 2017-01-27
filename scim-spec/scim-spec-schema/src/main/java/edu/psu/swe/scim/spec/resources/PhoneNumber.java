/**
 * 
 */
package edu.psu.swe.scim.spec.resources;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.psu.swe.scim.spec.annotation.ScimAttribute;
import edu.psu.swe.scim.spec.phonenumber.PhoneNumberLexer;
import edu.psu.swe.scim.spec.phonenumber.PhoneNumberParser;
import edu.psu.swe.scim.spec.phonenumber.TreePrintingListener;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;

/**
 * Scim core schema, <a
 * href="https://tools.ietf.org/html/rfc7643#section-4.1.2>section 4.1.2</a>
 *
 */

@XmlType
@XmlAccessorType(XmlAccessType.NONE)
@Data
@EqualsAndHashCode(callSuper = false)
public class PhoneNumber extends KeyedResource implements Serializable {

  private static final long serialVersionUID = 607319505715224096L;

  @Setter(AccessLevel.NONE)
  @XmlElement
  @ScimAttribute(description = "Phone number of the User")
  String value;

  @XmlElement
  @ScimAttribute(description = "A human readable name, primarily used for display purposes. READ-ONLY.")
  String display;

  @XmlElement
  @ScimAttribute(canonicalValueList = { "work", "home", "mobile", "fax", "pager", "other" }, description = "A label indicating the attribute's function; e.g., 'work' or 'home' or 'mobile' etc.")
  String type;

  @XmlElement
  @ScimAttribute(description = "A Boolean value indicating the 'primary' or preferred attribute value for this attribute, e.g. the preferred phone number or primary phone number. The primary attribute value 'true' MUST appear no more than once.")
  Boolean primary = false;

  String rawValue;
  String internationalCode;
  String number;
  String extension;
  String subAddress;
  String phoneContext;
  Map<String, String> params;

  public void setValue(String value) {
    PhoneNumberLexer phoneNumberLexer = new PhoneNumberLexer(new ANTLRInputStream(value));

    List<? extends Token> allTokens = phoneNumberLexer.getAllTokens();
    allTokens.stream()
             .forEach(System.out::println);

    phoneNumberLexer = new PhoneNumberLexer(new ANTLRInputStream(value));

    PhoneNumberParser p = new PhoneNumberParser(new CommonTokenStream(phoneNumberLexer));
    p.setBuildParseTree(true);

    p.addErrorListener(new BaseErrorListener() {
      @Override
      public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
        throw new IllegalStateException("failed to parse at line " + line + " due to " + msg, e);
      }
    });

    // try {
    ParseTree tree = p.phoneNumber();
    TreePrintingListener tpl = new TreePrintingListener();
    ParseTreeWalker.DEFAULT.walk(tpl, tree);
    // } catch (IllegalStateException e) {
    // TODO:remove generic exception for a more specific one
    // throw new Exception("Trouble with phone number value parser");
    // }

    this.value = value;
    this.rawValue = value;

    if (value.startsWith("tel:")) {
      rawValue = value.substring(value.indexOf(':') + 1);
    }

    if (rawValue.startsWith("+")) {
      String tmp = rawValue;
      internationalCode = tmp.replaceAll("[- ()].*", "");
    }

    if (rawValue.contains(";ext=")) {
      extension = rawValue.substring(rawValue.indexOf("=") + 1);
    }
  }

  /*
   * Two "tel" URIs are equivalent according to the following rules:
   * 
   * o Both must be either a 'local-number' or a 'global-number', i.e., start
   * with a '+'. o The 'global-number-digits' and the 'local-number-digits' must
   * be equal, after removing all visual separators. o For mandatory additional
   * parameters (section 5.4) and the 'phone- context' and 'extension'
   * parameters defined in this document, the 'phone-context' parameter value is
   * compared as a host name if it is a 'domainname' or digit by digit if it is
   * 'global-number- digits'. The latter is compared after removing all 'visual-
   * separator' characters. o Parameters are compared according to 'pname',
   * regardless of the order they appeared in the URI. If one URI has a
   * parameter name not found in the other, the two URIs are not equal. o URI
   * comparisons are case-insensitive.
   * 
   * All parameter names and values SHOULD use lower-case characters, as tel
   * URIs may be used within contexts where comparisons are case sensitive.
   */
  
  protected static class PhoneNumberBuilder {
    
    static final Logger LOGGER = LoggerFactory.getLogger(PhoneNumberBuilder.class);

    final String HYPHEN = "-";
    final String INTERNATIONAL_PREFIX = "+";
    final String PREFIX = "tel:%s";
    final String EXTENSTION_PREFIX = ";ext=%s";
    final String ISUB_PREFIX = ";isub=%s";
    final String CONTEXT_PREFIX = ";phone-context=%s";
    final String PARAMS_STRING = ";%s=%s";
    final String LOCAL_SUBSCRIBER_NUMBER_REGEX = "^[\\d\\.\\-\\(\\)]+$";
    final String DOMAIN_NAME_REGEX = "^[a-zA-Z0-9\\.\\-]+$";
    final String GLOBAL_NUMBER_REGEX = "^(\\+)?[\\d\\.\\-\\(\\)]+$";
    final String COUNTRY_CODE_REGEX = "^(\\+)?[1-9][0-9]{0,2}$";
    
    String number;
    String display;
    String extension;
    String subAddress;
    String phoneContext;
    Map<String, String> params;

    void setParam(String name, String value) {
      if (this.params == null) {
        this.params = new HashMap<String, String>();
      }

      if (name != null && !name.isEmpty()) {
        this.params.put(name, value);
      } else {
        throw new IllegalArgumentException("Name cannot be null for a PhoneNumber params property");
      }
    }

    String getFormattedExtension() {
      if (this.extension != null && !this.extension.isEmpty()) {
        return String.format(EXTENSTION_PREFIX, this.extension);
      }

      return null;
    }

    String getFormattedSubAddress() {
      if (this.subAddress != null && !this.subAddress.isEmpty()) {
        return String.format(ISUB_PREFIX, this.subAddress);
      }

      return null;
    }

    String getFormattedPhoneContext() {
      if (this.phoneContext != null && !this.phoneContext.isEmpty()) {
        return String.format(CONTEXT_PREFIX, this.phoneContext);
      }

      return null;
    }

    String getFormattedParams() {
      String paramsFormatted = "";

      if (params != null) {
        for (Map.Entry<String, String> entry : params.entrySet()) {
          paramsFormatted += String.format(PARAMS_STRING, entry.getKey(), entry.getValue());
        }
      }
      
      return !paramsFormatted.isEmpty() ? paramsFormatted : null;
    }

    String getFormattedValue() {
      String valueString = String.format(PREFIX, this.number);

      String fExtension = getFormattedExtension();
      if (fExtension != null) {
        valueString += fExtension;
      }

      String fSubAddr = getFormattedSubAddress();
      if (fSubAddr != null) {
        valueString += fSubAddr;
      }

      String fContext = getFormattedPhoneContext();
      if (fContext != null) {
        valueString += fContext;
      }

      String fParams = getFormattedParams();
      if (fParams != null) {
        valueString += fParams;
      }

      return !valueString.isEmpty() ? valueString : null;
    }

    PhoneNumber build() {
      if (!StringUtils.isBlank(extension) && !StringUtils.isBlank(subAddress)) {
        throw new IllegalArgumentException("PhoneNumberBuilder cannot have a value for both extension and subAddress.");
      }
      
      PhoneNumber phoneNumber = new PhoneNumber();
      
      String formattedValue = getFormattedValue();
      LOGGER.info("" + formattedValue);
      phoneNumber.setValue(formattedValue);

      return phoneNumber;
    }
  }

  public static class LocalPhoneNumberBuilder extends PhoneNumberBuilder {
    String subscriberNumber;
    String countryCode;
    String areaCode;
    String domainName;
    
    public LocalPhoneNumberBuilder(String subscriberNumber, String countryCode, String areaCode) {
      this.subscriberNumber = subscriberNumber;
      this.countryCode = countryCode;
      this.areaCode = areaCode;
    }

    public LocalPhoneNumberBuilder(String subscriberNumber, String domainName) {
      this.subscriberNumber = subscriberNumber;
      this.domainName = domainName;
    }

    public LocalPhoneNumberBuilder extension(String extension) {
      this.extension = extension;
      return this;
    }

    public LocalPhoneNumberBuilder subAddress(String subAddress) {
      this.subAddress = subAddress;
      return this;
    }

    public LocalPhoneNumberBuilder param(String name, String value) {
      super.setParam(name, value);
      return this;
    }
    
    public PhoneNumber build() {
      if (StringUtils.isBlank(subscriberNumber) || !subscriberNumber.matches(LOCAL_SUBSCRIBER_NUMBER_REGEX) ) {
        throw new IllegalArgumentException("LocalPhoneNumberBuilder subscriberNumber must contain only numeric characters and optional ., -, (, ) visual separator characters.");
      }
      
      this.number = subscriberNumber;

      if (StringUtils.isBlank(domainName)) {
        if (StringUtils.isBlank(countryCode) || !countryCode.matches(COUNTRY_CODE_REGEX)) {
          throw new IllegalArgumentException("LocalPhoneNumberBuilder countryCode must contain only numeric characters and an optional plus (+) prefix.");
        }
  
        if (StringUtils.isBlank(areaCode) || !StringUtils.isNumeric(areaCode)) {
          throw new IllegalArgumentException("LocalPhoneNumberBuilder areaCode must contain only numberic characters.");
        }
        
        if (countryCode.startsWith(INTERNATIONAL_PREFIX)) {
          this.phoneContext = countryCode + HYPHEN + areaCode;
        } else {
          this.phoneContext = INTERNATIONAL_PREFIX + countryCode + HYPHEN + areaCode;
        }
        
      } else {
        if (StringUtils.isBlank(domainName) || !domainName.matches(DOMAIN_NAME_REGEX)) {
          throw new IllegalArgumentException("LocalPhoneNumberBuilder domainName must contain only alphanumeric, . and - characters.");
        }
        
        this.phoneContext = domainName;
      }
      
      return super.build();
    }
  }

  public static class GlobalPhoneNumberBuilder extends PhoneNumberBuilder {
    String globalNumber;
    
    public GlobalPhoneNumberBuilder(String globalNumber) {
      this.globalNumber = globalNumber;
    }

    public GlobalPhoneNumberBuilder extension(String extension) {
      this.extension = extension;
      return this;
    }

    public GlobalPhoneNumberBuilder subAddress(String subAddress) {
      this.subAddress = subAddress;
      return this;
    }

    public GlobalPhoneNumberBuilder param(String name, String value) {
      super.setParam(name, value);
      return this;
    }
    
    public PhoneNumber build() {
      if (StringUtils.isBlank(globalNumber) || !globalNumber.matches(GLOBAL_NUMBER_REGEX)) {
        throw new IllegalArgumentException("GlobalPhoneNumberBuilder globalNumber must contain only numeric characters, optional ., -, (, ) visual separators, and an optional plus (+) prefix.");
      }

      if (globalNumber.startsWith(INTERNATIONAL_PREFIX)) {
        this.number = globalNumber;
      } else {
        this.number = INTERNATIONAL_PREFIX + globalNumber;
      }
      
      return super.build();
    }
  }

}
