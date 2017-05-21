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

package io.servicecomb.provider.springmvc.reference;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;

import io.netty.handler.codec.http.QueryStringDecoder;
import io.servicecomb.common.rest.RestConst;
import io.servicecomb.common.rest.codec.LocalRestServerRequest;
import io.servicecomb.common.rest.codec.RestServerRequest;
import io.servicecomb.common.rest.definition.RestOperationMeta;
import io.servicecomb.common.rest.definition.RestParam;
import io.servicecomb.common.rest.locator.OperationLocator;
import io.servicecomb.common.rest.locator.ServicePathManager;
import io.servicecomb.core.CseContext;
import io.servicecomb.core.Invocation;
import io.servicecomb.core.Response;
import io.servicecomb.core.definition.MicroserviceMeta;
import io.servicecomb.core.exception.ExceptionFactory;
import io.servicecomb.core.invocation.InvocationFactory;
import io.servicecomb.core.provider.consumer.InvokerUtils;
import io.servicecomb.core.provider.consumer.ReferenceConfig;

/**
 * <一句话功能简述>
 * <功能详细描述>
 * @author   
 * @version  [版本号, 2017年1月14日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public class CseClientHttpRequest extends OutputStream implements ClientHttpRequest {

    private static final Logger LOGGER = LoggerFactory.getLogger(CseClientHttpRequest.class);

    private URI uri;

    private HttpMethod method;

    private HttpHeaders httpHeaders = new HttpHeaders();

    private Object requestBody;

    public CseClientHttpRequest() {
    }

    /**
     * <构造函数>
     * @param params
     * @param body [参数说明]
     */
    public CseClientHttpRequest(URI uri, HttpMethod method) {
        this.uri = uri;
        this.method = method;
    }

    /**
     * 不支持，从outputStream继承，仅仅是为了在CseHttpMessageConverter中，将requestBody保存进来而已
     */
    @Override
    public void write(int b) throws IOException {
        throw new Error("not support");
    }

    /**
     * 对requestBody进行赋值
     * @param requestBody requestBody的新值
     */
    public void setRequestBody(Object requestBody) {
        this.requestBody = requestBody;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HttpMethod getMethod() {
        return method;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URI getURI() {
        return uri;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HttpHeaders getHeaders() {
        return httpHeaders;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OutputStream getBody() throws IOException {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ClientHttpResponse execute() throws IOException {
        RequestMeta requestMeta = createRequestMeta(method.name(), uri);

        QueryStringDecoder queryStringDecoder = new QueryStringDecoder(uri.getRawSchemeSpecificPart());
        Map<String, List<String>> queryParams = queryStringDecoder.parameters();

        Object[] args = this.collectArguments(requestMeta, queryParams);

        // 异常流程，直接抛异常出去
        return this.invoke(requestMeta, args);
    }

    /**
     * 处理调用URL
     * URL格式：cse://microserviceName/业务url
     * <功能详细描述>
     * @param httpMetod
     * @param url
     * @return
     */
    private RequestMeta createRequestMeta(String httpMetod, URI uri) {
        String microserviceName = uri.getAuthority();
        ReferenceConfig referenceConfig =
            CseContext.getInstance().getConsumerProviderManager().getReferenceConfig(microserviceName);

        MicroserviceMeta microserviceMeta = referenceConfig.getMicroserviceMeta();
        ServicePathManager servicePathManager = ServicePathManager.getServicePathManager(microserviceMeta);
        if (servicePathManager == null) {
            throw new Error(String.format("no schema defined for %s:%s",
                    microserviceMeta.getAppId(),
                    microserviceMeta.getName()));
        }

        OperationLocator locator = servicePathManager.locateOperation(uri.getPath(), httpMetod);
        RestOperationMeta swaggerRestOperation = locator.getOperation();

        Map<String, String> pathParams = locator.getPathVarMap();
        return new RequestMeta(referenceConfig, swaggerRestOperation, pathParams);
    }

    /**
     * <一句话功能简述>
     * <功能详细描述>
     * @param args
     * @param variables
     * @return
     * @throws Throwable
     */
    private CseClientHttpResponse invoke(RequestMeta requestMeta, Object[] args) {
        Invocation invocation =
            InvocationFactory.forConsumer(requestMeta.getReferenceConfig(),
                    requestMeta.getOperationMeta(),
                    args);
        invocation.getHandlerContext().put(RestConst.REST_CLIENT_REQUEST_PATH,
                this.uri.getPath() + "?" + this.uri.getQuery());
        Response response = InvokerUtils.innerSyncInvoke(invocation);

        if (response.isSuccessed()) {
            return new CseClientHttpResponse(response);
        }

        throw ExceptionFactory.convertConsumerException((Throwable) response.getResult());
    }

    /**
     * 从输入中获取args
     * @return
     * @throws Exception
     */
    private Object[] collectArguments(RequestMeta requestMeta, Map<String, List<String>> queryParams) {
        RestServerRequest mockRequest =
            new LocalRestServerRequest(requestMeta.getPathParams(), queryParams, httpHeaders, requestBody);
        List<RestParam> paramList = requestMeta.getSwaggerRestOperation().getParamList();
        Object[] args = new Object[paramList.size()];
        for (int idx = 0; idx < paramList.size(); idx++) {
            RestParam param = paramList.get(idx);
            try {
                args[idx] = param.getParamProcessor().getValue(mockRequest);
            } catch (Exception e) {
                LOGGER.error("error arguments for operation "
                        + requestMeta.getOperationMeta().getMicroserviceQualifiedName(), e);
                throw new Error(e);
            }
        }

        return args;
    }
}
