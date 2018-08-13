package org.apache.servicecomb.it.testcase.support;/*
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

public interface DataTypeRestIntf {
  String checkTransport();

  int intPath(int input);

  int intQuery(int input);

  int intHeader(int input);

  int intCookie(int input);

  int intBody(int input);

  int intForm(int a);

  int intAttribute(int a);

  int intAdd(int a, int b);

  int intPostAdd(int a, int b);

  int defaultPath();

  int intPathWithMinMax(int input);

  int intQueryWithMinMax(int input);

  int intHeaderWithMinMax(int input);

  int intCookieWithMinMax(int input);

  int intFormWithMinMax(int input);

  int intAttributeWithMinMax(int input);

  int intBodyWithMinMax(int input);

  String intMulti(int a, int b, int c, int d, int e);
}
