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

package io.servicecomb.codec.protobuf.utils;

import org.junit.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import io.servicecomb.codec.protobuf.definition.ProtobufManager;
import io.servicecomb.codec.protobuf.utils.ProtobufSchemaUtils;
import io.servicecomb.codec.protobuf.utils.WrapSchema;
import io.servicecomb.core.definition.OperationMeta;

public class TestUtils {

    private OperationMeta operationMeta = null;

    @Before
    public void setUp() throws Exception {
        operationMeta = Mockito.mock(OperationMeta.class);
    }

    @After
    public void tearDown() throws Exception {
        operationMeta = null;
    }

    @Test
    public void test() {
        WrapSchema wrapSchema = null;
        try {
            Mockito.when(operationMeta.getMethod()).thenReturn(ProtobufManager.class.getMethods()[0]);
            wrapSchema = ProtobufSchemaUtils.getOrCreateArgsSchema(operationMeta);
            Assert.assertNotNull(wrapSchema);
        } catch (Exception e) {
            Assert.assertEquals(true, (e.getMessage()).contains("failed to wrap class"));
        }
    }
}
