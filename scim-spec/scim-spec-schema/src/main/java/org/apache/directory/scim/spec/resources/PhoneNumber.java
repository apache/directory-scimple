/*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements.  See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership.  The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License.  You may obtain a copy of the License at
 
* http://www.apache.org/licenses/LICENSE-2.0

* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.apache.directory.scim.spec.resources;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

import lombok.ToString;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.directory.scim.spec.annotation.ScimAttribute;
import org.apache.directory.scim.spec.phonenumber.PhoneNumberLexer;
import org.apache.directory.scim.spec.phonenumber.PhoneNumberParseException;
import org.apache.directory.scim.spec.phonenumber.PhoneNumberParseTreeListener;
import org.apache.directory.scim.spec.phonenumber.PhoneNumberParser;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * Scim core schema, <a
 * href="https://tools.ietf.org/html/rfc7643#section-4.1.2">section 4.1.2</a>
 *
 */

@XmlType
@XmlAccessorType(XmlAccessType.NONE)
@ToString
public class PhoneNumber implements Serializable, TypedAttribute {

  private static final long serialVersionUID = 607319505715224096L;

  private static final String VISUAL_SEPARATORS = "[\\(\\)\\-\\.]";

  private static final Logger log = LoggerFactory.getLogger(PhoneNumber.class);

  @ScimAttribute(description = "Phone number of the User")
  String value;

  @XmlElement
  @ScimAttribute(description = "A human readable name, primarily used for display purposes. READ-ONLY.")
  @Getter
  @Setter
  String display;

  @XmlElement
  @ScimAttribute(canonicalValueList = { "work", "home", "mobile", "fax", "pager", "other" }, description = "A label indicating the attribute's function; e.g., 'work' or 'home' or 'mobile' etc.")
  @Getter
  @Setter
  String type;

  @XmlElement
  @ScimAttribute(description = "A Boolean value indicating the 'primary' or preferred attribute value for this attribute, e.g. the preferred phone number or primary phone number. The primary attribute value 'true' MUST appear no more than once.")
  @Getter
  @Setter
  Boolean primary = false;

  @Setter(AccessLevel.NONE)
  @Getter
  boolean isGlobalNumber = false;

  @Getter
  @Setter(AccessLevel.NONE)
  String number;

  @Setter(AccessLevel.NONE)
  @Getter
  String extension;

  @Setter(AccessLevel.NONE)
  @Getter
  String subAddress;

  @Setter(AccessLevel.NONE)
  @Getter
  String phoneContext;

  @Setter(AccessLevel.NONE)
  @Getter
  boolean isDomainPhoneContext = false;

  @Getter
  @Setter(AccessLevel.NONE)
  Map<String, String> params;

  public void addParam(String name, String value) {
    if (this.params == null) {
      this.params = new LinkedHashMap<>();
    }

    this.params.put(name, value);
  }

  // This is annotated here to ensure that JAXB uses the setter rather than
  // reflection
  // to assigned the value. Do not move the XmlElement annotation to the field
  // please.
  @XmlElement
  public String getValue() {
    return value;
  }

  public PhoneNumber setValue(String value) throws PhoneNumberParseException {
    return this.setValue(value, false);
  }

  public PhoneNumber setValue(String value, boolean strict) throws PhoneNumberParseException {
    if (value == null) {
      throw new PhoneNumberParseException("null values are illegal for phone numbers");
    }

      PhoneNumberLexer phoneNumberLexer = new PhoneNumberLexer(new ANTLRInputStream(value));
      PhoneNumberParser p = new PhoneNumberParser(new CommonTokenStream(phoneNumberLexer));
      p.setBuildParseTree(true);
      p.addErrorListener(new PhoneNumberErrorListener());
  
      PhoneNumberParseTreeListener tpl = new PhoneNumberParseTreeListener();
      try {
        ParseTree tree = p.phoneNumber();
        ParseTreeWalker.DEFAULT.walk(tpl, tree);
        PhoneNumber parsedPhoneNumber = tpl.getPhoneNumber();

        this.value = parsedPhoneNumber.getValue();
        this.number = parsedPhoneNumber.getNumber();
        this.extension = parsedPhoneNumber.getExtension();
        this.subAddress = parsedPhoneNumber.getSubAddress();
        this.phoneContext = parsedPhoneNumber.getPhoneContext();
        this.params = parsedPhoneNumber.getParams();
        this.isGlobalNumber = parsedPhoneNumber.isGlobalNumber();
        this.isDomainPhoneContext = parsedPhoneNumber.isDomainPhoneContext();
      } catch (IllegalStateException e) {
        // SCIM Core RFC section 4.1.2 states phone numbers SHOULD be formatted per RFC3966, e.g. 'tel:+1-201-555-0123'
        // but this is not required, if exception is thrown while parsing, fall back to original value, unless `strict`
        if (strict) {
          throw new PhoneNumberParseException(e);
        }
        log.debug("Failed to parse phone number '{}'", value, e);
        this.value = value;
      }
      return this;
  }

