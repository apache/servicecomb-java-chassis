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
package org.apache.servicecomb.samples.apm.impl.output;

import org.apache.servicecomb.common.rest.RestConst;
import org.apache.servicecomb.common.rest.definition.RestOperationMeta;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.event.InvocationFinishEvent;
import org.apache.servicecomb.core.invocation.InvocationStageTrace;

public class HeaderOutputGenerator extends AbstractOutputGenerator {
  @Override
  public void generate(StringBuilder sb, InvocationFinishEvent event) {
    Invocation invocation = event.getInvocation();
    RestOperationMeta restOperationMeta = invocation.getOperationMeta().getExtData(RestConst.SWAGGER_REST_OPERATION);
    InvocationStageTrace stageTrace = event.getInvocation().getInvocationStageTrace();

    sb.append(invocation.getInvocationQualifiedName()).append(":\n");
    appendLine(sb, PAD2_KEY11_FMT, "http method", restOperationMeta.getHttpMethod());
    appendLine(sb, PAD2_KEY11_FMT, "url", restOperationMeta.getAbsolutePath());
    appendLine(sb, PAD2_KEY11_FMT, "status code", event.getResponse().getStatusCode());
    appendLine(sb, PAD2_KEY11_FMT, "traceId", invocation.getTraceId());

    appendTimeLine(sb, PAD2_TIME_FMT, "total", stageTrace.calcTotalTime());
    appendTimeLine(sb, PAD4_TIME_FMT, InvocationStageTrace.PREPARE, stageTrace.calcInvocationPrepareTime());
  }
}
