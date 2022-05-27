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

import mockit.Deencapsulation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestURIEndpointObject {
  @Test
  public void testRestEndpointObject() {
    URIEndpointObject obj = new URIEndpointObject("http://127.0.2.0:8080");
    Assertions.assertEquals("127.0.2.0", obj.getHostOrIp());
    Assertions.assertEquals(8080, obj.getPort());
    Assertions.assertFalse(obj.isSslEnabled());

    obj = new URIEndpointObject("http://127.0.2.0:8080?sslEnabled=true");
    Assertions.assertEquals("127.0.2.0", obj.getHostOrIp());
    Assertions.assertEquals(8080, obj.getPort());
    Assertions.assertTrue(obj.isSslEnabled());
    Assertions.assertNull(obj.getFirst("notExist"));

    obj = new URIEndpointObject("http://127.0.2.0:8080?sslEnabled=true&protocol=http2");
    Assertions.assertEquals("127.0.2.0", obj.getHostOrIp());
    Assertions.assertEquals(8080, obj.getPort());
    Assertions.assertTrue(obj.isSslEnabled());
    Assertions.assertTrue(obj.isHttp2Enabled());
    Assertions.assertNull(obj.getFirst("notExist"));

    obj = new URIEndpointObject("rest://127.0.2.0:8080?urlPrefix=%2Froot");
    Assertions.assertEquals("127.0.2.0", obj.getHostOrIp());
    Assertions.assertEquals(8080, obj.getPort());
    Assertions.assertEquals("/root", obj.getQuery("urlPrefix").get(0));
  }

  @Test
  public void testRestEndpointObjectException() {
    Assertions.assertThrows(IllegalArgumentException.class, () -> new URIEndpointObject("http://127.0.2.0"));
  }

  @Test
  public void testQueryChineseAndSpaceAndEmpty() throws UnsupportedEncodingException {
    String strUri =
        "cse://1.1.1.1:1234/abc?a=1&b=&country=" + URLEncoder.encode("中 国", StandardCharsets.UTF_8.name());
    URIEndpointObject ep = new URIEndpointObject(strUri);

    Map<String, List<String>> querys = Deencapsulation.getField(ep, "querys");
    Assertions.assertEquals(3, querys.size());

    Assertions.assertEquals(1, ep.getQuery("a").size());
    Assertions.assertEquals("1", ep.getFirst("a"));

    Assertions.assertEquals(1, ep.getQuery("b").size());
    Assertions.assertEquals("", ep.getFirst("b"));

    Assertions.assertEquals(1, ep.getQuery("country").size());
    Assertions.assertEquals("中 国", ep.getFirst("country"));
  }
}
