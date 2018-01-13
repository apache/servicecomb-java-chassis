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

package org.apache.servicecomb.foundation.common.cache;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;

import mockit.Deencapsulation;

public class TestVersionedCache {
  private static AtomicInteger VERSION = Deencapsulation.getField(VersionedCache.class, "VERSION");

  @Test
  public void construct() {
    VersionedCache cache = new VersionedCache();

    Assert.assertNull(cache.data());
    Assert.assertNull(cache.arrayData());
    Assert.assertTrue(cache.isEmpty());
  }

  @Test
  public void autoCacheVersion() {
    VersionedCache cache = new VersionedCache().autoCacheVersion();

    Assert.assertEquals(VERSION.get(), cache.cacheVersion());
  }

  @Test
  public void setCacheVersion() {
    VersionedCache parent = new VersionedCache().autoCacheVersion();
    VersionedCache cache = new VersionedCache().cacheVersion(parent.cacheVersion());

    Assert.assertEquals(parent.cacheVersion(), cache.cacheVersion());
  }

  @Test
  public void setName() {
    VersionedCache cache = new VersionedCache().name("n");

    Assert.assertEquals("n", cache.name());
  }

  @Test
  public void setSubName() {
    VersionedCache parent = new VersionedCache().name("parent");
    VersionedCache child = new VersionedCache().subName(parent, "child");

    Assert.assertEquals("parent/child", child.name());
  }

  @Test
  public void setMapData() {
    VersionedCache cache = new VersionedCache().data(Collections.emptyMap());

    Assert.assertSame(Collections.emptyMap(), cache.data());
    Assert.assertSame(Collections.emptyMap(), cache.mapData());
    Assert.assertTrue(cache.isEmpty());

    cache.data(Collections.singletonMap("k", "v"));
    Assert.assertFalse(cache.isEmpty());
  }

  @Test
  public void setCollectionData() {
    VersionedCache cache = new VersionedCache().data(Collections.emptyList());

    Assert.assertSame(Collections.emptyList(), cache.data());
    Assert.assertSame(Collections.emptyList(), cache.collectionData());
    Assert.assertTrue(cache.isEmpty());

    cache.data(Collections.singletonList("v"));
    Assert.assertFalse(cache.isEmpty());
  }

  @Test
  public void setArrayData() {
    Object[] array = Collections.emptyList().toArray();
    VersionedCache cache = new VersionedCache().data(array);

    Assert.assertSame(array, cache.data());
    Assert.assertSame(array, cache.arrayData());
    Assert.assertTrue(cache.isEmpty());

    cache.data(new String[] {"a", "b"});
    Assert.assertFalse(cache.isEmpty());
  }

  @Test
  public void setCommonData() {
    VersionedCache cache = new VersionedCache().data(null);

    Assert.assertNull(cache.data());
    Assert.assertNull(cache.arrayData());
    Assert.assertTrue(cache.isEmpty());

    cache.data("a");
    Assert.assertFalse(cache.isEmpty());
  }

  @Test
  public void isExpired() {
    VersionedCache cacheOld = new VersionedCache().autoCacheVersion();
    VersionedCache cacheNew = new VersionedCache().autoCacheVersion();

    Assert.assertTrue(cacheOld.isExpired(cacheNew));
    Assert.assertFalse(cacheOld.isExpired(cacheOld));
    Assert.assertFalse(cacheNew.isExpired(cacheOld));
  }
}
