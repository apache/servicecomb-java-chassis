/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.serviceregistry.filter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

import io.servicecomb.foundation.common.cache.VersionedCache;
import io.servicecomb.serviceregistry.api.registry.MicroserviceInstance;

public class TestAbstractTransportDiscoveryFilter {
  class AbstractTransportDiscoveryFilterForTest extends AbstractTransportDiscoveryFilter {
    @Override
    public int getOrder() {
      return 0;
    }

    @Override
    protected String getTransportName(DiscoveryFilterContext context) {
      return transportName;
    }

    @Override
    protected Object createEndpoint(String transportName, String endpoint, MicroserviceInstance instance) {
      if (disableCreate) {
        return null;
      }

      return endpoint;
    }
  }

  boolean disableCreate;

  String transportName;

  AbstractTransportDiscoveryFilterForTest filter = new AbstractTransportDiscoveryFilterForTest();

  DiscoveryFilterContext context = new DiscoveryFilterContext();

  VersionedCache newCache;

  @Test
  public void isGroupingFilter() {
    Assert.assertTrue(filter.isGroupingFilter());
  }

  @Test
  public void doFilterTransportNotExist() {
    transportName = "notExist";
    newCache = new VersionedCache();
    VersionedCache result = filter.doFilter(context, newCache);

    Assert.assertSame(filter.empty, result);
  }

  private MicroserviceInstance createInstance(String... schemas) {
    String id = UUID.randomUUID().toString();
    MicroserviceInstance instance = new MicroserviceInstance();
    instance.setInstanceId(id);
    for (int idx = 0; idx < schemas.length; idx++) {
      String schema = schemas[idx];
      instance.getEndpoints().add(String.format("%s://%s:%d", schema, id, 8080 + idx));
    }
    return instance;
  }

  private VersionedCache createMicroserviceInstancesCache(String name, MicroserviceInstance... instances) {
    Map<String, MicroserviceInstance> map = new LinkedHashMap<>();
    for (MicroserviceInstance instance : instances) {
      map.put(instance.getInstanceId(), instance);
    }
    return new VersionedCache().autoCacheVersion().name(name).data(map);
  }

  @Test
  public void doFilterTransportExist() {
    MicroserviceInstance instance1 = createInstance("a", "b");
    MicroserviceInstance instance2 = createInstance("b");
    newCache = createMicroserviceInstancesCache("name", instance1, instance2);

    transportName = "a";
    filter.onChanged(context, newCache);
    VersionedCache result = filter.doFilter(context, newCache);

    Assert.assertEquals(newCache.cacheVersion(), result.cacheVersion());
    Assert.assertThat(result.collectionData(), Matchers.contains(instance1.getEndpoints().get(0)));
  }

  @Test
  public void doFilterTransportAll() {
    MicroserviceInstance instance1 = createInstance("a", "b");
    MicroserviceInstance instance2 = createInstance("b");
    newCache = createMicroserviceInstancesCache("name", instance1, instance2);

    transportName = "";
    filter.onChanged(context, newCache);
    VersionedCache result = filter.doFilter(context, newCache);

    Assert.assertEquals(newCache.cacheVersion(), result.cacheVersion());

    List<String> expect = new ArrayList<>();
    expect.addAll(instance1.getEndpoints());
    expect.addAll(instance2.getEndpoints());
    Assert.assertThat(result.collectionData(), Matchers.contains(expect.toArray()));
  }

  @Test
  public void onChangedIgnoreInvalid() {
    MicroserviceInstance instance1 = createInstance("a", "b");
    MicroserviceInstance instance2 = createInstance("");
    newCache = createMicroserviceInstancesCache("name", instance1, instance2);

    transportName = "";
    filter.onChanged(context, newCache);
    VersionedCache result = filter.doFilter(context, newCache);

    Assert.assertEquals(newCache.cacheVersion(), result.cacheVersion());
    Assert.assertThat(result.collectionData(), Matchers.contains(instance1.getEndpoints().toArray()));
  }

  @Test
  public void createEndpointNull() {
    disableCreate = true;
    MicroserviceInstance instance1 = createInstance("a", "b");
    newCache = createMicroserviceInstancesCache("name", instance1);

    transportName = "";
    filter.onChanged(context, newCache);
    VersionedCache result = filter.doFilter(context, newCache);

    Assert.assertEquals(newCache.cacheVersion(), result.cacheVersion());
    Assert.assertThat(result.collectionData(), Matchers.empty());
  }
}
