/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.common.rest;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.servicecomb.common.rest.codec.RestCodec;
import io.servicecomb.common.rest.codec.RestServerRequestInternal;
import io.servicecomb.common.rest.codec.produce.ProduceProcessor;
import io.servicecomb.common.rest.codec.produce.ProduceProcessorManager;
import io.servicecomb.common.rest.definition.RestOperationMeta;
import io.servicecomb.common.rest.locator.OperationLocator;
import io.servicecomb.common.rest.locator.ServicePathManager;
import com.huawei.paas.cse.core.Const;
import com.huawei.paas.cse.core.CseContext;
import com.huawei.paas.cse.core.Invocation;
import com.huawei.paas.cse.core.Response;
import com.huawei.paas.cse.core.Transport;
import com.huawei.paas.cse.core.definition.MicroserviceMeta;
import com.huawei.paas.cse.core.definition.OperationMeta;
import com.huawei.paas.cse.core.exception.InvocationException;
import com.huawei.paas.cse.core.invocation.InvocationFactory;
import com.huawei.paas.cse.serviceregistry.RegistryUtils;
import com.huawei.paas.foundation.common.utils.JsonUtils;

/**
 * <一句话功能简述>
 * <功能详细描述>
 * @author   
 * @version  [版本号, 2017年1月9日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public abstract class AbstractRestServer<HTTP_RESPONSE> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractRestServer.class);

    // 所属的Transport
    protected Transport transport;

    public void setTransport(Transport transport) {
        this.transport = transport;
    }

    protected void setContext(Invocation invocation, RestServerRequestInternal restRequest) throws Exception {
        String strCseContext = restRequest.getHeaderParam(Const.CSE_CONTEXT);
        if (StringUtils.isEmpty(strCseContext)) {
            return;
        }
        @SuppressWarnings("unchecked")
        Map<String, String> cseContext =
            JsonUtils.readValue(strCseContext.getBytes(StandardCharsets.UTF_8), Map.class);
        invocation.setContext(cseContext);
    }

    protected void handleRequest(RestServerRequestInternal restRequest, HTTP_RESPONSE httpResponse) {
        try {
            RestOperationMeta restOperation = findRestOperation(restRequest);
            OperationMeta operationMeta = restOperation.getOperationMeta();

            operationMeta.getExecutor().execute(() -> {
                try {
                    runOnExecutor(restRequest, restOperation, httpResponse);
                } catch (Exception e) {
                    LOGGER.error("rest server onRequest error", e);
                    sendFailResponse(restRequest, httpResponse, e);
                }
            });
        } catch (Exception e) {
            LOGGER.error("rest server onRequest error", e);
            sendFailResponse(restRequest, httpResponse, e);
        }
    }

    protected void runOnExecutor(RestServerRequestInternal restRequest, RestOperationMeta restOperation,
            HTTP_RESPONSE httpResponse) throws Exception {
        String acceptType = restRequest.getHeaderParam("Accept");
        ProduceProcessor produceProcessor =
            locateProduceProcessor(restRequest, httpResponse, restOperation, acceptType);
        if (produceProcessor == null) {
            // locateProduceProcessor内部已经应答了
            return;
        }

        Object[] args = RestCodec.restToArgs(restRequest, restOperation);
        Invocation invocation =
            InvocationFactory.forProvider(transport.getEndpoint(),
                    restOperation.getOperationMeta(),
                    args);

        this.setContext(invocation, restRequest);
        this.setHttpRequestContext(invocation, restRequest);

        invocation.next(resp -> {
            sendResponse(restRequest, httpResponse, produceProcessor, resp);
        });
    }

    protected RestOperationMeta findRestOperation(RestServerRequestInternal restRequest) {
        String selfName = RegistryUtils.getMicroservice().getServiceName();
        MicroserviceMeta selfMicroserviceMeta =
            CseContext.getInstance().getMicroserviceMetaManager().ensureFindValue(selfName);
        ServicePathManager servicePathManager = ServicePathManager.getServicePathManager(selfMicroserviceMeta);
        if (servicePathManager == null) {
            LOGGER.error("No schema in microservice");
            throw new InvocationException(Status.NOT_FOUND, Status.NOT_FOUND.getReasonPhrase());
        }

        OperationLocator locator = servicePathManager.locateOperation(restRequest.getPath(), restRequest.getMethod());
        restRequest.setPathParamMap(locator.getPathVarMap());

        return locator.getOperation();
    }

    /**
     * 找不到processor，则已经完成了应答，外界不必再处理
     * @param httpResponse
     * @param restOperation
     * @param contentType
     * @return
     */
    protected ProduceProcessor locateProduceProcessor(RestServerRequestInternal restRequest,
            HTTP_RESPONSE httpResponse,
            RestOperationMeta restOperation, String acceptType) {
        ProduceProcessor produceProcessor = restOperation.ensureFindProduceProcessor(acceptType);
        if (produceProcessor != null) {
            return produceProcessor;
        }

        String msg = String.format("Accept %s is not supported", acceptType);
        InvocationException exception = new InvocationException(Status.NOT_ACCEPTABLE, msg);
        sendFailResponse(restRequest, httpResponse, exception);
        return null;
    }

    public void sendFailResponse(RestServerRequestInternal restRequest, HTTP_RESPONSE httpResponse,
            Throwable throwable) {
        Response response = Response.createProducerFail(throwable);
        sendResponse(restRequest, httpResponse, ProduceProcessorManager.DEFAULT_PROCESSOR, response);
    }

    /**
     * 成功、失败的统一应答处理，这里不能再出异常了，再出了异常也没办法处理
     * @param httpServerResponse
     * @param produceProcessor
     * @param statusCode
     * @param reasonPhrase
     * @param errorData
     */
    protected void sendResponse(RestServerRequestInternal restRequest, HTTP_RESPONSE httpServerResponse,
            ProduceProcessor produceProcessor, Response response) {
        try {
            doSendResponse(httpServerResponse, produceProcessor, response);
        } catch (Throwable e) {
            // 这只能是bug，没有办法再兜底了，只能记录日志
            // 如果统一处理为500错误，也无法确定swagger中500对应的数据模型
            // 并且本次调用本身可能就是500进来的
            LOGGER.error("send response failed.", e);
        } finally {
            if (restRequest != null) {
                restRequest.complete();
            }
        }
    }

    /**
     * 成功、失败的统一应答处理
     * @param httpServerResponse
     * @param produceProcessor
     * @param response
     * @throws Exception
     */
    protected abstract void doSendResponse(HTTP_RESPONSE httpServerResponse, ProduceProcessor produceProcessor,
            Response response) throws Exception;

    /**
     * 将http request注入到invocation的handler context
     * @param invocation
     */
    protected abstract void setHttpRequestContext(Invocation invocation, RestServerRequestInternal restRequest);
}
