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

package org.apache.servicecomb.demo.springmvc.client;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.apache.servicecomb.demo.CategorizedTestCase;
import org.apache.servicecomb.demo.TestMgr;
import org.apache.servicecomb.provider.pojo.RpcReference;
import org.springframework.stereotype.Component;

@Component
public class TestBigNumberSchema implements CategorizedTestCase {
  interface IBigNumberSchema {
    BigInteger bigInteger(BigInteger intHeader, BigInteger intQuery, BigInteger intForm);

    BigDecimal bigDecimal(BigDecimal decimalHeader, BigDecimal decimalQuery, BigDecimal decimalForm);
  }

  @RpcReference(microserviceName = "springmvc", schemaId = "BigNumberSchema")
  private IBigNumberSchema schema;

  @Override
  public void testAllTransport() throws Exception {
    testBigInteger();
    testBigDecimal();
  }

  public void testBigInteger() {
    BigInteger result = schema.bigInteger(BigInteger.valueOf(100L), BigInteger.valueOf(3000000000000000000L),
        BigInteger.valueOf(200L));
    TestMgr.check("3000000000000000300", result.toString());
  }

  public void testBigDecimal() {
    BigDecimal a = BigDecimal.valueOf(100.1D);
    BigDecimal b = BigDecimal.valueOf(300000000000000000.1D);
    BigDecimal c = BigDecimal.valueOf(200.1D);
    BigDecimal expected = a.add(b).add(c);
    BigDecimal result = schema.bigDecimal(a, b, c);
    TestMgr.check(expected, result);
  }
}
