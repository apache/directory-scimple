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

package org.apache.directory.scim.spec.filter;

import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class FilterResponseTest {

  @Test
  void paginate_defaultPageRequest_returnsAll() {
    FilterResponse<String> response = Stream.of("a", "b", "c", "d", "e")
      .collect(FilterResponse.paginate(new PageRequest()));

    assertThat(response.getResources()).containsExactly("a", "b", "c", "d", "e");
    assertThat(response.getTotalResults()).isEqualTo(5);
  }

  @Test
  void paginate_specificPage_returnsSlice() {
    FilterResponse<String> response = Stream.of("a", "b", "c", "d", "e")
      .collect(FilterResponse.paginate(new PageRequest().setStartIndex(2).setCount(2)));

    assertThat(response.getResources()).containsExactly("b", "c");
    assertThat(response.getTotalResults()).isEqualTo(5);
  }

  @Test
  void paginate_startIndexBeyondResults_returnsEmpty() {
    FilterResponse<String> response = Stream.of("a", "b", "c")
      .collect(FilterResponse.paginate(new PageRequest().setStartIndex(10).setCount(2)));

    assertThat(response.getResources()).isEmpty();
    assertThat(response.getTotalResults()).isEqualTo(3);
  }

  @Test
  void paginate_countZero_returnsEmptyWithTotalResults() {
    FilterResponse<String> response = Stream.of("a", "b", "c")
      .collect(FilterResponse.paginate(new PageRequest().setCount(0)));

    assertThat(response.getResources()).isEmpty();
    assertThat(response.getTotalResults()).isEqualTo(3);
  }

  @Test
  void paginate_emptyStream_returnsEmptyResponse() {
    FilterResponse<String> response = Stream.<String>empty()
      .collect(FilterResponse.paginate(new PageRequest()));

    assertThat(response.getResources()).isEmpty();
    assertThat(response.getTotalResults()).isEqualTo(0);
  }

  @Test
  void paginate_parallelStream_correctResults() {
    FilterResponse<Integer> response = Stream.iterate(1, i -> i + 1)
      .limit(100)
      .parallel()
      .collect(FilterResponse.paginate(new PageRequest().setStartIndex(1).setCount(10)));

    assertThat(response.getResources()).containsExactly(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    assertThat(response.getTotalResults()).isEqualTo(100);
  }

  @Test
  void paginate_countExceedsRemaining_returnsAvailable() {
    FilterResponse<String> response = Stream.of("a", "b", "c")
      .collect(FilterResponse.paginate(new PageRequest().setStartIndex(2).setCount(10)));

    assertThat(response.getResources()).containsExactly("b", "c");
    assertThat(response.getTotalResults()).isEqualTo(3);
  }
}
