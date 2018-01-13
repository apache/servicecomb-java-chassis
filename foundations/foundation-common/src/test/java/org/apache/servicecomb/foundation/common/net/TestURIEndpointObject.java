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

package org.apache.servicecomb.foundation.common.net;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import mockit.Deencapsulation;

public class TestURIEndpointObject {
  @Test
  public void testRestEndpointObject() {
    URIEndpointObject obj = new URIEndpointObject("http://127.0.2.0:8080");
    Assert.assertEquals("127.0.2.0", obj.getHostOrIp());
    Assert.assertEquals(8080, obj.getPort());
    Assert.assertFalse(obj.isSslEnabled());

    obj = new URIEndpointObject("http://127.0.2.0:8080?sslEnabled=true");
    Assert.assertEquals("127.0.2.0", obj.getHostOrIp());
    Assert.assertEquals(8080, obj.getPort());
    Assert.assertTrue(obj.isSslEnabled());
    Assert.assertNull(obj.getFirst("notExist"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testRestEndpointObjectException() {
    new URIEndpointObject("http://127.0.2.0");
  }

  @Test
  public void testQueryChineseAndSpaceAndEmpty() throws UnsupportedEncodingException {
    String strUri =
        "cse://1.1.1.1:1234/abc?a=1&b=&country=" + URLEncoder.encode("中 国", StandardCharsets.UTF_8.name());
    URIEndpointObject ep = new URIEndpointObject(strUri);

    Map<String, List<String>> querys = Deencapsulation.getField(ep, "querys");
    Assert.assertEquals(3, querys.size());

    Assert.assertEquals(1, ep.getQuery("a").size());
    Assert.assertEquals("1", ep.getFirst("a"));

    Assert.assertEquals(1, ep.getQuery("b").size());
    Assert.assertEquals("", ep.getFirst("b"));

    Assert.assertEquals(1, ep.getQuery("country").size());
    Assert.assertEquals("中 国", ep.getFirst("country"));
  }
}
