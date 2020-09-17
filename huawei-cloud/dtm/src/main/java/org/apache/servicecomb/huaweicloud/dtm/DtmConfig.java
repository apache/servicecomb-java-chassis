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

import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;

public class DtmConfig {
  private static final String DTM_CONTEXT_CLASS_NAME_KEY = "servicecomb.dtm.className";

  private static final String DTM_CONTEXT_DEFAULT_CLASS_NAME = "com.huawei.middleware.dtm.client.context.DTMContext";

  public static final String DTM_EXPORT_METHOD = "getContextData";

  public static final String DTM_IMPORT_METHOD = "setContextData";

  public static final String DTM_TRACE_ID_KEY = "X-Dtm-Trace-Id-Key";

  public static final DtmConfig INSTANCE = new DtmConfig();

  private DynamicStringProperty contextClassNameProperty = DynamicPropertyFactory.getInstance()
      .getStringProperty(DTM_CONTEXT_CLASS_NAME_KEY, DTM_CONTEXT_DEFAULT_CLASS_NAME);

  private DtmConfig() {
  }

  public String getDtmContextClassName() {
    return contextClassNameProperty.get();
  }
}
