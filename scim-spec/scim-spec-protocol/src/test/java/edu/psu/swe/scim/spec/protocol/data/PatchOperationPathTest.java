package edu.psu.swe.scim.spec.protocol.data;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RunWith(JUnitParamsRunner.class)
public class PatchOperationPathTest {

  public String[] pathValues() {
    return new String[] { "members",
        "name.familyName",
        "addresses[type eq \"work\"]",
        "members[value eq \"2819c223-7f76-453a-919d-413861904646\"]",
        "members[value eq \"2819c223-7f76-453a-919d-413861904646\"].displayName" };
  }

  @Test
  @Parameters(method = "pathValues")
  public void testPathParsing(String value) throws Exception {
    PatchOperationPath path = new PatchOperationPath(value);
    log.info("ValuePathExpression: " + path.getValuePathExpression());
    
    String result = path.toString();
    log.info(result);
    Assert.assertNotNull(path.getValuePathExpression());
    Assert.assertEquals(value.toLowerCase(), result.toLowerCase());
  }

}
