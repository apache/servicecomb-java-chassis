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
package com.huawei.middleware.dtm.client.context;

import java.util.HashMap;
import java.util.Map;

import org.springframework.util.CollectionUtils;

public class DTMContext {
  public static final String GLOBAL_TX_ID_KEY = "dtm-global-tx-key";

  public static final String TRACE_ID_KEY = "X-Dtm-Trace-Id-Key";

  public static String GLOBAL_TX_ID = "";

  public static String TRACE_ID = "";

  public static Map<String, String> getContextData() {
    HashMap<String, String> context = new HashMap<>();
    context.put(GLOBAL_TX_ID_KEY, GLOBAL_TX_ID);
    return context;
  }

  public static void setContextData(Map<String, String> context) {
    if (CollectionUtils.isEmpty(context)) {
      return;
    }
    GLOBAL_TX_ID = context.getOrDefault(GLOBAL_TX_ID_KEY, "");
    TRACE_ID = context.getOrDefault(TRACE_ID_KEY, "");
  }
}
