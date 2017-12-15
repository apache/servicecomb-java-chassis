package io.servicecomb.transport.rest.vertx.accesslog.element.impl;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.mockito.Mockito;

import io.servicecomb.transport.rest.vertx.accesslog.AccessLogParam;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.impl.headers.VertxHttpHeaders;
import io.vertx.ext.web.RoutingContext;

public class RequestHeaderElementTest {

  private static final String HEADER_IDENTIFIER = "headerIdentifier";

  private static final RequestHeaderElement ELEMENT = new RequestHeaderElement(HEADER_IDENTIFIER);

  @Test
  public void getFormattedElement() {
    RoutingContext mockContext = Mockito.mock(RoutingContext.class);
    AccessLogParam param = new AccessLogParam().setRoutingContext(mockContext);
    HttpServerRequest mockRequest = Mockito.mock(HttpServerRequest.class);
    VertxHttpHeaders headers = new VertxHttpHeaders();
    String testValue = "testValue";
    headers.add(HEADER_IDENTIFIER, testValue);

    Mockito.when(mockContext.request()).thenReturn(mockRequest);
    Mockito.when(mockRequest.headers()).thenReturn(headers);

    String result = ELEMENT.getFormattedElement(param);

    assertEquals(testValue, result);
  }

  @Test
  public void getFormattedElementIfHeaderIsNull() {
    RoutingContext mockContext = Mockito.mock(RoutingContext.class);
    AccessLogParam param = new AccessLogParam().setRoutingContext(mockContext);
    HttpServerRequest mockRequest = Mockito.mock(HttpServerRequest.class);

    Mockito.when(mockContext.request()).thenReturn(mockRequest);
    Mockito.when(mockRequest.headers()).thenReturn(null);

    String result = ELEMENT.getFormattedElement(param);

    assertEquals("-", result);
  }

  @Test
  public void getFormattedElementIfNotFound() {
    RoutingContext mockContext = Mockito.mock(RoutingContext.class);
    AccessLogParam param = new AccessLogParam().setRoutingContext(mockContext);
    HttpServerRequest mockRequest = Mockito.mock(HttpServerRequest.class);
    VertxHttpHeaders headers = new VertxHttpHeaders();
    String testValue = "testValue";
    headers.add("anotherHeader", testValue);

    Mockito.when(mockContext.request()).thenReturn(mockRequest);
    Mockito.when(mockRequest.headers()).thenReturn(headers);

    String result = ELEMENT.getFormattedElement(param);

    assertEquals("-", result);
  }
}