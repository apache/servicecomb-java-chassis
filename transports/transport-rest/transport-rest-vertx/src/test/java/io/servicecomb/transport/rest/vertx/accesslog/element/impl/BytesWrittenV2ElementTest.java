package io.servicecomb.transport.rest.vertx.accesslog.element.impl;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.mockito.Mockito;

import io.servicecomb.transport.rest.vertx.accesslog.AccessLogParam;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

public class BytesWrittenV2ElementTest {

  public static final BytesWrittenV2Element ELEMENT = new BytesWrittenV2Element();

  @Test
  public void getFormattedElement() {
    AccessLogParam param = new AccessLogParam();
    RoutingContext mockContext = Mockito.mock(RoutingContext.class);
    HttpServerResponse mockResponse = Mockito.mock(HttpServerResponse.class);
    long bytesWritten = 16L;

    param.setRoutingContext(mockContext);
    Mockito.when(mockContext.response()).thenReturn(mockResponse);
    Mockito.when(mockResponse.bytesWritten()).thenReturn(bytesWritten);

    String result = ELEMENT.getFormattedElement(param);

    assertEquals(String.valueOf(bytesWritten), result);
  }


  @Test
  public void getFormattedElementOnResponseIsNull() {
    AccessLogParam param = new AccessLogParam();
    RoutingContext mockContext = Mockito.mock(RoutingContext.class);

    param.setRoutingContext(mockContext);
    Mockito.when(mockContext.response()).thenReturn(null);

    String result = ELEMENT.getFormattedElement(param);

    assertEquals("-", result);
  }


  @Test
  public void getFormattedElementOnBytesWrittenIsZero() {
    AccessLogParam param = new AccessLogParam();
    RoutingContext mockContext = Mockito.mock(RoutingContext.class);
    HttpServerResponse mockResponse = Mockito.mock(HttpServerResponse.class);
    long bytesWritten = 0l;

    param.setRoutingContext(mockContext);
    Mockito.when(mockContext.response()).thenReturn(mockResponse);
    Mockito.when(mockResponse.bytesWritten()).thenReturn(bytesWritten);

    String result = ELEMENT.getFormattedElement(param);
    assertEquals("-", result);
  }
}