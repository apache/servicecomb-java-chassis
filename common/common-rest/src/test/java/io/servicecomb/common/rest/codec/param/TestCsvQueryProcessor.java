package io.servicecomb.common.rest.codec.param;

import javax.servlet.http.HttpServletRequest;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.type.TypeFactory;

import io.servicecomb.common.rest.codec.QueryProcessorFactory.CsvQueryProcessor;
import mockit.Expectations;
import mockit.Mocked;

public class TestCsvQueryProcessor {
  @Mocked
  HttpServletRequest request;

  private ParamValueProcessor createProcessor(String name, Class<?> type) {
    return new CsvQueryProcessor(name, TypeFactory.defaultInstance().constructType(type));
  }

  @Test
  public void testGetValueNormal() throws Exception {
    new Expectations() {
      {
        request.getParameter("name");
        result = "value";
      }
    };

    ParamValueProcessor processor = createProcessor("name", String.class);
    Object value = processor.getValue(request);
    Assert.assertEquals("value", value);
  }

  @Test
  public void testGetValueContainerType() throws Exception {
    new Expectations() {
      {
        request.getParameterValues("name");
        result = new String[] {"v1,v2,v3"};
      }
    };

    ParamValueProcessor processor = createProcessor("name", String[].class);
    String[] value = (String[]) processor.getValue(request);
    Assert.assertThat(value, Matchers.arrayContaining("v1","v2","v3"));
  }

  @Test
  public void testGetProcessorType() {
    ParamValueProcessor processor = createProcessor("name", String.class);
    Assert.assertEquals("query", processor.getProcessorType());
  }
}
