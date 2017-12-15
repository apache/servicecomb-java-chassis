package io.servicecomb.transport.rest.vertx.accesslog.element.impl;

import static org.junit.Assert.*;

import org.junit.Test;

public class PlainTextElementTest {

  @Test
  public void getFormattedElement() {
    PlainTextElement element = new PlainTextElement("contentTest");
    assertEquals("contentTest", element.getFormattedElement(null));
  }
}