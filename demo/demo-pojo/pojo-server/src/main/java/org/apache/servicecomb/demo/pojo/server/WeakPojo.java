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

package org.apache.servicecomb.demo.pojo.server;

import java.util.List;

import org.apache.servicecomb.demo.server.GenericsModel;
import org.apache.servicecomb.provider.pojo.RpcSchema;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RpcSchema(schemaId = "WeakPojo")
public class WeakPojo {
  @ApiOperation(value = "differentName", nickname = "differentName")
  public int diffNames(@ApiParam(name = "x") int a, @ApiParam(name = "y") int b) {
    return a * 2 + b;
  }

  public List<List<String>> genericParams(int code, List<List<String>> names) {
    return names;
  }

  public GenericsModel genericParamsModel(int code, GenericsModel model) {
    return model;
  }

  public Object obj(Object obj) {
    return obj;
  }
}
