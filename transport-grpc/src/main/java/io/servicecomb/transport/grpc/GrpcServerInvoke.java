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

package io.servicecomb.transport.grpc;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;

import io.netty.channel.ChannelHandlerContext;
import io.servicecomb.codec.protobuf.definition.OperationProtobuf;
import io.servicecomb.codec.protobuf.definition.ProtobufManager;
import io.servicecomb.core.Const;
import io.servicecomb.core.CseContext;
import io.servicecomb.core.Invocation;
import io.servicecomb.core.Response;
import io.servicecomb.core.definition.MicroserviceMeta;
import io.servicecomb.core.definition.MicroserviceMetaManager;
import io.servicecomb.core.definition.OperationMeta;
import io.servicecomb.core.definition.SchemaMeta;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.http.impl.Http2ServerResponseImpl;
import io.vertx.ext.web.RoutingContext;

/**
 * <一句话功能简述>
 * <功能详细描述>
 * @author   
 * @version  [版本号, 2017年2月21日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public class GrpcServerInvoke {
    private static final Logger LOGGER = LoggerFactory.getLogger(GrpcServerInvoke.class);

    // response中的ctx，用于规避vertx http2.0在应答trailer时，没有flush的bug
    private static Field ctxfield;
    static {
        ctxfield = ReflectionUtils.findField(Http2ServerResponseImpl.class, "ctx");
        ctxfield.setAccessible(true);
    }

    private MicroserviceMetaManager microserviceMetaManager = CseContext.getInstance().getMicroserviceMetaManager();

    private RoutingContext routingContext;

    private OperationMeta operationMeta;

    private OperationProtobuf operationProtobuf;

    public void setMicroserviceMetaManager(MicroserviceMetaManager microserviceMetaManager) {
        this.microserviceMetaManager = microserviceMetaManager;
    }

    public void init(RoutingContext routingContext) {
        try {
            doInit(routingContext);
        } catch (Exception e) {
            String microserviceQualifidName = "unknown";
            if (operationMeta != null) {
                microserviceQualifidName = operationMeta.getMicroserviceQualifiedName();
            }
            String msg = String.format("decode request error, microserviceQualifidName=%s", microserviceQualifidName);
            LOGGER.error(msg, e);
        }
    }

    private void doInit(RoutingContext routingContext) throws Exception {
        String schemaId = routingContext.pathParam("schema");
        String operationName = routingContext.pathParam("operation");

        MicroserviceMeta microserviceMeta =
            microserviceMetaManager.ensureFindValue(routingContext.request().getHeader(Const.DEST_MICROSERVICE));
        SchemaMeta schemaMeta = microserviceMeta.ensureFindSchemaMeta(schemaId);

        this.routingContext = routingContext;
        this.operationMeta = schemaMeta.ensureFindOperation(operationName);
        this.operationProtobuf = ProtobufManager.getOrCreateOperation(operationMeta);
    }

    public void runInExecutor() {
        try {
            Invocation invocation = GrpcCodec.decodeRequest(routingContext, operationMeta);

            invocation.next(response -> {
                onProviderResponse(invocation, response);
            });
        } catch (Throwable e) {
            LOGGER.error("grpc server onrequest error", e);
            sendFailResponse(e);
        }
    }

    public void execute() {
        operationMeta.getExecutor().execute(this::runInExecutor);
    }

    private void onProviderResponse(Invocation invocation, Response response) {
        routingContext.response().putHeader("content-type", "application/grpc");

        if (response.getHeaders().getHeaderMap() != null) {
            for (Entry<String, List<Object>> entry : response.getHeaders().getHeaderMap().entrySet()) {
                for (Object value : entry.getValue()) {
                    routingContext.response().putHeader(entry.getKey(), String.valueOf(value));
                }
            }
        }

        if (response.isSuccessed()) {
            sendSuccessResponse(invocation, response);
            return;
        }

        sendFailResponse(response.getResult());
    }

    private void sendSuccessResponse(Invocation invocation, Response response) {
        Buffer buffer = null;
        try {
            buffer = GrpcCodec.encodeResponse(invocation, response, operationProtobuf);
        } catch (Exception e) {
            LOGGER.error("grpc encode success response failed.", e);
            sendFailResponse(e);
            return;
        }

        HttpServerResponse httpServerResponse = routingContext.response();
        httpServerResponse.putTrailer("grpc-status", "0");
        httpServerResponse.end(buffer);

        ChannelHandlerContext ctx = (ChannelHandlerContext) ReflectionUtils.getField(ctxfield, httpServerResponse);
        ctx.flush();
    }

    /**
     * <一句话功能简述>
     * <功能详细描述>
     */
    private void sendFailResponse(Throwable throwable) {
        // TODO:如果exception.getErrorData是protobuf不支持的类型，怎么包装？
        //        InvocationException exception = ExceptionUtils.convertException(throwable);

        routingContext.response().putTrailer("grpc-status", "0");
        //        routingContext.response().end(buffer);
    }
}
