package io.servicecomb.transport.rest.vertx.accesslog.element.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Assert;
import org.junit.Test;

import io.servicecomb.transport.rest.vertx.accesslog.AccessLogParam;

public class DurationSecondElementTest {

  public static final DurationSecondElement ELEMENT = new DurationSecondElement();

  @Test
  public void getFormattedElementOn999ms() {
    AccessLogParam param = new AccessLogParam().setStartMillisecond(1L).setEndMillisecond(1000L);

    String result = ELEMENT.getFormattedElement(param);

    assertEquals("0", result);
  }

  @Test
  public void getFormattedElementOn1000ms() {
    AccessLogParam param = new AccessLogParam().setStartMillisecond(1L).setEndMillisecond(1001L);

    String result = ELEMENT.getFormattedElement(param);

    assertEquals("1", result);
  }

  @Test
  public void getFormattedElementOn1001ms() {
    AccessLogParam param = new AccessLogParam().setStartMillisecond(1L).setEndMillisecond(1002L);

    String result = ELEMENT.getFormattedElement(param);

    assertEquals("1", result);
  }
}