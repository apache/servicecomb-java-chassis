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

package org.apache.servicecomb.foundation.metrics.publish;

import org.apache.servicecomb.foundation.common.exceptions.ServiceCombException;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class TestMetric {
  @Test
  public void testNewMetric() {

    Metric metric = new Metric("Key", 100);
    Assert.assertEquals(0, metric.getTagsCount());

    metric = new Metric("Key(A=1)", 100);
    Assert.assertEquals(1, metric.getTagsCount());
    Assert.assertEquals(true, metric.containsTagKey("A"));

    metric = new Metric("Key(A=1,B=X)", 100);
    Assert.assertEquals(2, metric.getTagsCount());
    Assert.assertEquals(true, metric.containsTagKey("A"));
    Assert.assertEquals(true, metric.containsTagKey("B"));
    Assert.assertEquals("1", metric.getTagValue("A"));
    Assert.assertEquals("X", metric.getTagValue("B"));

    checkBadIdFormat(null);
    checkBadIdFormat("");
    checkBadIdFormat("(");
    checkBadIdFormat(")");
    checkBadIdFormat("()");

    checkBadIdFormat("Key(");
    checkBadIdFormat("Key)");
    checkBadIdFormat("Key()");

    checkBadIdFormat("Key(X)");
    checkBadIdFormat("Key(X");
    checkBadIdFormat("Key(X))");
    checkBadIdFormat("Key((X)");

    checkBadIdFormat("Key(X=)");
    checkBadIdFormat("Key(X=");
    checkBadIdFormat("Key(X=))");
    checkBadIdFormat("Key((X=)");

    checkBadIdFormat("Key(X=,)");
    checkBadIdFormat("Key(X=,");
    checkBadIdFormat("Key(X=,))");
    checkBadIdFormat("Key((X=,)");

    checkBadIdFormat("Key(X=,Y)");
    checkBadIdFormat("Key(X=,Y");
    checkBadIdFormat("Key(X=,Y))");
    checkBadIdFormat("Key((X=,Y)");

    checkBadIdFormat("Key(X=1,Y)");
    checkBadIdFormat("Key(X=1,Y");
    checkBadIdFormat("Key(X=1,Y))");
    checkBadIdFormat("Key((X=1,Y)");

    checkBadIdFormat("Key(X=1))");
    checkBadIdFormat("Key((X=1)");

    checkBadIdFormat("Key(X=1) ");
    checkBadIdFormat("Key(X=1,Y=2)Z");

    checkBadIdFormat("Key(X=1)()");
    checkBadIdFormat("Key(X=1)(Y=1)");
  }

  @Test
  public void checkMetricContainsTag() {
    Metric metric = new Metric("Key(A=1,B=X)", 100);
    Assert.assertEquals(true, metric.containsTag("A", "1"));
  }

  @Test
  public void checkMetricContainsTagWithWrongTagsCount() {
    Metric metric = new Metric("Key(A=1,B=X)", 100);
    checkMetricContainsTagWithWrongTagsCount(metric, "A");
    checkMetricContainsTagWithWrongTagsCount(metric, "A", "1", "B");
    checkMetricContainsTagWithWrongTagsCount(metric, "A", "1", "B", "X", "C");
  }


  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private void checkMetricContainsTagWithWrongTagsCount(Metric metric, String... tags) {
    thrown.expect(ServiceCombException.class);
    metric.containsTag(tags);
    Assert.fail("checkMetricContainsTagWithWrongTagsCount failed : " + String.join(",", tags));
  }

  private void checkBadIdFormat(String id) {
    thrown.expect(ServiceCombException.class);
    new Metric(id, 100);
    Assert.fail("checkBadIdFormat failed : " + id);
  }
}
