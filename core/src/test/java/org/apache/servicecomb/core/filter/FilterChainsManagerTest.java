/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.servicecomb.core.filter;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.List;

import org.apache.servicecomb.core.filter.config.TransportFiltersConfig;
import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

public class FilterChainsManagerTest {
  @Before
  public void setUp() {
    ArchaiusUtils.resetConfig();
  }

  @AfterClass
  public static void afterAll() {
    ArchaiusUtils.resetConfig();
  }

  private void default_chain(String filters) {
    ArchaiusUtils.setProperty("servicecomb.filter-chains.consumer.default", filters);
  }

  private void microservice_chain(String microservice, String filters) {
    String key = String.format("servicecomb.filter-chains.consumer.policies.%s", microservice);
    ArchaiusUtils.setProperty(key, filters);
  }

  private FilterChainsManager createFilterChains() {
    return new FilterChainsManager()
        .setEnabled(true)
        .setTransportFiltersConfig(new TransportFiltersConfig())
        .setFilterManager(new FilterManager())
        .addProviders(() -> Collections.singletonList(SimpleRetryFilter.class))
        .init(null);
  }

  @Test
  public void should_allow_not_share_filter_instance() {
    default_chain("simple-load-balance");

    FilterChainsManager filterChains = createFilterChains();
    List<Filter> aFilters = filterChains.createConsumerFilters("a");
    List<Filter> bFilters = filterChains.createConsumerFilters("b");

    assertThat(aFilters.get(0)).isNotSameAs(bFilters.get(0));
  }

  @Test
  public void should_allow_share_filter_instance() {
    default_chain("simple-retry");

    FilterChainsManager filterChains = createFilterChains();
    List<Filter> aFilters = filterChains.createConsumerFilters("a");
    List<Filter> bFilters = filterChains.createConsumerFilters("b");

    assertThat(aFilters).hasSameElementsAs(bFilters);
  }

  @Test
  public void should_allow_mix_share_and_not_share_filter_instance() {
    default_chain("simple-load-balance, simple-retry");

    FilterChainsManager filterChains = createFilterChains();
    List<Filter> aFilters = filterChains.createConsumerFilters("a");
    List<Filter> bFilters = filterChains.createConsumerFilters("b");

    assertThat(aFilters.get(0)).isNotSameAs(bFilters.get(0));
    assertThat(aFilters.get(1)).isSameAs(bFilters.get(1));
  }

  @Test
  public void microservice_scope_should_override_default_scope() {
    default_chain("simple-load-balance");
    microservice_chain("a", "simple-retry");

    FilterChainsManager filterChains = createFilterChains();
    List<Filter> filters = filterChains.createConsumerFilters("a");

    assertThat(filters.get(0)).isInstanceOf(SimpleRetryFilter.class);
  }
}
