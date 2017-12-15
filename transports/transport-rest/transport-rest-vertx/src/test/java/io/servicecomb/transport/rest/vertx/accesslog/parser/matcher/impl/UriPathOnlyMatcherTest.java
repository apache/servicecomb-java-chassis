package io.servicecomb.transport.rest.vertx.accesslog.parser.matcher.impl;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import io.servicecomb.transport.rest.vertx.accesslog.element.impl.UriPathOnlyElement;

public class UriPathOnlyMatcherTest {

  public static final UriPathOnlyMatcher MATCHER = new UriPathOnlyMatcher();

  @Test
  public void getPlaceholderPatterns() {
    String[] patterns = MATCHER.getPlaceholderPatterns();

    assertEquals(2, patterns.length);
    assertEquals("%U", patterns[0]);
    assertEquals("cs-uri-stem", patterns[1]);
  }

  @Test
  public void getAccessLogElement() {
    assertEquals(UriPathOnlyElement.class, MATCHER.getAccessLogElement().getClass());
  }
}