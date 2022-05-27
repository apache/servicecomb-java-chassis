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

package org.apache.servicecomb.foundation.common.utils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ResourceUtilTest {

  /**
   * This case is coupled with the Spring dependency, but in order to check the ability to read resources inside the
   * jar packs, this may be the simplest way.
   */
  @Test
  public void loadResources_in_jar() throws IOException, URISyntaxException {
    List<URI> uris = ResourceUtil.findResources("META-INF", p -> p.toString().endsWith("spring.factories"));
    Assertions.assertEquals(1, uris.size());
    Assertions.assertTrue(uris.get(0).toString().startsWith("jar:file:"));
    Assertions.assertTrue(uris.get(0).toString().endsWith("!/META-INF/spring.factories"));
  }

  @Test
  public void loadResources_in_disk() throws IOException, URISyntaxException {
    List<URI> uris = ResourceUtil.findResourcesBySuffix("META-INF/spring", ".xml");
    Assertions.assertEquals(1, uris.size());
    URI uri = uris.get(0);
    Assertions.assertTrue(uri.toString().startsWith("file:"), "unexpected uri: " + uri);
    Assertions.assertTrue(uri.toString().endsWith("META-INF/spring/config.bean.xml"), "unexpected uri: " + uri);
  }

  @Test
  public void loadResources_exact_file_in_disk() throws IOException, URISyntaxException {
    List<URI> uris = ResourceUtil.findResources("META-INF/spring/config.bean.xml");
    Assertions.assertEquals(1, uris.size());
    URI uri = uris.get(0);
    Assertions.assertTrue(uri.toString().startsWith("file:"), "unexpected uri: " + uri);
    Assertions.assertTrue(uri.toString().endsWith("META-INF/spring/config.bean.xml"), "unexpected uri: " + uri);
  }
}
