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

package org.apache.servicecomb.core;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.core.definition.SchemaMeta;
import org.apache.servicecomb.core.event.InvocationBusinessMethodFinishEvent;
import org.apache.servicecomb.core.event.InvocationBusinessMethodStartEvent;
import org.apache.servicecomb.core.event.InvocationFinishEvent;
import org.apache.servicecomb.core.event.InvocationStartEvent;
import org.apache.servicecomb.core.invocation.InvocationStageTrace;
import org.apache.servicecomb.core.provider.consumer.ReferenceConfig;
import org.apache.servicecomb.core.tracing.TraceIdGenerator;
import org.apache.servicecomb.foundation.common.event.EventManager;
import org.apache.servicecomb.foundation.common.utils.SPIServiceUtils;
import org.apache.servicecomb.foundation.vertx.http.HttpServletRequestEx;
import org.apache.servicecomb.swagger.invocation.AsyncResponse;
import org.apache.servicecomb.swagger.invocation.InvocationType;
import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.SwaggerInvocation;

public class Invocation extends SwaggerInvocation {
  private static final Collection<TraceIdGenerator> TRACE_ID_GENERATORS = loadTraceIdGenerators();

  static Collection<TraceIdGenerator> loadTraceIdGenerators() {
    return SPIServiceUtils.getPriorityHighestServices(generator -> generator.getName(), TraceIdGenerator.class);
  }

  private ReferenceConfig referenceConfig;

  // 本次调用对应的schemaMeta
  private SchemaMeta schemaMeta;

  // 本次调用对应的operationMeta
  private OperationMeta operationMeta;

  // loadbalance查询得到的地址，由transport client使用
  // 之所以不放在handlerContext中，是因为这属于核心数据，没必要走那样的机制
  private Endpoint endpoint;

  // 只用于handler之间传递数据，是本地数据
  private Map<String, Object> handlerContext = localContext;

  // handler链，是arrayList，可以高效地通过index访问
  private List<Handler> handlerList;

  private int handlerIndex;

  // 应答的处理器
  // 同步模式：避免应答在网络线程中处理解码等等业务级逻辑
  private Executor responseExecutor;

  private boolean sync = true;

  private InvocationStageTrace invocationStageTrace = new InvocationStageTrace(this);

  private HttpServletRequestEx requestEx;

  private boolean finished;

  // not extend InvocationType
  // because isEdge() only affect to apm/metrics output, no need to change so many logic
  private boolean edge;

  public HttpServletRequestEx getRequestEx() {
    return requestEx;
  }

  public InvocationStageTrace getInvocationStageTrace() {
    return invocationStageTrace;
  }

  public String getTraceId() {
    return getContext(Const.TRACE_ID_NAME);
  }

  public String getTraceId(String traceIdName) {
    return getContext(traceIdName);
  }

  @Deprecated
  public long getStartTime() {
    return invocationStageTrace.getStart();
  }

  @Deprecated
  public long getStartExecutionTime() {
    return invocationStageTrace.getStartExecution();
  }

  public Invocation(ReferenceConfig referenceConfig, OperationMeta operationMeta, Object[] swaggerArguments) {
    this.invocationType = InvocationType.CONSUMER;
    this.referenceConfig = referenceConfig;
    init(operationMeta, swaggerArguments);
  }

  public Invocation(Endpoint endpoint, OperationMeta operationMeta, Object[] swaggerArguments) {
    this.invocationType = InvocationType.PRODUCER;
    this.endpoint = endpoint;
    init(operationMeta, swaggerArguments);
  }

  private void init(OperationMeta operationMeta, Object[] swaggerArguments) {
    this.schemaMeta = operationMeta.getSchemaMeta();
    this.operationMeta = operationMeta;
    this.swaggerArguments = swaggerArguments;
    this.handlerList = getHandlerChain();
    handlerIndex = 0;
  }

  public Transport getTransport() {
    if (endpoint == null) {
      throw new IllegalStateException(
          "Endpoint is empty. Forget to configure \"loadbalance\" in consumer handler chain?");
    }
    return endpoint.getTransport();
  }

  public List<Handler> getHandlerChain() {
    return (InvocationType.CONSUMER.equals(invocationType)) ? schemaMeta.getConsumerHandlerChain()
        : schemaMeta.getProviderHandlerChain();
  }

  public Executor getResponseExecutor() {
    return responseExecutor;
  }

  public void setResponseExecutor(Executor responseExecutor) {
    this.responseExecutor = responseExecutor;
  }

