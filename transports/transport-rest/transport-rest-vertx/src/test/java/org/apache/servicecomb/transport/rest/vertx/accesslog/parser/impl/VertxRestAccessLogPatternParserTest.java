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

package org.apache.servicecomb.transport.rest.vertx.accesslog.parser.impl;

import java.util.Comparator;
import java.util.function.Function;

import org.apache.servicecomb.transport.rest.vertx.accesslog.parser.AccessLogItemMeta;
import org.apache.servicecomb.transport.rest.vertx.accesslog.parser.impl.VertxRestAccessLogPatternParser.AccessLogItemMetaWrapper;
import org.junit.Assert;
import org.junit.Test;

public class VertxRestAccessLogPatternParserTest {

  Comparator<AccessLogItemMetaWrapper> comparator = VertxRestAccessLogPatternParser.accessLogItemMetaWrapperComparator;

  Function<AccessLogItemMeta, AccessLogItemMetaWrapper> wrapper =
      accessLogItemMeta -> new AccessLogItemMetaWrapper(accessLogItemMeta, null);

  /**
   * one factor test
   */
  @Test
  public void testCompareMetaSimple() {
    Assert.assertTrue(
        comparator.compare(
            wrapper.apply(new AccessLogItemMeta(null, null, 0)),
            wrapper.apply(new AccessLogItemMeta(null, null, 1))
        ) < 0
    );
    Assert.assertTrue(
        comparator.compare(
            wrapper.apply(new AccessLogItemMeta(null, "}abc")),
            wrapper.apply(new AccessLogItemMeta(null, null))
        ) < 0
    );
    Assert.assertTrue(
        comparator.compare(
            wrapper.apply(new AccessLogItemMeta(null, "}abc")),
            wrapper.apply(new AccessLogItemMeta(null, "}de"))
        ) < 0
    );
    Assert.assertTrue(
        comparator.compare(
            wrapper.apply(new AccessLogItemMeta(null, "}abc")),
            wrapper.apply(new AccessLogItemMeta(null, "}ab"))
        ) < 0
    );
    Assert.assertTrue(
        comparator.compare(
            wrapper.apply(new AccessLogItemMeta("%abc", null)),
            wrapper.apply(new AccessLogItemMeta("%de", null))
        ) < 0
    );
    Assert.assertTrue(
        comparator.compare(
            wrapper.apply(new AccessLogItemMeta("%abc", null)),
            wrapper.apply(new AccessLogItemMeta("%ab", null))
        ) < 0
    );
    Assert.assertEquals(0, comparator.compare(
        wrapper.apply(new AccessLogItemMeta("%abc", null)),
        wrapper.apply(new AccessLogItemMeta("%abc", null))
    ));
  }

  /**
   * multiple factors test
   */
  @Test
  public void testCompareMetaComplex() {
    Assert.assertTrue(
        comparator.compare(
            wrapper.apply(new AccessLogItemMeta("%bcd", "}ab", 0)),
            wrapper.apply(new AccessLogItemMeta("%abc", "}abc", 0))
        ) > 0
    );
    Assert.assertTrue(
        comparator.compare(
            wrapper.apply(new AccessLogItemMeta("%abc", null, 0)),
            wrapper.apply(new AccessLogItemMeta("%bcd", "}ab", 0))
        ) > 0
    );
    Assert.assertTrue(
        comparator.compare(
            wrapper.apply(new AccessLogItemMeta("%bcd", "}abc")),
            wrapper.apply(new AccessLogItemMeta("%abc", "}abc"))
        ) > 0
    );
    Assert.assertTrue(
        comparator.compare(
            wrapper.apply(new AccessLogItemMeta("%abc", "}abc", 1)),
            wrapper.apply(new AccessLogItemMeta("%ab", "}ab", 0))
        ) > 0
    );
  }

  @Test
  public void testComparePlaceholderString() {
    Assert.assertTrue(
        VertxRestAccessLogPatternParser.comparePlaceholderString("abc", "bbc") < 0
    );
    Assert.assertTrue(
        VertxRestAccessLogPatternParser.comparePlaceholderString("abc", "ab") < 0
    );
    Assert.assertEquals(0, VertxRestAccessLogPatternParser.comparePlaceholderString("abc", "abc"));
    Assert.assertTrue(
        VertxRestAccessLogPatternParser.comparePlaceholderString("bbc", "abc") > 0
    );
    Assert.assertTrue(
        VertxRestAccessLogPatternParser.comparePlaceholderString("ab", "abc") > 0
    );
  }
}
