package io.servicecomb.common.rest.codec;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.fasterxml.jackson.databind.JavaType;

import io.servicecomb.common.rest.codec.param.AbstractParamProcessor;
import io.servicecomb.common.rest.codec.param.ParamValueProcessor;

public class QueryProcessorFactory {

  public static final String PARAMTYPE = "query";

  private static Map strategyMap = new HashMap<>();

  public QueryProcessorFactory() {
  }

  public QueryProcessorFactory(String paramPath, JavaType targetType) {

    strategyMap.put(QueryTypeEnum.multi.value(), new MultiQueryProcessor(paramPath, targetType));
    strategyMap.put(QueryTypeEnum.csv.value(), new CsvQueryProcessor(paramPath, targetType));
  }

  private static QueryProcessorFactory factory = new QueryProcessorFactory();

  public static QueryProcessorFactory getInstance() {
    return factory;
  }

  public ParamValueProcessor creator(Integer type) {
    return (ParamValueProcessor) strategyMap.get(type);
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
        for (String str : strs) {
          value = Arrays.asList(str.split(","));
        }
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
