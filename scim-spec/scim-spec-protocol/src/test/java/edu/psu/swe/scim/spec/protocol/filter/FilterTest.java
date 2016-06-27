package edu.psu.swe.scim.spec.protocol.filter;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.psu.swe.scim.spec.protocol.search.Filter;

@RunWith(JUnitParamsRunner.class)
public class FilterTest extends AbstractLexerParserTest {

  private static final Logger LOG = LoggerFactory.getLogger(FilterTest.class);

  @SuppressWarnings("unused")
  private String[] getAllFilters() {
    return ALL;
  }

  @Test
  @Parameters(method = "getAllFilters")
  public void test(String filterText) throws Exception {
    LOG.info("Running Filter Parser test on input: " + filterText);
    Filter filter = new Filter(filterText);
    FilterExpression expression = filter.getExpression();
    LOG.info("Parsed String: " + expression.toFilter());
    Assert.assertNotNull(expression);
  }
}
