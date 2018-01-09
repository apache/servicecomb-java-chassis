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

package io.servicecomb.common.rest.codec.param;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import io.servicecomb.common.rest.codec.RestClientRequest;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.QueryParameter;

public class QueryProcessorCreator implements ParamValueProcessorCreator {

  public static final String PARAMTYPE = "query";

  public QueryProcessorCreator() {
    ParamValueProcessorCreatorManager.INSTANCE.register(PARAMTYPE, this);
  }

  @Override
  public ParamValueProcessor create(Parameter parameter, Type genericParamType) {
    JavaType targetType = TypeFactory.defaultInstance().constructType(genericParamType);

    String collectionFormat = ((QueryParameter) parameter).getCollectionFormat();

    if (collectionFormat == null) {
      return new QueryProcessor(parameter.getName(), targetType);
    } else if (collectionFormat.equals("csv")) {
      return new CsvQueryProcessor(parameter.getName(), targetType);
    } else if (collectionFormat.equals("multi")) {
      return new MultiQueryProcessor(parameter.getName(), targetType);
    }

    return new QueryProcessor(parameter.getName(), targetType);
  }


  public static class QueryProcessor extends AbstractParamProcessor {
    public QueryProcessor(String paramPath, JavaType targetType) {
      super(paramPath, targetType);
    }

    @Override
    public Object getValue(HttpServletRequest request) throws Exception {
      Object value = null;
      if (targetType.isContainerType()) {
        value = request.getParameterValues(paramPath);
      } else {
        value = request.getParameter(paramPath);
      }

      return convertValue(value, targetType);
    }

    @Override
    public void setValue(RestClientRequest clientRequest, Object arg) throws Exception {
      // query不需要set
    }

    @Override
    public String getProcessorType() {
      return PARAMTYPE;
    }
  }


  public static class MultiQueryProcessor extends AbstractParamProcessor {
    public MultiQueryProcessor(String paramPath, JavaType targetType) {
      super(paramPath, targetType);
    }

    @Override
    public Object getValue(HttpServletRequest request) throws Exception {
      Object value = null;
      if (targetType.isContainerType()) {
        value = request.getParameterValues(paramPath);
      } else {
        value = request.getParameter(paramPath);
      }

      return convertValue(value, targetType);
    }

    @Override
    public void setValue(RestClientRequest clientRequest, Object arg) throws Exception {
      // query不需要set
    }

    @Override
    public String getProcessorType() {
      return PARAMTYPE;
    }
  }

  public static class CsvQueryProcessor extends AbstractParamProcessor {


    public CsvQueryProcessor(String paramPath, JavaType targetType) {
      super(paramPath, targetType);
    }

    @Override
    public Object getValue(HttpServletRequest request) throws Exception {
      Object value = null;

      if (targetType.isContainerType()) {
        String[] strs = request.getParameterValues(paramPath);
        List<String> asList = new ArrayList<>();
        for (String str : strs) {
          asList.addAll(Arrays.asList(str.split(",")));
        }
        value = asList.toArray();
      } else {
        value = request.getParameter(paramPath);
      }

      return convertValue(value, targetType);
    }

    @Override
    public void setValue(RestClientRequest clientRequest, Object arg) throws Exception {

    }

    @Override
    public String getProcessorType() {
      return PARAMTYPE;
    }
  }
}