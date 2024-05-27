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

package org.apache.servicecomb.config.client;

import com.ctrip.framework.apollo.core.enums.ConfigFileFormat;
import org.apache.servicecomb.config.ConfigUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ApolloClientTest {
  @BeforeAll
  public static void setUpClass() {

    ApolloConfig.setConcurrentCompositeConfiguration(ConfigUtil.createLocalConfig());
  }


  @Test
  void testDetermineFileFormat() {
    ApolloClient apolloClient = new ApolloClient(null);
    Assertions.assertEquals(apolloClient.determineFileFormat("abc"), ConfigFileFormat.Properties);
    Assertions.assertEquals(apolloClient.determineFileFormat("abc.pRopErties"), ConfigFileFormat.Properties);
    Assertions.assertEquals(apolloClient.determineFileFormat("abc.properties"), ConfigFileFormat.Properties);
    Assertions.assertEquals(apolloClient.determineFileFormat("abc.xml"), ConfigFileFormat.XML);
    Assertions.assertEquals(apolloClient.determineFileFormat("abc.XmL"), ConfigFileFormat.XML);
    Assertions.assertEquals(apolloClient.determineFileFormat("abc.jSon"), ConfigFileFormat.JSON);
    Assertions.assertEquals(apolloClient.determineFileFormat("abc.jsOn"), ConfigFileFormat.JSON);
    Assertions.assertEquals(apolloClient.determineFileFormat("abc.yaml"), ConfigFileFormat.YAML);
    Assertions.assertEquals(apolloClient.determineFileFormat("abc.yAml"), ConfigFileFormat.YAML);
    Assertions.assertEquals(apolloClient.determineFileFormat("abc.yml"), ConfigFileFormat.YML);
    Assertions.assertEquals(apolloClient.determineFileFormat("abc.yMl"), ConfigFileFormat.YML);
    Assertions.assertEquals(apolloClient.determineFileFormat("abc.properties.yml"), ConfigFileFormat.YML);
  }

  @Test
  void refreshApolloConfig() {

  }
}
