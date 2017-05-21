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

package io.servicecomb.common.rest.definition;

import javax.ws.rs.core.MediaType;

import io.servicecomb.common.rest.codec.produce.ProduceProcessorManager;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.servicecomb.core.definition.OperationMeta;

public class TestRestOperationMeta {

    private RestOperationMeta operationMeta;

    @Before
    public void beforeTest() {
        operationMeta = new RestOperationMeta() {
            @Override
            public void init(OperationMeta operationMeta) {
            }
        };
    }

    @After
    public void afterTest() {

        operationMeta = null;

    }

    @Test
    public void testSplitAcceptTypes() {
        String types = "application/json;charset=utf-8,*/*;q=0.9";
        Assert.assertArrayEquals(new String[] {"application/json", "*/*"}, operationMeta.splitAcceptTypes(types));
    }

    @Test
    public void testContainSpecType() {
        Assert.assertTrue(operationMeta.containSpecType(new String[] {MediaType.WILDCARD}, MediaType.WILDCARD));
        Assert.assertFalse(
                operationMeta.containSpecType(new String[] {MediaType.APPLICATION_JSON}, MediaType.TEXT_PLAIN));
    }

    @Test
    public void testEnsureFindProduceProcessor() {
        operationMeta.setDefaultProcessor(ProduceProcessorManager.JSON_PROCESSOR);

        Assert.assertEquals(operationMeta.getDefaultProcessor(), operationMeta.ensureFindProduceProcessor(""));

        Assert.assertEquals(operationMeta.getDefaultProcessor(),
                operationMeta.ensureFindProduceProcessor(MediaType.WILDCARD));

        operationMeta.ensureFindProduceProcessor("####");
        operationMeta.createProduceProcessors();
        String types = "application/json;charset=utf-8,text/plain;q=0.8";
        Assert.assertEquals(ProduceProcessorManager.JSON_PROCESSOR, operationMeta.ensureFindProduceProcessor(types));

        Assert.assertEquals(null, operationMeta.getParamByName("test"));
        Assert.assertEquals(null, operationMeta.getPathBuilder());
        Assert.assertEquals(null, operationMeta.findProduceProcessor("test"));

    }
}
