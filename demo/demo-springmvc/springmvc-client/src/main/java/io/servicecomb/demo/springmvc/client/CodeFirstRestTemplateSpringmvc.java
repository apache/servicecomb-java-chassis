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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import io.servicecomb.demo.CodeFirstRestTemplate;
import io.servicecomb.demo.TestMgr;
import io.servicecomb.provider.pojo.RpcReference;
import io.servicecomb.provider.springmvc.reference.CseHttpEntity;
import io.servicecomb.serviceregistry.RegistryUtils;
import io.servicecomb.swagger.invocation.Response;

@Component
public class CodeFirstRestTemplateSpringmvc extends CodeFirstRestTemplate {

    @RpcReference(microserviceName = "springmvc", schemaId = "codeFirst")
    private CodeFirstSprigmvcIntf intf;

    @Override
    protected void testExtend(RestTemplate template, String cseUrlPrefix) {
        super.testExtend(template, cseUrlPrefix);

        testResponseEntity("springmvc", template, cseUrlPrefix);
        testIntf();
    }

    private void testIntf() {
        Date date = new Date();

        String srcName = RegistryUtils.getMicroserviceManager().getDefaultMicroserviceForce().getServiceName();

        ResponseEntity<Date> responseEntity = intf.responseEntity(date);
        TestMgr.check(date, responseEntity.getBody());
        TestMgr.check("h1v {x-cse-src-microservice=" + srcName + "}", responseEntity.getHeaders().getFirst("h1"));
        TestMgr.check("h2v {x-cse-src-microservice=" + srcName + "}", responseEntity.getHeaders().getFirst("h2"));

        checkStatusCode("springmvc", 202, responseEntity.getStatusCode());

        Response cseResponse = intf.cseResponse();
        TestMgr.check("User [name=nameA, age=100, index=0]", cseResponse.getResult());
        TestMgr.check("h1v {x-cse-src-microservice=" + srcName + "}", cseResponse.getHeaders().getFirst("h1"));
        TestMgr.check("h2v {x-cse-src-microservice=" + srcName + "}", cseResponse.getHeaders().getFirst("h2"));
    }

    private void testResponseEntity(String microserviceName, RestTemplate template, String cseUrlPrefix) {
        Map<String, Object> body = new HashMap<>();
        Date date = new Date();
        body.put("date", date);

        CseHttpEntity<Map<String, Object>> httpEntity = new CseHttpEntity<>(body);
        httpEntity.addContext("contextKey", "contextValue");

        String srcName = RegistryUtils.getMicroserviceManager().getDefaultMicroserviceForce().getServiceName();

        ResponseEntity<Date> responseEntity =
            template.exchange(cseUrlPrefix + "responseEntity", HttpMethod.POST, httpEntity, Date.class);
        TestMgr.check(date, responseEntity.getBody());
        TestMgr.check("h1v {contextKey=contextValue, x-cse-src-microservice=" + srcName + "}",
                responseEntity.getHeaders().getFirst("h1"));
        TestMgr.check("h2v {contextKey=contextValue, x-cse-src-microservice=" + srcName + "}",
                responseEntity.getHeaders().getFirst("h2"));
        checkStatusCode(microserviceName, 202, responseEntity.getStatusCode());
    }
}
