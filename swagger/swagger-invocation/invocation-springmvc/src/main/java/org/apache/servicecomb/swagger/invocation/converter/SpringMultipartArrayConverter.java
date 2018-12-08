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
package org.apache.servicecomb.swagger.invocation.converter;

import java.lang.reflect.Type;
import java.util.List;

import javax.servlet.http.Part;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.type.TypeFactory;


@Component
public class SpringMultipartArrayConverter implements CustomizedConverter {

  @Override
  public Type getSrcType() {
    return TypeFactory.defaultInstance().constructCollectionType(List.class, Part.class);
  }

  @Override
  public Type getTargetType() {
    return MultipartFile[].class;
  }

  @Override
  public Object convert(Object value) {
    if (value == null) {
      return null;
    }
    @SuppressWarnings("unchecked")
    List<Part> partList = (List<Part>) value;
    PartToMultipartFile[] partArray = new PartToMultipartFile[partList.size()];
    for (int i = 0; i < partArray.length; i++) {
      partArray[i] = new PartToMultipartFile(partList.get(i));
    }
    return partArray;
  }
}
