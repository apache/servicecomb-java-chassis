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
package com.huawei.paas.cse.transport.highway;

import java.net.InetSocketAddress;

import javax.xml.ws.Holder;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.servicecomb.codec.protobuf.utils.ProtobufSchemaUtils;
import io.servicecomb.codec.protobuf.utils.WrapSchema;
import com.huawei.paas.cse.transport.highway.message.LoginRequest;
import com.huawei.paas.cse.transport.highway.message.RequestHeader;
import io.servicecomb.foundation.vertx.stream.BufferOutputStream;

import io.protostuff.LinkedBuffer;
import io.protostuff.ProtobufOutput;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;
import io.vertx.core.net.impl.SocketAddressImpl;
import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;

/**
 * <一句话功能简述>
 * <功能详细描述>
 * @author
 * @version  [版本号, 2017年5月8日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public class TestHighwayServerConnection {
    private static WrapSchema requestHeaderSchema =
        ProtobufSchemaUtils.getOrCreateSchema(RequestHeader.class);

    private static WrapSchema setParameterRequestSchema =
        ProtobufSchemaUtils.getOrCreateSchema(LoginRequest.class);

    HighwayServerConnection connection;

    @Mocked
    NetSocket netSocket;

    RequestHeader header = new RequestHeader();

    @Before
    public void init() {
        new Expectations() {
            {
                netSocket.remoteAddress();
                result = new SocketAddressImpl(new InetSocketAddress("127.0.0.1", 80));
            }
        };
        connection = new HighwayServerConnection();
        connection.init(netSocket);

        header = new RequestHeader();
    }

    @Test
    public void testInvalidMsgType() throws Exception {
        header.setMsgType((byte) 100);
        Buffer headerBuffer = createBuffer(requestHeaderSchema, header);

        try {
            connection.handle(0, headerBuffer, null);
            throw new Error("must error");
        } catch (Throwable e) {
            Assert.assertEquals("Unknown tcp msgType 100", e.getMessage());
        }
    }

    @Test
    public void testReqeustHeaderError() throws Exception {
        header.setMsgType(MsgType.LOGIN);
        Buffer headerBuffer = createBuffer(requestHeaderSchema, header);

        headerBuffer.setByte(0, (byte) 100);

        connection.handle(0, headerBuffer, null);

        Assert.assertEquals(null, connection.getProtocol());
        Assert.assertEquals(null, connection.getZipName());
    }

    @Test
    public void testSetParameterNormal() throws Exception {
        header.setMsgType(MsgType.LOGIN);
        Buffer headerBuffer = createBuffer(requestHeaderSchema, header);

        LoginRequest body = new LoginRequest();
        body.setProtocol("p");
        body.setZipName("z");
        Buffer bodyBuffer = createBuffer(setParameterRequestSchema, body);

        connection.handle(0, headerBuffer, bodyBuffer);

        Assert.assertEquals("p", connection.getProtocol());
        Assert.assertEquals("z", connection.getZipName());
    }

    @Test
    public void testSetParameterError() throws Exception {
        header.setMsgType(MsgType.LOGIN);
        Buffer headerBuffer = createBuffer(requestHeaderSchema, header);

        LoginRequest body = new LoginRequest();
        body.setProtocol("p");
        body.setZipName("z");
        Buffer bodyBuffer = createBuffer(setParameterRequestSchema, body);
        bodyBuffer.setByte(0, (byte) 100);

        connection.handle(0, headerBuffer, bodyBuffer);

        Assert.assertEquals(null, connection.getProtocol());
        Assert.assertEquals(null, connection.getZipName());
    }

    @Test
    public void testRequestNormal() throws Exception {
        header.setMsgType(MsgType.REQUEST);
        Buffer headerBuffer = createBuffer(requestHeaderSchema, header);

        Buffer bodyBuffer = Buffer.buffer();

        Holder<Boolean> holder = new Holder<>();
        new MockUp<HighwayServerInvoke>() {
            @Mock
            public boolean init(NetSocket netSocket, long msgId,
                    RequestHeader header, Buffer bodyBuffer) {
                return true;
            }

            @Mock
            public void execute() {
                holder.value = true;
            }
        };

        connection.handle(0, headerBuffer, bodyBuffer);

        Assert.assertEquals(null, connection.getProtocol());
        Assert.assertEquals(null, connection.getZipName());
        Assert.assertEquals(true, holder.value);
    }

    @Test
    public void testRequestError() throws Exception {
        header.setMsgType(MsgType.REQUEST);
        Buffer headerBuffer = createBuffer(requestHeaderSchema, header);

        Buffer bodyBuffer = Buffer.buffer();

        Holder<Boolean> holder = new Holder<>(false);
        new MockUp<HighwayServerInvoke>() {
            @Mock
            public boolean init(NetSocket netSocket, long msgId,
                    RequestHeader header, Buffer bodyBuffer) {
                return false;
            }
        };

        connection.handle(0, headerBuffer, bodyBuffer);

        Assert.assertEquals(null, connection.getProtocol());
        Assert.assertEquals(null, connection.getZipName());
        Assert.assertEquals(false, holder.value);
    }

    protected Buffer createBuffer(WrapSchema schema, Object value) throws Exception {
        Buffer headerBuffer;
        LinkedBuffer linkedBuffer = LinkedBuffer.allocate();
        ProtobufOutput output = new ProtobufOutput(linkedBuffer);
        schema.writeObject(output, value);
        try (BufferOutputStream os = new BufferOutputStream()) {
            LinkedBuffer.writeTo(os, linkedBuffer);

            headerBuffer = os.getBuffer();
        }
        return headerBuffer;
    }
}
