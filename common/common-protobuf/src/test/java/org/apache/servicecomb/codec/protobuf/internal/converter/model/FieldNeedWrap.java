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
package org.apache.servicecomb.codec.protobuf.internal.converter.model;

import java.util.List;
import java.util.Map;

import org.apache.servicecomb.foundation.test.scaffolding.model.User;

public class FieldNeedWrap {
  public List<List<User>> listListUser;

  public List<Map<String, User>> listMapUser;

  public Map<String, List<User>> mapListUser;

  public Map<String, Map<String, User>> mapMapUser;

  public List<List<List<User>>> listListListUser;

  public List<List<Map<String, User>>> listListMapUser;

  public List<Map<String, List<User>>> listMapListUser;

  public List<Map<String, Map<String, User>>> listMapMapUser;

  public Map<String, Map<String, List<User>>> mapMapListUser;

  public Map<String, Map<String, Map<String, User>>> mapMapMapUser;
}
