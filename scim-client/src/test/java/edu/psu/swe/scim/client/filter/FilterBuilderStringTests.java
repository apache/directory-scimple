package edu.psu.swe.scim.client.filter;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.junit.Before;
import org.junit.Test;

import edu.psu.swe.scim.spec.protocol.filter.FilterParseException;
import edu.psu.swe.scim.spec.protocol.search.Filter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FilterBuilderStringTests {

  FilterBuilder filterBuilder;

  @Before
  public void init() {
    filterBuilder = new FilterBuilder();
  }

  @Test
  public void testEndsWith() throws UnsupportedEncodingException, FilterParseException {
    String encoded = filterBuilder.endsWith("address.streetAddress", "Way").build();
    Filter filter = new Filter(decode(encoded));
  }

  @Test
  public void testStartsWith()  throws UnsupportedEncodingException, FilterParseException {
    String encoded = filterBuilder.startsWith("address.streetAddress", "133").build();
    Filter filter = new Filter(decode(encoded));
  }

  @Test
  public void testContains()  throws UnsupportedEncodingException, FilterParseException {
    String encoded = filterBuilder.contains("address.streetAddress", "MacDuff").build();
    Filter filter = new Filter(decode(encoded));
  }

  private String decode(String encoded) throws UnsupportedEncodingException {

    log.info(encoded);
    
    String decoded = URLDecoder.decode(encoded, "UTF-8").replace("%20", " ");
    
    log.info(decoded);
    
    return decoded;
  }
}
