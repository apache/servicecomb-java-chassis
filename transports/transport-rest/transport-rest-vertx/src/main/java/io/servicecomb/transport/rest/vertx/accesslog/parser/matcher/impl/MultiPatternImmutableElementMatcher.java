package io.servicecomb.transport.rest.vertx.accesslog.parser.matcher.impl;

import java.util.ArrayList;
import java.util.List;

import io.servicecomb.transport.rest.vertx.accesslog.parser.AccessLogElementExtraction;

public abstract class MultiPatternImmutableElementMatcher extends ImmutableAccessLogElementMatcher {
  @Override
  public List<AccessLogElementExtraction> extractElementPlaceholder(String rawPattern) {
    List<AccessLogElementExtraction> extractionList = new ArrayList<>();

    String[] patterns = getPlaceholderPatterns();
    for (String pattern : patterns) {
      matchElementPlaceholder(rawPattern, pattern, extractionList);
    }

    return extractionList;
  }

  protected abstract String[] getPlaceholderPatterns();
}
