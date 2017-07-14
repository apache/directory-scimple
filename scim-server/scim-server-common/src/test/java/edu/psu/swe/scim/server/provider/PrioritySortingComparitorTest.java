package edu.psu.swe.scim.server.provider;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.assertj.core.api.Assertions;
import org.junit.Test;

public class PrioritySortingComparitorTest {

  @Test
  public void testSorting() throws Exception {
    Set<Object> priorities = new HashSet<>();
    priorities.add("1P");
    priorities.add("2P");
    PrioritySortingComparitor comparitor = new PrioritySortingComparitor(priorities);
    List<String> list = Arrays.asList("1", "2", "1P", "2P", "3", "4");
    Collections.sort(list, comparitor);
    System.out.println(list);
    
    Assertions.assertThat(list).hasSameElementsAs(Arrays.asList("1P", "2P", "1", "2", "3", "4"));
  }
  
}
