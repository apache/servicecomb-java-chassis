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

package io.servicecomb.foundation.metrics.performance;

import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author  
 * @since Mar 14, 2017
 * @see 
 */
public class TestPerfStatContext {

    PerfStatContext oPerfStatContext = null;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        oPerfStatContext = new PerfStatContext();
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
        oPerfStatContext = null;
    }

    /**
     * Test Un-Initialized Values
     */
    @Test
    public void testDefaultValues() {
        Assert.assertEquals(0, oPerfStatContext.getMsgCount());
        Assert.assertTrue(oPerfStatContext.getLatency() >= 0);
    }

    /**
     * Test the values after Initialization
     * @throws InterruptedException 
     */
    @Test
    public void testIntializedValues() throws InterruptedException {
        initializeObject(); //Initialize the object.
        TimeUnit.MILLISECONDS.sleep(4);
        Assert.assertEquals(10, oPerfStatContext.getMsgCount());
        Assert.assertTrue(oPerfStatContext.getLatency() > 2);
    }

    /**
     * Initialize the values for the object
     */
    private void initializeObject() {
        oPerfStatContext.setMsgCount(10);

    }

}
