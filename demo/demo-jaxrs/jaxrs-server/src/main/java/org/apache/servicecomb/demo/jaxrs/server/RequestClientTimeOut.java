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

package org.apache.servicecomb.demo.jaxrs.server;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.servicecomb.demo.validator.Student;
import org.apache.servicecomb.provider.rest.common.RestSchema;


@RestSchema(schemaId = "clientreqtimeout")
@Path("/clientreqtimeout")
@Produces(MediaType.APPLICATION_JSON)
public class RequestClientTimeOut {

  @Path("/add")
  @POST
  public int add(@FormParam("a") int a, @FormParam("b") int b) throws InterruptedException {
    Thread.sleep(2000);
    return a + b;
  }

  @Path("/sayhello")
  @POST
  public Student sayHello(Student student) throws InterruptedException {
    student.setName("hello " + student.getName());
    student.setAge(student.getAge());
    return student;
  }
}
