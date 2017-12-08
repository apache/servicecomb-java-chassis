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

package io.servicecomb.common.rest.codec.param;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.Part;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import io.servicecomb.common.rest.RestConst;
import io.servicecomb.common.rest.codec.RestClientRequest;
import io.swagger.models.parameters.FormParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.properties.FileProperty;

public class FormProcessorCreator implements ParamValueProcessorCreator {
  public static final String PARAMTYPE = "formData";

  public static class FormProcessor extends AbstractParamProcessor {
    public FormProcessor(String paramPath, JavaType targetType) {
      super(paramPath, targetType);
    }

    @Override
    public Object getValue(HttpServletRequest request) throws Exception {
      @SuppressWarnings("unchecked")
      Map<String, Object> forms = (Map<String, Object>) request.getAttribute(RestConst.FORM_PARAMETERS);
      if (forms != null) {
        return convertValue(forms.get(paramPath), targetType);
      }

      if (targetType.isContainerType()) {
        return convertValue(request.getParameterValues(paramPath), targetType);
      }

      return convertValue(request.getParameter(paramPath), targetType);
    }

    @Override
    public void setValue(RestClientRequest clientRequest, Object arg) throws Exception {
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
      return new PartProcessor(parameter.getName(), TypeFactory.defaultInstance().constructType(String[].class));
    }
    return new FormProcessor(parameter.getName(), targetType);
  }

  private boolean isPart(Parameter parameter) {
    return new FileProperty().getType().equals(((FormParameter) parameter).getType());
  }

  private static class PartProcessor extends AbstractParamProcessor {

    private static final String UPLOADS = "x-cse-uploads-";

    PartProcessor(String paramPath, JavaType targetType) {
      super(paramPath, targetType);
    }

    @Override
    public Object getValue(HttpServletRequest request) throws Exception {
      @SuppressWarnings("unchecked")
      Map<String, Object> forms = (Map<String, Object>) request.getAttribute(RestConst.FORM_PARAMETERS);
      if (forms != null) {
        request.setAttribute(UPLOADS + paramPath, convertValue(forms.get(paramPath), targetType));
        return request;
      }

      return new HttpServletRequestWrapper(request) {
        @Override
        public Part getPart(String name) throws IOException, ServletException {
          return super.getPart(paramPath);
        }
      };
    }

    @Override
    public void setValue(RestClientRequest clientRequest, Object arg) throws Exception {
      @SuppressWarnings("unchecked")
      String[] filenames = (String[]) ((HttpServletRequest) arg).getAttribute(UPLOADS + paramPath);
      for (String filename : filenames) {
        clientRequest.attach(paramPath, filename);
      }
    }

    @Override
    public String getProcessorType() {
      return PARAMTYPE;
    }
  }
}
