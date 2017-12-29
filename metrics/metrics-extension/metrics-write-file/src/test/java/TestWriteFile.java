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

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import io.servicecomb.metrics.common.CallMetric;
import io.servicecomb.metrics.common.ConsumerInvocationMetric;
import io.servicecomb.metrics.common.RegistryMetric;
import io.servicecomb.metrics.common.SystemMetric;
import io.servicecomb.metrics.common.TimerMetric;
import io.servicecomb.metrics.core.publish.DataSource;
import io.servicecomb.metrics.extension.writefile.SimpleFileContentConvertor;
import io.servicecomb.metrics.extension.writefile.SimpleFileContentFormatter;
import io.servicecomb.metrics.extension.writefile.WriteFileInitializer;
import io.servicecomb.metrics.extension.writefile.config.MetricsFileWriter;

public class TestWriteFile {

  @Test
  public void test() {

    StringBuilder builder = new StringBuilder();

    SimpleFileContentFormatter formatter = new SimpleFileContentFormatter("localhost", "appId.serviceName");
    SimpleFileContentConvertor convertor = new SimpleFileContentConvertor();

    MetricsFileWriter writer = (loggerName, filePrefix, content) ->
        builder.append(loggerName).append(filePrefix).append(content);

    SystemMetric systemMetric = new SystemMetric(50, 10, 1, 2, 3,
        4, 5, 6, 7, 8);

    Map<String, ConsumerInvocationMetric> consumerInvocationMetricMap = new HashMap<>();
    consumerInvocationMetricMap.put("A", new ConsumerInvocationMetric("A", "A",
        new TimerMetric("A1", 1, 2, 3, 4), new CallMetric("A2", 100, 999.44444)));

    consumerInvocationMetricMap.put("B", new ConsumerInvocationMetric("B", "B",
        new TimerMetric("B1", 1, 2, 3, 4), new CallMetric("B2", 100, 888.66666)));

    RegistryMetric metric = new RegistryMetric(systemMetric, consumerInvocationMetricMap, new HashMap<>());

    DataSource dataSource = Mockito.mock(DataSource.class);
    Mockito.when(dataSource.getRegistryMetric()).thenReturn(metric);

    WriteFileInitializer writeFileInitializer = new WriteFileInitializer(writer, convertor, formatter, dataSource,
        "appId.serviceName");

    writeFileInitializer.run();

    String sb = builder.toString();

    Assert.assertTrue(sb.contains("999.4"));
    Assert.assertTrue(sb.contains("888.7"));
  }
}
