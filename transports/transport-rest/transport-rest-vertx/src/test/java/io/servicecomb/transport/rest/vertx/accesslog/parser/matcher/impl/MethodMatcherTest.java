package io.servicecomb.transport.rest.vertx.accesslog.parser.matcher.impl;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.List;

import org.junit.Test;

import io.servicecomb.transport.rest.vertx.accesslog.parser.AccessLogElementExtraction;
import io.servicecomb.transport.rest.vertx.accesslog.parser.impl.DefaultAccessLogPatternParser;

public class MethodMatcherTest {
  private static final MethodMatcher MATCHER = new MethodMatcher();

  private static final String RAW_PATTERN = "cs-method %s cs-method %T %m";

  @Test
  public void extractElementPlaceHolder() {
    List<AccessLogElementExtraction> extractionList = MATCHER.extractElementPlaceholder(RAW_PATTERN);
    Collections.sort(extractionList, DefaultAccessLogPatternParser.ACCESS_LOG_ELEMENT_EXTRACTION_COMPARATOR);
    assertEquals(3, extractionList.size());
    assertEquals(0, extractionList.get(0).getStart());
    assertEquals(9, extractionList.get(0).getEnd());
    assertEquals(13, extractionList.get(1).getStart());
    assertEquals(22, extractionList.get(1).getEnd());
    assertEquals(26, extractionList.get(2).getStart());
    assertEquals(28, extractionList.get(2).getEnd());
  }
}