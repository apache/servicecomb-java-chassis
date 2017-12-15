package io.servicecomb.transport.rest.vertx.accesslog.element.impl;

import static org.junit.Assert.*;

import org.junit.Test;
import org.mockito.Mockito;

import io.servicecomb.transport.rest.vertx.accesslog.AccessLogParam;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

public class StatusElementTest {

  public static final StatusElement STATUS_ELEMENT = new StatusElement();

  @Test
  public void getFormattedElement() {
    AccessLogParam param = new AccessLogParam();
    RoutingContext context = Mockito.mock(RoutingContext.class);
    HttpServerResponse response = Mockito.mock(HttpServerResponse.class);
    int statusCode = 200;

    param.setRoutingContext(context);
    Mockito.when(context.response()).thenReturn(response);
    Mockito.when(response.getStatusCode()).thenReturn(statusCode);

    String result = STATUS_ELEMENT.getFormattedElement(param);

    assertEquals("200", result);
  }


  @Test
  public void getFormattedElementOnResponseIsNull() {
    AccessLogParam param = new AccessLogParam();
    RoutingContext context = Mockito.mock(RoutingContext.class);

    param.setRoutingContext(context);
    Mockito.when(context.response()).thenReturn(null);

    String result = STATUS_ELEMENT.getFormattedElement(param);

    assertEquals("-", result);
  }
}