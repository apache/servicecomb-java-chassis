package io.servicecomb.transport.rest.vertx.accesslog.element.impl;

import static org.junit.Assert.*;

import org.junit.Test;
import org.mockito.Mockito;

import io.servicecomb.transport.rest.vertx.accesslog.AccessLogParam;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;

public class QueryOnlyElementTest {

  @Test
  public void getFormattedElement() {
    AccessLogParam param = new AccessLogParam();
    RoutingContext context = Mockito.mock(RoutingContext.class);
    HttpServerRequest request = Mockito.mock(HttpServerRequest.class);
    String query = "?status=up";

    param.setRoutingContext(context);
    Mockito.when(context.request()).thenReturn(request);
    Mockito.when(request.query()).thenReturn(query);

    String result = new QueryOnlyElement().getFormattedElement(param);

    assertEquals(query, result);
  }

  @Test
  public void getFormattedElementOnRequestIsNull() {
    AccessLogParam param = new AccessLogParam();
    RoutingContext context = Mockito.mock(RoutingContext.class);

    param.setRoutingContext(context);
    Mockito.when(context.request()).thenReturn(null);

    String result = new QueryOnlyElement().getFormattedElement(param);

    assertEquals("-", result);
  }

  @Test
  public void getFormattedElementOnQueryIsNull() {
    AccessLogParam param = new AccessLogParam();
    RoutingContext context = Mockito.mock(RoutingContext.class);
    HttpServerRequest request = Mockito.mock(HttpServerRequest.class);

    param.setRoutingContext(context);
    Mockito.when(context.request()).thenReturn(request);
    Mockito.when(request.query()).thenReturn(null);

    String result = new QueryOnlyElement().getFormattedElement(param);

    assertEquals("-", result);
  }

  @Test
  public void getFormattedElementOnQueryIsEmpty() {
    AccessLogParam param = new AccessLogParam();
    RoutingContext context = Mockito.mock(RoutingContext.class);
    HttpServerRequest request = Mockito.mock(HttpServerRequest.class);
    String query = "";

    param.setRoutingContext(context);
    Mockito.when(context.request()).thenReturn(request);
    Mockito.when(request.query()).thenReturn(query);

    String result = new QueryOnlyElement().getFormattedElement(param);

    assertEquals("-", result);
  }
}