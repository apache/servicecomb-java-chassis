/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.servicecomb.demo.pojo.client;

import org.apache.servicecomb.demo.CategorizedTestCase;
import org.apache.servicecomb.demo.TestMgr;
import org.apache.servicecomb.demo.server.TestRequest;
import org.apache.servicecomb.demo.server.User;
import org.apache.servicecomb.provider.pojo.RpcReference;
import org.springframework.stereotype.Component;

@Component
public class TestClientInterfaceForServerTest implements CategorizedTestCase {
    @RpcReference(microserviceName = "pojo", schemaId = "server")
    private ClientInterfaceForServerTest pojo;

    @Override
    public void testAllTransport() throws Exception {
        User user = new User();
        user.setName("name");
        user.setIndex(100);
        User result = pojo.splitParam(100, user);
        TestMgr.check("name,  users count:0", result.getName());
        TestMgr.check(100, result.getIndex());

        TestRequest request = new TestRequest();
        request.setIndex(200);
        request.setUser(user);
        result = pojo.wrapParam(request);
        TestMgr.check("name,  users count:0", result.getName());
        TestMgr.check(200, result.getIndex());
    }
}
