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

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.core.definition.InvocationRuntimeType;
import org.apache.servicecomb.core.definition.MicroserviceMeta;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.core.definition.SchemaMeta;
import org.apache.servicecomb.core.event.InvocationBusinessMethodFinishEvent;
import org.apache.servicecomb.core.event.InvocationBusinessMethodStartEvent;
import org.apache.servicecomb.core.event.InvocationEncodeResponseStartEvent;
import org.apache.servicecomb.core.event.InvocationFinishEvent;
import org.apache.servicecomb.core.event.InvocationRunInExecutorFinishEvent;
import org.apache.servicecomb.core.event.InvocationRunInExecutorStartEvent;
import org.apache.servicecomb.core.event.InvocationStartEvent;
import org.apache.servicecomb.core.invocation.InvocationStageTrace;
import org.apache.servicecomb.core.provider.consumer.InvokerUtils;
import org.apache.servicecomb.core.provider.consumer.ReferenceConfig;
import org.apache.servicecomb.core.tracing.TraceIdGenerator;
import org.apache.servicecomb.core.tracing.TraceIdLogger;
import org.apache.servicecomb.foundation.common.event.EventManager;
import org.apache.servicecomb.foundation.common.utils.AsyncUtils;
import org.apache.servicecomb.foundation.common.utils.SPIServiceUtils;
import org.apache.servicecomb.foundation.vertx.http.HttpServletRequestEx;
import org.apache.servicecomb.swagger.invocation.AsyncResponse;
import org.apache.servicecomb.swagger.invocation.InvocationType;
import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.SwaggerInvocation;

import com.fasterxml.jackson.databind.JavaType;

public class Invocation extends SwaggerInvocation {
  private static final Collection<TraceIdGenerator> TRACE_ID_GENERATORS = loadTraceIdGenerators();

  protected static final AtomicLong INVOCATION_ID = new AtomicLong();

  static Collection<TraceIdGenerator> loadTraceIdGenerators() {
    return SPIServiceUtils.getPriorityHighestServices(TraceIdGenerator::getName, TraceIdGenerator.class);
  }

  protected ReferenceConfig referenceConfig;

  private InvocationRuntimeType invocationRuntimeType;

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

  private long invocationId;

  private TraceIdLogger traceIdLogger;

  private Map<String, Object> invocationArguments = Collections.emptyMap();

  private Object[] producerArguments;

  private Map<String, Object> swaggerArguments = Collections.emptyMap();

  public long getInvocationId() {
    return invocationId;
  }

  public TraceIdLogger getTraceIdLogger() {
    return this.traceIdLogger;
  }

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

  public Invocation() {
    // An empty invocation, used to mock or some other scenario do not need operation information.
    traceIdLogger = new TraceIdLogger(this);
  }

  public Invocation(ReferenceConfig referenceConfig, OperationMeta operationMeta,
      InvocationRuntimeType invocationRuntimeType,
      Map<String, Object> swaggerArguments) {
    this.invocationType = InvocationType.CONSUMER;
    this.referenceConfig = referenceConfig;
    this.invocationRuntimeType = invocationRuntimeType;
    init(operationMeta, swaggerArguments);
  }

  public Invocation(Endpoint endpoint, OperationMeta operationMeta, Map<String, Object> swaggerArguments) {
    this.invocationType = InvocationType.PRODUCER;
    this.invocationRuntimeType = operationMeta.buildBaseProviderRuntimeType();
    this.endpoint = endpoint;
    init(operationMeta, swaggerArguments);
  }

  private void init(OperationMeta operationMeta, Map<String, Object> swaggerArguments) {
    this.invocationId = INVOCATION_ID.getAndIncrement();
    this.schemaMeta = operationMeta.getSchemaMeta();
    this.operationMeta = operationMeta;
    this.setSwaggerArguments(swaggerArguments);
    this.handlerList = getHandlerChain();
    handlerIndex = 0;
    traceIdLogger = new TraceIdLogger(this);
  }

