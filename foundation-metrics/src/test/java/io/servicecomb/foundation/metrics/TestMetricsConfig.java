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

package io.servicecomb.foundation.metrics;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.servicecomb.foundation.metrics.MetricsConfig;

/**
 * @author  
 * @since Mar 14, 2017
 * @see 
 */
public class TestMetricsConfig {

    MetricsConfig oMetricsConfig = null;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        oMetricsConfig = new MetricsConfig();
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
        oMetricsConfig = null;
    }

    /**
     * Test com.huawei.paas.foundation.metrics.MetricsConfig.getMsCycle()
     * com.huawei.paas.foundation.metrics.MetricsConfig.isEnable()
     * com.huawei.paas.foundation.metrics.MetricsConfig.afterPropertiesSet() This cannot be tested as it will start original Thread
     */
    @Test
    public void test() {
        Assert.assertEquals(60000, MetricsConfig.getMsCycle());
        Assert.assertEquals(true, MetricsConfig.isEnable());
    }

}
