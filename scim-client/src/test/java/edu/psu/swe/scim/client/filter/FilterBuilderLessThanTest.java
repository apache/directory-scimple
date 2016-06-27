package edu.psu.swe.scim.client.filter;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import edu.psu.swe.scim.spec.protocol.filter.FilterParseException;
import edu.psu.swe.scim.spec.protocol.search.Filter;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RunWith(JUnitParamsRunner.class)
public class FilterBuilderLessThanTest {

  static final Integer[] INT_EXAMPLES = { 1, 12, 123, 1234, 12345, 123456 };
  static final Long[] LONG_EXAMPLES = { 3L, 33L, 333L, 3333L, 33333L, 333333L };
  static final Float [] FLOAT_EXAMPLES = {.14f, 3.14f, 2.1415f, 3.14E+10f, 333.14f};
  static final Double [] DOUBLE_EXAMPLES = {.14, 3.14, 2.1415, 3.14E+10, 333.14};
 
  Integer [] getIntExamples() {
    return INT_EXAMPLES;
  }
  
  Long [] getLongExamples() {
    return LONG_EXAMPLES;
  }
  
  Float [] getFloatExamples() {
    return FLOAT_EXAMPLES;
  }
  
  Double [] getDoubleExamples() {
    return DOUBLE_EXAMPLES;
  }
  
  FilterClient filterBuilder;

  @Before
  public void init() {
    filterBuilder = new FilterClient();
  }
  
  @Test
  @Parameters(method="getIntExamples")
  public void testLessThanT_Int(Integer arg) throws UnsupportedEncodingException, FilterParseException {
    
    String encoded = filterBuilder.lessThan("dog.weight", arg).build();
    Filter filter = new Filter(decode(encoded));
  }

  @Test
  @Parameters(method="getLongExamples")
  public void testLessThanT_Long(Long arg) throws UnsupportedEncodingException, FilterParseException {
    
    String encoded = filterBuilder.lessThan("dog.weight", arg).build();
    Filter filter = new Filter(decode(encoded));
  }
  
  @Test
  @Parameters(method="getFloatExamples")
  public void testLessThanT_Float(Float arg) throws UnsupportedEncodingException, FilterParseException {
    
    String encoded = filterBuilder.lessThan("dog.weight", arg).build();
    Filter filter = new Filter(decode(encoded));
  }
  
  @Test
  @Parameters(method="getDoubleExamples")
  public void testLessThanT_Double(Double arg) throws UnsupportedEncodingException, FilterParseException {
    
    String encoded = filterBuilder.lessThan("dog.weight", arg).build();
    Filter filter = new Filter(decode(encoded));
  }
  
  
  @Test
  public void testLessThanDate() throws UnsupportedEncodingException, FilterParseException {
    String encoded = filterBuilder.lessThan("dog.dob", new Date()).build();
    Filter filter = new Filter(decode(encoded));
  }

  @Test
  public void testLessThanLocalDate() throws UnsupportedEncodingException, FilterParseException {
    String encoded = filterBuilder.lessThan("dog.dob", LocalDate.now()).build();
    Filter filter = new Filter(decode(encoded));
  }

  @Test
  public void testLessThanLocalDateTime() throws UnsupportedEncodingException, FilterParseException  {
    String encoded = filterBuilder.lessThan("dog.dob", LocalDateTime.now()).build();
    Filter filter = new Filter(decode(encoded));
  }

  @Test
  @Parameters(method="getIntExamples")
  public void testLessThanOrEqualsT_Int(Integer arg) throws UnsupportedEncodingException, FilterParseException {
    
    String encoded = filterBuilder.lessThanOrEquals("dog.weight", arg).build();
    Filter filter = new Filter(decode(encoded));
  }

  @Test
  @Parameters(method="getLongExamples")
  public void testLessThanOrEqualsT_Long(Long arg) throws UnsupportedEncodingException, FilterParseException {
    
    String encoded = filterBuilder.lessThanOrEquals("dog.weight", arg).build();
    Filter filter = new Filter(decode(encoded));
  }
  
  @Test
  @Parameters(method="getFloatExamples")
  public void testLessThanOrEqualsT_Float(Float arg) throws UnsupportedEncodingException, FilterParseException {
    
    String encoded = filterBuilder.lessThanOrEquals("dog.weight", arg).build();
    Filter filter = new Filter(decode(encoded));
  }
  
  @Test
  @Parameters(method="getDoubleExamples")
  public void testLessThanOrEqualsT_Double(Double arg) throws UnsupportedEncodingException, FilterParseException {
    
    String encoded = filterBuilder.lessThanOrEquals("dog.weight", arg).build();
    Filter filter = new Filter(decode(encoded));
  }
  
  @Test
  public void testLessThanOrEqualsDate() throws UnsupportedEncodingException, FilterParseException {
    String encoded = filterBuilder.lessThanOrEquals("dog.dob", new Date()).build();
    Filter filter = new Filter(decode(encoded));
  }

  @Test
  public void testLessThanOrEqualsLocalDate()  throws UnsupportedEncodingException, FilterParseException {
    String encoded = filterBuilder.lessThanOrEquals("dog.dob", LocalDate.now()).build();
    Filter filter = new Filter(decode(encoded));
  }

  @Test
  public void testLessThanOrEqualsLocalDateTime() throws UnsupportedEncodingException, FilterParseException {
    String encoded = filterBuilder.lessThanOrEquals("dog.dob", LocalDateTime.now()).build();
    Filter filter = new Filter(decode(encoded));
  }

  private String decode(String encoded) throws UnsupportedEncodingException {

    log.info(encoded);
    
    String decoded = URLDecoder.decode(encoded, "UTF-8").replace("%20", " ");
    
    log.info(decoded);
    
    return decoded;
  }
}
