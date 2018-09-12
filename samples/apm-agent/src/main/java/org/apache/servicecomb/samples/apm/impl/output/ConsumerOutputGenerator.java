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

import org.apache.servicecomb.core.event.InvocationFinishEvent;
import org.apache.servicecomb.core.invocation.InvocationStageTrace;

public class ConsumerOutputGenerator extends AbstractOutputGenerator {
  @Override
  public void generate(StringBuilder sb, InvocationFinishEvent event) {
    InvocationStageTrace stageTrace = event.getInvocation().getInvocationStageTrace();

    appendTimeLine(sb, PAD4_TIME_FMT, InvocationStageTrace.HANDLERS_REQUEST, stageTrace.calcHandlersRequestTime());
    appendTimeLine(sb, PAD4_TIME_FMT, InvocationStageTrace.CLIENT_FILTERS_REQUEST,
        stageTrace.calcClientFiltersRequestTime());
    appendTimeLine(sb, PAD4_TIME_FMT, InvocationStageTrace.CONSUMER_SEND_REQUEST, stageTrace.calcSendRequestTime());
    appendTimeLine(sb, PAD6_TIME_FMT, InvocationStageTrace.CONSUMER_GET_CONNECTION, stageTrace.calcGetConnectionTime());
    appendTimeLine(sb, PAD6_TIME_FMT, InvocationStageTrace.CONSUMER_WRITE_TO_BUF, stageTrace.calcWriteToBufferTime());
    appendTimeLine(sb, PAD4_TIME_FMT, InvocationStageTrace.CONSUMER_WAIT_RESPONSE,
        stageTrace.calcReceiveResponseTime());
    appendTimeLine(sb, PAD4_TIME_FMT, InvocationStageTrace.CONSUMER_WAKE_CONSUMER, stageTrace.calcWakeConsumer());
    appendTimeLine(sb, PAD4_TIME_FMT, InvocationStageTrace.CLIENT_FILTERS_RESPONSE,
        stageTrace.calcClientFiltersResponseTime());
    appendTimeLine(sb, PAD4_TIME_FMT, InvocationStageTrace.HANDLERS_RESPONSE, stageTrace.calcHandlersResponseTime());
  }
}
