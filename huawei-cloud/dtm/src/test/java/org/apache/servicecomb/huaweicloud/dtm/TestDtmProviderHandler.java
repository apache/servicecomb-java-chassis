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
package org.apache.servicecomb.huaweicloud.dtm;

import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.core.Const;
import org.apache.servicecomb.core.Invocation;
import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.BeforeAll;

import com.huawei.middleware.dtm.client.context.DTMContext;

import mockit.Expectations;
import mockit.Mocked;

public class TestDtmProviderHandler {

  @BeforeAll
  public void init() {
    DTMContext.TRACE_ID = "";
    DTMContext.GLOBAL_TX_ID = "";
  }

  @Test
  public void testHandler(@Mocked Invocation invocation) throws Exception {
    DtmProviderHandler providerHandler = new DtmProviderHandler();
    providerHandler.init(null, null);
    Map<String, String> context = new HashMap<>();
    new Expectations() {{
      invocation.getContext();
      result = context;
    }};
    String expectTxId = "dtm-tx-id";
    String expectTraceId = "dtm-trace-id";
    context.put(DTMContext.GLOBAL_TX_ID_KEY, expectTxId);
    context.put(Const.TRACE_ID_NAME, expectTraceId);

    Assert.assertEquals("", DTMContext.TRACE_ID);
    Assert.assertEquals("", DTMContext.GLOBAL_TX_ID);
    providerHandler.handle(invocation, null);
    Assert.assertEquals(expectTraceId, DTMContext.TRACE_ID);
    Assert.assertEquals(expectTxId, DTMContext.GLOBAL_TX_ID);
  }
}