  public SchemaMeta getSchemaMeta() {
    return schemaMeta;
  }

  public OperationMeta getOperationMeta() {
    return operationMeta;
  }

  public Object[] getArgs() {
    return swaggerArguments;
  }

  public Endpoint getEndpoint() {
    return endpoint;
  }

  public void setEndpoint(Endpoint endpoint) {
    this.endpoint = endpoint;
  }

  public Map<String, Object> getHandlerContext() {
    return handlerContext;
  }

  public int getHandlerIndex() {
    return handlerIndex;
  }

  public void setHandlerIndex(int handlerIndex) {
    this.handlerIndex = handlerIndex;
  }

  public void next(AsyncResponse asyncResp) throws Exception {
    // 不必判断有效性，因为整个流程都是内部控制的
    int runIndex = handlerIndex;
    handlerIndex++;
    handlerList.get(runIndex).handle(this, asyncResp);
  }

  public String getSchemaId() {
    return schemaMeta.getSchemaId();
  }

  public String getOperationName() {
    return operationMeta.getOperationId();
  }

  public String getConfigTransportName() {
    if (operationMeta.getTransport() != null) {
      return operationMeta.getTransport();
    }
    return referenceConfig.getTransport();
  }

  public String getRealTransportName() {
    return (endpoint != null) ? endpoint.getTransport().getName() : getConfigTransportName();
  }

  public String getMicroserviceName() {
    return schemaMeta.getMicroserviceName();
  }

  public String getAppId() {
    return schemaMeta.getMicroserviceMeta().getAppId();
  }

  public String getMicroserviceVersionRule() {
    return referenceConfig.getVersionRule();
  }

  public String getInvocationQualifiedName() {
    return invocationType.name() + " " + getRealTransportName() + " "
        + getOperationMeta().getMicroserviceQualifiedName();
  }

  public String getMicroserviceQualifiedName() {
    return operationMeta.getMicroserviceQualifiedName();
  }

  protected void initTraceId() {
    for (TraceIdGenerator traceIdGenerator : TRACE_ID_GENERATORS) {
      initTraceId(traceIdGenerator);
    }
  }

  protected void initTraceId(TraceIdGenerator traceIdGenerator) {
    if (!StringUtils.isEmpty(getTraceId(traceIdGenerator.getTraceIdKeyName()))) {
      // if invocation context contains traceId, nothing needed to do
      return;
    }

    if (requestEx == null) {
      // it's a new consumer invocation, must generate a traceId
      addContext(traceIdGenerator.getTraceIdKeyName(), traceIdGenerator.generate());
      return;
    }

    String traceId = requestEx.getHeader(traceIdGenerator.getTraceIdKeyName());
    if (!StringUtils.isEmpty(traceId)) {
      // if request header contains traceId, save traceId into invocation context
      addContext(traceIdGenerator.getTraceIdKeyName(), traceId);
      return;
    }

    // if traceId not found, generate a traceId
    addContext(traceIdGenerator.getTraceIdKeyName(), traceIdGenerator.generate());
  }

  public void onStart(long start) {
    invocationStageTrace.start(start);
    initTraceId();
    EventManager.post(new InvocationStartEvent(this));
  }

  public void onStart(HttpServletRequestEx requestEx, long start) {
    this.requestEx = requestEx;
    onStart(start);
  }

  public void onExecuteStart() {
    invocationStageTrace.startExecution();
  }

  @Override
  public void onBusinessMethodStart() {
    invocationStageTrace.startBusinessMethod();
    EventManager.post(new InvocationBusinessMethodStartEvent(this));
  }

  @Override
  public void onBusinessMethodFinish() {
    EventManager.post(new InvocationBusinessMethodFinishEvent(this));
  }

  @Override
  public void onBusinessFinish() {
    invocationStageTrace.finishBusiness();
  }

  public void onFinish(Response response) {
    if (finished) {
      // avoid to post repeated event
      return;
    }

    invocationStageTrace.finish();
    EventManager.post(new InvocationFinishEvent(this, response));
    finished = true;
  }

  public boolean isFinished() {
    return finished;
  }

  public boolean isSync() {
    return sync;
  }

  public void setSync(boolean sync) {
    this.sync = sync;
  }

  public boolean isConsumer() {
    return InvocationType.CONSUMER.equals(invocationType);
  }

  public boolean isEdge() {
    return edge;
  }

  public void setEdge(boolean edge) {
    this.edge = edge;
  }
}
