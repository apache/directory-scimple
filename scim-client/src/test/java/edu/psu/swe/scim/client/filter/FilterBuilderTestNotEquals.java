package edu.psu.swe.scim.client.filter;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import edu.psu.swe.scim.spec.protocol.filter.FilterParseException;
import edu.psu.swe.scim.spec.protocol.search.Filter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FilterBuilderTestNotEquals {
  
  FilterBuilder filterBuilder;
  
  @Before
  public void init() {
    filterBuilder = new FilterBuilder();
  }

  @Test
  public void testNotnotEqualStringString() throws UnsupportedEncodingException, FilterParseException {
    String encoded = filterBuilder.notEqual("address.streetAddress", "7714 Sassafrass Way").build();
    Filter filter = new Filter(decode(encoded));
  }

  @Test
  public void testNotnotEqualStringBoolean() throws UnsupportedEncodingException, FilterParseException {
    String encoded = filterBuilder.notEqual("address.active", true).build();
    Filter filter = new Filter(decode(encoded));
  }

  @Test
  public void testNotnotEqualStringDate() throws UnsupportedEncodingException, FilterParseException {
    String encoded = filterBuilder.notEqual("date.date", new Date()).build();
    Filter filter = new Filter(decode(encoded));
  }

  @Test
  public void testNotnotEqualStringLocalDate() throws UnsupportedEncodingException, FilterParseException {
    String encoded = filterBuilder.notEqual("date.date", LocalDate.now()).build();
    Filter filter = new Filter(decode(encoded));
  }

  @Test
  public void testNotnotEqualStringLocalDateTime() throws UnsupportedEncodingException, FilterParseException {
    String encoded = filterBuilder.notEqual("date.date", LocalDateTime.now()).build();
    Filter filter = new Filter(decode(encoded));
  }

  @Test
  public void testNotnotEqualStringInteger() throws UnsupportedEncodingException, FilterParseException {
    int i = 10;
    String encoded = filterBuilder.notEqual("int.int", i).build();
    Filter filter = new Filter(decode(encoded));
  }

  @Test
  public void testNotnotEqualStringLong() throws UnsupportedEncodingException, FilterParseException {
    long i = 10l;
    String encoded = filterBuilder.notEqual("long.long", i).build();
    Filter filter = new Filter(decode(encoded));
  }

  @Test
  public void testNotnotEqualStringFloat() throws UnsupportedEncodingException, FilterParseException {
    float i = 10.2f;
    String encoded = filterBuilder.notEqual("long.long", i).build();
    Filter filter = new Filter(decode(encoded));
  }

  @Test
  public void testNotnotEqualStringDouble() throws UnsupportedEncodingException, FilterParseException {
    double i = 10.2;
    String encoded = filterBuilder.notEqual("long.long", i).build();
    Filter filter = new Filter(decode(encoded));
  }

  @Test
  public void testNotEqualNull() throws UnsupportedEncodingException, FilterParseException {
    String encoded = filterBuilder.equalNull("null.null").build();
    Filter filter = new Filter(decode(encoded));
  }
  
  private String decode(String encoded) throws UnsupportedEncodingException {

    log.info(encoded);
    
    String decoded = URLDecoder.decode(encoded, "UTF-8").replace("%20", " ");
    
    log.info(decoded);
    
    return decoded;
  }
}
