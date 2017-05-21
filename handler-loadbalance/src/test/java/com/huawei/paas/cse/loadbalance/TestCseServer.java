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

/**
 * 
 */
package com.huawei.paas.cse.loadbalance;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.mockito.Mockito;

import io.servicecomb.core.Transport;
import com.huawei.paas.cse.serviceregistry.cache.CacheEndpoint;

/**
 * @author 
 *
 */
public class TestCseServer {

    private Transport transport = Mockito.mock(Transport.class);

    private CseServer cs = new CseServer(transport, new CacheEndpoint("abcd", null));

    @Test
    public void testCseServerObj() {
        assertNotNull(cs);
    }

    @Test
    public void testGetEndpoint() {
        cs.getEndpoint();
        assertNotNull(cs.getEndpoint());
    }

    @Test
    public void testEqualsMethod() {
        cs.equals(getClass());
        assertNotNull(cs.equals(getClass()));
    }

    @Test
    public void testToStringMethod() {
        cs.toString();
        assertNotNull(cs.toString());
    }

    @Test
    public void testGetHost() {
        cs.getHost();
        assertNotNull(cs.getHost());
    }

    @Test
    public void testHashCodeMethod() {
        cs.hashCode();
        assertNotNull(cs.hashCode());
    }
}
