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

package io.servicecomb.demo.springmvc.client;

import io.servicecomb.demo.CodeFirstRestTemplate;
import io.servicecomb.demo.TestMgr;
import io.servicecomb.demo.server.User;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import io.servicecomb.core.Response;
import io.servicecomb.provider.pojo.RpcReference;

/**
 * <一句话功能简述>
 * <功能详细描述>
 *
 *
 * @version  [版本号, 2017年4月10日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
@Component
public class CodeFirstRestTemplateSpringmvc extends CodeFirstRestTemplate {

    @RpcReference(microserviceName = "springmvc", schemaId = "codeFirst")
    private CodeFirstSprigmvcIntf intf;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void testExtend(RestTemplate template, String cseUrlPrefix) {
        super.testExtend(template, cseUrlPrefix);

        testIntf();
        testResponseEntity("springmvc", template, cseUrlPrefix);
    }

    private void testIntf() {
        ResponseEntity<User> responseEntity = intf.responseEntity();
        TestMgr.check("User [name=nameA, age=100, index=0]", responseEntity.getBody());
        TestMgr.check("h1v", responseEntity.getHeaders().getFirst("h1"));
        TestMgr.check("h2v", responseEntity.getHeaders().getFirst("h2"));

        checkStatusCode("springmvc", 202, responseEntity.getStatusCode());

        Response cseResponse = intf.cseResponse();
        TestMgr.check("User [name=nameA, age=100, index=0]", cseResponse.getResult());
        TestMgr.check("h1v", cseResponse.getHeaders().getFirst("h1"));
        TestMgr.check("h2v", cseResponse.getHeaders().getFirst("h2"));
    }

    private void testResponseEntity(String microserviceName, RestTemplate template, String cseUrlPrefix) {
        ResponseEntity<User> responseEntity =
            template.exchange(cseUrlPrefix + "responseEntity", HttpMethod.GET, null, User.class);
        TestMgr.check("User [name=nameA, age=100, index=0]", responseEntity.getBody());
        TestMgr.check("h1v", responseEntity.getHeaders().getFirst("h1"));
        TestMgr.check("h2v", responseEntity.getHeaders().getFirst("h2"));
        checkStatusCode(microserviceName, 202, responseEntity.getStatusCode());
    }
}
