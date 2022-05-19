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

import mockit.Deencapsulation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestVersionedCache {
  private static final AtomicInteger VERSION = Deencapsulation.getField(VersionedCache.class, "VERSION");

  @Test
  public void construct() {
    VersionedCache cache = new VersionedCache();

    Assertions.assertNull(cache.data());
    Assertions.assertNull(cache.arrayData());
    Assertions.assertTrue(cache.isEmpty());
  }

  @Test
  public void autoCacheVersion() {
    VersionedCache cache = new VersionedCache().autoCacheVersion();

    Assertions.assertEquals(VERSION.get(), cache.cacheVersion());
  }

  @Test
  public void setCacheVersion() {
    VersionedCache parent = new VersionedCache().autoCacheVersion();
    VersionedCache cache = new VersionedCache().cacheVersion(parent.cacheVersion());

    Assertions.assertEquals(parent.cacheVersion(), cache.cacheVersion());
  }

  @Test
  public void setName() {
    VersionedCache cache = new VersionedCache().name("n");

    Assertions.assertEquals("n", cache.name());
  }

  @Test
  public void setSubName() {
    VersionedCache parent = new VersionedCache().name("parent");
    VersionedCache child = new VersionedCache().subName(parent, "child");

    Assertions.assertEquals("parent/child", child.name());
  }

  @Test
  public void setMapData() {
    VersionedCache cache = new VersionedCache().data(Collections.emptyMap());

    Assertions.assertSame(Collections.emptyMap(), cache.data());
    Assertions.assertSame(Collections.emptyMap(), cache.mapData());
    Assertions.assertTrue(cache.isEmpty());

    cache.data(Collections.singletonMap("k", "v"));
    Assertions.assertFalse(cache.isEmpty());
  }

  @Test
  public void setCollectionData() {
    VersionedCache cache = new VersionedCache().data(Collections.emptyList());

    Assertions.assertSame(Collections.emptyList(), cache.data());
    Assertions.assertSame(Collections.emptyList(), cache.collectionData());
    Assertions.assertTrue(cache.isEmpty());

    cache.data(Collections.singletonList("v"));
    Assertions.assertFalse(cache.isEmpty());
  }

  @Test
  public void setArrayData() {
    Object[] array = Collections.emptyList().toArray();
    VersionedCache cache = new VersionedCache().data(array);

    Assertions.assertSame(array, cache.data());
    Assertions.assertSame(array, cache.arrayData());
    Assertions.assertTrue(cache.isEmpty());

    cache.data(new String[] {"a", "b"});
    Assertions.assertFalse(cache.isEmpty());
  }

  @Test
  public void setCommonData() {
    VersionedCache cache = new VersionedCache().data(null);

    Assertions.assertNull(cache.data());
    Assertions.assertNull(cache.arrayData());
    Assertions.assertTrue(cache.isEmpty());

    cache.data("a");
    Assertions.assertFalse(cache.isEmpty());
  }

  @Test
  public void isExpired() {
    VersionedCache cacheOld = new VersionedCache().autoCacheVersion();
    VersionedCache cacheNew = new VersionedCache().autoCacheVersion();

    Assertions.assertTrue(cacheOld.isExpired(cacheNew));
    Assertions.assertFalse(cacheOld.isExpired(cacheOld));
    Assertions.assertFalse(cacheNew.isExpired(cacheOld));
  }

  @Test
  public void isSameVersion() {
    VersionedCache cacheOld = new VersionedCache().autoCacheVersion();
    VersionedCache cacheNew = new VersionedCache().autoCacheVersion();
    VersionedCache cacheSame = new VersionedCache().cacheVersion(cacheNew.cacheVersion());

    Assertions.assertFalse(cacheOld.isSameVersion(cacheNew));
    Assertions.assertTrue(cacheSame.isSameVersion(cacheNew));
  }
}
