package io.servicecomb.transport.rest.vertx.accesslog.element.impl;

import static org.junit.Assert.*;

import org.junit.Test;
import org.mockito.Mockito;

import io.servicecomb.transport.rest.vertx.accesslog.AccessLogParam;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.http.impl.headers.VertxHttpHeaders;
import io.vertx.ext.web.RoutingContext;

public class ResponseHeaderElementTest {

  public static final String IDENTIFIER = "identifier";

  private static final ResponseHeaderElement ELEMENT = new ResponseHeaderElement(IDENTIFIER);

  @Test
  public void getFormattedElement() {
    AccessLogParam param = new AccessLogParam();
    RoutingContext mockContext = Mockito.mock(RoutingContext.class);
    HttpServerResponse mockResponse = Mockito.mock(HttpServerResponse.class);
    VertxHttpHeaders headers = new VertxHttpHeaders();
    String headerValue = "headerValue";

    param.setRoutingContext(mockContext);
    headers.add(IDENTIFIER, headerValue);

    Mockito.when(mockContext.response()).thenReturn(mockResponse);
    Mockito.when(mockResponse.headers()).thenReturn(headers);

    String result = ELEMENT.getFormattedElement(param);

    assertEquals(headerValue, result);
  }

  @Test
  public void getFormattedElementOnHeadersIsNull() {
    AccessLogParam param = new AccessLogParam();
    RoutingContext mockContext = Mockito.mock(RoutingContext.class);
    HttpServerResponse mockResponse = Mockito.mock(HttpServerResponse.class);

    param.setRoutingContext(mockContext);

    Mockito.when(mockContext.response()).thenReturn(mockResponse);

    String result = ELEMENT.getFormattedElement(param);

    assertEquals("-", result);
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
  public void getFormattedElementOnNotFound() {
    AccessLogParam param = new AccessLogParam();
    RoutingContext mockContext = Mockito.mock(RoutingContext.class);
    HttpServerResponse mockResponse = Mockito.mock(HttpServerResponse.class);
    VertxHttpHeaders headers = new VertxHttpHeaders();
    String headerValue = "headerValue";

    param.setRoutingContext(mockContext);
    headers.add("anotherHeader", headerValue);

    Mockito.when(mockContext.response()).thenReturn(mockResponse);
    Mockito.when(mockResponse.headers()).thenReturn(headers);

    String result = ELEMENT.getFormattedElement(param);

    assertEquals("-", result);
  }
}