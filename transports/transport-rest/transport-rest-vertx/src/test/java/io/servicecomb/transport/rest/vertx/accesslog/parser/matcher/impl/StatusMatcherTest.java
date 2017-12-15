package io.servicecomb.transport.rest.vertx.accesslog.parser.matcher.impl;

import static org.junit.Assert.*;

import org.junit.Test;

import io.servicecomb.transport.rest.vertx.accesslog.element.impl.StatusElement;

public class StatusMatcherTest {
  private static final StatusMatcher MATCHER = new StatusMatcher();

  @Test
  public void getPlaceholderPatterns() {
    String[] patterns = MATCHER.getPlaceholderPatterns();
    assertEquals(2, patterns.length);
    assertEquals("%s", patterns[0]);
    assertEquals("cs-status", patterns[1]);
  }

  @Test
  public void getAccessLogElement() {
    assertEquals(StatusElement.class, MATCHER.getAccessLogElement().getClass());
  }
}