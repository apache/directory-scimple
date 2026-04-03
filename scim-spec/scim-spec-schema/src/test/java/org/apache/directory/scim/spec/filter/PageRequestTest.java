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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PageRequestTest {

  @Test
  void getZeroBasedStartIndex_withStartIndex1_returns0() {
    PageRequest pageRequest = new PageRequest().setStartIndex(1);

    assertThat(pageRequest.getZeroBasedStartIndex()).isEqualTo(0L);
  }

  @Test
  void getZeroBasedStartIndex_withStartIndex5_returns4() {
    PageRequest pageRequest = new PageRequest().setStartIndex(5);

    assertThat(pageRequest.getZeroBasedStartIndex()).isEqualTo(4L);
  }

  @Test
  void getZeroBasedStartIndex_withNullStartIndex_returns0() {
    PageRequest pageRequest = new PageRequest();

    assertThat(pageRequest.getZeroBasedStartIndex()).isEqualTo(0L);
  }

  @Test
  void getZeroBasedStartIndex_withStartIndex0_returns0_clamped() {
    PageRequest pageRequest = new PageRequest().setStartIndex(0);

    assertThat(pageRequest.getZeroBasedStartIndex()).isEqualTo(0L);
  }

  @Test
  void getEffectiveCount_withCount10_andTotalResults50_returns10() {
    PageRequest pageRequest = new PageRequest().setCount(10);

    assertThat(pageRequest.getEffectiveCount(50)).isEqualTo(10L);
  }

  @Test
  void getEffectiveCount_withNullCount_andTotalResults50_returns50() {
    PageRequest pageRequest = new PageRequest();

    assertThat(pageRequest.getEffectiveCount(50)).isEqualTo(50L);
  }

  @Test
  void getEffectiveCount_withCount0_returns0() {
    // RFC 7644 §3.4.2.4: count=0 means "return no resources"
    PageRequest pageRequest = new PageRequest().setCount(0);

    assertThat(pageRequest.getEffectiveCount(50)).isEqualTo(0L);
  }

  @Test
  void getEffectiveCount_withNegativeCount_returns0() {
    // RFC 7644 §3.4.2.4: negative count interpreted as 0
    PageRequest pageRequest = new PageRequest().setCount(-1);

    assertThat(pageRequest.getEffectiveCount(50)).isEqualTo(0L);
  }

  @Test
  void paginate_returnsCorrectPage() {
    List<String> items = List.of("a", "b", "c", "d", "e");
    PageRequest pageRequest = new PageRequest().setStartIndex(2).setCount(2);

    assertThat(pageRequest.paginate(items)).containsExactly("b", "c");
  }

  @Test
  void paginate_withDefaults_returnsAll() {
    List<String> items = List.of("a", "b", "c");
    PageRequest pageRequest = new PageRequest();

    assertThat(pageRequest.paginate(items)).containsExactly("a", "b", "c");
  }

  @Test
  void paginate_withStartBeyondSize_returnsEmpty() {
    List<String> items = List.of("a", "b");
    PageRequest pageRequest = new PageRequest().setStartIndex(10).setCount(5);

    assertThat(pageRequest.paginate(items)).isEmpty();
  }

  @Test
  void paginate_withCountBeyondRemaining_returnsTail() {
    List<String> items = List.of("a", "b", "c", "d", "e");
    PageRequest pageRequest = new PageRequest().setStartIndex(4).setCount(10);

    assertThat(pageRequest.paginate(items)).containsExactly("d", "e");
  }

  @Test
  void paginate_withEmptyList_returnsEmpty() {
    PageRequest pageRequest = new PageRequest().setStartIndex(1).setCount(10);

    assertThat(pageRequest.paginate(List.of())).isEmpty();
  }
}
