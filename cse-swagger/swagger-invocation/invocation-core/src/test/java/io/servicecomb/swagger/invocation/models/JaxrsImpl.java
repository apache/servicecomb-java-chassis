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

package io.servicecomb.swagger.invocation.models;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.CookieParam;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;

/**
 * <一句话功能简述>
 * <功能详细描述>
 *
 * @author   
 * @version  [版本号, 2017年4月14日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
@Path("/JaxrsImpl")
@Produces(MediaType.APPLICATION_JSON)
public class JaxrsImpl {
    @Path("/path")
    @PUT
    public String path(@PathParam("name") String name) {
        return name;
    }

    @Path("/form")
    @POST
    public int form(@FormParam("a") int a, @FormParam("b") int b) {
        return a + b;
    }

    @Path("/cookie")
    @POST
    public int cookie(@CookieParam("a") int a) {
        return a;
    }

    @Path("/header")
    @POST
    public String header(@HeaderParam("a") String a) {
        return a;
    }

    @Path("/query")
    @DELETE
    @Produces(MediaType.TEXT_PLAIN)
    public String query(@QueryParam("s") List<String> s) {
        return "";
    }

    @Path("/body")
    @POST
    public Person body(Person user) {
        return user;
    }

    @Path("/request")
    @GET
    @ApiImplicitParams({@ApiImplicitParam(name = "a", dataType = "integer", format = "int32", paramType = "query")})
    public int request(HttpServletRequest request) {
        int a = Integer.parseInt(request.getParameter("a"));
        return a;
    }

    @Path("/headerAndBody")
    @POST
    public String headerAndBody(@HeaderParam("a") String a, Person user) {
        return a + " " + user.getName();
    }
}
