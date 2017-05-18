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

package com.huawei.paas.cse.transport.rest.servlet;

import javax.servlet.http.HttpServletRequest;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.huawei.paas.cse.core.Invocation;

public class TestServletHttpRequestCreator {

    @Test
    public void testCreateMockParam() {
        boolean status = true;
        try {
            HttpServletRequest httpRequest = Mockito.mock(HttpServletRequest.class);
            ProducerServletHttpRequestArgMapper servletHttpRequestCreator =
                new ProducerServletHttpRequestArgMapper(httpRequest);
            Invocation invocation = Mockito.mock(Invocation.class);
            servletHttpRequestCreator.createContextArg(invocation);
        } catch (Exception ex) {
            status = false;
        }
        Assert.assertTrue(status);
    }
}
