package io.servicecomb.swagger.generator.springmvc.processor.annotation;


import javax.ws.rs.QueryParam;

import io.servicecomb.swagger.generator.core.processor.parameter.AbstractParameterProcessor;
import io.swagger.annotations.ApiParam;
import io.swagger.models.parameters.QueryParameter;

public class ApiParamAnnotationProcessor extends AbstractParameterProcessor<QueryParameter> {
  @Override
  protected QueryParameter createParameter() {
    return new QueryParameter();
  }

  @Override
  protected String getAnnotationParameterName(Object annotation) {
    return ((ApiParam) annotation).value();
  }

  @Override
  protected String getAnnotationParameterColFmt(Object annotation) {
    return ((ApiParam) annotation).collectionFormat();
  }
}
