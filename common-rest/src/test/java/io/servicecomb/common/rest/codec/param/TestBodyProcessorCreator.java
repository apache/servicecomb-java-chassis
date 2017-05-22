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

package io.servicecomb.common.rest.codec.param;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestBodyProcessorCreator {
    private static BodyProcessorCreator bodyCreator;

    @Before
    public void beforeTest() {
        bodyCreator = (BodyProcessorCreator) ParamValueProcessorCreatorManager.INSTANCE.getBodyProcessorCreater();
    }

    @Test
    public void testBodyProcessorCreator() throws Exception {
        ParamValueProcessor processor = bodyCreator.create(true, String.class);
        Assert.assertTrue(BodyProcessorCreator.RawJsonBodyProcessor.class.isInstance(processor));

        processor = bodyCreator.create(false, String.class);
        Assert.assertFalse(BodyProcessorCreator.RawJsonBodyProcessor.class.isInstance(processor));

        processor = bodyCreator.create(true, List.class);
        Assert.assertFalse(BodyProcessorCreator.RawJsonBodyProcessor.class.isInstance(processor));
    }

}
