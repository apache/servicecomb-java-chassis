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

package io.servicecomb.samples.codefirst.server;


import io.servicecomb.provider.pojo.RpcSchema;
import io.servicecomb.samples.codefirst.Hello;
import io.servicecomb.samples.codefirst.models.Person;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@RpcSchema(schemaId = "codeFirstJaxrsHello")
@Path("/codefirstjaxrshello")
@Produces(MediaType.APPLICATION_JSON)
public class CodeFIrstJaxrsHelloImpl implements Hello {

    @Path("/sayhi")
    @POST
    @Override
    public String sayHi(String name) {
        return "Jaxrs Hello " + name;
    }

    @Path("/sayhello")
    @POST
    @Override
    public String sayHello(Person person) {
        return "Jaxrs Hello person " + person.getName();
    }

}
