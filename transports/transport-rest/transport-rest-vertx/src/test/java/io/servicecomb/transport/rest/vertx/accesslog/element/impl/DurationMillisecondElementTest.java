package io.servicecomb.transport.rest.vertx.accesslog.element.impl;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import io.servicecomb.transport.rest.vertx.accesslog.AccessLogParam;

public class DurationMillisecondElementTest {

  public static final DurationMillisecondElement ELEMENT = new DurationMillisecondElement();

  @Test
  public void getFormattedElement() {
    AccessLogParam param = new AccessLogParam().setStartMillisecond(1L).setEndMillisecond(2L);

    String result = ELEMENT.getFormattedElement(param);

    assertEquals("1", result);
  }
}