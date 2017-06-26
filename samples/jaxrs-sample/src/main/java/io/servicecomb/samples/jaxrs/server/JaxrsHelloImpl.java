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

package io.servicecomb.samples.jaxrs.server;


import io.servicecomb.provider.pojo.RpcSchema;
import io.servicecomb.samples.jaxrs.Hello;
import io.servicecomb.samples.jaxrs.models.Person;


import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.awt.*;

@RpcSchema(schemaId = "jaxrsHello")
@Path("/jaxrshello")
@Produces(MediaType.APPLICATION_JSON)
public class JaxrsHelloImpl implements Hello {

    @Path("/sayhi")
    @POST
    @Override
    public String sayHi(String name) {
        return "Hello " + name;
    }

    @Path("/sayhello")
    @POST
    @Override
    public String sayHello(Person person) {
        return "Hello person " + person.getName();
    }

}
