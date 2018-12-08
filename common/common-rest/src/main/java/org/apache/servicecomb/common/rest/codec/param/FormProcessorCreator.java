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

package org.apache.servicecomb.common.rest.codec.param;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;
import javax.ws.rs.core.Response.Status;

import org.apache.servicecomb.common.rest.RestConst;
import org.apache.servicecomb.common.rest.codec.RestClientRequest;
import org.apache.servicecomb.foundation.vertx.http.FileUploadPart;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import io.swagger.models.parameters.FormParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.properties.FileProperty;
import io.swagger.models.properties.Property;

public class FormProcessorCreator implements ParamValueProcessorCreator {
  public static final String PARAMTYPE = "formData";

  public static class FormProcessor extends AbstractParamProcessor {
    public FormProcessor(String paramPath, JavaType targetType, Object defaultValue, boolean required) {
      super(paramPath, targetType, defaultValue, required);
    }

    @Override
    public Object getValue(HttpServletRequest request) {
      @SuppressWarnings("unchecked")
      Map<String, Object> forms = (Map<String, Object>) request.getAttribute(RestConst.FORM_PARAMETERS);
      if (forms != null && !forms.isEmpty()) {
        return convertValue(forms.get(paramPath), targetType);
      }

      if (targetType.isContainerType()) {
        //Even if the paramPath does not exist, it won't be null at now
        return convertValue(request.getParameterValues(paramPath), targetType);
      }

      Object value = request.getParameter(paramPath);
      if (value == null) {
        value = checkRequiredAndDefaultValue();
      }

      return convertValue(value, targetType);
    }

    private Object checkRequiredAndDefaultValue() {
      if (isRequired()) {
        throw new InvocationException(Status.BAD_REQUEST, "Parameter is required.");
      }
      return getDefaultValue();
    }

    @Override
    public void setValue(RestClientRequest clientRequest, Object arg) {
      clientRequest.addForm(paramPath, arg);
    }

    @Override
    public String getProcessorType() {
      return PARAMTYPE;
    }
  }

  public FormProcessorCreator() {
    ParamValueProcessorCreatorManager.INSTANCE.register(PARAMTYPE, this);
  }

  @Override
  public ParamValueProcessor create(Parameter parameter, Type genericParamType) {
    JavaType targetType = TypeFactory.defaultInstance().constructType(genericParamType);

    if (isPart(parameter)) {
      return new PartProcessor(parameter.getName(), targetType, ((FormParameter) parameter).getDefaultValue(),
          parameter.getRequired());
    }
    return new FormProcessor(parameter.getName(), targetType, ((FormParameter) parameter).getDefaultValue(),
        parameter.getRequired());
  }

  private boolean isPart(Parameter parameter) {
    //only check
    FormParameter formParameter = (FormParameter) parameter;
    if ("array".equals(formParameter.getType())) {
      Property items = formParameter.getItems();
      if (items != null) {
        return new FileProperty().getType().equals(items.getType());
      }
    }
    return new FileProperty().getType().equals(formParameter.getType());
  }

  public static class PartProcessor extends AbstractParamProcessor {
    PartProcessor(String paramPath, JavaType targetType, Object defaultValue, boolean required) {
      super(paramPath, targetType, defaultValue, required);
    }

    @Override
    public Object getValue(HttpServletRequest request) throws Exception {

      if (targetType.getRawClass().equals(List.class)) {
        JavaType contentType = targetType.getContentType();
        if (contentType != null && contentType.getRawClass().equals(Part.class)) {
          //get all parts
          Collection<Part> parts = request.getParts();
          List<Part> collect = parts.stream().filter(part -> part.getName().equals(paramPath))
              .collect(Collectors.toList());
          return collect;
        }
      }
      return request.getPart(paramPath);
    }

    @Override
    public void setValue(RestClientRequest clientRequest, Object arg) throws Exception {
      clientRequest.attach(paramPath, arg);
    }

    @Override
    public String getProcessorType() {
      return PARAMTYPE;
    }
  }
}
