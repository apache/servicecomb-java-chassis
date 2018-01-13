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

package org.apache.servicecomb.samples.pojo.provider;


import org.apache.servicecomb.provider.pojo.RpcSchema;
import org.apache.servicecomb.samples.pojo.Compute;

@RpcSchema(schemaId = "codeFirstCompute")
public class CodeFirstComputeImpl implements Compute {
  @Override
  public int add(int a, int b) {
    return a + b;
  }

  @Override
  public int multi(int a, int b) {
    return a * b;
  }

  @Override
  public int sub(int a, int b) {
    return a - b;
  }

  @Override
  public int divide(int a, int b) {
    if (b != 0) {
      return a / b;
    }
    return 0;
  }
}
