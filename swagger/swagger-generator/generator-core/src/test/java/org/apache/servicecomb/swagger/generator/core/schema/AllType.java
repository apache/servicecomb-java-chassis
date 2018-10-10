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

package org.apache.servicecomb.swagger.generator.core.schema;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.servicecomb.foundation.test.scaffolding.model.Color;
import org.apache.servicecomb.foundation.test.scaffolding.model.User;

public class AllType {
  public boolean bValue;

  public byte byteValue;

  public Byte byteObjectValue;

  public short sValue;

  public Short sObjectValue;

  public int iValue;

  public Integer iObjectValue;

  public long lValue;

  public Long lObjectValue;

  public float fValue;

  public Float fObjectValue;

  public double dValue;

  public Double dObjectValue;

  public Color enumValue;

  public char cValue;

  public Character cObjectValue;

  public byte[] bytes;

  public String strValue;

  public Set<String> set;

  public List<User> list;

  public Map<String, User> map;
}
