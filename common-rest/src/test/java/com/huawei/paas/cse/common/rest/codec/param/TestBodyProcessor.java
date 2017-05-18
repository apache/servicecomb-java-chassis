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

package com.huawei.paas.cse.common.rest.codec.param;

import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.huawei.paas.cse.common.rest.codec.RestServerRequest;
import com.huawei.paas.foundation.vertx.stream.BufferInputStream;

import io.vertx.core.buffer.Buffer;

public class TestBodyProcessor {
    private static RestServerRequest request;

    private static ParamValueProcessor bodyProcessor;

    @Before
    public void beforeTest() {
        request = Mockito.mock(RestServerRequest.class);
    }

    @Test
    public void testString() throws Exception {
        BodyProcessorCreator bodyCreator =
            (BodyProcessorCreator) ParamValueProcessorCreatorManager.INSTANCE.getBodyProcessorCreater();
        bodyProcessor = bodyCreator.create(false, String.class);

        Buffer buffer = Buffer.buffer("\"abc\"");
        when(request.getBody()).thenReturn(new BufferInputStream(buffer.getByteBuf()));

        String result = (String) bodyProcessor.getValue(request);
        Assert.assertEquals("abc", result);
    }

}

