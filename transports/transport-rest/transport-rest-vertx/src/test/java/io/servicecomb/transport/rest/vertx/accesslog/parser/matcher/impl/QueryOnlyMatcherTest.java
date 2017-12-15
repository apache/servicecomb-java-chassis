package io.servicecomb.transport.rest.vertx.accesslog.parser.matcher.impl;

import static org.junit.Assert.*;

import org.junit.Test;

import io.servicecomb.transport.rest.vertx.accesslog.element.impl.QueryOnlyElement;

public class QueryOnlyMatcherTest {

  public static final QueryOnlyMatcher MATCHER = new QueryOnlyMatcher();

  @Test
  public void getPlaceholderPatterns() {
    String[] patterns = MATCHER.getPlaceholderPatterns();

    assertEquals(2, patterns.length);
    assertEquals("%q", patterns[0]);
    assertEquals("cs-uri-query", patterns[1]);
  }

  @Test
  public void getAccessLogElement() {
    assertEquals(QueryOnlyElement.class, MATCHER.getAccessLogElement().getClass());
  }
}