  public Transport getTransport() {
    if (endpoint == null) {
      throw new IllegalStateException(
          "Endpoint is empty. Forget to configure \"loadbalance\" in consumer handler chain?");
    }
    return endpoint.getTransport();
  }

  public List<Handler> getHandlerChain() {
    return schemaMeta.getMicroserviceMeta().getHandlerChain();
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

  public Map<String, Object> getInvocationArguments() {
    return this.invocationArguments;
  }

  public Map<String, Object> getSwaggerArguments() {
    return this.swaggerArguments;
  }

  public Object getInvocationArgument(String name) {
    return this.invocationArguments.get(name);
  }

  public Object getSwaggerArgument(String name) {
    return this.swaggerArguments.get(name);
  }

  public void setInvocationArguments(Map<String, Object> invocationArguments) {
    if (invocationArguments == null) {
      // Empty arguments
      this.invocationArguments = new HashMap<>(0);
      return;
    }
    this.invocationArguments = invocationArguments;

    buildSwaggerArguments();
  }

  private void buildSwaggerArguments() {
    if (!this.invocationRuntimeType.isRawConsumer()) {
      this.swaggerArguments = this.invocationRuntimeType.getArgumentsMapper()
          .invocationArgumentToSwaggerArguments(this,
              this.invocationArguments);
    } else {
      this.swaggerArguments = invocationArguments;
    }
  }

  public void setSwaggerArguments(Map<String, Object> swaggerArguments) {
    if (swaggerArguments == null) {
      // Empty arguments
      this.swaggerArguments = new HashMap<>(0);
      return;
    }
    this.swaggerArguments = swaggerArguments;

    buildInvocationArguments();
  }

  private void buildInvocationArguments() {
    if (operationMeta.getSwaggerProducerOperation() != null && !isEdge()) {
      this.invocationArguments = operationMeta.getSwaggerProducerOperation().getArgumentsMapper()
          .swaggerArgumentToInvocationArguments(this,
              swaggerArguments);
    } else {
      this.invocationArguments = swaggerArguments;
    }
  }

  public Object[] toProducerArguments() {
    if (producerArguments != null) {
      return producerArguments;
    }

    Method method = operationMeta.getSwaggerProducerOperation().getProducerMethod();
    Object[] args = new Object[method.getParameterCount()];
    for (int i = 0; i < method.getParameterCount(); i++) {
      args[i] = this.invocationArguments.get(method.getParameters()[i].getName());
    }
    return producerArguments = args;
  }

  public void clearProducerArguments() {
    producerArguments = null;
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

  public MicroserviceMeta getMicroserviceMeta() {
    return schemaMeta.getMicroserviceMeta();
  }

  public String getMicroserviceVersionRule() {
    return referenceConfig.getVersionRule();
  }

  public InvocationRuntimeType getInvocationRuntimeType() {
    return this.invocationRuntimeType;
  }

  public JavaType findResponseType(int statusCode) {
    return this.invocationRuntimeType.findResponseType(statusCode);
  }

  public void setSuccessResponseType(JavaType javaType) {
    this.invocationRuntimeType.setSuccessResponseType(javaType);
  }

  @Override
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
    EventManager.post(new InvocationRunInExecutorStartEvent(this));
  }

  public void onExecuteFinish() {
    EventManager.post(new InvocationRunInExecutorFinishEvent(this));
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

  public void onEncodeResponseStart(Response response) {
    EventManager.post(new InvocationEncodeResponseStartEvent(this, response));
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

  public boolean isThirdPartyInvocation() {
    return referenceConfig.is3rdPartyService();
  }

  // ensure sync consumer invocation response flow not run in eventLoop
  public <T> CompletableFuture<T> optimizeSyncConsumerThread(CompletableFuture<T> future) {
    if (sync && !InvokerUtils.isInEventLoop()) {
      AsyncUtils.waitQuietly(future);
    }

    return future;
  }
}
