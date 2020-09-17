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

import static org.apache.servicecomb.huaweicloud.dtm.DtmConfig.DTM_TRACE_ID_KEY;

import java.lang.reflect.Method;
import java.util.Map;

import org.apache.servicecomb.core.Const;
import org.apache.servicecomb.core.Handler;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.definition.MicroserviceMeta;
import org.apache.servicecomb.swagger.invocation.AsyncResponse;
import org.apache.servicecomb.swagger.invocation.InvocationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DtmProviderHandler implements Handler {
  private static final Logger LOG = LoggerFactory.getLogger(DtmProviderHandler.class);

  private Method dtmContextImMethod;

  @Override
  public void init(MicroserviceMeta microserviceMeta, InvocationType invocationType) {
    String className = DtmConfig.INSTANCE.getDtmContextClassName();
    try {
      Class<?> clazz = Class.forName(className);
      dtmContextImMethod = clazz.getMethod(DtmConfig.DTM_IMPORT_METHOD, Map.class);
    } catch (Throwable e) {
      // ignore just warn
      LOG.warn("Failed to init method {}#{}", className, DtmConfig.DTM_IMPORT_METHOD, e);
    }
  }

  @Override
  public void handle(Invocation invocation, AsyncResponse asyncResp) throws Exception {
    try {
      if (dtmContextImMethod != null) {
        String traceId = invocation.getContext().get(Const.TRACE_ID_NAME);
        invocation.getContext().put(DTM_TRACE_ID_KEY, traceId);
        dtmContextImMethod.invoke(null, invocation.getContext());
      }
    } catch (Throwable e) {
      LOG.warn("Failed to execute method DTMContext#{}, please check", DtmConfig.DTM_IMPORT_METHOD, e);
    }
    invocation.next(asyncResp);
  }
}
