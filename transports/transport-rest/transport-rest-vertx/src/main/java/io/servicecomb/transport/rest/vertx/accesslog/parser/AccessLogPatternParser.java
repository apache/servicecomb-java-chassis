package io.servicecomb.transport.rest.vertx.accesslog.parser;

import java.util.List;

public interface AccessLogPatternParser {
  List<AccessLogElementExtraction> parsePattern(String rawPattern);
}