  /*
   * Implements RFC 3996 URI Equality for the value property
   * https://tools.ietf.org/html/rfc3966#section-3
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    PhoneNumber other = (PhoneNumber) obj;

    if (isGlobalNumber != other.isGlobalNumber)
      return false;

    String numberWithoutVisualSeparators = number != null ? number.replaceAll(VISUAL_SEPARATORS, "") : null;
    String otherNumberWithoutVisualSeparators = other.number != null ? other.number.replaceAll(VISUAL_SEPARATORS, "") : null;
    if (numberWithoutVisualSeparators == null) {
      if (otherNumberWithoutVisualSeparators != null)
        return false;
    } else if (!numberWithoutVisualSeparators.equals(otherNumberWithoutVisualSeparators))
      return false;

    String extensionWithoutVisualSeparators = extension != null ? extension.replaceAll(VISUAL_SEPARATORS, "") : null;
    String otherExtensionWithoutVisualSeparators = other.extension != null ? other.extension.replaceAll(VISUAL_SEPARATORS, "") : null;
    if (extensionWithoutVisualSeparators == null) {
      if (otherExtensionWithoutVisualSeparators != null)
        return false;
    } else if (!extensionWithoutVisualSeparators.equals(otherExtensionWithoutVisualSeparators))
      return false;

    if (subAddress == null) {
      if (other.subAddress != null)
        return false;
    } else if (!equalsIgnoreCase(subAddress, other.subAddress))
      return false;

    String phoneContextTemp = phoneContext;
    if (!StringUtils.isBlank(phoneContext) && !isDomainPhoneContext) {
      phoneContextTemp = phoneContext.replaceAll(VISUAL_SEPARATORS, "");
    }

    String otherPhoneContextTemp = other.phoneContext;
    if (!StringUtils.isBlank(other.phoneContext) && !other.isDomainPhoneContext) {
      otherPhoneContextTemp = other.phoneContext.replaceAll(VISUAL_SEPARATORS, "");
    }

    if (phoneContextTemp == null) {
      if (otherPhoneContextTemp != null)
        return false;
    } else if (!equalsIgnoreCase(phoneContextTemp, otherPhoneContextTemp))
      return false;

    if (!equalsIgnoreCaseAndOrderParams(other.params)) {
      return false;
    }

    if (primary == null) {
      if (other.primary != null)
        return false;
    } else if (!primary.equals(other.primary))
      return false;

    if (type == null) {
        return other.type == null;
    } else return equalsIgnoreCase(type, other.type);
  }

  /*
   * Implements RFC 3996 URI Equality for the value property
   * https://tools.ietf.org/html/rfc3966#section-3
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (isGlobalNumber ? 1231 : 1237);
    result = prime * result + ((number == null) ? 0 : number.replaceAll(VISUAL_SEPARATORS, "").hashCode());
    result = prime * result + ((extension == null) ? 0 : extension.replaceAll(VISUAL_SEPARATORS, "").hashCode());
    result = prime * result + ((subAddress == null) ? 0 : subAddress.toLowerCase(Locale.ROOT).hashCode());
    result = prime * result + ((phoneContext == null) ? 0 : (isDomainPhoneContext ? phoneContext.toLowerCase(Locale.ROOT).hashCode() : phoneContext.replaceAll(VISUAL_SEPARATORS, "").hashCode()));
    result = prime * result + ((params == null) ? 0 : paramsToLowerCase().hashCode());
    result = prime * result + ((primary == null) ? 0 : primary.hashCode());
    result = prime * result + ((type == null) ? 0 : type.toLowerCase(Locale.ROOT).hashCode());
    return result;
  }

  Map<String, String> paramsToLowerCase() {
    Map<String, String> paramsLowercase = new LinkedHashMap<>();
    for (Entry<String, String> entry : params.entrySet()) {
      paramsLowercase.put(entry.getKey().toLowerCase(Locale.ROOT), entry.getValue().toLowerCase(Locale.ROOT));
    }

    return paramsLowercase;
  }

  private static boolean equalsIgnoreCase(String a, String b) {
    if (a == b) {
      return true;
    }
    if (a == null || b == null) {
      return false;
    }
    return a.toLowerCase(Locale.ROOT).equals(b.toLowerCase(Locale.ROOT));
  }

  boolean equalsIgnoreCaseAndOrderParams(Map<String, String> otherParams) {
    if (params == null && otherParams == null) {
      return true;
    }

    if ((params == null && otherParams != null) || (params != null && otherParams == null) || (params.size() != otherParams.size())) {
      return false;
    }

    Map<String, String> paramsLowercase = paramsToLowerCase();

    for (Entry<String, String> entry : otherParams.entrySet()) {
      String foundValue = paramsLowercase.get(entry.getKey().toLowerCase(Locale.ROOT));

      if (!entry.getValue().toLowerCase(Locale.ROOT).equals(foundValue)) {
        return false;
      }
    }

    return true;
  }

  private static class PhoneNumberErrorListener extends BaseErrorListener {
    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
      throw new IllegalStateException("failed to parse at line " + line + " due to " + msg, e);
    }
  }

  @Data
  public abstract static class PhoneNumberBuilder {

    static final Logger LOGGER = LoggerFactory.getLogger(PhoneNumberBuilder.class);

    static final String HYPHEN = "-";
    static final String INTERNATIONAL_PREFIX = "+";
    static final String PREFIX = "tel:%s";
    static final String EXTENSTION_PREFIX = ";ext=%s";
    static final String ISUB_PREFIX = ";isub=%s";
    static final String CONTEXT_PREFIX = ";phone-context=%s";
    static final String PARAMS_STRING = ";%s=%s";
    static final String LOCAL_SUBSCRIBER_NUMBER_REGEX = "^[\\d\\.\\-\\(\\)]+$";
    static final String DOMAIN_NAME_REGEX = "^[a-zA-Z0-9\\.\\-]+$";
    static final String GLOBAL_NUMBER_REGEX = "^(\\+)?[\\d\\.\\-\\(\\)]+$";
    static final String COUNTRY_CODE_REGEX = "^(\\+)?[1-9][0-9]{0,2}$";

    String number;
    String display;
    String extension;
    String subAddress;
    String phoneContext;
    LinkedHashMap<String, String> params;

    boolean isGlobalNumber = false;
    boolean isDomainPhoneContext = false;

    public PhoneNumberBuilder display(String display) {
      this.display = display;
      return this;
    }

    public PhoneNumberBuilder extension(String extension) {
      this.extension = extension;
      return this;
    }

    public PhoneNumberBuilder subAddress(String subAddress) {
      this.subAddress = subAddress;
      return this;
    }

    public PhoneNumberBuilder phoneContext(String phoneContext) {
      this.phoneContext = phoneContext;
      return this;
    }

    public PhoneNumberBuilder setParams(Map<String, String> params) {
      this.params = params != null ? new LinkedHashMap<>(params) : null;
      return this;
    }

    public PhoneNumberBuilder param(String name, String value) {
      if (this.params == null) {
        this.params = new LinkedHashMap<>();
      }

      this.params.put(name, value);
      return this;
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
        paramsFormatted = params.entrySet().stream()
          .map(entry -> String.format(PARAMS_STRING, entry.getKey(), entry.getValue() != null ? entry.getValue() : ""))
          .collect(Collectors.joining());
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

    public PhoneNumber build() throws PhoneNumberParseException {
      return build(true);
    }

    public PhoneNumber build(boolean validate) throws PhoneNumberParseException {
      if (!StringUtils.isBlank(extension) && !StringUtils.isBlank(subAddress)) {
        throw new IllegalArgumentException("PhoneNumberBuilder cannot have a value for both extension and subAddress.");
      }

      if (extension != null && !extension.matches(LOCAL_SUBSCRIBER_NUMBER_REGEX)) {
        throw new IllegalArgumentException("PhoneNumberBuilder extension must contain only numeric characters and optional ., -, (, ) visual separator characters.");
      }

      if (params != null && !params.isEmpty()) {
        if (params.get("") != null || params.get(null) != null || params.containsValue(null) || params.containsValue("")) { //NOPMD - suppressed CollapsibleIfStatements
          throw new IllegalArgumentException("PhoneNumberBuilder params names and values cannot be null or empty.");
        }
      }

      PhoneNumber phoneNumber = new PhoneNumber();
      String formattedValue = getFormattedValue();
      LOGGER.debug("Building phone number: '{}'", formattedValue);

      if (validate) {
        phoneNumber.setValue(formattedValue, true);
      } else {
        phoneNumber.value = formattedValue;
        phoneNumber.extension = this.extension;
        phoneNumber.isDomainPhoneContext = this.isDomainPhoneContext;
        phoneNumber.isGlobalNumber = this.isGlobalNumber;
        phoneNumber.number = this.number;
        phoneNumber.params = this.params;
        phoneNumber.phoneContext = this.phoneContext;
        phoneNumber.subAddress = this.subAddress;
      }
      return phoneNumber;
    }
  }

  public static class LocalPhoneNumberBuilder extends PhoneNumberBuilder {
    String subscriberNumber;
    String countryCode;
    String areaCode;
    String domainName;

    public LocalPhoneNumberBuilder subscriberNumber(String subscriberNumber) {
      this.subscriberNumber = subscriberNumber;
      this.number = subscriberNumber;
      return this;
    }

    public LocalPhoneNumberBuilder countryCode(String countryCode) {

      String localCode = countryCode;

      if (localCode != null && !localCode.isEmpty()) {
        localCode = localCode.trim();
        if (localCode.length() > 0 && localCode.charAt(0) != '+') {
          localCode = '+' + localCode;
        }
      }
      this.countryCode = localCode;
      return this;
    }

    public LocalPhoneNumberBuilder areaCode(String areaCode) {
      this.areaCode = areaCode;
      return this;
    }

    public LocalPhoneNumberBuilder domainName(String domainName) {
      this.domainName = domainName;
      return this;
    }

    public LocalPhoneNumberBuilder isDomainPhoneContext(boolean hasDomainPhoneContext) {
      this.isDomainPhoneContext = hasDomainPhoneContext;
      return this;
    }

    @Override
    public PhoneNumber build() throws PhoneNumberParseException {
      if (StringUtils.isBlank(subscriberNumber) || !subscriberNumber.matches(LOCAL_SUBSCRIBER_NUMBER_REGEX)) {
        throw new IllegalArgumentException("LocalPhoneNumberBuilder subscriberNumber must contain only numeric characters and optional ., -, (, ) visual separator characters.");
      }

      if (StringUtils.isBlank(countryCode) && StringUtils.isBlank(domainName)) {
        throw new IllegalArgumentException("LocalPhoneNumberBuilder must have values for domainName or countryCode.");
      }

      if (StringUtils.isBlank(domainName)) {
        if (StringUtils.isBlank(countryCode) || !countryCode.matches(COUNTRY_CODE_REGEX)) {
          throw new IllegalArgumentException("LocalPhoneNumberBuilder countryCode must contain only numeric characters and an optional plus (+) prefix.");
        }

        if (areaCode != null && !StringUtils.isNumeric(areaCode)) {
          throw new IllegalArgumentException("LocalPhoneNumberBuilder areaCode must contain only numberic characters.");
        }

        if (!countryCode.startsWith(INTERNATIONAL_PREFIX)) {
          this.phoneContext = INTERNATIONAL_PREFIX + countryCode;
        } else {
          this.phoneContext = countryCode;
        }

        if (!StringUtils.isBlank(areaCode)) {
          this.phoneContext += (HYPHEN + areaCode);
        }

      } else {
        if (!domainName.matches(DOMAIN_NAME_REGEX)) {
          throw new IllegalArgumentException("LocalPhoneNumberBuilder domainName must contain only alphanumeric, . and - characters.");
        }

        this.phoneContext = domainName;
      }

      return super.build();
    }
  }

  public static class GlobalPhoneNumberBuilder extends PhoneNumberBuilder {
    String globalNumber;

    public GlobalPhoneNumberBuilder() {
      this.isGlobalNumber = true;
    }

    public GlobalPhoneNumberBuilder globalNumber(String globalNumber) {
      this.globalNumber = globalNumber;
     
      if (globalNumber != null) { 
        if (globalNumber.startsWith(INTERNATIONAL_PREFIX)) {
          this.number = globalNumber;
        } else {
          this.number = INTERNATIONAL_PREFIX + globalNumber;
        }
      }
      
      return this;
    }

    @Override
    public PhoneNumber build() throws PhoneNumberParseException {
      if (StringUtils.isBlank(globalNumber) || !globalNumber.matches(GLOBAL_NUMBER_REGEX)) {
        throw new IllegalArgumentException("GlobalPhoneNumberBuilder globalNumber must contain only numeric characters, optional ., -, (, ) visual separators, and an optional plus (+) prefix.");
      }

      return super.build();
    }
  }

}
