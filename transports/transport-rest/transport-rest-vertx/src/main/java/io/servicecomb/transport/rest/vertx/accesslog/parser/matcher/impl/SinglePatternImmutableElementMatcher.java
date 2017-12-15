package io.servicecomb.transport.rest.vertx.accesslog.parser.matcher.impl;

import java.util.ArrayList;
import java.util.List;

import io.servicecomb.transport.rest.vertx.accesslog.parser.AccessLogElementExtraction;

public abstract class SinglePatternImmutableElementMatcher extends ImmutableAccessLogElementMatcher {
  @Override
  public List<AccessLogElementExtraction> extractElementPlaceholder(String rawPattern) {
    final String pattern = getPlaceholderPattern();
    List<AccessLogElementExtraction> extractionList = new ArrayList<>();

    matchElementPlaceholder(rawPattern, pattern, extractionList);

    return extractionList;
  }

  protected abstract String getPlaceholderPattern();
}
